@echo off  
setlocal enabledelayedexpansion  

REM 设置要查找的进程名  
set "processName=cmd.exe"  
  
REM 使用 tasklist 获取 PID 列表，并使用 for /f 循环遍历它们  
for /f "tokens=2 delims=, " %%a in ('tasklist  /fi "IMAGENAME eq %processName%"  /fo csv ') do (  
if %%a neq "PID" (
    set "pid=%%a"  
    echo 结束进程 PID: !pid!  
    taskkill /PID !pid! /F  
)
)  

endlocal
