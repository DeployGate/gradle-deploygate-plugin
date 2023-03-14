package com.deploygate.gradle.plugins.internal.http

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import javax.annotation.Nonnull

class HttpClientResponseSpec  extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @Nonnull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "writeTo writes a raw response into a file"() {
        setup:
        def file = testProjectDir.newFile("raw")
        def response = new HttpClient.Response<String>("typed response is not a target", "hello raw response");

        when:
        response.writeTo(file)

        then:
        file.parentFile.exists()
        file.exists()
        file.text == "hello raw response"
    }
}
