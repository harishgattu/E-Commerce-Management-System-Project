@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@IF "%DEBUG%" == "" @ECHO OFF
@SETLOCAL
set ERROR_CODE=0

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJCmd
if exist "%JAVACMD%" goto chkMHome

echo The JAVA_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
echo NB: JAVA_HOME should point to a JDK not a JRE >&2
goto error

:chkMHome
set "DIRNAME=%~dp0%"
if "%DIRNAME%" == "" set "DIRNAME=."
set "APP_BASEDIR=%DIRNAME%"

@REM Fallback to checking if mvn is installed on PATH
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
  mvn %*
  goto end
)

echo [mvnw] Apache Maven not detected on PATH.
echo [mvnw] Please run using your IDE (Eclipse, IntelliJ, VS Code) or install Maven on PATH.
exit /b 1

:error
exit /b 1

:end
exit /b 0
