package com.deploygate.gradle.plugins

import org.junit.rules.ExternalResource

class TestSystemEnv extends ExternalResource {
    private Map<String, Object> envStub = new HashMap<String, Object>()

    private def realEnv = System.getenv()

    @Override
    protected void before() throws Throwable {
        System.metaClass.static.getenv = { String name ->
            if (envStub.containsKey(name)) {
                envStub[name] as String
            } else {
                realEnv[name]
            }
        }
    }

    @Override
    protected void after() {
        System.metaClass.static.getenv = { String name ->
            realEnv[name]
        }
    }

    def setEnv(Map<String, Object> env) {
        envStub.clear()
        envStub.putAll(env)
    }
}
