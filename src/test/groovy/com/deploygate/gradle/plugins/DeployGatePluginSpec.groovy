package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class DeployGatePluginSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    def setup() {
        testProjectDir.newFile("gradle.properties")
    }

    def "can apply this plugin to a project which does not have AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply(plugin: 'deploygate')

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }

    def "can apply this plugin to a project which has AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        // https://github.com/gradle/gradle/issues/13122#issuecomment-705614101
        // Before Gradle 7.1, gradle.properties must be loaded explicitly or AGP's ProjectOptionService
        // fails to initialize under ProjectBuilder. The internal API used here was moved/removed in
        // Gradle 9, so call it reflectively and only on the affected Gradle versions.
        if (GradleVersion.current() < GradleVersion.version("7.1")) {
            def controllerType = Class.forName("org.gradle.initialization.GradlePropertiesController")
            project.services.get(controllerType).loadGradlePropertiesFrom(testProjectDir.root)
        }

        when:
        project.apply(plugin: 'com.android.application') // see build.gradle if you want to check the version
        project.apply(plugin: 'deploygate')

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }
}
