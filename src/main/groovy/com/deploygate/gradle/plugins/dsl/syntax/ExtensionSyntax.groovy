package com.deploygate.gradle.plugins.dsl.syntax

import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.NamedDomainObjectContainer

import javax.annotation.Nonnull

interface ExtensionSyntax {
    void setApiToken(@Nonnull String apiToken)

    void setAppOwnerName(@Nonnull String appOwnerName)

    /**
     * this is for Groovy configuration
     * @param closure
     */
    void deployments(Closure closure)

    /**
     * this is for Kotlin-DSL
     * @param closure
     */
    @Nonnull
    NamedDomainObjectContainer<NamedDeployment> getDeployments()
}
