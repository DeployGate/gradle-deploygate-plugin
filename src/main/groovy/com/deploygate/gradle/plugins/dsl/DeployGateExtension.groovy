package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovy.transform.PackageScope
import groovyx.net.http.ContentType
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

import javax.annotation.Nonnull
import javax.annotation.Nullable

class DeployGateExtension {
    String token

    @Deprecated
    String userName

    String appOwnerName

    @Deprecated
    String endpoint = Config.DEPLOYGATE_ROOT

    @Deprecated
    String notifyKey = null

    @Nonnull
    private final Project project

    @Nonnull
    private final NamedDomainObjectContainer<VariantBasedDeployTargetImpl> variantConfigurations

    DeployGateExtension(@Nonnull Project project, NamedDomainObjectContainer<VariantBasedDeployTargetImpl> variantConfigurations) {
        this.project = project
        this.variantConfigurations = variantConfigurations
    }

    @Deprecated
    def apks(Closure closure) {
        deployments(closure)
    }

    def deployments(Closure closure) {
        variantConfigurations.configure(closure)
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

    boolean hasDeployTarget(@Nonnull String name) {
        return variantConfigurations.findByName(name)
    }

    VariantBasedDeployTargetImpl findDeployTarget(@Nonnull String name) {
        def result = new VariantBasedDeployTargetImpl(name)
        def defaultTarget = getDefaultDeployTarget(project)
        VariantBasedDeployTarget declaredTarget = variantConfigurations.findByName(name)

        if (declaredTarget) {
            mergeDeployTarget(result, declaredTarget)
        }

        mergeDeployTarget(result, defaultTarget)

        return result
    }

    @PackageScope
    static void mergeDeployTarget(@Nonnull VariantBasedDeployTargetImpl base, @Nullable VariantBasedDeployTarget other) {
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

        return new VariantBasedDeployTargetImpl(
                sourceFile: sourceFile,
                uploadMessage: uploadMessage,
                distributionKey: distributionKey,
                releaseNote: releaseNote,
                visibility: visibility,
                skipAssemble: false,
        )
    }
}
