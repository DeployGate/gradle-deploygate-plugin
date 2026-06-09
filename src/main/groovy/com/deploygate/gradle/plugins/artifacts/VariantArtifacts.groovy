package com.deploygate.gradle.plugins.artifacts

import org.gradle.api.file.Directory
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
 *
 * Two resolution modes exist:
 * <ul>
 *   <li><b>assembled</b> ({@link #apkInfoProvider}/{@link #aabInfoProvider}) — wires the strict
 *       Artifacts API provider; the caller must make the upload task depend on the assemble/bundle
 *       task so the artifact is produced before it is queried.</li>
 *   <li><b>preset/skipAssemble</b> ({@link #apkInfoFromConventionalOutput}/{@link #aabInfoFromConventionalOutput})
 *       — reads from AGP's conventional output directory without depending on (or triggering) the
 *       build, so a separately-built artifact can be uploaded.</li>
 * </ul>
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
            return toApkInfo(variantName, loader, apkDirectory)
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

    @NotNull
    static Provider<ApkInfo> apkInfoFromConventionalOutput(@NotNull /* ApplicationVariant */ variant, @NotNull Provider<Directory> buildDirectory) {
        def variantName = variant.name
        def loader = variant.artifacts.getBuiltArtifactsLoader()
        def relativePath = apkOutputRelativePath(variant)

        return buildDirectory.map { buildDir ->
            return toApkInfo(variantName, loader, buildDir.dir(relativePath))
        }
    }

    @NotNull
    static Provider<AabInfo> aabInfoFromConventionalOutput(@NotNull /* ApplicationVariant */ variant, @NotNull Provider<Directory> buildDirectory) {
        def variantName = variant.name

        return buildDirectory.map { buildDir ->
            def bundleDir = buildDir.dir("outputs/bundle/${variantName}").asFile
            File aabFile = bundleDir.listFiles()?.find { it.isFile() && it.name.endsWith(".aab") }
            return new DirectAabInfo(variantName, aabFile)
        }
    }

    private static ApkInfo toApkInfo(@NotNull String variantName, @NotNull loader, @NotNull Directory apkDirectory) {
        def builtArtifacts = loader.load(apkDirectory)
        def elements = builtArtifacts?.elements ?: []
        File apkFile = elements.isEmpty() ? null : resolveOutputFile(apkDirectory, elements.iterator().next().outputFile)
        // DeployGate only accepts a single universal APK; more than one element implies split APKs.
        // An empty result means the APK has not been built yet (apkFile stays null) rather than a
        // split build, so it is not flagged non-universal here — the null artifact surfaces the
        // clearer "artifact was not found" error downstream instead of a misleading non-universal one.
        boolean universal = elements.size() <= 1
        return new DirectApkInfo(variantName, apkFile, universal)
    }

    /**
     * {@code BuiltArtifact.outputFile} is an absolute path on supported AGP versions, but resolve it
     * against the artifact directory defensively in case a relative path is ever returned.
     */
    private static File resolveOutputFile(@NotNull Directory apkDirectory, @NotNull String outputFile) {
        def file = new File(outputFile)
        return file.isAbsolute() ? file : new File(apkDirectory.asFile, outputFile)
    }

    /**
     * AGP places APKs under {@code build/outputs/apk/<flavorName>/<buildType>} (the flavor segment is
     * omitted when the variant has no product flavors).
     */
    private static String apkOutputRelativePath(@NotNull variant) {
        def flavorName = variant.flavorName
        def buildType = variant.buildType
        if (flavorName) {
            return "outputs/apk/${flavorName}/${buildType}"
        }
        return "outputs/apk/${buildType}"
    }

    /**
     * Resolves a {@code com.android.build.api.artifact.SingleArtifact} constant (e.g. APK, BUNDLE)
     * via the variant's classloader, since AGP lives on a different classloader than this plugin.
     */
    private static Object singleArtifact(@NotNull variant, @NotNull String name) {
        // getClass() (not .class) avoids dynamic property interception on decorated AGP objects.
        return variant.getClass().classLoader
                .loadClass("com.android.build.api.artifact.SingleArtifact\$${name}")
                .INSTANCE
    }
}
