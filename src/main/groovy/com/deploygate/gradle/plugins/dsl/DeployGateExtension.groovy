package com.deploygate.gradle.plugins.dsl

import com.deploygate.gradle.plugins.DeployGatePlugin
import com.deploygate.gradle.plugins.dsl.syntax.ExtensionSyntax
import com.deploygate.gradle.plugins.internal.annotation.DeployGateInternal
import com.deploygate.gradle.plugins.internal.credentials.CliCredentialStore
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class DeployGateExtension implements ExtensionSyntax {
    String apiToken

    String appOwnerName

    @Deprecated
    String notifyKey = null

    @NotNull
    private final Project project

    @NotNull
    private final NamedDomainObjectContainer<NamedDeployment> deployments

    @NotNull
    private final CliCredentialStore credentialStore

    DeployGateExtension(@NotNull Project project, @NotNull NamedDomainObjectContainer<NamedDeployment> deployments, @NotNull CliCredentialStore credentialStore) {
        this.project = project
        this.deployments = deployments
        this.credentialStore = credentialStore

        this.appOwnerName = [
            System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME),
            System.getenv(DeployGatePlugin.ENV_NAME_APP_OWNER_NAME_V1),
            credentialStore.getName()
        ].find { it != null }

        this.apiToken = [
            System.getenv(DeployGatePlugin.ENV_NAME_API_TOKEN),
            credentialStore.getToken()
        ].find { it != null }
    }

    // backward compatibility

    @Nullable
    String getToken() {
        return getApiToken()
    }

    @Deprecated
    void setToken(@Nullable String token) {
        setApiToken(token)
    }

    @Nullable
    String getUserName() {
        return getAppOwnerName()
    }

    @Deprecated
    void setUserName(@Nullable String userName) {
        setAppOwnerName(userName)
    }

    /**
     * @depreacted use getDeployments instead
     * @see DeployGateExtension#getDeployments()
     */
    @Deprecated
    NamedDomainObjectContainer<NamedDeployment> getApks() {
        return getDeployments()
    }

    /**
     * @depreacted use deployments instead
     * @see DeployGateExtension#deployments(Closure)
     */
    @Deprecated
    def apks(Closure closure) {
        deployments(closure)
    }

    // end: backward compatibility

    @NotNull
    @Override
    NamedDomainObjectContainer<NamedDeployment> getDeployments() {
        return deployments
    }

    @Override
    void deployments(Closure closure) {
        deployments.configure(closure)
    }

    boolean hasDeployment(@NotNull String name) {
        return deployments.findByName(name)
    }

    @DeployGateInternal
    @NotNull
    @Internal
    CliCredentialStore getCredentialStore() {
        return credentialStore
    }
}
