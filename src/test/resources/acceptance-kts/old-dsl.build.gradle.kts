plugins {
    id("com.android.application")
    id("deploygate")
}

apply(from = "android_block.gradle")

deploygate {
    userName = "owner_1"
    token = "api token"

    apks {
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
            noAssemble = true
        }
        create("customApk") {
            sourceFile = file("${project.projectDir}/texture/sample.apk")
            message = "custom message"
            visibility = "custom visibility"
            distributionKey = "custom distributionKey"
            releaseNote = "custom releaseNote"
        }
    }
}

apply(from = "assertion_tasks.gradle")