package com.deploygate.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

public class DeployGateExtension {
    String token
    String userName
    NamedDomainObjectContainer<ApkTarget> apks

    public DeployGateExtension(NamedDomainObjectContainer<ApkTarget> apkTargets) {
        this.apks = apkTargets
    }

    public apks(Closure closure) {
        apks.configure(closure)
    }
}
