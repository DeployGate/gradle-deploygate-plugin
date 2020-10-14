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

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    @Unroll
    def "feature catalog verification. Unrolled #agpVersion"() {
        given:
        AndroidGradlePlugin.metaClass.static.getVersion = { ->
            VersionString.tryParse(agpVersion)
        }

        expect:
        AndroidGradlePlugin.isAppBundleSupported() == isAppBundleSupported
        AndroidGradlePlugin.isSigningConfigCollectionSupported() == isSigningConfigCollectionSupported
        AndroidGradlePlugin.isTaskProviderBased() == isTaskProviderBased

        where:
        agpVersion                 | isAppBundleSupported | isSigningConfigCollectionSupported | isTaskProviderBased
        "3.0.0"                    | false                | false                              | false
        "3.1.0"                    | false                | false                              | false
        "3.2.0"                    | true                 | false                              | false
        "3.2.1"                    | true                 | false                              | false
        "3.3.0"                    | true                 | true                               | true
        "3.4.0"                    | true                 | true                               | true
        "4.0.0"                    | true                 | true                               | true
        "4.1.0"                    | true                 | true                               | true
        "4.2.0-alpha01"            | true                 | true                               | true
        "${Integer.MAX_VALUE}.0.0" | true                 | true                               | true // fallback
    }
}
