@echo off
echo Starting server at http://localhost:8080
echo Open in browser: http://localhost:8080/index.html
echo Press Ctrl+C to stop.
cd /d "%~dp0"
python -m http.server 8080
pause
