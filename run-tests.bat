@echo off
echo ======================================
echo         NewPix - Executar Testes
echo ======================================
echo.

cd /d "%~dp0"

echo Compilando projeto...
mvn clean compile test-compile -q

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilacao!
    pause
    exit /b 1
)

echo.
echo Executando testes...
mvn test

echo.
echo Testes concluidos!
pause
