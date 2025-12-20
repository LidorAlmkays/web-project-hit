@echo off
setlocal

set MAVEN_HOME=%~dp0apache-maven-3.9.11
set PATH=%MAVEN_HOME%\bin;%PATH%

mvn %*

