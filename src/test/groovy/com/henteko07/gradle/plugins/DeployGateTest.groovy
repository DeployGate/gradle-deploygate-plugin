package com.deploygate.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

class SampleTest {
    @Test
    public void sampleTask() {
        Project target = ProjectBuilder.builder().build()
        target.apply plugin: 'deploygate'

        assertTrue(target.tasks.uploadDeployGate instanceof DeployGateTask)
    }
}
