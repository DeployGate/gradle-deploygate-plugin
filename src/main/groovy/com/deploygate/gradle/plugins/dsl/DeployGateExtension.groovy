package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.dsl.syntax.ExtensionSyntax
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import com.google.common.annotations.VisibleForTesting
import groovyx.net.http.ContentType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import javax.annotation.Nonnull
import javax.annotation.Nullable

class DeployGateExtension implements ExtensionSyntax {
    String apiToken

    String appOwnerName

    @Deprecated
    String endpoint = Config.DEPLOYGATE_ROOT

    @Deprecated
    String notifyKey = null

    @Nonnull
    private final Project project

    @Nonnull
    private final NamedDomainObjectContainer<NamedDeployment> variantConfigurations

    DeployGateExtension(@Nonnull Project project, @Nonnull NamedDomainObjectContainer<NamedDeployment> variantConfigurations) {
        this.project = project
        this.variantConfigurations = variantConfigurations
    }

    // backward compatibility

    @Deprecated
    void setToken(String token) {
        setApiToken(token)
    }

    @Deprecated
    void setUserName(String userName) {
        setAppOwnerName(userName)
    }

    /**
     * @depreacted use getDeployments instead
     * @see DeployGateExtension#getDeployments()
     */
    @Deprecated
    def getApks() {
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
        return variantConfigurations
    }

    @Override
    void deployments(Closure closure) {
        deployments.configure(closure)
    }

    boolean hasDeployment(@Nonnull String name) {
        return variantConfigurations.findByName(name)
    }

    @Nonnull
    NamedDeployment findDeploymentByName(@Nonnull String name) {
        def result = new NamedDeployment(name)
        NamedDeployment declaredTarget = variantConfigurations.findByName(name)

        if (declaredTarget) {
            mergeDeployments(result, declaredTarget)
        }

        mergeDeployments(result, getEnvironmentBasedDeployment(project))

        return result
    }

    @VisibleForTesting
    static void mergeDeployments(@Nonnull NamedDeployment base, @Nullable NamedDeployment other) {
        base.sourceFile = base.sourceFile ?: other.sourceFile
        base.uploadMessage = base.uploadMessage ?: other.uploadMessage
        base.distributionKey = base.distributionKey ?: other.distributionKey
        base.releaseNote = base.releaseNote ?: other.releaseNote
        base.visibility = base.visibility ?: other.visibility
        base.skipAssemble = base.skipAssemble || other.skipAssemble
    }

    @VisibleForTesting
    static NamedDeployment getEnvironmentBasedDeployment(Project project) {
        File sourceFile = System.getenv(DeployGatePlugin.ENV_NAME_SOURCE_FILE)?.with { it -> project.file(it) }
        String uploadMessage = System.getenv(DeployGatePlugin.ENV_NAME_UPLOAD_MESSAGE)
        String distributionKey = System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY)
        String releaseNote = System.getenv(DeployGatePlugin.ENV_NAME_RELEASE_NOTE)
        String visibility = System.getenv(DeployGatePlugin.ENV_NAME_VISIBILITY)

        def target = new NamedDeployment("")

        target.sourceFile = sourceFile
        target.uploadMessage = uploadMessage
        target.distributionKey = distributionKey
        target.releaseNote = releaseNote
        target.visibility = visibility

        return target
    }

    def notifyServer(String action, HashMap<String, String> data = null) {
        if (!notifyKey) {
            return
        }

        def query = ['key': notifyKey, 'command_action': action]

        if (data) {
            query = query + data
        }

        try {
            HTTPBuilderFactory.httpBuilder(endpoint).post path: "/cli/notify",
                    body: query, requestContentType: ContentType.URLENC
        } catch (ignored) {
        }
    }
}
