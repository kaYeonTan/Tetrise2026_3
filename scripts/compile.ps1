# A(src/kr) + B(src/main/java) -> target/classes
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\javac.exe" } else { "javac" }
New-Item -ItemType Directory -Force -Path "$root\target\classes" | Out-Null

$files = @(
    Get-ChildItem "$root\src\kr" -Recurse -Filter *.java
    Get-ChildItem "$root\src\main\java" -Recurse -Filter *.java
) | ForEach-Object FullName

$gson = Join-Path $root "lib\gson-2.11.0.jar"
if (-not (Test-Path $gson)) {
    Write-Error "Missing $gson — run Maven once or download Gson 2.11.0 into lib/"
}
& $javac -encoding UTF-8 --release 17 -cp $gson -d "$root\target\classes" @files
if (Test-Path "$root\src\main\resources") {
    Copy-Item -Recurse -Force "$root\src\main\resources\*" "$root\target\classes\"
}
Write-Host "OK -> $root\target\classes"
Write-Host "Run game:  java -cp `"target/classes;$gson`" kr.ac.jbnu.se.tetris.app.GameWindow"
Write-Host "Run lobby: java -cp `"target/classes;$gson`" kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain"
