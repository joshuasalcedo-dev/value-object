#!/usr/bin/env bash
# Runs the demo on the sample documents and saves a screenshot of each tab to image/ (for the README), then quits.
#
#   demo/screenshots.sh                     Primer Light
#   demo/screenshots.sh "Primer Dark"       another AtlantaFX theme
#
# Needs a JDK 25+, Maven, and a desktop (the window opens for a few seconds). The samples are made up: the library's
# test documents (src/test/resources/samples) and demo/samples.
set -euo pipefail
cd "$(dirname "$0")/.."

THEME="${1:-Primer Light}"

echo "Building the library and the demo..."
mvn -q install -DskipTests
mvn -q -f demo/pom.xml compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt

# Windows (Git Bash, Cygwin) separates a classpath with ';', everything else with ':'
SEP=":"
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac
CP="demo/target/classes${SEP}$(cat demo/target/classpath.txt)"

echo "Running the demo and taking screenshots..."
java --enable-native-access=ALL-UNNAMED -cp "$CP" io.joshuasalcedo.valueobject.demo.Launcher \
    --folder src/test/resources/samples --folder demo/samples \
    --theme "$THEME" --screenshots image

echo "Screenshots are in image/"
