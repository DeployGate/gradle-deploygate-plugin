package com.deploygate.gradle.plugins.entities

import org.gradle.api.Named
import org.gradle.api.Project

import javax.annotation.Nonnull

class DeployTarget implements Named {
    static DeployTarget fromEnvVars(@Nonnull Project project) {
        def deployTarget = new DeployTarget()

        deployTarget.with {
            sourceFile = System.getenv('DEPLOYGATE_SOURCE_FILE')?.with { project.file(this) }
            message = System.getenv('DEPLOYGATE_MESSAGE')
            distributionKey = System.getenv('DEPLOYGATE_DISTRIBUTION_KEY')
            releaseNote = System.getenv('DEPLOYGATE_RELEASE_NOTE')
            visibility = System.getenv('DEPLOYGATE_VISIBILITY')
        }

        deployTarget
    }

    String name

    File sourceFile
    String message
    String distributionKey
    String releaseNote
    String visibility
    boolean noAssemble

    AppBundleTarget bundle

    DeployTarget() {}

    DeployTarget(String name) {
        this.name = name
    }

    def bundle(Closure closure) {
        bundle.configure(closure)
    }

    DeployTarget merge(DeployTarget other) {
        with {
            sourceFile = sourceFile ?: other.sourceFile
            message = message ?: other.message
            distributionKey = distributionKey ?: other.distributionKey
            releaseNote = releaseNote ?: other.releaseNote
            visibility = visibility ?: other.visibility
        }
        return this
    }

    HashMap<String, String> toParams() {
        HashMap<String, String> params = new HashMap<String, String>()
        if (message != null) {
            params.put("message", message)
        }
        if (distributionKey != null) {
            params.put("distribution_key", distributionKey)
        }
        if (releaseNote != null) {
            params.put("release_note", releaseNote)
        }
        if (visibility != null) {
            params.put("visibility", visibility)
        }
        return params
    }
}
