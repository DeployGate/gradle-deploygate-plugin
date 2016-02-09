#  Gradle DeployGate Plugin

[![Build Status](https://travis-ci.org/DeployGate/gradle-deploygate-plugin.png?branch=master)](https://travis-ci.org/DeployGate/gradle-deploygate-plugin)

This is the DeployGate plugin for the Gradle. You can build and deploy your apps to DeployGate by running a single task.

## Getting started

### `dg deploy` way (for OS X)

If you have installed [`dg` command](https://github.com/deploygate/deploygate-cli) on your Mac, just move to your project root directory and type `dg deploy`.

```
cd Your/Project/Root
dg deploy
```

`dg` will setup the Gradle DeployGate Plugin on your project (if not yet installed) and start uploading.


### Gradle way 

1) Open your <code>build.gradle</code> on your project root and add a dependency.
```groovy
dependency {
  classpath 'com.deploygate:gradle:1.0.4'
}
```

2) Open your module build script file (<em>e.g.</em> <code>app/build.gradle</code>) and add the following line just after <code>apply plugin: 'com.android.application'</code>.
```groovy
apply plugin: 'deploygate'
```

3) If you are using Android Studio, click <strong>Sync Now</strong> link appearing on the right top corner of your editor window.

4) You are all set! Open <strong>Gradle</strong> tab on the right side of your Android Studio, and select <strong>Tasks - deploygate - uploadDeployGateDebug</strong> under your app module, or you can run as a Gradle command like:
```
./gradlew :app:uploadDeployGateDebug
```

By running `uploadDeployGate<FlavorName>` task, it will build your application,
set up your DeployGate credentials (for the first time) and upload your application.
You can deploy an update of your application by running the same task.


# Usage

## Tasks

Run `./gradlew tasks` on your project root to see all available tasks. 

* `uploadDeployGate[FlavorName]` - Build and upload app of [FlavorName]
* `loginDeployGate` - Log in to DeployGate and save credentials locally
* `logoutDeployGate` - Delete current credentials

If you define flavors in `apks` section, there will also be `uploadDeployGate` task which can upload all the flavors at once.   

## Example of `build.gradle`

### Project Build File

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'com.deploygate:gradle:1.0.4'   // add this line
  }
}
```

### Module Build File

```groovy
apply plugin: 'deploygate'                    // add this *after* 'android' plugin 

// Optional configurations
deploygate {

  // If you are using automated build, you can specify your account credentials like this
  userName = "[username of app owner]"
  token = "[your API token]"

  // You can also specify additional options for each flavor.
  apks {
    
    // this correspond to `debug` flavor and used for `uploadDeployGateDebug` task 
    debug {
      // ProTip: get git hash for current commit for easier troubleshooting
      def hash = 'git rev-parse --short HEAD'.execute([], project.rootDir).in.text.trim()
      // set as build message
      message = "debug build ${hash}"

      // if you are using a distribution page, you can update it simultaneously
      distributionKey = "1234567890abcdef1234567890abcdef"
      releaseNote = "release note sample"
    }
    
    // this creates `uploadDeployGateCustom` task to upload arbitrary APK file 
    custom {
      // set target file
      sourceFile = file("${project.rootDir}/app/build/some-custom-build.apk")
    }
  }
}
```

### Environment Variables

If you are using Continuous Integration, you can set these environment variables 
to provide default values for DeployGate Plugin instead of writing in `build.gradle`.

 * `DEPLOYGATE_USER_NAME`
 * `DEPLOYGATE_API_TOKEN`
 * `DEPLOYGATE_MESSAGE`
 * `DEPLOYGATE_DISTRIBUTION_KEY`
 * `DEPLOYGATE_RELEASE_NOTE`
 * `DEPLOYGATE_SOURCE_FILE`
 * `DEPLOYGATE_OPEN_BROWSER` (Env only; open the app page after the uploading finished) 

By using environment variables, you can avoid storing your credentials
in your source code repository and compose deployment messages dynamically.

For example, you can set application owner user to the organization you are belonging to
by running task like:

```
DEPLOYGATE_USER_NAME=YourOrganizationName ./gradlew :app:uploadDeployGateDebug
```

Note that these values are used as default values so `build.gradle` may override them.


# Changes

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

# License

Copyright 2015 DeployGate Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
