# /* DOS 16 bit - Watcom C 10.5
#  * Copyright (C) 1996, 1997, 1998 Markus F.X.J. Oberhumer
#  * type `make -f b/dos16/wc105.mak'
#  */

override b_dos16 = true
liblzo = lzo.lib
o = .obj
exe = .exe

CC = wcl -zq -ml -5 -bt=dos
CFLAGS          = -Iinclude -wx -oneatx
## CFLAGS         += -D__LZO_STRICT_16BIT
## CFLAGS         += -DLZO_DEBUG
LZO_COMPILE_C   = $(CC) $(CFLAGS) -zc -c $(<bs)
LZO_COMPILE_ASM = $(CC) -c $(<bs)
LZO_ARLIB       = wlib -q -b -n -t $(@bs) @b\\dos16\\wc105.rsp
LZO_LINK        = $(CC) -l=dos -fe=$(@bs) $(^bs)

VPATH = .;src;ltest;examples;tests
include b/lzo.mk

