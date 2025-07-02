package com.deploygate.gradle.plugins

import static com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat.getAabInfo
import static com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat.getApkInfo
import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidAssembleTaskName
import static com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin.androidBundleTaskName
import static com.deploygate.gradle.plugins.internal.gradle.ProviderFactoryUtils.environmentVariable

import com.deploygate.gradle.plugins.artifacts.DefaultPresetAabInfo
import com.deploygate.gradle.plugins.artifacts.DefaultPresetApkInfo
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.DeprecationLogger
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariantImpl
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.internal.http.HttpClient
import com.deploygate.gradle.plugins.internal.http.LocalServer
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
import org.gradle.api.provider.Provider
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

        // the presence of the value is same to the existence of the directory.
        // Defer file operations to execution time for Configuration Cache compatibility
        Provider<String> credentialDirPathProvider = project.providers.systemProperty("user.home").map { home ->
            new File(home, '.dg').absolutePath
        }

        // Detect AGP version for HttpClient
        def agpVersionProvider = project.providers.provider {
            try {
                def agpPlugin = project.plugins.findPlugin("com.android.application")
                if (agpPlugin) {
                    return AndroidGradlePlugin.getVersionString(agpPlugin.class.classLoader)
                }
            } catch (Throwable ignored) {
            }
            return "unknown"
        }

        def httpClientProvider = project.gradle.sharedServices.registerIfAbsent("httpclient", HttpClient) { spec ->
            spec.parameters.endpoint.set(environmentVariable(project.providers, "TEST_SERVER_URL").orElse(Config.getDEPLOYGATE_ROOT()))
            spec.parameters.agpVersion.set(agpVersionProvider)
        }

        def localServerProvider = project.gradle.sharedServices.registerIfAbsent("httpserver", LocalServer) { spec ->
            spec.parameters.httpClient.set(httpClientProvider)
            spec.parameters.credentialsDirPath.set(credentialDirPathProvider)
        }

        // Use Provider API for configuration cache compatibility
        def extension = project.extensions.getByName(EXTENSION_NAME) as DeployGateExtension
        def appOwnerNameProvider = project.providers.provider { extension.appOwnerName }
        def apiTokenProvider = project.providers.provider { extension.apiToken }
        def endpointProvider = project.providers.provider { extension.endpoint }
        def openBrowserProvider = environmentVariable(project.providers, ENV_NAME_OPEN_APP_DETAIL_AFTER_UPLOAD).map { it?.toBoolean() ?: false }

        def loginTaskProvider = project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) { task ->
            task.explicitAppOwnerName.set(appOwnerNameProvider)
            task.explicitApiToken.set(apiTokenProvider)
            task.credentialsDirPath.set(credentialDirPathProvider)
            task.httpClient.set(httpClientProvider)
            task.localServer.set(localServerProvider)
            task.usesService(httpClientProvider)
            task.usesService(localServerProvider)
        }

        project.tasks.register(Constants.LOGOUT_TASK_NAME, LogoutTask) { task ->
            task.credentialsDirPath.set(credentialDirPathProvider)
        }

        project.tasks.register(Constants.SUFFIX_APK_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-apk tasks"

            task.group = Constants.TASK_GROUP_NAME
        }

        project.tasks.register(Constants.SUFFIX_AAB_TASK_NAME, DefaultTask).configure { task ->
            task.description = "Execute all custom upload-aab tasks"

            task.group = Constants.TASK_GROUP_NAME
        }

        extension.deployments.configureEach { NamedDeployment deployment ->
            project.tasks.named(Constants.SUFFIX_APK_TASK_NAME).configure { task ->
                task.dependsOn(Constants.uploadApkTaskName(deployment.name))
            }

            project.tasks.named(Constants.SUFFIX_AAB_TASK_NAME).configure { task ->
                task.dependsOn(Constants.uploadAabTaskName(deployment.name))
            }

            project.tasks.register(Constants.uploadApkTaskName(deployment.name), UploadApkTask) { task ->
                task.description = "Deploy assembled ${deployment.name} APK to DeployGate"

                if (!deployment.skipAssemble) {
                    task.logger.debug("${deployment.name} required assmble but ignored")
                }

                task.credentials.set(loginTaskProvider.map { it.credentials })
                task.deployment.copyFrom(deployment)
                task.apkInfo.set(new DefaultPresetApkInfo(deployment.name))
                task.httpClient.set(httpClientProvider)
                task.endpoint.set(endpointProvider)
                task.openBrowserAfterUpload.set(openBrowserProvider)
                task.usesService(httpClientProvider)
                task.dependsOn(loginTaskProvider)
            }

            project.tasks.register(Constants.uploadAabTaskName(deployment.name), UploadAabTask) { task ->
                task.description = "Deploy bundled ${deployment.name} AAB to DeployGate"

                if (!deployment.skipAssemble) {
                    task.logger.debug("${deployment.name} required assmble but ignored")
                }

                task.credentials.set(loginTaskProvider.map { it.credentials })
                task.deployment.copyFrom(deployment)
                task.aabInfo.set(new DefaultPresetAabInfo(deployment.name))
                task.httpClient.set(httpClientProvider)
                task.endpoint.set(endpointProvider)
                task.openBrowserAfterUpload.set(openBrowserProvider)
                task.usesService(httpClientProvider)
                task.dependsOn(loginTaskProvider)
            }
        }

        AndroidGradlePlugin.ifPresent(project) {
            project.android.applicationVariants.configureEach { /* ApplicationVariant */ variant ->
                def variantProxy = new IApplicationVariantImpl(variant)

                namedOrRegister(project, Constants.uploadApkTaskName(variantProxy.name), UploadApkTask).configure { task ->
                    task.description = "Deploy assembled ${variantProxy.name} APK to DeployGate"
                    task.credentials.set(loginTaskProvider.map { it.credentials })

                    task.apkInfo.set(variantProxy.packageApplicationTaskProvider().map {getApkInfo(it, variantProxy.name) })
                    task.httpClient.set(httpClientProvider)
                    task.endpoint.set(endpointProvider)
                    task.openBrowserAfterUpload.set(openBrowserProvider)
                    task.usesService(httpClientProvider)

                    if (deployment.skipAssemble.get()) {
                        task.dependsOn(loginTaskProvider)
                    } else {
                        task.dependsOn(androidAssembleTaskName(variantProxy.name), loginTaskProvider)
                    }
                }

                namedOrRegister(project, Constants.uploadAabTaskName(variantProxy.name), UploadAabTask).configure { task ->
                    task.description = "Deploy bundled ${variantProxy.name} AAB to DeployGate"
                    task.credentials.set(loginTaskProvider.map { it.credentials })

                    task.aabInfo.set(variantProxy.packageApplicationTaskProvider().map {getAabInfo(it, variantProxy.name, project.layout.buildDirectory.get().asFile) })
                    task.httpClient.set(httpClientProvider)
                    task.endpoint.set(endpointProvider)
                    task.openBrowserAfterUpload.set(openBrowserProvider)
                    task.usesService(httpClientProvider)

                    if (deployment.skipAssemble.get()) {
                        task.dependsOn(loginTaskProvider)
                    } else {
                        task.dependsOn(androidBundleTaskName(variantProxy.name), loginTaskProvider)
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
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        // TODO we should use ExtensionSyntax as the 1st argument but we need to investigate the expected side effects first.
        project.extensions.create(DeployGateExtension, EXTENSION_NAME, DeployGateExtension, deployments)
    }
}
