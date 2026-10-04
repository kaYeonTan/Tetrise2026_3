@echo off
chcp 65001 >nul
echo ========================================
echo    테트리스 로비 서버 시작 (3조)
echo ========================================
echo.

REM 프로젝트 루트로 이동
cd /d "%~dp0.."

REM JAR 파일 확인
if exist "target\lobby-server.jar" (
    echo [OK] lobby-server.jar 발견
    echo.
    echo 서버 시작 중...
    java -jar target\lobby-server.jar %1
) else (
    echo [ERROR] lobby-server.jar 가 없습니다!
    echo.
    echo 먼저 빌드하세요:
    echo   mvn package -DskipTests
    echo.
    echo 또는 컴파일된 클래스로 실행:
    if exist "target\classes" (
        echo   java -cp "target/classes;lib/gson-2.11.0.jar" kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain
        echo.
        echo 지금 클래스로 실행할까요? (Y/N)
        set /p choice=
        if /i "%choice%"=="Y" (
            java -cp "target/classes;lib/gson-2.11.0.jar" kr.ac.jbnu.se.tetris.network.lobby.LobbyServerMain %1
        )
    ) else (
        echo   target/classes 폴더도 없습니다.
        echo   scripts/compile.ps1 또는 mvn compile 먼저 실행하세요.
    )
)

pause
