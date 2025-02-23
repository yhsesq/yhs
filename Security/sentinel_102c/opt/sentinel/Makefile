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

all: sentinel

sentinel: sentinel.o rmd160mc.o

rmd160.o: rmd160mc.c rmd160mc.h 
	$(CC) $(CFLAGS) -c rmd160.c

sentinel.o: sentinel.c 
	$(CC) $(CFLAGS) -c sentinel.c

clean:
	rm -f *.o 
