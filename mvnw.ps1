<#
.SYNOPSIS
    Project-bundled Maven launcher (Windows PowerShell)

.DESCRIPTION
    Locates the project root automatically and runs the Maven that ships
    inside this project (tools\apache-maven-3.9.6), so no separate Maven
    installation is needed on a new machine.

.EXAMPLE
    .\mvnw.ps1 clean install -DskipTests
    .\mvnw.ps1 -pl ruoyi-admin spring-boot:run

.NOTES
    Only a JDK (JAVA_HOME, or "java" on PATH) is required.
#>
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = 'Stop'

$ProjectDir = $PSScriptRoot
$MavenHome  = Join-Path $ProjectDir 'tools\apache-maven-3.9.6'
$Classworlds = Join-Path $MavenHome 'boot\plexus-classworlds-2.7.0.jar'
$M2Conf      = Join-Path $MavenHome 'bin\m2.conf'

if (-not (Test-Path $Classworlds)) {
    Write-Error "Bundled Maven not found: $Classworlds`nMake sure the tools\apache-maven-3.9.6 folder is copied with the project."
    exit 1
}

if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $JavaBin = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    Write-Warning 'JAVA_HOME is not set, falling back to "java" on PATH.'
    $JavaBin = 'java'
}

# 控制台编码设为 UTF-8，避免中文路径/中文输出在 GBK 控制台下乱码
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch {}
$env:MAVEN_OPTS = "$env:MAVEN_OPTS -Dfile.encoding=UTF-8"

# 直接用 java + classworlds 启动器调用 Maven，避免 mvn.cmd 的路径/代码页问题
& $JavaBin `
    '-Dfile.encoding=UTF-8' `
    "-Dmaven.home=$MavenHome" `
    "-Dmaven.multiModuleProjectDirectory=$ProjectDir" `
    "-Dclassworlds.conf=$M2Conf" `
    '-classpath' $Classworlds `
    'org.codehaus.plexus.classworlds.launcher.Launcher' `
    '-f' (Join-Path $ProjectDir 'pom.xml') `
    @MavenArgs

exit $LASTEXITCODE
