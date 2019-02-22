package com.deploygate.gradle.plugins

class TestDeployGatePlugin {
    List<File> loadPluginClasspath() {
        def pluginClasspathResource = getClass().classLoader.getResource("plugin-classpath.txt")

        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `createClasspathManifest` gradle task.")
        }

        return pluginClasspathResource.readLines().collect { new File(it) }
    }
}
