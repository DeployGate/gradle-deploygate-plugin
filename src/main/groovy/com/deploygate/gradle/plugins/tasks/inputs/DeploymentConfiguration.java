package com.deploygate.gradle.plugins.tasks.inputs;

import static com.deploygate.gradle.plugins.internal.gradle.GradleCompat.forUseAtConfigurationTime;

import com.deploygate.gradle.plugins.DeployGatePlugin;
import com.deploygate.gradle.plugins.dsl.Distribution;
import com.deploygate.gradle.plugins.dsl.NamedDeployment;
import com.deploygate.gradle.plugins.internal.gradle.ProviderFactoryUtils;
import javax.inject.Inject;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DeploymentConfiguration {

    /** must be an absolute path */
    @Input
    @Optional
    @NotNull public abstract Property<String> getSourceFilePath();

    @Input
    @Optional
    @NotNull public abstract Property<String> getMessage();

    @Input
    @NotNull public abstract Property<Boolean> getSkipAssemble();

    @Input
    @Optional
    @NotNull public abstract Property<String> getDistributionKey();

    @Input
    @Optional
    @NotNull public abstract Property<String> getDistributionReleaseNote();

    @Inject
    public DeploymentConfiguration(
            @NotNull ProviderFactory providerFactory, @NotNull ProjectLayout projectLayout) {
        getSourceFilePath()
                .set(
                        forUseAtConfigurationTime(
                                        providerFactory.environmentVariable(
                                                DeployGatePlugin.getENV_NAME_SOURCE_FILE()))
                                .map(
                                        s ->
                                                projectLayout
                                                        .getProjectDirectory()
                                                        .file(s)
                                                        .getAsFile()
                                                        .getAbsolutePath()));
        getMessage()
                .set(
                        forUseAtConfigurationTime(
                                providerFactory.environmentVariable(
                                        DeployGatePlugin.getENV_NAME_MESSAGE())));
        getDistributionKey()
                .set(
                        forUseAtConfigurationTime(
                                providerFactory.environmentVariable(
                                        DeployGatePlugin.getENV_NAME_DISTRIBUTION_KEY())));
        getDistributionReleaseNote()
                .set(
                        ProviderFactoryUtils.pickFirst(
                                forUseAtConfigurationTime(
                                        providerFactory.environmentVariable(
                                                DeployGatePlugin
                                                        .getENV_NAME_DISTRIBUTION_RELEASE_NOTE())),
                                forUseAtConfigurationTime(
                                        providerFactory.environmentVariable(
                                                DeployGatePlugin
                                                        .getENV_NAME_DISTRIBUTION_RELEASE_NOTE_V1()))));
        getSkipAssemble().set(false);
    }

    public final void copyFrom(@Nullable NamedDeployment deployment) {
        if (deployment == null) {
            return;
        }

        if (deployment.getSourceFile() != null) {
            getSourceFilePath().set(deployment.getSourceFile().getAbsolutePath());
        }

        if (deployment.getMessage() != null) {
            getMessage().set(deployment.getMessage());
        }

        final Distribution distribution = deployment.getDistribution();

        if (distribution.getKey() != null) {
            getDistributionKey().set(distribution.getKey());
        }

        if (distribution.getReleaseNote() != null) {
            getDistributionReleaseNote().set(distribution.getReleaseNote());
        }

        getSkipAssemble().set(deployment.isSkipAssemble());
    }
}
