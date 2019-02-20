package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.factory.LoginTaskFactory
import com.deploygate.gradle.plugins.tasks.factory.LogoutTaskFactory
import com.deploygate.gradle.plugins.tasks.factory.UploadApkTaskFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
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
    private UploadApkTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private UploadApkTaskFactory<String> stringBasedUploadApkTaskFactory

    @Nonnull
    private Processor processor

    def setup() {
        project = ProjectBuilder.builder().build()
        loginTaskFactory = Mock()
        logoutTaskFactory = Mock()
        applicationVariantBasedUploadApkTaskFactory = Mock()
        stringBasedUploadApkTaskFactory = Mock()
    }

    def "addVariantOrCustomName"() {
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

    def "registerLoginTask"() {
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

    def "registerLogoutTask"() {
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

    def "registerDeclarationAwareUploadApkTask"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerDeclarationAwareUploadApkTask("dep1")

        then:
        1 * stringBasedUploadApkTaskFactory.registerUploadApkTask("dep1", *Processor.getDependencyAncestorOfUploadTaskNames())

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "registerAggregatedDeclarationAwareUploadApkTask"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        processor.registerAggregatedDeclarationAwareUploadApkTask(["dep1", "dep2", "dep3"])

        then:
        1 * stringBasedUploadApkTaskFactory.registerAggregatedUploadApkTask(["uploadDeployGateDep1", "uploadDeployGateDep2", "uploadDeployGateDep3"])

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
    def "registerVariantAwareUploadApkTask should work if AndroidGradlePlugin is applied"() {
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
        1 * applicationVariantBasedUploadApkTaskFactory.registerUploadApkTask(applicationVariant, *Processor.getDependencyAncestorOfUploadTaskNames())

        and:
        0 * loginTaskFactory._
        0 * logoutTaskFactory._
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * stringBasedUploadApkTaskFactory._
    }

    def "getDependencyAncestorOfUploadTaskNames"() {
        when:
        "do nothing"

        then:
        Processor.getDependencyAncestorOfUploadTaskNames().toList().sort() == [LoginTaskFactory.TASK_NAME].sort()
    }

    def "canProcessVariantAware"() {
        given:
        processor = new Processor(project, loginTaskFactory, logoutTaskFactory, applicationVariantBasedUploadApkTaskFactory, stringBasedUploadApkTaskFactory)

        when:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            isAGPApplied
        }

        then:
        processor.canProcessVariantAware() == isAGPApplied

        where:
        isAGPApplied << [true, false]
    }
}
