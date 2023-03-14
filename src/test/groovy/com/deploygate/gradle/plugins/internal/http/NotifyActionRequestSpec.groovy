package com.deploygate.gradle.plugins.internal.http

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import spock.lang.Specification

class NotifyActionRequestSpec extends Specification {
    def "toEntity should build an entity successfully"() {
        given:
        def request = new NotifyActionRequest("action")

        when:
        def entity = request.toEntity("notifyKey") // do not throw an exception

        then:
        entity instanceof UrlEncodedFormEntity
    }

    def "buildNameValuePairs should contain parameters"() {
        given:
        def notifyKey = "notifyKey"
        def action = "action"

        def request = new NotifyActionRequest(action)

        when:
        def pairs = request.buildNameValuePairs(notifyKey) // do not throw an exception

        then:
        pairs.any { it.name == "key" && it.value == notifyKey }
        pairs.any { it.name == "command_action" && it.value == action }

        when:
        request = new NotifyActionRequest(action)
        request.setParameter("param1", "value1")
        request.setParameter("param2", null)
        pairs = request.buildNameValuePairs(notifyKey)

        then:
        pairs.any { it.name == "param1" && it.value == "value1" }
        pairs.any { it.name == "param2" && it.value == "" }
    }
}
