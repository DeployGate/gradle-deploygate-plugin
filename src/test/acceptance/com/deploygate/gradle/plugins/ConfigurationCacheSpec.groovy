package com.deploygate.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests to verify the plugin's compatibility with Gradle's configuration cache feature.
 * These tests ensure that the plugin can be used with --configuration-cache flag
 * without any configuration cache problems.
 */
class ConfigurationCacheSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    File buildFile
    File settingsFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        settingsFile = testProjectDir.newFile('settings.gradle')
        def localPropertiesFile = testProjectDir.newFile('local.properties')

        settingsFile << '''
            rootProject.name = 'test-project'
        '''

        // Set Android SDK location
        def androidHome = System.getenv("ANDROID_HOME") ?: "${System.getProperty('user.home')}/Android/Sdk"
        localPropertiesFile << "sdk.dir=${androidHome}"
    }

    /**
     * Creates the plugin classpath for GradleRunner.
     * This method loads the plugin classpath from the test resources.
     */
    private List<File> createPluginClasspath() {
        def pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")

        if (pluginClasspathResource == null) {
            throw new IllegalStateException(
            "Did not find plugin classpath resource, run `createClasspathManifest` gradle task.")
        }

        return pluginClasspathResource.readLines().collect { new File(it) }
    }

    @Unroll
    def "plugin supports configuration cache with #taskName task"() {
        given: "A project with the DeployGate plugin applied"
        def agpVersion = System.getenv("TEST_AGP_VERSION") ?: "8.7.0"
        // Add Android plugin repository
        buildFile << """
            buildscript {
                repositories {
                    google()
                    mavenCentral()
                }
                dependencies {
                    classpath 'com.android.tools.build:gradle:${agpVersion}'
                    classpath files(${createPluginClasspath().collect { "'${it.absolutePath}'"
            }.join(', ')
        })
                }
            }

            apply plugin: 'com.android.application'
            apply plugin: 'deploygate'

            repositories {
                google()
                mavenCentral()
            }

            android {
                namespace = 'com.example.test'
                compileSdk = 30

                defaultConfig {
                    applicationId = "com.example.test"
                    minSdk = 21
                    targetSdk = 30
                    versionCode = 1
                    versionName = "1.0"
                }

                buildTypes {
                    release {
                        minifyEnabled = false
                    }
                }
            }

            deploygate {
                appOwnerName = "test-owner"
                apiToken = "test-token"
                
                deployments {
                    debug {
                        skipAssemble = true
                    }
                    release {
                        skipAssemble = true
                    }
                }
            }
        """

when: "Running the task with configuration cache enabled"
def gradleVersion = System.getenv("TEST_GRADLE_VERSION")
def result = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withPluginClasspath(createPluginClasspath())
        .withArguments('--configuration-cache', taskName, '--dry-run')
        .build()

then: "The task runs successfully without configuration cache problems"
result.output.contains('Configuration cache entry stored')
!result.output.contains('Configuration cache problems found')

when: "Running the same task again to reuse the configuration cache"
def cachedResult = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(gradleVersion)
        .withPluginClasspath(createPluginClasspath())
        .withArguments('--configuration-cache', taskName, '--dry-run')
        .build()

then: "The configuration cache is reused successfully"
cachedResult.output.contains('Configuration cache entry reused')
!cachedResult.output.contains('Configuration cache problems found')

where:
taskName << [
    'loginDeployGate',
    'logoutDeployGate',
    'uploadDeployGateDebug',
    'uploadDeployGateRelease'
]
}

def "plugin properly handles environment variables with configuration cache"() {
given: "A project using environment variables"
buildFile << androidProject("""
            deploygate {
                // These will be read from environment variables
            }
        """)

and: "Environment variables are set"
def env = [
    'DEPLOYGATE_APP_OWNER_NAME': 'env-owner',
    'DEPLOYGATE_API_TOKEN': 'env-token',
    'DEPLOYGATE_OPEN_BROWSER': 'false'
]

when: "Running with configuration cache"
def result = configurationCacheRunner('loginDeployGate')
        .withEnvironment(env)
        .build()

then: "Environment variables are properly handled"
result.output.contains('Configuration cache entry stored')
!result.output.contains('Configuration cache problems found')
}

def "plugin BuildServices work correctly with configuration cache"() {
given: "A project that uses HttpClient BuildService"
buildFile << androidProject("""
            deploygate {
                appOwnerName = "test-owner"
                apiToken = "test-token"
            }

            tasks.register('testBuildService') {
                doLast {
                    println "BuildService test task executed"
                }
                dependsOn 'loginDeployGate'
            }
        """)

when: "Running custom task with configuration cache"
def result = configurationCacheRunner('testBuildService').build()

then: "BuildServices are properly registered and reused"
result.output.contains('Configuration cache entry stored')
!result.output.contains('Configuration cache problems found')
}

def "provider chains work correctly with configuration cache"() {
given: "A project with custom deployments"
buildFile << androidProject("""
            deploygate {
                appOwnerName = "test-owner"
                apiToken = "test-token"

                deployments {
                    customRelease {
                        message = "Custom release build"
                        skipAssemble = false
                        distribution {
                            key = "dist-key"
                            releaseNote = "Release notes"
                        }
                    }
                }
            }
        """)

when: "Running deployment task with configuration cache"
def result = configurationCacheRunner('uploadDeployGateCustomRelease').build()

then: "Complex provider chains are handled correctly"
result.output.contains('Configuration cache entry stored')
!result.output.contains('Configuration cache problems found')
}

/**
 * Builds an inline Android application project that applies the deploygate plugin, parameterized by
 * the AGP version under test and using the new DSL so it is valid on AGP 7.0+ (including AGP 9.x).
 * The caller supplies the trailing deploygate {} (and any extra) configuration.
 */
private String androidProject(String trailingConfiguration) {
def agpVersion = System.getenv("TEST_AGP_VERSION") ?: "8.7.0"
def pluginClasspath = createPluginClasspath().collect { "'${it.absolutePath}'" }.join(', ')
return """
            buildscript {
                repositories {
                    google()
                    mavenCentral()
                }
                dependencies {
                    classpath 'com.android.tools.build:gradle:${agpVersion}'
                    classpath files(${pluginClasspath})
                }
            }

            apply plugin: 'com.android.application'
            apply plugin: 'deploygate'

            repositories {
                google()
                mavenCentral()
            }

            android {
                namespace = 'com.example.test'
                compileSdk = 30

                defaultConfig {
                    applicationId = "com.example.test"
                    minSdk = 21
                    targetSdk = 30
                    versionCode = 1
                    versionName = "1.0"
                }

                buildTypes {
                    release {
                        minifyEnabled = false
                    }
                }
            }
${trailingConfiguration}
        """
}

private GradleRunner configurationCacheRunner(String taskName) {
return GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withGradleVersion(System.getenv("TEST_GRADLE_VERSION"))
        .withPluginClasspath(createPluginClasspath())
        .withArguments('--configuration-cache', taskName, '--dry-run')
}
}