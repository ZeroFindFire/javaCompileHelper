@echo off
set c1=%~f1
set c2=%~f2
if "%c1%"=="" goto errorMsg
if "%c2%"=="" goto errorMsg
java -cp %ZFF_HOME%\jcmp;D:\jar\gson-2.3.jar Cmp %~f1 %~f2 %~f3
goto end
:errorMsg
echo you should input source file and output file, could also input settings file
:end
@echo on