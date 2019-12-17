package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.factory.LoginTaskFactory
import com.deploygate.gradle.plugins.tasks.factory.LogoutTaskFactory
import com.deploygate.gradle.plugins.tasks.factory.UploadArtifactTaskFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import javax.annotation.Nonnull

class ProcessorSpec extends Specification {
    @Nonnull
    private Project project

    @Nonnull
    private LoginTaskFactory loginTaskFactory

    @Nonnull
    private LogoutTaskFactory logoutTaskFactory

    @Nonnull
    private UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private UploadArtifactTaskFactory<String> stringBasedUploadApkTaskFactory

    @Nonnull
    private Processor processor

    def setup() {
        project = ProjectBuilder.builder().build()
        loginTaskFactory = Mock()
        logoutTaskFactory = Mock()
        applicationVariantBasedUploadApkTaskFactory = Mock()
        stringBasedUploadApkTaskFactory = Mock()
    }

    def "addVariantOrCustomName should store given names except empty"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.addVariantOrCustomName("")

        then:
        processor.declaredNames.isEmpty()

        when:
        processor.addVariantOrCustomName("dep1")

        then:
        processor.declaredNames.contains("dep1")

        when:
        processor.addVariantOrCustomName("dep2")

        then:
        processor.declaredNames.contains("dep1")
        processor.declaredNames.contains("dep2")

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "registerLoginTask should manipulate LoginTaskFactory"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerLoginTask()

        then:
        1 * loginTaskFactory.registerLoginTask()

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "registerLogoutTask should manipulate LogoutTaskFactory"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerLogoutTask()

        then:
        1 * logoutTaskFactory.registerLogoutTask()

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "registerDeclarationAwareUploadApkTask should manipulate String-based UploadApkTaskFactory"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerDeclarationAwareUploadApkTask("dep1")

        then:
        1 * stringBasedUploadApkTaskFactory.registerUploadArtifactTask("dep1", LoginTaskFactory.TASK_NAME)

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "registerAggregatedDeclarationAwareUploadApkTask should collect upload tasks"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerAggregatedDeclarationAwareUploadApkTask(["dep1", "dep2", "dep3"])

        then:
        1 * stringBasedUploadApkTaskFactory.registerAggregatedUploadArtifactTask(["uploadDeployGateDep1", "uploadDeployGateDep2", "uploadDeployGateDep3"])

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    def "registerVariantAwareUploadApkTask should not do nothing unless AndroidGradlePlugin is applied"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)
        IApplicationVariant applicationVariant = Mock()

        and:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            false
        }

        when:
        processor.registerVariantAwareUploadApkTask(applicationVariant)

        then:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    def "registerVariantAwareUploadApkTask should manipulate IApplicationVariant-based UploadApkTaskFactory if AndroidGradlePlugin is applied"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)
        IApplicationVariant applicationVariant = Mock()

        and:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            true
        }

        when:
        processor.registerVariantAwareUploadApkTask(applicationVariant)

        then:
        1 * applicationVariantBasedUploadApkTaskFactory.registerUploadArtifactTask(applicationVariant, LoginTaskFactory.TASK_NAME)

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "just check getDependencyAncestorOfUploadTaskNames"() {
        when:
        "do nothing"

        then:
        Processor.getDependencyAncestorOfUploadTaskNames().toList().sort() == [LoginTaskFactory.TASK_NAME].sort()
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    @Unroll
    def "canProcessVariantAware should depend on AndroidGradlePlugin.isApplied (#isAGPApplied)"() {
        setup:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        and:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            isAGPApplied
        }

        expect:
        processor.canProcessVariantAware() == isAGPApplied

        where:
        isAGPApplied << [true, false]
    }
}
