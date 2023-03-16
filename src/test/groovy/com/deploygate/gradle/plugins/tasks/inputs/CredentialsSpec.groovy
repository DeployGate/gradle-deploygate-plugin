package com.deploygate.gradle.plugins.tasks.inputs


import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CredentialsSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    def "normalizeAndValidate requires the both of api token and app owner name"() {
        setup:
        def credentials1 = project.objects.newInstance(Credentials)

        when:
        credentials1.normalizeAndValidate()

        then:
        thrown(GradleException)

        when:
        credentials1.appOwnerName.set("")
        credentials1.apiToken.set("api token")

        and:
        credentials1.normalizeAndValidate()

        then:
        thrown(GradleException)

        when:
        credentials1.appOwnerName.set("app owner")
        credentials1.apiToken.set("")

        and:
        credentials1.normalizeAndValidate()

        then:
        thrown(GradleException)

        when: "reset"
        def credentials2 = project.objects.newInstance(Credentials)

        and:
        credentials2.appOwnerName.set("app owner")
        credentials2.apiToken.set("api token")

        and:
        credentials2.normalizeAndValidate()

        then:
        credentials2.appOwnerName.get() == "app owner"
        credentials2.apiToken.get() == "api token"
    }

    def "normalizeAndValidate trim values"() {
        setup:
        def credentials = project.objects.newInstance(Credentials)

        when:
        credentials.appOwnerName.set("  app owner")
        credentials.apiToken.set("  api token   ")

        and:
        credentials.normalizeAndValidate()

        then:
        credentials.appOwnerName.get() == "app owner"
        credentials.apiToken.get() == "api token"
    }
}
