@echo off
cmd.exe /k cd /d "%~dp0"
javac Celebro.java Memoria.java ChiwabeLLM.java
java Celebro
pause