package com.deploygate.gradle.plugins.internal.utils

import com.deploygate.gradle.plugins.TestSystemEnv
import com.deploygate.gradle.plugins.internal.utils.BrowserUtils
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class BrowserUtilsSpec extends Specification {
    @Rule
    TestSystemEnv envStub = new TestSystemEnv()

    @ConfineMetaClassChanges([BrowserUtils])
    @Unroll
    def "openBrowser. Unrolled #name"() {
        given:
        [""].class.metaClass.execute = {
            ->
            [waitFor: { -> 0 }]
        }
        BrowserUtils.metaClass.static.hasBrowserLegacy = { -> hasBrowser }
        BrowserUtils.metaClass.static.isExecutableOnMacOS = { -> onMacOS }
        BrowserUtils.metaClass.static.isExecutableOnWindows = { -> onWindows }
        BrowserUtils.metaClass.static.isExecutableOnLinux = { -> onLinux }

        expect:
        BrowserUtils.openBrowser(url) == result

        where:
        name                       | url                                  | hasBrowser | onMacOS | onWindows | onLinux | result
        "mac os has a browser"     | "https://deploygate.com/"            | true       | true    | false     | false   | true
        "windows has a browser"    | "https://deploygate.com/#anchor"     | true       | false   | true      | false   | true
        "linux has a browser"      | "https://deploygate.com/subpath"     | true       | false   | false     | true    | true
        "unknown os has a browser" | "https://deploygate.com/?query=true" | true       | false   | false     | false   | false
        "otherwise"                | "https://deploygate.com/?query=true" | false      | false   | false     | false   | false
    }

    @Unroll
    def "isCiEnvironment. Unrolled #name"() {
        given:
        envStub.setEnv([
            "CI"         : isCI,
            "JENKINS_URL": jenkinsUrl
        ])

        expect:
        BrowserUtils.isCiEnvironment() == result

        where:
        name                     | isCI  | jenkinsUrl    | result
        "ci env"                 | true  | null          | true
        "jenkins url"            | false | "jenkins url" | true
        "whitespace jenkins url" | false | " "           | true
        "otherwise"              | false | null          | false
    }


    @ConfineMetaClassChanges([BrowserUtils])
    @Unroll
    def "isExecutableXXX. Unrolled osName is #osName"() {
        given:
        envStub.setEnv([
            "DISPLAY": display,
        ])
        BrowserUtils.metaClass.static.getOS_NAME = { -> osName }

        expect:
        BrowserUtils.isExecutableOnMacOS() == onMacOS
        BrowserUtils.isExecutableOnWindows() == onWindows
        BrowserUtils.isExecutableOnLinux() == onLinux

        where:
        osName                  | onMacOS | onWindows | onLinux | display
        "mac"                   | true    | false     | false   | null
        "mac snow lepard"       | true    | false     | false   | null
        "windows"               | false   | true      | false   | null
        "windows 97"            | false   | true      | false   | null
        "linux"                 | false   | false     | false   | null
        "linux w/o display"     | false   | false     | false   | null
        "linux w empty display" | false   | false     | false   | " "
        "linux w/ display"      | false   | false     | true    | "display"
        "otherwise"             | false   | false     | false   | null
    }


    @ConfineMetaClassChanges([BrowserUtils])
    @Unroll
    def "hasBrowser. Unrolled #name"() {
        given:
        BrowserUtils.metaClass.static.isCiEnvironment = { -> isCI }
        BrowserUtils.metaClass.static.isExecutableOnMacOS = { -> onMacOS }
        BrowserUtils.metaClass.static.isExecutableOnWindows = { -> onWindows }
        BrowserUtils.metaClass.static.isExecutableOnLinux = { -> onLinux }

        expect:
        BrowserUtils.hasBrowser() == result

        where:
        name         | isCI  | onMacOS | onWindows | onLinux | result
        "on ci"      | true  | false   | false     | false   | false
        "on macos"   | false | true    | false     | false   | true
        "on windows" | false | false   | true      | false   | true
        "on linux"   | false | false   | false     | true    | true
        "otherwise"  | false | false   | false     | false   | false
    }
}
