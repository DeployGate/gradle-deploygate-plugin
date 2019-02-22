package com.deploygate.gradle.plugins.internal.gradle

import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class TaskFactorySpec extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildGradle

    def setup() {
        buildGradle = testProjectDir.newFile("build.gradle")
    }

    @ConfineMetaClassChanges([GradleCompat])
    @Unroll
    def "TaskFactory can add tasks based on Gradle API #gradleVersion"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        GradleCompat.metaClass.static.getVersion = { ->
            VersionString.tryParse(gradleVersion)
        }

        and:
        TaskFactory taskFactory = new TaskFactory(project)

        and:
        def result = taskFactory.register(taskName, DefaultTask)

        expect:
        expectedTaskClass.isInstance(result)

        where:
        taskName        | gradleVersion | expectedTaskClass
        "agp300"        | "4.1"         | SingleTask
        "agp310"        | "4.4"         | SingleTask
        "agp320"        | "4.6"         | SingleTask
        "border"        | "4.7"         | SingleTask
        "border"        | "4.8"         | SingleTask
        "border"        | "4.9"         | TaskProvider
        "agp330"        | "4.10.1"      | TaskProvider
        "agp340-beta04" | "5.1.1"       | TaskProvider
    }

    @ConfineMetaClassChanges([GradleCompat])
    @Unroll
    def "TaskFactory can handle duplicated tasks regardless of #gradleVersion"() {
        given:
        Project project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        GradleCompat.metaClass.static.getVersion = { ->
            VersionString.tryParse(gradleVersion)
        }

        and:
        TaskFactory taskFactory = new TaskFactory(project)
        taskFactory.register(taskName, DefaultTask)

        and:
        def result = taskFactory.register(taskName, DefaultTask, allowExisting)

        expect:
        !allowExisting && result == null || expectedTaskClass.isInstance(result)

        where:
        taskName        | gradleVersion | allowExisting | expectedTaskClass
        "agp300"        | "4.1"         | true          | SingleTask
        "agp310"        | "4.4"         | true          | SingleTask
        "agp320"        | "4.6"         | true          | SingleTask
        "border"        | "4.7"         | true          | SingleTask
        "border"        | "4.8"         | true          | SingleTask
        "agp330"        | "4.10.1"      | true          | SingleTask
        "agp340-beta04" | "5.1.1"       | true          | SingleTask
        "agp300"        | "4.1"         | false         | null
        "agp310"        | "4.4"         | false         | null
        "agp320"        | "4.6"         | false         | null
        "border"        | "4.7"         | false         | null
        "border"        | "4.8"         | false         | null
        "agp330"        | "4.10.1"      | false         | null
        "agp340-beta04" | "5.1.1"       | false         | null
    }
}
