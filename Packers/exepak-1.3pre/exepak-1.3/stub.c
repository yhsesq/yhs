/********************************************************************
 *
 * EXEPAK self-extractor code stub (c)1997 Adam Ierymenko
 * Changes (c) 1999 Zurk (zurk@geocities.com) R.Georgi (georgi@falcom.de)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *********************************************************************/

/* Warning: this stub code is written to be as small and fast as possible,
 * not to be pretty.  Modifiers beware. */

#include <stdio.h>
#include <malloc.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/mman.h>
#include <syscall.h>
#include <fcntl.h>
#include <time.h>

#include "../include/lzoconf.h"
#include "../include/lzo1z.h"
#include "my_unistd.h"

/* Define this to the file we should open to get the original program */
#define TEMP_EXE dfname+11   

/* Marker */
char marker[] = "EPK13";

char **environ = NULL;
int errno = 0;
extern unsigned int mapcompressed[];

/* Saves a bit of space rather than having this in every string */

void error(char *what)
{
    write(2,"exepak: ",8);
    write(2,what,strlen(what));
    exit(1);
}

void tempfile(char *bp, char *init)
{
    const char digits[16] = "0123456789abcdef";
    unsigned int pid = getpid();

    /* copy file name */
    do {
        *bp++ = *init++;
    } while( *init );
    /* copy pid */
    do {
  	    *--bp = digits[pid&15];
    } while( pid /= 16 );
}

/*
 * strcat(), Add one string to another
 */
char *my_strcat(char *dest, const char *src)
{
	char *p;

	for (p = dest; *p; ++p) ;
	while( (*p++ = *src++) ) ;
	return(dest);
}

/* 
 * Everything here is done in main() to minimize code size 
 * We also use goto's in here to call redundant code to save space  
 */
int main(int argc,char *argv[])
{
    static int mmap_args[] = { 0, 0, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0 };
	int  i, tempfd;
	char dfname[32];
    char *writebuf, *readbuf = (char *)&mapcompressed[2];
    unsigned int destlen = 0, srclen = 0;
    unsigned int decomplen;
	char dontspawn = 0;

	/* 
     * Init the LZO library (this should never fail), This functions checks 
     * basic architecture issues of the LZO library. If the compression was 
     * succesfull this call will also be successfull. In any case makes the 
     * binary bigger and is not currently necessary for the decompress function. 
     */
#if 0
	if( lzo_init() != LZO_E_OK )
		exit(1); 
#endif

	/* 
     * Check the arguments for a first argument ordering us 
     * to decompress to stdout 
     */
	if( argc == 2 && !strcmp(argv[1],">d") ) 
        dontspawn = 1;
    else {
    	/* Open the temporary executable (unless we're outputting to stdout) */
        tempfile(dfname,"can't open /tmp/.00000000");
		if( (tempfd = open(TEMP_EXE,O_WRONLY|O_CREAT|O_TRUNC,mapcompressed[1])) < 0 ) {
            my_strcat(dfname,"\n");
            error(dfname);
		}
	}	
#ifdef DO_ENCRYPT
	/* First thing we do is read the 'encrypted' status byte */
	if( mapcompressed[0] && dontspawn ) 
    	error("encrypted file\n");
    if( mapcompressed[0] ) {
	    /* If it's "encrypted", binary 'NOT' each byte */
       	for( i=0;i<mapcompressed[0];i+=sizeof(int) )
	    	*(unsigned int*)&readbuf[i] = ~*(unsigned int*)&readbuf[i];
        /* eof marker for decompress */
        *(unsigned int*)&readbuf[mapcompressed[0]] = 0;
    }
#endif
	/* 
     * The compressed data in the compressed executable is blocked, with
	 * each compressed block being preceded with two 32-bit integers in
	 * whatever byte order this host uses telling the size of the compressed
	 * block and the size of the decompressed block. 
     */
    while( 1 ) {
        /* [0] = comp block size, [1] = decomp block size */	
        if( !(srclen = *(unsigned int*)readbuf) ) 
            break;
        readbuf += sizeof(unsigned int);
        destlen = *(unsigned int*)readbuf;
        readbuf += sizeof(unsigned int);
        /* alloc decompresion buffer */
		if( !mmap_args[1] ) {
            mmap_args[1] = destlen+4096;
            if( (writebuf = (char *)mmap(&mmap_args)) < 0 ) {
            	unlink(TEMP_EXE);
				error("out of memory\n");
            }
		} 
		/* Decompress and sanity check */
        if( lzo1z_decompress(readbuf,srclen,writebuf,&decomplen,(void *)0) != LZO_E_OK || destlen != decomplen )
			goto executable_corrupted;
		/* Write decompressed data */
		if( dontspawn )
			write(1,writebuf,destlen);
		else if( write(tempfd,writebuf,destlen) != destlen ) {
			unlink(TEMP_EXE);
			error("write error\n");
		}
        /* goto to next input block */
        readbuf += srclen;
	}
	/* Make sure we actually did something */
	if( !destlen ) {
executable_corrupted:
		unlink(TEMP_EXE);
		error("corrupt file\n");
    }    
	/* We don't need this anymore */
	munmap(writebuf,mmap_args[1]);
    close(tempfd);

	/* If we've been ordered to permanently decompress, don't spawn */
	if( dontspawn )	goto exitus; 
	/* 
     * Fork off a subprocess to clean up
	 * We have to do this stupid double-fork trick to keep a zombie from
	 * hanging around if the spawned original program doesn't check for
	 * subprocesses.  (As well as to prevent the real program from getting
	 * confused about this subprocess it shouldn't have) 
     */
	if( !fork() ) {
		if( !fork() ) {
            struct timeval t = { 0,100000 };
            struct select_args {
                int fd;
                fd_set *r,*w,*e;
                struct timeval *t; 
            } sleep_time = { 0,NULL,NULL,NULL,&t };
			select(&sleep_time);
			unlink(TEMP_EXE);
		}
		return 0; 
	}
	/* Wait for the first fork()'d process to die */
	waitpid(-1,NULL,0);
	/* Spawn the original process */
	execve(TEMP_EXE,argv,environ);
exitus:
    return 1;
}
