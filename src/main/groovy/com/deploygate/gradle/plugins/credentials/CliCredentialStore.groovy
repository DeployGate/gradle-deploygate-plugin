package com.deploygate.gradle.plugins.credentials

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class CliCredentialStore {
    def store
    String name
    String token

    CliCredentialStore() {
        load()
    }

    def load() {
        def contents = loadLocalCredentialFile()
        if (contents) {
            store = new JsonSlurper().parseText(contents)
            name = store.name
            token = store.token
            return true
        }
        return false
    }

    def save() {
        if (!store)
            store = [:]
        store.name = name
        store.token = token
        saveLocalCredentialFile(JsonOutput.toJson(store))
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
        if (!ensureDirectoryWritable())
            return false
        def file = localCredentialFile()
        if (!file.exists() || file.canWrite()) {
            file.write(str, 'UTF-8')
            return true
        }
        return false
    }

    private boolean ensureDirectoryWritable() {
        File dir = baseDir()
        dir.exists() || dir.mkdirs()
    }

    def localCredentialFile() {
        new File(baseDir(), 'credentials')
    }

    def baseDir() {
        new File(System.getProperty('user.home'), '.dg')
    }
}
