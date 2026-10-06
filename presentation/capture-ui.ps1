param([string]$Shot = '')
$ErrorActionPreference = 'Stop'
$taskAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $taskAdb -s emulator-5554 shell uiautomator dump /sdcard/placement-ppt-ui.xml | Out-Null
& $taskAdb -s emulator-5554 pull /sdcard/placement-ppt-ui.xml "$env:TEMP\placement-ppt-ui.xml" | Out-Null
[xml]$taskUi = Get-Content "$env:TEMP\placement-ppt-ui.xml"
$taskUi.SelectNodes('//node') | Where-Object { $_.text -or $_.'content-desc' -or $_.class -match 'EditText' } | Select-Object text,bounds,content-desc
if ($Shot) {
    & $taskAdb -s emulator-5554 shell screencap -p /sdcard/placement-ppt.png
    & $taskAdb -s emulator-5554 pull /sdcard/placement-ppt.png (Join-Path $PSScriptRoot "assets\$Shot.png")
}
