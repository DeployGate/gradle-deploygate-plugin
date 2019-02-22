package com.deploygate.gradle.plugins.artifacts

import spock.lang.Specification
import spock.lang.Unroll

class DirectApkInfoSpec extends Specification {

    @Unroll
    def "given arguments should be returned as it is. Unrolled #variantName"() {
        expect:
        def directApkInfo = new DirectApkInfo(variantName, apkFile, signingReady, universalApk)
        directApkInfo.variantName == variantName
        directApkInfo.apkFile == apkFile
        directApkInfo.signingReady == signingReady
        directApkInfo.universalApk == universalApk

        where:
        variantName | apkFile                  | signingReady | universalApk
        "foo"       | new File("foo")          | true         | true
        "bar"       | new File("build.gradle") | false        | true
        "baz"       | new File("foo/bar")      | false        | false
        "que"       | null                     | true         | false
    }

    def "variantName must be non-null"() {
        when:
        new DirectApkInfo(null, null, false, false)

        then:
        thrown(IllegalArgumentException)
    }
}
