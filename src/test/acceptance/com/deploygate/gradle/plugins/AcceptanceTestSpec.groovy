package com.deploygate.gradle.plugins

class AcceptanceTestSpec extends AcceptanceTestBaseSpec {

    @Override
    void useProperResource() {
        testAndroidProject.useAcceptanceResourceDir()
    }

    @Override
    AGPEnv[] getTestTargetAGPEnvs() {
        return [
                new AGPEnv("3.0.0", "4.1"),
                new AGPEnv("3.1.0", "4.4"),
                new AGPEnv("3.2.0", "4.6"),
                new AGPEnv("3.3.0", "4.10.1"),
                new AGPEnv("3.4.0-beta04", "5.1.1"),
        ]
    }
}
