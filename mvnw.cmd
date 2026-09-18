@echo off
rem ============================================================
rem  Project-bundled Maven launcher (Windows CMD / PowerShell)
rem
rem  Usage:  mvnw.cmd clean install -DskipTests
rem
rem  Uses the Maven shipped inside this project (tools\apache-maven-3.9.6),
rem  so no separate Maven installation is required on a new machine.
rem
rem  NOTE: keep this file ASCII-only. Batch files are parsed with the
rem  console code page (GBK on Chinese Windows); non-ASCII comments break
rem  parsing and produce spurious "not recognized as a command" errors.
rem ============================================================
setlocal EnableExtensions

rem Project root = directory of this script, without trailing backslash
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

set "MAVEN_HOME=%PROJECT_DIR%\tools\apache-maven-3.9.6"
set "CW_JAR=%MAVEN_HOME%\boot\plexus-classworlds-2.7.0.jar"
set "M2_CONF=%MAVEN_HOME%\bin\m2.conf"

if not exist "%CW_JAR%" (
    echo [ERROR] Bundled Maven not found: %CW_JAR%
    echo         Make sure the tools\apache-maven-3.9.6 folder is copied with the project.
    endlocal
    exit /b 1
)

if not defined JAVA_HOME (
    echo [WARN] JAVA_HOME is not set, falling back to "java" on PATH.
)

set "MAVEN_OPTS=%MAVEN_OPTS% -Dfile.encoding=UTF-8"

rem Invoke Maven through java + classworlds launcher directly.
rem This avoids mvn.cmd's own path resolution and any code-page trouble
rem caused by non-ASCII characters (e.g. the Chinese project path).
set "JAVA_BIN=java"
if defined JAVA_HOME set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"

"%JAVA_BIN%" -Dfile.encoding=UTF-8 -Dmaven.home="%MAVEN_HOME%" -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" -Dclassworlds.conf="%M2_CONF%" -classpath "%CW_JAR%" org.codehaus.plexus.classworlds.launcher.Launcher -f "%PROJECT_DIR%\pom.xml" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
