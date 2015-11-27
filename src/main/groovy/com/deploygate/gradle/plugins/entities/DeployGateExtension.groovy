package com.deploygate.gradle.plugins.entities

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.utils.UrlUtils
import org.gradle.api.NamedDomainObjectContainer

public class DeployGateExtension {
    String token
    String userName
    NamedDomainObjectContainer<DeployTarget> apks

    String notifyKey = null

    public DeployGateExtension(NamedDomainObjectContainer<DeployTarget> apkTargets) {
        this.apks = apkTargets
    }

    public apks(Closure closure) {
        apks.configure(closure)
    }

    def notifyServer(String action, HashMap<String, String> data = null) {
        if (!notifyKey)
            return

        def query = [ 'key': notifyKey, 'command_action': action ]
        if (data)
            query = query + data

        try {
            new URL("${Config.DEPLOYGATE_HOST}/cli/notify?${UrlUtils.toQueryString(query)}").getText()
        } catch (e) {}
    }
}
