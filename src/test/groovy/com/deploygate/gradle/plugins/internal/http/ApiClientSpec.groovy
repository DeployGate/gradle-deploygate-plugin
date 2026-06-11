package com.deploygate.gradle.plugins.internal.http

import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import org.apache.hc.client5.http.HttpResponseException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.annotations.NotNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class ApiClientSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    @NotNull
    private Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    @Unroll
    def "uploadApp should be #fail"() {
        setup:
        def credentials = project.objects.newInstance(Credentials)
        credentials.appOwnerName.set(appOwnerName)
        credentials.apiToken.set(apiToken)
        def client = project.gradle.sharedServices.registerIfAbsent("httpclient", HttpClient) { spec ->
            spec.parameters.endpoint.set(System.getenv("TEST_SERVER_URL") ?: "https://deploygate.com")
            spec.parameters.agpVersion.set("unknown")
            spec.parameters.pluginVersion.set("test")
            spec.parameters.pluginVersionCode.set("1")
            spec.parameters.pluginVersionName.set("test")
        }.get().getApiClient(credentials)

        and:
        def request = new UploadAppRequest(appFile)
        request.setMessage(message)
        request.setDistributionKey(distributionKey)
        request.setReleaseNote(releaseNote)

        and:
        UploadAppResponse response = null
        HttpResponseException e = null

        try {
            response = client.uploadApp(request).typedResponse
        } catch (HttpResponseException th) {
            e = th
        }

        expect:
        if (fail) {
            e != null
        } else {
            response != null
            response.application.revision == 18
            response.application.path == "/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example"
        }

        where:
        appOwnerName | apiToken | appFile                            | message   | distributionKey | releaseNote    | fail
        "dummy"      | "token"  | File.createTempFile("pre", "post") | null      | null            | null           | true
        "owner_1"    | "dummy"  | File.createTempFile("pre", "post") | null      | null            | null           | true
        "owner_1"    | "token"  | File.createTempFile("pre", "post") | null      | null            | null           | false
        "owner_1"    | "token"  | File.createTempFile("pre", "post") | "message" | null            | null           | false
        "owner_1"    | "token"  | File.createTempFile("pre", "post") | null      | "dist_key"      | null           | false
        "owner_1"    | "token"  | File.createTempFile("pre", "post") | "message" | "dist_key"      | null           | false
        "owner_1"    | "token"  | File.createTempFile("pre", "post") | "message" | "dist_key"      | "release_note" | false
    }
}
