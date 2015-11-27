package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.entities.ApkTarget
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.http.protocol.HTTP
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.json.JSONObject

import java.nio.charset.Charset

abstract class DeployGateTask extends DefaultTask {

    def upload(Project project, ApkTarget apk) {
        String endPoint = getEndPoint(project)
        String token = getToken(project)

        JSONObject json = httpPost(endPoint, token, apk)
        errorHandling(apk, json)

        json
    }

    private void errorHandling(apk, JSONObject json) {
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
        String endPoint = Config.API_END_POINT + "/users/${userName}/apps"
        return endPoint
    }

    private HttpClient getHttpClient() {
        HttpClient httpclient = new DefaultHttpClient()

        ProxySelectorRoutePlanner routePlanner =
            new ProxySelectorRoutePlanner(httpclient.getConnectionManager().getSchemeRegistry(), ProxySelector.getDefault());
        httpclient.setRoutePlanner(routePlanner);

        return httpclient;
    }

    private JSONObject httpPost(String endPoint, String token, ApkTarget apk) {
        HttpClient httpclient = getHttpClient()
        HttpPost httppost = new HttpPost(endPoint)
        MultipartEntity request_entity = new MultipartEntity()
        Charset charset = Charset.forName(HTTP.UTF_8)

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
            InputStream is = entity.getContent()
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
                JSONObject json = new JSONObject(reader.readLine())
                return json
            } finally {
                is.close()
            }
        }
    }
}
