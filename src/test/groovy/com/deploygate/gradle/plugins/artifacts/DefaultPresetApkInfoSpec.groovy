package com.deploygate.gradle.plugins.artifacts

import spock.lang.Specification
import spock.lang.Unroll

class DefaultPresetApkInfoSpec extends Specification {

    @Unroll
    def "given arguments should be returned as it is. Unrolled #variantName"() {
        expect:
        def presetApkInfo = new DefaultPresetApkInfo(variantName)
        presetApkInfo.variantName == variantName
        presetApkInfo.apkFile == null
        presetApkInfo.signingReady
        presetApkInfo.universalApk

        where:
        variantName << ["foo", "bar", "baz", "que"]
    }

    def "variantName must be non-null"() {
        when:
        new DefaultPresetApkInfo(null)

        then:
        thrown(IllegalArgumentException)
    }
}
