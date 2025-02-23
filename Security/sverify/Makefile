#
# Makefile for UNIX
#
#

CC=gcc

# Compiler directives and other goodies
SYSTEM = UNIX
DEBUG = -g
OPTIMIZE = -O2 
CFLAGS = $(OPTIMIZE)

all: hashtest

hashtest: hashtest.o rmd160.o
	$(CC) $(CFLAGS) hashtest.o rmd160.o -o hashtest

rmd160.o: rmd160.c rmd160.h 
	$(CC) $(CFLAGS) -c rmd160.c

sentinel.o: hashtest.c 
	$(CC) $(CFLAGS) -c hashtest.c

clean:
	rm -f *.o 
