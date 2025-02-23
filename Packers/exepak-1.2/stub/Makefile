# Makefile for EXEPAK stub
# (c)1997 Adam Ierymenko

CC=gcc
CFLAGS=-O2 -fomit-frame-pointer -s -pipe -fstrength-reduce
STRIP=strip --strip-all
#CFLAGS=-g

all: stub

stub:
	$(CC) $(CFLAGS) -o exepak_stub stub.c ../lzo/src/.libs/liblzo.a
	$(STRIP) exepak_stub
	rm -f *.o

stubencode:
	$(CC) $(CFLAGS) -o exepak_stubencode stubencode.c
	./exepak_stubencode

stubber:
	rm -f exepak_stubencode __stub.h stubsize.h
	ls -al exepak_stub | awk '{print "#define EXEPAK_STUBSIZE " $$5}' >>stubsize.h
	rm -f exepak_stub 

clean:
	rm -f exepak_stub exepak_stubencode __stub.h

