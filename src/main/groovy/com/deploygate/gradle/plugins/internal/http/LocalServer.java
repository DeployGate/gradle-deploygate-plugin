package com.deploygate.gradle.plugins.internal.http;

import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore;
import com.deploygate.gradle.plugins.internal.utils.UrlUtils;
import com.sun.net.httpserver.HttpServer;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class LocalServer implements BuildService<LocalServer.Params>, AutoCloseable {

    interface Params extends BuildServiceParameters {
        Property<HttpClient> getHttpClient();

        Property<String> getCredentialsDirPath();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServer.class);

    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final File credentialStoreDir;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<HttpServer> optionalHttpServer;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Inject
    public LocalServer() {
        this.httpClient = getParameters().getHttpClient().get();
        this.credentialStoreDir = new File(getParameters().getCredentialsDirPath().get());

        InetSocketAddress address = new InetSocketAddress("localhost", 0);

        HttpServer s;

        try {
            s = HttpServer.create(address, 0);
        } catch (IOException ignore) {
            s = null;
        }

        optionalHttpServer = Optional.ofNullable(s);
        optionalHttpServer.ifPresent(server -> {
            server.createContext("/token", httpExchange -> {
                httpExchange.sendResponseHeaders(204, -1);
                httpExchange.close();

                try {
                    Map<String, String> params = UrlUtils.parseQueryString(httpExchange.getRequestURI().getQuery());

                    if (!params.containsKey("cancel")) {
                        httpClient.getNotificationKey().set(params.get("key"));
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        });
    }

    public int start() {
        if (!optionalHttpServer.isPresent()) {
            countDownLatch.countDown();
            return -1;
        }

        HttpServer server = optionalHttpServer.get();

        server.start();
        return server.getAddress().getPort();
    }

    @Nullable
    public boolean await() throws InterruptedException, TimeoutException {
        long now = System.currentTimeMillis();
        long until = now + TimeUnit.MINUTES.toMillis(3);

        while (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            LOGGER.info(".");
            if (System.currentTimeMillis() > until) {
                throw new TimeoutException("Timeout while waiting for browser response");
            }
        }

        return fetchAndSaveCredentials();
    }

    @Override
    public void close() throws Exception {
        optionalHttpServer.ifPresent(s -> s.stop(1));
    }

    boolean fetchAndSaveCredentials() {
        try {
            HttpClient.Response<GetCredentialsResponse> response = httpClient.getLifecycleNotificationClient().getCredentials();

            if (response == null) {
                LOGGER.error("could not get a client key for the authentication.");
                return false;
            }

            CliCredentialStore credentialStore = new CliCredentialStore(credentialStoreDir);

            if (!credentialStore.saveLocalCredentialFile(response.rawResponse)) {
                throw new GradleException("failed to save the fetched credentials");
            }

            this.httpClient.getLifecycleNotificationClient().notifyOnCredentialSaved();
            return true;
        } catch (Throwable th) {
            LOGGER.error("failed to retrieve credential", th);
            return false;
        }
    }
}
