package com.deploygate.gradle.plugins

import org.gradle.api.Project

class Apk {
    final String name
    File file
    String message
    String distributionKey
    String releaseNote
    String visibility

    Apk(String name) {
        this.name = name
        this.file = null
        this.message = null
        this.distributionKey = null
        this.releaseNote = null
        this.visibility = 'private'
    }

    public HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<String, String>()
        if(message != null) {
            params.put("message", message)
        }
        if(distributionKey != null) {
            params.put("distribution_key", distributionKey)
        }
        if(releaseNote != null) {
            params.put("release_note", releaseNote)
        }
        if(visibility != null) {
            params.put("visibility", visibility)
        }
        return params
    }

    public static List<Apk> getApks(Project project, String searchApkName = "") {
        List<Apk> apks = []
        for (_apk in project.deploygateApks) {
            String name = _apk.name
            if(searchApkName != "" && searchApkName != name) continue

            File file = null
            String message = ""
            String distributionKey = null
            String releaseNote = null
            String visibility = "private"

            if(_apk.hasProperty("sourceFile") && _apk.sourceFile != null) {
                file = _apk.sourceFile
            }
            if(_apk.hasProperty("message") && _apk.message != null) {
                message = _apk.message
            }
            if(_apk.hasProperty("distributionKey") && _apk.distributionKey != null) {
                distributionKey = _apk.distributionKey
            }
            if(_apk.hasProperty("releaseNote") && _apk.releaseNote != null) {
                releaseNote = _apk.releaseNote 
            }
            if(_apk.hasProperty("visibility") && _apk.visibility != null) {
                visibility = _apk.visibility
            }

            Apk apk = new Apk(name, file, message, distributionKey, releaseNote, visibility)
            apks.add(apk)
        }
        return apks
    }
}
