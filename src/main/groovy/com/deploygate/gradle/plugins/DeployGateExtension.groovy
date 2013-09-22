package com.deploygate.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

public class DeployGateExtension {
  final private NamedDomainObjectContainer<ApkTarget> deploygateApks
  String token
  String userName

  public DeployGateExtension(NamedDomainObjectContainer<ApkTarget> apks) {
    deploygateApks = apks
  }

  public apks(Closure closure) {
    deploygateApks.configure(closure)
  }
}
