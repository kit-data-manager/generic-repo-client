@ECHO OFF
REM *************************************************************************** 
REM *************************************************************************** 
REM ****       Batch script for Ingest via Drag'n Drop                     **** 
REM *************************************************************************** 
REM ****                                                                   **** 
REM ****       Don't move this file.                                       **** 
REM ****       You may create a shortcut on the desktop.                   **** 
REM ****                                                                   **** 
REM *************************************************************************** 
REM *************************************************************************** 

REM Change drive if necessary
%~d0%

REM Change directory
CD %~dp0%\..

REM Call ingest for directories
CALL bin\repoClient.bat ingest -i %* -n "Upload via Drag'n Drop"

CD bin

REM Wait for input before closing terminal
SET /p id="Press RETURN to close terminal!"
