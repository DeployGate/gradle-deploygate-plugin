package com.deploygate.gradle.plugins.tasks

import com.deploygate.gradle.plugins.Config
import com.deploygate.gradle.plugins.auth.DeployGateLocalCredential
import com.deploygate.gradle.plugins.utils.UrlUtils
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.awt.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class DeployGateSetupCredentialTask extends DefaultTask {
    def port = 0

    CountDownLatch latch
    boolean saved
    DeployGateLocalCredential localCredential

    @TaskAction
    def setup() {
        localCredential = new DeployGateLocalCredential()
        if (!hasCredential())
            if (!setupCredential())
                throw new RuntimeException('Failed to retrieve DeployGate credentials. Please try again or specify it in your build.gradle script.')

        if (!project.deploygate.userName)
            project.deploygate.userName = localCredential.name
        if (!project.deploygate.token)
            project.deploygate.token = localCredential.token
    }

    boolean hasCredential() {
        hasCreadentialInScript() || hasSavedCredential()
    }

    private boolean hasSavedCredential() {
        localCredential.name && localCredential.token
    }

    private boolean hasCreadentialInScript() {
        project.deploygate.userName && project.deploygate.token
    }

    def setupCredential() {
        saved = false
        if (Desktop.isDesktopSupported()) {
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
            if (Desktop.isDesktopSupported()) {
                def url = "${Config.DEPLOYGATE_HOST}/cli/login?port=${port}"
                try {
                    Desktop.getDesktop().browse(URI.create(url))
                } catch (e) {
                    println 'Please log in to DeployGate by opening the following URL on your browser:'
                    println url
                }
            }
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

    def startLocalServer() {
        def address = new InetSocketAddress("localhost", 0)
        def httpServer = HttpServer.create(address, 0)
        port = httpServer.address.port

        httpServer.createContext "/token", new HttpHandler() {
            @Override
            void handle(HttpExchange httpExchange) throws IOException {
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
        def json = getCredentialJsonFromKey(key)
        if (json) {
            if (localCredential.saveLocalCredentialFile(json)) {
                localCredential.load()
                saved = true
            }
        }
    }

    def getCredentialJsonFromKey(key) {
        try {
            new URL("${Config.DEPLOYGATE_HOST}/cli/credential?key=${key}").getText()
        } catch (e) {
            logger.error('failed to retrieve credential: ' + e.message)
            return null
        }
    }
}
