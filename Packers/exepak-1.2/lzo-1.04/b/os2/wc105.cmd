rem /* OS/2 32 bit - Watcom C 10.5
rem  * a very simple make driver
rem  * Copyright (C) 1996, 1997, 1998 Markus F.X.J. Oberhumer
rem  */

@if "%LZO_ECHO%"=="n" echo off

set _CC=wcl386 -zq -mf -5r -bt#os2v2
set CFLAGS=-Iinclude -wx -oneatx
set MYLIB=lzo.lib

echo Compiling, please be patient...
set CC=%_CC%
%CC% %CFLAGS% -zc -c src\*.c
@if errorlevel 1 goto error
%CC% -wx -c src\i386\d_asm2\*.asm
@if errorlevel 1 goto error
if exist %MYLIB% del %MYLIB%
wlib -q -b -n -t %MYLIB% @b\os2\wc105.rsp
@if errorlevel 1 goto error

set CC=%_CC% -zc -l#os2v2
%CC% %CFLAGS% ltest\ltest.c %MYLIB%
@if errorlevel 1 goto error

%CC% %CFLAGS% examples\dict.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% examples\lpack.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% examples\overlap.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% examples\precomp.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% examples\precomp2.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% examples\simple.c %MYLIB%
@if errorlevel 1 goto error

%CC% %CFLAGS% -Isrc tests\align.c %MYLIB%
@if errorlevel 1 goto error
%CC% %CFLAGS% -Isrc tests\chksum.c %MYLIB%
@if errorlevel 1 goto error

%CC% %CFLAGS% -fe#testmini.exe minilzo\*.c
@if errorlevel 1 goto error

echo Done.
goto end
:error
echo error!
:end
