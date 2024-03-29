package com.deploygate.gradle.plugins.internal.credentials

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class CliCredentialStore {
    public static final String CREDENTIALS_FILE_NAME = "credentials"

    private static final Gson GSON = new Gson()

    private final File baseDir

    String name
    String token

    CliCredentialStore(@NotNull File baseDir) {
        if (baseDir.exists()) {
            if (!baseDir.isDirectory()) {
                throw new IllegalArgumentException("${baseDir.absolutePath} is not a directory")
            }
        } else if (!baseDir.mkdirs()) {
            throw new IllegalStateException("${baseDir.absolutePath} cannot be created")
        }

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
        } else {
            name = null
            token = null
        }
    }

    boolean save() {
        if (name && token) {
            JsonObject serialized = new JsonObject()

            serialized.addProperty("name", name)
            serialized.addProperty("token", token)

            return saveLocalCredentialFile(GSON.toJson(serialized))
        } else {
            return false
        }
    }

    /**
     * Delete the only credential file
     *
     * @return true if the file does not exist after processing, otherwise false.
     */
    boolean delete() {
        File file = localCredentialFile()
        return !file.exists() || file.delete()
    }

    /**
     * Whether or not the file is an expected format.
     *
     * @return true if the content is valid, otherwise false.
     */
    boolean isValid() {
        // xor is not available for null...
        return name && token || !name && !token
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
        File file = localCredentialFile()

        if (!file.exists() || file.canWrite()) {
            file.write(str, 'UTF-8')
            load()
        } else {
            false
        }
    }

    File localCredentialFile() {
        return new File(baseDir, CREDENTIALS_FILE_NAME)
    }
}
