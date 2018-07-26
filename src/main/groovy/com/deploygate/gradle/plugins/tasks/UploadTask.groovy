package com.deploygate.gradle.plugins.tasks

import com.android.tools.build.bundletool.commands.BuildApksCommand
import com.android.tools.build.bundletool.model.Aapt2Command
import com.deploygate.gradle.plugins.artifacts.AppBundleInfo
import com.deploygate.gradle.plugins.entities.DeployTarget
import com.deploygate.gradle.plugins.utils.AndroidPlatformUtils
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
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

class UploadTask extends DefaultTask {
    String outputName
    boolean hasSigningConfig
    File defaultSourceFile

    AppBundleInfo appBundleInfo

    @TaskAction
    def upload() {
        if (!hasSigningConfig)
            throw new GradleException('Cannot upload a build without code signature to DeployGate')

        DeployTarget target = findTarget()

        buildApkFromAppBundleIfNeeded(target)

        if (!target.sourceFile?.exists())
            throw new GradleException("APK file ${target.sourceFile} was not found. If you are using Android Build Tools >= 3.0.0, you need to set `sourceFile` in your build.gradle. See https://docs.deploygate.com/docs/gradle-plugin")

        onBeforeUpload(target)
        def res = uploadProject(project, target)
        onAfterUpload(res)
    }

    private void buildApkFromAppBundleIfNeeded(DeployTarget target) {
        if (appBundleInfo) {
            def outputDir = appBundleInfo.getApksFile().parentFile

            outputDir.mkdirs()

            def universalApkGenerateCommand = BuildApksCommand.builder()
                    .setGenerateOnlyUniversalApk(true)
                    .setOverwriteOutput(true)
                    .setBundlePath(appBundleInfo.getBundleFile(project).toPath())
                    .setSigningConfiguration(appBundleInfo.signingConfig.toSigningConfiguration())
                    .setOutputFile(appBundleInfo.getApksFile().toPath())
                    .setAapt2Command(Aapt2Command.createFromExecutablePath(new File(AndroidPlatformUtils.getAapt2Location(project)).toPath()))
                    .build()

            def path = universalApkGenerateCommand.execute()
            def p = "unzip -d ${outputDir} ${path}".execute()
            if (p.waitFor() != 0) {
                throw new GradleException('unzipping apks file failed')
            }

            target.sourceFile.parentFile.mkdirs()
            new File(outputDir, 'universal.apk').renameTo(target.sourceFile)
        }
    }

    private DeployTarget findTarget() {
        DeployTarget target = project.deploygate.apks.findByName(outputName)
        if (!target)
            target = new DeployTarget(outputName)
        if (!target.sourceFile)
            target.sourceFile = defaultSourceFile
        fillFromEnv(target)
        target
    }

    private def fillFromEnv(DeployTarget target) {
        def envFile = System.getenv('DEPLOYGATE_SOURCE_FILE')
        target.with {
            sourceFile = sourceFile ?: (envFile ? project.file(envFile) : null)
            message = message ?: System.getenv('DEPLOYGATE_MESSAGE')
            distributionKey = distributionKey ?: System.getenv('DEPLOYGATE_DISTRIBUTION_KEY')
            releaseNote = releaseNote ?: System.getenv('DEPLOYGATE_RELEASE_NOTE')
            visibility = visibility ?: System.getenv('DEPLOYGATE_VISIBILITY')
        }
    }

    private void onBeforeUpload(DeployTarget target) {
        project.deploygate.notifyServer 'start_upload', ['length': Long.toString(target.sourceFile.length())]
    }

    private void onAfterUpload(res) {
        if (res.error)
            project.deploygate.notifyServer 'upload_finished', ['error': true, message: res.message]
        else {
            def sent = project.deploygate.notifyServer 'upload_finished', ['path': res.results.path]
            if (!sent &&
                    (hasOpenBrowserFlag() || res.results.revision == 1)) {
                BrowserUtils.openBrowser "${project.deploygate.endpoint}${res.results.path}"
            }
        }
    }

    static boolean hasOpenBrowserFlag() {
        System.getenv('DEPLOYGATE_OPEN_BROWSER')
    }

    def uploadProject(Project project, DeployTarget apk) {
        String userName = getUserName(project)
        String token = getToken(project)

        def result = postApk(userName, token, apk)
        errorHandling(apk, result)

        result.data
    }

    private static void errorHandling(apk, result) {
        if (result.status != 200 || result.data.error) {
            throw new GradleException("${apk.name} error message: ${result.data.message}")
        }
    }

    private static String getToken(Project project) {
        String token = project.deploygate.token
        if (!token?.trim()) {
            throw new GradleException('token is missing. Please enter the token.')
        }
        token
    }

    private static String getUserName(Project project) {
        String userName = project.deploygate.userName
        if (!userName?.trim()) {
            throw new GradleException('userName is missing. Please enter the userName.')
        }
        userName
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