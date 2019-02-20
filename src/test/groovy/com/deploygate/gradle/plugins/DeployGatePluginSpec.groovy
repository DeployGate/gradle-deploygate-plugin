package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DeployGatePluginSpec extends Specification {
    def "can apply this plugin to a project which does not have AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'deploygate'

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }

    def "can apply this plugin to a project which has AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'com.android.application' // see build.gradle if you want to check the version
        project.apply plugin: 'deploygate'

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }

    def "can apply this plugin to a project which has AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'com.android.application' // see build.gradle if you want to check the version
        project.apply plugin: 'deploygate'

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }
}
