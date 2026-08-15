@echo off
REM Convenience launcher for Windows. Creates a venv on first run, then starts the companion.

if not exist .venv (
    python -m venv .venv
    call .venv\Scripts\activate.bat
    pip install -r requirements.txt
) else (
    call .venv\Scripts\activate.bat
)

python -m companion.app %*
