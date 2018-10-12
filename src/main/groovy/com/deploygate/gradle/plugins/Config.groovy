package com.deploygate.gradle.plugins

class Config {
    static final def VERSION = Config.class.getResourceAsStream("/VERSION").text.trim()
    static final def USER_AGENT = "gradle-deploygate-plugin/${VERSION}"
    static final def DEPLOYGATE_ROOT = 'https://deploygate.com'
}
