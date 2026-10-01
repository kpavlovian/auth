@echo off
chcp 65001 >nul
if not exist out mkdir out
javac -encoding UTF-8 -d out src\auth\*.java
xcopy /E /I /Y resources\captcha out\captcha >nul
java -Dfile.encoding=UTF-8 -cp "out;lib\sqlite-jdbc.jar" auth.Main
pause
