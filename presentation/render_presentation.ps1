$ErrorActionPreference = 'Stop'
$taskDeck = Join-Path $PSScriptRoot 'Placement_Companion_Lab_Presentation.pptx'
$taskPdf = Join-Path $PSScriptRoot 'Placement_Companion_Lab_Presentation.pdf'
$taskPreview = Join-Path $PSScriptRoot 'preview'
New-Item -ItemType Directory -Force $taskPreview | Out-Null
$taskPowerPoint = New-Object -ComObject PowerPoint.Application
$taskPresentation = $null
try {
    # Open read-only, without a presentation window.
    $taskPresentation = $taskPowerPoint.Presentations.Open($taskDeck, -1, 0, 0)
    $taskPresentation.SaveAs($taskPdf, 32)
    foreach ($taskSlide in $taskPresentation.Slides) {
        $taskSlide.Export((Join-Path $taskPreview ("slide-{0:00}.png" -f $taskSlide.SlideIndex)), 'PNG', 1600, 900)
    }
    Write-Output "Rendered $($taskPresentation.Slides.Count) slides and PDF."
} finally {
    if ($taskPresentation) { $taskPresentation.Close() }
    # Preserve any presentation the user has open in a shared PowerPoint instance.
    if ($taskPowerPoint.Presentations.Count -eq 0) { $taskPowerPoint.Quit() }
    [void][Runtime.InteropServices.Marshal]::FinalReleaseComObject($taskPowerPoint)
}
