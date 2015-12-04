#!/usr/bin/env bash

./gradlew clean build uploadArchives -Psigning.secretKeyRingFile=$HOME/.gnupg/secring.gpg -PsonatypeUsername=henteko -PsonatypeFullname=deploygate -Psigning.keyId=$SIGNING_KEY_ID -Psigning.password=$SIGNING_PASSWORD -PsonatypePassword=$SONATYPE_PASSWORD
curl -X POST --data-urlencode 'payload={"channel": "#henteko-test", "username": "DG-RELEASE-GRADLE-PLUGIN", "text": "Released a gradle-deploygate-plugin in Maven Central! Please check <https://oss.sonatype.org/content/groups/public/com/deploygate/|public> or <https://oss.sonatype.org/index.html#stagingRepositories|stagingRepositories>", "icon_emoji": ":tada:"}' $SLACK_URL
