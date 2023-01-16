package com.deploygate.gradle.plugins.internal.http

import spock.lang.Specification

class ErrorResponseSpec extends Specification {
    def "deserialize a json response"() {
        given:
        def jsonStr = """
{
  "error": true,
  "message": "error message"
}

"""

        when:
        def response = ApiClient.GSON.fromJson(jsonStr, ErrorResponse)

        then:
        response.message == "error message"
    }
}
