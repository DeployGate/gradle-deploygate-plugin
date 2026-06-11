package com.deploygate.gradle.plugins.internal.utils

import static com.deploygate.gradle.plugins.internal.gradle.ProviderFactoryUtils.environmentVariable

import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.annotations.NotNull

class BrowserUtils {

    @NotNull
    static String getOS_NAME() {
        return (System.getProperty("os.name") ?: "unknown").toLowerCase(Locale.US)
    }

    // Legacy methods for backward compatibility with tests
    static boolean openBrowser(@NotNull String url) {
        return openBrowserLegacy(url)
    }

    static boolean hasBrowser() {
        return hasBrowserLegacy()
    }

    static boolean isExecutableOnLinux() {
        return getOS_NAME().startsWith("linux") && isDisplayAvailable()
    }

    static boolean isExecutableOnMacOS() {
        return getOS_NAME().startsWith("mac")
    }

    static boolean isExecutableOnWindows() {
        return getOS_NAME().startsWith("windows")
    }

    static boolean isDisplayAvailable() {
        String display = System.getenv("DISPLAY")
        return display != null && !display.trim().isEmpty()
    }

    static boolean isCiEnvironment() {
        System.getenv('CI') == "true" || System.getenv('JENKINS_URL')
    }

    /**
     * Opens a browser with the specified URL using configuration cache compatible approach.
     * This method uses Gradle's Provider API to defer environment and system property access
     * until task execution time, ensuring compatibility with configuration cache.
     *
     * @param url The URL to open in the browser
     * @param providers The ProviderFactory to access environment variables and system properties
     * @return true if the browser was successfully opened, false otherwise
     * @since 3.0.0
     */
    static boolean openBrowser(@NotNull String url, @NotNull ProviderFactory providers) {
        def osNameProvider = GradleCompat.forUseAtConfigurationTime(providers.systemProperty("os.name"))
        def displayProvider = environmentVariable(providers, "DISPLAY")
        def ciProvider = environmentVariable(providers, "CI")
        def jenkinsUrlProvider = environmentVariable(providers, "JENKINS_URL")
        return openBrowser(url, osNameProvider, displayProvider, ciProvider, jenkinsUrlProvider)
    }

    /**
     * Checks if a browser is available in the current environment using configuration cache compatible approach.
     * This method uses Gradle's Provider API to defer environment checks until task execution time.
     *
     * @param providers The ProviderFactory to access environment variables and system properties
     * @return true if a browser is available and the environment is not CI, false otherwise
     * @since 3.0.0
     */
    static boolean hasBrowser(@NotNull ProviderFactory providers) {
        def osNameProvider = GradleCompat.forUseAtConfigurationTime(providers.systemProperty("os.name"))
        def displayProvider = environmentVariable(providers, "DISPLAY")
        def ciProvider = environmentVariable(providers, "CI")
        def jenkinsUrlProvider = environmentVariable(providers, "JENKINS_URL")
        return hasBrowser(osNameProvider, displayProvider, ciProvider, jenkinsUrlProvider)
    }

    @NotNull
    private static String getOSNameFromProvider(Provider<String> osNameProvider) {
        return (osNameProvider.getOrElse("unknown")).toLowerCase(Locale.US)
    }

    /**
     * Opens a browser with the specified URL using individual Provider instances.
     * This overload provides fine-grained control over provider sources for advanced use cases.
     *
     * @param url The URL to open in the browser
     * @param osNameProvider Provider for the OS name system property
     * @param displayProvider Provider for the DISPLAY environment variable
     * @param ciProvider Provider for the CI environment variable
     * @param jenkinsUrlProvider Provider for the JENKINS_URL environment variable
     * @return true if the browser was successfully opened, false otherwise
     * @since 3.0.0
     */
    static boolean openBrowser(@NotNull String url, Provider<String> osNameProvider, Provider<String> displayProvider, Provider<String> ciProvider, Provider<String> jenkinsUrlProvider) {
        if (hasBrowser(osNameProvider, displayProvider, ciProvider, jenkinsUrlProvider)) {
            def osName = getOSNameFromProvider(osNameProvider)
            return executeBrowserCommand(url, osName,
                    isExecutableOnLinux(osNameProvider, displayProvider))
        }
        false
    }

    // Legacy method without providers
    private static boolean openBrowserLegacy(@NotNull String url) {
        if (hasBrowserLegacy()) {
            return executeBrowserCommand(url, OS_NAME, isExecutableOnLinux())
        }
        false
    }

    /**
     * Executes the appropriate browser command based on the operating system.
     * This method contains the common logic for opening browsers across different OS.
     *
     * @param url The URL to open
     * @param osName The normalized OS name (lowercase)
     * @param isLinuxExecutable Whether Linux is executable (has display)
     * @return true if browser was opened successfully, false otherwise
     */
    private static boolean executeBrowserCommand(@NotNull String url, @NotNull String osName, boolean isLinuxExecutable) {
        try {
            if (osName.startsWith("mac")) {
                return openBrowserForMac(url)
            } else if (osName.startsWith("windows")) {
                return openBrowserForWindows(url)
            } else if (osName.startsWith("linux") && isLinuxExecutable) {
                return openBrowserForLinux(url)
            } else {
                return false
            }
        } catch (ignored) {
            return false
        }
    }

    static boolean openBrowserForMac(@NotNull String url) {
        return ['open', url].execute().waitFor() == 0
    }

    static boolean openBrowserForWindows(@NotNull String url) {
        return ['cmd', '/c', 'start', url].execute().waitFor() == 0
    }

    static boolean openBrowserForLinux(@NotNull String url) {
        try {
            int result = ['xdg-open', url].execute().waitFor()
            if (result == 0) {
                return true
            } else {
                throw new RuntimeException()
            }
        } catch (ignored) {
            return ['gnome-open', url].execute().waitFor() == 0
        }
    }

    /**
     * Checks if a browser is available using individual Provider instances.
     * This method evaluates OS compatibility and CI environment status using providers.
     *
     * @param osNameProvider Provider for the OS name system property
     * @param displayProvider Provider for the DISPLAY environment variable (Linux)
     * @param ciProvider Provider for the CI environment variable
     * @param jenkinsUrlProvider Provider for the JENKINS_URL environment variable
     * @return true if a browser is available and not in CI environment, false otherwise
     * @since 3.0.0
     */
    static boolean hasBrowser(Provider<String> osNameProvider, Provider<String> displayProvider, Provider<String> ciProvider, Provider<String> jenkinsUrlProvider) {
        !isCiEnvironment(ciProvider, jenkinsUrlProvider) && (isExecutableOnMacOS(osNameProvider) || isExecutableOnWindows(osNameProvider) || isExecutableOnLinux(osNameProvider, displayProvider))
    }

    private static boolean hasBrowserLegacy() {
        !isCiEnvironment() && (isExecutableOnMacOS() || isExecutableOnWindows() || isExecutableOnLinux())
    }

    static boolean isExecutableOnLinux(Provider<String> osNameProvider, Provider<String> displayProvider) {
        return getOSNameFromProvider(osNameProvider).startsWith("linux") && isDisplayAvailable(displayProvider)
    }

    static boolean isExecutableOnMacOS(Provider<String> osNameProvider) {
        return getOSNameFromProvider(osNameProvider).startsWith("mac")
    }

    static boolean isExecutableOnWindows(Provider<String> osNameProvider) {
        return getOSNameFromProvider(osNameProvider).startsWith("windows")
    }

    static boolean isDisplayAvailable(Provider<String> displayProvider) {
        String display = displayProvider.getOrNull()
        return display != null && !display.trim().isEmpty()
    }

    static boolean isCiEnvironment(Provider<String> ciProvider, Provider<String> jenkinsUrlProvider) {
        ciProvider.getOrElse("") == "true" || jenkinsUrlProvider.isPresent()
    }
}
