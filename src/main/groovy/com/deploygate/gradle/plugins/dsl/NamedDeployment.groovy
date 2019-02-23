package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.dsl.syntax.DeploymentSyntax
import org.gradle.api.Named

import javax.annotation.Nonnull
import javax.annotation.Nullable

class NamedDeployment implements Named, DeploymentSyntax {
    @Nonnull
    private String name

    @Nullable
    File sourceFile

    @Nullable
    String message

    @Nullable
    String visibility

    boolean skipAssemble

    // Avoid using Optional like Guava for now because we want to reduce external dependencies as much as possible.
    @Nonnull
    private Distribution[] optionalDistribution

    NamedDeployment(@Nonnull String name) {
        this.name = name
        this.optionalDistribution = Collections.singletonList(new Distribution())
    }

    @Override
    String getName() {
        return name
    }

    @Override
    void distribution(@Nonnull Closure closure) {
        optionalDistribution[0].configure(closure)
    }

    @Nullable
    Distribution getDistribution() {
        return optionalDistribution.find { it.isPresent() }
    }

    // backward compatibility

    @Deprecated
    @Nullable
    String getDistributionKey() {
        return distribution?.key
    }

    @Deprecated
    void setDistributionKey(@Nullable String distributionKey) {
        distribution {
            delegate.key = distributionKey
        }
    }

    @Deprecated
    @Nullable
    String getReleaseNote() {
        return distribution?.releaseNote
    }

    @Deprecated
    void setReleaseNote(@Nullable String releaseNote) {
        distribution {
            delegate.releaseNote = releaseNote
        }
    }

    @Deprecated
    boolean getNoAssemble() {
        return isSkipAssemble()
    }

    @Deprecated
    void setNoAssemble(boolean noAssemble) {
        setSkipAssemble(noAssemble)
    }

    // end: backward compatibility

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        NamedDeployment that = (NamedDeployment) o

        if (skipAssemble != that.skipAssemble) return false
        if (distribution != that.distribution) return false
        if (name != that.name) return false
        if (sourceFile != that.sourceFile) return false
        if (message != that.message) return false
        if (visibility != that.visibility) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0)
        result = 31 * result + (message != null ? message.hashCode() : 0)
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0)
        result = 31 * result + (skipAssemble ? 1 : 0)
        result = 31 * result + (distribution != null ? distribution.hashCode() : 0)
        return result
    }
}