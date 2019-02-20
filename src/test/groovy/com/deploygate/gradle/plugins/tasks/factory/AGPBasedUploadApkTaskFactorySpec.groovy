package com.deploygate.gradle.plugins.tasks.factory

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import javax.annotation.Nonnull

class AGPBasedUploadApkTaskFactorySpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private AGPBasedUploadApkTaskFactory agpBasedUploadApkTaskFactory

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def "registerUploadApkTask should not be supported"() {
        given:
        agpBasedUploadApkTaskFactory = new AGPBasedUploadApkTaskFactory(project)

        when:
        agpBasedUploadApkTaskFactory.registerUploadApkTask()

        then:
        thrown(IllegalAccessException)
    }

    def "registerAggregatedUploadApkTask should not be supported"() {
        given:
        agpBasedUploadApkTaskFactory = new AGPBasedUploadApkTaskFactory(project)

        when:
        agpBasedUploadApkTaskFactory.registerAggregatedUploadApkTask()

        then:
        thrown(IllegalAccessException)
    }
}
