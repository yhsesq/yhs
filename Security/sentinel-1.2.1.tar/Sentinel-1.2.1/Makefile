#
# Makefile for UNIX
#
#

CC=gcc

# Compiler directives and other goodies
SYSTEM = UNIX
DEBUG = -g
OPTIMIZE = -O2 -static
CFLAGS = $(OPTIMIZE)

all: sentinel gsentinel sentineld clean

sentinel: sentinel.o rmd160mc.o
	$(CC) $(CFLAGS) sentinel.o rmd160mc.o -o sentinel

rmd160.o: rmd160mc.c rmd160mc.h 
	$(CC) $(CFLAGS) -c rmd160.c

sentinel.o: sentinel.c sentinel.h
	$(CC) $(CFLAGS) -c sentinel.c

gsentinel: gsentinel.c sentinel.h
	$(CC) $(CFLAGS) -g gsentinel.c `gtk-config --cflags --libs` -o gsentinel

sentineld: sentineld.c sentinel.h
	$(CC) $(CFLAGS) sentineld.c -o sentineld

clean:
	rm -f *.o 

genheader:
	./sentinel -createheaderfile

compress:
	exepak-1.2/exepak -e sentinel
	cd ..
	rm -r -f exepak-1.2/

distclean:
	rm -f sentinel.1
	rm -f sentinel.1.bak
	rm -f sentinel.2
	rm -f sentinel.2.bak
	rm -f sentinel.sig.*
	rm -f sentinel.lock
	rm -f sentinel.tmp
	rm -f sentinel.tmpx
	rm -f sentineld
	rm -f gsentinel
	rm -f sentinel

uninstall:
	rm -r -f /opt/sentinel

install:
	mkdir -p /opt/sentinel
	cp -f sentinel /opt/sentinel
	cp -f sentinel.conf /opt/sentinel

