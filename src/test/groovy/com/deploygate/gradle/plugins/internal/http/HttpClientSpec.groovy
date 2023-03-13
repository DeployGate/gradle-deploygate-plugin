package com.deploygate.gradle.plugins.internal.http

import org.apache.hc.client5.http.HttpResponseException
import spock.lang.Specification
import spock.lang.Unroll

class HttpClientSpec extends Specification {
    @Unroll
    def "uploadApp should be #fail"() {
        setup:
        def client = new HttpClient(System.getenv("TEST_SERVER_URL"))

        and:
        def request = new UploadAppRequest(appFile)
        request.setMessage(message)
        request.setDistributionKey(distributionKey)
        request.setReleaseNote(releaseNote)

        and:
        UploadAppResponse response = null
        HttpResponseException e = null

        try {
            response = client.uploadApp(appOwnerName, apiToken, request).typedResponse
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
