import com.deploygate.gradle.plugins.dsl.Distribution

plugins {
    id("com.android.application")
    id("deploygate")
}

apply(from = "android_block.gradle")

deploygate {
    // for testing
    endpoint = System.getenv("TEST_SERVER_URL")

    appOwnerName = "owner_1"
    apiToken = "api token"

    deployments {
        create("flavor1Flavor3Debug") {
        }
        create("flavor2Flavor3Debug") {
            message = "flavor2Flavor3Debug"
        }
        create("flavor1Flavor4Debug") {
            message = "flavor1Flavor4Debug"
        }
        create("flavor2Flavor4Debug") {
            message = "flavor2Flavor4Debug"
            skipAssemble = true
        }
        create("customApk") {
            sourceFile = file("${project.projectDir}/texture/sample.apk")
            message = "custom message"
            visibility = "custom visibility"

            distribution {
                key = "custom distributionKey"
                releaseNote = "custom releaseNote"
            }
        }
        create("customApkForBackwardCompatibility") {
            sourceFile = file("${project.projectDir}/texture/sample.apk")
            message = "custom message"
            visibility = "custom visibility"

            distribution(closureOf<Distribution> {
                key = "custom distributionKey"
                releaseNote = "custom releaseNote"
            })
        }
    }
}

apply(from = "assertion_tasks.gradle")