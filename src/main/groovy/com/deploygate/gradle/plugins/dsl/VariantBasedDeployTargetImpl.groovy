package com.deploygate.gradle.plugins.dsl


import javax.annotation.Nullable

class VariantBasedDeployTargetImpl implements VariantBasedDeployTarget {

    private String name

    @Nullable
    private File sourceFile

    @Nullable
    private String uploadMessage

    @Nullable
    private String distributionKey

    @Nullable
    private String releaseNote

    @Nullable
    private String visibility

    private boolean skipAssemble

    VariantBasedDeployTargetImpl() {
    }

    VariantBasedDeployTargetImpl(String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }

    @Deprecated
    void setMessage(@Nullable String message) {
        setUploadMessage(message)
    }

    @Deprecated
    void setNoAssemble(boolean noAssemble) {
        setSkipAssemble(noAssemble)
    }

    @Override
    void setSourceFile(@Nullable File sourceFile) {
        this.sourceFile = sourceFile
    }

    @Override
    File getSourceFile() {
        return sourceFile
    }

    @Override
    void setUploadMessage(@Nullable String uploadMessage) {
        this.uploadMessage = uploadMessage
    }

    @Override
    String getUploadMessage() {
        return uploadMessage
    }

    @Override
    void setDistributionKey(@Nullable String distributionKey) {
        this.distributionKey = distributionKey
    }

    @Override
    String getDistributionKey() {
        return distributionKey
    }

    @Override
    void setReleaseNote(@Nullable String releaseNote) {
        this.releaseNote = releaseNote
    }

    @Override
    String getReleaseNote() {
        return releaseNote
    }

    @Override
    void setVisibility(@Nullable String visibility) {
        this.visibility = visibility
    }

    @Override
    String getVisibility() {
        return visibility
    }

    @Override
    void setSkipAssemble(boolean skipAssemble) {
        this.skipAssemble = skipAssemble
    }

    @Override
    boolean isSkipAssemble() {
        return skipAssemble
    }
}
