@if "%DEBUG%"=="" ""=="" goto mainEnd
@echo off

@if ""%JAVA_HOME%""=="" "" goto findJavaFromPath
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto init

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
set JAVA_HOME=
set JAVA_EXE=
goto fail

:findJavaFromPath
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto init

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
:fail
set EXIT_CODE=1
if not "%OS%"=="Windows_NT" goto mainEnd
if %ERRORLEVEL% equ 0 set EXIT_CODE=0
exit /b %EXIT_CODE%

:init
set APP_BASE_NAME=%~n0
set APP_HOME=%~dp0
if "%APP_HOME%"=="" set APP_HOME=.
set APP_HOME=%APP_HOME:~0,-1%

set CLASSPATH=%APP_HOME%\gradle\wrapper\*
set DIST_DIR=%APP_HOME%\gradle\wrapper\dists

for /f "delims=" %%G in ('dir /s /b "%DIST_DIR%\gradle.bat" 2^>NUL') do set GRADLE_EXE=%%G
if "%GRADLE_EXE%"=="" (
    if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
    powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $tmp=[System.IO.Path]::GetTempFileName(); try { (New-Object Net.WebClient).DownloadFile('%WRAPPER_DIST%', $tmp); Expand-Archive -Path $tmp -DestinationPath '%DIST_DIR%' -Force } catch { exit 1 } finally { Remove-Item $tmp -ErrorAction SilentlyContinue }" || goto downloadFail
    for /f "delims=" %%G in ('dir /s /b "%DIST_DIR%\gradle.bat" 2^>NUL') do set GRADLE_EXE=%%G
)
if "%GRADLE_EXE%"=="" goto downloadFail

if not exist "%CLASSPATH%" (
    set WRAPPER_DIST=https://services.gradle.org/distributions/gradle-8.10.1-bin.zip
    if exist "%APP_HOME%\gradle\wrapper" (
        rem path exists
    ) else (
        mkdir "%APP_HOME%\gradle\wrapper" || goto downloadFail
    )
    powershell -Command "Add-Type -AssemblyName System.IO.Compression.FileSystem; $tmp=[System.IO.Path]::GetTempFileName(); try { (New-Object Net.WebClient).DownloadFile('%WRAPPER_DIST%', $tmp); $archive=[System.IO.Compression.ZipFile]::OpenRead($tmp); $entries=$archive.Entries | Where-Object { $_.FullName -like '*wrapper*.jar' }; if (-not $entries) { exit 1 }; foreach($entry in $entries) { $entry.ExtractToFile('%APP_HOME%\gradle\wrapper\' + [System.IO.Path]::GetFileName($entry.FullName), $true) } } catch { exit 1 } finally { Remove-Item $tmp -ErrorAction SilentlyContinue }" || goto downloadFail
)

goto parseArgs

downloadFail:
echo.
echo Unable to download Gradle wrapper jar from %WRAPPER_JAR_URL%
goto fail

:parseArgs
set APP_ARGS=
set _SKIP=2
:argsLoop
if "%~1"=="" goto execute
set APP_ARGS=%APP_ARGS% "%~1"
shift
goto argsLoop

:execute
"%GRADLE_EXE%" %APP_ARGS%

:mainEnd
