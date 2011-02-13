  /*

MultiSlice RTP Environment
(C) 1998,1999,2000,2001,2002 Yohann Sulaiman.
(C) 1998 Gloria Bueno
(C) 2002  Free Software Foundation, Inc. 


    This file is part of the source code of the MultiSlice RTP Environment.

    MultiSlice RTP Environment is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    MultiSlice RTP Environment  is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MultiSlice RTP Environment; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/


/*--------------------------------------------------------------*/
/*								*/
/*	File: print.c						*/
/*								*/
/*	OSF/Motif version.					*/
/*								*/
/*	December 1997						*/
/*								*/
/*--------------------------------------------------------------*/

#include "main.h"
#include <math.h>
#include <stdio.h>
#include <time.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>
#include <X11/Xutil.h>
#include <X11/cursorfont.h>

#include <Xm/Xm.h>
#include <Xm/Form.h>
#include <Xm/Frame.h>
#include <Xm/DrawingA.h>
#include <Xm/PushB.h>
#include <Xm/CascadeB.h>
#include <Xm/Separator.h>
#include <Xm/RowColumn.h>
#include <Xm/Label.h>


/* Extern variables */
extern int      yhs_files_open;
extern char     *yhs_filename1;
extern int	yhs_filename[11];

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;
void *malloc (size_t);
/* XWDFileHeader xwdhdr; */
time_t bintime;
int looph,file_ended=0;
unsigned char **image;
unsigned char *rowi;
double greylevel;
unsigned int tc;
size_t rowsize,numread;
char *psname;
char *prolog[] =
{
"%%Pages: 1\n",
"%%EndProlog\n",
"%%Page: 1 1\n",
"/draw_image % stack should have: width, height\n",
"% Draw image (lower-left corner at current origin)\n",
"{\n",
"% arguments...\n",
"    /height exch def\n",
"    /width exch def\n",
"/oneline\n",
"    width\n",
"    string\n",
"    def\n",
"% Read and draw image. Assume 8-bit image...\n",
"    width height 8 [width 0 0 height 0 0]\n",
"    {currentfile oneline readhexstring pop }\n",
"    image\n",
"} def\n",
NULL
};

char *epilog[] =
{
"\n",
"showpage\n",
"%%Trailer\n",
NULL
};
/* Function callbacks */

void print_callback(Widget x, XtPointer client_data, 
                             XmAnyCallbackStruct *call_data);
unsigned long bel2lel(unsigned long be);

unsigned long bel2lel(unsigned long be)
{
	union
	{
	   unsigned long x;
	   unsigned char y[4];
	}beb4;
register unsigned char t;

beb4.x=be;
/* XCHG bytes 0 and 3 */
t=beb4.y[3];
beb4.y[3]=beb4.y[0];
beb4.y[0]=t;
/* XCHG bytes 1 and 2 */
t=beb4.y[2];
beb4.y[2]=beb4.y[1];
beb4.y[1]=t;
return (beb4.x);
}

/*-------------------------------------------------------------*/
/*  p r i n t _ c a l l b a c k		                       */
/*                                                             */
/*  Activate the printf->PS routine                            */
/*-------------------------------------------------------------*/
void print_callback(Widget x, XtPointer client_data,
		XmAnyCallbackStruct *call_data)
{
int array[512][512];
int left=0,bottom=0,scale=1,hact=0,wact=0,lmargin=0,bmargin=0;
int i,j,w,h;
FILE *p_file,*outfile;
char *temp="Empty file name ...............................................";
char *addps=".ps";

if (yhs_files_open != 0 && yhs_filename[0] != 0){ strcpy(temp,yhs_filename1);
   strcat(temp,addps);
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (i=0;i<512;i++){
    for (j=0;j<512;j++)
    { array[i][j]=getc(p_file); }}
    fclose(p_file);
    p_file=fopen(yhs_filename1,"r");
    outfile=fopen(temp,"w");

h = /*(int) bel2lel(512);*/ 512;
w = h;

fprintf(outfile,"%%!PS-Adobe-2.0 EPSF-1.2\n");
fprintf(outfile,"%%%%Creator:Yohann Sulaiman\n");
fprintf(outfile,"%%%%For: MultiSlice RTP Environment\n");
fprintf(outfile,"%%%%Title:%s\n",temp);
/* time(&bintime); */
fprintf(outfile,"%%%%CreationDate: %s", ctime(&bintime));

fprintf(stderr,"\nPrinting...PostScript output is %s\n",temp);



lmargin=50+left;
bmargin=50+bottom;

if(h%2)h++;
hact = (int)(((long)scale * (long)h)/ 100L);
hact=hact+512;
wact = (int)(((long)scale * (long)w)/ 100L);
wact=wact+512;
fprintf(outfile, "%%%%BoundingBox: %d %d %d %d\n", lmargin, bmargin, lmargin+wact, bmargin+hact);
hact=hact-512;
wact=wact-512;
fprintf(outfile,"%%%%EndComments\n");

for(i=0;prolog[i] != NULL;i++)
fprintf(outfile,"%s",prolog[i]);

lmargin=50;
bmargin=50;

looph = h;
if(h%2) looph += 1;
fprintf(outfile,"%d %d translate\n",lmargin,bmargin);
fprintf(outfile,"%d %d scale\n",hact+512,wact+512);
fprintf(outfile,"%d %d draw_image\n",h,w);

rowsize=/*bel2lel(512);*/ 512;

if((image = (unsigned char **) malloc(looph*sizeof(unsigned char *))) == NULL)
{fprintf(stderr,"Error allocating PostScript RAM");}

for(i=0;i<512;i++)
{ image[i] = (unsigned char *)malloc(rowsize);
numread=fread(image[i],1,rowsize,p_file);
}

for(j=0;j<512;j++)
{for (i=0;i<512;i++){
rowi=image[i];
tc=rowi[j] & 0xff;
greylevel=(double)array[i][j];
tc=greylevel; 
fprintf(outfile,"%2.2x",tc);
}}

for(i=0;epilog[i] != NULL;i++) fprintf(outfile,"%s", epilog[i]);
fclose(p_file);
fclose(outfile);
fprintf(stderr,"Completed. Type qpr %s <enter> to send to the printer...\n",temp);
}}

