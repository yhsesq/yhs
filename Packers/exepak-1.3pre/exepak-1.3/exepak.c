/********************************************************************
 * 
 * EXEPAK version 1.3 (c)1997 Adam Ierymenko  
 * Changes (C) 1999 Zurk (zurk@geocities.com R.Georgi (georgi@flacom.de) 
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

/* This code is not pretty, but it works. :) */

#include <stdio.h>
#include <stdarg.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <malloc.h>
#include <string.h>
#include <utime.h>
#include <errno.h>
#include <linux/elf.h>
#include "../include/lzoconf.h"
#include "../include/lzo1z.h"
#include "stubcode.h"

#define EXEPAK_VERSION   "1.3"
#define BUILD_DATE       "20-Feb-00"

#ifndef TRUE
#define TRUE    1
#define FALSE   0
#endif
     
/* Prototypes */

char *tospaces(char *str);
char my_rename(char *oldname,char *newname);
char check_already_compressed(char *fname);
char create_sfx_binary(char *infile,struct stat *fstat);
char decompress_sfx_binary(char *infile,struct stat *fstat,int version);
int  check_permissions(char *infile,struct stat *fstat);
void usage(int header);
int  options(char *argv);

/* Configuration options */

char *toolname;
int blocksize = 128*1024;
char encrypting = 0;
char testing = 0;
char extracting = 0;
char file_buffer[65536];

/* Compression work memory and status stuff */

unsigned long lzo_workmem[(LZO1Z_999_MEM_COMPRESS / sizeof(long)) + 1];
unsigned int input_original;
unsigned int input_complete;
unsigned int comp_complete;
char *status_name;

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

char *strip_slash(char *name)
{
    char *my = name;

    if( name == NULL )
        return "exepak";
    while( *name ) {
    	if( *name++ == '/' )
	        my = name;
    }
    return my;
}

/*
 * Rename function which will work between two different filesystems.
 * Returns nonzero if error, zero if successful.
 * (I have /home and /tmp mounted on two different disks)
 */
char my_rename(char *oldname,char *newname)
{
	int fdin,fdout;
	int n, error = 0;
	
	if( rename(oldname,newname) < 0 ) {
   		if( (fdout = open(newname,O_WRONLY|O_CREAT|O_TRUNC)) < 0 )
    		return 1;
	    if( (fdin = open(oldname,O_RDONLY)) < 0 ) {
		    close(fdout);
   			return 1;
    	}
	    while( (n = read(fdin,&file_buffer,sizeof(file_buffer))) > 0 ) {
       		if( write(fdout,&file_buffer,n) != n ) {
		    	error = 1;
    			break;
	    	}
            if( n < sizeof(file_buffer) ) {
                break;
            }
        }
  		close(fdin);
    	close(fdout);
        return error;
	}
	return 0;
}

/* 
 * read_headers() reads the ELF header and the program header table,
 * and checks to make sure that this is in fact a file that we should
 * be munging.
 */

static int read_headers(int fd)
{
    Elf32_Ehdr elfhdr;
    
    if( read(fd, &elfhdr, sizeof(elfhdr)) != sizeof(elfhdr) )
	    return FALSE;
    if( elfhdr.e_ident[EI_MAG0] != ELFMAG0
		|| elfhdr.e_ident[EI_MAG1] != ELFMAG1
		|| elfhdr.e_ident[EI_MAG2] != ELFMAG2
		|| elfhdr.e_ident[EI_MAG3] != ELFMAG3 
        || elfhdr.e_type != ET_EXEC )
	    return FALSE;
    if( elfhdr.e_ehsize != sizeof(Elf32_Ehdr) ) 
    	return FALSE;

	lseek(fd,0,SEEK_SET);
    return TRUE;
}

int append_stubfile(int fdcompressed,int fdout,char *stub,struct stat *infstat,unsigned int compressed_size)
{
    static unsigned int stub_filesz = 0;
    Elf32_Ehdr *elfhdr = (Elf32_Ehdr *)stub; 
    Elf32_Phdr *phdr = (Elf32_Phdr *)&stub[elfhdr->e_phoff];
    unsigned int mode[2];
    int n;

    /* encryption and permission for decompression */ 
    mode[0] = (encrypting ? compressed_size : 0);
    mode[1] = infstat->st_mode;
    /* paranoia check */
    if( elfhdr->e_ident[EI_MAG0] != ELFMAG0
		|| elfhdr->e_ident[EI_MAG1] != ELFMAG1
		|| elfhdr->e_ident[EI_MAG2] != ELFMAG2
		|| elfhdr->e_ident[EI_MAG3] != ELFMAG3 
        || elfhdr->e_type != ET_EXEC )
	    return FALSE;
    if( elfhdr->e_ehsize != sizeof(Elf32_Ehdr) ) 
    	return FALSE;
    if( !elfhdr->e_phoff && elfhdr->e_phnum != 2 &&
        elfhdr->e_phentsize != sizeof(Elf32_Phdr) ) 
    	return FALSE;
    /* hold original size of stub data segment */
    if( !stub_filesz ) 
        stub_filesz = phdr[1].p_filesz;

    /* 
     * Remove references to the section header table if
     * it was removed, and reduces program header table entries that
     * included truncated bytes at the end of the file.
     */
    elfhdr->e_shoff = 0;
	elfhdr->e_shnum = 0;
    elfhdr->e_shentsize = 0;
	elfhdr->e_shstrndx = 0;
    /* 
     * Adjust the size of the data section, assuming 
     * its the last entry in the pheader table. Add
     * a integer at the end to break decompress loop.
     */
    phdr[1].p_filesz = stub_filesz + sizeof(mode) + compressed_size;
    phdr[1].p_memsz  = phdr[1].p_filesz + sizeof(int);
    /* 
     * Write the new ordered stub file at the begin
     * to the compressed file and sets the new file size.
     */
    lseek(fdcompressed,0,SEEK_SET);
    n = phdr[1].p_offset + stub_filesz;
    if( write(fdout,stub,n) != n || write(fdout,mode,sizeof(mode)) != sizeof(mode) )
        return FALSE;
	while( (n = read(fdcompressed,file_buffer,sizeof(file_buffer))) > 0 ) {
	    if( write(fdout,file_buffer,n) != n ) {
			return FALSE;
		}
		if( n < sizeof(file_buffer) )
  			break;
	}
    return TRUE;
}

static void compress_callback(unsigned int insize,unsigned int compressed)
{
    static int last_message = 0;

    /* Show status in ~32kB steps */
    if( !last_message ) {
        insize += input_complete;
        compressed += comp_complete;
        printf("\r%s: read %dkB/%dkB written %dkB (%.2f%%)",status_name,
                insize/1024,input_original/1024,compressed/1024,
                (float)compressed/(float)insize*100);
    	fflush(stdout);
        last_message = 32;
    }
    last_message--;
}

/*
 * Compresses an uncompressed executable.
 * Returns nonzero if successful.
 * Original input file renamed to <filaname>~
 */
char create_sfx_binary(char *infile,struct stat *fstat)
{
	unsigned int sizes[2]; /* Two 32-bit ints to write before each block */
	char *inbuf,*outbuf;
	int  i, outsize, n;
	int  fdin, fdout;
	char ofname[256], newfname[256];
	struct utimbuf utb;
	
	sprintf(ofname,"/tmp/.%.08x",getpid());
	sprintf(newfname,"%s~",infile);
	
	/* Can't compress files we don't own unless we're super-user */
	if( getuid() && fstat->st_uid != getuid() ) {
		printf("%s: permission denied\n",infile);
		return 0;
	}
	/* Open the input file */
	if( (fdin = open(infile,O_RDONLY)) < 0 ) {
		printf("%s: compress failed, %s\n",infile,strerror(errno));
		return 0;
	}
	/* Check to make sure if it's ELF it's an executable (not a dll) */
	if( !read_headers(fdin) ) {
		printf("%s: compress failed, file corrupt or ELF binary is not an executable.\n",infile);
		close(fdin);
		return 0;
	}
	/* Open the output file */
	if( (fdout = open(ofname,O_RDWR|O_CREAT|O_TRUNC)) < 0 ) {
		printf("%s: compress failed, could not create temporary file\n",infile);
		close(fdin);
		return 0;
	}
	/* Allocate memory */
	if( !(inbuf = malloc(blocksize+16)) || !(outbuf = malloc(blocksize+(blocksize/64+16+4))) ) {
		printf("%s: compress failed, out of memory!\n",infile);
		goto compress_error;
	}
    /* start of compresion, status information */
    input_original = fstat->st_size;    
    status_name = infile;
    input_complete = 0;
    comp_complete = 0;

	while( (n = read(fdin,inbuf,blocksize)) > 0 ) {
		/* Compress the block of read data, using best compression level of lzo1z compression */
		if( lzo1z_999_compress_level(inbuf,n,outbuf,&outsize,lzo_workmem,NULL,0,compress_callback,9) != LZO_E_OK ) {
			printf("\r%s: compress failed, internal compression error\n",infile);
            goto compress_error;
		}
#ifdef DO_ENCRYPT
		/* If we are encrypting, binary 'NOT' the compressed data
		 * (this is cheesy but prevents someone from reading any visible
		 * text strings.. a hacker could decompress it anyway) */
		if( encrypting ) {
    		/* Write the block size before the actual block */
    		sizes[0] = ~outsize;
	    	sizes[1] = ~n;
			for( i=0;i<outsize;i+=sizeof(int) )
				*(unsigned int*)&outbuf[i] = ~*(unsigned int*)&outbuf[i];
		} 
        else 
#endif
        {
    		/* Write the block size before the actual block */
    		sizes[0] = outsize;
	    	sizes[1] = n;
        }
		/* Write the block of compressed data */
		if( write(fdout,sizes,sizeof(sizes)) != sizeof(sizes) || 
            write(fdout,outbuf,outsize) != outsize ) {
			printf("\r%s: compress failed, error writing to temporary file: %s\n",
                    infile,strerror(errno));
            goto compress_error;
		}
        /* Correct actual sizes */
        input_complete += n;
        comp_complete += outsize + sizeof(sizes);
		/* End of input? */
		if( n < blocksize ) break;
	}
	if( !input_complete ) {
		printf("\r%s: compress failed, error reading input file\n",infile);
        goto compress_error;
	}
	/* Check to see if we got something smaller than before, if not we fail */
	if( comp_complete + EXEPAK_STUBSIZE >= input_complete ) {
		printf("\r%s: compress failed, no reduction in size was achieved\n",infile);
        goto compress_error;
	}
    close(fdin);
	/* Change regular file name */
	if( my_rename(infile,newfname) ) {
		printf("\r%s: compress failed, could not rename original binary\n",infile);
        goto compress_error;
	}
    /* Reopen and write stub file to disk */
    if( (fdin=open(infile,O_WRONLY|O_CREAT|O_TRUNC)) < 0 || !append_stubfile(fdout,fdin,exepak_stub,fstat,comp_complete) ) {
		printf("\r%s: compress failed, could not replace original binary; %s\n",infile,strerror(errno));
		my_rename(newfname,infile);
compress_error:
		close(fdin);
		close(fdout);
		unlink(ofname);
		free(inbuf);
		free(outbuf);
		return 0;
    }
    /* close all resources */
	comp_complete = lseek(fdin,0,SEEK_END);
	close(fdin);
	close(fdout);
	unlink(ofname);
	free(inbuf);
	free(outbuf);

	/* Set up permissions on new file */
	chown(infile,fstat->st_uid,fstat->st_gid);
	chmod(infile,fstat->st_mode);
	utb.modtime = fstat->st_mtime;
	utb.actime  = fstat->st_atime;
	utime(infile,&utb);
    	
	printf("\r%s: compress successful: original %dKb, compressed %dKb (%.2f%%)\n",
            infile,input_complete/1024,comp_complete/1024,(float)comp_complete/(float)input_complete*100);
	printf("%s renamed original uncompressed binary to %s\n",tospaces(infile),newfname);
	
	return 1;
}

/*
 * Decompresses a self-extracting binary, returns nonzero if successful.
 * Writes infile~ as output file.
 */
char decompress_sfx_binary(char *infile,struct stat *fstat,int version)
{
	int  npid, n;
	char ofname[256], newfname[256];
	int  fd, pipes[2];
	char *args[3];
	char *tmp1,*tmp2;
	int  newsize;
	struct utimbuf utb;
	
    if( version < 3 ) {
        printf("%s: decompress failed: this is an exepak 1.0 binary,\n"
            "run it \"%s ^dc-<target filename>\" to decompress\n",infile,infile);
    }
	sprintf(ofname,"/tmp/.%.08x",getpid());
	sprintf(newfname,"%s~",infile);
	
    if( getuid() && fstat->st_uid != getuid() ) {
		printf("%s: permission denied\n",infile);
		return 0;
	}
	/* Open output file */
	if( (fd = open(ofname,O_RDWR|O_CREAT|O_TRUNC)) < 0 ) {
		printf("%s: decompress failed, could not create %s\n",infile,ofname);
		return 0;
	}
	/* Create pipes */
	if( pipe(pipes) ) {
		printf("%s: decompress failed, could not create pipes!\n",infile);
		close(fd);
		unlink(ofname);
		return 0;
	}
	/* Fork off and execute compressed binary in dump-decompressed-data mode */
	if( !(npid = fork()) ) {
		close(STDOUT_FILENO);
		close(STDERR_FILENO);
		dup2(pipes[1],STDOUT_FILENO);
		dup2(pipes[1],STDERR_FILENO);
		args[0] = infile;
		args[1] = ">d";
		args[2] = NULL;
		execv(infile,args);
	}
	close(pipes[1]);
	if (npid < 0) {
		printf("%s: decompress failed, could not fork!\n",infile);
        goto close_pipes;
	}
	while( (n = read(pipes[0],file_buffer,sizeof(file_buffer))) > 0 ) {
		if( write(fd,file_buffer,n) != n ) {
			printf("%s: decompress failed, write error\n",infile);
close_pipes:
			close(pipes[0]);
			close(fd);
			unlink(ofname);
			return 0;
		}
	}
	close(pipes[0]);
	
	/* 
     * Look to make sure it's a real exe and not an error from the
	 * self-decompressing executable 
     */
    lseek(fd,0,SEEK_SET);
	if( (n = read(fd,file_buffer,256)) <= 0 ) {
		printf("%s: decompress failed, write error\n",infile);
        goto decompress_error;
	}
	file_buffer[n+1] = '\0';
	if( strstr(file_buffer,"can't extract") || strstr(file_buffer,"exepak:") ) {
		if( (tmp1 = strchr(file_buffer,':')) ) {
            if( (tmp2 = strchr(file_buffer,'\n')) ) *tmp2 = '\0';
			printf("%s: decompress failed, extraction error:%s\n",infile,tmp1+1);
        }
        else 
            printf("%s: decompress failed, extraction error\n",infile);
decompress_error:
		close(fd);
		unlink(ofname);
		return 0;
	}
    /* Get size of new written file */
	newsize = lseek(fd,0,SEEK_END);
	close(fd);
	/* Swap temporary and regular files */
	if( my_rename(infile,newfname) ) {
		printf("%s: decompress failed: could not rename original binary\n",infile);
		unlink(ofname);
		return 0;
	}
	if( my_rename(ofname,infile) ) {
		printf("%s: decompress failed: could not replace original binary\n",infile);
		unlink(ofname);
		my_rename(newfname,infile);
		return 0;
	}
	/* Set up permissions on new file */
	chown(infile,fstat->st_uid,fstat->st_gid);
	chmod(infile,fstat->st_mode);
	utb.modtime = fstat->st_mtime;
	utb.actime  = fstat->st_atime;
	utime(infile,&utb);

	printf("%s: decompress successful: compressed %ld, original %d\n",infile,fstat->st_size,newsize);
	printf("%s: renamed original compressed binary to %s\n",tospaces(infile),newfname);

	return 1;
}

/*
 * This function checks to see if a file is already compressed by looking
 * for the marker string within the first 16k of the file (or less if the
 * file is smaller). Returns nonzero if true.
 */
char check_already_compressed(char *infile)
{
	int fd, i, n;

	if( (fd = open(infile,O_RDONLY)) < 0 ) {
        printf("%s: file not found or unreadable\n",infile);
        return 0;
    }
    else if( (n = read(fd,file_buffer,16384)) > 0 ) {
    	/* Get rid of nulls so we can use strstr() */
	    for(i=0;i<n;i++) {
		   	if( file_buffer[i] == '\0' )
		    	file_buffer[i] = ' ';
		}
    	file_buffer[n+1] = '\0';
	    if (strstr(file_buffer,"EPKMAINEXE11"))
		   	n = 2;
    	else if (strstr(file_buffer,"EXEPAK"))
	    	n = 3;
		else if (strstr(file_buffer,"EPK11"))
		    n = 4;
    	else if (strstr(file_buffer,"EPK13"))
	    	n = 5;
        else
            n = 1;
	}
    close(fd);
    return n;
}
 
int check_permissions(char *infile,struct stat *fstat)
{
	/* Can't decompress files we don't own unless we're super-user */
	if( stat(infile,fstat) ) {
		printf("%s: no such file or directory\n",infile);
        return 0;
    }
	/* Check to make sure it's an executable file */
	if( S_ISDIR(fstat->st_mode) || 
        (!(fstat->st_mode & S_IXUSR) && 
         !(fstat->st_mode & S_IXGRP) &&
		 !(fstat->st_mode & S_IXOTH)) ) {
		printf("%s: not an executable file\n",infile);
        return 0;
    }
    return 1;
}
	
/* Called for bad or nonexistant command line options */

void usage(int header)
{
    if( header )
       	printf("exepak, a LZO based executable file compressor, Version %s, %s.\n\n",EXEPAK_VERSION,BUILD_DATE);
	
    printf("usage: %s [options] [file ...]\n"
           "\t-h     - print this message\n" 
           "\t-bnum  - sets compression block size (default 128kB)\n"
           "\t-t     - just test whether executables are compressed\n"
           "\t-x     - decompress a compressed executable\n"
           "\t-L     - display software version & license\n"
           "\t-V     - display software version & license\n"
#ifdef DO_ENCRYPT
          "\t-e     - \"encrypt\" compressed executables\n"
#endif
          "\n",toolname); 
	exit(1);
}

int options(char *options)
{
    if( options[0] == '-' ) {
        switch( options[1] ) {
        	case 'b':
	        	blocksize = atoi(&options[2])*1024;
		        if( blocksize < 16*1024 ) {
			       	printf("%s: minimum blocksize for compression is 16kB.\n",toolname);
			        usage(0);
    			}
	    		break;
#ifdef DO_ENCRYPT
		    case 'e': 
			    encrypting = 1;
    			break;
#endif
	    	case 't':
		    	testing = 1;
			    break;
    		case 'x':
	    		extracting = 1;
		    	break;
            case 'L':
                printf("exepak, a LZO based executable file compressor, Version %s, %s.\n\n"
                       "  exepak (c) 1997-1999 Adam Ierymenko, Zurk (zurk@geocities.com)\n"
                       "  LZO library (c) 1996-1999 Markus F.X.J. Oberhumer\n\n"
                       "  This program is free software; you can redistribute it and/or modify\n"
                       "  it under the terms of the GNU General Public License as published by\n"
                       "  the Free Software Foundation; either version 2 of the License, or\n"
                       "  (at your option) any later version.\n\n"
                       "  This program is distributed in the hope that it will be useful,\n"
                       "  but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
                       "  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
                       "  GNU General Public License for more details.\n\n"
                       "  You should have received a copy of the GNU General Public License\n"
                       "  along with this program; if not, write to the Free Software\n"
                       "  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.\n\n",
                       EXEPAK_VERSION,BUILD_DATE); 
                exit(0);
            case 'V':
                return 1;
	    	default:
		    	usage(1);
        }
    }
    return FALSE;
}

int main(int argc,char *argv[])
{
	int i, exe, actions = 0;
	struct stat infstat;
	
    toolname = strip_slash(argv[0]);

	/* Initialize LZO compression library */
	if (lzo_init() != LZO_E_OK) {
		printf("%s: LZO compression library failed to initialize, give up!\n",toolname);
		return 1;
	}

	/* Process command line options */
	for( i=1;i<argc;i++ ) 
    	actions |= options(argv[i]);
    /* Print version message */
    if( actions ) {
       	printf("exepak, a LZO based executable file compressor, Version %s, %s.\n\n"
               "exepak (c) 1997-1999 Adam Ierymenko, Zurk (zurk@geocities.com)\n"
               "LZO library (c) 1996-1999 Markus F.X.J. Oberhumer\n\n",
               EXEPAK_VERSION,BUILD_DATE);
    }
    /* Perform action to every file */
	for( i=1;i<argc;i++ ) {
		if( *argv[i] != '-' ) {
            if( !check_permissions(argv[i],&infstat) ||
                !(exe = check_already_compressed(argv[i])) ) {
                continue;
            }
			if( testing ) {
    			if( exe == 1 )
                     printf("%s: file uncompressed\n",argv[i]);
				else 
                    printf("%s: compress failed, already compressed (%s)\n",argv[i],
                        (exe == 4) ? "EXEPAKv1.1" : (exe == 5) ? "EXEPAKv1.3" : "EXEPAKv1.0");
            }
            else if( extracting ) {
                if( exe > 1 )
    				decompress_sfx_binary(argv[i],&infstat,exe);
                else 
                    printf("%s: decompress failed, file not compressed\n",argv[i]);
			} 
            else {
				if( exe == 1 )
					create_sfx_binary(argv[i],&infstat);
				else 
                    printf("%s: compress failed, already compressed (%s)\n",argv[i],
                        (exe == 4) ? "EXEPAKv1.1" : (exe == 5) ? "EXEPAKv1.3" : "EXEPAKv1.0");
			}
    		waitpid(-1,(int *)0,WNOHANG);
            actions = 1;
		}
	}
    if( !actions ) usage(1);

	return 0;
}
