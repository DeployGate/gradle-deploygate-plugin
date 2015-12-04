#!/usr/bin/env bash

./gradlew clean build uploadArchives -Psigning.secretKeyRingFile=$HOME/.gnupg/secring.gpg -PsonatypeUsername=henteko -PsonatypeFullname=deploygate -Psigning.keyId=$SIGNING_KEY_ID -Psigning.password=$SIGNING_PASSWORD -PsonatypePassword=$SONATYPE_PASSWORD
