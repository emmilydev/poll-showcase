#!/bin/bash

set -e

REPO_URL="https://github.com/emmily-dev/sigma.git"
DIR_NAME="deps"

run_maven_install() {
  local dir="$1"
  cd "$dir"

  if command -v mvn &> /dev/null; then
    echo "Maven is installed, using it in $dir"
    mvn clean install -DskipTests
  else
    echo "Maven is not installed in $dir"

    if [ -f "./mvnw" ]; then
      echo "mvnw found in $dir, using it"
      ./mvnw clean install -DskipTests
    else
      echo "mvnw not found in $dir, downloading wrapper..."

      mkdir -p .mvn/wrapper

      curl -s -o mvnw https://raw.githubusercontent.com/takari/maven-wrapper/master/mvnw
      curl -s -o mvnw.cmd https://raw.githubusercontent.com/takari/maven-wrapper/master/mvnw.cmd
      curl -s -o .mvn/wrapper/maven-wrapper.jar https://repo1.maven.org/maven2/io/takari/maven-wrapper/0.5.6/maven-wrapper-0.5.6.jar
      echo "distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" > .mvn/wrapper/maven-wrapper.properties

      chmod +x mvnw
      ./mvnw clean install -DskipTests
    fi
  fi

  cd - > /dev/null
}

if [ ! -d "$DIR_NAME" ]; then
  git clone "$REPO_URL" "$DIR_NAME"
fi

run_maven_install "$DIR_NAME"

run_maven_install "."

echo "Sigma has been successfully installed."
