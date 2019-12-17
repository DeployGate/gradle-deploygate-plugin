package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.dsl.NamedDeployment
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import com.google.common.annotations.VisibleForTesting
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.nio.charset.Charset

abstract class UploadArtifactTask extends DefaultTask {
    static class Configuration {
        boolean isSigningReady
        boolean isUniversalApk

        File artifactFile

        UploadParams uploadParams
    }

    static class UploadParams {
        String message
        String distributionKey
        String releaseNote
        String visibility

        HashMap<String, String> toMap() {
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

    static UploadParams createUploadParams(@Nonnull NamedDeployment deployment) {
        return new UploadParams(
                message: deployment.message,
                distributionKey: deployment.distribution?.key,
                releaseNote: deployment.distribution?.releaseNote,
                visibility: deployment.visibility
        )
    }

    @Nullable
    private String variantName

    Configuration configuration

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

    // Add TaskAction annotation in overrider classes
    void doUpload() {
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

        def response = postRequestToUpload(appOwnerName, apiToken, configuration.artifactFile, configuration.uploadParams)

        handleResponse(response, response.data)
    }

    private void onBeforeUpload() {
        project.deploygate.notifyServer 'start_upload', ['length': Long.toString(configuration.artifactFile.length())]
    }

    private void handleResponse(HttpResponseDecorator response, data) {
        if (!(200 <= response.status && response.status < 300) || data.error) {
            throw new GradleException("${variantName} failed due to ${data.message}")
        }

        if (data.error)
            project.deploygate.notifyServer 'upload_finished', ['error': true, message: data.message]
        else {
            def sent = project.deploygate.notifyServer 'upload_finished', ['path': data.results.path]

            if (!sent && (Config.shouldOpenAppDetailAfterUpload() || data.results.revision == 1)) {
                BrowserUtils.openBrowser "${project.deploygate.endpoint}${data.results.path}"
            }
        }
    }

    @Nonnull
    @VisibleForTesting
    String getApiToken() {
        def apiToken = project.deploygate.apiToken

        if (!apiToken?.trim()) {
            throw new GradleException('apiToken is missing. Please enter the token.')
        }

        apiToken.trim()
    }

    @Nonnull
    @VisibleForTesting
    String getAppOwnerName() {
        def appOwnerName = project.deploygate.appOwnerName

        if (!appOwnerName?.trim()) {
            throw new GradleException('appOwnerName is missing. Please enter the token.')
        }

        appOwnerName.trim()
    }

    private HttpResponseDecorator postRequestToUpload(String appOwnerName, String apiToken, File artifactFile, UploadParams uploadParams) {
        MultipartEntity entity = new MultipartEntity()
        Charset charset = Charset.forName('UTF-8')

        entity.addPart("file", new FileBody(artifactFile.getAbsoluteFile()))
        entity.addPart("token", new StringBody(apiToken, charset))

        def params = uploadParams.toMap()

        for (String key : params.keySet()) {
            entity.addPart(key, new StringBody(params.get(key), charset))
        }

        HTTPBuilderFactory.restClient(project.deploygate.endpoint).request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/api/users/${appOwnerName}/apps"
            req.entity = entity
        } as HttpResponseDecorator
    }
}
