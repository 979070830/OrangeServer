

::Э���ļ�·��, ���Ҫ����\������
set SOURCE_FOLDER=.\protofile

::Java������·��
set JAVA_COMPILER_PATH=tool\protoc-3.6.1-win32\bin\protoc.exe


::Java�ļ�����·��, ���Ҫ����\������
set JAVA_PATH=..\..\src

::ɾ��֮ǰ�������ļ�
del %JAVA_PATH%\com\google\protobuf\*.* /f /s /q

::pause

::���������ļ�
for /f "delims=" %%i in ('dir /b "%SOURCE_FOLDER%\*.proto"') do (

::���� Java ����
echo %JAVA_COMPILER_PATH% --java_out=%JAVA_PATH% %SOURCE_FOLDER%\%%i
%JAVA_COMPILER_PATH% --java_out=%JAVA_PATH% %SOURCE_FOLDER%\%%i 
 
)

echo Э���������

pause