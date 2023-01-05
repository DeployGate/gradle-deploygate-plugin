package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.credentials.CliCredentialStore
import com.deploygate.gradle.plugins.utils.BrowserUtils
import com.deploygate.gradle.plugins.utils.UrlUtils
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class LoginTask extends DefaultTask {
    @Internal def port = 0

    @Internal CountDownLatch latch
    boolean saved
    @Internal CliCredentialStore localCredential

    @TaskAction
    def setup() {
        localCredential = new CliCredentialStore()
        if (!hasCredential())
            if (!setupCredential())
                throw new RuntimeException('Failed to retrieve DeployGate credentials. Please try again or specify it in your build.gradle script.')

        project.deploygate.appOwnerName =
                [project.deploygate.appOwnerName, System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME), System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME_V1), localCredential.name].find {
                    it != null
                }
        project.deploygate.apiToken =
                [project.deploygate.apiToken, System.getenv(DeployGatePlugin.ENV_NAME_API_TOKEN), localCredential.token].find {
                    it != null
                }
    }

    boolean hasCredential() {
        hasCreadentialInScript() || hasSavedCredential() || hasCredentialInEnv()
    }

    boolean hasCredentialInEnv() {
        [System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME), System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME_V1)].any() &&
                [System.getenv(DeployGatePlugin.ENV_NAME_API_TOKEN)].any()
    }

    // From Gradle 7.0, only one method to get boolean property value is allowed
    // whereas Groovy generates both isSaved() and getSaved(), Gradle 7.0 considers 2 getters exist for saved property.
    // To suppress getSaved() we explicitly define isSaved().
    private boolean isSaved() {
        saved
    }

    private boolean hasSavedCredential() {
        localCredential.name && localCredential.token
    }

    private boolean hasCreadentialInScript() {
        project.deploygate.appOwnerName && project.deploygate.apiToken
    }

    def setupCredential() {
        saved = false
        if (BrowserUtils.hasBrowser()) {
            setupBrowser()
        } else {
            setupTerminal()
        }
    }

    def setupTerminal() {
        // @TODO implement
        false
    }

    def setupBrowser() {
        def server
        try {
            server = startLocalServer()
            openBrowser()
            waitForResponse()
            if (saved) {
                println "Welcome ${localCredential.name}!"
                return true
            }
        } catch (e) {
            logger.error("Failed to log in with browser: " + e.message)
        } finally {
            if (server) {
                server.stop(1)
            }
        }
    }

    boolean openBrowser() {
        def url = "${project.deploygate.endpoint}/cli/login?port=${port}&client=gradle"
        if (BrowserUtils.openBrowser(url))
            return true
        logger.warn 'Could not open a browser on current environment.'
        println 'Please log in to DeployGate by opening the following URL on your browser:'
        println url
        false
    }

    def startLocalServer() {
        def address = new InetSocketAddress("localhost", 0)
        def httpServer = HttpServer.create(address, 0)
        port = httpServer.address.port

        httpServer.createContext "/token", { HttpExchange httpExchange ->
            def query = UrlUtils.parseQueryString(httpExchange.requestURI.query)
            project.deploygate.notifyKey = query.key
            httpExchange.sendResponseHeaders(204, -1)
            httpExchange.close()

            if (!query.containsKey('cancel')) {
                retrieveCredentialFromKey(query.key)
                project.deploygate.notifyServer 'credential_saved'
            }

            latch.countDown()
        }
        httpServer.start()
        httpServer
    }


    def waitForResponse() {
        def timeout = 180 * 1000
        def start = System.currentTimeMillis()

        latch = new CountDownLatch(1)
        while (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            print "."
            if (System.currentTimeMillis() - start > timeout)
                throw new TimeoutException('Timeout while waiting for browser response')
        }
    }

    boolean retrieveCredentialFromKey(String key) {
        def jsonStr = getCredentialJsonFromKey(key)
        if (jsonStr) {
            if (localCredential.saveLocalCredentialFile(jsonStr)) {
                localCredential.load()
                saved = true
            }
        }
    }

    String getCredentialJsonFromKey(key) {
        try {
//            HTTPBuilderFactory.restClient(project.deploygate.endpoint).
//                    get(path: '/cli/credential', query: [key: key], contentType: ContentType.TEXT).data.text
        } catch (e) {
            logger.error('failed to retrieve credential: ' + e.message)
            return null
        }
    }
}
