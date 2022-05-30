@echo off
set DIR="%~dp0"
set JAVA_EXEC="%DIR:"=%\bin\javaw"



pushd %DIR% & start "image2C" %JAVA_EXEC% %CDS_JVM_OPTS%  -p "%~dp0/../app" -m image2C/image2C.views.ImageApp  %* & popd
