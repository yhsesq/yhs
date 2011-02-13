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


/*-------------------------------------------------------------*/
/*                                                             */
/*     File: ctomass.c                                           */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>

/* Extern variables */
extern char *action;
extern int selection;
extern Widget draw_2;
extern GC image_gc_2;
extern unsigned long  bg;
extern Pixmap thePixmap_2;
extern XImage *theXImage_1;
extern XImage *theXImage_2;
extern int file_not_loaded;
extern void refresh_action(void);

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

/* Function prototypes */
void activate_ctomass_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data);

/* Function definition */
void activate_ctomass_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data)
{
	int i, j;
	long kk;
	float *ii,  *jj ;
	unsigned long val = 134L;

	if (file_not_loaded) return;

  	selection = CTOMASS;
  	action = SELECT;
  	refresh_action();

	

/*      Calculate the coordinates of the center of mass of the region(s)
	marked with the value VAL. Return as (II,JJ).                   */


	kk = 0;
	*ii = 0.0;      *jj = 0.0;
	for (i=0; i<IMAGE_WIDTH; i++) {
	   for (j=0; j<IMAGE_HEIGHT; j++) {
		if (XGetPixel(theXImage_1,i,j) == val) {
			*ii += (float)i;        *jj += (float)j;
			kk += 1;
			
		}
	   }
	}

	if (kk==0) {
		return;
	}
	*ii = *ii/(float)kk;            *jj = *jj/(float)kk;
	
	printf("Center of Mass = %f\n , %f\n", *ii, *jj);
}
