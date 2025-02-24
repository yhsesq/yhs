/*

Sentinel v1.2.1c - Secure drive checker.
(C) 1999,2000,2001 Zurk Technology Inc.

Uses modified directory parsing code by Jeff Tranter (tranter@pobox.com) also released under the GNU GPL v2.0.
Uses RIPEMD - 160 bit hash functions for file integrity checking (derived from RIPEMD-160 reference implementations).

PLEASE MODIFY THE KEY VARIABLE IN THE FILE sentinel.h BEFORE USING THIS PROGRAM.

*/

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


#include <assert.h>
#include <ctype.h>
#include <dirent.h>
#include <errno.h>
#ifdef GETOPTLONG
#include <getopt.h>
#endif /* GETOPTLONG */
#include <pwd.h>
#include <regex.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>
#include <unistd.h>
#include "rmd160mc.h"
#define RMDsize 160

#include "sentinel.h"
time_t timevaluestruct;
int verbose=0; /* verbosity level */
int checkdeleted=0; /* check deleted 0 = No 1 = Yes */
int checkadded=0; /* check added 0 = No 1 = Yes */
int stealth=0;     /* Stealth/Cloak enabling */
int batchmode=0;   /* batch file execution */
int autoinit=0;    /* if 1 skip exit on unopened file */
int interlock=0; /* adding file check interlock */
int cannotcompute=0; /* cannot compute file */
FILE *batchptr;
FILE *logfilep;      /* dedicated pointers */
FILE *filelistp;     /* for some important */
FILE *logfilebakp;   /* files which need   */
FILE *filelistbakp;  /* to stay open and   */
FILE *lockp;         /* tamper resistant.  */
FILE *ptrp; /* general file read-only pointer. */
FILE *tmpp;     /* temporary file pointer */
FILE *tmpxp;    /* temporary file pointer */


/* start RMD-160bit message block functions */


void createkey()
{
/* j=1+(int) (102400.0*rand()/(RAND_MAX+1.0)); */
/*
long int random(void);
void srandom(unsigned int seed);
The  random()  function uses a non-linear additive feedback random number generator employing a default
table of size 31 long integers to return successive pseudo-random  numbers  in  the  range  from  0  to
RAND_MAX.  The period of this random number generator is very large, approximately 16*((2**31)-1).
The srandom() function sets its argument as the seed for a new sequence of pseudo-random integers to be
returned by random().  These sequences are repeatable by calling srandom() with the  same  seed  value.
If no seed value is provided, the random() function is automatically seeded with a value of 1. */
/* Simple algorithm - get the seed value from the user provided key value, generate repeatable random 
numbers and extract the key data from the executable file - more complicated to fudge for an attacker. 
Adding on executable compression should help. */
/* Not yet implemented. Added executable compression - not added this routine. Test executable compression
before adding rotating keys. Note that the plug-in for this function is already present. Simply implement
function to start using it. No return required. modified only key variable. */
}

byte *RMDMACbinary(char *fname)
{
   FILE         *mf;                  /* pointer to file <fname>      */
   byte          data[1024];          /* contains current mess. block */
   dword         nbytes;              /* length of this block         */
   dword         MDbuf[RMDsize/32];   /* contains (A, B, C, D(, E))   */
   static byte   rmdmac[RMDsize/8];   /* for final mac-value          */
   dword         X[16];               /* current 16-word chunk        */
   unsigned int  i, j;                /* counters                     */
   dword         length[2];           /* length in bytes of message   */
   dword         offset;              /* # of unprocessed bytes at    */
                                      /*          call of MDMACfinish */
   dword        *MDK;                 /* pointer to expanded key      */

   /* key setup */
   MDK = MDMACsetup(key);

   /* initialize */
   if ((mf = fopen(fname, "rb")) == NULL) {
      printf("\n!!0000!!%s\nERROR: RMDbinary: cannot open file \"%s\".\n",fname,fname);
      printf("This error is usually caused by a bad link or device file which cannot be opened.\nCheck the README for more information. ");
	cannotcompute=1;
      if (autoinit != 1){printf("!!!!!!!!");exit(1);}
   }
else {
   MDMACinit(MDK, MDbuf);
   length[0] = 0;
   length[1] = 0;
   cannotcompute=0;
   while ((nbytes = fread(data, 1, 1024, mf)) != 0) {
      /* process all complete blocks */
      for (i=0; i<(nbytes>>6); i++) {
         for (j=0; j<16; j++)
            X[j] = BYTES_TO_DWORD(data+64*i+4*j);
         compress(MDK, MDbuf, X);
      }
      /* update length[] */
      if (length[0] + nbytes < length[0])
         length[1]++;                  /* overflow to msb of length */
      length[0] += nbytes;
   }

   /* finish: */
   offset = length[0] & 0x3C0;   /* extract bytes 6 to 10 inclusive */
   MDMACfinish(MDK, MDbuf, data+offset, length[0], length[1]);

   for (i=0; i<RMDsize/8; i+=4) {
      rmdmac[i]   =  MDbuf[i>>2];
      rmdmac[i+1] = (MDbuf[i>>2] >>  8);
      rmdmac[i+2] = (MDbuf[i>>2] >> 16);
      rmdmac[i+3] = (MDbuf[i>>2] >> 24);
   }

   fclose(mf);
	}
   return (byte *)rmdmac;
}


int calculatermd (char *fullfilepath, FILE *outputfileptr)
{
  unsigned int   i, j;
  byte          *rmdmac;

if ((autoinit == 1) && (cannotcompute == 1))
{cannotcompute=0;return 1;}else{
   MDMACconstT();
   rmdmac = RMDMACbinary(fullfilepath);
         for (j=0; j<RMDsize/8; j++) {
            fprintf(outputfileptr,"%02x", rmdmac[j]); 
                                   }
   return 0;}
}

int checkfileisindb(char *filetoadd)
{
char si;
int xi=0,fi=0,flagi=0;
int flagx=0;
int flashout=0;
int yesmatch=0;
char xci;
char shortstring1[1024]; /* signature */
char shortstring2[1024]; /* mode */
char shortstring3[1024]; /* date + time */
char longstring[16384];
char *ptrtostring;
struct stat *statbp;
struct stat statb;
/* about as good as can be done. relies on an full scroll but does a single for the average case. */
ptrtostring=&longstring[0];
si='a';fi=1;xi=0;
statbp=&statb;
/* filesig,mode,size,date+time,filename<CR> */
yesmatch=0;flashout=0;
logfilep=fopen(logfile,"ab");
while (si != (char) EOF && flashout==0) {
si=getc(filelistp); 
if (fi==1){
if (si != ','){
if (si=='-'){si=getc(filelistp);if(checkdeleted==0){}}
shortstring1[xi]=si;xi++;}
else {fi++;shortstring1[xi]='\0';xi=0;}
          }
else if (fi==2){
if (si != ','){shortstring2[xi]=si;xi++;}
else {fi++;shortstring2[xi]='\0';xi=0;}
          }
else if (fi==3){
if (si != ','){shortstring3[xi]=si;xi++;}
else {fi++;shortstring3[xi]='\0';xi=0;}
          }
else if (fi==4){
 if (si != '\n'){ if (xi < 16383) { longstring[xi]=si; } xi++;}
 else {  longstring[xi]='\0';
for (xi=0;xi<1024;xi++){shortstring1[xi]='\0';
 shortstring2[xi]='\0';  shortstring3[xi]='\0';   }
  xi=0;fi=1; if (strcmp(ptrtostring,filetoadd)==0) { flashout=1; yesmatch=1;  } 
if (interlock==0 && yesmatch==0) { interlock=1; 
fclose(filelistp); filelistp=fopen(filelist,"rb");
si='a';fi=1;xi=0; }    }
	}
else{ flashout=1; }
	}
time(&timevaluestruct);
if (yesmatch != 1) {
fprintf(logfilep, "Check:[WARN] Unknown file %s has been added to the drive. :: %s",filetoadd,ctime(&timevaluestruct)); }
if (interlock == 1 && yesmatch != 1) {
fclose(filelistp); filelistp=fopen(filelist,"rb");
interlock=2; }
if (interlock==2 && yesmatch==1) { interlock=0; }
fclose(logfilep);
yesmatch=0;
return 0;
}


int addfiletofilelistp(char *filetoadd)
{
struct stat *statbp;
struct stat statb;

statbp=&statb;
/* filesig,mode,size,date+time,filename<CR> */
if (autoinit==1){
if (strstr(filetoadd, "/dev/") == filetoadd)
      return 0;
if (strstr(filetoadd, "/proc/") == filetoadd)
      return 0;
if (strstr(filetoadd, "/tmp/") == filetoadd)
      return 0;}
if ((calculatermd (filetoadd, filelistp))==0)
{lstat(filetoadd, statbp);
fprintf(filelistp,",%x,%x,%s\n",statbp->st_mode,statbp->st_mtime,filetoadd);}
else {
fprintf(logfilep,"Addfiletofilelistp: [CRITICAL] Cannot add %s to the database :: %s",filetoadd,ctime(&timevaluestruct));
}return 0;
}

/* end RMD-160 bit message block functions */

/* recursively..err..recurse a directory */
static void RecurseDirectory(const char *dir)
{
  DIR *dptr;
  struct dirent *dentry;
  struct stat statbuf;
  int status;
  char *path;
  static int level;
  level++;
  dptr = opendir(dir);
  if (dptr == 0) {
    printf("\n!!0001!!%s\nERROR: Opendir of `%s' failed. Quitting.", dir,dir);
fprintf(logfilep,"RecurseDirectory: [CRITICAL] Cannot enter directory %s :: %s",dir,ctime(&timevaluestruct));
    if (autoinit != 1){printf("!!!!!!!!");exit(100);}else{return;}}
  while ((dentry = readdir(dptr)) != 0) {
    /* skip '.' and '..' */
    if (!strcmp(dentry->d_name, ".") || !strcmp(dentry->d_name, ".."))
      continue;
    path = (char*) malloc(strlen(dir) + strlen(dentry->d_name) + 2);
    assert(path != 0);
    strcpy(path, dir);
    if (path[strlen(path)-1] != '/')
      strcat(path, "/");
    strcat(path, dentry->d_name);
    status = lstat(path, &statbuf);
    if (status == -1) {
      printf("\n!!0002!!%s\nERROR: Stat of `%s' failed. Quitting.", path, path);
fprintf(logfilep,"RecurseDirectory: [CRITICAL] Cannot stat directory %s :: %s",path,ctime(&timevaluestruct));
      if (autoinit != 1){printf("!!!!!!!!");exit(-1);}else{return;}}
	addfiletofilelistp(path);
    /* if directory, call self recursively */
    if (S_ISDIR(statbuf.st_mode))
      RecurseDirectory(path);
    free(path);}
  closedir(dptr);
  level--;
}


/* recurse directories for specialised functions */
static void RecDir(const char *dir)
{
  DIR *dptr;
  struct dirent *dentry;
  struct stat statbuf;
  int status;
  char *path;
  static int level;
  level++;
  dptr = opendir(dir);
if (dptr==0) {return;}
 /* we dont care if dptr fails */
  while ((dentry = readdir(dptr)) != 0) {
    /* skip '.' and '..' */
    if (!strcmp(dentry->d_name, ".") || !strcmp(dentry->d_name, ".."))
      continue;
    path = (char*) malloc(strlen(dir) + strlen(dentry->d_name) + 2);
    assert(path != 0);
    strcpy(path, dir);
    if (path[strlen(path)-1] != '/')
      strcat(path, "/");
    strcat(path, dentry->d_name);
    status = lstat(path, &statbuf);
if (status == -1) {return;}
/* we dont care if status == -1 failure */
	checkfileisindb(path);
    /* if directory, call self recursively */
    if (S_ISDIR(statbuf.st_mode))
      RecDir(path);
    free(path);}
  closedir(dptr);
  level--;
}



int comparefiles(char *first, char *second)
{
FILE *f1;
FILE *f2;
int c1,c2,c3;
c1=9;c2=9;c3=0;
f1=fopen(first,"rb");
f2=fopen(second,"rb");
while ((c1 != (char) EOF) && (c2 != (char) EOF))
{c1=getc(f1);
c2=getc(f2);
if (c1 != c2){c3++;} }
fclose(f1);
fclose(f2);
return c3;
}

int checkall(char *myfile)
{
/* integrity checking functions..also open all necessary files */
if (geteuid()!=0){printf("\n!!0003!!\nI_Check: Only root can run this function. [FAIL]"); printf("!!!!!!!!");exit(99);}
if ((ptrp = fopen(executablesig, "rb")) == NULL){printf("\n!!0004!!\nERROR: Files missing. Has sentinel -init been used ?");printf("!!!!!!!!");exit(100);}
fclose(ptrp);
tmpp=fopen(tmp,"wb");
calculatermd (executable, tmpp);
fclose(tmpp);
if (comparefiles(executablesig,tmp) != 0){printf("\n!!0005!!\nI_Check: Executable RIPEMD-160 MAC signature invalid. [FAIL]");printf("!!!!!!!!");exit(100);}
tmpp=fopen(tmp,"wb");
calculatermd (myfile, tmpp);
fclose(tmpp);
if (comparefiles(executablesig,tmp) != 0){printf("\n!!0006!!\nI_Check: File in argv RIPEMD-160 MAC signature invalid. [FAIL]");printf("!!!!!!!!");exit(100);}
tmpp=fopen(tmp,"wb");
calculatermd (logfile, tmpp);
fclose(tmpp);
if (comparefiles(logfilesig,tmp) != 0){printf("\n!!0007!!\nI_Check: Logfile RIPEMD-160 MAC signature invalid. [FAIL]");printf("!!!!!!!!");exit(100);}
tmpp=fopen(tmp,"wb");
calculatermd (filelist, tmpp);
fclose(tmpp);
if (comparefiles(filelistsig,tmp) != 0){printf("\n!!0008!!\nI_Check: File list RIPEMD-160 MAC signature invalid. [FAIL]");printf("!!!!!!!!");exit(100);}
}


int rebuilddatabase()
{
int x,y,z;
char longstring[16384];
char *ptrtostring;
x=9;y=0;z=0;
ptrtostring=&longstring[0];
logfilep=fopen(logfile,"ab");
filelistp=fopen(filelist,"wb");
ptrp=fopen(config,"rb");
time(&timevaluestruct);
fprintf(logfilep, "Rebuild:[CRITICAL] Database rebuild attempted by root. Conf file rescanned. :: %s",ctime(&timevaluestruct));
while (x != (char) EOF)
{
for (z=0;z<16384;z++){longstring[z]='\0';}
while ((x != 10) && (x != (char) EOF) && (y < 16384))
{
x=fgetc(ptrp);
if (x != 10 && x != (char) EOF){longstring[y]=x;y++;}
}

if (x != (char) EOF){x=9;
if (y != 0){
RecurseDirectory(ptrtostring);
}}
y=0;

}
fclose(logfilep);
logfilebakp=fopen(logfilesig,"wb");
calculatermd (logfile, logfilebakp);
fclose(logfilebakp);
fclose(filelistp);
filelistbakp=fopen(filelistsig,"wb");
calculatermd (filelist, filelistbakp);
fclose(filelistbakp);
fclose(ptrp);
}

int addsinglefile(char *singlefile)
{
int x,y,z;
char longstring[16384];
char *ptrtostring;
char thru='A';
x=9;y=0;z=0;
ptrtostring=&longstring[0];
logfilep=fopen(logfile,"ab");
filelistp=fopen(filelist,"rb");
filelistbakp=fopen(filelistbak,"wb");
time(&timevaluestruct);
fprintf(logfilep, "Add:[CRITICAL] File %s added to database. :: %s",singlefile,ctime(&timevaluestruct));
fclose(logfilep);
logfilebakp=fopen(logfilesig,"wb");
calculatermd (logfile, logfilebakp);
fclose(logfilebakp);
while (thru != (char) EOF)
{thru=fgetc(filelistp);
fputc(thru, filelistbakp);}
fclose(filelistbakp);
fclose(filelistp);
filelistp=fopen(filelist,"wb");
filelistbakp=fopen(filelistbak,"rb");
addfiletofilelistp(singlefile);
thru='A';
while (thru != (char) EOF)
{thru=fgetc(filelistbakp);
fputc(thru, filelistp);}
fclose(filelistp);
fclose(filelistbakp);
filelistbakp=fopen(filelistsig,"wb");
calculatermd (filelist, filelistbakp);
fclose(filelistbakp);
}


int checkeveryfile()
{
char si;
int xi,fi,flagi;
int flagx=0;
char xci;
char shortstring1[1024]; /* signature */
char shortstring2[1024]; /* mode */
char shortstring3[1024]; /* date + time */
char longstring[16384];
char *ptrtostring;
int nocheckflag=0; /* 0=check 1=dont check */
struct stat *statbp;
struct stat statb;
int x,y,z;
statbp=&statb;
si='a';fi=1;xi=0;
ptrtostring=&longstring[0];
filelistp=fopen(filelist,"rb");
logfilep=fopen(logfile,"ab");
printf("Initiating file/database checks...");
time(&timevaluestruct);
fprintf(logfilep, "Check:[NORMAL] Database check initiated by root. :: %s",ctime(&timevaluestruct));
/* parse the database, Calculate and check the file sig on disk, Check the mode etc and if anything is wrong open the
logfile and dump the errors+stdout..but dont quit. Uses comparefiles(tmp,tmpx); */
/* not very efficient but reliable */
while (si != (char) EOF)
{si=getc(filelistp); 
/* start */

if (fi==1){
if (si != ','){
if (si=='-'){si=getc(filelistp);if(checkdeleted==0){nocheckflag=1;}}
shortstring1[xi]=si;xi++;}
else {fi++;shortstring1[xi]='\0';xi=0;}
          }

else if (fi==2){
if (si != ','){shortstring2[xi]=si;xi++;}
else {fi++;shortstring2[xi]='\0';xi=0;}
          }

else if (fi==3){
if (si != ','){shortstring3[xi]=si;xi++;}
else {fi++;shortstring3[xi]='\0';xi=0;}
          }

else if (fi==4){
if (si != '\n'){if (xi < 16383){longstring[xi]=si;} xi++;}
else {longstring[xi]='\0';
time(&timevaluestruct);
if ((ptrp=fopen(ptrtostring,"rb"))==NULL){if (nocheckflag != 1){
fprintf(logfilep, "Check:[WARN] File %s has been deleted. :: %s",ptrtostring,ctime(&timevaluestruct));}}
else
{fclose(ptrp);xi=0;
/* parsing complete */
tmpp=fopen(tmp,"wb");
calculatermd (ptrtostring, tmpp);
fclose(tmpp);
lstat(ptrtostring, statbp);

tmpp=fopen(tmp,"rb");xci='A';flagi=0;xi=0;
while(xci != (char) EOF)
{xci=getc(tmpp);
if (xci != (char) EOF){if(shortstring1[xi] != xci){flagi++;flagx=1;}}xi++;}
fclose(tmpp);
if (flagi != 0)
{time(&timevaluestruct);if (nocheckflag != 1){
fprintf(logfilep, "Check:[WARN] Data in file %s has been modified by %d%%. :: %s",ptrtostring,flagi*2,ctime(&timevaluestruct));
if (batchmode==1) {batchptr=fopen(batchfile,"ab");fprintf(batchptr, "./sbatch %s\n",ptrtostring);fclose(batchptr);}
}else {flagx=0;}}
xi=0;

tmpp=fopen(tmp,"wb");
fprintf(tmpp,"%x",statbp->st_mode);
fclose(tmpp);
tmpxp=fopen(tmpx,"wb");
while(shortstring2[xi] != '\0')
{putc(shortstring2[xi],tmpxp);
xi++;}
fclose(tmpxp);
xi=0;
if (comparefiles(tmp,tmpx) != 0)
{time(&timevaluestruct);
if (nocheckflag != 1){
fprintf(logfilep, "Check:[WARN] Mode of file %s has been modified. :: %s",ptrtostring,ctime(&timevaluestruct));}}

tmpp=fopen(tmp,"wb");
fprintf(tmpp,"%x",statbp->st_mtime);
fclose(tmpp);
tmpxp=fopen(tmpx,"wb");
while(shortstring3[xi] != '\0')
{putc(shortstring3[xi],tmpxp);
xi++;}
fclose(tmpxp);
xi=0;
if (comparefiles(tmp,tmpx) != 0)
{time(&timevaluestruct);if (nocheckflag != 1){
fprintf(logfilep, "Check:[WARN] Time of file %s has been modified. :: %s",ptrtostring,ctime(&timevaluestruct));}}

/* thats all folks. */
}
for (xi=0;xi<1024;xi++)
{shortstring1[xi]='\0';
shortstring2[xi]='\0';
shortstring3[xi]='\0';}
xi=0;fi=1;nocheckflag=0;
if (stealth==1){sleep(1);}
	  }}

/* finish */
}

if (checkadded==1) {
fclose(logfilep);
fclose(filelistp);
filelistp=fopen(filelist,"rb");
x=9;y=0;z=0;
ptrp=fopen(config,"rb");
while (x != (char) EOF)
{
for (z=0;z<16384;z++){longstring[z]='\0';}
while ((x != 10) && (x != (char) EOF) && (y < 16384))
{
x=fgetc(ptrp);
if (x != 10 && x != (char) EOF){longstring[y]=x;y++;}
}
if (x != (char) EOF){x=9;
if (y != 0){
RecDir(ptrtostring);
}}
y=0;
}
fclose(ptrp);
fclose(filelistp);
filelistp=fopen(filelist,"rb");
logfilep=fopen(logfile,"ab");
}

time(&timevaluestruct);
fprintf(logfilep, "Check:[NORMAL] Database check completed successfully. :: %s",ctime(&timevaluestruct));
fclose(logfilep);
logfilebakp=fopen(logfilesig,"wb");
calculatermd (logfile, logfilebakp);
fclose(logfilebakp);
fclose(filelistp);
if (flagx==0)
{printf("Completed.");}
else {printf("Completed. Check logs for error(s).");}
}

/* start delete block */

int changefilestate(char *delfilename, int delflag)
{

/* changes a file to delete/undelete.
delflag=0 - delete the file.
delflag=1 - undelete the file.

In case anyones wondering why i couldnt have simply used checkeveryfile(),
the answer is complexity - its easier to make a mistake with more complex code,
and this provides a measure of redundancy as well. yes, i know its less efficient 
this way...

*/

char si;
int xi,fi,flagi;
int flagx=0;
char xci;
char shortstring1[1024]; /* signature */
char shortstring2[1024]; /* mode */
char shortstring3[1024]; /* date + time */
char longstring[16384];
char *ptrtostring;
struct stat *statbp;
struct stat statb;

statbp=&statb;
si='a';fi=1;xi=0;
ptrtostring=&longstring[0];
filelistp=fopen(filelist,"rb");
filelistbakp=fopen(filelistbak,"wb");
logfilep=fopen(logfile,"ab");
printf("Initiating file delete/undelete..[checking]..[processing]");
time(&timevaluestruct);
if (delflag==0)
{fprintf(logfilep, "Delete:[NORMAL] File %s delete attempted by root. :: %s",delfilename, ctime(&timevaluestruct));}
else
{fprintf(logfilep, "Delete:[NORMAL] File %s undelete attempted by root. :: %s",delfilename, ctime(&timevaluestruct));}
/* parse the database, Calculate and check the file sig on disk, Check the mode etc and if anything is wrong open the
logfile and dump the errors+stdout..but dont quit. Uses comparefiles(tmp,tmpx); */
/* not very efficient but reliable */
while (si != (char) EOF)
{si=getc(filelistp); 
/* start */

if (fi==1){
if (si != ','){shortstring1[xi]=si;xi++;}
else {fi++;shortstring1[xi]='\0';xi=0;}
          }

else if (fi==2){
if (si != ','){shortstring2[xi]=si;xi++;}
else {fi++;shortstring2[xi]='\0';xi=0;}
          }

else if (fi==3){
if (si != ','){shortstring3[xi]=si;xi++;}
else {fi++;shortstring3[xi]='\0';xi=0;}
          }

else if (fi==4){
if (si != '\n'){if (xi < 16383){longstring[xi]=si;} xi++;}
else {longstring[xi]='\0';
time(&timevaluestruct);
xi=0;
/* check and delete/undelete */
if (strcmp(&longstring[0],delfilename) == 0)
{if (delflag == 0)
{fprintf(filelistbakp,"-%s,%s,%s,%s\n",&shortstring1[0],&shortstring2[0],&shortstring3[0],&longstring[0]);}
else if (delflag == 1)
{fprintf(filelistbakp,"%s,%s,%s,%s\n",&shortstring1[1],&shortstring2[0],&shortstring3[0],&longstring[0]);}
}else 
{fprintf(filelistbakp,"%s,%s,%s,%s\n",&shortstring1[0],&shortstring2[0],&shortstring3[0],&longstring[0]);}
xi=0;
for (xi=0;xi<1024;xi++)
{shortstring1[xi]='\0';
shortstring2[xi]='\0';
shortstring3[xi]='\0';}
xi=0;fi=1;
	  }}

/* finish */
}
fclose(logfilep);
logfilebakp=fopen(logfilesig,"wb");
calculatermd (logfile, logfilebakp);
fclose(logfilebakp);
fclose(filelistp);
fclose(filelistbakp);
/* transfer data from filelistbak to filelist */
si='a';
filelistp=fopen(filelist,"wb");
filelistbakp=fopen(filelistbak,"rb");
while (si != (char) EOF)
{si=getc(filelistbakp);
fputc(si,filelistp);}
fclose(filelistp);
fclose(filelistbakp);
/* end data transfer */
filelistbakp=fopen(filelistsig,"wb");
calculatermd (filelist, filelistbakp);
fclose(filelistbakp);
}

/* end delete block */


int main(int argc, char **argv)
{
int i=0;
char *garbage;
struct stat *statbp;
struct stat statb;

statbp=&statb;
time(&timevaluestruct); createkey();
if ((lockp = fopen(lock, "rb")) != NULL)
{printf("\n!!0009!!\nERROR: Lock is already existing : %s\n If sentinel has crashed earlier, remove the sentinel.lock file and
continue.\nCheck the README for more details.",lock);printf("!!!!!!!!");exit(100);}
if ((lockp=fopen(lock,"wb"))==NULL){printf("\n!!0010!!\nERROR: Cannot create lock file : %s\n Write protected drive ?",lock);printf("!!!!!!!!");exit(100);}
if (argc > 1){if (strcmp (argv[1], "-cloakcheck") != 0){fprintf(lockp,"%d",getpid());}else{fprintf(lockp,"0000");}}
if (argc == 1)
{printf("\n ----");
printf("\nSentinel v1.2.1c -- Drive integrity checker.");
printf("\n (C) 1999,2000,2001 Zurk Technology Inc. ");
printf("\nThis software is licensed under the terms of the GNU GPL v2.0 or later (www.gnu.org).");
printf("\n\nOptions :");
printf("\n -check : Check the database and all files in the database, excluding deleted files. Normal check for a cron job.");
printf("\n -fullcheck : Check the database and all files in the database, excluding deleted files. Also checks for added files.");
printf("\n -batchcheck : Check the database and all files in the database, excluding deleted files & create a batchfile.");
printf("\n -init : rebuild all program files from scratch. USE WHEN INSTALLING ONLY.");
printf("\n -rebuild : Rebuild the database only. Use when adding file(s) to the database.");
printf("\n -autoinit : rebuild all program files from scratch. USE WHEN INSTALLING ONLY. Ignores bad links/files/tmp/proc/dev.");
printf("\n -autorebuild : Rebuild the database only. Use when adding file(s) to the database. Ignores bad links/files/tmp/proc/dev.");
printf("\n -wipelog : Wipe the logfile clean. Backs up logfile and wipes it clean.");
printf("\n -cloakcheck : Allows limited cloaking/stealth while checking. Slows down the check.");
printf("\n -cloakfullcheck : Allows limited cloaking/stealth while checking. Slows down the check.");
printf("\n -cloakadddeletecheck : Allows limited cloaking/stealth while checking. Does a full check. Slows down the check.");
printf("\n -deletefile <filename> : Allows a file to be deleted. Deleted files are not checked but remain in the database.");
printf("\n -addfile <filename> : Adds a file to the database.");
printf("\n -deletecheck : Checks ALL files, including deleted files, in the database.");
printf("\n -adddeletecheck : Checks ALL files, including deleted files and added files, in the database.");
printf("\n -undelete <filename> : Removes a file from the deleted list.");
printf("\n -extradetail : Quick reference help for important sentinel files/file structures.");
printf("\n----\n\n");}
else if (argc==2){
if ((strcmp (argv[1], "-init") == 0) || (strcmp (argv[1], "-autoinit") == 0))
{if (strcmp (argv[1], "-autoinit") == 0){autoinit=1;}else{autoinit=0;}
time(&timevaluestruct);
printf("\n!!9999!!\n");
printf("\nSentinel: Initialising all files...%s\n",ctime(&timevaluestruct));
printf("Checking for superuser permissions..\n");
if (geteuid()!=0){printf("\n!!0014!!\nERROR: Only the root or super-user (UID:0) can run this function."); printf("!!!!!!!!");exit(100);}
printf("Checking if executable signature exists (init has been run before)...\n");
if ((ptrp = fopen(executablesig, "rb")) != NULL){printf("\n!!0015!!\nERROR: Executable signature exists.");fclose(ptrp);printf("!!!!!!!!");exit(100);}
printf("Creating all databases...\n");
logfilep=fopen(logfile,"wb");
filelistp=fopen(filelist,"wb");
time(&timevaluestruct);
fprintf(logfilep, "0\nINIT:[CRITICAL] System initialised by root. All files rebuilt. :: %s",ctime(&timevaluestruct));
printf("Creating all signatures...\n");
logfilebakp=fopen(executablesig,"wb");
filelistbakp=fopen(logfilesig,"wb");
calculatermd (executable, logfilebakp);
calculatermd (logfile, filelistbakp);
printf("Checking for required sentinel.conf file...\n");
if ((ptrp = fopen(config, "rb")) == NULL){printf("\n!!0016!!\nERROR: Cannot open sentinel.conf file.");printf("!!!!!!!!");exit(100);}
fclose(logfilep);
fclose(filelistp);
fclose(logfilebakp);
fclose(filelistbakp);
fclose(ptrp);
printf("Checking for required size of sentinel.conf file...\n");
lstat(config, statbp);
if ((statbp->st_size) < 2)
{printf("\n!!0011!!\nERROR: sentinel.conf file has a length less than 2 bytes.");printf("!!!!!!!!");exit(100);} 
printf("Rebuilding file database...\n");
rebuilddatabase();
printf("\nSentinel: init complete. Files can now be checked regularly.\n");
}

if (strcmp (argv[1], "-check") == 0)
{printf("\n!!9998!!\n");
checkall(argv[0]);
checkeveryfile();
}

if (strcmp (argv[1], "-batchcheck") == 0)
{printf("\n!!9997!!\n");
checkall(argv[0]);
batchmode=1;
checkeveryfile();
}

if ((strcmp (argv[1], "-rebuild") == 0) || (strcmp (argv[1], "-autorebuild") == 0))
{if (strcmp (argv[1], "-autorebuild") == 0){autoinit=1;}else{autoinit=0;}
printf("\n!!9996!!\n");
checkall(argv[0]);
rebuilddatabase();
}

if (strcmp (argv[1], "-cloakcheck") == 0)
{printf("\n!!9995!!\n");
checkall(argv[0]);
/* cloaks into an httpd process and reduces CPU usage for stealth checking */
for (garbage = argv[0]; *garbage; garbage++)
{*garbage='\0';}
for (garbage = argv[1]; *garbage; garbage++)
{*garbage='\0';}
strcpy(argv[0],"httpd");
stealth=1;
checkeveryfile();
}

if (strcmp (argv[1], "-cloakadddeletecheck") == 0)
{printf("\n!!9995!!\n");
checkall(argv[0]);
/* cloaks into an httpd process and reduces CPU usage for stealth checking */
for (garbage = argv[0]; *garbage; garbage++)
{*garbage='\0';}
for (garbage = argv[1]; *garbage; garbage++)
{*garbage='\0';}
strcpy(argv[0],"httpd");
stealth=1; checkdeleted=1;checkadded=1;
checkeveryfile();
}

if (strcmp (argv[1], "-cloakfullcheck") == 0)
{printf("\n!!9995!!\n");
checkall(argv[0]);
/* cloaks into an httpd process and reduces CPU usage for stealth checking */
for (garbage = argv[0]; *garbage; garbage++)
{*garbage='\0';}
for (garbage = argv[1]; *garbage; garbage++)
{*garbage='\0';}
strcpy(argv[0],"httpd");
stealth=1; checkadded=1;
checkeveryfile();
}

if (strcmp (argv[1], "-deletecheck") == 0)
{printf("\n!!9994!!\n");
checkall(argv[0]);checkdeleted=1;
checkeveryfile();
}

if (strcmp (argv[1], "-adddeletecheck") == 0)
{printf("\n!!9994!!\n");
checkall(argv[0]);checkdeleted=1;checkadded=1;
checkeveryfile();
}

if (strcmp (argv[1], "-fullcheck") == 0)
{printf("\n!!9994!!\n");
checkall(argv[0]);checkadded=1;
checkeveryfile();
}

if (strcmp (argv[1], "-createheaderfile") == 0)
{tmpp=fopen("sentinel.h","wb");
fprintf(tmpp,"static char *pdir=\"/opt/sentinel/\";\n");
fprintf(tmpp,"static char *pfile=\"%ssentinel.\";\n",pdir);
fprintf(tmpp,"static char *psig=\"%ssentinel.sig.\";\n",pdir);
fprintf(tmpp,"static char *batchfile=\"%ssentinel.batch\";\n",pdir);
fprintf(tmpp,"static char *executable=\"%ssentinel\";\n",pdir);
fprintf(tmpp,"static char *filelist=\"%ssentinel.1\";\n",pdir);
fprintf(tmpp,"static char *logfile=\"%ssentinel.2\";\n",pdir);
fprintf(tmpp,"static char *readmefile=\"%sREADME.sentinel\";\n",pdir);
fprintf(tmpp,"static char *executablesig=\"%ssentinel.sig.0\";\n",pdir);
fprintf(tmpp,"static char *filelistsig=\"%ssentinel.sig.1\";\n",pdir);
fprintf(tmpp,"static char *logfilesig=\"%ssentinel.sig.2\";\n",pdir);
fprintf(tmpp,"static char *filelistbak=\"%ssentinel.1.bak\";\n",pdir);
fprintf(tmpp,"static char *logfilebak=\"%ssentinel.2.bak\";\n",pdir);
fprintf(tmpp,"static char *config=\"%ssentinel.conf\";\n",pdir);
fprintf(tmpp,"static char *lock=\"%ssentinel.lock\";\n",pdir);
fprintf(tmpp,"static char *tmp=\"%ssentinel.tmp\";\n",pdir);
fprintf(tmpp,"static char *tmpx=\"%ssentinel.tmpx\";\n",pdir);
fprintf(tmpp,"static unsigned char key[16] = {");
system("head -c20 /dev/random >random");
tmpxp=fopen("random","rb");
/* hopefully secure - no other known method */
i=1;
while (i<16)
{fprintf(tmpp,"0x%02x,",getc(tmpxp));i++;}
fprintf(tmpp,"0x%02x};\n",getc(tmpxp));
fprintf(tmpp,"unsigned int seed = %i;",getc(tmpxp));
i=0;fclose(tmpp);fclose(tmpxp);
/* wipe it out */
tmpxp=fopen("random","wb");
fprintf(tmpxp,".....................................................................");
fclose(tmpxp);
unlink("random");
}

if (strcmp (argv[1], "-wipelog") == 0)
{printf("\n!!9993!!\n");
checkall(argv[0]);
lstat(logfile, statbp); 
if ((statbp->st_size) > 10240)
{logfilep=fopen(logfile,"rb");
logfilebakp=fopen(logfilebak,"wb");
i=99;
while (i != (char) EOF)
{i=getc(logfilep);
putc(i,logfilebakp);}
i=99;
fclose(logfilep);
logfilep=fopen(logfilesig,"rb");
while (i != (char) EOF)
{i=getc(logfilep);
putc(i,logfilebakp);}
fclose(logfilebakp);
fclose(logfilep);
logfilep=fopen(logfile,"wb");
fprintf(logfilep, "0\nINIT:[CRITICAL] Logfile wiped by root. Backup saved with signature. Computed backup+sig signature is : ");
calculatermd (logfilebak, logfilep);
fprintf(logfilep, ". :: %s",ctime(&timevaluestruct));
fclose(logfilep);
logfilebakp=fopen(logfilesig,"wb");
calculatermd (logfile, logfilebakp);
fclose(logfilebakp);
} else {
printf("\n!!0012!!\nWARN:Unable to clean logfile. wipelog requires log > 10K in size.");
}}

if (strcmp (argv[1], "-extradetail") == 0)
{printf("\n!!9992!!\n");
printf("\n Sentinel v1.2.1c Extra detail ");
printf("\n\nFile List :");
printf("\n %s  : Executable",pfile);
printf("\n %s1 : File list with added RIPEMD-160 MAC signatures",pfile);
printf("\n %s2 : Log file with added RIPEMD-160 MAC signatures",pfile);
printf("\n %s0 : RIPEMD-160 MAC Signature of this executable created at INIT",psig);
printf("\n %s1 : RIPEMD-160 MAC Signature of file list ",psig);
printf("\n %s2 : RIPEMD-160 MAC Signature of log file ",psig);
printf("\n %ssentinel.conf : List of directories to recurse, seperated by <CR>. Updated when the database is rebuilt",pdir);
printf("\n\nFile structure :");
printf("\nLog file : loglevel<CR> Module1:<CR> Module2:..n<CR>");
printf("\nFile List : (-=deleted)filesig,mode,date+time,filename<CR>");
printf("\n\nShortened help :");
printf("\nIf a user runs it or sentinel is interrupted while checking delete the lock file and rerun the check.");
printf("\nThe sentinel.conf file is simply a list of directories (dir1<CR>dir2<CR>) which will be recursed while checking.");
}

if (strcmp (argv[1], "-version") == 0)
{printf("Sentinel120cIPC0");
}

}

else if (argc==3){

if (strcmp (argv[1], "-addfile") == 0)
{printf("\n!!9991!!\n");
checkall(argv[0]);
if ((ptrp=fopen(argv[2],"rb"))==NULL){printf("\n!!0013!!\nERROR: Cannot open file %s.",argv[2]);printf("!!!!!!!!");exit(100);}
else {fclose(ptrp);
addsinglefile(argv[2]);}
}


if (strcmp (argv[1], "-deletefile") == 0)
{printf("\n!!9990!!\n");
checkall(argv[0]);
changefilestate(argv[2], 0);
}

if (strcmp (argv[1], "-undelete") == 0)
{printf("\n!!9989!!\n");
checkall(argv[0]);
changefilestate(argv[2], 1);
}

}

fclose(lockp);
unlink(lock);
unlink(tmp);
unlink(tmpx);
printf("\n!!5000!!\n");
exit(0);
}
