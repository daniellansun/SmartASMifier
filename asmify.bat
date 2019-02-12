@echo off
set GROOVY_TURN_OFF_JAVA_WARNINGS=true
call groovy %~dp0\SmartASMifier.groovy %*
