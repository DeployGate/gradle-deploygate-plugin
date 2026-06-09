package com.deploygate.gradle.plugins.artifacts

import spock.lang.Specification
import spock.lang.Unroll

class DirectApkInfoSpec extends Specification {

    @Unroll
    def "given arguments should be returned as it is. Unrolled #variantName"() {
        expect:
        def directApkInfo = new DirectApkInfo(variantName, apkFile, universalApk)
        directApkInfo.variantName == variantName
        directApkInfo.apkFile == apkFile
        directApkInfo.universalApk == universalApk

        where:
        variantName | apkFile                  | universalApk
        "foo"       | new File("foo")          | true
        "bar"       | new File("build.gradle") | true
        "baz"       | new File("foo/bar")      | false
        "que"       | null                     | false
    }

    def "variantName must be non-null"() {
        when:
        new DirectApkInfo(null, null, false)

        then:
        thrown(IllegalArgumentException)
    }
}
