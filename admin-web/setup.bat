@echo off
REM Medical B2B Admin Panel - Quick Setup Script
REM Run this script to set up the project quickly

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║   Medical B2B Admin Panel - Setup Script                  ║
echo ║   This will configure environment and install dependencies ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js is not installed!
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo ✅ Node.js detected: %node --version%
echo.

REM Check if npm is installed
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ npm is not installed!
    echo Please install Node.js (npm comes with it)
    pause
    exit /b 1
)

echo ✅ npm detected: %npm --version%
echo.

REM Install dependencies
echo 📦 Installing dependencies...
echo This may take a few minutes...
echo.
npm install

if errorlevel 1 (
    echo ❌ Failed to install dependencies
    pause
    exit /b 1
)

echo.
echo ✅ Dependencies installed successfully!
echo.

REM Check if .env exists
if exist .env (
    echo ✅ .env file exists
) else (
    echo.
    echo ⚠️  .env file not found
    echo Creating .env from template...
    copy .env.example .env
    echo.
    echo 📝 Please edit .env file with your email credentials:
    echo    1. Open .env in a text editor
    echo    2. Fill in EMAIL_PROVIDER and credentials
    echo    3. Save the file
    echo.
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║   Setup Complete! Next Steps:                             ║
echo ║                                                            ║
echo ║   1. Edit .env file with your email credentials           ║
echo ║                                                            ║
echo ║   2. Open two terminals:                                  ║
echo ║      Terminal 1: node server.js    (Email Backend)        ║
echo ║      Terminal 2: npx serve -l 8080 (Web Server)           ║
echo ║                                                            ║
echo ║   3. Open: http://localhost:8080                          ║
echo ║                                                            ║
echo ║   4. Login or Register & Test                             ║
echo ║                                                            ║
echo ║   For detailed instructions:                              ║
echo ║   Read: APPROVAL_WORKFLOW_SETUP.md                        ║
echo ║                                                            ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

pause
