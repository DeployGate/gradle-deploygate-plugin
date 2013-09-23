package com.deploygate.gradle.plugins

import org.gradle.api.DomainObjectSet
import org.gradle.api.Named
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.binaries.model.NativeDependencyCapableSourceSet;
import org.gradle.plugins.binaries.model.NativeDependencySet

class ApkTarget implements Named {
    String name
    ProjectInternal target 
    File sourceFile
    String message
    String distributionKey
    String releaseNote
    public ApkTarget(String name) {
      super()
      this.name = name
      this.target = target
    }
}
