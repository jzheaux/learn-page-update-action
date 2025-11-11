#!/bin/bash

if test -f pom.xml
then
  export RELEASES_PROJECT_VERSION=$(mvn help:evaluate -Dexpression="project.version" -q -DforceStdout)
else
  export RELEASES_PROJECT_VERSION=$(cat gradle.properties | sed -n '/^version=/ { s/^version=//;p }')
fi

java -jar /opt/action/releases-api-sync.jar $@
