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
    String uploadMessage

    @Nullable
    String visibility

    boolean skipAssemble

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
        def distribution = optionalDistribution[0]

        closure.delegate = distribution
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call(distribution)
    }

    @Nullable
    Distribution getDistribution() {
        return optionalDistribution.find { it.isPresent() }
    }

    // backward compatibility

    @Deprecated
    void setMessage(@Nullable String message) {
        setUploadMessage(message)
    }

    @Deprecated
    void setDistributionKey(@Nullable String distributionKey) {
        distribution {
            delegate.key = distributionKey
        }
    }

    @Deprecated
    void setReleaseNote(@Nullable String releaseNote) {
        distribution {
            delegate.releaseNote = releaseNote
        }
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
        if (uploadMessage != that.uploadMessage) return false
        if (visibility != that.visibility) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0)
        result = 31 * result + (uploadMessage != null ? uploadMessage.hashCode() : 0)
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0)
        result = 31 * result + (skipAssemble ? 1 : 0)
        result = 31 * result + (distribution != null ? distribution.hashCode() : 0)
        return result
    }
}