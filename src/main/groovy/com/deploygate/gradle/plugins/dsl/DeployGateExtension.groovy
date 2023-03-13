package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.syntax.ExtensionSyntax
import com.deploygate.gradle.plugins.internal.annotation.DeployGateInternal
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.NotifyActionRequest
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

    @Nonnull
    private final CliCredentialStore credentialStore;

    DeployGateExtension(@Nonnull Project project, @Nonnull NamedDomainObjectContainer<NamedDeployment> deployments, @Nonnull CliCredentialStore credentialStore) {
        this.project = project
        this.deployments = deployments
        this.credentialStore = credentialStore

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

    @DeployGateInternal
    @Deprecated
    String getEndpoint() {
        return ApiClient.endpoint
    }

    @DeployGateInternal
    @Deprecated
    void setEndpoint(String value) {
        ApiClient.endpoint = value
    }

    /**
     * Notify the plugin's action to the server. Never throw any exception.
     *
     * @param action an action name in plugin lifecycle.
     * @param data a map of key-values
     * @return true if the request has been processed regardless of its result, otherwise false.
     */
    @DeployGateInternal
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

    @DeployGateInternal
    @Nonnull
    CliCredentialStore getCredentialStore() {
        return credentialStore
    }
}
