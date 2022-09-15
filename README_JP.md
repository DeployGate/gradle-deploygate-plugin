#  Gradle DeployGate Plugin

[![Build Status](https://travis-ci.org/DeployGate/gradle-deploygate-plugin.png?branch=master)](https://travis-ci.org/DeployGate/gradle-deploygate-plugin)
[ ![Download](https://img.shields.io/maven-central/v/com.deploygate/gradle) ](https://search.maven.org/artifact/com.deploygate/gradle)

DeployGate Gradle プラグインです。DeployGate 経由でアプリを配信するための Gradle タスクが簡単に設定できます。

## Getting started

スナップショットリリースを使いたい方は[こちら](#snapshot)。

1 ) *build.gradle* を編集し、mavenCentral と DeployGate Gradle プラグインの記述を追加します。

```groovy
buildscript {
  ext {
    deployGatePluginVersion = '...'
  }
  repositories {
    mavenCentral()
  }

  dependencies {
    ... // `classpath 'com.android.tools.build:gradle:x.y.z'` も記述されている場合があります。
    classpath "com.deploygate:gradle:$deployGatePluginVersion"
  }
}
```

plugin ブロック記法を利用している人は *setting.gradle* に下記の設定が必要です。

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

2 ) app モジュールに DeployGate Gradle プラグインを apply します。

```groovy
apply plugin: 'com.android.application' // Android Plugin for Gradle を先に apply してください
apply plugin: 'deploygate'
```

*plugin ブロック記法*

```groovy
plugins {
    id "com.deploygate" version "the latest version"
}
```

DeployGate Gradle プラグインは非 app モジュールや android-library モジュールだと動作しないことがあります。

3 ) これでデプロイの初期設定は完了しています。

実行するタスクについては次の `Usage#Tasks` セクションを参照してください。

## Version Compatibility

Android Gradle Plugin | Gradle DeployGate Plugin
:----|:----
< 3.0.0 | 1.1.5 (保守されません)
3.3.x, 3.4.x, 3.5.x | 2.0.0から
3.6.x | 2.1.0から
4.0.x | 2.2.0から
4.1.x | 2.3.0から
4.2.x | 2.4.0から
7.0.x (Arctic Fox) | 2.4.0から
7.1.x (Bumblebee) | 2.4.0から
7.2.x (Chipmunk) | 2.4.0から
7.3.x (Dolphin) | 2.4.0から

## Usage

### Tasks

```
./gradlew tasks | grep "DeployGate"
```

* `loginDeployGate` - (認証情報が取得できなかった場合) DeployGate へのログインと認証データの保存を行います
* `logoutDeployGate` - ローカルに保存された認証情報削除します
* `uploadDeployGate<capitalized VariantName>` - *\<VariantName\>* に対応した apk をアップロードします
* `uploadDeployGate` - 設定に明示的に記述された uploadDeployGateXXX タスクを全て実行します
* `uploadDeployGateAab<capitalized VariantName>` - *\<VariantName\>* に対応した aab をアップロードします
* `uploadDeployGateAab` - 設定に明示的に記述された uploadDeployGateAabXXX タスクを全て実行します

*NOTE: Split apks に紐付いたタスクは `:tasks` で表示されません。*

> [VariantName] は頭文字を大文字にした productFlavor と buildType から構成されます。
> 例えば `fooBar` であれば `foo` product flavor と `bar` build type に対応します。

#### loginDeployGate

ローカル、または指定された認証情報を読み込みます。認証情報が見つからなかった場合、DeployGate へのログインと(成功した場合)認証情報のローカルへの保存を行います。

#### logoutDeployGate

ローカルに保存されている認証情報を削除します。

#### uploadDeployGate\<capitalized VariantName\> または uploadDeployGateAab\<capitalized VariantName\>

- assemble または bundle タスクの実行
- (認証情報がない場合) loginDeployGate タスクの実行
- DeployGate への apk/aab のアップロード

を行います。認証情報を設定できればタスクを実行することで、継続的にアプリの更新を行うことができます。

**uploadDeployGate または uploadDeployGateAab**

後述する `deployments` ブロックで宣言した名前に関連する `uploadDeployGateXXX` タスクを全て実行し、まとめて複数の設定を DeployGate にデプロイすることができます。

下記の設定であれば、 `uploadDeployGate` は `uploadDeployGateFoo` と `uploadDeployGateBar` を、`uploadDeployGateAab` は `uploadDeployGateAabFoo` と `uploadDeployGateAabBar` を実行します。

```
deploygate {
  deployments {
    foo { ... }
    bar { ... }
  }
}
```

## デプロイの設定方法

*v2* では設定 DSL を変更しています。その変更については [v1 から v2 への移行](#migrate-v2) を参照してください。 

```groovy
apply plugin: 'deploygate' // android plugin のあとに記述してください

// 設定は任意です
deploygate {

  // 認証情報を指定することができます
  appOwnerName = "[name of app owner]"
  apiToken = "[your or app owner's API token]"

  // 各 Variant についてのデプロイ設定をここに記述します。
  deployments {
    
    // `flavor1` product flavor と `debug` buildType への設定です。
    // `uploadDeployGateFlavor1Debug` タスクがこの設定に基づいて動作します。
    flavor1Debug {
      // ProTip: Git ハッシュをメッセージに指定すると便利です。
      def hash = "git rev-parse --short HEAD".execute([], project.rootDir).in.text.trim()

      // アップロードする apk にテキストメッセージを付与することができます。
      message = "debug build ${hash}" // デフォルトは null です。

      // このフラグが true の場合、`assembleFlavor1Debug` は実行されません。
      skipAssemble = true // デフォルトは false です。
      
      // product flavor と build type に関連付けられた設定の場合、基本的に指定する必要はありません。
      // プラグインは flavor1Debug の apk 保存先を自動で読み取ります。
      sourceFile = file("${project.rootDir}/app/build/outputs/apk/manual-manipulate/app-signed.apk")

      // 配布ページの更新も可能です。(任意)
      // 既知の制限: 配布ページ名の指定はサポートされていないため、配布ページの新規作成はできません。
      distribution {
          // 配布ページのハッシュ(key)が指定できます。
          key = "1234567890abcdef1234567890abcdef"
          // この配布ページに載せるリリースノートを指定できます。上記の key が設定されていない場合は利用されません。
          releaseNote = "release note sample"
      }
      
      // KotlinDSL を利用している場合は以下の記述をお使いください。
      distribution(closureOf<com.deploygate.gradle.plugins.dsl.Distribution> {
          ...
      })
    }
    
    // 任意の名前を設定することも可能です。
    // この設定だと `uploadDeployGateUniversalApkOfAab` タスクが生成されます。
    universalApkOfAab {
      sourceFile = file("${project.rootDir}/app/build/from-aab/universal.apk")

      // skipAssemble を除いた設定が可能です。skipAssemble は常に true と同等です。
    }
  }
}
```

### 環境変数による設定

*v2* では環境変数名を変更しました。その変更については [v1 から v2 への移行](#migrate-v2) を参照してください。

環境変数を利用して設定することが可能です。CI/CD での利用の場合などに有用です。

 * `DEPLOYGATE_APP_OWNER_NAME`
 * `DEPLOYGATE_API_TOKEN`
 * `DEPLOYGATE_MESSAGE`
 * `DEPLOYGATE_DISTRIBUTION_KEY`
 * `DEPLOYGATE_DISTRIBUTION_RELEASE_NOTE`
 * `DEPLOYGATE_SOURCE_FILE`
 * `DEPLOYGATE_OPEN_BROWSER` - 環境変数からのみ設定可能。 アップロードが終わり次第ブラウザを開くかどうかが設定できます 

*環境変数経由であれば、認証情報を直接 build.gradle に記述することなく設定できます。*

Tip: 実行しているシェルで環境変数を export をする必要はなく、下記のように設定することも可能です。

```
DEPLOYGATE_APP_OWNER_NAME=YourOrganizationName ./gradlew uploadDeployGateFlavor1Debug
```

環境変数で指定した内容よりも、*build.gradle* に記述した設定が優先されます。各設定の優先度は以下の通りです。

*build.gradle に記述された設定* \> *環境変数* \> *自動検出*

## プロキシ設定

System プロパティからプロキシの設定が可能です。詳細は Gradle 公式ドキュメントをお読みください。

- https://docs.gradle.org/current/userguide/build_environment.html#sec:accessing_the_web_via_a_proxy
- https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties

## 既知の制限

- 配布ページの新規作成ができない
- Split apks (multiple apks) のアップロードには対応していません
- プロジェクトの評価後にタスクが生成されます。

## <a name="snapshot">スナップショット</a>

`jitpack.io` が利用できます。

```groovy
buildscript {
  repositories {
    maven { url "https://jitpack.io" }
  }

  dependencies {
    // 特定のコミットを利用したい場合
    classpath "com.github.deploygate:gradle-deploygate-plugin:${commit_hash}"
    // 特定のブランチの最新版を利用したい場合
    classpath "com.github.deploygate:gradle-deploygate-plugin:${branch_name}-SNAPSHOT"
  }
}
```

jitpack.io は初回のリクエストを受けてからスナップショット作成を行うため、タイムアウトすることがあります。その場合はもう一度タスクを実行ください。

## 開発

下記のステップでローカルでプラグインを試すことができます。

0. リポジトリのクローン
1. `/VERSION` ファイルを *リリースされていないバージョン* に変更してください。e.g. 2.0.0-beta01)
2. `./gradlew publishToMavenLocal` を実行することで、ローカルの maven リポジトリに登録できます。
3. テストプロジェクトに mavenLocal を追加します。
4. ステップ1 で変更したバージョンを指定します。

また変更したあとはユニットテストと受け入れテストが通ることを確認してください。

```bash
./gradlew test acceptanceTest
```

## <a name="migrate-v2">v1 から v2 への移行</a>

いくつかの記述方法を非推奨とし、新しい記述方法を導入しています。変更点は次の表の通りです。

非推奨 | 新しく推奨される記述方法
:---|:----
*userName* | **appOwnerName**
*token* | **apiToken**
*apks* closure | **deployments** closure
*noAssemble* | **skipAssemble**
*distributionKey* | **distribution** closure の **key**
*releaseNote* | **distribution** closure の **releaseNote**
*DEPLOYGATE_USER_NAME* env | **DEPLOYGATE_APP_OWNER_NAME**
*DEPLOYGATE_RELEASE_NOTE* env | **DEPLOYGATE_DISTRIBUTION_RELEASE_NOTE**

*非推奨となった記述と新しい記述が混在する場合、新しい記述が基本的には優先されます。*

**バージョン 2.0.x は v1 の記法をそのまま利用できます。バージョン2.1.0 より、非推奨となった記法を削除していきます。**  

以下の v1 の設定に対して、v2 への移行を行います。

```groovy
deploygate {
  userName = "deploygate-user"
  token = "abcdef..."
  apks {
    flavor1Debug {
    // Kotlin DSL では create("flavor1Debug")
      noAssmble = true
      distributionKey = "xyz..."
      releaseNote = "foobar"
    }
  }
}
```

### v2 Groovy 設定

v2 の Groovy での設定は以下の通りです。

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

NOTE: バージョン 2.0.0 では意図しない記述方法の破壊的変更がありました。2.0.1 では修正されているため、アップデートすることをオススメします。

v2 の Kotlin DSL での設定は以下の通りです。

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

何か質問などのある方は Issue を利用してください。

# ChangeLog

[CHANGELOG.md](./CHANGELOG.md) はこちらから参照してください。

# License

Copyright 2015-2019 DeployGate Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
