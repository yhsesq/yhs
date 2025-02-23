/* EXEPAK version 1.2 */ 
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

/* This code is not pretty, but it works. :) */

#include <stdio.h>
#include <unistd.h>
#include <stdarg.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <malloc.h>
#include <string.h>
#include <utime.h>
#include <errno.h>
#include "../lzo/include/lzoconf.h"
#include "../lzo/include/lzo1x.h"
#include "../stub/__stub.h"
#include "../stub/stubsize.h"

/* This lets us know we are the EXEPAK executable */
static char *marker = "EPKMAINEXE12";

/* Prototypes */
char *tospaces(char *str);
char my_rename(char *oldname,char *newname);
char check_already_compressed(char *fname);
char create_sfx_binary(char *infile);
char decompress_sfx_binary(char *infile);
void bad_options(void);

/* Configuration options */
int blocksize = (128*1024);
char encrypting = 0;
char testing = 0;
char extracting = 0;

/* Compression work memory */
unsigned long lzo_workmem[(LZO1X_999_MEM_COMPRESS / sizeof(long))+1];

/* Cheezy little formatting function */
char *tospaces(char *str)
{
	static char buf[512];
	register int i = 0;
	
	while(*(str+i))
		buf[i++] = ' ';
	buf[i] = '\0';
	
	return buf;
}

/*
 * Rename function which will work between two different filesystems.
 * Returns nonzero if error, zero if successful.
 *
 * (I have /home and /tmp mounted on two different disks)
 */
char my_rename(char *oldname,char *newname)
{
	FILE *infile,*outfile;
	char buf[65536];
	register int n;
	
	errno = 0;
	if (rename(oldname,newname)) {
		if (errno == EXDEV) {
			if (!(outfile = fopen(newname,"w")))
				return 1;
			if (!(infile = fopen(oldname,"r"))) {
				fclose(outfile);
				return 1;
			}
			while((n = fread(&buf,1,sizeof(buf),infile)) > 0) {
				if (fwrite(&buf,1,n,outfile) != n) {
					fclose(outfile);
					return 1;
				}
			}
			fclose(infile);
			fclose(outfile);
			unlink(oldname);
		} else return 1;
	}
	return 0;
}

/*
 * This function checks to see if a file is already compressed by looking
 * for the marker string within the first 16k of the file (or less if the
 * file is smaller). Returns nonzero if true.
 */
char check_already_compressed(char *fname)
{
	FILE *inf;
	char buf[16400];
	int n;
	register int i;
	struct stat infstat;
	
	if (stat(fname,&infstat))
		return 0;

	/* If it's not an executable, return 0 */
	if ((!(infstat.st_mode & S_IXUSR))&&(!(infstat.st_mode & S_IXGRP))) {
		if (!(infstat.st_mode & S_IXOTH))
			return 0;
	}
	if (S_ISDIR(infstat.st_mode))
		return 0;

	if (!(inf = fopen(fname,"r")))
		return 0;
	n = fread(&buf,1,16384,inf);
	if (n > 0) {
		/* Get rid of nulls so we can use strstr() */
		for(i=0;i<n;i++) {
			if (buf[i] == '\0')
				buf[i] = (char)1;
		}
		buf[n+1] = '\0';
		if (strstr(buf,"EPKMAINEXE11"))
			return 0;
		if (strstr(buf,"EXEPAK"))
			return 1;
		if (strstr(buf,"EPK11"))
			return 2;
	}
	fclose(inf);
	return 0;
}
 
/*
 * Compresses an uncompressed executable.
 * Returns nonzero if successful.
 * Original input file renamed to <filaname>~
 */
char create_sfx_binary(char *infile)
{
	unsigned int sizes[2]; /* Two 32-bit ints to write before each block */
	char *inbuf,*outbuf;
	int outsize,n;
	FILE *inf,*of;
	struct stat infstat;
	register int i;
	char ofname[256];
	char newfname[256];
	int oldsize,newsize;
	int progmeter = 0;
	struct utimbuf utb;
	char didsomething = 0;
	
	sprintf(ofname,"/tmp/_comp%x%x",time(NULL),getpid());
	sprintf(newfname,"%s~",infile);
	
	/* Allocate memory */
	if ((!(inbuf = malloc(blocksize+1)))||(!(outbuf = malloc(blocksize+(blocksize/64+16+3))))) {
		printf("%s: compress failed: out of memory!\n",infile);
		return 0;
	}

	/* Stat the input file */
	if (stat(infile,&infstat)) {
		printf("%s: compress failed: can't stat\n",infile);
		free(inbuf);
		free(outbuf);
		return 0;
	}
	
	/* Can't compress files we don't own unless we're super-user */
	if ((getuid())&&(infstat.st_uid != getuid())) {
		printf("%s: compress failed: permission denied\n",infile);
		free(inbuf);
		free(outbuf);
		return 0;
	}
	
	/* Check to make sure it's an executable file */
	if ((!(infstat.st_mode & S_IXUSR))&&(!(infstat.st_mode & S_IXGRP))) {
		if (!(infstat.st_mode & S_IXOTH)) {
			printf("%s: compress failed: not an executable file\n",infile);
			free(inbuf);
			free(outbuf);
			return 0;
		}
	}
	if (S_ISDIR(infstat.st_mode)) {
		printf("%s: compress failed: %s is a directory\n",infile,infile);
		free(inbuf);
		free(outbuf);
		return 0;
	}
	
	/* Open the input file */
	if (!(inf = fopen(infile,"r"))) {
		printf("%s: compress failed: %s\n",infile,strerror(errno));
		free(inbuf);
		free(outbuf);
		return 0;
	}
	
	/* Check to make sure if it's ELF it's an executable (not a dll) */
	if (fread(inbuf,1,64,inf) != 64) {
		printf("%s: compress failed: file corrupt or read error\n",infile);
		free(inbuf);
		free(outbuf);
		fclose(inf);
		return 0;
	}
	if (!strncmp((inbuf+1),"ELF",3)) {
		if (*(inbuf+16) != (char)2) {
			printf("%s: compress failed: ELF binary is not an executable (lib?)\n",infile);
			free(inbuf);
			free(outbuf);
			fclose(inf);
			return 0;
		}
	}
	
	rewind(inf);
	
	/* Open the output file */
	if (!(of = fopen(ofname,"w"))) {
		printf("%s: compress failed: could not create temporary file\n",infile);
		free(inbuf);
		free(outbuf);
		fclose(inf);
		return 0;
	}
	
	/* First we write the EXEPAK self extractor to the file */
	if (fwrite(exepak_stub,1,EXEPAK_STUBSIZE,of) != EXEPAK_STUBSIZE) {
		printf("%s: compress failed: error writing to temporary file: %s\n",infile,strerror(errno));
		fclose(of);
		fclose(inf);
		free(inbuf);
		free(outbuf);
		unlink(ofname);
		return 0;
	}
	
	/* Next we write the 'encrypted' byte (zero/nonzero) out to the target */
	if (fwrite((void *)(&encrypting),1,1,of) != 1) {
		printf("%s: compress failed: error writing to temporary file: %s\n",infile,strerror(errno));
		fclose(of);
		fclose(inf);
		free(inbuf);
		free(outbuf);
		unlink(ofname);
		return 0;
	}
	
	printf("\r%s: read=0/%d written=%d (...)",infile,infstat.st_size,ftell(of));
	fflush(stdout);

	/* Copy and compress infile to outfile */
	while((n = fread(inbuf,1,blocksize,inf))) {
		didsomething = 1;
		/* Compress the block of read data */
		if (lzo1x_999_compress(inbuf,n,outbuf,&outsize,lzo_workmem) != LZO_E_OK) {
			printf("\r%s: compress failed: internal compression error\n",infile);
			fclose(of);
			fclose(inf);
			free(inbuf);
			free(outbuf);
			unlink(ofname);
			return 0;
		}
		/* If we are encrypting, binary 'NOT' the compressed data
		 * (this is cheesy but prevents someone from reading any visible
		 * text strings.. a hacker could decompress it anyway) */
		if (encrypting) {
			for(i=0;i<outsize;i++)
				*(outbuf+i) = ~(*(outbuf+i));
		}
		/* Write the block size before the actual block */
		sizes[0] = outsize;
		sizes[1] = n;
		if (fwrite(&sizes,1,sizeof(sizes),of) != sizeof(sizes)) {
			printf("\r%s: compress failed: error writing to temporary file: %s\n",infile,strerror(errno));
			fclose(of);
			fclose(inf);
			free(inbuf);
			free(outbuf);
			unlink(ofname);
			return 0;
		}
		/* Write the block of compressed data */
		if (fwrite(outbuf,1,outsize,of) != outsize) {
			printf("\r%s: compress failed: error writing to temporary file: %s\n",infile,strerror(errno));
			fclose(of);
			fclose(inf);
			free(inbuf);
			free(outbuf);
			unlink(ofname);
			return 0;
		}
		printf("\r%s: read=%d/%d written=%d (%.02f:1)",infile,(progmeter += n),infstat.st_size,ftell(of),(((float)n) / ((float)outsize)));
		fflush(stdout);
	}
	
	if (!didsomething) {
		printf("\r%s: compress failed: read error\n",infile);
		fclose(of);
		fclose(inf);
		free(inbuf);
		free(outbuf);
		unlink(ofname);
		return 0;
	}
	
	oldsize = ftell(inf);
	newsize = ftell(of);

	/* Check to see if we got something smaller than before, if not we fail */
	if (newsize >= oldsize) {
		printf("\r%s: compress failed: no reduction in size was achieved\n",infile);
		fclose(of);
		fclose(inf);
		free(inbuf);
		free(outbuf);
		unlink(ofname);
		return 0;
	}
	
	fclose(inf);
	fclose(of);
	free(inbuf);
	free(outbuf);

	/* Swap temporary and regular files */
	if (my_rename(infile,newfname)) {
		printf("\r%s: compress failed: could not rename original binary\n",infile);
		unlink(ofname);
		return 0;
	}
	if (my_rename(ofname,infile)) {
		printf("\r%s: compress failed: could not replace original binary\n",infile);
		unlink(ofname);
		my_rename(newfname,infile);
		return 0;
	}

	/* Set up permissions on new file */
	chown(infile,infstat.st_uid,infstat.st_gid);
	chmod(infile,infstat.st_mode);
	utb.modtime = infstat.st_mtime;
	utb.actime = infstat.st_atime;
	utime(infile,&utb);
	
	printf("\r%s: compress successful: initial=%d, compressed=%d (%.02f:1)\n",infile,oldsize,newsize,(((float)oldsize) / ((float)newsize)));
	printf("%s  renamed original uncompressed binary to %s\n",tospaces(infile),newfname);
	
	return 1;
}

/*
 * Decompresses a self-extracting binary, returns nonzero if successful.
 * Writes infile~ as output file.
 */
char decompress_sfx_binary(char *infile)
{
	FILE *of;
	int npid,n;
	char *args[3];
	char ofname[256];
	int pipes[2];
	char readbuf[16384];
	char *tmp1,*tmp2;
	struct stat infstat;
	char newfname[256];
	int newsize;
	struct utimbuf utb;
	
	sprintf(ofname,"/tmp/_decomp%x%x",time(NULL),getpid());
	sprintf(newfname,"%s~",infile);
	
	/* Stat the input file */
	if (stat(infile,&infstat)) {
		printf("%s: decompress failed: can't stat\n",infile);
		return 0;
	}

	/* Can't decompress files we don't own unless we're super-user */
	if ((getuid())&&(infstat.st_uid != getuid())) {
		printf("%s: decompress failed: permission denied\n",infile);
		return 0;
	}

	/* Check to make sure it's an executable file */
	if ((!(infstat.st_mode & S_IXUSR))&&(!(infstat.st_mode & S_IXGRP))) {
		if (!(infstat.st_mode & S_IXOTH)) {
			printf("%s: decompress failed: not an executable file\n",infile);
			return 0;
		}
	}
	if (S_ISDIR(infstat.st_mode)) {
		printf("%s: decompress failed: %s is a directory\n",infile,infile);
		return 0;
	}
	
	/* Open output file */
	if (!(of = fopen(ofname,"w+"))) {
		printf("%s: decompress failed: could not create %s\n",infile,ofname);
		return 0;
	}
	
	/* Create pipes */
	if (pipe(pipes)) {
		printf("%s: decompress failed: could not create pipes!\n",infile);
		fclose(of);
		unlink(ofname);
		return 0;
	}
	
	/* Fork off and execute compressed binary in dump-decompressed-data mode */
	if (!(npid = fork())) {
		close(STDOUT_FILENO);
		dup2(pipes[1],STDOUT_FILENO);
		dup2(pipes[1],STDERR_FILENO);
		args[0] = infile;
		args[1] = ">d";
		args[2] = (char *)0;
		execv(infile,args);
	}
	close(pipes[1]);
	if (npid < 0) {
		printf("%s: decompress failed: could not fork!\n",infile);
		close(pipes[0]);
		fclose(of);
		unlink(ofname);
		return 0;
	}

	while((n = read(pipes[0],&readbuf,sizeof(readbuf))) > 0) {
		if (fwrite(&readbuf,1,n,of) != n) {
			printf("%s: decompress failed: write error\n",infile);
			close(pipes[0]);
			fclose(of);
			unlink(ofname);
			return 0;
		}
	}
	
	close(pipes[0]);
	
	/* Look to make sure it's a real exe and not an error from the
	 * self-decompressing executable */
	newsize = ftell(of);
	rewind(of);
	if ((n = fread(&readbuf,1,128,of)) <= 0) {
		printf("%s: decompress failed: write error\n",infile);
		fclose(of);
		unlink(ofname);
		return 0;
	}
	readbuf[n+1] = '\0';
	if (strstr(readbuf,"can't extract")) {
		if ((tmp1 = strchr(readbuf,'\n'))) {
			if ((tmp2 = strchr(readbuf,':'))) {
				*tmp1 = '\0';
				printf("%s: decompress failed: extraction error%s\n",infile,tmp2);
			}
		} else printf("%s: decompress failed: extraction error\n",infile);
		fclose(of);
		unlink(ofname);
		return 0;
	}
	
	fclose(of);

	/* Swap temporary and regular files */
	if (my_rename(infile,newfname)) {
		printf("%s: decompress failed: could not rename original binary\n",infile);
		unlink(ofname);
		return 0;
	}
	if (my_rename(ofname,infile)) {
		printf("%s: decompress failed: could not replace original binary\n",infile);
		unlink(ofname);
		my_rename(newfname,infile);
		return 0;
	}

	/* Set up permissions on new file */
	chown(infile,infstat.st_uid,infstat.st_gid);
	chmod(infile,infstat.st_mode);
	utb.modtime = infstat.st_mtime;
	utb.actime = infstat.st_atime;
	utime(infile,&utb);

	printf("%s: decompress successful: compressed=%d, decompressed=%d\n",infile,infstat.st_size,newsize);
	printf("%s  renamed original compressed binary to %s\n",tospaces(infile),newfname);

	return 1;
}

/* Called for bad or nonexistant command line options */
void bad_options(void)
{
	printf("Usage: exepak [options] <files...>

Options are:
   -b##             - Sets compression block size in K (default:128)
   -t               - Just test whether executables are compressed
   -e               - \"Encrypt\" compressed executables
   -x               - Decompress a compressed executable\n");
	exit(0);
}

void main(int argc,char *argv[])
{
	int i;
	char x;
	FILE *tf;
	struct stat infstat;
	
	/* Initialize LZO compression library */
	if (lzo_init() != LZO_E_OK) {
		printf("LZO compression library failed to initialize!\n");
		exit(-1);
	}
	
	printf("EXEPAK version 1.2 executable file compressor \nmodifications (c) 1999 Zurk (zurk@geocities.com) original code (c)1997 Adam Ierymenko\nLZO compression library (c)1996,1997 Markus F.X.J. Oberhumer\nEXEPAK is distributed under the terms of the GNU GPL v2 or later.\n\n");

	/* Process command line options */
	if (argc < 2)
		bad_options();
	for(i=1;i<argc;i++) {
		if (*argv[i] == '-') {
			switch(*(argv[i]+1)) {
				case 'b':
					blocksize = 1024 * atoi(argv[i]+2);
					if (blocksize <= (1024 * 16)) {
						printf("Minimum blocksize is 16k\n\n");
						bad_options();
					}
					break;
				case 'e':
					encrypting = 1;
					break;
				case 't':
					testing = 1;
					break;
				case 'x':
					extracting = 1;
					break;
				default:
					bad_options();
			}
		} else {
			if (testing) {
				if (stat(argv[i],&infstat))
					printf("%s: can't stat\n",argv[i]);
				else {
					if ((!(infstat.st_mode & S_IXUSR))&&(!(infstat.st_mode & S_IXGRP))) {
						if (!(infstat.st_mode & S_IXOTH)) {
							printf("%s: not an executable file\n",argv[i]);
							goto not_executable_file;
						}
					}
					if (S_ISDIR(infstat.st_mode)) {
						printf("%s: is a directory\n",argv[i]);
						goto not_executable_file;
					}
					if ((tf = fopen(argv[i],"r"))) {
						fclose(tf);
						if ((x = check_already_compressed(argv[i])))
							printf("%s: compressed (%s)\n",argv[i],((x == 2) ? "1.1" : "1.0"));
						else printf("%s: uncompressed\n",argv[i]);
					} else printf("%s: file not found or unreadable\n",argv[i]);
				}
not_executable_file:
			} else if (extracting) {
				if ((x = check_already_compressed(argv[i]))) {
					if (x == 2)
						decompress_sfx_binary(argv[i]);
					else printf("%s: decompress failed: this is an exepak 1.0 binary, run it\n%s  with the command line \"^dc-<target filename>\" to decompress\n",argv[i],tospaces(argv[i]));
				} else printf("%s: decompress failed: not compressed\n",argv[i]);
			} else {
				if (!check_already_compressed(argv[i]))
					create_sfx_binary(argv[i]);
				else printf("%s: compress failed: already compressed\n",argv[i]);
			}
		}
		waitpid(-1,(int *)0,WNOHANG);
	}
	
	exit(0);
}
