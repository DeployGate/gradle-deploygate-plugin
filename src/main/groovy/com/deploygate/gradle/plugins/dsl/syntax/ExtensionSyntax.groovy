package com.deploygate.gradle.plugins.dsl.syntax

import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.NamedDomainObjectContainer

import javax.annotation.Nonnull

interface ExtensionSyntax {
    /**
     * Set the api token.
     *
     * @param apiToken
     * @see com.deploygate.gradle.plugins.DeployGatePlugin#ENV_NAME_API_TOKEN
     */
    void setApiToken(@Nonnull String apiToken)

    /**
     * Set the application owner name.
     *
     * @param appOwnerName
     * @see com.deploygate.gradle.plugins.DeployGatePlugin#ENV_NAME_APP_OWNER_NAME
     */
    void setAppOwnerName(@Nonnull String appOwnerName)

    /**
     * Define deployments for each product flavors.
     *
     * @param closure
     */
    void deployments(Closure closure) // Groovy

    /**
     * Define deployments for each product flavors.
     */
    @Nonnull
    NamedDomainObjectContainer<NamedDeployment> getDeployments() // for Kotlin DSL
}
