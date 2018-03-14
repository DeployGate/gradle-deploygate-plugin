package com.deploygate.gradle.plugins.utils

class BrowserUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase()

    static boolean openBrowser(String url) {
        if (hasBrowser()) {
            try {
                if(isMac()) {
                    openBrowserForMac(url)
                } else if(isWindows()) {
                    openBrowserForWindows(url)
                } else if(isLinux()) {
                    openBrowserForLinux(url)
                } else {
                    return false
                }

                return true
            } catch (ignored) {
            }
        }
        false
    }

    static void openBrowserForMac(String url) {
        "open $url".execute().waitFor()
    }

    static void openBrowserForWindows(String url) {
        "cmd /c start $url".execute().waitFor()
    }

    static void openBrowserForLinux(String url) {
        String result = "xdg-open $url".execute().waitFor()
        if(!result.equals('0')) {
            "gnome-open $url".execute().waitFor()
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
