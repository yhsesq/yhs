##################################################
#                                                #
#   Makefile for CT Image Application Program    #
#                                                #
##################################################
# Some common definitions...

RM = rm -f
CXX = cc

# Compiler flags, paths for include files and libraries

CFLAGS = -v -static -fwritable-strings
INCLUDES = -I. -I/usr/include -I/usr/X11R6/include -I/usr/include/sys \
-I/usr/X11R6/lib -I/usr/X11R6
XMLIBS = -L/usr/lesstif/lib -L/usr/X11R6/lib -lMrm  -lXm -lXt -lXmu -lX11 -lm -lSM -lICE 

# /usr/X11R6/lib/libXm.so /usr/X11R6/lib/libXt.so \
# /usr/X11R6/lib/libX11.so

# -lX11 -lXext -lXt -lICE -lSM -lFS -lXm -lm

# -l Xmu

FILES = slice.o \
        label.o \
        menu.o \
        open.o \
        save.o \
        exit.o \
        measure.o \
        region.o \
        edge.o \
        ctomass.o \
	watershed.o \
	editing_tools.o \
	region_ops.o \
	zoom.o \
	split_merge.o \
	neighbour.o \
        print.o \
	filters.o \
	version.o

# Rule to create .o files from .c files
.c.o:
	$(RM) $@
	$(CXX) -c $(CFLAGS) $(INCLUDES) $*.c
	
# Targets...

all::	slice

slice: $(FILES)
	$(RM) $@
	$(CXX) -o $@ $(CFLAGS) $(INCLUDES) $(FILES) $(XMLIBS)  -lm
	
