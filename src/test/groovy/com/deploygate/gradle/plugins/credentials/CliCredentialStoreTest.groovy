package com.deploygate.gradle.plugins.credentials

import groovy.mock.interceptor.StubFor
import org.junit.Test

class CliCredentialStoreTest {
    @Test
    void loadCredentialTest() {
        def file = new StubFor(File)
        file.demand.with {
            getText(1) {
                '{"name":"test","token":"testtest"}'
            }
            exists(1) {
                true
            }
        }
        file.use {
            def store = new CliCredentialStore()
            assert store.name == "test"
            assert store.token == "testtest"
        }
        file.expect.verify()
    }

    @Test
    void emptyCredentialTest() {
        def file = new StubFor(File)
        file.demand.with {
            exists(1) {
                false
            }
        }
        file.use {
            def store = new CliCredentialStore()
            assert store.name == null
            assert store.token == null
        }
        file.expect.verify()
    }

    @Test
    void saveCredentialTest() {
        def file = new StubFor(File)
        file.demand.with {
            // load()
            exists { false }
            // ensureDirectoryWritable()
            exists { true }
            // localCredentialFile.exists()
            exists { false }
            write { str, encoding ->
                assert str == '{"name":"test","token":"testtest"}'
            }
        }
        file.use {
            def store = new CliCredentialStore()
            store.name = "test"
            store.token = "testtest"
            store.save()
        }
        file.expect.verify()
    }
}
