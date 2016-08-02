@echo off

@for /f %%i in ('cd') do set PWD=%%i

@title Git-Bash@%PWD%


c:\Windows\system32\cmd.exe /c ""C:\Program Files\Git\git-bash.exe" --cd=%PWD%"

