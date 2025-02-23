/* EXEPAK self-extractor code stub */
/* (c)1997 Adam Ierymenko */

/* Modifications (C) 1999 Zurk (zurk@geocities.com */ 
/********************************************************************
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
 ********************************************************************
 */

/* Warning: this stub code is written to be as small and fast as possible,
 * not to be pretty.  Modifiers beware. */

#include <stdio.h>
#include <malloc.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "../lzo/include/lzoconf.h"
#include "../lzo/include/lzo1x.h"
#include "stubsize.h"

/* Define this to the file we should open to get the original program */
#define PROC_EXE "/proc/self/exe"

/* Marker */
static char marker[] = "EPK11";

/* Here we prototype the LZO1X assembler decompressor on ix86 systems */
/* #ifdef __LZO_i386
LZO_EXTERN(int)
lzo1x_decompress_asm (const lzo_byte *src, lzo_uint src_len,
	lzo_byte *dst, lzo_uint *dst_len,
	lzo_voidp wrkmem */ /* NOT USED *//* );
#endif */ /* __LZO_i386 */

/* Saves a bit of space rather than having this in every string */
char *blah = "can't extract: ";

/* Everything here is done in main() to minimize code size */
/* We also use goto's in here to call redundant code to save space */
void main(int argc,char *argv[])
{
	int sfd,dfd = -1,readbufsize = 0,writebufsize = 0;
	char dfname[20],*readbuf,*writebuf;
	unsigned int sizes[2]; /* [0] = comp block size, [1] = decomp block size */
	unsigned int destlen;
	char dontspawn = 0;
	struct stat sfstat;
	int olduid = -1,oldgid = -1;
	char encrypted = 0;
	register int i;

	/* Init the LZO library (this should never fail) */
	/* This makes the binary bigger and is not currently necessary for
	 * just using the asm decompress function */
	if (lzo_init() != LZO_E_OK)
		return; 

	/* Stat the executable file from whence we came to figure out setuid/
	 * setgid issues */
	if (stat(PROC_EXE,&sfstat))
		goto cant_open;
	if (sfstat.st_mode & S_ISUID) {
		olduid = getuid();
		seteuid(sfstat.st_uid);
	}
	if (sfstat.st_mode & S_ISGID) {
		oldgid = getgid();
		setegid(sfstat.st_gid);
	}

	/* Open the source executable */
	if ((sfd = open(PROC_EXE,O_RDONLY)) <= 0) {
cant_open:
		printf("%sno %s\n",blah,PROC_EXE);
		return;
	}
	
	/* Check the arguments for a first argument ordering us to decompress
	 * to stdout */
	if ((argc > 1)&&(!strcmp(argv[1],">d")))
		dontspawn = 1;
	else sprintf(dfname,"/tmp/%o",getpid());

	/* Open the temporary executable (unless we're outputting to stdout) */
	if (!dontspawn) {
		if ((dfd = open(dfname,O_WRONLY|O_CREAT|O_TRUNC,sfstat.st_mode)) <= 0) {
			printf("%scan't open %s\n",blah,dfname);
			return;
		}
	}
		
	/* Seek to the location of the actual compressed executable */
	if (lseek(sfd,EXEPAK_STUBSIZE,SEEK_SET) < 0) {
executable_corrupted:
		printf("%scorrupt\n",blah);
		close(dfd);
		unlink(dfname);
		return;
	}
	
	/* First thing we do is read the 'encrypted' status byte */
	if (read(sfd,(char *)(&encrypted),1) != 1)
		goto executable_corrupted;
	if ((encrypted)&&(dontspawn)) {
		printf("%sencrypted\n",blah);
		return;
	}

	/* The compressed data in the compressed executable is blocked, with
	 * each compressed block being preceded with two 32-bit integers in
	 * whatever byte order this host uses telling the size of the compressed
	 * block and the size of the decompressed block. */
	while(read(sfd,&sizes,sizeof(sizes)) == sizeof(sizes)) {
		/* Check to make sure buffers are big enough */
		if (readbufsize <= sizes[0]+1) {
			if (!readbufsize) {
				if (!(readbuf = malloc((readbufsize = sizes[0]+1)))) {
out_of_memory:
					close(dfd);
					unlink(dfname);
					printf("%sout of memory\n",blah);
					return;
				}
			} else {
				if (!(readbuf = realloc(readbuf,(readbufsize += ((sizes[0] - readbufsize) + 1)))))
					goto out_of_memory;
			}
		}
		if (writebufsize <= sizes[1]+1024) {
			if (!writebufsize) {
				if (!(writebuf = malloc((writebufsize = sizes[1]+1024))))
					goto out_of_memory;
			} else {
				if (!(writebuf = realloc(writebuf,(writebufsize += ((sizes[1] - writebufsize) + 1024)))))
					goto out_of_memory;
			}
		}
		
		/* Read the compressed data block */
		if (read(sfd,readbuf,sizes[0]) != sizes[0])
			goto executable_corrupted;
		
		/* If it's "encrypted", binary 'NOT' each byte */
		if (encrypted) {
			for(i=0;i<sizes[0];i++)
				*(readbuf+i) = ~(*(readbuf+i));
		}

		/* Decompress */
		if (lzo1x_decompress(readbuf,sizes[0],writebuf,&destlen,(void *)0) != LZO_E_OK)
			goto executable_corrupted;
		
		/* Sanity check */
		if (destlen != sizes[1])
			goto executable_corrupted;

		/* Write decompressed data */
		if (dontspawn)
			write(STDOUT_FILENO,writebuf,destlen);
		else {
			if (write(dfd,writebuf,destlen) != destlen) {
				printf("%swrite error\n",blah);
				close(dfd);
				unlink(dfname);
				return;
			}
		}
	}
	
	/* Make sure we actually did something */
	if (!readbufsize)
		goto executable_corrupted;
	
	/* We don't need this anymore */
	free(readbuf);
	free(writebuf);
	close(sfd);
	close(dfd);
	
	/* If we've been ordered to permanently decompress, don't spawn */
	if (dontspawn)
		return;

	/* Fork off a subprocess to clean up */
	/* We have to do this stupid double-fork trick to keep a zombie from
	 * hanging around if the spawned original program doesn't check for
	 * subprocesses.  (As well as to prevent the real program from getting
	 * confused about this subprocess it shouldn't have) */
	if (!fork()) {
		if (!fork()) {
			sleep(1);
			unlink(dfname);
			return;
		}
		return;
	}
	
	/* Wait for the first fork()'d process to die */
	wait((int *)0);
	
	/* Reset original uid/gid */
	if (olduid)
		seteuid(olduid);
	if (oldgid)
		setegid(oldgid);

	/* Spawn the original process */
	execv(dfname,argv);
}
