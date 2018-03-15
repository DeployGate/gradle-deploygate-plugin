package com.deploygate.gradle.plugins.utils

import org.junit.Test

class BrowserUtilsTest {
    @Test
    public void testOpenBrowser() {
        java.util.List.metaClass.execute = { ->
            [ waitFor: { -> 0 }]
        }
        BrowserUtils.metaClass.static.hasBrowser = { -> true }

        def time = 'aaa'
        def urlGString = "https://deploygate.com/#${time}"
        assert BrowserUtils.openBrowser(urlGString)

        def urlString = "https://deploygate.com/"
        assert BrowserUtils.openBrowser(urlString)
    }
}
