package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.VariantBasedDeployTarget
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployGatePlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'deploygate'

    // env names must start with 'DEPLOYGATE_'
    static final String ENV_NAME_SOURCE_FILE = "DEPLOYGATE_SOURCE_FILE"
    static final String ENV_NAME_UPLOAD_MESSAGE = "DEPLOYGATE_MESSAGE"
    static final String ENV_NAME_DISTRIBUTION_KEY = "DEPLOYGATE_DISTRIBUTION_KEY"
    static final String ENV_NAME_RELEASE_NOTE = "DEPLOYGATE_RELEASE_NOTE"
    static final String ENV_NAME_VISIBILITY = "DEPLOYGATE_VISIBILITY"

    static final String ENV_NAME_OPEN_APP_DETAIL_AFTER_UPLOAD = "DEPLOYGATE_OPEN_BROWSER"

    private Processor processor

    @Override
    void apply(Project project) {
        setupExtension(project)
        initProcessor(project)

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private static void setupExtension(Project project) {
        NamedDomainObjectContainer<VariantBasedDeployTarget> targets = project.container(VariantBasedDeployTarget)
        project.extensions.add(EXTENSION_NAME, new DeployGateExtension(project, targets))
    }

    private void initProcessor(Project project) {
        processor = new Processor(this, project)

        project.deploygate.apks.all { VariantBasedDeployTarget target ->
            processor.addVariantOrCustomName(target.name)
        }
    }

    private void onProjectEvaluated(Project project) {
        project.gradle.buildFinished { buildResult ->
            project.deploygate.notifyServer('finished', [result: Boolean.toString(buildResult.failure == null)])
        }

        processor.registerLoginTask()
        processor.registerLogoutTask()

        processor.declaredNames.forEach { variantOrCustomName ->
            processor.registerDeclarationAwareUploadApkTask(variantOrCustomName)

            if (AndroidGradlePlugin.isAppBundleSupported()) {
                // TODO create tasks for app bundle
            }
        }

        processor.registerAggregatedDeclarationAwareUploadApkTask(processor.declaredNames)

        if (!processor.canProcessVariantAware()) {
            project.logger.warn("DeployGate Gradle Plugin is stopped because Android Gradle Plugin must be applied before.")
            return
        }

//        (project.android as com.android.build.gradle.AppExtension).applicationVariants.all { com.android.build.gradle.api.ApplicationVariant variant ->
        project.android.applicationVariants.all { variant ->
            processor.registerVariantAwareUploadApkTask(variant)

            if (AndroidGradlePlugin.isAppBundleSupported()) {
                // TODO create tasks for app bundle
            }
        }
    }

//    private void createFromAabUploadTasks(Project project, ApkInfo apkInfo) {
//        if (!AndroidGradlePlugin.isAppBundleSupported()) {
//            return
//        }
//
//        def tasksDependsOn = project.getTasksByName("loginDeployGate", false).toList()
//        def bundleTask = project.getTasksByName("bundle${apkInfo.variantName.capitalize()}", false)
//
//        if (!bundleTask.empty) {
//            tasksDependsOn.add(0, bundleTask.first())
//        }
//
//        project.task([type: UploadTask, overwrite: true], "uploadFromAabDeployGate${apkInfo.variantName.capitalize()}") {
//
//            description "Deploy an universal apk which is generated from an app bundle of ${apkInfo.variantName.capitalize()} to DeployGate"
//
//            group GROUP_NAME
//
//            dependsOn tasksDependsOn
//
//            // UploadTask properties
//
//            outputName apkInfo.variantName
//            hasSigningConfig apkInfo.signingConfig != null
//
//            defaultSourceFile apkInfo.apkFile
//            appBundleInfo new AppBundleInfo(apkInfo)
//        }
//    }
}
