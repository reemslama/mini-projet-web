<#
.SYNOPSIS
    Launches all backend services of the Arène project.
.DESCRIPTION
    Eureka (8761), MS4 Evaluation (8084, 8087),
    MS3 Soumission (8083), MS5 Verification (8085). Adds the eureka1 host alias if needed
    and starts MySQL via Docker when it is not already running.
.EXAMPLE
    .\start-all.bat
    .\start-all.ps1 -SkipDb
#>
param(
    [switch]$SkipDb,
    [int]$WaitTimeout = 300
)

$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot
$LogDir = Join-Path $Root 'logs'
$RunDir = Join-Path $Root 'run'
New-Item -ItemType Directory -Force -Path $LogDir, $RunDir | Out-Null

function Write-Step {
    param([string]$Message)
    Write-Host "[start-all] $Message"
}
function Write-WarnMsg {
    param([string]$Message)
    Write-Host "[start-all] WARNING: $Message" -ForegroundColor Yellow
}

function Test-PortOpen {
    param([int]$Port)
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $result = $client.BeginConnect('127.0.0.1', $Port, $null, $null)
        if (-not $result.AsyncWaitHandle.WaitOne(300, $false)) { return $false }
        $client.EndConnect($result)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Wait-Port {
    param(
        [string]$Label,
        [int]$Port,
        [int]$TimeoutSec,
        [int]$ProcessId = 0,
        [string]$LogName = ''
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortOpen -Port $Port) {
            Write-Step "$Label is listening on http://localhost:$Port"
            return $true
        }
        if ($ProcessId -gt 0 -and -not (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue)) {
            Write-WarnMsg "$Label exited before opening port $Port (see $LogDir\$LogName.log)"
            return $false
        }
        Start-Sleep -Seconds 2
    }
    Write-WarnMsg "$Label did not open port $Port within ${TimeoutSec}s (see $LogDir\$LogName.log)"
    return $false
}

function Wait-Service {
    param([string]$Name, [string]$Label, [int]$Port)
    $pidValue = 0
    $pidFile = Join-Path $RunDir "$Name.pid"
    if (Test-Path $pidFile) {
        [void][int]::TryParse((Get-Content -LiteralPath $pidFile -Raw).Trim(), [ref]$pidValue)
    }
    return (Wait-Port -Label $Label -Port $Port -TimeoutSec $WaitTimeout -ProcessId $pidValue -LogName $Name)
}

function Ensure-HostAliases {
    $hostsPath = Join-Path $env:SystemRoot 'System32\drivers\etc\hosts'
    $missing = @()
    foreach ($alias in @('eureka1')) {
        $pattern = '(^|\s)' + $alias + '(\s|$)'
        if (-not (Select-String -LiteralPath $hostsPath -Pattern $pattern -Quiet -ErrorAction SilentlyContinue)) {
            $missing += $alias
        }
    }
    if ($missing.Count -eq 0) {
        Write-Step 'host alias eureka1 is already configured'
        return $true
    }

    $entries = @($missing | ForEach-Object { "127.0.0.1 $_" })
    Write-Step "adding $($missing -join '/') to the hosts file"
    try {
        Add-Content -LiteralPath $hostsPath -Value $entries -ErrorAction Stop
    } catch {
        $tmpFile = Join-Path $env:TEMP "arene-hosts-$PID.txt"
        Set-Content -LiteralPath $tmpFile -Value $entries
        $elevatedArgs = '/c type "{0}" >> "{1}"' -f $tmpFile, $hostsPath
        try {
            $process = Start-Process -FilePath "$env:SystemRoot\System32\cmd.exe" -ArgumentList $elevatedArgs -Verb RunAs -Wait -PassThru
            if ($process.ExitCode -ne 0) { throw 'elevated hosts update failed' }
        } catch {
            Write-WarnMsg "could not update $hostsPath automatically; add these lines as Administrator:"
            foreach ($entry in $entries) { Write-Host "    $entry" }
            return $false
        } finally {
            Remove-Item -LiteralPath $tmpFile -ErrorAction SilentlyContinue
        }
    }

    foreach ($alias in $missing) {
        $pattern = '(^|\s)' + $alias + '(\s|$)'
        if (-not (Select-String -LiteralPath $hostsPath -Pattern $pattern -Quiet -ErrorAction SilentlyContinue)) {
            Write-WarnMsg "host alias $alias is still missing from $hostsPath"
            return $false
        }
    }
    if (Get-Command Clear-DnsClientCache -ErrorAction SilentlyContinue) {
        Clear-DnsClientCache
    }
    Write-Step 'host aliases configured'
    return $true
}

function Ensure-Mysql {
    if (Test-PortOpen -Port 3306) {
        Write-Step 'MySQL is already listening on port 3306'
        return $true
    }
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-WarnMsg 'MySQL is not running on port 3306 and Docker is unavailable.'
        Write-WarnMsg 'MS4 needs MySQL (database forge_evaluation, user root, empty password).'
        return $false
    }

    & docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        Write-WarnMsg 'Docker is installed but its daemon is not available.'
        return $false
    }

    $containers = @(& docker ps -a --format '{{.Names}}')
    if ($containers -contains 'arene-mysql') {
        Write-Step 'starting existing arene-mysql container'
        & docker start arene-mysql *> $null
    } else {
        Write-Step "starting MySQL 8 container 'arene-mysql'"
        & docker run -d --name arene-mysql -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=forge_evaluation -p 3306:3306 mysql:8 *> $null
    }
    if ($LASTEXITCODE -ne 0) {
        Write-WarnMsg 'could not start the MySQL container.'
        return $false
    }
    return (Wait-Port -Label 'MySQL' -Port 3306 -TimeoutSec 120)
}

# Hibernate cannot auto-detect the dialect on recent MariaDB releases.
function Get-DbDialectArg {
    foreach ($client in @('mariadb', 'mysql')) {
        if (Get-Command $client -ErrorAction SilentlyContinue) {
            $version = (& $client -uroot -N -B -e 'SELECT VERSION()' 2>$null) -join ' '
            if ($version -match 'MariaDB') {
                return '--spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect'
            }
        }
    }
    return ''
}

function Get-MavenCmd {
    param([string]$Dir)
    $wrapper = Join-Path $Dir 'mvnw.cmd'
    if (Test-Path $wrapper) { return $wrapper }
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($maven) { return $maven.Source }
    throw 'Maven was not found (no mvnw.cmd and no mvn.cmd on PATH).'
}

function Start-ServiceProcess {
    param(
        [string]$Name,
        [string]$Dir,
        [int]$Port,
        [string]$RunArgs = ''
    )

    $pidFile = Join-Path $RunDir "$Name.pid"
    if (Test-Path $pidFile) {
        $oldPid = 0
        $rawPid = (Get-Content -LiteralPath $pidFile -Raw).Trim()
        if ([int]::TryParse($rawPid, [ref]$oldPid) -and (Get-Process -Id $oldPid -ErrorAction SilentlyContinue)) {
            Write-Step "$Name is already running (PID $oldPid)"
            return
        }
    }
    if (Test-PortOpen -Port $Port) {
        Write-WarnMsg "port $Port is already in use; skipping $Name"
        return
    }

    $maven = Get-MavenCmd -Dir $Dir
    $arguments = @('spring-boot:run')
    if ($RunArgs) {
        $arguments += ('-Dspring-boot.run.arguments="{0}"' -f $RunArgs)
    }
    # A batch file must be launched through cmd.exe when its output is redirected.
    $argumentLine = $arguments -join ' '
    $cmdArgs = '/d /c ""{0}" {1}"' -f $maven, $argumentLine
    $process = Start-Process -FilePath (Join-Path $env:SystemRoot 'System32\cmd.exe') -ArgumentList $cmdArgs -WorkingDirectory $Dir `
        -RedirectStandardOutput ( Join-Path $LogDir "$Name.log" ) `
        -RedirectStandardError  ( Join-Path $LogDir "$Name.err.log" ) `
        -WindowStyle Hidden -PassThru
    Set-Content -LiteralPath $pidFile -Value $process.Id
    Write-Step "started $Name (PID $($process.Id))"
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host '[start-all] ERROR: Java is not installed or not on PATH.' -ForegroundColor Red
    exit 1
}

Write-Step "project root: $Root"
if (-not (Ensure-HostAliases)) {
    Write-Host '[start-all] ERROR: Eureka needs the eureka1 host alias.' -ForegroundColor Red
    exit 1
}
$dbDialectArg = ''
if (-not $SkipDb) {
    if (-not (Ensure-Mysql)) {
        Write-WarnMsg 'continuing without MySQL; MS4 will report itself DOWN until a database is available'
    } else {
        $dbDialectArg = Get-DbDialectArg
    }
}

# Discovery server first, so clients can register as soon as it is up.
Start-ServiceProcess -Name 'eureka-1' -Dir (Join-Path $Root 'Backend\eureka-server') -Port 8761 `
    -RunArgs '--server.port=8761 --eureka.instance.hostname=eureka1 --eureka.client.service-url.defaultZone=http://localhost:8761/eureka/'
[void](Wait-Service -Name 'eureka-1' -Label 'Eureka' -Port 8761)

Start-ServiceProcess -Name 'ms4-evaluation-1' -Dir (Join-Path $Root 'Backend\ms4-evaluation') -Port 8084 -RunArgs ("--server.port=8084 $dbDialectArg").Trim()
Start-ServiceProcess -Name 'ms4-evaluation-2' -Dir (Join-Path $Root 'Backend\ms4-evaluation') -Port 8087 -RunArgs ("--server.port=8087 $dbDialectArg").Trim()
Start-ServiceProcess -Name 'ms3-soumission' -Dir (Join-Path $Root 'Backend\soumission') -Port 8083
Start-ServiceProcess -Name 'ms5-verification' -Dir (Join-Path $Root 'Backend\ms5-verification') -Port 8085

[void](Wait-Service -Name 'ms4-evaluation-1' -Label 'MS4 Evaluation 1' -Port 8084)
[void](Wait-Service -Name 'ms4-evaluation-2' -Label 'MS4 Evaluation 2' -Port 8087)
[void](Wait-Service -Name 'ms3-soumission' -Label 'MS3 Soumission' -Port 8083)
[void](Wait-Service -Name 'ms5-verification' -Label 'MS5 Verification' -Port 8085)

Write-Host ''
Write-Step 'services requested:'
Write-Host '  Eureka          http://localhost:8761  (registry: http://localhost:8761/eureka/apps)'
Write-Host '  MS4 (8084)      health http://localhost:8084/actuator/health  docs http://localhost:8084/swagger-ui.html'
Write-Host '  MS4 (8087)      health http://localhost:8087/actuator/health  docs http://localhost:8087/swagger-ui.html'
Write-Host '  MS3 (8083)      health http://localhost:8083/actuator/health  docs http://localhost:8083/swagger-ui.html'
Write-Host '  MS5 (8085)      health http://localhost:8085/actuator/health  docs http://localhost:8085/swagger-ui.html'
Write-Host '  MS5 OpenAPI     http://localhost:8085/v3/api-docs  |  eureka http://localhost:8761/eureka/apps/MS5-VERIFICATION'
Write-Host "  MS5 requests    $Root\Backend\ms5-verification\verifications.http"
Write-Host "  Logs            $LogDir"
Write-Host '  Stop all        .\stop-all.bat'
