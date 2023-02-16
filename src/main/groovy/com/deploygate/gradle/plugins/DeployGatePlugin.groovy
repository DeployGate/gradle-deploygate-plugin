package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.DeprecationLogger
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariantImpl
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.Constants
import com.deploygate.gradle.plugins.tasks.LoginTask
import com.deploygate.gradle.plugins.tasks.LogoutTask
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull

import javax.annotation.Nonnull

class DeployGatePlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'deploygate'

    // env names must start with 'DEPLOYGATE_'
    static final String ENV_NAME_APP_OWNER_NAME = "DEPLOYGATE_APP_OWNER_NAME"
    @Deprecated
    static final String ENV_NAME_APP_OWNER_NAME_V1 = "DEPLOYGATE_USER_NAME"
    static final String ENV_NAME_API_TOKEN = "DEPLOYGATE_API_TOKEN"
    static final String ENV_NAME_SOURCE_FILE = "DEPLOYGATE_SOURCE_FILE"
    static final String ENV_NAME_MESSAGE = "DEPLOYGATE_MESSAGE"
    static final String ENV_NAME_DISTRIBUTION_KEY = "DEPLOYGATE_DISTRIBUTION_KEY"
    static final String ENV_NAME_DISTRIBUTION_RELEASE_NOTE = "DEPLOYGATE_DISTRIBUTION_RELEASE_NOTE"
    @Deprecated
    static final String ENV_NAME_DISTRIBUTION_RELEASE_NOTE_V1 = "DEPLOYGATE_RELEASE_NOTE"
    @Deprecated
    static final String ENV_NAME_APP_VISIBILITY = "DEPLOYGATE_VISIBILITY"

    static final String ENV_NAME_OPEN_APP_DETAIL_AFTER_UPLOAD = "DEPLOYGATE_OPEN_BROWSER"

    private Processor processor

    @Override
    void apply(Project project) {
        DeprecationLogger.reset()

        def credentialStore = new CliCredentialStore()
        def extension = setupExtension(project, credentialStore)

        GradleCompat.init(project)
        AndroidGradlePlugin.init(project)
        initProcessor(project)

        project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) {
            it.description = "Check the configured credentials and launch the authentication flow if they are not enough."

            it.group = Constants.TASK_GROUP_NAME
            it.deployGateExtension = extension
            it.credentialStore = credentialStore
        }

        project.tasks.register(Constants.LOGOUT_TASK_NAME, LogoutTask) {
            it.description = "Remove the local persisted credentials"

            it.group = Constants.TASK_GROUP_NAME
            it.credentialStore = credentialStore
        }

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private static DeployGateExtension setupExtension(@NotNull Project project, @NotNull CliCredentialStore credentialStore) {
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        DeployGateExtension extension = new DeployGateExtension(project, deployments, credentialStore)
        project.extensions.add(EXTENSION_NAME, extension)
        return extension
    }

    private void initProcessor(@Nonnull Project project) {
        processor = new Processor(project)

        GradleCompat.configureEach(project.deploygate.deployments) { NamedDeployment deployment ->
            processor.addVariantOrCustomName(deployment.name)
        }
    }

    private void onProjectEvaluated(Project project) {
        processor.declaredNames.forEach { variantOrCustomName ->
            processor.registerDeclarationAwareUploadApkTask(variantOrCustomName)
            processor.registerDeclarationAwareUploadAabTask(variantOrCustomName)
        }

        processor.registerAggregatedDeclarationAwareUploadApkTask(processor.declaredNames)
        processor.registerAggregatedDeclarationAwareUploadAabTask(processor.declaredNames)

        if (!processor.canProcessVariantAware()) {
            project.logger.warn("DeployGate Gradle Plugin is stopped because Android Gradle Plugin must be applied before.")
            return
        }

        GradleCompat.configureEach(project.android.applicationVariants) { /* ApplicationVariant */ variant ->
            def variantProxy = new IApplicationVariantImpl(variant)

            processor.registerVariantAwareUploadApkTask(variantProxy)
            processor.registerVariantAwareUploadAabTask(variantProxy)
        }
    }
}
