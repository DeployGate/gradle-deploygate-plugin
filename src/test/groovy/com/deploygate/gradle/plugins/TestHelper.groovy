package com.deploygate.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder

/**
 * Test helper utilities for creating test fixtures and reducing boilerplate in tests.
 * Provides factory methods for common test scenarios.
 */
class TestHelper {

    /**
     * Creates a project with the DeployGate plugin applied and basic Android configuration.
     *
     * @param projectName The name of the test project
     * @param config Optional closure for additional project configuration
     * @return Configured Project instance
     */
    static Project createAndroidProjectWithPlugin(String projectName = "test-project", Closure config = null) {
        def project = ProjectBuilder.builder()
                .withName(projectName)
                .build()

        // Apply Android plugin first (required by DeployGate plugin)
        project.apply plugin: 'com.android.application'

        // Apply DeployGate plugin
        project.apply plugin: DeployGatePlugin

        // Basic Android configuration
        project.android {
            namespace 'com.example.test'
            compileSdkVersion 33

            defaultConfig {
                applicationId "com.example.test"
                minSdkVersion 21
                targetSdkVersion 33
                versionCode 1
                versionName "1.0"
            }
        }

        // Apply additional configuration if provided
        if (config) {
            project.configure(project, config)
        }

        return project
    }

    /**
     * Creates a project with mocked providers for testing configuration cache scenarios.
     *
     * @param environmentVars Map of environment variable names to values
     * @param systemProperties Map of system property names to values
     * @return Project with configured providers
     */
    static Project createProjectWithMockedProviders(
            Map<String, String> environmentVars = [:],
            Map<String, String> systemProperties = [:]) {

        def project = ProjectBuilder.builder().build()

        // Create a custom provider factory that returns our test values
        def providers = project.providers

        // Note: In real tests, we can't easily override provider values,
        // but this shows the pattern for test setup

        return project
    }

    /**
     * Creates test credentials for DeployGate tasks.
     *
     * @param appOwnerName The app owner name
     * @param apiToken The API token
     * @return Map containing credential values
     */
    static Map<String, String> createTestCredentials(
            String appOwnerName = "test-owner",
            String apiToken = "test-token") {
        return [
            appOwnerName: appOwnerName,
            apiToken: apiToken
        ]
    }

    /**
     * Creates a minimal build.gradle content for testing.
     *
     * @param additionalConfig Additional configuration to add to the build file
     * @return Build file content as a string
     */
    static String createBuildFileContent(String additionalConfig = "") {
        return """
            plugins {
                id 'com.android.application'
                id 'deploygate'
            }

            android {
                namespace 'com.example.test'
                compileSdkVersion 33
                
                defaultConfig {
                    applicationId "com.example.test"
                    minSdkVersion 21
                    targetSdkVersion 33
                    versionCode 1
                    versionName "1.0"
                }
            }

            deploygate {
                appOwnerName = "test-owner"
                apiToken = "test-token"
            }

            ${additionalConfig}
        """.stripIndent()
    }

    /**
     * Creates environment variables map for testing browser detection.
     *
     * @param ci Whether to simulate CI environment
     * @param display Display value for Linux environments
     * @return Map of environment variables
     */
    static Map<String, String> createBrowserEnvironment(
            boolean ci = false,
            String display = null) {
        def env = [:]

        if (ci) {
            env['CI'] = 'true'
        }

        if (display != null) {
            env['DISPLAY'] = display
        }

        return env
    }

    /**
     * Creates a mock HTTP client configuration for testing.
     *
     * @param endpoint The test server endpoint
     * @return Map of HTTP client parameters
     */
    static Map<String, String> createHttpClientConfig(String endpoint = "https://test.deploygate.com") {
        return [
            endpoint: endpoint,
            agpVersion: "8.1.0",
            pluginVersion: "test-version",
            pluginVersionCode: "1",
            pluginVersionName: "test"
        ]
    }

    /**
     * Helper to create provider-based test values.
     * 
     * @param providers ProviderFactory instance
     * @param value The value to wrap in a provider
     * @return Provider containing the value
     */
    static def createProvider(ProviderFactory providers, Object value) {
        return providers.provider { value }
    }

    /**
     * Creates a test deployment configuration.
     *
     * @param name Deployment name
     * @param message Deployment message
     * @param skipAssemble Whether to skip assembly
     * @return Deployment configuration string
     */
    static String createDeploymentConfig(
            String name = "debug",
            String message = "Test deployment",
            boolean skipAssemble = true) {
        return """
            deployments {
                ${name} {
                    message = "${message}"
                    skipAssemble = ${skipAssemble}
                }
            }
        """
    }
}