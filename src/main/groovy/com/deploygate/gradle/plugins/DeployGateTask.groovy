package com.deploygate.gradle.plugins

import org.gradle.api.GradleException
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.apache.http.protocol.HTTP
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.content.FileBody
import java.util.HashMap
import org.json.JSONObject
import java.nio.charset.Charset

class DeployGateTask extends DefaultTask {
    private final String API_END_POINT = "https://deploygate.com/api"

    private void upload(Project project, List<Apk> apks) {
        String endPoint = getEndPoint(project)
        String token = getToken(project)

        HashMap<String, JSONObject> result = httpPost(endPoint, token, apks)
        for(Apk apk in apks) {
            JSONObject json = result.get(apk.name)
            errorHandling(apk, json) 
            println "${apk.name} result: ${json.toString()}"
        }
    }

    private void errorHandling(Apk apk, JSONObject json) {
        if(json['error'] == true) {
            throw new GradleException("${apk.name} error massage: ${json['message']}")
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
        String endPoint = API_END_POINT + "/users/${userName}/apps"
        return endPoint
    }

    private HashMap<String, JSONObject> httpPost(String endPoint, String token, List<Apk> apks) {
        HashMap<String, JSONObject> result = new HashMap<String, JSONObject>() 
        for(Apk apk in apks) {
            HttpClient httpclient = new DefaultHttpClient()
            HttpPost httppost = new HttpPost(endPoint)
            MultipartEntity request_entity = new MultipartEntity()
            Charset charset = Charset.forName(HTTP.UTF_8)

            File file = apk.file
            request_entity.addPart("file", new FileBody(file.getAbsoluteFile()))
            request_entity.addPart("token", new StringBody(token, charset))

            HashMap<String, String> params = apk.getParams() 
            for (String key : params.keySet()) {
                request_entity.addPart(key, new StringBody(params.get(key), charset))
            }

            httppost.setEntity(request_entity)
            HttpResponse response = httpclient.execute(httppost)
            HttpEntity entity = response.getEntity()

            if (entity != null) {
                InputStream is = entity.getContent()
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
                JSONObject json = new JSONObject(reader.readLine())
                result.put(apk.name, json)
                try {
                } finally {
                    is.close()
                }
            }
        }
        return result 
    }
}
