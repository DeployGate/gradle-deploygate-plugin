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
    }

    def setupExtension (Project project) {
        def apkTargets = project.container(ApkTarget)
        apkTargets.all {
            tasksToCreate.add name
        }
        project.extensions.add 'deploygate', new DeployGateExtension(apkTargets)
    }

    def createDeployGateTasks (project) {
        def loginTask = project.task('deployGateLogin', type: DeployGateSetupCredentialTask, group: 'DeployGate')

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
        def taskName = "deployGateUpload${capitalized}"
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

    private void updateRunConfigurations(workspaceFile) {
        int state = 0
        def preLines = new ArrayList<String>()
        def postLines = new ArrayList<String>()
        workspaceFile.eachLine { line ->
            if (state == 0) {
                if (line =~ /<component.+name="RunManager"/) {
                    state = 1
                    return
                }
                preLines.add line
            }
            if (state == 1) {
                if (line =~ /<\/component>/) {
                    state = 2
                    return
                }
            }
            if (state == 2) {
                postLines.add line
            }
        }

        def xml = new XmlParser().parse(workspaceFile)
        def runManager = xml.component.find { it.@name.equals('RunManager') }
        clearDeployGateRunConfig(runManager)
        addDeployGateRunConfig(runManager)

        workspaceFile.renameTo workspaceFile.getPath() + '.orig'
        workspaceFile.withPrintWriter { writer ->
            preLines.each { line -> writer.println line }

            def printer = new IndentPrinter(writer)
            printer.incrementIndent()
            def xmlNodePrinter = new XmlNodePrinter(printer)
            xmlNodePrinter.print runManager
            printer.flush()

            postLines.each { line -> writer.println line }
        }
    }

    private void clearDeployGateRunConfig(runManager) {
        runManager.configuration.findAll {
            it.@type == 'GradleRunConfiguration' && it.ExternalSystemSettings.option.find {
                it.@name == 'taskDescriptions'
            }.list.option.any { it.@name == 'generatedBy' && it.@value == 'DeployGate' }
        }.each { it.replaceNode {} }
    }

    private void addDeployGateRunConfig(runManager) {
        runManager.configuration.plus {
            configuration(default: false, name: 'DeployGateUpload', type: 'GradleRunConfiguration', factoryName: 'Gradle', temporary: true) {
                ExternalSystemSettings {
                    option name: 'executionName'
                    option name: 'externalProjectPath', value: '$PROJECT_DIR$'
                    option name: 'externalSystemIdString', value: 'GRADLE'
                    option name: 'scriptParameters', value: ''
                    option(name: 'taskDescriptions') {
                        list {
                            option name: 'generatedBy', value: 'DeployGate'
                        }
                    }
                    option(name: 'taskNames') {
                        list {
                            option value: 'uploadDeployGate'
                        }
                    }
                    option name: 'vmOptions', value: ''
                }
                method()
            }
        }
    }
}



