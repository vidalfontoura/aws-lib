@REM ----------------------------------------------------------------------------
@REM  SQS Demo
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM SQS Demo Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@'
@echo off

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:init
@REM Decide how to startup depending on the version of windows

@REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
:Win9xApp
if %1a==a goto endInit
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
SET JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTCWJars

@REM -- Regular WinNT shell

@REM ToDo :: Set Classpath

goto runsqs

@REM The 4NT Shell from jp software
:4NTCWJars

@REM ToDo :: Set Classpath

goto runsqs

@REM Start SQS Tester
:runsqs
%JAVA_EXE% -classpath %APP_JAR% -Daws.accessKeyId=AKIAIYTG3H22JHEMHAYQ -Daws.secretKey=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 -Daws.endpoint=sqs.us-east-1.amazonaws.com com.charter.aesd.aws.sqsclient.demo.SQSDemo SQSTestQueue 5
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set JAVA_EXE=
goto postExec

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec

cmd /C exit /B %ERROR_CODE%

