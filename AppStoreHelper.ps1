# AppStoreHelper.ps1
# Set UTF-8 encoding for console output
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Ensure folders exist
$baseDir = Get-Location
$uploadsDir = Join-Path $baseDir "uploads"
$iconsDir = Join-Path $uploadsDir "icons"
$bannersDir = Join-Path $uploadsDir "banners"
$screensDir = Join-Path $uploadsDir "screenshots"
$appsDir = Join-Path $uploadsDir "apps"

New-Item -ItemType Directory -Force -Path $iconsDir | Out-Null
New-Item -ItemType Directory -Force -Path $bannersDir | Out-Null
New-Item -ItemType Directory -Force -Path $screensDir | Out-Null
New-Item -ItemType Directory -Force -Path $appsDir | Out-Null

$appsJsonPath = Join-Path $baseDir "apps.json"

# MIME Types mapping
$mimeTypes = @{
    ".html" = "text/html; charset=utf-8"
    ".css"  = "text/css; charset=utf-8"
    ".js"   = "application/javascript; charset=utf-8"
    ".json" = "application/json; charset=utf-8"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".jpeg" = "image/jpeg"
    ".gif"  = "image/gif"
    ".svg"  = "image/svg+xml"
    ".zip"  = "application/zip"
    ".exe"  = "application/x-msdownload"
    ".msi"  = "application/octet-stream"
}

# Start HTTP Listener
$port = 8080
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$port/")
try {
    $listener.Start()
} catch {
    Write-Host "Error starting HTTP listener on port $port: $_"
    exit 1
}

Write-Host "========================================================"
Write-Host "  App Store Local Admin Helper is running"
Write-Host "  URL: http://localhost:$port/index.html"
Write-Host "========================================================"
Write-Host "  - Uploads are saved to the local folder."
Write-Host "  - Code changes are automatically committed & pushed."
Write-Host "  - Passcode is read from config.json."
Write-Host "  - Close this window or press Ctrl+C to stop."
Write-Host "========================================================"

# Open local page in browser
Start-Process "http://localhost:$port/index.html"

# Helper function to parse Base64 data URLs and save to files
function Save-Base64File($base64Data, $targetFolder, $filenamePrefix, $defaultExt) {
    if ([string]::IsNullOrEmpty($base64Data)) { return "" }
    
    # regex to split mime type and base64 string
    if ($base64Data -match '^data:(?<mime>[^;]+);base64,(?<data>.+)$') {
        $mime = $Matches.mime
        $data = $Matches.data
        
        # map mime to extension
        $ext = $defaultExt
        foreach ($key in $mimeTypes.Keys) {
            if ($mimeTypes[$key].StartsWith($mime)) {
                $ext = $key
                break
            }
        }
        
        # Sanitize filename prefix
        $safePrefix = $filenamePrefix -replace '[^a-zA-Z0-9_\-]', '_'
        $filename = "${safePrefix}_$((Get-Date).Ticks)$ext"
        $filePath = Join-Path $targetFolder $filename
        $bytes = [System.Convert]::FromBase64String($data)
        [System.IO.File]::WriteAllBytes($filePath, $bytes)
        
        # return relative path for web use (Windows backslash to web forward slash)
        $relPath = $filePath.Replace($baseDir, "").Replace("\", "/").TrimStart("/")
        return $relPath
    }
    return $base64Data # return as-is if it's already a URL
}

# Helper to load and validate passcode from config.json
function Test-Passcode($clientPasscode) {
    $configPath = Join-Path $baseDir "config.json"
    if (Test-Path $configPath) {
        try {
            $config = Get-Content $configPath -Raw | ConvertFrom-Json
            if ($config.passcode) {
                return $config.passcode.ToString().Trim() -eq $clientPasscode.ToString().Trim()
            }
        } catch {
            Write-Host "Warning: Failed to parse config.json. Rejecting passcode."
        }
    }
    # Fallback default passcode if config doesn't exist/is broken
    return "1234" -eq $clientPasscode.ToString().Trim()
}

while ($listener.IsListening) {
    try {
        $context = $listener.GetContext()
    } catch {
        # Listener was stopped
        break
    }
    
    $req = $context.Request
    $res = $context.Response
    
    $rawPath = $req.Url.LocalPath
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $($req.HttpMethod) $rawPath"
    
    if ($req.HttpMethod -eq "GET") {
        # Serve static files
        $relPath = $rawPath.TrimStart("/")
        if ([string]::IsNullOrEmpty($relPath)) { $relPath = "index.html" }
        
        $fullPath = Join-Path $baseDir $relPath
        
        # Prevent directory traversal attack
        if (-not $fullPath.StartsWith($baseDir)) {
            $res.StatusCode = 403
            $res.Close()
            continue
        }
        
        if (Test-Path $fullPath -PathType Leaf) {
            $ext = [System.IO.Path]::GetExtension($fullPath).ToLower()
            $mime = $mimeTypes[$ext]
            if (-not $mime) { $mime = "application/octet-stream" }
            $res.ContentType = $mime
            
            try {
                $bytes = [System.IO.File]::ReadAllBytes($fullPath)
                $res.ContentLength64 = $bytes.Length
                $res.OutputStream.Write($bytes, 0, $bytes.Length)
            } catch {
                $res.StatusCode = 500
            }
        } else {
            $res.StatusCode = 404
            $errorMessage = "File not found"
            $errBytes = [System.Text.Encoding]::UTF8.GetBytes($errorMessage)
            $res.OutputStream.Write($errBytes, 0, $errBytes.Length)
        }
        $res.Close()
    }
    elseif ($req.HttpMethod -eq "POST" -and $rawPath -eq "/api/upload") {
        # Handle new app upload
        try {
            $reader = New-Object System.IO.StreamReader($req.InputStream, [System.Text.Encoding]::UTF8)
            $body = $reader.ReadToEnd()
            $app = ConvertFrom-Json $body
            
            # Validate passcode
            if (-not (Test-Passcode $app.passcode)) {
                Write-Host "Upload rejected: Invalid passcode."
                $res.StatusCode = 401
                $res.ContentType = "application/json"
                $responseObj = @{ success = $false; error = "Invalid admin passcode." }
                $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
                $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
                $res.Close()
                continue
            }
            
            Write-Host "Processing upload for: $($app.name)"
            
            # Save files and get relative paths
            $iconPath = Save-Base64File $app.icon $iconsDir "$($app.name)_icon" ".png"
            $bannerPath = Save-Base64File $app.banner $bannersDir "$($app.name)_banner" ".png"
            
            $screenshotPaths = @()
            $idx = 1
            foreach ($s in $app.screenshots) {
                $sUrl = Save-Base64File $s $screensDir "$($app.name)_screen_$idx" ".png"
                if ($sUrl) { $screenshotPaths += $sUrl }
                $idx++
            }
            
            $appFilePath = ""
            if ($app.file) {
                $appFilePath = Save-Base64File $app.file $appsDir "$($app.name)_app" ".zip"
            }
            
            # Final download URL
            $finalUrl = $appFilePath
            if ([string]::IsNullOrEmpty($finalUrl)) {
                $finalUrl = $app.url
            }
            
            # Read existing apps.json
            $existingApps = @()
            if (Test-Path $appsJsonPath) {
                $existingApps = Get-Content $appsJsonPath -Raw | ConvertFrom-Json
            }
            
            # Create new app object
            $newApp = @{
                id = "app-$((Get-Date).Ticks)"
                name = $app.name
                developer = $app.developer
                category = $app.category
                platform = $app.platform
                icon = $iconPath
                banner = $bannerPath
                screenshots = $screenshotPaths
                description = $app.description
                rating = $null
                ratingCount = "0"
                downloads = "0"
                versions = @(
                    @{
                        version = $app.version
                        notes = if ($app.notes) { $app.notes } else { "Initial release." }
                        url = $finalUrl
                        size = if ($app.size) { $app.size } else { "Unknown size" }
                        date = (Get-Date -Format "yyyy-MM-dd")
                    }
                )
            }
            
            # Prepend new app to array
            $updatedApps = ,$newApp + $existingApps
            
            # Save back to apps.json
            $updatedApps | ConvertTo-Json -Depth 10 | Out-File -FilePath $appsJsonPath -Encoding utf8
            
            Write-Host "App saved locally. Committing and pushing to GitHub..."
            
            # Git integration
            git add .
            git commit -m "Upload app: $($app.name) v$($app.version)"
            git push
            
            Write-Host "Sync complete!"
            
            $res.ContentType = "application/json"
            $responseObj = @{ success = $true }
            $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
            $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
        } catch {
            Write-Host "Error during upload: $_"
            $res.StatusCode = 500
            $res.ContentType = "application/json"
            $responseObj = @{ success = $false; error = $_.Exception.Message }
            $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
            $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
        }
        $res.Close()
    }
    elseif ($req.HttpMethod -eq "POST" -and $rawPath -eq "/api/add-version") {
        # Handle adding a version to an existing app
        try {
            $reader = New-Object System.IO.StreamReader($req.InputStream, [System.Text.Encoding]::UTF8)
            $body = $reader.ReadToEnd()
            $payload = ConvertFrom-Json $body
            
            # Validate passcode
            if (-not (Test-Passcode $payload.passcode)) {
                Write-Host "Add version rejected: Invalid passcode."
                $res.StatusCode = 401
                $res.ContentType = "application/json"
                $responseObj = @{ success = $false; error = "Invalid admin passcode." }
                $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
                $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
                $res.Close()
                continue
            }
            
            Write-Host "Adding version $($payload.version) for App ID: $($payload.appId)"
            
            # Read existing apps.json
            if (-not (Test-Path $appsJsonPath)) {
                throw "apps.json database not found."
            }
            $existingApps = Get-Content $appsJsonPath -Raw | ConvertFrom-Json
            
            # Find and update the app
            $found = $false
            foreach ($app in $existingApps) {
                if ($app.id -eq $payload.appId) {
                    $found = $true
                    
                    # Create new version object
                    $newVersion = @{
                        version = $payload.version
                        notes = if ($payload.notes) { $payload.notes } else { "Version update." }
                        url = if ($payload.url) { $payload.url } else { "#" }
                        size = if ($payload.size) { $payload.size } else { "" }
                        date = (Get-Date -Format "yyyy-MM-dd")
                    }
                    
                    # Prepend new version
                    $app.versions = ,$newVersion + $app.versions
                    break
                }
            }
            
            if (-not $found) {
                throw "App with ID $($payload.appId) not found."
            }
            
            # Save back to apps.json
            $existingApps | ConvertTo-Json -Depth 10 | Out-File -FilePath $appsJsonPath -Encoding utf8
            
            Write-Host "Version saved locally. Committing and pushing to GitHub..."
            
            # Git integration
            git add .
            git commit -m "Add version $($payload.version) to app ID $($payload.appId)"
            git push
            
            Write-Host "Sync complete!"
            
            $res.ContentType = "application/json"
            $responseObj = @{ success = $true }
            $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
            $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
        } catch {
            Write-Host "Error during add-version: $_"
            $res.StatusCode = 500
            $res.ContentType = "application/json"
            $responseObj = @{ success = $false; error = $_.Exception.Message }
            $resBytes = [System.Text.Encoding]::UTF8.GetBytes(($responseObj | ConvertTo-Json))
            $res.OutputStream.Write($resBytes, 0, $resBytes.Length)
        }
        $res.Close()
    } else {
        $res.StatusCode = 405
        $res.Close()
    }
}
