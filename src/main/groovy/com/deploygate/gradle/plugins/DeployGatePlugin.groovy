package com.deploygate.gradle.plugins

import static com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat.getAabInfo
import static com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat.getApkInfo
import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidAssembleTaskName
import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidBundleTaskName

import com.deploygate.gradle.plugins.artifacts.DefaultPresetAabInfo
import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.DeprecationLogger
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariantImpl
import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.internal.http.HttpClient
import com.deploygate.gradle.plugins.tasks.Constants
import com.deploygate.gradle.plugins.tasks.LoginTask
import com.deploygate.gradle.plugins.tasks.LogoutTask
import com.deploygate.gradle.plugins.tasks.UploadAabTask
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
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

    @Override
    void apply(Project project) {
        DeprecationLogger.reset()

        setupExtension(project)

        GradleCompat.init(project)

        def httpClientProvider = project.gradle.sharedServices.registerIfAbsent("httpclient", HttpClient) { spec ->
            spec.parameters.endpoint.set(GradleCompat.forUseAtConfigurationTime(project.providers.environmentVariable("TEST_SERVER_URL")))
        }

        def loginTask = project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) { task ->
            DeployGateExtension deploygate = project.deploygate

            task.deployGateExtension = deploygate
            task.appOwnerName.set(deploygate.appOwnerName)
            task.apiToken.set(deploygate.apiToken)
            task.httpClient.set(httpClientProvider)
            task.usesService(httpClientProvider)
        }

        project.tasks.register(Constants.LOGOUT_TASK_NAME, LogoutTask) { task ->
            task.credentialStore = project.deploygate.credentialStore
        }

        project.tasks.register(Constants.SUFFIX_APK_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-apk tasks"

            task.group = Constants.TASK_GROUP_NAME
        }

        project.tasks.register(Constants.SUFFIX_AAB_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-aab tasks"

            task.group = Constants.TASK_GROUP_NAME
        }

        project.deploygate.deployments.configureEach { NamedDeployment deployment ->
            project.tasks.named(Constants.SUFFIX_APK_TASK_NAME).configure { task ->
                task.dependsOn(Constants.uploadApkTaskName(deployment.name))
            }

            project.tasks.named(Constants.SUFFIX_AAB_TASK_NAME).configure { task ->
                task.dependsOn(Constants.uploadAabTaskName(deployment.name))
            }

            project.tasks.register(Constants.uploadApkTaskName(deployment.name), UploadApkTask) { task ->
                if (!deployment.skipAssemble) {
                    task.logger.debug("${deployment.name} required assmble but ignored")
                }

                task.credentials.set(loginTask.map { it.credentials })
                task.deployment.copyFrom(deployment)
                task.apkInfo.set(new DefaultPresetApkInfo(deployment.name))
                task.httpClient.set(httpClientProvider)
                task.usesService(httpClientProvider)
                task.dependsOn(loginTask)
            }

            project.tasks.register(Constants.uploadAabTaskName(deployment.name), UploadAabTask) { task ->
                if (!deployment.skipAssemble) {
                    task.logger.debug("${deployment.name} required assmble but ignored")
                }

                task.credentials.set(loginTask.map { it.credentials })
                task.deployment.copyFrom(deployment)
                task.aabInfo.set(new DefaultPresetAabInfo(deployment.name))
                task.httpClient.set(httpClientProvider)
                task.usesService(httpClientProvider)
                task.dependsOn(loginTask)
            }
        }

        AndroidGradlePlugin.ifPresent(project) {
            project.android.applicationVariants.configureEach { /* ApplicationVariant */ variant ->
                def variantProxy = new IApplicationVariantImpl(variant)

                namedOrRegister(project, Constants.uploadApkTaskName(variantProxy.name), UploadApkTask).configure { task ->
                    task.credentials.set(loginTask.map { it.credentials })

                    task.apkInfo.set(variantProxy.packageApplicationTaskProvider().map {getApkInfo(it, variantProxy.name) })
                    task.httpClient.set(httpClientProvider)
                    task.usesService(httpClientProvider)

                    if (deployment.skipAssemble.get()) {
                        task.dependsOn(loginTask)
                    } else {
                        task.dependsOn(androidAssembleTaskName(variantProxy.name), loginTask)
                    }
                }

                namedOrRegister(project, Constants.uploadAabTaskName(variantProxy.name), UploadAabTask).configure { task ->
                    task.credentials.set(loginTask.map { it.credentials })

                    task.aabInfo.set(variantProxy.packageApplicationTaskProvider().map {getAabInfo(it, variantProxy.name, project.buildDir) })
                    task.httpClient.set(httpClientProvider)
                    task.usesService(httpClientProvider)

                    if (deployment.skipAssemble.get()) {
                        task.dependsOn(loginTask)
                    } else {
                        task.dependsOn(androidBundleTaskName(variantProxy.name), loginTask)
                    }
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
