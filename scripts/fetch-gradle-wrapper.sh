#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.14"
DISTRIBUTION_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
ZIP_PATH="gradle-${GRADLE_VERSION}-bin.zip"
TARGET_DIR="gradle/wrapper"
TARGET_JAR="${TARGET_DIR}/gradle-wrapper.jar"

if [ -f "$TARGET_JAR" ]; then
  echo "Gradle wrapper jar already present at $TARGET_JAR"
  exit 0
fi

mkdir -p "$TARGET_DIR"

echo "Downloading Gradle ${GRADLE_VERSION} distribution..."
curl -sSL "$DISTRIBUTION_URL" -o "$ZIP_PATH"

echo "Extracting wrapper jar..."
unzip -j "$ZIP_PATH" "gradle-${GRADLE_VERSION}/lib/gradle-wrapper-*.jar" -d "$TARGET_DIR"

rm "$ZIP_PATH"

echo "Gradle wrapper jar written to $TARGET_JAR"
