package com.deploygate.gradle.plugins.internal.http

import spock.lang.Specification

class UploadAppResponseSpec extends Specification {
    def "deserialize a json response"() {
        given:
        def jsonStr = """
{
  "error": false,
  "results": {
    "name": "com.deploygate.example",
    "package_name": "com.deploygate.example",
    "labels": {},
    "os_name": "Android",
    "path": "/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example",
    "updated_at": 1673597091,
    "version_code": "1",
    "version_name": "1.0",
    "sdk_version": 26,
    "raw_sdk_version": "26",
    "target_sdk_version": 26,
    "signature": null,
    "md5": null,
    "revision": 18,
    "file_size": null,
    "icon": "https://deploygate.com/api/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example/binaries/18/download/icon",
    "message": "",
    "file": "https://deploygate.com/api/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example/binaries/18/download/binary.apk",
    "user": {
      "name": "____this_is_dummy____",
      "profile_icon": "http://example.com"
    }
  }
}
"""

        when:
        def response = ApiClient.GSON.fromJson(jsonStr, UploadAppResponse)

        then:
        response.application.revision == 18
        response.application.path == "/users/____this_is_dummy____/platforms/android/apps/com.deploygate.example"
    }
}
