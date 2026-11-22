# Runs the demo on the sample documents and saves a screenshot of each tab to image\ (for the README), then quits.
#
#   demo\screenshots.ps1                     Primer Light
#   demo\screenshots.ps1 -Theme "Nord Dark"  another AtlantaFX theme
#
# Needs a JDK 25+, Maven, and a desktop (the window opens for a few seconds). The samples are made up: the library's
# test documents (src\test\resources\samples) and demo\samples. The same as demo/screenshots.sh, for PowerShell.
param([string]$Theme = "Primer Light")
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Building the library and the demo..."
mvn -q install -DskipTests
if ($LASTEXITCODE -ne 0) { throw "mvn install failed" }
mvn -q -f demo/pom.xml compile dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt"
if ($LASTEXITCODE -ne 0) { throw "building the demo failed" }

$cp = "demo\target\classes;" + (Get-Content "demo\target\classpath.txt" -Raw).Trim()

Write-Host "Running the demo and taking screenshots..."
java --enable-native-access=ALL-UNNAMED -cp $cp io.joshuasalcedo.valueobject.demo.Launcher `
    --folder src\test\resources\samples --folder demo\samples `
    --theme $Theme --screenshots image
if ($LASTEXITCODE -ne 0) { throw "the demo failed" }

Write-Host "Screenshots are in image\"
