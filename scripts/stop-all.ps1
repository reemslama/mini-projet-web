<#
.SYNOPSIS
    Stops every service started by scripts\start-all.ps1.
#>
$ErrorActionPreference = 'Continue'
$RunDir = Join-Path $PSScriptRoot 'run'

if (-not (Test-Path $RunDir)) {
    Write-Host '[stop-all] nothing to stop'
    exit 0
}

function Stop-ProcessTree {
    param([int]$Id)
    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $Id" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -Id $child.ProcessId
    }
    Stop-Process -Id $Id -Force -ErrorAction SilentlyContinue
}

Get-ChildItem -LiteralPath $RunDir -Filter '*.pid' -File | ForEach-Object {
    $name = $_.BaseName
    $pidValue = 0
    $rawPid = (Get-Content -LiteralPath $_.FullName -Raw).Trim()
    if ([int]::TryParse($rawPid, [ref]$pidValue) -and (Get-Process -Id $pidValue -ErrorAction SilentlyContinue)) {
        Stop-ProcessTree -Id $pidValue
        Write-Host "[stop-all] stopped $name (PID $pidValue)"
    } else {
        Write-Host "[stop-all] $name was not running"
    }
    Remove-Item -LiteralPath $_.FullName -Force
}
