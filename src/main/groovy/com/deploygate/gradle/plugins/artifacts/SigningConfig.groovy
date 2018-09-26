package com.deploygate.gradle.plugins.artifacts

import com.android.tools.build.bundletool.model.SigningConfiguration
import com.android.tools.build.bundletool.utils.flags.Flag

class SigningConfig {
    private final File storeFile
    private final String keyPassword
    private final String keyAlias
    private final String storePassword

    SigningConfig(File storeFile, String keyPassword, String keyAlias, String storePassword) {
        this.storeFile = storeFile
        this.keyPassword = keyPassword
        this.keyAlias = keyAlias
        this.storePassword = storePassword
    }

    SigningConfiguration toSigningConfiguration() {
        SigningConfiguration.extractFromKeystore(
                storeFile.toPath(),
                keyAlias,
                Optional.ofNullable(Flag.Password.createFromFlagValue("pass:${storePassword}")),
                Optional.ofNullable(Flag.Password.createFromFlagValue("pass:${keyPassword}"))
        )
    }
}