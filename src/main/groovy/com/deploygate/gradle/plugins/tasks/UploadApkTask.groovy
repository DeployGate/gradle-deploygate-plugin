package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.artifacts.ApkInfo
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.factory.DeployGateTaskFactory
import com.deploygate.gradle.plugins.utils.HTTPBuilderFactory
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.gradle.api.DefaultTask

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.nio.charset.Charset

class UploadApkTask extends DefaultTask {
    static class Configuration {
        DeployTarget deployTarget
        ApkInfo apkInfo
    }

    @Nullable
    private String variantName

    private Configuration configuration

    void setVariantName(@Nonnull String variantName) {
        if (this.variantName) {
            throw new IllegalStateException("variant name cannot be assigned twice")
        }

        this.variantName = variantName
        setDescription("Deploy assembled $variantName to DeployGate")
    }

    void setConfiguration(@Nonnull Configuration configuration) {
        if (!this.variantName) {
            throw new IllegalStateException("variant name must be set first")
        }

        if (this.configuration) {
            throw new IllegalStateException("configuration cannot be assigned twice")
        }

        this.configuration = configuration

        if (!configuration.apkInfo.signingReady) {
            // require signing config to build a signed APKs
            setDescription(description + " (requires valid signingConfig setting)")
        }

        if (configuration.apkInfo.universalApk) {
            group = DeployGateTaskFactory.GROUP_NAME
        }
    }

    private HttpResponseDecorator postApk(String userName, String token, DeployTarget apk) {
        MultipartEntity entity = new MultipartEntity()
        Charset charset = Charset.forName('UTF-8')

        File file = apk.sourceFile
        entity.addPart("file", new FileBody(file.getAbsoluteFile()))
        entity.addPart("token", new StringBody(token, charset))

        HashMap<String, String> params = apk.toParams()
        for (String key : params.keySet()) {
            entity.addPart(key, new StringBody(params.get(key), charset))
        }

        HTTPBuilderFactory.restClient(project.deploygate.endpoint).request(Method.POST, ContentType.JSON) { req ->
            uri.path = "/api/users/${userName}/apps"
            req.entity = entity
        } as HttpResponseDecorator
    }
}
