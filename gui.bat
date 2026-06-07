@echo off
REM Script para executar a interface gráfica do Chiwabe
REM Chiwabe GUI Launcher

echo.
echo ========================================
echo   Chiwabe Chatbot
echo ========================================
echo.

cd /d "%~dp0"

REM Executar com Maven
mvn javafx:run -Dexec.mainClass="com.chiwabe.ChiwabeGUI"

pause
