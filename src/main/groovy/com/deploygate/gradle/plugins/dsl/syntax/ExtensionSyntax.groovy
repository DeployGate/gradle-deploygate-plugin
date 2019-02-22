package com.deploygate.gradle.plugins.dsl.syntax

import com.deploygate.gradle.plugins.dsl.NamedDeployment
import org.gradle.api.NamedDomainObjectContainer

import javax.annotation.Nonnull

interface ExtensionSyntax {
    void setApiToken(@Nonnull String apiToken)

    void setAppOwnerName(@Nonnull String appOwnerName)

    void deployments(Closure closure)

    @Nonnull
    NamedDomainObjectContainer<NamedDeployment> getDeployments()
}
