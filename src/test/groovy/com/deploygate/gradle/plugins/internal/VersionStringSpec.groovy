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
        result.prerelease == prerelease
        result.metaBuild == metaBuild

        where:
        version          | major | minor | patch | prerelease | metaBuild
        "1.0"            | 1     | 0     | 0     | null       | 0
        "1.2.3"          | 1     | 2     | 3     | null       | 0
        "1.0.3-extra"    | 1     | 0     | 3     | "extra"    | 0
        "1.0.3-extra03"  | 1     | 0     | 3     | "extra"    | 3
        "1.0.3-extra-03" | 1     | 0     | 3     | "extra"    | 3
    }

    @Unroll
    def "toString. Unrolled #version"() {
        given:
        def result = VersionString.tryParse(version)

        expect:
        result.toString() == stringVersion

        where:
        version          | stringVersion
        "1.0"            | "1.0"
        "1.2.3"          | "1.2.3"
        "1.0.3-extra"    | "1.0.3-extra"
        "1.0.3-extra03"  | "1.0.3-extra03"
        "1.0.3-extra-03" | "1.0.3-extra-03"
    }

    @Unroll
    def "compareTo. Unrolled #version"() {
        given:
        def lhs = VersionString.tryParse(leftVersion)
        def rhs = VersionString.tryParse(rightVersion)

        expect:
        lhs <=> rhs == expected

        where:
        leftVersion      | rightVersion   | expected
        "1.0"            | "1.0"          | 0
        "1.2.1"          | "1.2.3"        | -1
        "1.2.3"          | "1.2.3"        | 0
        "1.0.3-extra"    | "1.0.3-extra0" | 0
        "1.0.3-extra03"  | "1.0.3-extra3" | 0
        "1.0.3-rc1"      | "1.0.3-alpha1" | 1
        "1.0.3-rc1"      | "1.0.3-beta1"  | 1
        "1.0.3-alpha1"   | "1.0.3-rc1"    | -1
        "1.0.3-alpha1"   | "1.0.3-beta1"  | -1
        "1.0.3-beta1"    | "1.0.3-rc1"    | -1
        "1.0.3-beta1"    | "1.0.3-beta2"  | -1
        "1.0.3-beta2"    | "1.0.3-beta1"  | 1
        "1.0.3"          | "1.0.3-alpha1" | 1
        "1.0.3"          | "1.0.3-beta1"  | 1
        "1.0.3"          | "1.0.3-rc1"    | 1
        "1.0.3-alpha1"   | "1.0.3"        | -1
        "1.0.3-beta1"    | "1.0.3"        | -1
        "1.0.3-rc1"      | "1.0.3"        | -1
    }
}
