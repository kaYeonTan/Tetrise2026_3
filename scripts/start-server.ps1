# 테트리스 로비 서버 시작 스크립트 (PowerShell)
# 사용법: .\scripts\start-server.ps1 [포트번호]

param(
    [int]$Port = 8765
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Write-Host "========================================"
Write-Host "   테트리스 로비 서버 시작 (3조)"
Write-Host "========================================"
Write-Host ""

# JAR 파일 확인
$jarPath = Join-Path $root "target\lobby-server.jar"
$classesPath = Join-Path $root "target\classes"
$gsonPath = Join-Path $root "lib\gson-2.11.0.jar"

if (Test-Path $jarPath) {
    Write-Host "[OK] lobby-server.jar 발견" -ForegroundColor Green
    Write-Host ""
    Write-Host "서버 시작 중... (포트: $Port)"
    Write-Host ""
    & java -jar $jarPath $Port
}
elseif (Test-Path $classesPath) {
    Write-Host "[INFO] JAR 없음, 컴파일된 클래스로 실행" -ForegroundColor Yellow
    Write-Host ""
    $cp = "$classesPath;$gsonPath"
    & java -cp $cp kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain $Port
}
else {
    Write-Host "[ERROR] 빌드가 필요합니다!" -ForegroundColor Red
    Write-Host ""
    Write-Host "다음 중 하나를 실행하세요:"
    Write-Host "  .\scripts\compile.ps1     (빠른 컴파일)"
    Write-Host "  mvn package -DskipTests   (JAR 빌드)"
    exit 1
}
