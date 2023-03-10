package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import org.gradle.api.Project
import org.gradle.initialization.GradlePropertiesController
import org.gradle.testfixtures.ProjectBuilder
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

        // We can remove this workaround when upgrading Gradle to 7.1
        // https://github.com/gradle/gradle/issues/13122#issuecomment-705614101
        project.services.get(GradlePropertiesController).loadGradlePropertiesFrom(testProjectDir.root)

        when:
        project.apply(plugin: 'com.android.application') // see build.gradle if you want to check the version
        project.apply(plugin: 'deploygate')

        then:
        project.extensions.deploygate instanceof DeployGateExtension
    }
}
