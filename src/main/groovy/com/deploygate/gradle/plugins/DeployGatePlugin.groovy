package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.artifacts.DefaultPresetAabInfo
import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
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
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.annotations.NotNull

import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidAssembleTaskName
import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidBundleTaskName

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

    @Override
    void apply(Project project) {
        DeprecationLogger.reset()

        setupExtension(project)

        GradleCompat.init(project)

        project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) { task ->
            task.description = "Check the configured credentials and launch the authentication flow if they are not enough."

            task.group = Constants.TASK_GROUP_NAME
            task.deployGateExtension = project.deploygate
        }

        project.tasks.register(Constants.LOGOUT_TASK_NAME, LogoutTask) { task ->
            task.description = "Remove the local persisted credentials."

            task.group = Constants.TASK_GROUP_NAME
            task.credentialStore = project.deploygate.credentialStore
        }

        project.tasks.register(DeployGateTaskFactory.SUFFIX_APK_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-apk tasks"

            task.group = DeployGateTaskFactory.GROUP_NAME
        }

        project.tasks.register(DeployGateTaskFactory.SUFFIX_AAB_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-aab tasks"

            task.group = DeployGateTaskFactory.GROUP_NAME
        }

        project.deploygate.deployments.configureEach { NamedDeployment d ->
            project.tasks.named(DeployGateTaskFactory.SUFFIX_APK_TASK_NAME).configure { task ->
                task.dependsOn(DeployGateTaskFactory.uploadApkTaskName(d.name))
            }

            project.tasks.named(DeployGateTaskFactory.SUFFIX_AAB_TASK_NAME).configure { task ->
                task.dependsOn(DeployGateTaskFactory.uploadAabTaskName(d.name))
            }

            project.tasks.register(DeployGateTaskFactory.uploadApkTaskName(d.name), UploadApkTask) { task ->
                final NamedDeployment deployment = project.deploygate.findDeploymentByName(d.name)

                if (!deployment.skipAssemble) {
                    task.logger.debug("${d.name} required assmble but ignored")
                }

                task.variantName = d.name
                task.dependsOn(Constants.LOGIN_TASK_NAME)

                task.configuration = UploadApkTask.createConfiguration(deployment, new DefaultPresetApkInfo(d.name))
                task.applyTaskProfile()
            }

            project.tasks.register(DeployGateTaskFactory.uploadAabTaskName(d.name), UploadAabTask) { task ->
                final NamedDeployment deployment = project.deploygate.findDeploymentByName(d.name)

                if (!deployment.skipAssemble) {
                    task.logger.debug("${d.name} required assmble but ignored")
                }

                task.variantName = d.name
                task.dependsOn(Constants.LOGIN_TASK_NAME)

                task.configuration = UploadAabTask.createConfiguration(deployment, new DefaultPresetAabInfo(d.name))
                task.applyTaskProfile()
            }
        }

        AndroidGradlePlugin.ifPresent(project) {
            project.android.applicationVariants.configureEach { /* ApplicationVariant */ variant ->
                def variantProxy = new IApplicationVariantImpl(variant)

                namedOrRegister(project, DeployGateTaskFactory.uploadApkTaskName(variantProxy.name), UploadApkTask).configure { task ->
                    final NamedDeployment deployment = project.deploygate.findDeploymentByName(variantProxy.name)

                    task.variantName = variantProxy.name

                    if (deployment?.skipAssemble) {
                        task.dependsOn(Constants.LOGIN_TASK_NAME)
                    } else {
                        task.dependsOn(androidAssembleTaskName(variantProxy.name), Constants.LOGIN_TASK_NAME)
                    }

                    // evaluate the provider here to fix https://github.com/DeployGate/gradle-deploygate-plugin/issues/86
                    def packageAppTask = variantProxy.packageApplicationTaskProvider().get()

                    task.configuration =  UploadApkTask.createConfiguration(deployment, PackageAppTaskCompat.getApkInfo(packageAppTask, variantProxy.name))
                    task.applyTaskProfile()
                }

                namedOrRegister(project, DeployGateTaskFactory.uploadAabTaskName(variantProxy.name), UploadAabTask).configure { task ->
                    final NamedDeployment deployment = project.deploygate.findDeploymentByName(variantProxy.name)

                    task.variantName = variantProxy.name

                    if (deployment?.skipAssemble) {
                        task.dependsOn(Constants.LOGIN_TASK_NAME)
                    } else {
                        task.dependsOn(androidBundleTaskName(variantProxy.name), Constants.LOGIN_TASK_NAME)
                    }

                    // evaluate the provider here to fix https://github.com/DeployGate/gradle-deploygate-plugin/issues/86
                    def packageAppTask = variantProxy.packageApplicationTaskProvider().get()

                    task.configuration =  UploadAabTask.createConfiguration(deployment, PackageAppTaskCompat.getAabInfo(packageAppTask, variantProxy.name, project.buildDir))
                    task.applyTaskProfile()
                }
            }
        }
    }

    private static <T extends Task> TaskProvider<T> namedOrRegister(@NotNull Project project, @NotNull String name, @NotNull Class<T> klass) {
        if (project.tasks.names.contains(name)) {
            return project.tasks.named(name, klass)
        } else {
            return project.tasks.register(name, klass)
        }
    }

    private static void setupExtension(@NotNull Project project) {
        CliCredentialStore credentialStore = new CliCredentialStore()
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        // TODO we should use ExtensionSyntax as the 1st argument but we need to investigate the expected side effects first.
        project.extensions.create(DeployGateExtension, EXTENSION_NAME, DeployGateExtension, project, deployments, credentialStore)
    }
}
