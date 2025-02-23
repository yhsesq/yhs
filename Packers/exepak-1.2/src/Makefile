# Makefile for EXEPAK

CC=gcc
#CFLAGS=-g
CFLAGS=-O2 -fomit-frame-pointer -s -pipe -fstrength-reduce
LDFLAGS=-s
STRIP=strip --strip-all

exepak:
	$(CC) $(CFLAGS) $(LDFLAGS) -o exepak exepak.c ../lzo/src/.libs/liblzo.a
	$(STRIP) exepak

all: exepak

clean:
	rm -f exepak
