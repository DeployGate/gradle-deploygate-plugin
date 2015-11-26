package com.deploygate.gradle.plugins

import org.apache.commons.lang.WordUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class DeployGate implements Plugin<Project> {
    HashSet<String> tasksToCreate

    void apply(Project project) {
        tasksToCreate = new HashSet<>()
        setupExtension project
        project.afterEvaluate {
            if (it.plugins.hasPlugin('com.android.application')) {
                createDeployGateTasks it
            }
        }
        project.gradle.buildFinished { buildResult ->
            project.deploygate.notifyServer 'finished', [ result: Boolean.toString(buildResult.failure == null) ]
        }
    }

    def setupExtension (Project project) {
        def apkTargets = project.container(ApkTarget)
        apkTargets.all {
            tasksToCreate.add name
        }
        project.extensions.add 'deploygate', new DeployGateExtension(apkTargets)
    }

    def createDeployGateTasks (project) {
        project.task 'logoutDeployGate', type: DeployGateRemoveCredentialTask, group: 'DeployGate'
        def loginTask = project.task('loginDeployGate', type: DeployGateSetupCredentialTask, group: 'DeployGate')

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

        def capitalized = WordUtils.capitalize(name)
        def taskName = "uploadDeployGate${capitalized}"
        project.task(taskName,
                type: DeployGateUploadTask,
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
}



