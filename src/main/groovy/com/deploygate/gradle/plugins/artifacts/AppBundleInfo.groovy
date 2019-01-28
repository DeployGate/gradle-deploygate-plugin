package com.deploygate.gradle.plugins.artifacts

import com.deploygate.gradle.plugins.entities.DeployTarget
import org.gradle.api.Project

class AppBundleInfo {
    private final ApkInfo apkInfo

    private File apksFile
    private File bundleFile

    AppBundleInfo(ApkInfo apkInfo) {
        this.apkInfo = apkInfo
    }

    File getApksFile() {
        if (apksFile) {
            return apksFile
        }

        return apksFile = new File(apkInfo.apkFile.parentFile, apkInfo.apkFile.name.replace(".apk", ".apks"))
    }

    File getBundleFile(Project project) {
        if (bundleFile) {
            return bundleFile
        }

        def deployTarget = project.deploygate.apks.findByName(apkInfo.variantName) as DeployTarget

        return bundleFile = project.file(deployTarget?.bundle?.source ?: "build/outputs/bundle/${apkInfo.variantName}/${project.name}.aab")
    }
}