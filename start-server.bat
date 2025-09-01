@echo off
echo ======================================
echo        NewPix Server - Iniciando
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
echo Iniciando servidor com interface grafica...
mvn exec:java -Dexec.mainClass="com.newpix.server.gui.ServerGUI" -q

pause
