package com.deploygate.gradle.plugins.auth

import org.json.JSONObject

class DeployGateLocalCredential {
    JSONObject json
    String name
    String token

    DeployGateLocalCredential() {
        load()
    }

    def load() {
        def contents = loadLocalCredentialFile()
        if (contents) {
            json = new JSONObject(contents)
            name = json.getString('name')
            token = json.getString('token')
            return true
        }
        return false
    }

    def save() {
        if (!json)
            json = new JSONObject()
        json.put('name', name)
        json.put('token', token)
        saveLocalCredentialFile(json.toString())
    }

    def delete() {
        localCredentialFile().delete()
    }

    def loadLocalCredentialFile() {
        def file = localCredentialFile()
        if (file.exists())
            return file.getText('UTF-8')
        return null
    }

    def saveLocalCredentialFile(String str) {
        def dir = baseDir()
        if (!dir.exists() && !dir.mkdirs())
            return false
        def file = localCredentialFile()
        if (!file.exists() || file.canWrite()) {
            file.write(str, 'UTF-8')
            return true
        }
        return false
    }

    def localCredentialFile() {
        new File(baseDir(), 'credentials')
    }

    def baseDir() {
        new File(System.getProperty('user.home'), '.dg')
    }
}
