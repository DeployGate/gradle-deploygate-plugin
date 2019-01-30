package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
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
    private final NamedDomainObjectContainer<VariantBasedDeployTarget> variantConfigurations

    DeployGateExtension(@Nonnull Project project, NamedDomainObjectContainer<VariantBasedDeployTarget> variantConfigurations) {
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

    VariantBasedDeployTarget findDeployTarget(@Nonnull String name) {
        def result = new VariantBasedDeployTarget(name)
        def defaultTarget = VariantBasedDeployTarget.getDefaultDeployTarget(project)
        VariantBasedDeployTarget declaredTarget = variantConfigurations.findByName(name)

        if (declaredTarget) {
            mergeDeployTarget(result, declaredTarget)
        }

        mergeDeployTarget(result, defaultTarget)

        return result
    }

    static void mergeDeployTarget(@Nonnull VariantBasedDeployTarget base, @Nullable VariantBasedDeployTarget other) {
        base.sourceFile = base.sourceFile ?: other.sourceFile
        base.message = base.message ?: other.message
        base.distributionKey = base.distributionKey ?: other.distributionKey
        base.releaseNote = base.releaseNote ?: other.releaseNote
        base.visibility = base.visibility ?: other.visibility
    }
}
