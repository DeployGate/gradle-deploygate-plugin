package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.dsl.syntax.DeploymentSyntax
import com.deploygate.gradle.plugins.internal.DeprecationLogger
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
        DeprecationLogger.deprecation("NamedDeployment.setVisibility(String)", "2.5", "3.0", "This API has no effect and no alternative is available. You would see this message until ${DeployGatePlugin.ENV_NAME_APP_VISIBILITY} environment variable is removed.")
        _internalSetVisibility(visibility)
    }

    @Deprecated
    String getVisibility() {
        DeprecationLogger.deprecation("NamedDeployment.getVisibility()", "2.5", "3.0", "This API has no effect and no alternative is available.")
        return _internalGetVisibility()
    }

    @Deprecated
    @Nullable
    String getDistributionKey() {
        DeprecationLogger.deprecation("NamedDeployment.getDistributionKey()", "2.0", "3.0", "Use distribution closure directly.")
        return distribution?.key
    }

    @Deprecated
    void setDistributionKey(@Nullable String distributionKey) {
        DeprecationLogger.deprecation("NamedDeployment.setDistributionKey(String)", "2.0", "3.0", "Use distribution closure instead.")
        distribution {
            delegate.key = distributionKey
        }
    }

    @Deprecated
    @Nullable
    String getReleaseNote() {
        DeprecationLogger.deprecation("NamedDeployment.getReleaseNote()", "2.0", "3.0", "Use distribution closure directly.")
        return distribution?.releaseNote
    }

    @Deprecated
    void setReleaseNote(@Nullable String releaseNote) {
        DeprecationLogger.deprecation("NamedDeployment.setReleaseNote(String)", "2.0", "3.0", "Use distribution closure instead.")
        distribution {
            delegate.releaseNote = releaseNote
        }
    }

    @Deprecated
    boolean getNoAssemble() {
        DeprecationLogger.deprecation("NamedDeployment.getNoAssemble()", "2.0", "3.0", "Use isSkipAssemble() instead.")
        return isSkipAssemble()
    }

    @Deprecated
    void setNoAssemble(boolean noAssemble) {
        DeprecationLogger.deprecation("NamedDeployment.setNoAssemble(boolean)", "2.0", "3.0", "Use setSkipAssemble() instead.")
        setSkipAssemble(noAssemble)
    }

    // end: backward compatibility

    // just for avoiding deprecation logging. non-public api.

    @Deprecated
    void _internalSetVisibility(@Nullable String visibility) {
        this.visibility = visibility
    }

    @Deprecated
    String _internalGetVisibility() {
        return visibility
    }

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