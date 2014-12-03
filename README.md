[![Build Status](https://travis-ci.org/DeployGate/gradle-deploygate-plugin.png?branch=master)](https://travis-ci.org/DeployGate/gradle-deploygate-plugin)

This is the DeployGate plugin for the Gradle.  
This plugin, you can use the deploygate API from Gradle easily.

For issue tracking see the GitHub issues page: https://github.com/DeployGate/gradle-deploygate-plugin/issues

## Update
### ver 0.6.2
* Supported proxy

### ver 0.6.1
* Fix error message
* Supported Push API visibility option

## Usage
### Tasks
* uploadDeployGate              - Uploads the APK file. Also updates the distribution specified by distributionKey if configured
* uploadDeployGate[FlavorName]  - Upload an APK file of [FlavorName]

### Edit build.gradle

```
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'com.deploygate:gradle:0.6.2'
  }
}
apply plugin: 'deploygate'

deploygate {
  userName = "[owner name]"
  token = "[token]"

  apks {
    release {
      sourceFile = file("[apk1 file path]")
    }

    debug {
      sourceFile = file("[apk2 file path]")

      //Below is optional
      message = "test upload2 sample"
      visibility = "public" // default private
      distributionKey = "[distribution_key]"
      releaseNote = "release note sample"
    }
  }
}
```
Replace [owner name] [apk file path] [token] [distribution_key] with your param.  
Please check [Push API](https://deploygate.com/docs/api) for param information. 

### Run

```
$ gradle uploadDeployGate 
```

or

```
$ gradle uploadDeployGate[FlavorName]
```

## License
Copyright 2012-2014 DeployGate, henteko

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
