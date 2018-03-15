package com.deploygate.gradle.plugins.utils

class BrowserUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase()

    static boolean openBrowser(String url) {
        if (hasBrowser()) {
            try {
                if(isMac()) {
                    return openBrowserForMac(url)
                } else if(isWindows()) {
                    return openBrowserForWindows(url)
                } else if(isLinux()) {
                    return openBrowserForLinux(url)
                } else {
                    return false
                }
            } catch (ignored) {
            }
        }
        false
    }

    static boolean openBrowserForMac(String url) {
        return ['open', url].execute().waitFor() == 0
    }

    static boolean openBrowserForWindows(String url) {
        return ['cmd', '/c', 'start', url].execute().waitFor() == 0
    }

    static boolean openBrowserForLinux(String url) {
        try {
            int result = ['xdg-open', url].execute().waitFor()
            if(result == 0) {
                return true
            } else {
                throw new RuntimeException()
            }
        } catch (ignored) {
            return ['gnome-open', url].execute().waitFor() == 0
        }
    }

    static boolean hasBrowser() {
        !isCiEnvironment() && (isMac() || isWindows() || (isLinux() && isDisplayAvailable()))
    }

    static boolean isLinux() {
        return OS_NAME.startsWith("linux")
    }

    static boolean isMac() {
        return OS_NAME.startsWith("mac")
    }

    static boolean isWindows() {
        return OS_NAME.startsWith("windows")
    }

    static boolean isDisplayAvailable() {
        String display = System.getenv("DISPLAY")
        return display != null && !display.trim().isEmpty()
    }

    static boolean isCiEnvironment() {
        System.getenv('CI') || System.getenv('JENKINS_URL')
    }
}
