# ChangeLog

## ver 2

TBW

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
