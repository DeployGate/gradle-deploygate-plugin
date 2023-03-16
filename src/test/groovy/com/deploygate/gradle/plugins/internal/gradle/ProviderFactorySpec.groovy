package com.deploygate.gradle.plugins.internal.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProviderFactorySpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "return the non-null value that is seen first"() {
        setup:
        def providerUnhit1 = project.providers.provider { null }
        def providerHit = project.providers.provider { "hit" }
        def providerUnhit2 = project.providers.provider { "unhit2" }

        when:
        def provider = ProviderFactoryUtils.pickFirst(providerUnhit1, providerHit, providerUnhit2)

        then:
        provider.get() == "hit"
    }
}
