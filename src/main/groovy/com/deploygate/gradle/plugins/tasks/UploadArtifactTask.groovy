package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.UploadAppRequest
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.annotation.Nonnull

abstract class UploadArtifactTask extends DefaultTask {
    static class InputParams {
        @Input
        String variantName

        @Input
        boolean isSigningReady

        @Input
        boolean isUniversalApk

        @InputFile
        @Optional
        final Closure<File> artifactFile = {
            // workaround of OptionalInputFile: https://github.com/gradle/gradle/issues/2016
            def f = new File(artifactFilePath)
            f.exists() ? f : null
        }

        @Input
        @PathSensitive(PathSensitivity.ABSOLUTE)
        String artifactFilePath

        @Input
        @Optional
        String message

        @Nested
        @Optional
        String distributionKey

        @Input
        @Optional
        String releaseNote
    }

    @Nested
    Property<Credentials> credentials

    @Nested
    Property<NamedDeployment> deployment

    @OutputFile
    File response = new File(new File(new File(project.buildDir, "deploygate"), name), "response.json")

    UploadArtifactTask(@NotNull ObjectFactory objectFactory) {
        super()
        credentials = objectFactory.property(Credentials)
        deployment = objectFactory.property(NamedDeployment)

        outputs.upToDateWhen { false } // disable caching by default
    }

    abstract InputParams getInputParams();

    // Add TaskAction annotation in overrider classes
    protected void doUpload() {
        def artifactFile = inputParams.artifactFile.call()

        if (!artifactFile) {
            throw new IllegalStateException("An artifact file (${inputParams.artifactFilePath}) was not found.")
        }

        def appOwnerName = getAppOwnerName()
        def apiToken = getApiToken()

        uploadArtifactToServer(appOwnerName, apiToken, artifactFile)
    }

    private void uploadArtifactToServer(@NotNull String appOwnerName, @NotNull String apiToken, @NotNull File artifactFile) {
        onBeforeUpload(artifactFile)

        def request = new UploadAppRequest(artifactFile).tap {
            it.message = inputParams.message
            it.distributionKey = inputParams.distributionKey
            it.releaseNote = inputParams.releaseNote
        }

        try {
            def response = ApiClient.getInstance().uploadApp(appOwnerName, apiToken, request)
            writeUploadResponse(response.rawResponse)
            def sent = project.deploygate.notifyServer 'upload_finished', ['path': response.typedResponse.application.path]

            if (!sent && (Config.shouldOpenAppDetailAfterUpload() || response.typedResponse.application.revision == 1)) {
                BrowserUtils.openBrowser "${project.deploygate.endpoint}${response.typedResponse.application.path}"
            }
        } catch (Throwable e) {
            logger.debug(e.message, e)
            project.deploygate.notifyServer 'upload_finished', ['error': true, message: e.message]
            throw new GradleException("${inputParams.variantName} failed due to ${e.message}", e)
        }
    }

    private void onBeforeUpload(@NotNull File artifactFile) {
        project.deploygate.notifyServer 'start_upload', ['length': Long.toString(artifactFile.length())]
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
        def apiToken = credentials.getOrNull()?.apiToken?.trim()

        if (!apiToken) {
            throw new GradleException('apiToken is missing. Please enter the token.')
        }

        apiToken
    }

    @Nonnull
    @VisibleForTesting
    @Internal
    String getAppOwnerName() {
        def appOwnerName = credentials.getOrNull()?.appOwnerName?.trim()

        if (!appOwnerName) {
            throw new GradleException('appOwnerName is missing. Please enter the token.')
        }

        appOwnerName
    }

    @Nullable
    @Internal
    File getArtifactFile() {
        return inputParams.artifactFile.call()
    }
}
