package com.deploygate.gradle.plugins.internal.agp


import com.deploygate.gradle.plugins.internal.VersionString
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class AndroidGradlePluginSpec extends Specification {

    def "can apply this plugin to a project which does not have AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        "no-op"

        then:
        !AndroidGradlePlugin.isApplied(project)
    }

    def "can apply this plugin to a project which has AGP"() {
        given:
        Project project = ProjectBuilder.builder().build()

        when:
        project.apply plugin: 'com.android.application'

        then:
        AndroidGradlePlugin.isApplied(project)
    }
}
