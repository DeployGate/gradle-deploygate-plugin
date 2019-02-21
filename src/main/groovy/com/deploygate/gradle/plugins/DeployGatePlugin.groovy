package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariantImpl
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.annotation.Nonnull

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
        GradleCompat.init(project)
        AndroidGradlePlugin.init(project)
        initProcessor(project)

        project.afterEvaluate { Project evaluatedProject ->
            onProjectEvaluated(evaluatedProject)
        }
    }

    private static void setupExtension(Project project) {
        NamedDomainObjectContainer<NamedDeployment> deployments = project.container(NamedDeployment)
        project.extensions.add(EXTENSION_NAME, new DeployGateExtension(project, deployments))
    }

    private void initProcessor(@Nonnull Project project) {
        processor = new Processor(project)

        project.deploygate.apks.all { NamedDeployment deployment ->
            processor.addVariantOrCustomName(deployment.name)
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

        GradleCompat.configureEach(project.android.applicationVariants) { /* ApplicationVariant */ variant ->
            def variantProxy = new IApplicationVariantImpl(variant)

            processor.registerVariantAwareUploadApkTask(variantProxy)

            if (AndroidGradlePlugin.isAppBundleSupported()) {
                // TODO create tasks for app bundle
            }
        }
    }
}
