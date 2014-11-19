package com.deploygate.gradle.plugins

import org.gradle.api.Named
import org.gradle.api.internal.project.ProjectInternal

class ApkTarget implements Named {
    String name
    ProjectInternal target 
    File sourceFile
    String message
    String distributionKey
    String releaseNote
    String visibility
    public ApkTarget(String name) {
      super()
      this.name = name
      this.target = target
    }
}
