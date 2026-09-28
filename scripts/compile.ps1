# A(src/kr) + B(src/main/java) -> target/classes
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\javac.exe" } else { "javac" }
New-Item -ItemType Directory -Force -Path "$root\target\classes" | Out-Null

$files = @(
    Get-ChildItem "$root\src\kr" -Recurse -Filter *.java
    Get-ChildItem "$root\src\main\java" -Recurse -Filter *.java
) | ForEach-Object FullName

& $javac -encoding UTF-8 --release 17 -d "$root\target\classes" @files
if (Test-Path "$root\src\main\resources") {
    Copy-Item -Recurse -Force "$root\src\main\resources\*" "$root\target\classes\"
}
Write-Host "OK -> $root\target\classes"
Write-Host "Run: java -cp target/classes kr.ac.jbnu.se.tetris.app.GameWindow"
