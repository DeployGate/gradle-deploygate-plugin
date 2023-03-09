package com.deploygate.gradle.plugins

import com.deploygate.gradle.plugins.internal.agp.AndroidGradlePlugin
import com.deploygate.gradle.plugins.internal.agp.IApplicationVariant
import com.deploygate.gradle.plugins.tasks.Constants
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
    private UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadApkTaskFactory

    @Nonnull
    private UploadArtifactTaskFactory<IApplicationVariant> applicationVariantBasedUploadAabTaskFactory

    @Nonnull
    private Processor processor

    def setup() {
        project = ProjectBuilder.builder().build()
        applicationVariantBasedUploadApkTaskFactory = Mock()
        applicationVariantBasedUploadAabTaskFactory = Mock()
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    def "registerVariantAwareUploadApkTask should not do nothing unless AndroidGradlePlugin is applied"() {
        given:
        processor = new Processor(project, applicationVariantBasedUploadApkTaskFactory, applicationVariantBasedUploadAabTaskFactory)
        IApplicationVariant applicationVariant = Mock()

        and:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            false
        }

        when:
        processor.registerVariantAwareUploadApkTask(applicationVariant)

        then:
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * applicationVariantBasedUploadAabTaskFactory._
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    def "registerVariantAwareUploadApkTask should manipulate IApplicationVariant-based UploadApkTaskFactory if AndroidGradlePlugin is applied"() {
        given:
        processor = new Processor(project, applicationVariantBasedUploadApkTaskFactory, applicationVariantBasedUploadAabTaskFactory)
        IApplicationVariant applicationVariant = Mock()

        and:
        AndroidGradlePlugin.metaClass.static.isApplied = { Project _ ->
            true
        }

        when:
        processor.registerVariantAwareUploadApkTask(applicationVariant)

        then:
        1 * applicationVariantBasedUploadApkTaskFactory.registerUploadArtifactTask(applicationVariant, Constants.LOGIN_TASK_NAME)

        and:
        0 * applicationVariantBasedUploadApkTaskFactory._
        0 * applicationVariantBasedUploadAabTaskFactory._
    }

    def "just check getDependencyAncestorOfUploadTaskNames"() {
        when:
        "do nothing"

        then:
        Processor.getDependencyAncestorOfUploadTaskNames().toList().sort() == [Constants.LOGIN_TASK_NAME].sort()
    }

    @ConfineMetaClassChanges([AndroidGradlePlugin])
    @Unroll
    def "canProcessVariantAware should depend on AndroidGradlePlugin.isApplied (#isAGPApplied)"() {
        setup:
        processor = new Processor(project, applicationVariantBasedUploadApkTaskFactory, applicationVariantBasedUploadAabTaskFactory)

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
