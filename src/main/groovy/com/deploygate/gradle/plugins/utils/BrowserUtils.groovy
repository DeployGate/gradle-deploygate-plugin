package com.deploygate.gradle.plugins.utils

import java.awt.*

class BrowserUtils {
    static boolean openBrowser(String url) {
        if (hasBrowser()) {
            try {
                Desktop.getDesktop().browse(URI.create(url))
                return true
            } catch (ignored) {
            }
        }
        false
    }

    static boolean hasBrowser() {
        !isAwtHeadless() && !isCiEnvironment() && Desktop.isDesktopSupported()
    }

    static boolean isCiEnvironment() {
        System.getenv('CI') || System.getenv('JENKINS_URL')
    }

    static boolean isAwtHeadless() {
        System.getProperty('java.awt.headless')
    }
}
