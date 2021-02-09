#  Gradle DeployGate Plugin

[![Build Status](https://travis-ci.org/DeployGate/gradle-deploygate-plugin.png?branch=master)](https://travis-ci.org/DeployGate/gradle-deploygate-plugin)
[ ![Download](https://img.shields.io/maven-central/v/com.deploygate/gradle) ](https://search.maven.org/artifact/com.deploygate/gradle)

This is the DeployGate plugin for the Gradle. You can build and deploy your apps to DeployGate by running a single task.

*We have migrated to `v2`. See [Migrate from v1 to v2](#migrate-v2) for the migration for more detail*

## Getting started

Snapshot? See [how to use snapshot](#snapshot)

1 ) Add mavenCentral to and the dependency of this plugin to your *build.gradle*.

```groovy
buildscript {
  ext {
    deployGatePluginVersion = '...'
  }
  repositories {
    mavenCentral()
  }

  dependencies {
    ... // maybe `classpath 'com.android.tools.build:gradle:x.y.z'` also exists
    classpath "com.deploygate:gradle:$deployGatePluginVersion"
  }
}
```

If you are using the new plugin block DSL, then the following is required in your *settings.gradle*.

```groovy
pluginManagement {
    repositories {
        mavenCentral()
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

*The new plugin block DSL*

```groovy
plugins {
    id "com.deploygate" version "the latest version"
}
```

This plugin does not work with non-app modules and/or library modules correctly.

3 ) Ready for deployments. Run tasks which you need. Please check the *Usage#Tasks* section for the detail of added tasks.
 
### If you are using `dg` command (for MacOSX)

[dg](https://github.com/deploygate/deploygate-cli) will make diffs to apply this plugin if you run `dg deploy` on the project root. 

## Usage

### Tasks

```
./gradlew tasks | grep "DeployGate"
```

* `loginDeployGate` - Log in to DeployGate and save credentials to your local
* `logoutDeployGate` - Delete current credentials
* `uploadDeployGate<capitalized VariantName>` - Build and upload an apk artifact of *\<VariantName\>*
* `uploadDeployGate` - Run uploadDeployGateXXX tasks which are defined in a gradle file
* `uploadDeployGateAab<capitalized VariantName>` - Build and upload an aab artifact of *\<VariantName\>*
* `uploadDeployGateAab` - Run uploadDeployGateAabXXX tasks which are defined in a gradle file

*NOTE: Tasks, which relate with variants which generate split apks, are not visible because they do not belong to any group.*

> [VariantName] is built by appending capitalized names of productFlavor and buildType.
> For example, `fooBar` is a variant name if you have `foo` product flavor and `bar` build type.

#### loginDeployGate

This task reads stored and/or specified credentials. 
If no credentials are found, this requests you to log in to DeployGate and save credentials to your local.

#### logoutDeployGate

This task deletes stored credentials on your local.

#### uploadDeployGate\<capitalized VariantName\> or uploadDeployGateAab\<capitalized VariantName\>

These task will do:

- Assemble your app / Bundle your app
- Start set-up your DeployGate credentials if no credential is found
- Upload a built artifact to DeployGate

You can continue to deploy updates by running the same task once credential prepared.

**uploadDeployGate and/or uploadDeployGateAab**

If you define deployment names in `deployments` section, there will also be `uploadDeployGate` task which can upload all the associated deployments at once.
For example, `uploadDeployGate` will run `uploadDeployGateFoo` and `uploadDeployGateBar`, and `uploadDeployGateAab` will run `uploadDeployGateAabFoo` and `uploadDeployGateAabBar` based on a configuration below.

```
deploygate {
  deployments {
    foo { ... }
    bar { ... }
  }
}
```

## How configure your deployments

*v2* has changed the DSL. See [Migrate from v1 to v2](#migrate-v2) for more detail. 

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
      def hash = "git rev-parse --short HEAD".execute([], project.rootDir).in.text.trim()

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
      
      // If you are using KotlinDSL
      distribution(closureOf<com.deploygate.gradle.plugins.dsl.Distribution> {
          ...
      })
    }
    
    // You can define any names which you would like to use.
    // In this case, this plugin creates `uploadDeployGateUniversalApkOfAab` task to upload the specified apk file.
    universalApkOfAab {
      sourceFile = file("${project.rootDir}/app/build/from-aab/universal.apk")

      // All options except skipAssemble will work fine as well.
    }
  }
}
```

### Environment Variables

*v2* has renamed some environment variables. See [Migrate from v1 to v2](#migrate-v2) for more detail.

You can configure this plugin as well by providing environment variables. This would be useful for CI/CD.

 * `DEPLOYGATE_APP_OWNER_NAME`
 * `DEPLOYGATE_API_TOKEN`
 * `DEPLOYGATE_MESSAGE`
 * `DEPLOYGATE_DISTRIBUTION_KEY`
 * `DEPLOYGATE_DISTRIBUTION_RELEASE_NOTE`
 * `DEPLOYGATE_SOURCE_FILE`
 * `DEPLOYGATE_OPEN_BROWSER` (Env only; open the app page after the uploading finished) 

Environment variable configurations allow you to avoid writing your credentials directly in the code.

Tip: You do not need to export these values to the current shell. You can use DEPLOYGATE_USER_NAME like the following:

```
DEPLOYGATE_APP_OWNER_NAME=YourOrganizationName ./gradlew uploadDeployGateFlavor1Debug
```

Note that this plugin will read environment values first, and overwrite them by specified configurations later.
Configuration priority is based on the following.

*Specified configurations* \> *Environment variables* \> *Auto detection*

## Proxy setting

You can configure proxy settings via system properties. Please follow the official document of Gradle.

- https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy
- https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties

## Known limitations

- To create a new distribution is not supported
- Split apks are not supported
- Tasks will be added after project-evaluated

## <a name="snapshot">Snapshot</a>

You can use `jitpack.io` for snapshots.

```groovy
buildscript {
  repositories {
    maven { url "https://jitpack.io" }
  }

  dependencies {
    // if you want to use the specified commit
    classpath "com.github.deploygate:gradle-deploygate-plugin:${commit_hash}"
    // if you want to use the branch HEAD
    classpath "com.github.deploygate:gradle-deploygate-plugin:${branch_name}-SNAPSHOT"
  }
}
```

jitpack.io will store artifacts once required, so the request may cause time-out for the first time.
If you get a time-out error from jitpack, then please run your task again.

## Development

You can try this plugin locally by following the steps below.

0. Clone this repository
1. Edit `/VERSION` file to a non-released version (e.g. 2.0.0-beta01)
2. Run `./gradlew install` to make it available on your local
3. Add mavenLocal to buildscript repository of a test project
4. Specify the version which you specify at step 1

And also, please make sure your changes pass unit tests and acceptance tests.  

```bash
./gradlew test acceptanceTest
```

## <a name="migrate-v2">Migrate from v1 to v2</a>

We have deprecated some syntax and introduced the new syntax based on the table below.

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

*If both of v1 and v2 variables are specified, v2 variables will be used.*

**v2.0.x can use the v1 syntax as it is, but we will start to make it obsolete from v2.1.0**  

Let's say we have a v1 configuration like below. 

```groovy
deploygate {
  userName = "deploygate-user"
  token = "abcdef..."
  apks {
    flavor1Debug { // create("flavor1Debug") if Kotlin DSL
      noAssmble = true
      distributionKey = "xyz..."
      releaseNote = "foobar"
    }
  }
}
```

### v2 Groovy configuration

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

### v2 Kotlin DSL

NOTE: 2.0.1 fixed the broken DSL. Please upgrade to 2.0.1 if you are using 2.0.0 and have troubles with it.

then, new v2 configuration which is the same to the above will be like below:

```kotlin
import com.deploygate.gradle.plugins.dsl.Distribution

deploygate {
  appOwnerName = "deploygate-user"
  apiToken = "abcdef..."
  deployments {
    create("flavor1Debug") {
      skipAssemble = true
      distribution(closureOf<Distribution> {
        key = "xyz..."
        releaseNote = "foobar"
      })
    }
  }
}
```

Please feel free to open an issue on this repository if you have any questions.

# ChangeLog

See [CHANGELOG.md](./CHANGELOG.md)

# License

Copyright 2015-2020 DeployGate Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
