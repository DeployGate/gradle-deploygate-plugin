package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.DeprecationLogger
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariantImpl
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.tasks.Constants
import com.deploygate.gradle.plugins.tasks.LoginTask
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

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

        def extension = setupExtension(project)

        GradleCompat.init(project)
        AndroidGradlePlugin.init(project)
        initProcessor(project)

        project.tasks.register(Constants.LOGIN_TASK_NAME, LoginTask) {
            group = Constants.TASK_GROUP_NAME
            deployGateExtension = extension
        }

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private static DeployGateExtension setupExtension(Project project) {
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        DeployGateExtension extension = new DeployGateExtension(project, deployments)
        project.extensions.add(EXTENSION_NAME, extension)
        return extension
    }

    private void initProcessor(@Nonnull Project project) {
        processor = new Processor(project)

        GradleCompat.configureEach(project.deploygate.deployments) { NamedDeployment deployment ->
            processor.addVariantOrCustomName(deployment.name)
        }
    }

    private void onProjectEvaluated(Project project) {
        processor.registerLogoutTask()

        processor.declaredNames.forEach { variantOrCustomName ->
            processor.registerDeclarationAwareUploadApkTask(variantOrCustomName)
            processor.registerDeclarationAwareUploadAabTask(variantOrCustomName)
        }

        processor.registerAggregatedDeclarationAwareUploadApkTask(processor.declaredNames)
        processor.registerAggregatedDeclarationAwareUploadAabTask(processor.declaredNames)

        if (!processor.canProcessVariantAware()) {
            project.logger.warn("DeployGate Gradle Plugin is stopped because Android Gradle Plugin must be applied before.")
            return
        }

        GradleCompat.configureEach(project.android.applicationVariants) { /* ApplicationVariant */ variant ->
            def variantProxy = new IApplicationVariantImpl(variant)

            processor.registerVariantAwareUploadApkTask(variantProxy)
            processor.registerVariantAwareUploadAabTask(variantProxy)
        }
    }
}
