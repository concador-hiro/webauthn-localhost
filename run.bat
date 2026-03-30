@echo off
setlocal

set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"

if exist "%APP_HOME%\runtime\jre\bin\java.exe" (
  set "JAVA_CMD=%APP_HOME%\runtime\jre\bin\java.exe"
) else if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_CMD=java"
)

if not exist "%APP_HOME%\target\webauthn-servlet.jar" (
  echo [ERROR] target\webauthn-servlet.jar not found.
  echo Run build.bat first.
  exit /b 1
)

echo [INFO] Starting webauthn-servlet...
"%JAVA_CMD%" "-Dapp.home=%APP_HOME%" -jar "%APP_HOME%\target\webauthn-servlet.jar"

exit /b %ERRORLEVEL%
