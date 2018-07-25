package com.deploygate.gradle.plugins.artifacts

class ApkInfoCompat {
    private ApkInfoCompat() {
    }

    static ApkInfo blank(String name) {
        return new ApkInfo() {
            @Override
            String getVariantName() {
                return name
            }

            @Override
            File getApkFile() {
                return null
            }

            @Override
            boolean isSigningReady() {
                return true
            }

            @Override
            boolean isUniversalApk() {
                return true
            }
        }
    }

    static ApkInfo from(/*ApplicationVariant*/ applicationVariant, /*BaseVariantOutput*/ variantOutput) {
        if (isBefore300Preview()) {
            return new ApkInfoCompatBefore300Preview(applicationVariant, variantOutput)
        } else if (is300Preview()) {
            return new ApkInfoCompat300Preview(applicationVariant, variantOutput)
        } else {
            return new ApkInfoCompatLatest(applicationVariant, variantOutput)
        }
    }

    private static String getVersionString() {
        try {
            return Class.forName("com.android.builder.model.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        // before 3.1
        try {
            return Class.forName("com.android.builder.Version").getField("ANDROID_GRADLE_PLUGIN_VERSION").get(null)
        } catch (Throwable ignored) {
        }

        return Integer.MAX_VALUE + ".0.0"
    }

    private static boolean isBefore300Preview() {
        int majorVersion = Integer.parseInt(getVersionString().split("\\.")[0])

        return majorVersion < 3
    }

    private static boolean is300Preview() {
        return getVersionString().startsWith("3.0.0-")
    }

    private static abstract class BaseApkInfo implements ApkInfo {
        // Remove comment-out while debugging
//        private final ApplicationVariant applicationVariant
//        private final BaseVariantOutput variantOutput
        protected def applicationVariant
        protected def variantOutput

        BaseApkInfo(applicationVariant, variantOutput) {
            this.applicationVariant = applicationVariant
            this.variantOutput = variantOutput
        }

        @Override
        String getVariantName() {
            return variantOutput.name
        }

        @Override
        File getApkFile() {
            return variantOutput.outputFile
        }

        @Override
        boolean isSigningReady() {
            return true
        }

        @Override
        boolean isUniversalApk() {
            return variantOutput.outputs.get(0).filters.empty
        }
    }

    private static class ApkInfoCompatBefore300Preview extends BaseApkInfo {
        ApkInfoCompatBefore300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }
    }

    private static class ApkInfoCompat300Preview extends BaseApkInfo {

        ApkInfoCompat300Preview(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        File getApkFile() {
            return applicationVariant.variantData.scope.apkLocation
        }

        @Override
        boolean isSigningReady() {
            if (output.hasProperty('variantOutputData')) {
                return output.variantOutputData.variantData.signed
            } else {
                return super.isSigningReady()
            }
        }
    }

    private static class ApkInfoCompatLatest extends BaseApkInfo {
        ApkInfoCompatLatest(applicationVariant, variantOutput) {
            super(applicationVariant, variantOutput)
        }

        @Override
        boolean isUniversalApk() {
            return variantOutput.filters.empty
        }
    }
}