package com.deploygate.gradle.plugins.tasks;

import com.deploygate.gradle.plugins.DeployGatePlugin;
import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore;
import com.deploygate.gradle.plugins.internal.http.HttpClient;
import com.deploygate.gradle.plugins.internal.http.LocalServer;
import com.deploygate.gradle.plugins.internal.utils.StringUtils;
import com.deploygate.gradle.plugins.tasks.inputs.Credentials;
import com.deploygate.gradle.plugins.internal.utils.BrowserUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.deploygate.gradle.plugins.internal.gradle.PropertyUtils.setIfAbsent;
import static com.deploygate.gradle.plugins.internal.gradle.ProviderFactoryUtils.environmentVariable;

/**
 * Provide the *enough* credentials to the next tasks. The credentials may cause Unauthorized error.
 */
public abstract class LoginTask extends DefaultTask {

    @Input
    @Optional
    private final Provider<String> appOwnerName;

    @Input
    @Optional
    private final Provider<String> apiToken;

    @InputDirectory
    @Optional
    private final Supplier<@Nullable File> credentialsDirectoryProvider = () -> {
        // workaround of OptionalInputFile: https://github.com/gradle/gradle/issues/2016
        String path = getCredentialsDirPath().getOrNull();

        if (path == null || path.isEmpty()) {
            return null;
        }

        File f = new File(path);
        return f.exists() && f.isDirectory() ? f : null;
    };

    @Internal
    @NotNull
    public final Credentials credentials;

    @Inject
    LoginTask(@NotNull ObjectFactory objectFactory, @NotNull ProviderFactory providerFactory) {
        //noinspection unchecked
        appOwnerName = getExplicitAppOwnerName().orElse(environmentVariable(providerFactory, DeployGatePlugin.getENV_NAME_APP_OWNER_NAME(), DeployGatePlugin.getENV_NAME_APP_OWNER_NAME_V1()));

        //noinspection unchecked
        apiToken = getExplicitApiToken().orElse(
                environmentVariable(providerFactory, DeployGatePlugin.getENV_NAME_API_TOKEN())
        );

        credentials = objectFactory.newInstance(Credentials.class);

        setDescription("Check the configured credentials and launch the authentication flow if they are not enough.");
        setGroup(Constants.TASK_GROUP_NAME);
    }

    @Internal
    @Override
    public String getDescription() {
        return "Load the credentials that shared between the plugin and DeployGate's cli";
    }

    @Input
    @Optional
    @NotNull
    public abstract Property<String> getExplicitAppOwnerName();

    @Input
    @Optional
    @NotNull
    public abstract Property<String> getExplicitApiToken();

    /**
     * A path to a directory that may contain a credential file.
     *
     * @return a directory path property
     */
    @Input
    @NotNull
    public abstract Property<String> getCredentialsDirPath();

    @Internal
    @NotNull
    public abstract Property<HttpClient> getHttpClient();

    @Internal
    @NotNull
    public abstract Property<LocalServer> getLocalServer();

    @TaskAction
    public void execute() {
        credentials.getAppOwnerName().set(appOwnerName);
        credentials.getApiToken().set(apiToken);

        if (credentials.isPresent()) {
            getLogger().info("Don't need to read a credential file.");
            return;
        }

        File credentialsDir = credentialsDirectoryProvider.get();

        if (credentialsDir != null) {
            CliCredentialStore store = new CliCredentialStore(credentialsDir);

            if (StringUtils.isNullOrBlank(store.getName()) || StringUtils.isNullOrBlank(store.getToken())) {
                getLogger().info("A local credential file does not contain app owner name and token.");
            } else {
                setIfAbsent(credentials.getAppOwnerName(), store.getName());
                setIfAbsent(credentials.getApiToken(), store.getToken());
                getLogger().info("Set the credentials from the local file.");
            }
        } else {
            getLogger().info("No credential file is found on local system.");

            credentialsDir = new File(getCredentialsDirPath().get());
            if (!credentialsDir.mkdirs()) {
                throw new GradleException("Cannot create a directory on this local system.");
            }
        }

        if (credentials.isPresent()) {
            getLogger().info("Credentials are read from the file.");
            return;
        }

        if (!setupCredential()) {
            getLogger().error("We could not retrieve DeployGate credentials.");
            getLogger().error("Please make sure you have configured app owner name and api token or any browser application is available to launch the authentication flow.");

            throw new RuntimeException("Could not fetch the credentials");
        }

        CliCredentialStore store = new CliCredentialStore(credentialsDir);

        System.out.printf(Locale.US, "Welcome %s!%n", store.getName());

        getLogger().info("The authentication has succeeded. The application owner name is {}.", store.getName());

        // We can set the values unless they are found because of the idempotency.
        credentials.getAppOwnerName().set(store.getName());
        credentials.getApiToken().set(store.getToken());
    }

    /**
     * Launch the authentication flow and fetch the credentials if possible
     *
     * @return true if the credentials are persisted, otherwise false.
     */
    @VisibleForTesting
    boolean setupCredential() {
        if (BrowserUtils.hasBrowser()) {
            return setupBrowser();
        } else {
            return setupTerminal();
        }
    }

    private boolean setupTerminal() {
        getLogger().error("The authentication flow within the terminal is not supported yet.");
        // @TODO implement the terminal authentication flow
        return false;
    }

    /**
     * Launch the authentication flow by using a browser application.
     */
    private boolean setupBrowser() {
        LocalServer server = getLocalServer().get();
        int port = server.start();

        if (port > 0) {
            openBrowser(port);
        }

        try {
            return server.await();
        } catch (InterruptedException | TimeoutException e) {
            getLogger().error("Failed to log in with browser: {}", e.getMessage(), e);
            return false;
        }
    }

    private void openBrowser(int port) {
        Map<String, String> params = new HashMap<>();
        params.put("port", String.valueOf(port));
        params.put("client", "gradle");

        String url = getHttpClient().get().buildURI(params, "cli", "login").toString();

        if (!BrowserUtils.openBrowser(url)) {
            getLogger().error("Could not open a browser on current environment.");
            System.out.println("Please log in to DeployGate by opening the following URL on your browser:");
            System.out.println(url);
        }
    }
}
