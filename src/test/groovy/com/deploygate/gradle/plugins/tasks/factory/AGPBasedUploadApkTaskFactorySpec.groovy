package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.DirectApkInfo
import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat
import com.deploygate.gradle.plugins.internal.gradle.LazyConfigurableTask
import com.deploygate.gradle.plugins.tasks.UploadApkTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.annotation.Nonnull

class AGPBasedUploadApkTaskFactorySpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private NamedDomainObjectContainer<NamedDeployment> deployments

    @Nonnull
    private AGPBasedUploadApkTaskFactory agpBasedUploadApkTaskFactory

    def setup() {
        project = ProjectBuilder.builder().build()
        GradleCompat.init(project)
        deployments = project.container(NamedDeployment)

        project.extensions.add("deploygate", new DeployGateExtension(project, deployments))
    }

    def "registerAggregatedUploadApkTask should not be supported"() {
        given:
        agpBasedUploadApkTaskFactory = new AGPBasedUploadApkTaskFactory(project)

        when:
        agpBasedUploadApkTaskFactory.registerAggregatedUploadApkTask()

        then:
        thrown(IllegalAccessException)
    }

    @ConfineMetaClassChanges([PackageAppTaskCompat])
    def "registerUploadApkTask should add a UploadApkTask"() {
        setup:
        def variantName = "dep1"
        def variant = Mock(IApplicationVariant)
        variant.name >> variantName
        variant.lazyPackageApplication() >> Stub(LazyConfigurableTask, name: variantName)

        and:
        agpBasedUploadApkTaskFactory = new AGPBasedUploadApkTaskFactory(project)

        and:
        PackageAppTaskCompat.metaClass.static.getApkInfo = { LazyConfigurableTask _ ->
            new DirectApkInfo(variantName, null, true, true)
        }

        when:
        agpBasedUploadApkTaskFactory.registerUploadApkTask(variant)

        and:
        def task = project.tasks.findByName("uploadDeployGateDep1")

        then:
        task
        task instanceof UploadApkTask
    }

    @ConfineMetaClassChanges([PackageAppTaskCompat])
    def "registerUploadApkTask should modify the existing instance if already exist"() {
        given:
        def variantName = "dep1"
        def variant = Mock(IApplicationVariant)
        variant.name >> variantName
        variant.lazyPackageApplication() >> Stub(LazyConfigurableTask, name: variantName)

        and:
        agpBasedUploadApkTaskFactory = new AGPBasedUploadApkTaskFactory(project)

        and:
        PackageAppTaskCompat.metaClass.static.getApkInfo = { LazyConfigurableTask _ ->
            new DirectApkInfo(variantName, null, true, true)
        }

        when:
        agpBasedUploadApkTaskFactory.registerUploadApkTask(variant)

        and:
        def firstTask = project.tasks.findByName("uploadDeployGateDep1")

        then:
        firstTask

        when:
        agpBasedUploadApkTaskFactory.registerUploadApkTask(variant)

        and:
        def secondTask = project.tasks.findByName("uploadDeployGateDep1")

        then:
        firstTask == secondTask
    }
}
