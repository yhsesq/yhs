# Makefile for EXEPAK

CC=gcc
CFLAGS=-O2 -m386 -fomit-frame-pointer -s -pipe -fstrength-reduce -Wall -I../include
LDFLAGS=-s
STUBFLAGS=-nostartfiles -nostdlib
STRIP=strip --strip-all --remove-section=.note --remove-section=.comment

#
# Compile with the encrypt feature of exepak
#
# CFLAGS += -DDO_ENCRYPT

all: exepak exepak_stub exepak_stubencode

exepak: exepak.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o exepak exepak.c ../src/.libs/liblzo.a
	$(STRIP) exepak

exepak_stub: stub.c
	$(CC) $(CFLAGS) $(STUBFLAGS) -o exepak_stub stub.c ../src/.libs/liblzo.a

exepak_stubencode: stubencode.c
	$(CC) $(CFLAGS) -o exepak_stubencode stubencode.c
	./exepak_stubencode

clean:
	rm -f exepak exepak_stub exepak_stubencode stubcode.h
