package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.dsl.syntax.DistributionSyntax
import org.gradle.util.Configurable

import javax.annotation.Nullable

class Distribution implements DistributionSyntax, Configurable<Distribution> {

    @Nullable
    String key

    @Nullable
    String releaseNote

    @Override
    Distribution configure(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_ONLY
        cl.call(this)
        return this
    }

    boolean isPresent() {
        return key || releaseNote
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Distribution that = (Distribution) o

        if (key != that.key) return false
        if (releaseNote != that.releaseNote) return false

        return true
    }

    int hashCode() {
        int result
        result = (key != null ? key.hashCode() : 0)
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0)
        return result
    }
}
