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
/*     File: measure.c                                       */
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
extern unsigned long fg,bg;
extern Pixmap thePixmap_2;
extern XImage *theXImage_1;
extern XImage *theXImage_2;
extern int file_not_loaded;
extern void refresh_action(void);

/* Function prototypes */

void activate_area_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data);

void activate_perimeter_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data);

void activate_circular_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data);

int range (int n, int m);
int nay4 (int i, int j, int value);
float perimeter (int val);
int area (int val);
float circular (int val);


/* Function definition */
void activate_perimeter_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data)


/*      Compute the perimeter of the region(s) marked with VAL  */

{
	
	unsigned long val = 134L;
	float per;

 	if (file_not_loaded) return;

	selection = PERIMETER;
	action = SELECT;
	refresh_action();
	
	per = perimeter(val);
 	printf("Perimeter = %f\n", per);
}

void activate_circular_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data)


/*      Compute the circularity of the region(s) marked with VAL  */

{
	
	unsigned long val = 73L;
	

 	if (file_not_loaded) return;

	selection = CIRCULAR;
	action = SELECT;
	refresh_action();
	
 	printf("Circular = %f\n", circular(val));
}


void activate_area_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data)
{
  
  unsigned long val = 134L;
  int a;

  if (file_not_loaded) return;

  selection = AREA;
  action = SELECT;
  refresh_action();

  a= area(val); 
 
  printf("Area = %d\n",a);
}

/*   Count and return the number of pixels having value VAL  */

int area (int val)
{
  int i,j,k;

 /* Count and return the number of pixels having value VAL  */
  k = 0;
  for (i=0; i<IMAGE_WIDTH; i++)
    for (j=0; j<IMAGE_HEIGHT; j++) 
      if (XGetPixel(theXImage_1,i,j) == val) k++;
   return k;

}


float perimeter (int val)
{
	int i,j,k, ii,jj,t;
	float p;

	p = 0.0;

/* Remove all pixels except those having value VAL */
	for (i=0; i<IMAGE_WIDTH; i++) {
	   for (j=0; j<IMAGE_HEIGHT; j++) {
		if (XGetPixel(theXImage_1,i,j) != val) {
			XPutPixel(theXImage_2,i,j,fg);
			continue;
		}
		k = nay4(i, j, val); /* How many neighbors are VAL */
		if (k < 4)              /* If not all, this is on perim */
			XPutPixel(theXImage_2,i,j,bg);
		else XPutPixel(theXImage_2,i,j,fg);
	}  }

	for (i=0; i<IMAGE_WIDTH; i++) {
	   for (j=0; j<IMAGE_HEIGHT; j++) {
		if (XGetPixel(theXImage_2,i,j) != fg) continue;

/*      Match one of the templates      */

		k = 1;  t = 0;
		for (ii= -1; ii<=1; ii++) {
		   for (jj = -1; jj<=1; jj++) {
			if (ii==0 && jj==0) continue;
			if (XGetPixel(theXImage_2,i+ii,j+jj)== bg)
				t = t + k;
			k = k << 1;
		   }
		}

/*      Templates for 1.207:
     o o o   o o #   o # o   o # o    # o o    o o #    o o o   # o o
     # # o   # # o   o # o   o # o    o # o    o # o    o # #   o # #
     o o #   o o o   # o o   o o #    o # o    o # o    # o o   o o o
 T=   210    014       042      202      101      104      060     021

	Templates for 1.414:
	 # o o   o o #   # o o   o o #   o o o   # o #
	 o P o   o P o   o P o   o P o   o P o   o P o
	 o o #   # o o   # o o   o o #   # o #   o o o
  T=        201     044     041     204    240    005

	Templates for 1.0:

		o o o           o # o    o o o   o o o   o # o   o # o
		# # #           o # o    # # o   o # #   # # o   o # #
		o o o           o # o    o # o   o # o   o o o   o o o
		030               102     72      80      10      18

*/
		if (t==0210 || t == 014 || t == 042 ||
		    t==0202 || t ==0101 || t ==0104 ||
		    t== 060 || t == 021) {
			p += 1.207;
			continue;
		}

		if (t == 0201 || t == 044 || t == 041 ||
		    t == 0204 || t ==0240 || t == 005) {
			p += 1.414;
			continue;
		}

		if (t == 030 || t == 0102 || t == 80 ||
		    t == 10 || t == 18) {
			p += 1.0;
			continue;
		}

		p += 1.207;
	}   }
	
	return p;

}


int range (int n, int m)
{
/*      Return 1 if (n,m) are legal (row,column) indices for image X    */

	if (n < 0 || n >= IMAGE_WIDTH) return 0;
	if (m < 0 || m >= IMAGE_HEIGHT) return 0;
	return 1;
}

int nay4 (int i, int j, int value)
{
	int n,m,k;

	if (XGetPixel(theXImage_1, i, j) != value) return 0;
	k = 0;
	for (n= -1; n<=1; n++) {
	   for (m= -1; m<=1; m++) {
		if (n*m) continue;
		if (range(i+n, j+m)) 
		  if (XGetPixel(theXImage_1, i+n, j+m) == value) k++;
	   }
	}
	return k-1;
}

/*      Calculate the circularity measure circular, ratio or area to perimeter    */

float circular (int val)
{
	float p,a,c;

	p = perimeter( val);
	a = (float)area (val);
	c = p*p/(3.1414926535*4.0*a);
	return c;
}
