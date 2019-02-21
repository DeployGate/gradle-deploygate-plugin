#  Gradle DeployGate Plugin

[![Build Status](https://travis-ci.org/DeployGate/gradle-deploygate-plugin.png?branch=master)](https://travis-ci.org/DeployGate/gradle-deploygate-plugin)
[ ![Download](https://api.bintray.com/packages/deploygate/maven/gradle/images/download.svg) ](https://bintray.com/deploygate/maven/gradle/_latestVersion)

This is the DeployGate plugin for the Gradle. You can build and deploy your apps to DeployGate by running a single task.

## Getting started

1 ) Add jcenter to and the dependency of this plugin to your *build.gradle*.

```groovy
buildscript {
  ext {
    deployGatePluginVersion = '...'
  }
  repositories {
    jcenter()
  }

  dependencies {
    ... // maybe `classpath 'com.android.tools.build:gradle:x.y.z'` also exists
    classpath "com.deploygate:gradle:$deployGatePluginVersion"
  }
}
```

If you are using the new plugin DSL, then the following is required in your *setting.gradle*.

```groovy
pluginManagement {
    repositories {
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            switch (requested.id.id) {
                case "deploygate":
                    useModule("com.deploygate:gradle:${required.version}")
                    break
            }
        }
    }
}
```

2 ) Apply this plugin to your app module

```groovy
apply plugin: 'com.android.application' // It's better to apply Android Plugin for Gradle first. 
apply plugin: 'deploygate'
```

This plugin does not work with non-app modules and/or library modules correctly.

3 ) You can see DeployGate tasks

```
# Tasks, which relate with variants which generate split apks, are not visible because they do not belong to any group.
./gradlew tasks | grep "DeployGate"
```

If you are an android app development beginner.

- Do `Sync Gradle` if you are using AndroidStudio and/or IDEA.

By running `uploadDeployGate<VariantName>` task, it will build your application,
set up your DeployGate credentials (for the first time) and upload your application.
You can deploy an update of your application by running the same task.

### If you are using `dg` command (for MacOSX)

[dg](https://github.com/deploygate/deploygate-cli) is a command line tool to help your deployments to DeployGate.
The command will make diffs to apply this plugin if you run `dg deploy` on the project root. 

## Usage

### Tasks

Run `./gradlew tasks` on your project root to see all available tasks. 

* `uploadDeployGate[capitalized VariantName]` - Build and upload an apk artifact of *\<VariantName\>*
* `loginDeployGate` - Log in to DeployGate and save credentials locally
* `logoutDeployGate` - Delete current credentials

[VariantName] is built by appending capitalized names of productFlavor and buildType.
For example, `fooBar` is a variant name if you have `foo` product flavor and `bar` build type.

If you define deployment names in `deployments` section, there will also be `uploadDeployGate` task which can upload all the associated deployments at once.

## How configure your deployments

### build.gradle

*v2* has changed the DSL. See [Migrate to v2](#migrate-v2) for more detail.

```groovy
apply plugin: 'deploygate'                    // add this *after* 'android' plugin 

// Optional configuration
deploygate {

  // If you are using automated build, you can specify your account credentials like this
  appOwnerName = "[name of app owner]"
  apiToken = "[your or app owner's API token]"

  // You can also specify additional configurations for each variants.
  deployments {
    
    // This corresponds to `flavor1` product flavor and `debug` buildType
    // This configuration will be used for `uploadDeployGateFlavor1Debug` task 
    flavor1Debug {
      // ProTip: Use Git hash of the current commit for easier troubleshooting
      def hash = 'git rev-parse --short HEAD'.execute([], project.rootDir).in.text.trim()

      // Set a text which is associated with an application file on DeployGate
      message = "debug build ${hash}" // null by default

      // `uploadDeployGateFlavor1Debug` will skip running `assembleFlavor1Debug` if this property is `true`.
      skipAssemble = true // false by default
      
      // This property is basically optional.
      // Because this plugin will set the apk path automatically if you would like to upload a build artifact of this variant
      sourceFile = file("${project.rootDir}/app/build/outputs/apk/manual-manipulate/app-signed.apk")

      // You can update a distribution as well. This configuration is optional.
      // Known limitation: *name* is not supported, so this plugin cannot create a new distribution.
      distribution {
          // A key of an existing distribution 
          key = "1234567890abcdef1234567890abcdef"
          // A release note of a distribution which is associated with this build
          releaseNote = "release note sample"
      }
    }
    
    // You can define any names which you would like to use.
    // In this case, this plugin creates `uploadDeployGateUniversalApkOfAab` task to upload the specified apk file.
    // Custom
    universalApkOfAab {
      sourceFile = file("${project.rootDir}/app/build/from-aab/universal.apk")

      // All options except skipAssemble will work fine as well.
    }
  }
}
```

### Environment Variables

You can configure this plugin as well by providing environment variables. This would be useful for CI/CD.

 * `DEPLOYGATE_USER_NAME`
 * `DEPLOYGATE_API_TOKEN`
 * `DEPLOYGATE_MESSAGE`
 * `DEPLOYGATE_DISTRIBUTION_KEY`
 * `DEPLOYGATE_RELEASE_NOTE`
 * `DEPLOYGATE_SOURCE_FILE`
 * `DEPLOYGATE_OPEN_BROWSER` (Env only; open the app page after the uploading finished) 

Environment variable configurations allow you to avoid writing your credentials directly in the code.

Tip: You do not need to export these values to the current shell. You can use DEPLOYGATE_USER_NAME like the following:

```
DEPLOYGATE_USER_NAME=YourOrganizationName ./gradlew :app:uploadDeployGateFlavor1Debug
```

Note that this plugin will read environment values first, and overwrite them by specified configurations later.
Configuration priority is based on the following.

*Specified configurations* \> *Environment variables* \> *Auto detection*

## Known limitations

- To create a new distribution is not supported
- Split apks are not supported
- Android App Bundle is not supported. See [a tracking issue and a workaround](https://github.com/DeployGate/gradle-deploygate-plugin/issues/60#issuecomment-464448962).

## <a name="migrate-v2">Migrate v1 to v2</a>

We have deprecated some syntax and introduced the new syntax based on the table below.

Deprecated | New
:---|:----
*userName* | Use **appOwnerName**
*token* | Use **apiToken**
*apks* closure | Use **deployments** closure
*noAssemble* | Use **skipAssemble**
*distributionKey* | Use **key** in **distribution** closure
*releaseNote* | Use **releaseNote** in **distribution** closure

*v2.0.x can use the v1 syntax as it is, but we will make it obsolete on v2.1.0*  

Let's say we have a v1 configuration like below. 

```groovy
deploygate {
  userName = "deploygate-user"
  token = "abcdef..."
  apks {
    flavor1Debug {
      noAssmble = true
      distributionKey = "xyz..."
      releaseNote = "foobar"
    }
  }
}
```

then, new v2 configuration which is the same to the above will be like below:

```groovy
deploygate {
  appOwnerName = "deploygate-user"
  apiToken = "abcdef..."
  deployments {
    flavor1Debug {
      skipAssemble = true
      distribution {
        key = "xyz..."
        releaseNote = "foobar"
      }
    }
  }
}
```

Please feel free to open an issue on this repository if you have any questions.

# ChangeLog

See [CHANGELOG.md](./CHANGELOG.md)

# License

Copyright 2015-2019 DeployGate Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
