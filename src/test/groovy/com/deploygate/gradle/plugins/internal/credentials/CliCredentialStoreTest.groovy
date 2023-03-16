package com.deploygate.gradle.plugins.internal.credentials

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CliCredentialStoreTest extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    def "load the file content on initialization"() {
        setup:
        def file1 = new File(testProjectDir.newFolder("test1"), "credentials")
        file1.write('{"name":"name","token":"token"}')

        when:
        def store1 = new CliCredentialStore(file1.parentFile)

        then:
        store1.name == "name"
        store1.token == "token"

        when:
        def file2 = new File(testProjectDir.newFolder("test2"), "credentials")

        and:
        def store2 = new CliCredentialStore(file2.parentFile)

        then:
        !store2.name
        !store2.token
    }

    def "save the file and it must be readable"() {
        setup:
        def baseDir = testProjectDir.newFolder("base")

        when:
        def store = new CliCredentialStore(baseDir)
        store.name = "name"
        store.token = "token"

        and:
        store.save()
        def reloadedStore = new CliCredentialStore(baseDir)

        then:
        reloadedStore.name == "name"
        reloadedStore.token == "token"
    }

    def "load refreshes the state"() {
        setup:
        def baseDir = testProjectDir.newFolder("base")

        when:
        def store1 = new CliCredentialStore(baseDir)
        def store2 = new CliCredentialStore(baseDir)
        store1.name = "name"
        store1.token = "token"

        then:
        !store2.name
        !store2.token

        when:
        store1.save()
        store2.load()

        then:
        store2.name == "name"
        store2.token == "token"
    }
}
