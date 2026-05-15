@echo off
REM Script para compilar e executar o Chiwabe Chatbot
REM Estrutura Maven - Compilação com javac

cd /d "%~dp0"

REM Criar diretório de saída se não existir
if not exist "target\classes" mkdir target\classes

echo.
echo ========================================
echo Compilando arquivos Java...
echo ========================================
echo.

REM Compilar apenas os arquivos que não dependem de bibliotecas externas
REM ChiwabeDiscord.java requer JDA e será compilado com Maven
javac -d target/classes -cp "src/main/java" ^
    src/main/java/com/chiwabe/Memoria.java ^
    src/main/java/com/chiwabe/ChiwabeLLM.java ^
    src/main/java/com/chiwabe/Celebro.java

if errorlevel 1 (
    echo.
    echo ========================================
    echo ERRO na compilacao!
    echo ========================================
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilacao concluida com sucesso!
echo ========================================
echo.
echo Executando Celebro...
echo.

REM Executar a classe principal
java -cp target/classes com.chiwabe.Celebro

pause
