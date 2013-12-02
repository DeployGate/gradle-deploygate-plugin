package com.deploygate.gradle.plugins

import org.gradle.api.Project

class Apk {
    String name
    File file
    String message
    String distributionKey
    String releaseNote

    public Apk(String name, File file, String message = "", String distributionKey = null, String releaseNote = null) {
        this.name = name
        this.file = file
        this.message = message
        this.distributionKey = distributionKey
        this.releaseNote = releaseNote
    }

    public HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<String, String>()
        params.put("message", message)
        if(distributionKey != null) {
            params.put("distribution_key", distributionKey)
        }
        if(releaseNote != null) {
            params.put("release_note", releaseNote)
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

            if(_apk.hasProperty("sourceFile")) {
                file = _apk.sourceFile
            }
            if(_apk.hasProperty("message")) {
                message = _apk.message
            }
            if(_apk.hasProperty("distributionKey")) {
                distributionKey = _apk.distributionKey
            }
            if(_apk.hasProperty("releaseNote")) {
                releaseNote = _apk.releaseNote 
            }

            Apk apk = new Apk(name, file, message, distributionKey, releaseNote)
            apks.add(apk)
        }
        return apks
    }
}
