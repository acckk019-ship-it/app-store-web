@echo off
title App Store Admin Helper
cd /d "%~dp0"
echo Starting App Store Helper...
echo Press Ctrl+C in the terminal to stop the helper server.
powershell -NoProfile -ExecutionPolicy Bypass -File .\AppStoreHelper.ps1
pause
