package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.internal.http.HttpClient
import com.deploygate.gradle.plugins.tasks.inputs.Credentials
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder

/**
 * Helper class for creating and configuring tasks in tests.
 * Reduces boilerplate code for task setup.
 */
class TaskTestHelper {

    /**
     * Creates a configured UploadApkTask for testing.
     *
     * @param project The project to create the task in
     * @param taskName The name of the task
     * @param config Optional configuration closure
     * @return Configured UploadApkTask
     */
    static UploadApkTask createUploadApkTask(
            Project project,
            String taskName = "testUploadApk",
            Closure config = null) {

        def task = project.tasks.create(taskName, UploadApkTask)

        // Set required properties
        task.credentials.set(createTestCredentials(project))
        task.httpClient.set(createMockHttpClient(project))
        task.endpoint.set(project.providers.provider { "https://test.deploygate.com" })
        task.openBrowserAfterUpload.set(project.providers.provider { false })

        if (config) {
            project.configure(task, config)
        }

        return task
    }

    /**
     * Creates a configured UploadAabTask for testing.
     *
     * @param project The project to create the task in
     * @param taskName The name of the task
     * @param config Optional configuration closure
     * @return Configured UploadAabTask
     */
    static UploadAabTask createUploadAabTask(
            Project project,
            String taskName = "testUploadAab",
            Closure config = null) {

        def task = project.tasks.create(taskName, UploadAabTask)

        // Set required properties
        task.credentials.set(createTestCredentials(project))
        task.httpClient.set(createMockHttpClient(project))
        task.endpoint.set(project.providers.provider { "https://test.deploygate.com" })
        task.openBrowserAfterUpload.set(project.providers.provider { false })

        if (config) {
            project.configure(task, config)
        }

        return task
    }

    /**
     * Creates a configured LoginTask for testing.
     *
     * @param project The project to create the task in
     * @param taskName The name of the task
     * @return Configured LoginTask
     */
    static LoginTask createLoginTask(
            Project project,
            String taskName = "testLogin") {

        def task = project.tasks.create(taskName, LoginTask)

        task.explicitAppOwnerName.set("test-owner")
        task.explicitApiToken.set("test-token")
        task.httpClient.set(createMockHttpClient(project))

        return task
    }

    /**
     * Creates test credentials.
     *
     * @param project The project for object creation
     * @param appOwnerName The app owner name
     * @param apiToken The API token
     * @return Provider of Credentials
     */
    static Provider<Credentials> createTestCredentials(
            Project project,
            String appOwnerName = "test-owner",
            String apiToken = "test-token") {

        def credentials = project.objects.newInstance(Credentials)
        credentials.appOwnerName.set(project.providers.provider { appOwnerName })
        credentials.apiToken.set(project.providers.provider { apiToken })

        return project.providers.provider { credentials }
    }

    /**
     * Creates a mock HttpClient provider for testing.
     *
     * @param project The project for provider creation
     * @return Provider of HttpClient
     */
    static Provider<HttpClient> createMockHttpClient(Project project) {
        // In real tests, this would be properly mocked
        // For now, return a provider that would need to be stubbed
        return project.providers.provider { null as HttpClient }
    }

    /**
     * Creates test input parameters for UploadArtifactTask.
     *
     * @param artifactPath Path to the artifact file
     * @param variantName The variant name
     * @return InputParams instance
     */
    static UploadArtifactTask.InputParams createTestInputParams(
            String artifactPath,
            String variantName = "debug") {

        def params = new UploadArtifactTask.InputParams()
        params.variantName = variantName
        params.isSigningReady = true
        params.isUniversalApk = true
        params.artifactFilePath = artifactPath
        params.message = "Test upload"

        return params
    }

    /**
     * Creates a test artifact file.
     *
     * @param directory The directory to create the file in
     * @param filename The artifact filename
     * @return The created file
     */
    static File createTestArtifact(File directory, String filename = "test.apk") {
        def file = new File(directory, filename)
        file.text = "Test artifact content"
        return file
    }
}