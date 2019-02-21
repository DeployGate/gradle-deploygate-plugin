package com.deploygate.gradle.plugins.tasks.factory

import spock.lang.Specification

class DeployGateTaskFactorySpec extends Specification {

    def "uploadApkTaskName should return #taskName"() {
        expect:
        DeployGateTaskFactory.uploadApkTaskName(variantName) == taskName

        where:
        variantName | taskName
        "dep1"      | "uploadDeployGateDep1"
        "dep2"      | "uploadDeployGateDep2"
        "dep3Dep4"  | "uploadDeployGateDep3Dep4"
    }
}
