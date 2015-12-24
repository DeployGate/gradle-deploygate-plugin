package com.deploygate.gradle.plugins.utils

import org.codehaus.groovy.runtime.GStringImpl
import org.junit.Test

import java.awt.*

class BrowserUtilsTest {
    @Test
    public void testOpenBrowser() {
        Desktop.metaClass.static.getDesktop = { ->
            [ browse: { URI uri -> true }]
        }
        BrowserUtils.metaClass.static.hasBrowser = { -> true }

        def time = 'aaa'
        def urlGString = "https://deploygate.com/#${time}"
        assert BrowserUtils.openBrowser(urlGString)

        def urlString = "https://deploygate.com/"
        assert BrowserUtils.openBrowser(urlString)
    }
}
