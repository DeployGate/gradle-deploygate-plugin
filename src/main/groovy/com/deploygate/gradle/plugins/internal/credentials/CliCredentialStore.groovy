package com.deploygate.gradle.plugins.internal.credentials

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.VisibleForTesting

class CliCredentialStore {
    private static final Gson GSON = new Gson()

    private final File baseDir

    String name
    String token

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
            def values = GSON.fromJson(contents, JsonObject)
            name = values.get("name")?.with { it.isJsonNull() ? null : it.asString }
            token = values.get("token")?.with { it.isJsonNull() ? null : it.asString }
            true
        }
    }

    boolean save() {
        JsonObject serialized = new JsonObject()

        if (name && token) {
            serialized.addProperty("name", name)
            serialized.addProperty("token", token)
        }

        saveLocalCredentialFile(GSON.toJson(serialized))
    }

    boolean delete() {
        localCredentialFile().delete()
    }

    @Nullable
    String loadLocalCredentialFile() {
        File file = localCredentialFile()

        if (file.exists()) {
            file.getText('UTF-8')
        } else {
            null
        }
    }

    boolean saveLocalCredentialFile(@NotNull String str) {
        if (!ensureDirectoryWritable()) {
            return false
        }

        File file = localCredentialFile()

        if (!file.exists() || file.canWrite()) {
            file.write(str, 'UTF-8')
            load()
        } else {
            false
        }
    }

    private boolean ensureDirectoryWritable() {
        return baseDir.exists() || baseDir.mkdirs()
    }

    File localCredentialFile() {
        return new File(baseDir, 'credentials')
    }
}
