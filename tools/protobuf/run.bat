

::协议文件路径, 最后不要跟“\”符号
set SOURCE_FOLDER=.\protofile

::Java编译器路径
set JAVA_COMPILER_PATH=tool\protoc-3.6.1-win32\bin\protoc.exe


::Java文件生成路径, 最后不要跟“\”符号
set JAVA_PATH=..\..\src

::删除之前创建的文件
del %JAVA_PATH%\com\google\protobuf\*.* /f /s /q

::pause

::遍历所有文件
for /f "delims=" %%i in ('dir /b "%SOURCE_FOLDER%\*.proto"') do (

::生成 Java 代码
echo %JAVA_COMPILER_PATH% --java_out=%JAVA_PATH% %SOURCE_FOLDER%\%%i
%JAVA_COMPILER_PATH% --java_out=%JAVA_PATH% %SOURCE_FOLDER%\%%i 
 
)

echo 协议生成完毕

pause