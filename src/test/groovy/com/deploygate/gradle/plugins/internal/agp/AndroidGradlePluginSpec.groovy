package com.deploygate.gradle.plugins.internal.agp

import spock.lang.Specification
import spock.lang.Unroll

class AndroidGradlePluginSpec extends Specification {

    @Unroll
    def "isInternalSigningConfigData returns #expected for AGP #agpVersion"() {
        expect:
        AndroidGradlePlugin.isInternalSigningConfigData(agpVersion) == expected

        where:
        agpVersion | expected
        "8.2.0"    | false
        "8.3.0"    | true
        "8.7.0"    | true
        // major version bumps must keep the threshold semantics (regression guard for AGP 9.x)
        "9.0.0"    | true
        "9.2.0"    | true
        "10.0.0"   | true
    }

    @Unroll
    def "hasOutputsHandlerApiOnPackageApplication returns #expected for AGP #agpVersion"() {
        expect:
        AndroidGradlePlugin.hasOutputsHandlerApiOnPackageApplication(agpVersion) == expected

        where:
        agpVersion | expected
        "8.0.0"    | false
        "8.1.0"    | true
        "8.7.0"    | true
        // major version bumps must keep the threshold semantics (regression guard for AGP 9.x)
        "9.0.0"    | true
        "9.2.0"    | true
        "10.0.0"   | true
    }
}
