import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.dsl.Distribution

plugins {
    id("com.android.application")
    id("deploygate")
}

apply(from = "android_block.gradle")

deploygate {
    // for testing
    endpoint = "http://localhost:8888"

    setUserName("appOwner")
    setToken("api token")

    apks(closureOf<NamedDomainObjectContainer<NamedDeployment>> {
        create("flavor1Flavor3Debug").apply {
        }
        create("flavor2Flavor3Debug").apply {
            message = "flavor2Flavor3Debug"
        }
        create("flavor1Flavor4Debug").apply {
            message = "flavor1Flavor4Debug"
        }
        create("flavor2Flavor4Debug").apply {
            message = "flavor2Flavor4Debug"
            setNoAssemble(true)
        }
        create("customApk").apply {
            sourceFile = file("${project.projectDir}/texture/sample.apk")
            message = "custom message"
            visibility = "custom visibility"
            setDistributionKey("custom distributionKey")
            setReleaseNote("custom releaseNote")
        }
    })
}

apply(from = "assertion_tasks.gradle")