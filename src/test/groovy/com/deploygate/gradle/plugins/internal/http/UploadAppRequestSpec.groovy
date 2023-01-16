package com.deploygate.gradle.plugins.internal.http


import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.entity.mime.StringBody
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class UploadAppRequestSpec extends Specification {
    @Unroll
    def "toEntity should build multiparts"() {
        given:
        def builder = Mock(MultipartEntityBuilder)
        def request = new UploadAppRequest(appFile, builder)
        request.setMessage(message)
        request.setReleaseNote(releaseNote)
        request.setDistributionKey(distributionKey)

        when:
        request.toEntity() // do not throw an exception

        then:
        1 * builder.addBinaryBody("file", appFile)

        if (message != null) {
            1 * builder.addPart("message", { (it as StringBody).getContentLength() == message.getBytes(StandardCharsets.UTF_8).length })
        } else {
            0 * builder.addPart("message", _)
        }

        if (distributionKey != null) {
            1 * builder.addPart("distribution_key", { (it as StringBody).getContentLength() == distributionKey.getBytes(StandardCharsets.UTF_8).length })
        } else {
            0 * builder.addPart("distribution_key", _)
        }

        if (distributionKey != null && releaseNote != null) {
            1 * builder.addPart("release_note", { (it as StringBody).getContentLength() == releaseNote.getBytes(StandardCharsets.UTF_8).length })
        } else {
            0 * builder.addPart("release_note", _)
        }

        where:
        appFile    | message  | distributionKey | releaseNote
        Mock(File) | null     | null            | null
        Mock(File) | "abc123" | null            | null
        Mock(File) | "abc123" | "def456"        | null
        Mock(File) | "abc123" | "def456"        | "ghi789"
        Mock(File) | "abc123" | null            | "ghi789"
    }
}
