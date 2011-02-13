  /*

MultiSlice RTP Environment
(C) 1998,1999,2000,2001,2002 Yohann Sulaiman.
(C) 1998 Gloria Bueno
. 


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
/*     File: edge.c                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"


#include <Xm/Xm.h>
#include <Xm/SelectioB.h>

#define THRESHOLD_MESSAGE "Enter threshold value (0-255):"
#define SMOOTH_MESSAGE "Enter threshold value (0-255):"

/* Extern variables */
extern char *action;
extern int selection;
static int threshold;
extern Widget draw_2;
extern GC image_gc_2;
extern Pixmap thePixmap_2;
extern XImage *theXImage_1;
extern XImage *theXImage_2;
extern int file_not_loaded;
extern void refresh_action(void);
extern unsigned long fg, bg;
extern int array1[512][512];
extern int array2[512][512];
extern int yhs_files_open;
extern int yhs_filename[11];
extern char *yhs_filename1;
extern int run_once;
extern int running;
extern int file_loader;
extern char          *file_yhs;
extern char          *tempfileold;
extern char          *tempfilenew;
extern char          *addcharac;

extern int range ( int n, int m);

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

static Widget threshold_dialog = (Widget) NULL;
static Widget smooth_t_dialog = (Widget) NULL;
static Widget hough_dialog = (Widget) NULL;


/* Function prototypes */

void edge_sobel_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_kirsch_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_prewitt_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_frei_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void line_frei_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_marr_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_roberts_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void edge_vert_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void create_edge_hv_dialog(Widget parent);
void activate_edge_hv_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data);
void deactivate_edge_hv_dialog(void);
static void ok_edge_hvbutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data);
static void cancel_edge_hv_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data);
void edge_hv(int iterations);

void create_edge_dilate_dialog(Widget parent);
void activate_edge_dilate_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data);
void deactivate_edge_dilate_dialog(void);
static void ok_edge_dilatebutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data);
static void cancel_edge_dilate_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data);
void edge_dilate(int loop);
void edge_unsharp_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);

void mean_filter_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);

void create_smooth_t_dialog(Widget parent);
void activate_smooth_t_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
void deactivate_smooth_t_dialog(void);
static void ok_smooth_tbutton_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);
static void cancel_smooth_t_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);

void smooth_t(int t);

void smooth_mask1_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void smooth_mask2_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void median_filter_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);
void median_vf_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data);

void create_hough_dialog(Widget parent);
void activate_hough_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
void deactivate_hough_dialog(void);
static void ok_hough_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);
static void cancel_hough_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);
void hough(int t);


void create_threshold_dialog(Widget parent);
void activate_threshold_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
void deactivate_threshold_dialog(void);
static void ok_threshbutton_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);
static void cancel_threshbutton_callback(Widget w, XtPointer client_data,
				XmSelectionBoxCallbackStruct *call_data);
static void thresholding(int t);


void histogram_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
void histo_eq_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);


void convolve (int y[3][3]);
int max4 (int a, int b, int c, int d);
int min4 (int a, int b, int c, int d);
void medsort(int *arr, int n);
Widget edge_hv_dialog;
Widget edge_dilate_dialog;

/* Function definition */

/* Apply a Sobel edge mask to the image theXImage_1 */
void edge_sobel_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data) {
  int i,j;
  float rng;
  unsigned long n, m, k, rmax, rmin;

  if (file_not_loaded) return;

  selection = EDGE_SOBEL;
  action = SELECT;
  refresh_action();

  /* Locate MAX and MIN for rescaling */
  rmax = XGetPixel(theXImage_1,0,0);
  rmin = rmax;
  for (i=1; i<IMAGE_WIDTH-1; i++)
    for (j=1; j<IMAGE_HEIGHT-1; j++) {
      n = ( XGetPixel(theXImage_1,i-1,j+1) +
	    2*XGetPixel(theXImage_1,i,j+1) +
	    XGetPixel(theXImage_1,i+1,j+1) ) -
	  ( XGetPixel(theXImage_1,i-1,j-1) +
	    2*XGetPixel(theXImage_1,i,j-1) +
	    XGetPixel(theXImage_1,i+1,j-1) );
      m = ( XGetPixel(theXImage_1,i+1,j-1) +
	    2*XGetPixel(theXImage_1,i+1,j) +
	    XGetPixel(theXImage_1,i+1,j+1) ) -
	  ( XGetPixel(theXImage_1,i-1,j-1) +
	    2*XGetPixel(theXImage_1,i-1,j) +
	    XGetPixel(theXImage_1,i-1,j+1) );
      k = abs(n)+abs(m);
      if (k>rmax) rmax = k;
      if (k<rmin) rmin = k;
    }
  rng = (float)(rmax-rmin);

  /* Now compute the convolution, scaling */
  for (i=1; i<IMAGE_WIDTH-1; i++)
    for (j=1; j<IMAGE_HEIGHT-1; j++) {
      n = ( XGetPixel(theXImage_1,i-1,j+1) +
            2*XGetPixel(theXImage_1,i,j+1) +
	    XGetPixel(theXImage_1,i+1,j+1) ) -
          ( XGetPixel(theXImage_1,i-1,j-1) +
            2*XGetPixel(theXImage_1,i,j-1) +
	    XGetPixel(theXImage_1,i+1,j-1) );
      m = ( XGetPixel(theXImage_1,i+1,j-1) +
	    2*XGetPixel(theXImage_1,i+1,j) +
            XGetPixel(theXImage_1,i+1,j+1) ) -
          ( XGetPixel(theXImage_1,i-1,j-1) +
            2*XGetPixel(theXImage_1,i-1,j) +
            XGetPixel(theXImage_1,i-1,j+1) );
      k = abs(n)+abs(m);
      XPutPixel(theXImage_2,i,j,(unsigned long) (((float)(k-rmin)/rng)*256.0));
    }

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
              0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(w),XtWindow(draw_2),0,0,0,0,True);
}

/*      Apply a Laplacian edge mask to the image X      */
void edge_laplac_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data) {

	int mask[3][3];

  if (file_not_loaded) return;

  selection = EDGE_LAPLAC;
  action = SELECT;
  refresh_action();



	mask[0][0]=0; mask[0][1]=1; mask[0][2]=0;
	mask[1][0] = 1; mask[1][1] = -4; mask[1][2]=1;
	mask[2][0]=0; mask[2][1]=1; mask[2][2]=0;
	convolve ( mask);

/* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(w),XtWindow(draw_2),0,0,0,0,True);

}


/*      Do a simple convolution of the image X with the 3x3 mask Y      */

void convolve (int y[3][3])
{
	int i,j,n,m,k,rmax,rmin;
	
	float rng;



/* Locate MAX and MIN for rescaling */
	rmax = XGetPixel(theXImage_1,0,0); rmin = rmax;
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		k = 0;
		for (n=0; n<3; n++)
		   for (m=0; m<3; m++)
		      k += y[n][m]*XGetPixel(theXImage_1,i+n-1,j+m-1);
		if (k>rmax) rmax = k;
		if (k<rmin) rmin = k;
	   }
	rng = (float)(rmax-rmin);

/* Now compute the convolution, scaling */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		k = 0;
		for (n=0; n<3; n++)
		   for (m=0; m<3; m++)
		      k += y[n][m]*XGetPixel(theXImage_1,i+n-1,j+m-1);
		XPutPixel(theXImage_2,i,j,(unsigned long) (((float)(k-rmin)/rng)*256.0));
	   }

	XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2, 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
	

}


/*      Apply a Kirsch edge mask to the image X */

void edge_kirsch_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{


	int i,j,k1,k2,k3,k4,k,rmax,rmin;
	
	float rng;

  if (file_not_loaded) return;

  selection = EDGE_KIRSCH;
  action = SELECT;
  refresh_action();
	

/* Locate MAX and MIN for rescaling */
	rmax = XGetPixel(theXImage_1,0,0); rmin = rmax;
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		k1 = XGetPixel(theXImage_1,i,j+1) + XGetPixel(theXImage_1,i-1,j+1) + XGetPixel(theXImage_1,i+1,j+1) -
		    (XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i+1,j-1));
		k2 = XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i-1,j) + XGetPixel(theXImage_1,i-1,j+1) -
		    (XGetPixel(theXImage_1,i+1,j-1) + XGetPixel(theXImage_1,i+1,j) + XGetPixel(theXImage_1,i+1,j+1));
		k3 = XGetPixel(theXImage_1,i-1,j) + XGetPixel(theXImage_1,i-1,j+1) + XGetPixel(theXImage_1,i,j+1) -
		    (XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i+1,j-1) + XGetPixel(theXImage_1,i+1,j));
		k4 = XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i-1,j) -
		    (XGetPixel(theXImage_1,i,j+1) + XGetPixel(theXImage_1,i+1,j+1) + XGetPixel(theXImage_1,i+1,j));
		k = (k1+k2+k3+k4)/4;
		if (rmax < k) rmax = k;
		if (rmin > k) rmin = k;
	   }
	rng = (float)(rmax-rmin);

/* Now compute the convolution, scaling */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		k1 = XGetPixel(theXImage_1,i,j+1) + XGetPixel(theXImage_1,i-1,j+1) + XGetPixel(theXImage_1,i+1,j+1) -
		    (XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i+1,j-1));
		k2 = XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i-1,j) + XGetPixel(theXImage_1,i-1,j+1) -
		    (XGetPixel(theXImage_1,i+1,j-1) + XGetPixel(theXImage_1,i+1,j) + XGetPixel(theXImage_1,i+1,j+1));
		k3 = XGetPixel(theXImage_1,i-1,j) + XGetPixel(theXImage_1,i-1,j+1) + XGetPixel(theXImage_1,i,j+1) -
		    (XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i+1,j-1) + XGetPixel(theXImage_1,i+1,j));
		k4 = XGetPixel(theXImage_1,i,j-1) + XGetPixel(theXImage_1,i-1,j-1) + XGetPixel(theXImage_1,i-1,j) -
		    (XGetPixel(theXImage_1,i,j+1) + XGetPixel(theXImage_1,i+1,j+1) + XGetPixel(theXImage_1,i+1,j));
		k = (k1+k2+k3+k4)/4;
		XPutPixel(theXImage_2,i,j, (unsigned long) (((float)(k-rmin)/rng)*256.0));
	   }

	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);

	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);

}

void edge_prewitt_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{
 	int i,j,n,m,k,rmax,rmin;
	
	float rng;

  if (file_not_loaded) return;

  selection = EDGE_PREWITT;
  action = SELECT;
  refresh_action();

	

/* Locate MAX and MIN for rescaling */
	rmax = XGetPixel(theXImage_1,0,0); rmin = rmax;
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		n = (XGetPixel(theXImage_1,i-1,j+1)+XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i+1,j+1)) -
		    (XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i+1,j-1));
		m = (XGetPixel(theXImage_1,i+1,j-1)+XGetPixel(theXImage_1,i+1,j)+XGetPixel(theXImage_1,i+1,j+1))-
		    (XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i-1,j+1));
		k = abs(n)+abs(m);
		if (k>rmax) rmax = k;
		if (k<rmin) rmin = k;
	   }
	rng = (float)(rmax-rmin);

/* Now compute the convolution, scaling */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		n = (XGetPixel(theXImage_1,i-1,j+1)+XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i+1,j+1)) -
		    (XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i+1,j-1));
		m = (XGetPixel(theXImage_1,i+1,j-1)+XGetPixel(theXImage_1,i+1,j)+XGetPixel(theXImage_1,i+1,j+1))-
		    (XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i-1,j+1));
		k = abs(n)+abs(m);
		XPutPixel(theXImage_2,i,j, (unsigned long) (((float)(k-rmin)/rng)*256.0));
	   }


       	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}

/*      Apply Frei-Chen edge masks to the image X       */

void edge_frei_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{
	int i,j,n,m,k,rmax,rmin;
	
	float rng,sq2;

  if (file_not_loaded) return;

  selection = EDGE_FREI;
  action = SELECT;
  refresh_action();
		
  sq2 = /*sqrt(2.0);*/ 1.41421 ; /* si pongo sqrt da unresolved ????  */
	

/* Locate MAX and MIN for rescaling */
	rmax =XGetPixel(theXImage_1,0,0); rmin = rmax;
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		n = (XGetPixel(theXImage_1,i-1,j+1)+sq2*XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i+1,j+1)) -
		    (XGetPixel(theXImage_1,i-1,j-1)+sq2*XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i+1,j-1));
		m = (XGetPixel(theXImage_1,i+1,j-1)+sq2*XGetPixel(theXImage_1,i+1,j)+XGetPixel(theXImage_1,i+1,j+1))-
		    (XGetPixel(theXImage_1,i-1,j-1)+sq2*XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i-1,j+1));
		k = abs(n)+abs(m);
		if (k>rmax) rmax = k;
		if (k<rmin) rmin = k;
	   }
	rng = (float)(rmax-rmin);

/* Now compute the convolution, scaling */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		n = (XGetPixel(theXImage_1,i-1,j+1)+sq2*XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i+1,j+1)) -
		    (XGetPixel(theXImage_1,i-1,j-1)+sq2*XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i+1,j-1));
		m = (XGetPixel(theXImage_1,i+1,j-1)+sq2*XGetPixel(theXImage_1,i+1,j)+XGetPixel(theXImage_1,i+1,j+1))-
		    (XGetPixel(theXImage_1,i-1,j-1)+sq2*XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i-1,j+1));
		k = abs(n)+abs(m);
		XPutPixel(theXImage_2,i,j, (unsigned long) (((float)(k-rmin)/rng)*256.0));
	   }


       	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
	
}

/*      Apply the Frei-Chen line masks to the image X   */

void line_frei_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{
 
	int i,j,k,rmax,rmin;
	float rng,sq2;
	int f1,f2,f3,f4;


  if (file_not_loaded) return;

  selection = LINE_FREI;
  action = SELECT;
  refresh_action();



	sq2 = /*sqrt(2.0)*/ 1.41421;
	

/* Locate MAX and MIN for rescaling */
	rmax = (XGetPixel(theXImage_1,0,0)); rmin = rmax;
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
	     f1=XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i+1,j)- XGetPixel(theXImage_1,i,j-1)+
XGetPixel(theXImage_1,i,j+1);
	     f2 = XGetPixel(theXImage_1,i-1,j+1)+ XGetPixel(theXImage_1,i+1,j-1)- XGetPixel(theXImage_1,i-1,j-1)+
XGetPixel(theXImage_1,i+1,j+1);
	     f3 = XGetPixel(theXImage_1,i-1,j-1)+ XGetPixel(theXImage_1,i-1,j+1)+ XGetPixel(theXImage_1,i+1,j-1)+ 
XGetPixel(theXImage_1,i+1,j+1) -2.0*XGetPixel(theXImage_1,i-1,j)+XGetPixel(theXImage_1,i+1,j)+
XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i,j)*4.0;
	     f4 = XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i-1,j)+
XGetPixel(theXImage_1,i+1,j) -2.0*XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i-1,j+1)+
XGetPixel(theXImage_1,i+1,j-1)+ XGetPixel(theXImage_1,i+1,j+1)+ 4.0 * XGetPixel(theXImage_1,i,j);
		k = abs(f1)+abs(f2)+abs(f3)+abs(f4);
		k = (f1,f2,f3,f4);
		if (k>rmax) rmax = k;
		if (k<rmin) rmin = k;
	   }
	rng = (float)(rmax-rmin);

/* Now compute the convolution, scaling */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
	     f1= XGetPixel(theXImage_1,i-1,j)+ XGetPixel(theXImage_1,i+1,j)- XGetPixel(theXImage_1,i,j-1)+
XGetPixel(theXImage_1,i,j+1);
	     f2 = XGetPixel(theXImage_1,i-1,j+1)+XGetPixel(theXImage_1,i+1,j-1)- XGetPixel(theXImage_1,i-1,j-1)+
XGetPixel(theXImage_1,i+1,j+1);
	     f3 = XGetPixel(theXImage_1,i-1,j-1)+XGetPixel(theXImage_1,i-1,j+1)+XGetPixel(theXImage_1,i+1,j-1)+
XGetPixel(theXImage_1,i+1,j+1) -2.0* XGetPixel(theXImage_1,i-1,j)+ XGetPixel(theXImage_1,i+1,j)+ 
XGetPixel(theXImage_1,i,j-1)+ XGetPixel(theXImage_1,i,j+1)+ XGetPixel(theXImage_1,i,j)*4.0;
	     f4 = XGetPixel(theXImage_1,i,j-1)+XGetPixel(theXImage_1,i,j+1)+XGetPixel(theXImage_1,i-1,j)+
XGetPixel(theXImage_1,i+1,j) -2.0*XGetPixel(theXImage_1,i-1,j-1)+ XGetPixel(theXImage_1,i-1,j+1)+
XGetPixel(theXImage_1,i+1,j-1)+	XGetPixel(theXImage_1,i+1,j+1)+4.0* XGetPixel(theXImage_1,i,j);
		k = abs(f1)+abs(f2)+abs(f3)+abs(f4);
		k = max4 (f1,f2,f3,f4);
		XPutPixel(theXImage_2,i,j, (unsigned long)  (((float)(k-rmin)/rng)*256.0));
	   }



	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}

/*      Apply the Marr edge detector to the image X     */

void edge_marr_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{

	int i,j,n,m,k,rmax,rmin;
	int h[11][11];
	
	float rng;
 
  if (file_not_loaded) return;

  selection = EDGE_MARR;
  action = SELECT;
  refresh_action();

	
	

	/* pone la imagen 2 a blanmco. la mia sta ya a negro */ 
	/*for (i=0; i<IMAGE_WIDTH; i++)
	   for (j=0; j<IMAGE_HEIGHT; j++)
		z->data[i,j] = 255;*/

/* Initialize the Laplacian of Gaussian mask */

/* Initialize the Laplacian of Gaussian mask */
h[0][0]=0; h[0][1]=0; h[0][2]=0; h[0][3]=0; h[0][4]=0; h[0][5]=-1; 
h[0][6]=0; h[0][7]=0; h[0][8]=0; h[0][9]=0; h[0][10]=0;
h[1][0]=0; h[1][1]=0; h[1][2]=2; h[1][3]=4; h[1][4]=-2; h[1][5]=-10; 
h[1][6]=-2; h[1][7]=4; h[1][8]=2; h[1][9]=0; h[1][10]=0;
h[2][0]=0; h[2][1]=2; h[2][2]=13; h[2][3]=20; h[2][4]=-13; h[2][5]=-46; 
h[2][6]=-13; h[2][7]=20; h[2][8]=13; h[2][9]=2; h[2][10]=0; 
h[3][0]=0; h[3][1]=4; h[3][2]=20; h[3][3]=30; h[3][4]=-19; h[3][5]=-71; 
h[3][6]=-19; h[3][7]=30; h[3][8]=20; h[3][9]=4; h[3][10]=0; 
h[4][0]=0; h[4][1]=-2; h[4][2]=-13; h[4][3]=-19; h[4][4]=13; h[4][5]=46;
h[4][6]=13; h[4][7]=-19; h[4][8]=-13; h[4][9]=-2; h[4][10]=0; 
h[5][0]=-1; h[5][1]=-10; h[5][2]=-46; h[5][3]=-71; h[5][4]=46; h[5][5]=166; 
h[5][6]=46; h[5][7]=-71; h[5][8]=-46; h[5][9]=-10; h[5][10]=-1;
h[6][0]=0; h[6][1]=-2; h[6][2]=-13; h[6][3]=-19; h[6][4]=13; h[6][5]=46; 
h[6][6]=13; h[6][7]=-19; h[6][8]=-13; h[6][9]=-2; h[6][10]=0;
h[7][0]=0; h[7][1]=4; h[7][2]=20; h[7][3]=30; h[7][4]=-19; h[7][5]=-71; 
h[7][6]=-19; h[7][7]=30; h[7][8]=20; h[7][9]=4; h[7][10]=0; 
h[8][0]=0; h[8][1]=2; h[8][2]=13; h[8][3]=20; h[8][4]=-13; h[8][5]=-46; 
h[8][6]=-13; h[8][7]=20; h[8][8]=13; h[8][9]=2; h[8][10]=0; 
h[9][0]=0; h[9][1]=0; h[9][2]=2; h[9][3]=4; h[9][4]=-2; h[9][5]=-10; 
h[9][6]=-2; h[9][7]=4; h[9][8]=2; h[9][9]=0; h[9][10]=0;
h[10][0]=0; h[10][1]=0; h[10][2]=0; h[10][3]=0; h[10][4]=0; h[10][5]=-1; 
h[10][6]=0; h[10][7]=0; h[10][8]=0; h[10][9]=0; h[10][10]=0;

/* Locate MAX and MIN for rescaling */
	rmax = (XGetPixel(theXImage_1,0,0)); rmin = rmax;
	for (i=5; i<IMAGE_WIDTH-5; i++)
	   for (j=5; j<IMAGE_HEIGHT-5; j++) {
		k = 0;
		for (n=0; n<11; n++)
		   for (m=0; m<11; m++)
		      k += h[n][m]* XGetPixel(theXImage_1,i+n-5,j+m-5);
		if (k>rmax) rmax = k;
		if (k<rmin) rmin = k;
	   }
	rng = (double)(rmax-rmin);

	printf ("Max=%d  Min = %d\n",rmax, rmin);

/* Now compute the convolution, scaling */
	for (i=5; i<IMAGE_WIDTH-5; i++)
	   for (j=5; j<IMAGE_HEIGHT-5; j++) {
		k = 0;
		for (n=0; n<11; n++)
		   for (m=0; m<11; m++)
		      k += h[n][m]* (XGetPixel(theXImage_1,i+n-5,j+m-5));
		XPutPixel(theXImage_2,i,j, (unsigned long)(((float)(k-rmin)/rng)*256.0));
	   }


	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}

/* void edge_marr2_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{

  if (file_not_loaded) return;

  selection = EDGE_MARR2;
  action = SELECT;
  refresh_action();

	int i,j,n,m,k,rmax,rmin;
	int h[11,11];
	int zval, nr, nc;
	FILE *z;


	nr = IMAGE_WIDTH; nc = IMAGE_HEIGHT;
	z = make_vai ("d:z.dat", nr, nc);
	if (z == 0) {
	   *error_code = OUT_OF_STORAGE;
	   return;
	}

	for (i=0; i<nr; i++)
	   for (j=0; j<nc; j++)
		if (put_vai (z, i, j, nr, nc, 255) == 0) {
		   *error_code = IO_ERROR;
		   fclose (z); unlink ("d:z.dat");
		   return;
		}
h[0][0]=0; h[0][1]=0; h[0][2]=0;h[0][3]= -1; h[0][4] = -1; h[0][5] = -2;
 h[0][6] = -1; h[0][7] = -1; h[0][8]=0; h[0][9]=0;h[0][10] = 0;
h[1][0]=0; h[1][1]=0; h[1][2]= -2; h[1][3]= -4; h[1][4] = -8;
 h[1][5] = -9;
 h[1][6] = -8; h[1][7] = -4; h[1][8]= -2; h[1][9]=0;h[1][10] = 0;
h[2][0]=0;h[2][1]= -2;h[2][2]= -7;h[2][3]= -15;h[2][4] = -22;h[2][5]= -23;
 h[2][6] = -22;h[2][7] = -15;h[2][8]= -7;h[2][9]= -2;h[2][10] = 0;
h[3][0]= -1;h[3][1]= -4;h[3][2]= -15;h[3][3]= -24;h[3][4] = -14;h[3][5]= -1;
 h[3][6] = -14;h[3][7] = -24;h[3][8]= -15;h[3][9]= -4;h[3][10] = -1;
h[4][0]= -1;h[4][1]= -8;h[4][2]= -22;h[4][3]= -14;h[4][4] = 52;h[4][5]= 103;
 h[4][6] = 52;h[4][7] = -14;h[4][8]= -22;h[4][9]= -8;h[4][10] = -1;
h[5][0]= -2;h[5][1]= -9;h[5][2]= -23;h[5][3]= -1;h[5][4] = 103;h[5][5]= 178;
 h[5][6] = 103;h[5][7] = -1;h[5][8]= -23;h[5][9]= -9;h[5][10] = -2;
h[6][0]= -1;h[6][1]= -8;h[6][2]= -22;h[6][3]= -14;h[6][4] = 52;h[6][5]= 103;
 h[6][6] = 52;h[6][7] = -14;h[6][8]= -22;h[6][9]= -8;h[6][10] = -1;
h[7][0]= -1;h[7][1]= -4;h[7][2]= -15;h[7][3]= -24;h[7][4] = -14;h[7][5]= -1;
 h[7][6] = -14;h[7][7] = -24;h[7][8]= -15;h[7][9]= -4;h[7][10] = -1;
h[8][0]=0;h[8][1]= -2;h[8][2]= -7;h[8][3]= -15;h[8][4] = -22;h[8][5]= -23;
 h[8][6] = -22;h[8][7] = -15;h[8][8]= -7;h[8][9]= -2;h[8][10] = 0;
h[9][0]=0; h[9][1]=0; h[9][2]= -2; h[9][3]= -4; h[9][4] = -8;
 h[9][5] = -9;
 h[9][6] = -8; h[9][7] = -4; h[9][8]= -2; h[9][9]=0;h[9][10] = 0;
h[10][0]=0; h[10][1]=0; h[10][2]=0;h[0][3]= -1; h[10][4] = -1; h[10][5] = -2;
 h[10][6] = -1; h[10][7] = -1; h[10][8]=0; h[10][9]=0;h[10][10] = 0;

	for (i=5; i<IMAGE_WIDTH-5; i++)
	   for (j=5; j<IMAGE_HEIGHT-5; j++) {
		k = 0;
		for (n=0; n<11; n++)
		   for (m=0; m<11; m++)
		      k += h[n,m]*(XGetPixel(theXImage_1,i+n-5,j+m-5);
		if (put_vai (z, i, j, nr, nc, k)==0) {
		   *error_code = IO_ERROR;
		   fclose(z); unlink ("d:z.dat");
		   return;
		}
	   }

	for (i=5; i<IMAGE_WIDTH-5; i++)
	   for (j=5; j<IMAGE_HEIGHT-5; j++) {
		   rmin = (XGetPixel(theXImage_1,i-1,j); rmax = rmin;
		   for (n= -1; n<=1; n++)
		      for (m= -1; m<=1; m++)
			 if (n!=0 || m!=0) {
			   if (get_vai (z, i+n,j+m, nr,nc, &zval)== 0) {
			      *error_code = IO_ERROR;
			      fclose (z);  unlink ("d:z.dat");
			      return;
			   }
			   if (zval > rmax) rmax = zval;
			   if (zval < rmin) rmin = zval;
			 }
		   if (get_vai (z, i, j, nr, nc, &k) == 0) {
		      *error_code = IO_ERROR;
		      fclose (z);  unlink ("d:z.dat");
		      return;
		   }
		   if ((k>t &&rmin< -t) || (k< -t && rmax>t))
			(XGetPixel(theXImage_1,i,j) = 0;
		   else (XGetPixel(theXImage_1,i,j) = 255;
	   }
	   fclose (z);
	   unlink ("d:z.dat");
}*/


/*      Apply the Roberts edge detector to the image X  */

void edge_roberts_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{
 int i,j,n,m;

  if (file_not_loaded) return;

  selection = EDGE_ROBERTS;
  action = SELECT;
  refresh_action();



	for (i=0; i<IMAGE_WIDTH-1; i++)
	   for (j=0; j<IMAGE_HEIGHT-1; j++) {
		n = XGetPixel(theXImage_1,i+1,j+1)-XGetPixel(theXImage_1,i,j);
		m = XGetPixel(theXImage_1,i,j+1)- XGetPixel(theXImage_1,i+1,j);
		m = (abs(n)+abs(m))/2;
	}
	XPutPixel(theXImage_2,i,j, (unsigned char)m);
	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);

}

/*      Locate vertical edges in X using a simple gradient      */

void edge_vert_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{


	int mask[3][3];

  if (file_not_loaded) return;

  selection = EDGE_VERT;
  action = SELECT;
  refresh_action();


	mask[0][0]=0; mask[0][1]=0; mask[0][2]=0;
	mask[1][0] = -1; mask[1][1] = 1; mask[1][2]=0;
	mask[2][0]=0; mask[2][1]=0; mask[2][2]=0;
	convolve (mask);

/* Despues de usar convolve, hacer un XClear () para que pinte la imagen*/

	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}

/*      Locate horizontal edges in X using a simple gradient    */



/*----------------------------------------------------------------------*/
/*   				Dilate 		   			*/
/*									*/
/*  						   			*/
/*----------------------------------------------------------------------*/

void create_edge_hv_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate("Enter no. of iterations", XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  edge_hv_dialog = XmCreatePromptDialog(temp_widget, "Iterations",
					  args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(edge_hv_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(edge_hv_dialog, XmNokCallback,
		(XtCallbackProc) ok_edge_hvbutton_callback, (XtPointer) NULL);
  XtAddCallback(edge_hv_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_edge_hv_callback, (XtPointer) NULL);

  XmStringFree(message);
}

void activate_edge_hv_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data) {
  if (yhs_files_open > 0 && yhs_files_open < 20 && running == 0 && yhs_filename[0] != 0){
   selection = EDGE_HV;
 
  action = SELECT;
  refresh_action();
  XtManageChild(edge_hv_dialog);}
}

void deactivate_edge_hv_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged *
   * whenever any of its buttons are pressed.              */
}

static void ok_edge_hvbutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data) {
  int iterations;
  char *thresh;

  /* Get threshold value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thresh);
   XtUnmanageChild(edge_hv_dialog);
   XFlush(XtDisplay(edge_hv_dialog));
  iterations = atoi(thresh);
  if (yhs_files_open > 0 && yhs_files_open < 20 && running == 0)
{
  if ( iterations<1 || iterations>500 )
    XBell(XtDisplay(w),100);
  else
    edge_hv(iterations);
}}

static void cancel_edge_hv_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data) {
  deactivate_edge_hv_dialog();
}


void edge_hv(int iterations)
{
        Arg al[10];
    int ac,s,t;
    int current;
    FILE *p_file;
int i,j,n;
    int a,b;
	unsigned long  max=0, aux[512][512], aux1[512][512];
fprintf(stderr,"\nDilating...\n");
    for (a=0;a<512;a++){for (b=0;b<512;b++) { array1[a][b]=0; array2[a][b]=0; }}

  if (yhs_files_open > 0 && yhs_files_open < 20 && running == 0 && yhs_filename[0] != 0)
{
running=1;

    if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);

for (i=0; i<IMAGE_WIDTH; i++)
	   for (j=0; j<IMAGE_HEIGHT; j++)
	     {aux1[i][j]= array1[i][j];
	      aux[i][j]= array1[i][j];
	     }

for (n=0; n<iterations; n++)
 {
     for (i=0; i<IMAGE_WIDTH; i++)
	   for (j=0; j<IMAGE_HEIGHT; j++) 
     {
         for (s= -1; s<=1; s++)
	for (t= -1; t<=1; t++) 
	 if ( (i+s)>=0 && (i+s)<IMAGE_WIDTH && (j+t)>=0 && (j+t)<IMAGE_HEIGHT )
		 if (aux[i+s][j+t] > max) max= aux[i+s][j+t];   
    
      aux1[i][j]=max;
      max=0;
   }
    for (i=1; i<IMAGE_WIDTH-1; i++)
	for (j=1; j<IMAGE_HEIGHT-1; j++)
	    aux[i][j]= aux1[i][j];
 }


for (i=0; i<IMAGE_WIDTH; i++)
	   for (j=0; j<IMAGE_HEIGHT; j++)
		array2[i][j]=aux1[i][j];

  /* Copy image into pixmap */
/*  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
              0, 0, 0, 0, theXImage_2->width, theXImage_2->height); */

  /* Clear the drawing window so the image is displayed again */
       strcpy(tempfileold,tempfilenew); /* copy the new filename to the old file i.e. save the old file */
       strcat(tempfilenew,addcharac); /* new free temp file */
  if((p_file = fopen(tempfileold,"w")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s - Temporary file for writing.\n",tempfileold);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { putc(array2[a][b],p_file); }}
    fclose(p_file);
    run_once=0;
    file_loader=1;
    file_yhs=tempfileold;
/* XClearArea(XtDisplay(draw_2), XtWindow(draw_2), 0, 0, 0, 0, True); */
running=0;
}}

/*----------------------------------------------------------------------*/
/*   				Erode		   			*/
/*									*/
/*  						   			*/
/*----------------------------------------------------------------------*/

void create_edge_dilate_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate("Enter no. of iterations", XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  edge_dilate_dialog = XmCreatePromptDialog(temp_widget, "Iterations",
					  args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(edge_dilate_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(edge_dilate_dialog, XmNokCallback,
		(XtCallbackProc) ok_edge_dilatebutton_callback, (XtPointer) NULL);
  XtAddCallback(edge_dilate_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_edge_dilate_callback, (XtPointer) NULL);
  
  XmStringFree(message);
}

void activate_edge_dilate_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data) {
  if (yhs_files_open > 0 && yhs_files_open < 20 && yhs_filename[0] != 0){
   selection = EDGE_HV;
 
  action = SELECT;
  refresh_action();
  running=0;
  XtManageChild(edge_dilate_dialog);}
}

void deactivate_edge_dilate_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged *
   * whenever any of its buttons are pressed.              */
}

static void ok_edge_dilatebutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data) {
  int iterations;
  char *thresh;
  if (yhs_files_open > 0 && yhs_files_open < 20 && running==0 && yhs_filename[0] != 0)
{
running=1;
  /* Get threshold value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thresh);

  iterations = atoi(thresh);
  XtUnmanageChild(edge_dilate_dialog);
  XFlush(XtDisplay(edge_dilate_dialog));
  if ( iterations<1 || iterations>500 )
    XBell(XtDisplay(w),100);
  else
    edge_dilate(iterations);
running=0;
}}

static void cancel_edge_dilate_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data) {
  deactivate_edge_dilate_dialog();
}

void edge_dilate(int loop)
{
     Arg al[10];
    int ac,s,t;
    int lp;
    int current;
    FILE *p_file;
    int a,b;
	int mask[3][3], i, j;
	unsigned long aux[512][512], min=255;unsigned long aux1[512][512];
fprintf(stderr,"Eroding...\n");
    for (a=0;a<512;a++){for (b=0;b<512;b++) { array1[a][b]=0; array2[a][b]=0; }}  
running=1;
  selection = EDGE_HORIZ;
  action = SELECT;
  refresh_action();
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);

	for (i=0; i<512; i++)
	   for (j=0; j<512; j++)
	     {aux[i][j]= array1[i][j];
		aux1[i][j]= array1[i][j];}

for (lp=0;lp<loop+1;lp++)
{
 for (i=0; i<512; i++)
	   for (j=0; j<512; j++) 
     {
        for (s= -1; s<=1; s++)
	for (t= -1; t<=1; t++) 
	 if ( (i+s)>=0 && (i+s)<512 && (j+t)>=0 && (j+t)<512 )
		 if (aux[i+s][j+t] <min) min=aux[i+s][j+t];   
    
      aux1[i][j]=min;
      min=255;
   }
 for (i=0; i<512; i++)
	for (j=0; j<512; j++)
	    aux[i][j]= aux1[i][j];
 }
   for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array2[a][b]=aux1[a][b]; }}

      strcpy(tempfileold,tempfilenew); /* copy the new filename to the old file i.e. save the old file */
       strcat(tempfilenew,addcharac); /* new free temp file */
  if((p_file = fopen(tempfileold,"w")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s - Temporary file for writing.\n",tempfileold);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { putc(array2[a][b],p_file); }}
    fclose(p_file);
    run_once=0;
    file_loader=1;
    file_yhs=tempfileold;
running=0;
}


/*      Apply unsharp masking to the image X    */

void edge_unsharp_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{

	int mask[3][3];

  if (file_not_loaded) return;

  selection = EDGE_UNSHARP;
  action = SELECT;
  refresh_action();


	mask[0][0]= -1; mask[0][1]= -1; mask[0][2]= -1;
	mask[1][0] =  -1; mask[1][1] = 8; mask[1][2]= -1;
	mask[2][0]= -1; mask[2][1]= -1; mask[2][2]= -1;
	convolve (mask);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True); 
}

int max4 (int a, int b, int c, int d)
{

	if (b>a) a = b;
	if (d>c) c = d;
	if (a>c) return a;
	return c;
}

int min4 (int a, int b, int c, int d)
{
	if (b < a) a = b;
	if (d < c) c = d;
	if (a < c) return a;
	return c;
}

/*      Replace each pixel by the mean of the surrounding pixels        */

void mean_filter_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{
 
	int i,j,ii,jj,n, sum;

  if (file_not_loaded) return;

  selection = MEAN_FILTER;
  action = SELECT;
  refresh_action();



	for (i=0; i<IMAGE_WIDTH; i++)
	   for (j=0; j<IMAGE_HEIGHT; j++) {
	      n = 0; sum = 0;
	      for (ii= -1; ii<=1; ii++) 
		 for (jj = -1; jj<=1; jj++) 
		    if (range ( i+ii, j+jj)) {
			sum += XGetPixel(theXImage_1,i+ii,j+jj);
			n++;
		    }
	      if (n) XPutPixel(theXImage_2,i,j, (unsigned long) sum/n);
	      else XPutPixel(theXImage_2,i,j, 0);
	   }
	
	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}


/*      Noise reduction using the mask:   1 1 1
					  1 2 1
					  1 1 1                 */

void smooth_mask1_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data)
{
 
	int mask[3][3],i,j,n,m,k;


  if (file_not_loaded) return;

  selection = SMOOTH_MASK1;
  action = SELECT;
  refresh_action();



/* Initialize the mask */
	for (i=0; i<3; i++)
	   for (j=0; j<3; j++) mask[i][j] = 1;
	mask[1][1] = 2;

/* Compute the mask value at each pixel (can't be done in place) */
	for (i=1; i<IMAGE_WIDTH-1-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1-1; j++) {
		k = 0;
		for (n=0; n<3; n++)
		   for (m=0; m<3; m++)
		      k += mask[n][m]*XGetPixel(theXImage_1,i+n-1,j+m-1);
		k = k/10;
		XPutPixel(theXImage_2,i,j, (unsigned long)k);
	   }

	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);
}

/*      Noise reduction using the mask:    1 2 1
					   2 4 2
					   1 2 1        */

void smooth_mask2_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{
 
	int mask[3][3],i,j,n,m,k;

  if (file_not_loaded) return;

  selection = SMOOTH_MASK2;
  action = SELECT;
  refresh_action();

/* Initialize the mask */
	for (i=0; i<3; i++)
	   for (j=0; j<3; j++) mask[i][j] = 1;
	mask[1][1] = 4;
	mask[1][0] = 2; mask[1][2] = 2; mask[0][1] = 2; mask[2][1] = 2;
	
/* Apply the mask to each pixel */
	for (i=1; i<IMAGE_WIDTH-1-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1-1; j++) {
		k = 0;
		for (n=0; n<3; n++)
		   for (m=0; m<3; m++)
		      k += mask[n][m]*XGetPixel(theXImage_1,i+n-1,j+m-1);
		k = k/16;
		XPutPixel(theXImage_2,i,j, (unsigned long)k);
	   }


	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);

}


void create_smooth_t_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(SMOOTH_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  smooth_t_dialog = XmCreatePromptDialog(temp_widget, "threshold dialog",
					  args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(smooth_t_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(smooth_t_dialog, XmNokCallback,
		(XtCallbackProc) ok_smooth_tbutton_callback, (XtPointer) NULL);
  XtAddCallback(smooth_t_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_smooth_t_callback, (XtPointer) NULL);

  XmStringFree(message);
}

void activate_smooth_t_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data) {
  if (file_not_loaded) return;
  selection = SMOOTH_T;
  action = SELECT;
  refresh_action();
  XtManageChild(smooth_t_dialog);
}

void deactivate_smooth_t_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged *
   * whenever any of its buttons are pressed.              */
}

static void ok_smooth_tbutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data) {
  int t;
  char *thresh;

  /* Get threshold value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thresh);

  t = atoi(thresh);

  if ( t<0 || t>255 )
    XBell(XtDisplay(w),100);
  else
    smooth_t(t);
}

static void cancel_smooth_t_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data) {
  deactivate_smooth_t_dialog();
}

/*      Smooth an image using a threshold. If the difference between the
	pixel and the mean of the 3x3 region around that pixel is less
	than the threshold T then replace the pixel by the mean.        */

void smooth_t(int t)
{

int i,j,n,m,k;

  if (file_not_loaded) return;

  selection = SMOOTH_T;
  action = SELECT;
  refresh_action();


/* Compute the mean about each pixel */
	for (i=1; i<IMAGE_WIDTH-1-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1-1; j++) {
		k = 0;
		for (n=0; n<3; n++)
		   for (m=0; m<3; m++)
		      k += XGetPixel(theXImage_1,i+n-1,j+m-1);
		k = (int)( (double)k/9.0 );

/* The difference between the pixel and the mean is K */
		m = abs(XGetPixel(theXImage_1,i,j)-k);

/* Test the difference against the threshold */
		if (m<t) XPutPixel(theXImage_2,i,j, (unsigned long)k);
	   }

	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(draw_2), XtWindow(draw_2), 0, 0, 0, 0, True);

}

/*      Perform a 5 or 13 point median filter, according to the parameter
	SIZE, to the image X: replace each pixel by the median of a small
	region centered at that pixel.                                  */

void median_filter_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{

int dat[14], i,j,nn,mm,k1;
int size = 13; /* PUEDE SER 5 */

  if (file_not_loaded) return;

  selection = MEDIAN_FILTER;
  action = SELECT;
  refresh_action();

/* SIZE should be 5 or 13; if not, 5 will be assumed. */
	if (size != 5 && size != 13) size = 5;


	nn = IMAGE_WIDTH-1;
	mm = IMAGE_HEIGHT-1;

/* Examine all pixels in the source image */
	for (i=1; i<IMAGE_WIDTH-1; i++)
	   for (j=1; j<IMAGE_HEIGHT-1; j++) {
		k1=0;

/* Assemble the pixels in the region into the array DAT */
		dat[0] = XGetPixel(theXImage_1,i,j); k1 = 1;
		if ((j-1) >= 0) { dat[k1] = XGetPixel(theXImage_1,i,j-1); k1++; }
		if ((j+1) < mm) { dat[k1] = XGetPixel(theXImage_1,i,j+1); k1++; }
		if ((i-1) >= 0) { dat[k1] = XGetPixel(theXImage_1,i-1,j); k1++; }
		if ((i+1) < nn) { dat[k1] = XGetPixel(theXImage_1,i+1,j); k1++; }
		if (size == 13) {
		 if(i-2>=0) { dat[k1] = XGetPixel(theXImage_1,i-2,j); k1++; }
		 if(i+2<nn) { dat[k1] = XGetPixel(theXImage_1,i+2,j); k1++; }
		 if (j-2 >= 0) { dat[k1] = XGetPixel(theXImage_1,i,j-2); k1++; }
		 if (j+2 < mm) { dat[k1] = XGetPixel(theXImage_1,i,j+2); k1++; }
		 if ((i-1>=0) && (j-1>=0)) {dat[k1]=XGetPixel(theXImage_1,i-1,j-1); k1++; }
		 if ((i+1<nn) && (j-1>=0)) {dat[k1]=XGetPixel(theXImage_1,i+1,j-1); k1++; }
		 if ((i-1>=0) && (j+1<mm)) {dat[k1]=XGetPixel(theXImage_1,i-1,j+1); k1++; }
		 if ((i+1<nn) && (j+1<mm)) {dat[k1]=XGetPixel(theXImage_1,i+1,j+1); k1++; }
		}

/* Sort DAT */
		medsort (dat, k1);

/* Select the 'middle' value from DAT */
		k1 = (k1-1)/2;
		if (k1 < 0) k1 = 0;

		XPutPixel(theXImage_2,i,j, (unsigned long)(dat[k1]));
/* aqui en el codigo ponia unsigned char ???? */
	}
	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);

}

void median_vf_callback(Widget w, XtPointer client_data,
			 XmAnyCallbackStruct *call_data)
{

int dat[14], i,j,nn,mm,k1, k;
int size = 5;

  if (file_not_loaded) return;

  selection = MEDIAN_VF;
  action = SELECT;
  refresh_action();

/* SIZE should be 5 or 13; if not, 5 will be assumed. */
	if (size != 5 && size != 13) size = 5;

/* Create the temporary image */

	nn = IMAGE_WIDTH-1;
	mm = IMAGE_HEIGHT-1;

/* Examine all pixels in the source image */
	for (i=1; i<nn; i++)
	   for (j=1; j<mm; j++) {
		k1=0;
		for (k=i-size/2; k<=i+size/2; k++)
		   if (range(k,j)) 
			   dat[k1++] = XGetPixel(theXImage_1,k,j);

/* Sort DAT */
		medsort (dat, k1);

/* Select the 'middle' value from DAT */
		k1 = (k1-1)/2;
		if (k1 < 0) k1 = 0;
		XPutPixel(theXImage_2,i,j, (unsigned long)(dat[k1]));
	}

	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(w), XtWindow(draw_2), 0, 0, 0, 0, True);

}

/*      Sort the integer array ARR having N elements    */

void medsort(int *arr, int n)

{
 
int i,j,a;

 

	for (j=1; j<n; j++) {
	   a = arr[j];
	   i = j - 1;
	   while (i>=0 && arr[i]>a) {
		arr[i+1] = arr[i];
		i--;
	   }
	   arr[i+1] = a;
	}
}



void create_hough_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(THRESHOLD_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  hough_dialog = XmCreatePromptDialog(temp_widget, "threshold dialog",
					  args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(hough_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */

  XtAddCallback(hough_dialog, XmNokCallback,
		(XtCallbackProc) ok_hough_callback, (XtPointer) NULL);
  XtAddCallback(hough_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_hough_callback, (XtPointer) NULL);

  XmStringFree(message);
}

void activate_hough_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data) {
  if (file_not_loaded) return;
  selection = HOUGH;
  action = SELECT;
  refresh_action();
  XtManageChild(hough_dialog);
}

void deactivate_hough_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged *
   * whenever any of its buttons are pressed.              */
}

static void ok_hough_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data) {
  int t;
  char *thresh;

  /* Get threshold value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thresh);

  t = atoi(thresh);

  if ( t<0 || t>255 )
    XBell(XtDisplay(w),100);
  else
    hough(t);
}

static void cancel_hough_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data) {
  deactivate_hough_dialog();
}


/*      Compute the Hough transformation on pixels in image X having a
	value less than the threshold T. Writes the resulting image
	to the Alpha format file named 'hough.alp'.                     */

void hough(int t)
{

	int center_x, center_y, r, omega, i, j, rmax;
	double conv, rr;
	static double sarr[180], carr[180];


	conv = 3.1415926535/180.0;
	center_x = (IMAGE_HEIGHT-1)/2;       center_y = (IMAGE_WIDTH-1)/2;
	rmax = (int)(sqrt((double)((IMAGE_HEIGHT-1)*(IMAGE_HEIGHT-1) +
			  (IMAGE_WIDTH-1)*(IMAGE_WIDTH-1)))/2.0);
;

/* Allocate the Hough image: 180 degrees by twice the
   maximum possible distance found in the image X. */
	/*z = newimage (180, 2*rmax+1, error_code);*/

/* Initialize a table of sine and cosine values */

	for (omega=0; omega<180; omega++) {
		sarr[omega] = 0.5 /*sin((double)(omega*conv))*/;
		carr[omega] = 0.5 /*cos((double)(omega*conv))*/;
	}

/* Clear the Hough image */

	for (r = 0; r < 2 * rmax+1; r++)
	   for (omega = 0; omega < 180; omega++)
		XPutPixel(theXImage_2,omega,r, 0);


/* Transform each pixel in X into Hough coordinate (r,omega) if
   it has a value <= T. Increment all Hough pixels that correspond */

	for (i = 0; i < IMAGE_WIDTH-1; i++)
	   for (j = 0; j < IMAGE_HEIGHT-1; j++)
		if (XGetPixel(theXImage_1,i,j) <= threshold)
		   for (omega = 0; omega < 180; ++omega) {
			rr = (j - center_y) * sarr[omega]
			   - (i - center_x) * carr[omega];
			if (rr < 0.0) r = (int)rr;
			 else r = (int)rr + 1;
			if (XGetPixel(theXImage_2,omega,rmax+r)<255)
			XPutPixel(theXImage_2,omega,rmax+r, XGetPixel(theXImage_2,omega,rmax+r)+ 1);
		   }

	XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2,theXImage_2, 0, 0, 0, 0,theXImage_2->width,theXImage_2->height);
	XClearArea(XtDisplay(draw_2), XtWindow(draw_2), 0, 0, 0, 0, True);

}


/* Function definition */


void histogram_callback(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data)
{
  int x,y,i,j;
  double offset;
  long min, max;
  long hist[256], k;
  XPoint points[3];

  if (file_not_loaded) return;

  selection = HISTOGRAM;
  action = SELECT;
  refresh_action();

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
    for(j=0; j<IMAGE_HEIGHT; j++)
	 XPutPixel(theXImage_2,i,j,bg);

  /* Copy image into pixmap */
  XPutImage(XtDisplay(w), thePixmap_2, image_gc_2, theXImage_2,
	    0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Construct a grey-level histogram of the image theXImage_1 */
  for (i=0; i<256; i++)
    hist[i] = 0L;

  for (i=0; i<IMAGE_WIDTH; i++)
    for (j=0; j<IMAGE_HEIGHT; j++) {
      k = XGetPixel(theXImage_1,i,j);
      hist[k] += 1;
    }

  /* Calculate minimun and maximun values of the histogram */
  min = 262144L;
  max = 0L;
  for (i=0; i<256; i++) {
    if (hist[i] > max) max = hist[i];
    if (hist[i] < min) min = hist[i];
  }

  /* Draw frame into pixmap */
  XDrawRectangle(XtDisplay(w),thePixmap_2,image_gc_2,29,29,451,451);

  /* Draw axis into pixmap */
  points[0].x = 129;     points[0].y = 63;
  points[1].x = 129;     points[1].y = 63+400;
  points[2].x = 129+255; points[2].y = 63+400;
  XDrawLines(XtDisplay(w),thePixmap_2,image_gc_2,points,3,CoordModeOrigin);

  /* Draw histogram values into pixmap */
  for (i=0; i<256; i++) {
    x = 129+i;
    offset = (double) ((0-400)/(double)(max-min))*(double)(hist[i]-min)+400;
    y = 63 + (int) offset;
    XDrawLine(XtDisplay(w),thePixmap_2,image_gc_2,x,y,x,63+399);
  }

  /* Copy pixmap into image */
  theXImage_2 = XGetImage(XtDisplay(w), thePixmap_2, 0, 0,
			  theXImage_2->width, theXImage_2->height,
			  AllPlanes, ZPixmap);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(w),XtWindow(draw_2),0,0,0,0,True);
}

/* Histogram equalize the image X. */
void histo_eq_callback(Widget w, XtPointer client_data,
		       XmAnyCallbackStruct *call_data)
{

  /* Histogram equalization */
  int i,j,k;
  unsigned long histo[256]; /* Histogram of the image */
  unsigned long cum[256];   /* Cumulative sum of h    */
  unsigned long ideal[256]; /* Ideal cumulative sum   */
  unsigned long map[256];   /* Old to new level map   */

  if (file_not_loaded) return;

  selection = HISTO_EQ;
  action = SELECT;
  refresh_action();

  /* Construct a grey-level histogram of theXImage_1 */
  for (i=0; i<256; i++)
    histo[i] = 0L;

  for (i=0; i<IMAGE_WIDTH; i++)
    for (j=0; j<IMAGE_HEIGHT; j++) {
      k = XGetPixel(theXImage_1,i,j);
      histo[k] += 1;
    }

  /* Generate the cumulative sum */
  cum[0] = histo[0];
  for (i=1; i<256; i++)
    cum[i] = cum[i-1] + histo[i];

  for (i=0; i<256; i++) map[i] = i;
  ideal[0] = (IMAGE_WIDTH*IMAGE_HEIGHT)/256;
  for (i=1; i<256; i++) ideal[i] = ideal[i-1] + ideal[0];

  j=0; i=0;
  while (j<256) {
    while (cum[i] < ideal[j]) {
      i++;
      map[i] = j;
    }
    j++;
  }

  for (i=0; i<IMAGE_WIDTH; i++)
    for (j=0; j<IMAGE_HEIGHT; j++) {
       k = XGetPixel(theXImage_1,i,j);
       XPutPixel(theXImage_2,i,j,map[k]);
    }

  /* Copy image into pixmap */
  XPutImage(XtDisplay(w), thePixmap_2, image_gc_2, theXImage_2,
	    0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(w),XtWindow(draw_2),0,0,0,0,True);
}

void create_threshold_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(THRESHOLD_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  threshold_dialog = XmCreatePromptDialog(temp_widget, "threshold dialog",
					  args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(threshold_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(threshold_dialog, XmNokCallback,
		(XtCallbackProc) ok_threshbutton_callback, (XtPointer) NULL);
  XtAddCallback(threshold_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_threshbutton_callback, (XtPointer) NULL);

  XmStringFree(message);
}

void activate_threshold_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data) {
  if (file_not_loaded) return;
  selection = THRESHOLD;
  action = SELECT;
  refresh_action();
  XtManageChild(threshold_dialog);
}

void deactivate_threshold_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged *
   * whenever any of its buttons are pressed.              */
}

static void ok_threshbutton_callback(Widget w, XtPointer client_data,
			       XmSelectionBoxCallbackStruct *call_data) {
  int t;
  char *thresh;

  /* Get threshold value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thresh);

  t = atoi(thresh);

  if ( t<0 || t>255 )
    XBell(XtDisplay(w),100);
  else
    thresholding(t);
}

static void cancel_threshbutton_callback(Widget w, XtPointer client_data,
				   XmSelectionBoxCallbackStruct *call_data) {
  deactivate_threshold_dialog();
}

/* Threshold the image 1. Any pixels with a level less than T will be set *
 * to background color; others will be set to foreground color.           */
static void thresholding(int t)
{
  int i,j;

  for (i=0; i<IMAGE_WIDTH; i++)
    for (j=0; j<IMAGE_HEIGHT; j++)
      if (XGetPixel(theXImage_1,i,j) < (unsigned long) t)
	XPutPixel(theXImage_2,i,j,bg);
      else
	XPutPixel(theXImage_2,i,j,fg);

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
	    0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);
}
