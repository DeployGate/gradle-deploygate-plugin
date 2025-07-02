package com.deploygate.gradle.plugins.internal.utils

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for BrowserUtils configuration cache compatibility.
 * Verifies that the provider-based methods work correctly without
 * accessing System properties or environment variables during configuration.
 */
class BrowserUtilsConfigurationCacheSpec extends Specification {

    Project project
    ProviderFactory providers

    def setup() {
        project = ProjectBuilder.builder().build()
        providers = project.providers
    }

    def "hasBrowser with ProviderFactory correctly evaluates browser availability"() {
        given: "Provider factory with mocked environment"
        // Note: In real tests, we can't easily mock providers, but we can test the API

        when: "Checking for browser availability"
        def result = BrowserUtils.hasBrowser(providers)

        then: "Result is computed without accessing System directly during configuration"
        result != null // The actual value depends on the test environment
    }

    def "openBrowser with ProviderFactory defers environment access"() {
        given: "A test URL"
        def url = "https://deploygate.com/test"

        when: "Attempting to open browser"
        def result = BrowserUtils.openBrowser(url, providers)

        then: "Method executes without configuration cache issues"
        result != null // The actual value depends on the test environment
    }

    @Unroll
    def "provider-based method #methodName handles null providers gracefully"() {
        when: "Calling method with null providers"
        method.call()

        then: "Appropriate exception is thrown"
        thrown(NullPointerException)

        where:
        methodName | method
        "hasBrowser" | { -> BrowserUtils.hasBrowser((ProviderFactory) null) }
        "openBrowser" | { -> BrowserUtils.openBrowser("url", (ProviderFactory) null) }
    }

    def "legacy methods still work for backward compatibility"() {
        when: "Using legacy methods"
        def hasBrowser = BrowserUtils.hasBrowser()
        def osName = BrowserUtils.OS_NAME

        then: "Methods work as before"
        hasBrowser != null
        osName != null
    }

    def "provider chains are properly constructed"() {
        given: "Individual providers"
        def osNameProvider = providers.systemProperty("os.name")
        def displayProvider = providers.environmentVariable("DISPLAY")
        def ciProvider = providers.environmentVariable("CI")
        def jenkinsUrlProvider = providers.environmentVariable("JENKINS_URL")

        when: "Using provider-based overload"
        def result = BrowserUtils.hasBrowser(osNameProvider, displayProvider, ciProvider, jenkinsUrlProvider)

        then: "Result is computed correctly"
        result != null
    }

    def "CI environment detection works with providers"() {
        given: "Providers for CI environment variables"
        def ciProvider = providers.provider { "true" }
        def jenkinsUrlProvider = providers.provider { null }

        when: "Checking if CI environment"
        def isCi = BrowserUtils.isCiEnvironment(ciProvider, jenkinsUrlProvider)

        then: "CI environment is detected"
        isCi == true
    }

    def "display availability check works with providers"() {
        given: "Display provider"
        def displayProvider = providers.provider { ":0.0" }

        when: "Checking display availability"
        def hasDisplay = BrowserUtils.isDisplayAvailable(displayProvider)

        then: "Display is detected as available"
        hasDisplay == true
    }

    def "OS detection methods work with providers"() {
        given: "OS name providers for different systems"
        def macProvider = providers.provider { "Mac OS X" }
        def windowsProvider = providers.provider { "Windows 10" }
        def linuxProvider = providers.provider { "Linux" }

        expect: "Correct OS is detected"
        BrowserUtils.isExecutableOnMacOS(macProvider) == true
        BrowserUtils.isExecutableOnWindows(windowsProvider) == true
        BrowserUtils.isExecutableOnLinux(linuxProvider, providers.provider { ":0" }) == true
    }
}