# ChangeLog

## ver 2.8.0

- Support Android Gradle Plugin 8.3.0 (Iguana) with Gradle 8.4

## ver 2.7.0

- Support Android Studio 8.1 (Giraffe) officially

## ver 2.6.0

- Support Gradle 8 builds. [#159](https://github.com/DeployGate/gradle-deploygate-plugin/pull/159)
- Use lazy configuration to set up.

*Breaking Changes*

- Drop support for Android Gradle Plugin 4.1.x or lower. [#164](https://github.com/DeployGate/gradle-deploygate-plugin/pull/164)
  - Changed the minimum required JRE 7 -> 8 and the minimum required Gradle 5.4 -> 6.7.1.
- Builds will fail if only api token is provided but no app owner name is found. [#172](https://github.com/DeployGate/gradle-deploygate-plugin/pull/172)

## ver 2.5.0

- Deprecate `visibility` property [#136](https://github.com/DeployGate/gradle-deploygate-plugin/pull/136)

This version also includes

- Several improvements for developers' support.
- Arctic Fox, Bumblebee, Chipmunk supports.

## ver 2.4.0

- Support Android Studio 4.2.0 stable [#114](https://github.com/DeployGate/gradle-deploygate-plugin/pull/114)
- Support Gradle 7.0 which is the min required version of Arctic Fox [#121](https://github.com/DeployGate/gradle-deploygate-plugin/pull/121)

## ver 2.3.0

- Expose the response of Upload API for other tasks [#109](https://github.com/DeployGate/gradle-deploygate-plugin/pull/109)

Includes changes to support Android Studio 4.1.0-beta01, 4.2.0-alpha01 [#106](https://github.com/DeployGate/gradle-deploygate-plugin/pull/106)

## ver 2.2.0

- Support Android Studio 4.0.0 ref: [#100](https://github.com/DeployGate/gradle-deploygate-plugin/issues/100), [#102](https://github.com/DeployGate/gradle-deploygate-plugin/pull/102)

## ver 2.1.0

- Support Android Studio 3.6.0-rc01 (and beta01-05) ref: [#90](https://github.com/DeployGate/gradle-deploygate-plugin/issues/90)
- Support App Bundle Upload ref: [#60](https://github.com/DeployGate/gradle-deploygate-plugin/issues/60)

## ver 2.0.2

- Fixed `skipAssemble=true` caused a build failure because of AGP API breaking changes ref: [#86](https://github.com/DeployGate/gradle-deploygate-plugin/issues/86)

## ver 2.0.1

- Fixed unexpected broken v1 configuration on Kotlin DSL

## ver 2.0.0

- Change the DSL syntax
- Change environment variable names
- Support Android Studio 3.3.0 and higher
- Avoid using obsoleted apis of Android Plugin for Gradle whose version is lower than 3.4.0-beta04

*Breaking changes*

- Drop Android Studio 2 supports
- Drop Android Studio 3.0.0-preview supports

*Deprecation*

Deprecated | New
:---|:----
*userName* | Use **appOwnerName**
*token* | Use **apiToken**
*apks* closure | Use **deployments** closure
*noAssemble* | Use **skipAssemble**
*distributionKey* | Use **key** in **distribution** closure
*releaseNote* | Use **releaseNote** in **distribution** closure
*DEPLOYGATE_USER_NAME* env | Use **DEPLOYGATE_APP_OWNER_NAME** instead
*DEPLOYGATE_RELEASE_NOTE* env | Use **DEPLOYGATE_DISTRIBUTION_RELEASE_NOTE** instead

*Other*

- Revised README.md
- Separated README.md and CHANGELOG
- Supported snapshot versions
- Added an example to build a universal apk from Android App Bundle

## ver 1.1.5

 * Fixed that loginDeployGate task couldn't open a browser. [#47](https://github.com/DeployGate/gradle-deploygate-plugin/issues/47)

## ver 1.1.4

 * Fix producing corrupted task names for flavors on Android Plugin for Gradle 3.0.0 (like uploadDeployGateDev-debug for uploadDeployGateDevDebug)

## ver 1.1.3

 * Restore auto configuring APK file path functionality (supports Android Gradle Plugin 3.0.0-alpha4)

## ver 1.1.2

 * Fix failing first time upload with Free plans

## ver 1.1.1

 * Workaround for the issue on Android Gradle Plugin 3.0 Preview
    * You need to specify the `sourceFile` option manually in your build.gradle to upload builds. This temporal limitation will be resolved in future release of Android Gradle Plugin.

## ver 1.1.0

 * Add noAssemble option for just uploading artifacts (by @operando)

## ver 1.0.4

 * Fix: Browser doesn't open on the first upload

## ver 1.0.3

 * Open the app page for the first upload or environment variable set

## ver 1.0.2

 * Restore `uploadDeployGate` feature

## ver 1.0.1

 * Prevent invoking browser on headless environment
 * Allow passing values from environment variable

## ver 1.0.0

 * Support browser log in and share credentials with `dg` command.
 * DeployGate plugin now handles all Android project automatically, so you don't have to write `deploygate` settings to your `build.gradle`.
