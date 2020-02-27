package com.deploygate.gradle.plugins

class AcceptanceTestSpec extends AcceptanceTestBaseSpec {

    @Override
    void useProperResource() {
        testAndroidProject.useAcceptanceResourceDir()
    }
}
