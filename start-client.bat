@echo off
echo ======================================
echo        NewPix Cliente - Iniciando
echo ======================================
echo.

cd /d "%~dp0"

echo Compilando projeto...
mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

echo.
echo Iniciando cliente...
mvn exec:java -Dexec.mainClass="com.newpix.client.gui.LoginGUI" -q

pause
