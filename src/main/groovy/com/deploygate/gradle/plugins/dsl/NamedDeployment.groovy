package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.dsl.syntax.DeploymentSyntax
import com.deploygate.gradle.plugins.internal.Logger
import org.gradle.api.Named

import javax.annotation.Nonnull
import javax.annotation.Nullable

class NamedDeployment implements Named, DeploymentSyntax {
    private static final Logger logger = Logger.getLogger(NamedDeployment)

    @Nonnull
    private String name

    @Nullable
    File sourceFile

    @Nullable
    String message

    boolean skipAssemble

    @Nullable
    @Deprecated
    private String visibility

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

    @Override
    @Deprecated
    void setVisibility(@Nullable String visibility) {
        logger.deprecation("void setVisibility(String)", "2.5", "3.0", "This API has no effect and no alternative is available.")
        this.visibility = visibility
    }

    @Deprecated
    String getVisibility() {
        logger.deprecation("String getVisibility()", "2.5", "3.0", "This API has no effect and no alternative is available.")
        return visibility
    }

    @Deprecated
    @Nullable
    String getDistributionKey() {
        logger.deprecation("String getDistributionKey()", "2.0", "3.0", "Use distribution closure directly.")
        return distribution?.key
    }

    @Deprecated
    void setDistributionKey(@Nullable String distributionKey) {
        logger.deprecation("void setDistributionKey(String)", "2.0", "3.0", "Use distribution closure instead.")
        distribution {
            delegate.key = distributionKey
        }
    }

    @Deprecated
    @Nullable
    String getReleaseNote() {
        logger.deprecation("String getReleaseNote()", "2.0", "3.0", "Use distribution closure directly.")
        return distribution?.releaseNote
    }

    @Deprecated
    void setReleaseNote(@Nullable String releaseNote) {
        logger.deprecation("void setReleaseNote(String)", "2.0", "3.0", "Use distribution closure instead.")
        distribution {
            delegate.releaseNote = releaseNote
        }
    }

    @Deprecated
    boolean getNoAssemble() {
        logger.deprecation("boolean getNoAssemble()", "2.0", "3.0", "Use isSkipAssemble() instead.")
        return isSkipAssemble()
    }

    @Deprecated
    void setNoAssemble(boolean noAssemble) {
        logger.deprecation("void setNoAssemble(boolean)", "2.0", "3.0", "Use setSkipAssemble() instead.")
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

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0)
        result = 31 * result + (message != null ? message.hashCode() : 0)
        result = 31 * result + (skipAssemble ? 1 : 0)
        result = 31 * result + (distribution != null ? distribution.hashCode() : 0)
        return result
    }
}