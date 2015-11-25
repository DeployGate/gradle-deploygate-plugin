package com.deploygate.gradle.plugins

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.awt.Desktop
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Created by tnj on 11/25/15.
 */
class DeployGateSetupCredentialTask extends DefaultTask {
    static def LOCAL_PORT = 64126

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
                def url = "${Config.DEPLOYGATE_HOST}/cli/login?port=${LOCAL_PORT}"
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
        def httpServer = HttpServer.create(new InetSocketAddress("localhost", LOCAL_PORT), LOCAL_PORT)
        httpServer.createContext "/token", new HttpHandler() {
            @Override
            void handle(HttpExchange httpExchange) throws IOException {
                def query = parseQueryString(httpExchange.requestURI.query)
                if (!query.containsKey('cancel')) {
                    httpExchange.getResponseHeaders().add("Location", "${Config.DEPLOYGATE_HOST}/cli/setup_progress?token=${query.token}")
                    httpExchange.sendResponseHeaders(302, 0)
                    httpExchange.close()

                    retrieveCredentialFromToken(query.token)
                } else {
                    httpExchange.getResponseHeaders().add("Location", "${Config.DEPLOYGATE_HOST}/cli/setup_cancelled")
                    httpExchange.sendResponseHeaders(302, 0)
                    httpExchange.close()
                }
                latch.countDown()
            }
        }
        httpServer.start()
        httpServer
    }

    def waitForResponse() {
        latch = new CountDownLatch(1)
        while (!latch.await(5000, TimeUnit.MILLISECONDS)) {
        }
    }

    boolean retrieveCredentialFromToken(String token) {
        def json = getCredentialJsonFromToken(token)
        if (json) {
            if (localCredential.saveLocalCredentialFile(json)) {
                localCredential.load()
                saved = true
            }
        }
    }

    def getCredentialJsonFromToken(token) {
        try {
            new URL("${Config.DEPLOYGATE_HOST}/cli/credential?token=${token}").getText()
        } catch (e) {
            logger.error('failed to retrieve credential: ' + e.message)
            return null
        }
    }

    static def parseQueryString(String s) {
        (s ?: "").split("&").inject([:]) { LinkedHashMap<String, String> m, String str ->
            def (k, v) = str.split("=", 2).collect { URLDecoder.decode(it, 'UTF-8') }
            m.put k, v
            m
        } as LinkedHashMap<String, String>
    }
}
