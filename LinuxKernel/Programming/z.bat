edit zdoc.c
erase zdoc.prc
c:\Programming\gnu\pilrc zdoc.rcp
m68k-palmos-coff-gcc -O3 zdoc.c -o zdoc
m68k-palmos-coff-obj-res zdoc
build-prc zdoc.prc " Z-DOC " ZURK *.grc *.bin
erase *.grc
erase *.bin
erase zdoc
