package com.deploygate.gradle.plugins.internal.utils

import org.jetbrains.annotations.NotNull

class BrowserUtils {

    @NotNull
    private static String getOS_NAME() {
        return (System.getProperty("os.name") ?: "unknown").toLowerCase(Locale.US)
    }

    static boolean openBrowser(@NotNull String url) {
        if (hasBrowser()) {
            try {
                if (isExecutableOnMacOS()) {
                    return openBrowserForMac(url)
                } else if (isExecutableOnWindows()) {
                    return openBrowserForWindows(url)
                } else if (isExecutableOnLinux()) {
                    return openBrowserForLinux(url)
                } else {
                    return false
                }
            } catch (ignored) {
            }
        }
        false
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

    static boolean hasBrowser() {
        !isCiEnvironment() && (isExecutableOnMacOS() || isExecutableOnWindows() || isExecutableOnLinux())
    }

    static boolean isExecutableOnLinux() {
        return OS_NAME.startsWith("linux") && isDisplayAvailable()
    }

    static boolean isExecutableOnMacOS() {
        return OS_NAME.startsWith("mac")
    }

    static boolean isExecutableOnWindows() {
        return OS_NAME.startsWith("windows")
    }

    static boolean isDisplayAvailable() {
        String display = System.getenv("DISPLAY")
        return display != null && !display.trim().isEmpty()
    }

    static boolean isCiEnvironment() {
        System.getenv('CI') == "true" || System.getenv('JENKINS_URL')
    }
}
