package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.NetworkFailure
import com.deploygate.gradle.plugins.internal.http.UploadAppRequest
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.google.common.annotations.VisibleForTesting
import org.apache.hc.core5.http.HttpException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

import javax.annotation.Nonnull
import javax.annotation.Nullable

abstract class UploadArtifactTask extends DefaultTask {
    static class Configuration {
        boolean isSigningReady
        boolean isUniversalApk

        File artifactFile

        String message
        String distributionKey
        String releaseNote
    }

    @Internal
    @Nullable
    private String variantName

    @Internal
    Configuration configuration

    private def packageApplicationTaskProvider

    @OutputFile
    File response = new File(new File(new File(project.buildDir, "deploygate"), name), "response.json")

    void setVariantName(@Nonnull String variantName) {
        if (this.variantName && this.variantName != variantName) {
            throw new IllegalStateException("different variant name cannot be assigned")
        }

        this.variantName = variantName
    }

    @Nullable
    String getVariantName() {
        return variantName
    }

    void setConfiguration(@Nonnull Configuration configuration) {
        if (!this.variantName) {
            throw new IllegalStateException("variant name must be set first")
        }

        if (this.configuration) {
            logger.debug("$variantName upload artifact (${this.getClass().simpleName}) task configuration has been overwritten")
        }

        this.configuration = configuration
    }

    void setPackageApplicationTaskProvider(packageApplicationTaskProvider) {
        this.packageApplicationTaskProvider = packageApplicationTaskProvider
    }

    // Add TaskAction annotation in overrider classes
    void doUpload() {
        if (packageApplicationTaskProvider) {
            // evaluate surely
            // ref: https://github.com/DeployGate/gradle-deploygate-plugin/issues/86
            packageApplicationTaskProvider.get()
            logger.debug("$variantName's package application task has been evaluated")
        } else {
            logger.debug("$variantName's package application task is not found")
        }

        runArtifactSpecificVerification()
        uploadArtifactToServer()
    }

    abstract void applyTaskProfile()

    abstract void runArtifactSpecificVerification()

    private void uploadArtifactToServer() {
        if (!configuration.artifactFile) {
            throw new IllegalStateException("An artifact file to be upload not specified.")
        }

        if (!configuration.artifactFile.exists()) {
            throw new IllegalStateException("An artifact file (${configuration.artifactFile}) was not found. If you are using Android Build Tools >= 3.0.0, you need to set `sourceFile` in your build.gradle. See https://docs.deploygate.com/docs/gradle-plugin")
        }

        def appOwnerName = getAppOwnerName()
        def apiToken = getApiToken()

        onBeforeUpload()

        def request = new UploadAppRequest(configuration.artifactFile).tap {
            it.message = configuration.message
            it.distributionKey = configuration.distributionKey
            it.releaseNote = configuration.releaseNote
        }

        try {
            def response = ApiClient.getInstance().uploadApp(appOwnerName, apiToken, request)
            writeUploadResponse(response.rawResponse)
            def sent = project.deploygate.notifyServer 'upload_finished', ['path': response.typedResponse.application.path]

            if (!sent && (Config.shouldOpenAppDetailAfterUpload() || response.typedResponse.application.revision == 1)) {
                BrowserUtils.openBrowser "${project.deploygate.endpoint}${response.typedResponse.application.path}"
            }
        } catch (HttpException e) {
            logger.debug(e.message, e)
            project.deploygate.notifyServer 'upload_finished', ['error': true, message: e.message]
            throw new GradleException("${variantName} failed due to ${e.message}")
        } catch (NetworkFailure e) {
            logger.debug(e.message, e)
            throw new GradleException("${variantName} failed due to ${e.message}")
        }
    }

    private void onBeforeUpload() {
        project.deploygate.notifyServer 'start_upload', ['length': Long.toString(configuration.artifactFile.length())]
    }

    private void writeUploadResponse(String rawResponse) {
        if (!response.parentFile.exists()) {
            response.parentFile.mkdirs()
        }

        if (response.exists()) {
            response.delete()
        }
        response.write(rawResponse)
    }

    @Nonnull
    @VisibleForTesting
    @Internal
    String getApiToken() {
        def apiToken = project.deploygate.apiToken

        if (!apiToken?.trim()) {
            throw new GradleException('apiToken is missing. Please enter the token.')
        }

        apiToken.trim()
    }

    @Nonnull
    @VisibleForTesting
    @Internal
    String getAppOwnerName() {
        def appOwnerName = project.deploygate.appOwnerName

        if (!appOwnerName?.trim()) {
            throw new GradleException('appOwnerName is missing. Please enter the token.')
        }

        appOwnerName.trim()
    }
}
