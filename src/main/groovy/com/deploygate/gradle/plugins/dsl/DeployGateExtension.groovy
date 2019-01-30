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
    String userName
    String endpoint = Config.DEPLOYGATE_ROOT

    NamedDomainObjectContainer<DeployTarget> apks
    String notifyKey = null

    @Nonnull
    private final Project project

    DeployGateExtension(@Nonnull Project project, NamedDomainObjectContainer<DeployTarget> apkTargets) {
        this.project = project
        this.apks = apkTargets
    }

    def apks(Closure closure) {
        apks.configure(closure)
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
        return apks.findByName(name)
    }

    DeployTarget findDeployTarget(@Nonnull String name) {
        def result = new DeployTarget(name)
        def defaultTarget = DeployTarget.getDefaultDeployTarget(project)
        DeployTarget declaredTarget = apks.findByName(name)

        if (declaredTarget) {
            mergeDeployTarget(result, declaredTarget)
        }

        mergeDeployTarget(result, defaultTarget)

        return result
    }

    static void mergeDeployTarget(@Nonnull DeployTarget base, @Nullable DeployTarget other) {
        base.sourceFile = base.sourceFile ?: other.sourceFile
        base.message = base.message ?: other.message
        base.distributionKey = base.distributionKey ?: other.distributionKey
        base.releaseNote = base.releaseNote ?: other.releaseNote
        base.visibility = base.visibility ?: other.visibility
    }
}
