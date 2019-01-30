package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.artifacts.ApkInfo
import com.deploygate.gradle.plugins.dsl.DeployTarget
import com.deploygate.gradle.plugins.tasks.factory.DeployGateTaskFactory
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.nio.charset.Charset

class UploadApkTask extends DefaultTask {
    static class Configuration {
        boolean isSigningReady
        boolean isUniversalApk

        File apkFile
        String uploadMessage
        String distributionKey
        String releaseNote
        String visibility

        HashMap<String, String> toUploadParams() {
            HashMap<String, String> params = new HashMap<String, String>()
            if (uploadMessage != null) {
                params.put("message", uploadMessage)
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

    static Configuration createConfiguration(@Nonnull DeployTarget deployTarget, @Nonnull ApkInfo apkInfo) {
        return new Configuration(
                isSigningReady: apkInfo.isSigningReady(),
                isUniversalApk: apkInfo.isUniversalApk(),
                apkFile: deployTarget.sourceFile ?: apkInfo.apkFile,
                uploadMessage: deployTarget.message,
                distributionKey: deployTarget.distributionKey,
                releaseNote: deployTarget.releaseNote,
                visibility: deployTarget.releaseNote
        )
    }

    @Nullable
    private String variantName

    private Configuration configuration

    void setVariantName(@Nonnull String variantName) {
        if (this.variantName && this.variantName != variantName) {
            throw new IllegalStateException("different variant name cannot be assigned")
        }

        this.variantName = variantName
    }

    void setConfiguration(@Nonnull Configuration configuration) {
        if (!this.variantName) {
            throw new IllegalStateException("variant name must be set first")
        }

        if (this.configuration) {
            logger.debug("$variantName upload apk task configuration has been overwritten")
        }

        this.configuration = configuration
    }

    void applyTaskProfile() {
        setDescription("Deploy assembled $variantName to DeployGate")

        if (!configuration.isSigningReady) {
            // require signing config to build a signed APKs
            setDescription(description + " (requires valid signingConfig setting)")
        }

        if (configuration.isUniversalApk) {
            group = DeployGateTaskFactory.GROUP_NAME
        }
    }

    @TaskAction
    def uploadApkToServer() {
        if (!configuration.isSigningReady) {
            throw new GradleException('Cannot upload a build without code signature to DeployGate')
        }

        if (!configuration.apkFile) {
            throw new IllegalStateException("An apk file to be upload not specified.")
        }

        if (!configuration.apkFile.exists()) {
            throw new IllegalStateException("APK file ${configuration.apkFile} was not found. If you are using Android Build Tools >= 3.0.0, you need to set `sourceFile` in your build.gradle. See https://docs.deploygate.com/docs/gradle-plugin")
        }

        onBeforeUpload()

        // FIXME token and user name verification should be done before onBeforeUpload
        def response = postRequestToUpload(getUserName(), getToken(), configuration.apkFile, configuration.toUploadParams())

        handleResponse(response, response.data)
    }

    def onBeforeUpload() {
        project.deploygate.notifyServer 'start_upload', ['length': Long.toString(configuration.apkFile.length())]
    }

    def handleResponse(response, data) {
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

    private String getToken() {
        def taken = project.deploygate.token

        if (!taken.trim()) {
            throw new GradleException('token is missing. Please enter the token.')
        }

        taken.trim()
    }

    private String getUserName() {
        def userName = project.deploygate.userName

        if (!userName.trim()) {
            throw new GradleException('userName is missing. Please enter the token.')
        }

        userName.trim()
    }

    private HttpResponseDecorator postRequestToUpload(String userName, String token, File apkFile, Map<String, String> params) {
        MultipartEntity entity = new MultipartEntity()
        Charset charset = Charset.forName('UTF-8')

        entity.addPart("file", new FileBody(apkFile.getAbsoluteFile()))
        entity.addPart("token", new StringBody(token, charset))

        for (String key : params.keySet()) {
            entity.addPart(key, new StringBody(params.get(key), charset))
        }

        HTTPBuilderFactory.restClient(project.deploygate.endpoint).request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/api/users/${userName}/apps"
            req.entity = entity
        } as HttpResponseDecorator
    }
}
