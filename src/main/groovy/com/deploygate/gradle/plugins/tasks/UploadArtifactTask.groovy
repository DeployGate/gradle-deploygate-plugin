package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.internal.http.HttpClient
import com.deploygate.gradle.plugins.internal.http.UploadAppRequest
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import com.deploygate.gradle.plugins.tasks.inputs.DeploymentConfiguration
import com.deploygate.gradle.plugins.internal.utils.BrowserUtils
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

        /**
         * must be an absolute path
         */
        @Input
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

    @Internal
    final Property<HttpClient> httpClient

    @OutputFile
    final Provider<RegularFile> response

    UploadArtifactTask(@NotNull ObjectFactory objectFactory, @NotNull ProjectLayout projectLayout) {
        super()
        group = Constants.TASK_GROUP_NAME

        credentials = objectFactory.property(Credentials)
        deployment = objectFactory.newInstance(DeploymentConfiguration)
        httpClient = objectFactory.property(HttpClient)

        response = projectLayout.buildDirectory.file([
            "deploygate",
            name,
            "response.json"
        ].join(File.separator))
    }

    @NotNull
    abstract Provider<InputParams> getInputParamsProvider()

    protected final void doUpload(@NotNull InputParams inputParams) {
        if (!inputParams.artifactFile) {
            throw new IllegalStateException("An artifact file (${inputParams.artifactFilePath}) was not found.")
        }

        uploadArtifactToServer(credentials.get(), inputParams)
    }

    private void uploadArtifactToServer(@NotNull Credentials credentials, @NotNull InputParams inputParams) {
        httpClient.get().lifecycleNotificationClient.notifyOnBeforeArtifactUpload(inputParams.artifactFile.length())

        def request = new UploadAppRequest(inputParams.artifactFile).tap {
            it.message = inputParams.message
            it.distributionKey = inputParams.distributionKey
            it.releaseNote = inputParams.releaseNote
        }

        try {
            def uploadResponse = httpClient.get().getApiClient(credentials).uploadApp(request)

            uploadResponse.writeTo(response.get().asFile)

            def hasNotified = httpClient.get().lifecycleNotificationClient.notifyOnSuccessOfArtifactUpload(uploadResponse.typedResponse.application.path)

            if (!hasNotified && (Config.shouldOpenAppDetailAfterUpload() || uploadResponse.typedResponse.application.revision == 1)) {
                BrowserUtils.openBrowser "${project.deploygate.endpoint}${uploadResponse.typedResponse.application.path}"
            }
        } catch (Throwable e) {
            logger.debug(e.message, e)
            httpClient.get().lifecycleNotificationClient.notifyOnFailureOfArtifactUpload(e.message ?: 'something went wrong');
            throw new GradleException("${inputParams.variantName} failed due to ${e.message}", e)
        }
    }
}
