param(
    [ValidateSet('setup','build','test','run','smoke','export','capture','sim','benchmark')]
    [string]$Task = 'test'
)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$projectRoot = Split-Path -Parent $PSScriptRoot
$gamePath = Join-Path $projectRoot 'Redesign-Evolution/Game'
$solutionPath = Join-Path $projectRoot 'Redesign-Evolution/Redesign-Evolution.sln'
$toolPath = Join-Path $projectRoot '.tools/godot'
$godotPath = Join-Path $toolPath 'Godot_v4.7.2-stable_mono_win64/Godot_v4.7.2-stable_mono_win64_console.exe'
$env:DOTNET_CLI_HOME = Join-Path $projectRoot '.tools/dotnet-home'
$env:NUGET_PACKAGES = Join-Path $projectRoot '.tools/nuget'
$env:DOTNET_CLI_TELEMETRY_OPTOUT = '1'
$env:DOTNET_SKIP_FIRST_TIME_EXPERIENCE = '1'
$env:DOTNET_GENERATE_ASPNET_CERTIFICATE = 'false'
$env:DOTNET_NOLOGO = '1'

function Invoke-Checked {
    param([string]$Program, [string[]]$Arguments)
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Program exited with code $LASTEXITCODE" }
}
function Build-Game {
    Invoke-Checked dotnet @('restore', $solutionPath, '--locked-mode', '--ignore-failed-sources')
    Invoke-Checked dotnet @('build', $solutionPath, '--no-restore', '-v', 'minimal')
}
function Test-Export {
    $exportExe = Join-Path $projectRoot '.artifacts/windows/Redesign-Evolution.exe'
    $exportLog = Join-Path $projectRoot '.artifacts/export-smoke.log'
    $exportArguments = '--headless --log-file "{0}" -- --smoke' -f $exportLog
    if (Test-Path -LiteralPath $exportLog) { Remove-Item -LiteralPath $exportLog }
    $exportProcess = Start-Process -FilePath $exportExe -ArgumentList $exportArguments -WorkingDirectory (Join-Path $projectRoot '.artifacts/windows') -WindowStyle Hidden -PassThru
    if (-not $exportProcess.WaitForExit(30000)) {
        Stop-Process -Id $exportProcess.Id
        throw 'Exported game smoke test exceeded 30 seconds.'
    }
    $exportProcess.Refresh()
    if ($exportProcess.ExitCode -ne 0 -or -not (Test-Path -LiteralPath $exportLog) -or -not (Select-String -LiteralPath $exportLog -SimpleMatch 'SMOKE PASS:' -Quiet)) {
        if (Test-Path -LiteralPath $exportLog) { Get-Content -LiteralPath $exportLog }
        throw 'Exported game failed its integration smoke test.'
    }
    Write-Output 'Exported Windows game: SMOKE PASS'
}
Push-Location $projectRoot
try {
    if ($Task -eq 'setup') {
        New-Item -ItemType Directory -Force -Path $toolPath | Out-Null
        $downloads = @(
            @{ File='editor.zip'; Asset='Godot_v4.7.2-stable_mono_win64.zip'; Hash='a2a48473a7414c5f19fab690518caebb738c09ef9601f6bd2388676a7f53b3c0'; Destination=$toolPath },
            @{ File='templates.zip'; Asset='Godot_v4.7.2-stable_mono_export_templates.tpz'; Hash='92f8681e349ef1f90891b792da95e3b2b0bd1ed610b78018c58feb2d87e15a9d'; Destination=(Join-Path $toolPath 'export') }
        )
        foreach ($download in $downloads) {
            $archivePath = Join-Path $toolPath $download.File
            if (-not (Test-Path -LiteralPath $archivePath)) {
                Invoke-WebRequest -UseBasicParsing -Uri ('https://github.com/godotengine/godot-builds/releases/download/4.7.2-stable/' + $download.Asset) -OutFile $archivePath
            }
            if ((Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant() -ne $download.Hash) {
                throw "Tool archive checksum mismatch: $archivePath"
            }
            Expand-Archive -LiteralPath $archivePath -DestinationPath $download.Destination -Force
        }
        Invoke-Checked dotnet @('restore', $solutionPath, '--locked-mode', '--source', 'https://api.nuget.org/v3/index.json')
        Invoke-Checked dotnet @('restore', (Join-Path $gamePath 'Redesign.Game.csproj'), '-p:Configuration=ExportRelease', '-r', 'win-x64', '--locked-mode', '--source', 'https://api.nuget.org/v3/index.json')
        Write-Output 'Pinned toolchain ready. Run ./scripts/dev.ps1 test next.'
        return
    }
    if (-not (Test-Path -LiteralPath $godotPath)) { throw 'Godot not found. Run ./scripts/dev.ps1 setup first.' }
    switch ($Task) {
        'build' { Build-Game }
        'test' {
            Build-Game
            Invoke-Checked dotnet @('test', (Join-Path $projectRoot 'Redesign-Evolution/Tests/Redesign.Tests.csproj'), '--no-build', '--no-restore', '--logger', 'trx;LogFileName=foundation.trx', '--results-directory', (Join-Path $projectRoot '.artifacts/tests'))
        }
        'run' { Build-Game; Invoke-Checked $godotPath @('--path', $gamePath) }
        'smoke' {
            Build-Game
            New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.artifacts') | Out-Null
            Invoke-Checked $godotPath @('--headless', '--path', $gamePath, '--log-file', (Join-Path $projectRoot '.artifacts/editor-smoke.log'), '--', '--smoke')
        }
        'sim' { Invoke-Checked dotnet @('run', '--no-restore', '--project', (Join-Path $projectRoot 'Redesign-Evolution/SimRunner/Redesign.SimRunner.csproj')) }
        'benchmark' {
            Build-Game
            New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.artifacts') | Out-Null
            $report = & dotnet (Join-Path $projectRoot 'Redesign-Evolution/SimRunner/bin/Debug/net8.0/Redesign.SimRunner.dll') --benchmark
            if ($LASTEXITCODE -ne 0) { throw 'Combat benchmark failed.' }
            $report | Set-Content -Encoding UTF8 -LiteralPath (Join-Path $projectRoot '.artifacts/spatial-benchmark.json')
            Write-Output $report
        }
        'capture' {
            Build-Game
            New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.artifacts') | Out-Null
            Invoke-Checked $godotPath @('--path', $gamePath, '--resolution', '1440x900', '--', '--demo', ('--capture=' + (Join-Path $projectRoot '.artifacts/emberwatch.png')))
        }
        'export' {
            Build-Game
            New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.artifacts/windows') | Out-Null
            Invoke-Checked $godotPath @('--headless', '--path', $gamePath, '--editor', '--import')
            Invoke-Checked $godotPath @('--headless', '--path', $gamePath, '--export-release', 'Windows Desktop')
            Test-Export
            $packagePath = Join-Path $projectRoot '.artifacts/windows'
            Invoke-Checked $godotPath @('--headless', '--path', $gamePath, '--', ('--write-notices=' + (Join-Path $packagePath 'GODOT-NOTICES.txt')))
            Copy-Item -LiteralPath (Join-Path $projectRoot 'docs/PLAYABLE_BUILD.md') -Destination (Join-Path $packagePath 'README.md')
            $runtimePath = Join-Path $env:NUGET_PACKAGES 'microsoft.netcore.app.runtime.win-x64/8.0.30'
            foreach ($noticeFile in @('LICENSE.TXT','THIRD-PARTY-NOTICES.TXT')) {
                $sourceNotice = Join-Path $runtimePath $noticeFile
                if (Test-Path -LiteralPath $sourceNotice) { Copy-Item -LiteralPath $sourceNotice -Destination (Join-Path $packagePath ('DOTNET-' + $noticeFile)) }
            }
            $zipPath = Join-Path $projectRoot '.artifacts/Redesign-Evolution-Emberwatch-Windows.zip'
            Compress-Archive -LiteralPath $packagePath -DestinationPath $zipPath -Force
            $zipHash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToLowerInvariant()
            ($zipHash + '  Redesign-Evolution-Emberwatch-Windows.zip') | Set-Content -Encoding ASCII -LiteralPath ($zipPath + '.sha256')
            Write-Output ('Packaged Windows build: ' + $zipPath + ' / SHA256 ' + $zipHash)
        }
    }
} finally { Pop-Location }
