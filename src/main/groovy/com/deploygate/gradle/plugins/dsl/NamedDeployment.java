package com.deploygate.gradle.plugins.dsl;

import com.deploygate.gradle.plugins.dsl.syntax.DeploymentSyntax;
import com.deploygate.gradle.plugins.internal.DeprecationLogger;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.tasks.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/*
 * This file cannot be written in Groovy until we decide to drop supporting Gradle 6.7 or lower.
 * Calling Action from such lower Gradle do not work if interfaces are originally written in Groovy.
 *
 * For the more details, please check https://github.com/DeployGate/gradle-deploygate-plugin/pull/144 out.
 */
public class NamedDeployment implements Named, DeploymentSyntax {
    @Nonnull
    private final String name;

    @Nullable
    private File sourceFile;

    @Nullable
    private String message;

    private boolean skipAssemble;

    @Nullable
    @Deprecated
    private String visibility;

    // Avoid using Optional like Guava for now because we want to reduce external dependencies as much as possible.
    @Nonnull
    private Distribution[] optionalDistribution;

    public NamedDeployment(@Nonnull String name) {
        this.name = name;
        this.optionalDistribution = new Distribution[]{new Distribution()};
    }

    @Override
    @Nonnull
    @Internal
    public String getName() {
        return name;
    }

    @Input
    public boolean isSkipAssemble() {
        return skipAssemble;
    }

    /**
     * for Kotlin properly access
     */

    @Internal
    public boolean getSkipAssemble() {
        return skipAssemble;
    }

    @Override
    public void setSkipAssemble(boolean skipAssemble) {
        this.skipAssemble = skipAssemble;
    }

    @Nullable
    @Internal
    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public void setSourceFile(@Nullable File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Nullable
    @Input
    @Optional
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    @Override
    public void distribution(@Nonnull Action<Distribution> builder) {
        builder.execute(getDistribution());
    }

    @Override
    public void distribution(@Nonnull Closure cl) {
        Distribution distribution = getDistribution();
        cl.setDelegate(distribution);
        cl.setResolveStrategy(Closure.DELEGATE_ONLY);
        cl.call(distribution);
    }

    @Internal
    public boolean hasDistribution() {
        return getDistribution().isPresent();
    }

    @Nonnull
    @Nested
    @Optional
    public Distribution getDistribution() {
        return optionalDistribution[0];
    }

    // backward compatibility

    @Override
    @Deprecated
    public void setVisibility(@Nullable String visibility) {
        DeprecationLogger.deprecation("NamedDeployment.setVisibility(String)", "2.5", "3.0", "This API has no effect and no alternative is available. You would see this message until ${DeployGatePlugin.ENV_NAME_APP_VISIBILITY} environment variable is removed.");
        _internalSetVisibility(visibility);
    }

    @Deprecated
    @Internal
    public String getVisibility() {
        DeprecationLogger.deprecation("NamedDeployment.getVisibility()", "2.5", "3.0", "This API has no effect and no alternative is available.");
        return _internalGetVisibility();
    }

    @Deprecated
    @Nullable
    @Internal
    public String getDistributionKey() {
        DeprecationLogger.deprecation("NamedDeployment.getDistributionKey()", "2.0", "3.0", "Use distribution closure directly.");
        return hasDistribution() ? getDistribution().getKey() : null;
    }

    @Deprecated
    public void setDistributionKey(@Nullable final String distributionKey) {
        DeprecationLogger.deprecation("NamedDeployment.setDistributionKey(String)", "2.0", "3.0", "Use distribution closure instead.");
        distribution(new Action<Distribution>() {
            @Override
            public void execute(Distribution distribution) {
                distribution.setKey(distributionKey);
            }
        });
    }

    @Deprecated
    @Nullable
    @Internal
    public String getReleaseNote() {
        DeprecationLogger.deprecation("NamedDeployment.getReleaseNote()", "2.0", "3.0", "Use distribution closure directly.");
        return hasDistribution() ? getDistribution().getReleaseNote() : null;
    }

    @Deprecated
    public void setReleaseNote(@Nullable final String releaseNote) {
        DeprecationLogger.deprecation("NamedDeployment.setReleaseNote(String)", "2.0", "3.0", "Use distribution closure instead.");
        distribution(new Action<Distribution>() {
            @Override
            public void execute(Distribution distribution) {
                distribution.setReleaseNote(releaseNote);
            }
        });
    }

    @Deprecated
    @Internal
    public boolean getNoAssemble() {
        DeprecationLogger.deprecation("NamedDeployment.getNoAssemble()", "2.0", "3.0", "Use isSkipAssemble() instead.");
        return isSkipAssemble();
    }

    @Deprecated
    public void setNoAssemble(boolean noAssemble) {
        DeprecationLogger.deprecation("NamedDeployment.setNoAssemble(boolean)", "2.0", "3.0", "Use setSkipAssemble() instead.");
        setSkipAssemble(noAssemble);
    }

    // end: backward compatibility

    // just for avoiding deprecation logging. non-public api.

    @Deprecated
    public void _internalSetVisibility(@Nullable String visibility) {
        this.visibility = visibility;
    }

    @Deprecated
    public String _internalGetVisibility() {
        return visibility;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (getClass() != o.getClass()) return false;

        NamedDeployment that = (NamedDeployment) o;

        if (skipAssemble != that.skipAssemble) return false;
        if (!getDistribution().equals(that.getDistribution())) return false;
        if (!name.equals(that.name)) return false;
        if (sourceFile != that.sourceFile) return false;
        if (!Objects.equals(message, that.message)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = name.hashCode();
        result = 31 * result + (sourceFile != null ? sourceFile.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (skipAssemble ? 1 : 0);
        result = 31 * result + getDistribution().hashCode();
        return result;
    }
}