package com.deploygate.gradle.plugins

import com.android.build.gradle.AppExtension
import com.deploygate.gradle.plugins.entities.DeployGateExtension
import com.deploygate.gradle.plugins.entities.DeployTarget
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class DeployGatePlugin implements Plugin<Project> {
    private static final String EXTENSION_NAME = 'deploygate'

    // env names must start with 'DEPLOYGATE_'
    static final String ENV_NAME_SOURCE_FILE = "DEPLOYGATE_SOURCE_FILE"
    static final String ENV_NAME_UPLOAD_MESSAGE = "DEPLOYGATE_MESSAGE"
    static final String ENV_NAME_DISTRIBUTION_KEY = "DEPLOYGATE_DISTRIBUTION_KEY"
    static final String ENV_NAME_RELEASE_NOTE = "DEPLOYGATE_RELEASE_NOTE"
    static final String ENV_NAME_VISIBILITY = "DEPLOYGATE_VISIBILITY"

    enum ArtifactType {
        Apk,
        ApkViaAab
    }

    private Map<ArtifactType, List<String>> variantNamesMap = new ConcurrentHashMap<>()

    @Override
    void apply(Project project) {
        onPluginApplied(project)

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private void onPluginApplied(Project project) {
        NamedDomainObjectContainer<DeployTarget> targets = project.container(DeployTarget)
        project.extensions.add(EXTENSION_NAME, new DeployGateExtension(targets))

        def declaredNames = []

        targets.all { declaredNames += name }

        ArtifactType.values().each {
            variantNamesMap[it] = new CopyOnWriteArrayList<>(declaredNames)
        }
    }

    private void onProjectEvaluated(Project project) {
        if (!Processor.isProcessable(project)) {
            project.logger.warn("DeployGate Gradle Plugin cannot be applied")
            return
        }

        project.gradle.buildFinished { buildResult ->
            project.deploygate.notifyServer('finished', [result: Boolean.toString(buildResult.failure == null)])
        }

        Processor processor = new Processor(this, project)

        processor.registerLoginTask()
        processor.registerLogoutTask()

        variantNamesMap.forEach { artifactType, variantNames ->
            switch (artifactType) {
                case ArtifactType.Apk:
                    variantNames.forEach { variantName ->
                        processor.registerDeclarationAwareUploadApkTask(variantName)
                    }

                    processor.registerAggregatedDeclarationAwareUploadApkTask(variantNames)
                    break
                case ArtifactType.ApkViaAab:
                    break
            }
        }

        def androidExtension = project.android as AppExtension

        androidExtension.applicationVariants.all { /* com.android.build.gradle.api.ApplicationVariant */ variant ->
            processor.registerVariantAwareUploadApkTask(variant)
        }
    }

//    def createDeployGateTasks(Project project, Set<String> declaredVariantNames) {
////        createMultipleUploadApkTask(project, declaredVariantNames)
//
//        def names = new HashSet<String>(declaredVariantNames)
//
//        // @see ApplicationVariantFactory#createVariantData
//        // variant is for applicationFlavors
//        project.android.applicationVariants.all { variant ->
//            registerVariantAwareUploadApkTask(project, variant, loginTask)
//            names.remove(variant.name)
//        }
//
////        names.collect { ApkInfoCompat.blank(it) }.each { apkInfo ->
////            registerVariantAwareUploadApkTask(project, apkInfo)
////        }
//    }
//
//    private void registerVariantAwareUploadApkTask(Project project, ApkInfo apkInfo, Task assembleTask = null) {
//        def deployTarget = project.deploygate.apks.findByName(apkInfo.variantName) as DeployTarget
//        def tasksDependsOn = project.getTasksByName(LOGIN_TASK_NAME, false).toList()
//
//        if (assembleTask && !deployTarget?.noAssemble) {
//            tasksDependsOn.add(0, assembleTask)
//        }
//
//        // FIXME to support aab, uploadDeployGate naming will be deprecated.
//        project.task([type: UploadTask, overwrite: true], "uploadDeployGate${apkInfo.variantName.capitalize()}") {
//
//            def desc = "Deploy assembled ${apkInfo.variantName.capitalize()} to DeployGate"
//
//            // require signing config to build a signed APKs
//            if (!apkInfo.signingReady) {
//                desc += " (requires valid signingConfig setting)"
//            }
//
//            description desc
//
//            // universal builds show in DeployGate group
//            if (apkInfo.universalApk) {
//                group GROUP_NAME
//            }
//
//            dependsOn tasksDependsOn
//
//            // UploadTask properties
//
//            outputName apkInfo.variantName
//            hasSigningConfig apkInfo.signingReady
//
//            defaultSourceFile apkInfo.apkFile
//        }
//    }
//
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
//
//    private static void createMultipleUploadApkTask(Project project, Set<String> declaredVariantNames) {
//        if (declaredVariantNames.empty) {
//            return
//        }
//
//        // FIXME to support aab, uploadDeployGate naming will be deprecated.
//        project.task('uploadDeployGate') {
//            description 'Upload all builds defined in build.gradle to DeployGate'
//            group GROUP_NAME
//
//            // Don't need to let this task depend on undeclared variants
//            dependsOn declaredVariantNames.collect { "uploadDeployGate${it.capitalize()}" }
//        }
//    }
}
