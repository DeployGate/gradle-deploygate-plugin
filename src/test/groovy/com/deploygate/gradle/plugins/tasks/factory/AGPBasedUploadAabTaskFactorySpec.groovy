package com.deploygate.gradle.plugins.tasks.factory

import com.deploygate.gradle.plugins.artifacts.DirectAabInfo
import com.deploygate.gradle.plugins.artifacts.PackageAppTaskCompat
import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.internal.gradle.GradleCompat

import com.deploygate.gradle.plugins.tasks.UploadAabTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.annotation.Nonnull

class AGPBasedUploadAabTaskFactorySpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private NamedDomainObjectContainer<NamedDeployment> deployments

    @Nonnull
    private AGPBasedUploadAabTaskFactory agpBasedUploadAabTaskFactory

    def setup() {
        project = ProjectBuilder.builder().build()
        GradleCompat.init(project)
        deployments = project.container(NamedDeployment)

        project.extensions.add("deploygate", new DeployGateExtension(project, deployments, new CliCredentialStore(File.createTempDir())))
    }

    def "registerAggregatedUploadApkTask should not be supported"() {
        given:
        agpBasedUploadAabTaskFactory = new AGPBasedUploadAabTaskFactory(project)

        when:
        agpBasedUploadAabTaskFactory.registerAggregatedUploadArtifactTask()

        then:
        thrown(IllegalAccessException)
    }

    @ConfineMetaClassChanges([PackageAppTaskCompat])
    def "registerUploadArtifactTask should add a UploadAabTask"() {
        setup:
        def variantName = "dep1"
        def variant = Mock(IApplicationVariant)
        variant.name >> variantName
        variant.packageApplicationTaskProvider() >> Stub(TaskProvider, name: variantName)

        and:
        agpBasedUploadAabTaskFactory = new AGPBasedUploadAabTaskFactory(project)

        and:
        PackageAppTaskCompat.metaClass.static.getAabInfo = { TaskProvider _ ->
            new DirectAabInfo(variantName, null)
        }

        when:
        agpBasedUploadAabTaskFactory.registerUploadArtifactTask(variant)

        and:
        def task = project.tasks.findByName("uploadDeployGateAabDep1")

        then:
        task
        task instanceof UploadAabTask
    }

    @ConfineMetaClassChanges([PackageAppTaskCompat])
    def "registerUploadArtifactTask should modify the existing instance if already exist"() {
        given:
        def variantName = "dep1"
        def variant = Mock(IApplicationVariant)
        variant.name >> variantName
        variant.packageApplicationTaskProvider() >> Stub(TaskProvider, name: variantName)

        and:
        agpBasedUploadAabTaskFactory = new AGPBasedUploadAabTaskFactory(project)

        and:
        PackageAppTaskCompat.metaClass.static.getAabInfo = { TaskProvider _ ->
            new DirectAabInfo(variantName, null)
        }

        when:
        agpBasedUploadAabTaskFactory.registerUploadArtifactTask(variant)

        and:
        def firstTask = project.tasks.findByName("uploadDeployGateAabDep1")

        then:
        firstTask

        when:
        agpBasedUploadAabTaskFactory.registerUploadArtifactTask(variant)

        and:
        def secondTask = project.tasks.findByName("uploadDeployGateAabDep1")

        then:
        firstTask == secondTask
    }
}
