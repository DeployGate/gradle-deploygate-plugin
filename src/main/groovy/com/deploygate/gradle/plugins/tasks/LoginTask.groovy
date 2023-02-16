package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.dsl.DeployGateExtension
import com.deploygate.gradle.plugins.internal.http.ApiClient
import com.deploygate.gradle.plugins.internal.http.GetCredentialsResponse
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.deploygate.gradle.plugins.utils.UrlUtils
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LoginTask extends DefaultTask {

    @Internal
    DeployGateExtension deployGateExtension
    
    @Internal
    CliCredentialStore credentialStore

    private CountDownLatch latch

    @Nullable
    private String onetimeKey

    @TaskAction
    def setup() {
        if (!(deployGateExtension.appOwnerName && deployGateExtension.apiToken)) {
            if (!setupCredential()) {
                throw new RuntimeException('We could not retrieve DeployGate credentials. Please make sure you have configured app owner name and api token or any browser application is available to launch the authentication flow.')
            }

            println "Welcome ${credentialStore.name}!"

            logger.info("The authentication has succeeded. The application owner name is ${credentialStore.name}.")

            // We should be carefully handling these values to avoid the inconsistent credentials and non-idempotency.
            if (deployGateExtension.appOwnerName && deployGateExtension.appOwnerName != credentialStore.name) {
                logger.error("Another application owner name (${deployGateExtension.appOwnerName}) has already been configured")
                throw new GradleException("The authentication has succeeded but the configured application owner name conflicts with the credentials that currently retrieved.")
            } else if (deployGateExtension.apiToken && deployGateExtension.apiToken != credentialStore.token) {
                throw new GradleException("The authentication has succeeded but the configured api token doesn't match with the credentials that currently retrieved")
            }

            deployGateExtension.setAppOwnerName(credentialStore.name)
            deployGateExtension.setApiToken(credentialStore.token)
        }
    }

    /**
     * Launch the authentication flow and fetch the credentials if possible
     * @return true if the credentials are persisted, otherwise false.
     */
    private boolean setupCredential() {
        if (BrowserUtils.hasBrowser()) {
            setupBrowser()
        } else {
            setupTerminal()
        }

        if (!onetimeKey || !retrieveCredentialFromKey(onetimeKey)) {
            return false
        }

        deployGateExtension.notifyKey = onetimeKey
        deployGateExtension.notifyServer('credential_saved')

        return true
    }

    private void setupTerminal() {
        logger.error("The authentication flow within the terminal is not supported yet.")
        // @TODO implement the terminal authentication flow
        false
    }

    /**
     * Launch the authentication flow by using a browser application.
     */
    private void setupBrowser() {
        def server
        try {
            server = startLocalServer()
            openBrowser(server.address.port)
            waitForResponse()
        } catch (e) {
            logger.error("Failed to log in with browser: ${e.message}", e)
        } finally {
            if (server) {
                server.stop(1)
            }
        }
    }

    void openBrowser(int port) {
        def url = "${deployGateExtension.endpoint}/cli/login?port=${port}&client=gradle"

        if (!BrowserUtils.openBrowser(url)) {
            logger.error('Could not open a browser on current environment.')
            println 'Please log in to DeployGate by opening the following URL on your browser:'
            println url
        }
    }

    def startLocalServer() {
        latch = new CountDownLatch(1)

        def address = new InetSocketAddress("localhost", 0)
        def httpServer = HttpServer.create(address, 0)

        httpServer.createContext "/token", { HttpExchange httpExchange ->
            try {
                httpExchange.sendResponseHeaders(204, -1)
                httpExchange.close()

                def query = UrlUtils.parseQueryString(httpExchange.requestURI.query)

                if (!query.containsKey('cancel')) {
                    onetimeKey = query.key
                }
            } finally {
                latch.countDown()
            }
        }
        httpServer.start()
        httpServer
    }


    def waitForResponse() {
        def timeout = 180 * 1000
        def start = System.currentTimeMillis()

        while (!latch.await(5, TimeUnit.SECONDS)) {
            print "."

            if (System.currentTimeMillis() - start > timeout) {
                throw new TimeoutException('Timeout while waiting for browser response')
            }
        }
    }

    boolean retrieveCredentialFromKey(String key) {
        ApiClient.Response<GetCredentialsResponse> response

        try {
            response = ApiClient.instance.getCredentials(key)
        } catch (Throwable th) {
            logger.error('failed to retrieve credential', th)
            return false
        }

        if (!credentialStore.savecredentialStoreFile(response.rawResponse)) {
            throw new GradleException("failed to save the fetched credentials")
        }

        return credentialStore.load()
    }
}
