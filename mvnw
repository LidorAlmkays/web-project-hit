#!/bin/sh
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
export MAVEN_HOME="$SCRIPT_DIR/apache-maven-3.9.11"
export PATH="$MAVEN_HOME/bin:$PATH"

mvn "$@"

