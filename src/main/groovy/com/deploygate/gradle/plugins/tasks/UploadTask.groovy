package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.entities.DeployTarget
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

class UploadTask extends DefaultTask {
    String outputName
    boolean hasSigningConfig
    File defaultSourceFile

    @TaskAction
    def upload() {
        if (!hasSigningConfig) {
            throw new GradleException('Cannot upload a build without code signature to DeployGate')
        }

        DeployTarget target = project.deploygate.apks.findByName(outputName)
        if (!target)
            target = new DeployTarget(outputName)
        if (target.sourceFile == null)
            target.sourceFile = defaultSourceFile

        project.deploygate.notifyServer 'start_upload', [ 'length': Long.toString(target.sourceFile?.length()) ]

        def res = uploadProject(project, target)

        if (res.error)
            project.deploygate.notifyServer 'upload_finished', [ 'error': true, message: res.message ]
        else
            project.deploygate.notifyServer 'upload_finished', [ 'path': res.results.path ]
    }

    def uploadProject(Project project, DeployTarget apk) {
        String endPoint = getEndPoint(project)
        String token = getToken(project)

        def json = httpPost(endPoint, token, apk)
        errorHandling(apk, json)

        json
    }

    private void errorHandling(apk, json) {
        if(json['error'] == true) {
            throw new GradleException("${apk.name} error message: ${json['message']}")
        }
    }

    private String getToken(Project project) {
        String token = project.deploygate.token
        if(token == null || token == '') {
            throw new GradleException('token is missing. Please enter the token.')
        }
        return token
    }

    private String getEndPoint(Project project) {
        String userName = project.deploygate.userName
        if(userName == null || userName == '') {
            throw new GradleException('userName is missing. Please enter the userName.')
        }
        String endPoint = project.deploygate.endpoint + "/api/users/${userName}/apps"
        return endPoint
    }

    private HttpClient getHttpClient() {
        HttpClient httpclient = new DefaultHttpClient()

        ProxySelectorRoutePlanner routePlanner =
                new ProxySelectorRoutePlanner(httpclient.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());
        httpclient.setRoutePlanner(routePlanner);

        return httpclient;
    }

    private def httpPost(String endPoint, String token, DeployTarget apk) {
        HttpClient httpclient = getHttpClient()
        HttpPost httppost = new HttpPost(endPoint)
        MultipartEntity request_entity = new MultipartEntity()
        Charset charset = Charset.forName('UTF-8')

        File file = apk.sourceFile
        request_entity.addPart("file", new FileBody(file.getAbsoluteFile()))
        request_entity.addPart("token", new StringBody(token, charset))

        HashMap<String, String> params = apk.toParams()
        for (String key : params.keySet()) {
            request_entity.addPart(key, new StringBody(params.get(key), charset))
        }

        httppost.setEntity(request_entity)
        HttpResponse response = httpclient.execute(httppost)
        HttpEntity entity = response.getEntity()

        if (entity != null) {
            return new JsonSlurper().parse(entity.getContent(), 'UTF-8')
        }
    }
}
