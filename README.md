This is the DeployGate plugin for the Gradle.  
This plugin, you can use the deploygate API from Gradle easily.

### Please check [document](https://deploygate.com/docs/gradle) for usage.

For issue tracking see the GitHub issues page: https://github.com/DeployGate/gradle-deploygate-plugin/issues

## Usage
### Tasks
uploadDeployGate          - Upload the apk file to deploygate  
updateDeployGateDistribution - Apk upload and distribution update  

### Edit build.gradle

```
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.deploygate:gradle:0.2'
  }
}
apply plugin: 'deploygate'

deploygate {
  userName "[owner name]"
  apkPath "[apk file path]"
  token "[token]"
  message "sample"

  //Below is optional
  distributionKey "[distribution_key]"
  releaseNote "release note sample"
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
$ gradle updateDeployGateDistribution 
```

## License
Copyright 2012 DeployGate, henteko

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
