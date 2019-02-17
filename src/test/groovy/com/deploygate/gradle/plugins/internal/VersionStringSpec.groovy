package com.deploygate.gradle.plugins.internal

import spock.lang.Specification
import spock.lang.Unroll

class VersionStringSpec extends Specification {

    @Unroll
    def "tryParse. Unrolled #version"() {
        given:
        def result = VersionString.tryParse(version)

        expect:
        result.major == major
        result.minor == minor
        result.patch == patch
        result.addition == addition

        where:
        version          | major | minor | patch | addition
        "1.0"            | 1     | 0     | 0     | null
        "1.2.3"          | 1     | 2     | 3     | null
        "1.0.3-extra"    | 1     | 0     | 3     | "extra"
        "1.0.3-extra03"  | 1     | 0     | 3     | "extra03"
        "1.0.3-extra-03" | 1     | 0     | 3     | "extra-03"
    }

    @Unroll
    def "toString. Unrolled #version"() {
        given:
        def result = VersionString.tryParse(version)

        expect:
        result.toString().startsWith(version)

        where:
        version << [
                "1.0",
                "1.2.3",
                "1.0.3-extra",
                "1.0.3-extra03",
                "1.0.3-extra-03"
        ]
    }
}
