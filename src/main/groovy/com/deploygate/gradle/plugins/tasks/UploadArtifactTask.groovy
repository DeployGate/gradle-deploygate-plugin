package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.UploadAppRequest
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import com.deploygate.gradle.plugins.utils.BrowserUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

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
        final Closure<File> artifactFileProvider = {
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

        @Internal
        @Nullable
        File getArtifactFile() {
            return artifactFileProvider.call()
        }
    }

    @Nested
    final Property<Credentials> credentials

    @Nested
    final DeploymentConfiguration deployment

    @OutputFile
    final Provider<RegularFile> response

    UploadArtifactTask(@NotNull ObjectFactory objectFactory, @NotNull ProjectLayout projectLayout) {
        super()
        group = Constants.TASK_GROUP_NAME

        credentials = objectFactory.property(Credentials)
        deployment = objectFactory.newInstance(DeploymentConfiguration)

        response = projectLayout.buildDirectory.file(["deploygate", name, "response.json"].join(File.separator))
    }

    @NotNull
    abstract Provider<InputParams> getInputParamsProvider()

    protected final void doUpload(@NotNull InputParams inputParams) {
        if (!inputParams.artifactFile) {
            throw new IllegalStateException("An artifact file (${inputParams.artifactFilePath}) was not found.")
        }

        def appOwnerName = credentials.get().appOwnerName.get()
        def apiToken = credentials.get().apiToken.get()

        uploadArtifactToServer(appOwnerName, apiToken, inputParams)
    }

    private void uploadArtifactToServer(@NotNull String appOwnerName, @NotNull String apiToken, @NotNull InputParams inputParams) {
        onBeforeUpload(inputParams.artifactFile)

        def request = new UploadAppRequest(inputParams.artifactFile).tap {
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
        def f = response.get().asFile
        if (!f.parentFile.exists()) {
            f.parentFile.mkdirs()
        }

        if (f.exists()) {
            f.delete()
        }
        f.write(rawResponse)
    }
}
