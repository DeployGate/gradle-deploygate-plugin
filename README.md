This is the DeployGate plugin for the Gradle.  
This plugin, you can use the deploygate API from Gradle easily.

For issue tracking see the GitHub issues page: https://github.com/DeployGate/gradle-deploygate-plugin/issues

## Usage
### This plugin is published in preparation to the Maven Central Repository

### Tasks
apkUpload          - Upload the apk file to deploygate
distributionUpdate - Apk upload and distribution update

### Edit build.gradle

```
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.deploygate:gradle:0.1'
  }
}
apply plugin: 'deploygate'

deploygate {
  userName "<deploygate user name>"
  apkPath "<apk file path>"
  token "<deploygate api key>"
  message "message(optional)"
  distributionKey "<deploygate distribution key(distributionUpdate task)>"
  releaseNote "release note(distributionUpdate task optional)"
}
```

### run

```
$ gradle apkUpload
```
or

```
$ gradle distributionUpdate 
```


## Licence
Copyright 2012 DeployGate, henteko

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
