package com.deploygate.gradle.plugins

import org.gradle.api.GradleException;
import org.gradle.api.Plugin
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

class DeployGate implements Plugin<Project> {
    void apply(Project target) {
        target.extensions.create('deploygate', DeployGateExtension)
        def apkUpload = target.task('uploadDeployGate') << {
            HashMap<String, String> params = setBaseParams(target)
            JSONObject json = httpPost(params)
            errorHandling(json) 

            println json.toString() 
        }
        apkUpload.group = 'DeployGate' 
        apkUpload.description = 'Upload the apk file to deploygate'

        def distributionUpdate = target.task('updateDeployGateDistribution') << {
            String distributionKey = target.deploygate.distributionKey
            String releaseNote = target.deploygate.releaseNote
            if(distributionKey == null || distributionKey == '') {
                throw new GradleException('distributionKey is missing. Please enter the distributionKey.')
            }
            if(releaseNote == null) {
                releaseNote = ''
            }
            
            HashMap<String, String> params = setBaseParams(target)
            params.put("distribution_key", distributionKey)
            params.put("release_note", target.deploygate.releaseNote)

            JSONObject json = httpPost(params)
            errorHandling(json) 

            println json.toString()
        }
        distributionUpdate.group = 'DeployGate' 
        distributionUpdate.description = 'Apk upload and distribution update'
    }

    private void errorHandling(JSONObject json) {
        if(json['error'] == true) {
            throw new GradleException('error massage: ' + json['message'])
        }
    }

    private HashMap<String, String> setBaseParams(Project target) {
        String userName = target.deploygate.userName
        String apkPath = target.deploygate.apkPath
        String token = target.deploygate.token
        String message = target.deploygate.message
        if(userName == null || userName == '') {
            throw new GradleException('userName is missing. Please enter the userName.')
        }
        if(apkPath == null || apkPath == '') {
            throw new GradleException('apkPath is missing. Please enter the apkPath.')
        }
        if(token == null || token == '') {
            throw new GradleException('token is missing. Please enter the token.')
        }
        if(message == null) {
            message = ''
        }

        String endPoint = "https://deploygate.com/api/users/${userName}/apps"
        HashMap<String, String> params = new HashMap<String, String>()
        params.put("file", apkPath)
        params.put("token", token)
        params.put("message", target.deploygate.message)
        params.put("endPoint", endPoint)
        
        return params
    }

    private JSONObject httpPost(HashMap<String, String> params) {
        HttpClient httpclient = new DefaultHttpClient()
        String endPoint = params.get("endPoint")
        HttpPost httppost = new HttpPost(endPoint)
        MultipartEntity request_entity = new MultipartEntity()
        Charset charset = Charset.forName(HTTP.UTF_8)
        
        File file = new File(params.get("file"))
        request_entity.addPart("file", new FileBody(file.getAbsoluteFile()))
        params.remove("file")
        params.remove("endPoint")

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
            return json
            try {
            } finally {
              is.close()
            }
        }
        return null
    }
}

class DeployGateExtension {
    String apkPath
    String userName
    String token
    String message 
    String distributionKey
    String releaseNote
}
