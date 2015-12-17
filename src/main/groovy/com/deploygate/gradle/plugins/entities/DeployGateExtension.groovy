package com.deploygate.gradle.plugins.entities

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovyx.net.http.ContentType
import org.gradle.api.NamedDomainObjectContainer

public class DeployGateExtension {
    def String token
    def String userName
    def String endpoint = Config.DEPLOYGATE_ROOT

    def NamedDomainObjectContainer<DeployTarget> apks
    def String notifyKey = null

    public DeployGateExtension(NamedDomainObjectContainer<DeployTarget> apkTargets) {
        this.apks = apkTargets
    }

    public apks(Closure closure) {
        apks.configure(closure)
    }

    def notifyServer(String action, HashMap<String, String> data = null) {
        if (!notifyKey)
            return

        def query = ['key': notifyKey, 'command_action': action]
        if (data)
            query = query + data

        try {
            HTTPBuilderFactory.httpBuilder(endpoint).post path: "/cli/notify",
                    body: query, requestContentType: ContentType.URLENC
        } catch (ignored) {
        }
    }
}
