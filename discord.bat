@echo off
REM ============================================
REM Script para compilar e executar Chiwabe Discord Bot
REM Usa Maven para resolver dependências corretamente
REM ============================================

setlocal enabledelayedexpansion

REM Mudar para o diretório do script
cd /d "%~dp0"

echo.
echo ============================================
echo Chiwabe Discord Bot - Inicializador
echo ============================================
echo.

REM Verificar se Maven está instalado
echo Verificando Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERRO] Maven não encontrado no PATH!
    echo.
    echo Solução:
    echo 1. Instale Maven de: https://maven.apache.org/download.cgi
    echo 2. Adicione o diretório bin do Maven ao PATH do Windows
    echo 3. Reinicie o terminal e tente novamente
    echo.
    pause
    exit /b 1
)

REM Verificar se Java está instalado
echo Verificando Java...
where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERRO] Java não encontrado no PATH!
    echo.
    echo Solução:
    echo 1. Instale Java 21 ou superior
    echo 2. Adicione o diretório bin do Java ao PATH do Windows
    echo 3. Reinicie o terminal e tente novamente
    echo.
    pause
    exit /b 1
)

REM Verificar se arquivo .env existe
if not exist "ChiwabeChatbot\.env" (
    echo.
    echo [ERRO] Arquivo ChiwabeChatbot\.env não encontrado!
    echo.
    echo Crie o arquivo com as seguintes variáveis:
    echo API_KEY=sua_chave_openrouter
    echo DISCORD_BOT_TOKEN=seu_token_discord
    echo DISCORD_CLIENT_ID=seu_client_id
    echo.
    pause
    exit /b 1
)

echo [OK] Dependências encontradas
echo.

REM Compilar com Maven
echo ============================================
echo Compilando projeto com Maven...
echo ============================================
echo.

call mvn clean compile -q -DskipTests=true

if errorlevel 1 (
    echo.
    echo [ERRO] Falha na compilação!
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] Compilação concluída com sucesso!
echo.

REM Executar com Maven
echo ============================================
echo Executando ChiwabeDiscord...
echo ============================================
echo.

call mvn exec:java -Dexec.mainClass="com.chiwabe.ChiwabeDiscord" -q

if errorlevel 1 (
    echo.
    echo [ERRO] Falha na execução!
    echo.
    pause
    exit /b 1
)

pause
