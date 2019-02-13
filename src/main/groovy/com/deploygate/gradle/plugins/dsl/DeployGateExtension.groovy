package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.dsl.syntax.ExtensionSyntax
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovy.transform.PackageScope
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
    private final NamedDomainObjectContainer<VariantBasedDeployTarget> variantConfigurations

    DeployGateExtension(@Nonnull Project project, @Nonnull NamedDomainObjectContainer<VariantBasedDeployTarget> variantConfigurations) {
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
     * @depreacted use {@link DeployGateExtension#getDeployments()} instead
     * @return
     */
    @Deprecated
    def getApks() {
        return variantConfigurations
    }

    // end: backward compatibility

    @Nonnull
    @Override
    NamedDomainObjectContainer<VariantBasedDeployTarget> getDeployments() {
        return variantConfigurations
    }

    boolean hasDeployTarget(@Nonnull String name) {
        return variantConfigurations.findByName(name)
    }

    @Nonnull
    VariantBasedDeployTarget findDeployTarget(@Nonnull String name) {
        def result = new VariantBasedDeployTarget(name)
        def defaultTarget = getDefaultDeployTarget(project)
        VariantBasedDeployTarget declaredTarget = variantConfigurations.findByName(name)

        if (declaredTarget) {
            mergeDeployTarget(result, declaredTarget)
        }

        mergeDeployTarget(result, defaultTarget)

        return result
    }

    @PackageScope
    static void mergeDeployTarget(@Nonnull VariantBasedDeployTarget base, @Nullable VariantBasedDeployTarget other) {
        base.sourceFile = base.sourceFile ?: other.sourceFile
        base.uploadMessage = base.uploadMessage ?: other.uploadMessage
        base.distributionKey = base.distributionKey ?: other.distributionKey
        base.releaseNote = base.releaseNote ?: other.releaseNote
        base.visibility = base.visibility ?: other.visibility
    }

    @PackageScope
    static VariantBasedDeployTarget getDefaultDeployTarget(Project project) {
        File sourceFile = System.getenv(DeployGatePlugin.ENV_NAME_SOURCE_FILE)?.with { project.file(this) }
        String uploadMessage = System.getenv(DeployGatePlugin.ENV_NAME_UPLOAD_MESSAGE)
        String distributionKey = System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY)
        String releaseNote = System.getenv(DeployGatePlugin.ENV_NAME_RELEASE_NOTE)
        String visibility = System.getenv(DeployGatePlugin.ENV_NAME_VISIBILITY)

        def target = new VariantBasedDeployTarget("")

        target.with {
            sourceFile = System.getenv(DeployGatePlugin.ENV_NAME_SOURCE_FILE)?.with { project.file(this) }
            uploadMessage = System.getenv(DeployGatePlugin.ENV_NAME_UPLOAD_MESSAGE)
            distributionKey = System.getenv(DeployGatePlugin.ENV_NAME_DISTRIBUTION_KEY)
            releaseNote = System.getenv(DeployGatePlugin.ENV_NAME_RELEASE_NOTE)
            visibility = System.getenv(DeployGatePlugin.ENV_NAME_VISIBILITY)
            skipAssemble = false
        }

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
