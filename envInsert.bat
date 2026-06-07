@echo off
REM Script para executar a interface gráfica do Chiwabe

echo.
echo ========================================
echo   ENV Loader
echo ========================================
echo.
echo Initializing...
echo.



cd /d "%~dp0" && mvn clean compile && mvn exec:java -Dexec.mainClass="com.chiwabe.EnvInsert" 2>&1

pause
