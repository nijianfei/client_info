@echo off  
setlocal enabledelayedexpansion  

REM ����Ҫ���ҵĽ�����  
set "processName=cmd.exe"  
  
REM ʹ�� tasklist ��ȡ PID �б���ʹ�� for /f ѭ����������  
for /f "tokens=2 delims=, " %%a in ('tasklist  /fi "IMAGENAME eq %processName%"  /fo csv ') do (  
if %%a neq "PID" (
    set "pid=%%a"  
    echo �������� PID: !pid!  
    taskkill /PID !pid! /F  
)
)  

endlocal
