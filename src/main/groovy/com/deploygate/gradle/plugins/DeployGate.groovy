package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.entities.DeployGateExtension
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.tasks.LoginTask
import com.deploygate.gradle.plugins.tasks.LogoutTask
import com.deploygate.gradle.plugins.tasks.UploadTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployGate implements Plugin<Project> {
    HashSet<String> tasksToCreate

    void apply(Project project) {
        tasksToCreate = new HashSet<>()
        setupExtension project
        project.afterEvaluate { prj ->
            if (['com.android.application', 'android'].any { prj.plugins.hasPlugin(it) }) {
                createDeployGateTasks prj
            }
        }
        project.gradle.buildFinished { buildResult ->
            project.deploygate.notifyServer 'finished', [ result: Boolean.toString(buildResult.failure == null) ]
        }
    }

    def setupExtension (Project project) {
        def apkTargets = project.container(DeployTarget)
        apkTargets.all {
            tasksToCreate.add name
        }
        project.extensions.add 'deploygate', new DeployGateExtension(apkTargets)
    }

    def createDeployGateTasks (project) {
        project.task 'logoutDeployGate', type: LogoutTask, group: 'DeployGate'
        def loginTask = project.task('loginDeployGate', type: LoginTask, group: 'DeployGate')

        createMultipleUploadTask(project, tasksToCreate)

        // @see ApplicationVariantFactory#createVariantData
        // variant is for applicationFlavors
        project.android.applicationVariants.all { variant ->
            // variant is for splits
            variant.outputs.eachWithIndex { output, idx ->
                createTask(project, output, loginTask)
                tasksToCreate.remove output.name
            }
        }

        tasksToCreate.each { name ->
            createTask(project, name, loginTask)
        }
    }

    private void createTask(project, output, loginTask) {
        def name
        def signingReady = true
        def isUniversal = true
        def assemble = null
        def outputFile = null

        if (output instanceof String) {
            name = output
        } else {
            name = output.name
            signingReady = output.variantOutputData.variantData.signed
            isUniversal = output.outputs.get(0).filters.size() == 0
            assemble = output.assemble
            outputFile = output.outputFile
        }

        def capitalized = name.capitalize()
        def taskName = "uploadDeployGate${capitalized}"
        project.task(taskName,
                type: UploadTask,
                dependsOn: ([ assemble, loginTask ] - null),
                overwrite: true) {

            def desc = "Deploy assembled ${capitalized} to DeployGate"

            // require signing config to build a signed APKs
            if (!signingReady) {
                desc += " (requires valid signingConfig setting)"
            }

            // universal builds show in DeployGate group
            if (isUniversal) {
                group 'DeployGate'
            }

            description desc
            outputName name
            hasSigningConfig signingReady

            defaultSourceFile outputFile
        }
    }

    def createMultipleUploadTask(Project project, HashSet<String> dependsOn) {
        if (dependsOn.empty) return
        project.task 'uploadDeployGate',
                dependsOn: dependsOn.toArray().collect { "uploadDeployGate${it.capitalize()}" },
                description: 'Upload all builds defined in build.gradle to DeployGate',
                group: 'DeployGate'
    }
}
