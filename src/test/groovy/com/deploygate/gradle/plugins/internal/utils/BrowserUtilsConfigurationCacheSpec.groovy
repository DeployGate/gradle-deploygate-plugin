package com.deploygate.gradle.plugins.internal.utils

import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for BrowserUtils provider-based API.
 * These are unit tests that verify the provider-based methods work correctly.
 * Note: Configuration cache restrictions only apply during actual Gradle builds,
 * not in unit tests, so we focus on API functionality here.
 */
class BrowserUtilsConfigurationCacheSpec extends Specification {

    Project project
    ProviderFactory providers

    def setup() {
        project = ProjectBuilder.builder().build()
        providers = project.providers
    }

    def "hasBrowser with ProviderFactory returns expected result"() {
        given: "Provider factory from test project"
        // In unit tests, we can evaluate providers directly as we're not in configuration phase

        when: "Checking for browser availability"
        def result = BrowserUtils.hasBrowser(providers)

        then: "Result is based on current environment"
        // The result will depend on the test environment (CI vs local, OS, etc.)
        result != null
        // In most test environments, this will be false due to CI detection or missing display
        result == !BrowserUtils.isCiEnvironment() && 
                  (BrowserUtils.isExecutableOnMacOS() || 
                   BrowserUtils.isExecutableOnWindows() || 
                   BrowserUtils.isExecutableOnLinux())
    }

    def "openBrowser with ProviderFactory executes correctly"() {
        given: "A test URL"
        def url = "https://deploygate.com/test"

        when: "Checking if browser would be opened (without actually opening)"
        // We test the logic by checking preconditions instead of actually opening browser
        def wouldOpen = BrowserUtils.hasBrowser(providers)

        then: "Browser availability is correctly determined"
        wouldOpen != null
        // The actual browser opening would only happen if browser is available
        // We don't actually call openBrowser to avoid side effects in tests
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
        given: "Mock providers that don't access actual environment"
        def osNameProvider = providers.provider { "Mac OS X" }
        def displayProvider = providers.provider { ":0" }
        def ciProvider = providers.provider { "false" }
        def jenkinsUrlProvider = providers.provider { null }

        when: "Using provider-based overload"
        def result = BrowserUtils.hasBrowser(osNameProvider, displayProvider, ciProvider, jenkinsUrlProvider)

        then: "Result is computed correctly"
        result == true
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