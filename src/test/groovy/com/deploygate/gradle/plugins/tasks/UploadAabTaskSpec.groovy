package com.deploygate.gradle.plugins.tasks


import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class UploadAabTaskSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    // no specific assertions
}
