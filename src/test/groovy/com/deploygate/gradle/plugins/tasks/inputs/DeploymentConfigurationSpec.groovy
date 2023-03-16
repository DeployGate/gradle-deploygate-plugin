package com.deploygate.gradle.plugins.tasks.inputs

import com.deploygate.gradle.plugins.dsl.Distribution
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DeploymentConfigurationSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        GradleCompat.init(project)
    }

    def "copyFrom should overwrite the instance by the given argument"() {
        given:
        def base = project.objects.newInstance(DeploymentConfiguration)
        base.sourceFilePath.set(project.file("base").absolutePath)
        base.message.set("base")
        base.distributionKey.set("base")
        base.distributionReleaseNote.set("base")

        and:
        def other = new NamedDeployment("other")

        when:
        base.copyFrom(other)

        then:
        base.sourceFilePath.getOrNull() == project.file("base").absolutePath
        base.message.getOrNull() == "base"
        base.distributionKey.getOrNull() == "base"
        base.distributionReleaseNote.getOrNull() == "base"
        !base.skipAssemble.get()

        when:
        other.sourceFile = project.file("other")
        other.message = "other message"
        other.distribution { Distribution distribution ->
            distribution.key = "other distributionKey"
            distribution.releaseNote = "other distributionReleaseNote"
        }
        other.visibility = "other visibility"
        other.skipAssemble = true

        and:
        base.copyFrom(other)

        then:
        base.sourceFilePath.getOrNull() == project.file("other").absolutePath
        base.message.getOrNull() == "other message"
        base.distributionKey.getOrNull() == "other distributionKey"
        base.distributionReleaseNote.getOrNull() == "other distributionReleaseNote"
        base.skipAssemble

        when:
        base.sourceFilePath.set(null)
        base.message.set("")
        base.distributionKey.set("")
        base.distributionReleaseNote.set("")
        base.skipAssemble.set(false)

        and:
        base.copyFrom(other)

        then:
        base.sourceFilePath.getOrNull() == project.file("other").absolutePath
        base.message.getOrNull() == "other message"
        base.distributionKey.getOrNull() == "other distributionKey"
        base.distributionReleaseNote.getOrNull() == "other distributionReleaseNote"
        base.skipAssemble
    }
}
