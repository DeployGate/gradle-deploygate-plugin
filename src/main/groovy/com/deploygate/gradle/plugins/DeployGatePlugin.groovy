package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.artifacts.DefaultPresetAabInfo
import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
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
import com.deploygate.gradle.plugins.tasks.UploadAabTask
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.annotations.NotNull

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

        setupExtension(project)

        GradleCompat.init(project)
        AndroidGradlePlugin.init(project)

        processor = new Processor(project)

        project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) {
            it.description = "Check the configured credentials and launch the authentication flow if they are not enough."

            it.group = Constants.TASK_GROUP_NAME
            it.deployGateExtension = project.deploygate
        }

        project.tasks.register(Constants.LOGOUT_TASK_NAME, LogoutTask) {
            it.description = "Remove the local persisted credentials."

            it.group = Constants.TASK_GROUP_NAME
            it.credentialStore = project.deploygate.credentialStore
        }

        project.tasks.register(DeployGateTaskFactory.SUFFIX_APK_TASK_NAME, DefaultTask).configure {
            it.group = DeployGateTaskFactory.GROUP_NAME
        }

        project.tasks.register(DeployGateTaskFactory.SUFFIX_AAB_TASK_NAME, DefaultTask).configure {
            it.group = DeployGateTaskFactory.GROUP_NAME
        }

        project.deploygate.deployments.configureEach { d ->
            project.tasks.named(DeployGateTaskFactory.SUFFIX_APK_TASK_NAME) {
                it.dependsOn(DeployGateTaskFactory.uploadApkTaskName(d.name))
            }

            project.tasks.named(DeployGateTaskFactory.SUFFIX_APK_TASK_NAME) {
                it.dependsOn(DeployGateTaskFactory.uploadAabTaskName(d.name))
            }

            project.tasks.register(DeployGateTaskFactory.uploadApkTaskName(d.name), UploadApkTask) {
                final NamedDeployment deployment = project.deploygate.findDeploymentByName(d.name)

                if (!deployment.skipAssemble) {
                    project.logger.debug("${d.name} required assmble but ignored")
                }

                it.variantName = d.name
                it.dependsOn(Constants.LOGIN_TASK_NAME)

                it.configuration = UploadApkTask.createConfiguration(deployment, new DefaultPresetApkInfo(d.name))
                it.applyTaskProfile()
            }

            project.tasks.register(DeployGateTaskFactory.uploadAabTaskName(d.name), UploadAabTask) {
                final NamedDeployment deployment = project.deploygate.findDeploymentByName(d.name)

                if (!deployment.skipAssemble) {
                    project.logger.debug("${d.name} required assmble but ignored")
                }

                it.variantName = d.name
                it.dependsOn(Constants.LOGIN_TASK_NAME)

                it.configuration = UploadAabTask.createConfiguration(deployment, new DefaultPresetAabInfo(d.name))
                it.applyTaskProfile()
            }
        }

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private static void setupExtension(@NotNull Project project) {
        CliCredentialStore credentialStore = new CliCredentialStore()
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        // TODO we should use ExtensionSyntax as the 1st argument but we need to investigate the expected side effects first.
        project.extensions.create(DeployGateExtension, EXTENSION_NAME, DeployGateExtension, project, deployments, credentialStore)
    }

    private void onProjectEvaluated(Project project) {
        if (!processor.canProcessVariantAware()) {
            project.logger.warn("DeployGate Gradle Plugin is stopped because Android Gradle Plugin must be applied before.")
            return
        }

        // TODO use artifact API
        project.android.applicationVariants.configureEach { /* ApplicationVariant */ variant ->
            def variantProxy = new IApplicationVariantImpl(variant)

            processor.registerVariantAwareUploadApkTask(variantProxy)
            processor.registerVariantAwareUploadAabTask(variantProxy)
        }
    }
}
