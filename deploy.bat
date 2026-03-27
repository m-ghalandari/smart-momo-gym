@echo off
echo [0/4] Setze Java Version auf 21...
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1/4] Starte Maven Build...
call mvn clean package -s private-settings.xml
if %errorlevel% neq 0 (
    echo.
    echo ❌ Fehler beim Maven Build! Abbruch.
    exit /b %errorlevel%
)

echo.
echo [2/4] Baue Docker Image...
docker build -t momo-local .
if %errorlevel% neq 0 (
    echo.
    echo ❌ Fehler beim Docker Build! Abbruch.
    exit /b %errorlevel%
)

echo.
echo [3/4] Tagge Docker Image...
docker tag momo-local ghcr.io/m-ghalandari/smart-momo-gym:latest

echo.
echo [4/4] Pushe Docker Image zu GitHub...
docker push ghcr.io/m-ghalandari/smart-momo-gym:latest

echo.
echo ✅ Fertig! Das Image ist online und bereit fuer Render.