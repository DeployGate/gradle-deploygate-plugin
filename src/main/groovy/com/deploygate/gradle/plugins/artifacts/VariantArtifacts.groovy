package com.deploygate.gradle.plugins.artifacts

import org.gradle.api.provider.Provider
import org.jetbrains.annotations.NotNull

/**
 * Resolves the final APK/AAB outputs of an AGP variant through the public Variant/Artifacts API
 * (androidComponents). This replaces the previous reflection into the internal package task, and
 * works uniformly across AGP versions without any version-specific branching.
 *
 * AGP classes are loaded by a classloader that is separate from this plugin's, so everything here
 * is accessed dynamically (no compile-time references to AGP types) and the artifact type constants
 * are looked up through the variant's own classloader.
 */
class VariantArtifacts {
    private VariantArtifacts() {
    }

    @NotNull
    static Provider<ApkInfo> apkInfoProvider(@NotNull /* ApplicationVariant */ variant) {
        def variantName = variant.name
        def artifacts = variant.artifacts
        def loader = artifacts.getBuiltArtifactsLoader()
        def apkArtifactType = singleArtifact(variant, 'APK')

        return artifacts.get(apkArtifactType).map { apkDirectory ->
            def builtArtifacts = loader.load(apkDirectory)
            def elements = builtArtifacts?.elements ?: []
            File apkFile = elements.isEmpty() ? null : new File(elements.iterator().next().outputFile)
            // DeployGate only accepts a single universal APK; multiple elements imply split APKs.
            boolean universal = elements.size() == 1
            // Signing readiness is no longer pre-checked here: the public API does not expose it,
            // and DeployGate rejects unsigned uploads server-side.
            return new DirectApkInfo(variantName, apkFile, true, universal)
        }
    }

    @NotNull
    static Provider<AabInfo> aabInfoProvider(@NotNull /* ApplicationVariant */ variant) {
        def variantName = variant.name
        def bundleArtifactType = singleArtifact(variant, 'BUNDLE')

        return variant.artifacts.get(bundleArtifactType).map { aabRegularFile ->
            return new DirectAabInfo(variantName, aabRegularFile.asFile)
        }
    }

    /**
     * Resolves a {@code com.android.build.api.artifact.SingleArtifact} constant (e.g. APK, BUNDLE)
     * via the variant's classloader, since AGP lives on a different classloader than this plugin.
     */
    private static Object singleArtifact(@NotNull variant, @NotNull String name) {
        return variant.class.classLoader
                .loadClass("com.android.build.api.artifact.SingleArtifact\$${name}")
                .INSTANCE
    }
}
