package com.deploygate.gradle.plugins.entities

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovyx.net.http.ContentType
import org.gradle.api.NamedDomainObjectContainer

class DeployGateExtension {
    String token
    String userName
    String endpoint = Config.DEPLOYGATE_ROOT

    NamedDomainObjectContainer<DeployTarget> apks
    String notifyKey = null

    DeployGateExtension(NamedDomainObjectContainer<DeployTarget> apkTargets) {
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
}
