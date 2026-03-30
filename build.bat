@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

if not defined JAVA_HOME set "JAVA_HOME=C:\java\jdk-8u202"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] JAVA_HOME is invalid: %JAVA_HOME%
  exit /b 1
)

if not defined MAVEN_HOME set "MAVEN_HOME=C:\java\apache-maven-3.9.14"
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo [ERROR] MAVEN_HOME is invalid: %MAVEN_HOME%
  exit /b 1
)

if not defined MAVEN_REPO set "MAVEN_REPO=%SCRIPT_DIR%offline-m2\repository"
if not exist "%MAVEN_REPO%" (
  if exist "%USERPROFILE%\.m2\repository" (
    set "MAVEN_REPO=%USERPROFILE%\.m2\repository"
  ) else (
    echo [ERROR] Offline Maven repository not found.
    echo         Set MAVEN_REPO to your preloaded repository path.
    echo         Example: set MAVEN_REPO=C:\java\m2-offline\repository
    exit /b 1
  )
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo [INFO] JAVA_HOME=%JAVA_HOME%
echo [INFO] MAVEN_HOME=%MAVEN_HOME%
echo [INFO] MAVEN_REPO=%MAVEN_REPO%
call "%MAVEN_HOME%\bin\mvn.cmd" -o -Dmaven.repo.local="%MAVEN_REPO%" -DskipTests clean package
exit /b %ERRORLEVEL%
