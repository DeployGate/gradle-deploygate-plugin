package com.deploygate.gradle.plugins.dsl.syntax

import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.annotations.NotNull

interface ExtensionSyntax {
    /**
     * Set the api token.
     *
     * @param apiToken
     * @see com.deploygate.gradle.plugins.DeployGatePlugin#ENV_NAME_API_TOKEN
     */
    void setApiToken(@NotNull String apiToken)

    /**
     * Set the application owner name.
     *
     * @param appOwnerName
     * @see com.deploygate.gradle.plugins.DeployGatePlugin#ENV_NAME_APP_OWNER_NAME
     */
    void setAppOwnerName(@NotNull String appOwnerName)

    /**
     * Define deployments for each product flavors.
     *
     * @param closure
     */
    void deployments(Closure closure) // Groovy

    /**
     * Define deployments for each product flavors.
     */
    @NotNull
    NamedDomainObjectContainer<NamedDeployment> getDeployments() // for Kotlin DSL
}
