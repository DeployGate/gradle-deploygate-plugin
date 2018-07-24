package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.artifacts.ApkInfo
import com.deploygate.gradle.plugins.artifacts.ApkInfoCompat
import com.deploygate.gradle.plugins.entities.DeployGateExtension
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.tasks.LoginTask
import com.deploygate.gradle.plugins.tasks.LogoutTask
import com.deploygate.gradle.plugins.tasks.UploadTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class DeployGate implements Plugin<Project> {
    private static final String GROUP_NAME = 'DeployGate'
    private static final String LOGIN_TASK_NAME = 'loginDeployGate'
    private static final String LOGOUT_TASK_NAME = 'logoutDeployGate'

    @Override
    void apply(Project project) {
        def declaredVariantNames = setupExtension(project)

        project.afterEvaluate { prj ->
            if (['com.android.application', 'android'].any { prj.plugins.hasPlugin(it) }) {
                createDeployGateTasks(prj, declaredVariantNames)
            }
        }
        project.gradle.buildFinished { buildResult ->
            project.deploygate.notifyServer('finished', [result: Boolean.toString(buildResult.failure == null)])
        }
    }

    private static Set<String> setupExtension(Project project) {
        NamedDomainObjectContainer<DeployTarget> targets = project.container(DeployTarget)
        project.extensions.add('deploygate', new DeployGateExtension(targets))

        return targets.collect { it.name }.toSet()
    }

    def createDeployGateTasks(Project project, Set<String> declaredVariantNames) {
        project.task(LOGIN_TASK_NAME, type: LoginTask, group: GROUP_NAME)
        project.task(LOGOUT_TASK_NAME, type: LogoutTask, group: GROUP_NAME)

        createMultipleUploadTask(project, declaredVariantNames)

        def names = new HashSet(declaredVariantNames)

        // @see ApplicationVariantFactory#createVariantData
        // variant is for applicationFlavors
        project.android.applicationVariants.all { variant ->
            // variant is for splits
            variant.outputs.each { output ->
                def apkInfo = ApkInfoCompat.from(variant, output)

                createTask(project, apkInfo, output.assemble)

                names.remove(apkInfo.variantName)
            }
        }

        names.collect { ApkInfoCompat.blank(it) }.each { apkInfo ->
            createTask(project, apkInfo)
        }
    }

    private void createTask(Project project, ApkInfo apkInfo, Task assembleTask = null) {
        def tasksDependsOn = project.getTasksByName(LOGIN_TASK_NAME, false).toList()

        if (assembleTask && !project.deploygate.apks.findByName(apkInfo.variantName)?.noAssemble) {
            tasksDependsOn.add(0, assembleTask)
        }

        project.task([type: UploadTask, overwrite: true], "uploadDeployGate${apkInfo.variantName.capitalize()}") {

            def desc = "Deploy assembled ${apkInfo.variantName.capitalize()} to DeployGate"

            // require signing config to build a signed APKs
            if (!apkInfo.signingReady) {
                desc += " (requires valid signingConfig setting)"
            }

            description desc

            // universal builds show in DeployGate group
            if (apkInfo.universalApk) {
                group GROUP_NAME
            }

            dependsOn tasksDependsOn

            // UploadTask properties

            outputName apkInfo.variantName
            hasSigningConfig apkInfo.signingReady

            defaultSourceFile apkInfo.apkFile
        }
    }

    private static void createMultipleUploadTask(Project project, Set<String> declaredVariantNames) {
        if (declaredVariantNames.empty) {
            return
        }

        project.task('uploadDeployGate') {
            description 'Upload all builds defined in build.gradle to DeployGate'
            group GROUP_NAME

            // Don't need to let this task depend on undeclared variants
            dependsOn declaredVariantNames.collect { "uploadDeployGate${it.capitalize()}" }
        }
    }
}
