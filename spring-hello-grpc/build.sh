#!/bin/bash

set -e

BUILD_NUMBER=$(./gradlew -q buildNumber)

if [ -z "$BUILD_NUMBER" ]; then
    echo "Error: BUILD_NUMBER environment variable is not set." >&2
    exit 1
fi

./gradlew clean :api:publish
./gradlew :client:publish
./gradlew :server:build
