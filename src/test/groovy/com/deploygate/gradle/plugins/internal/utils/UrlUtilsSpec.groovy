package com.deploygate.gradle.plugins.internal.utils

import spock.lang.Specification
import spock.lang.Unroll

class UrlUtilsSpec extends Specification {

    @Unroll
    def "parseQueryString #query"() {
        given:
        def result = UrlUtils.parseQueryString(query)

        expect:
        result == expected

        where:
        query                                   | expected
        null                                    | [:]
        ""                                      | [:]
        "key1=abc%20def%20gh"                   | ["key1": "abc def gh"]
        "key1=abc%20def%20gh&key2=value2&key3=" | ["key1": "abc def gh", "key2": "value2", "key3": ""]
        "key1=abc%20def%20gh&&key3="            | ["key1": "abc def gh", "key3": ""]
    }
}
