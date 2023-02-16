package com.deploygate.gradle.plugins.credentials

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.jetbrains.annotations.VisibleForTesting

class CliCredentialStore {
    def store
    String name
    String token

    private final File baseDir

    CliCredentialStore() {
        this(new File(System.getProperty('user.home'), '.dg'))
    }

    @VisibleForTesting
    CliCredentialStore(File baseDir) {
        this.baseDir = baseDir
        load()
    }

    boolean load() {
        def contents = loadLocalCredentialFile()
        if (contents) {
            store = new JsonSlurper().parseText(contents)
            name = store.name
            token = store.token
            true
        }
    }

    boolean save() {
        if (!store)
            store = [:]
        store.name = name
        store.token = token
        saveLocalCredentialFile(JsonOutput.toJson(store))
    }

    boolean delete() {
        localCredentialFile().delete()
    }

    String loadLocalCredentialFile() {
        def file = localCredentialFile()
        if (file.exists())
            file.getText('UTF-8')
    }

    boolean saveLocalCredentialFile(String str) {
        if (!ensureDirectoryWritable())
            return false

        def file = localCredentialFile()
        if (!file.exists() || file.canWrite()) {
            file.write(str, 'UTF-8')
            true
        }
    }

    private boolean ensureDirectoryWritable() {
        baseDir.exists() || baseDir.mkdirs()
    }

    def localCredentialFile() {
        new File(baseDir, 'credentials')
    }
}
