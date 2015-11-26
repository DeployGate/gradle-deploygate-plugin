package com.deploygate.gradle.plugins

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.awt.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class DeployGateRemoveCredentialTask extends DefaultTask {
    @TaskAction
    def remove() {
        new DeployGateLocalCredential().delete()
    }
}
