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
    String distributionKey

    @Nullable
    String releaseNote

    @Nullable
    String visibility

    boolean skipAssemble

    NamedDeployment(@Nonnull String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }

    // backward compatibility

    @Deprecated
    void setMessage(@Nullable String message) {
        setUploadMessage(message)
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
        if (distributionKey != that.distributionKey) return false
        if (name != that.name) return false
        if (releaseNote != that.releaseNote) return false
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
        result = 31 * result + (distributionKey != null ? distributionKey.hashCode() : 0)
        result = 31 * result + (releaseNote != null ? releaseNote.hashCode() : 0)
        result = 31 * result + (visibility != null ? visibility.hashCode() : 0)
        result = 31 * result + (skipAssemble ? 1 : 0)
        return result
    }
}