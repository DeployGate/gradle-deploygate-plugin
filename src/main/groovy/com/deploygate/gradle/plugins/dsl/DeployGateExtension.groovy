package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.syntax.ExtensionSyntax
import com.deploygate.gradle.plugins.internal.annotation.Internal
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.NotifyActionRequest
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import javax.annotation.Nonnull
import javax.annotation.Nullable

class DeployGateExtension implements ExtensionSyntax {
    String apiToken

    String appOwnerName

    @Deprecated
    String notifyKey = null

    @Nonnull
    private final Project project

    @Nonnull
    private final NamedDomainObjectContainer<NamedDeployment> deployments

    DeployGateExtension(@Nonnull Project project, @Nonnull NamedDomainObjectContainer<NamedDeployment> deployments, @Nonnull CliCredentialStore credentialStore) {
        this.project = project
        this.deployments = deployments

        this.appOwnerName = [System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME), System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME_V1), credentialStore.name].find {
            it != null
        }

        this.apiToken = [System.getenv(DeployGatePlugin.ENV_NAME_API_TOKEN), credentialStore.token].find {
            it != null
        }
    }

    // backward compatibility

    @Nullable
    String getToken() {
        return getApiToken()
    }

    @Deprecated
    void setToken(@Nullable String token) {
        setApiToken(token)
    }

    @Nullable
    String getUserName() {
        return getAppOwnerName()
    }

    @Deprecated
    void setUserName(@Nullable String userName) {
        setAppOwnerName(userName)
    }

    /**
     * @depreacted use getDeployments instead
     * @see DeployGateExtension#getDeployments()
     */
    @Deprecated
    NamedDomainObjectContainer<NamedDeployment> getApks() {
        return getDeployments()
    }

    /**
     * @depreacted use deployments instead
     * @see DeployGateExtension#deployments(Closure)
     */
    @Deprecated
    def apks(Closure closure) {
        deployments(closure)
    }

    // end: backward compatibility

    @Nonnull
    @Override
    NamedDomainObjectContainer<NamedDeployment> getDeployments() {
        return deployments
    }

    @Override
    void deployments(Closure closure) {
        deployments.configure(closure)
    }

    boolean hasDeployment(@Nonnull String name) {
        return deployments.findByName(name)
    }

    @Nonnull
    NamedDeployment findDeploymentByName(@Nonnull String name) {
        def result = new NamedDeployment(name)
        NamedDeployment declaredTarget = deployments.findByName(name)

        if (declaredTarget) {
            mergeDeployments(result, declaredTarget)
        }

        mergeDeployments(result, getEnvironmentBasedDeployment(project))

        return result
    }

    @Internal
    @Deprecated
    String getEndpoint() {
        return ApiClient.endpoint
    }

    @Internal
    @Deprecated
    void setEndpoint(String value) {
        ApiClient.endpoint = value
    }

    @VisibleForTesting
    static void mergeDeployments(@Nonnull NamedDeployment base, @Nullable NamedDeployment other) {
        base.sourceFile = base.sourceFile ?: other.sourceFile
        base.message = base.message ?: other.message
        base._internalSetVisibility(base._internalGetVisibility() ?: other._internalGetVisibility())
        base.skipAssemble = base.skipAssemble || other.skipAssemble
        base.distribution.merge(other.distribution)
    }

    @VisibleForTesting
    static NamedDeployment getEnvironmentBasedDeployment(Project project) {
        File sourceFile = System.getenv(DeployGatePlugin.ENV_NAME_SOURCE_FILE)?.with { it -> project.file(it) }
        String message = System.getenv(DeployGatePlugin.ENV_NAME_MESSAGE)
        String distributionKey = System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY)
        String distributionReleaseNote = [
                System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_RELEASE_NOTE),
                System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_RELEASE_NOTE_V1)
        ].find { it }
        String visibility = System.getenv(DeployGatePlugin.ENV_NAME_APP_VISIBILITY)

        def deployment = new NamedDeployment("environment-based")

        deployment.sourceFile = sourceFile
        deployment.message = message
        deployment.distribution { Distribution distribution ->
            distribution.key = distributionKey
            distribution.releaseNote = distributionReleaseNote
        }
        if (visibility) {
            deployment.visibility = visibility
        }

        return deployment
    }

    /**
     * Notify the plugin's action to the server. Never throw any exception.
     *
     * @param action an action name in plugin lifecycle.
     * @param data a map of key-values
     * @return true if the request has been processed regardless of its result, otherwise false.
     */
    @Internal
    boolean notifyServer(String action, HashMap<String, String> data = null) {
        if (!notifyKey) {
            return false
        }

        def request = new NotifyActionRequest(notifyKey, action)

        if (data) {
            data.each {
                request.setParameter(it.key, it.value)
            }
        }

        try {
            ApiClient.getInstance().notify(request)
        } catch (Throwable ignore) {
        }

        return true
    }
}
