$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$mysqlHome = Join-Path $projectRoot ".local/mysql/mysql-8.4.10-winx64"
$mysqlIni = Join-Path $projectRoot ".local/mysql/my.ini"
$logPath = Join-Path $projectRoot ".local/mysql/mysqld.log"
$errPath = Join-Path $projectRoot ".local/mysql/mysqld.err.log"

if (-not (Test-Path -LiteralPath (Join-Path $mysqlHome "bin/mysqld.exe"))) {
    throw "MySQL executable not found. Expected: $mysqlHome"
}

$existing = Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Output "MySQL port 3306 is already listening. PID(s): $($existing.OwningProcess -join ', ')"
    exit 0
}

$process = Start-Process `
    -FilePath (Join-Path $mysqlHome "bin/mysqld.exe") `
    -ArgumentList @("--defaults-file=$mysqlIni") `
    -WorkingDirectory (Join-Path $projectRoot ".local/mysql") `
    -RedirectStandardOutput $logPath `
    -RedirectStandardError $errPath `
    -WindowStyle Hidden `
    -PassThru

Write-Output "Started local MySQL on port 3306. PID: $($process.Id)"
