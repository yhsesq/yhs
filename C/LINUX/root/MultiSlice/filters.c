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
/*--------------------------------------------------------------*/
/*								*/
/*	File: filters.c						*/
/*								*/
/*	C-T Image Application Program				*/
/*								*/
/*	OSF/Motif version.					*/
/*								*/
/*--------------------------------------------------------------*/                                                             

#include "main.h"
#include <math.h>
#include <Xm/Xm.h>
#include <Xm/PushB.h>
#include <Xm/CascadeB.h>
#include <Xm/SelectioB.h>
#include <Xm/Separator.h>
#include <Xm/RowColumn.h>

#define SIZE1 200
#define SIZE2 100
#define min(a,b) ((a) < (b)?(a):(b))
#define max(a,b) ((a) > (b)?(a):(b))

#define f1 1
#define f2 2
#define f3 3
#define f4 4
#define f5 5
#define f6 6
#define f7 7

#define SIGMA_MESSAGE "Enter sigma value (1.0-20):"
#define SIGMAVALUE    "Introduce sigma value"

typedef float matrix[IMAGE_WIDTH][IMAGE_HEIGHT];

extern Widget   main_window;
extern Cursor	theCursor;

/* Extern variables */
extern char *action;
extern int yhs_files_open;
extern int yhs_filename[21];
extern int selection;
extern Widget draw_1,draw_2;
extern GC image_gc_2;
extern Pixmap thePixmap_2;
extern XImage *theXImage_1;
extern XImage *theXImage_2;
extern int file_not_loaded;
extern unsigned long fg, bg;
extern void refresh_action(void);
extern int figure;
extern int array1[512][512];
extern int array2[512][512];
extern int running;
extern char *yhs_filename1;

int filter;
float top;

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;
static Widget sigma_dialog = (Widget) NULL;


/* Procedures to apply the convolution operation */
static void convo_vectorx(matrix m, float *mask, int len,
	float *max, float *min, float resul[][IMAGE_HEIGHT]);

static void convo_vectory(matrix m, float *mask, int len, float *max,
	float *min, float resul[][IMAGE_HEIGHT]);

static void convolution (matrix m, float y[SIZE2][SIZE2],int length,
	float *max, float *min, float resul[][IMAGE_HEIGHT]);

extern void watershed_callback(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);

/* Procedures to get the masks */
void get_gaussian(float s, float *y, int *len);

void get_derigaussian(float s, float *y, int *len);

void get_2derigaussian(float s, float *y, int *len);


/*Gaussian Filter*/
void create_sigma_dialog(Widget parent);
void activate_gaussian_dialog(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);
void deactivate_sigma_dialog(void);
void ok_sigmabutton_callback(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data);
void cancel_sigmabutton_callback(Widget w, XtPointer client_data, 
			XmSelectionBoxCallbackStruct *call_data);
void gaussian_filter(float sigma);


/* Derivative Gaussian Filter */
void activate_dgaussian_dialog(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);
void dgaussian_filter(float sigma);


/* Second derivative of Gaussian filter */
void activate_d2gaussian_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data);
void d2gaussian_filter(float sigma);


/*Derivative of a Gaussian in rows and Gaussian in columns.*/
void activate_dgaux_gauy_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void dgaux_gauy_filter(float sigma);


/*Derivative of a Gaussian in columns and Gaussian in rows.*/
void activate_dgauy_gaux_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void dgauy_gaux_filter(float sigma);


/* Total Gradient= sqrt(pow(dgauxgauy,2)+pow(dgauygaux,2)) */
void activate_total_gradient_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void total_gradient_filter(float sigma);


/* Gradient_x_y = sqrt(pow(gaussian_x,2)+pow(gaussian_y,2) */
void activate_gradient_x_y_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void gradient_x_y_filter(float sigma);


/* Difference x filter, differences between horizontal adjacent points */
void differencex_filter();


/* Difference y filter, differences between vertical adjacent points */
void differencey_filter();


/* gradient = sqrt( pow(differencex,2)+pow(differencey,2)) */
void gradient_filter();



/* Makes the convolution for the x direction */
void convo_vectorx(matrix m, float *mask, int len,
	 float *max, float *min, float resul[][IMAGE_HEIGHT]){
  float conv[IMAGE_WIDTH+SIZE1];
  int i,j,k,first;

  first = 1;
  for(i=0;i<IMAGE_WIDTH;i++){
    for(k=0;k<512+len;k++){
	conv[k]=0;
	for(j=max(0,k-len);j<=min(k,511);j++)
	  conv[k]+= m[i][j]* mask[k-j];
	if (first==1){*max=conv[k];
		      *min=conv[k];
		      first=2;}
	if (conv[k]<*min) *min=conv[k];
	if (conv[k]>*max) *max=conv[k];
    }
    for(j=0;j<512;j++)
	resul[i][j]=conv[j+(len/2)];
  }
}


/* Makes the convolution for the y direction */
void convo_vectory(matrix m, float *mask, int len, float *max,
		 float *min, float resul[][IMAGE_HEIGHT]){
  float conv[IMAGE_WIDTH+SIZE1];
  int i,j,k,first;

  first = 1;
  for(j=0;j<IMAGE_WIDTH;j++){
    for(k=0;k<512+len;k++){
	conv[k]=0;
	for(i=max(0,k-len);i<=min(k,511);i++)
	  conv[k]+= m[i][j]* mask[k-i];
	if (first==1){*max=conv[k];
		      *min=conv[k];
		      first=2;}
	if (conv[k]<*min) *min=conv[k];
	if (conv[k]>*max) *max=conv[k];
    }
    for(i=0;i<512;i++)
	resul[i][j]=conv[i+(len/2)];
  }
}


/* Procedure to convolution a mask matrix with the Ximage. */
/* The dimensions of the matrix are [0..length][0..length]. */
static void convolution (matrix ima, float y[SIZE2][SIZE2],int length,
	float *max, float *min, float resul[][IMAGE_WIDTH])
{
  int i,j,m,n,lon,first;
  float k;

  /* Locate MAX and MIN for rescaling */
  /* Variable lon is used to calculate the central pixel in the mask. */
  lon=length/2;
  
  
  for (i=lon; i<IMAGE_WIDTH-lon; i++)
    for (j=lon; j<IMAGE_HEIGHT-lon; j++)
    {
	k = 0;
	for (n=0; n<=length; n++)
	  for (m=0; m<=length; m++)
	    k += y[n][m]*ima[i+n-lon][j+m-lon];
      
        /* Store the values obtained in the output matrix */
	resul[i][j]=k;

	/* Updating the values of rmax and rmin. */
	if (first == 1) {*max = k;
			 *min = k;
			 first = 2;}
	if (k>*max) *max = k;
        if (k<*min) *min = k;
    }

}


/* Calculate a vectorial Gaussian mask with a standar deviation of sigma. */
/* Cut off when tails reach 1% of the maximum value. */
void get_gaussian(float s, float *y, int *len){
  int r,i;
  float gaussian_r;
   
  r=1;
  gaussian_r=1;
  *len=0;
  y[*len]=gaussian_r;
  while(gaussian_r >= 0.01){
	gaussian_r=exp(-0.5*pow(r,2)/pow(s,2));
	if (gaussian_r>=0.01){
          for (i=*len;i>=0;i--) y[i+1]=y[i];
 	  i=*len;
	  y[i+2]=gaussian_r;
	  y[0]=gaussian_r;
	  r=r+1;
	  *len=i+2;
	
	}/*end if*/
  
  }/*end while*/

}/*end get_gaussian*/


/* Calculate a vectorial derivate of a Gaussian mask with a standar deviation */
/* of sigma. Cut off when tails reach 1% of the maximum value.*/
void get_derigaussian(float s, float *y, int *len){
  int r,i,j,follow;
  float deriv_gaussian_r;
  float max_val;

  r=1;
  max_val=0;
  *len=0;
  deriv_gaussian_r=max_val;
  y[*len]=deriv_gaussian_r;
  while (deriv_gaussian_r>=0.01*max_val){
	deriv_gaussian_r=r/(pow(s,2))*exp(-0.5*pow(r,2)/pow(s,2));
	  for(i=*len;i>=0;i--) y[i+1]=y[i];
	  i=*len;
	  y[i+2]= (- deriv_gaussian_r);
	  y[0]=deriv_gaussian_r;
	  r+=1;
	  *len=i+2;
	  if (deriv_gaussian_r > max_val) max_val=deriv_gaussian_r;

  }/*end while*/
  top = max_val;
}/*end get_derigaussian*/


/* Calculate a vectorial second derivative of a Gaussian mask with a standard */
/* deviation of sigma. Cut off when the tails reach 1% of the maximum value. */
void get_2derigaussian(float s, float *y, int *len){
  int r,i,j,negative;
  float max_val, d2_gaussian_r;
  
  r = 1;
  max_val = - 1/pow(s,2);
  *len = 0;
  d2_gaussian_r = max_val;
  y[*len] = d2_gaussian_r;
  if (max_val<0) negative=1;
  while ((d2_gaussian_r>=0.01*max_val)||(negative==1)){
	d2_gaussian_r=((pow(r,2)-pow(s,2))/pow(s,4))*exp(-0.5*pow(r,2)/pow(s,2));
        for (i=*len;i>=0;i--) y[i+1]=y[i];
	i=*len;
	y[i+2] = d2_gaussian_r;
	y[0] = d2_gaussian_r;
	r+=1;
	*len=i+2;
	if (d2_gaussian_r > max_val) max_val = d2_gaussian_r;
  	if (max_val>=0) negative=2;
  }/* end while */
  top=max_val;
}/* end get_2derigaussian */


/* DEFINITION OF GAUSSIAN FUNCTIONS */
/* Procedure to create de dialog. */
void create_sigma_dialog(Widget parent){
  
  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(SIGMA_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  sigma_dialog = XmCreatePromptDialog(temp_widget, "gaussian dialog",
					args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(sigma_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(sigma_dialog, XmNokCallback,
		(XtCallbackProc) ok_sigmabutton_callback, (XtPointer) NULL);
  XtAddCallback(sigma_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_sigmabutton_callback, (XtPointer) NULL);

  XmStringFree(message);

}/*end create_sigma_dialog*/


/* Procedure to activate de gaussian dialog. */
void activate_gaussian_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
  if (file_not_loaded) return;
  selection = GAUSSIAN;
  filter = f1;
  action = SIGMAVALUE;
  refresh_action();
  XtManageChild(sigma_dialog);
}


/* Procedure to deactivate gaussian dialog. */
void deactivate_sigma_dialog(void){
   /* null - no actions at present dialog is auto unmanaged
      whenever any of its buttons are pressed.              */
}


/* Procedure associated with the ok button. */
void ok_sigmabutton_callback(Widget w, XtPointer client_data,
                               XmSelectionBoxCallbackStruct *call_data){
    float sigma;
	 char *value;

    /* Get sigma value from user's selection */
	 XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &value);

	 sigma = atof(value);

    if ( atoi(value) < 0.5 || atoi(value) > 20 )
      XBell(XtDisplay(w),100);
    else
      switch (filter) {
	case f1: gaussian_filter(sigma); break;
	case f2: dgaussian_filter(sigma); break;
	case f3: d2gaussian_filter(sigma); break;
	case f4: dgaux_gauy_filter(sigma); break;
	case f5: dgauy_gaux_filter(sigma); break;
	case f6: total_gradient_filter(sigma); break;
	case f7: gradient_x_y_filter(sigma); break;
	default: break;
      }

    action=SELECT;
    refresh_action();	
}


/* Procedure associated with the cancel button. */
void cancel_sigmabutton_callback(Widget w, XtPointer client_data,
                                   XmSelectionBoxCallbackStruct *call_data){
  
    action=SELECT;
    refresh_action();
    deactivate_sigma_dialog();
}


/* Procedure that aply a gaussian matrix filter to the image */
void gaussian_filter(float sigma){
  int i,j,start,length_gaussian,lon;
  float value, rmax,rmin,rng, mask_gaussian[SIZE2][SIZE2];
  float gaussian[SIZE1];
  matrix m,resul;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  /* Storing the matrix we want to process in m */
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  get_gaussian(sigma, gaussian, &length_gaussian);

  fprintf(stderr,"la long es %i \n",length_gaussian);

  /* Only the values higher than 0.01 are desirable */
  start=0;
  value=pow(gaussian[0],2);
  while (value<0.01){
        start += 1;
  	value = pow(gaussian[start],2);
  }	
     
  for(i=start;i<=length_gaussian-start;i++)
     for(j=start;j<=length_gaussian-start;j++){
	   mask_gaussian[i-start][j-start]= gaussian[i]*gaussian[j];	  
           fprintf(stderr,"la %i,%i es %f \n",i-start,j-start,
		mask_gaussian[i-start][j-start]);

	 }

  length_gaussian = length_gaussian - (2*start);

  fprintf(stderr,"la long es %i \n",length_gaussian);

  convolution(m, mask_gaussian, length_gaussian, &rmax, &rmin, resul);

  rng = (float) (rmax - rmin);

  lon = length_gaussian/2;
  
  /* Now compute the convolution, scaling. */
  for (i=lon; i<IMAGE_WIDTH-lon; i++)
   for (j=lon; j<IMAGE_HEIGHT-lon; j++)
        XPutPixel(theXImage_2,i,j,
		(unsigned long) (((float)(resul[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap. */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
	0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
  
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}


/*Definition of Derivate Gaussian Functions*/
void activate_dgaussian_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
   if (file_not_loaded) return;
   selection = DGAUSSIAN;
   filter = f2;
   action = SIGMAVALUE;
   refresh_action();
   XtManageChild(sigma_dialog);
}


void dgaussian_filter(float sigma){
  int i,j,length_dgaussian,lon,start;
  float rmax, rmin, rng, value, mask_dgaussian[SIZE2][SIZE2];
  float dgaussian[SIZE1];
  matrix m,resul;

  /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  /* Storing the matrix we want to process in m */
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  get_derigaussian(sigma, dgaussian, &length_dgaussian);
  
  fprintf(stderr,"la long es %i \n",length_dgaussian);

  /* Only the values higher than 0.01 are desirable */
  start=0;
  value=pow(dgaussian[0],2);
  fprintf(stderr,"el top es %f\n",top);

  while (value<0.01*pow(top,2)){
        start += 1;
  	value = pow(dgaussian[start],2);
  }
  
     
  for(i=start;i<=length_dgaussian-start;i++)
     for(j=start;j<=length_dgaussian-start;j++){
	   mask_dgaussian[i-start][j-start]= dgaussian[i]*dgaussian[0];	  
           fprintf(stderr,"la %i,%i es %f \n",i-start,j-start,
		mask_dgaussian[i-start][j-start]);
	 }

  length_dgaussian = length_dgaussian - (2*start);

  fprintf(stderr,"la long es %i \n",length_dgaussian);

  convolution(m,mask_dgaussian,length_dgaussian,&rmax,&rmin,resul);

  /*
  convo_vectorx(m,dgaussian,length_dgaussian,&rmax,&rmin,m);
  convo_vectory(m,dgaussian,length_dgaussian,&rmax,&rmin,resul);
  */

  rng = (float) (rmax - rmin);

  lon = length_dgaussian/2;
  
  /* Now compute the convolution, scaling. */
  for (i=lon; i<IMAGE_WIDTH-lon; i++)
   for (j=lon; j<IMAGE_HEIGHT-lon; j++)
        XPutPixel(theXImage_2,i,j,
		(unsigned long) (((float)(resul[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap. */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
	0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
  
 
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);
}



/*Definition of the second Derivate Gaussian Functions*/
void activate_d2gaussian_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
   if (file_not_loaded) return;
   selection = DGAUSSIAN;
   filter = f3;
   action = SIGMAVALUE;
   refresh_action();
   XtManageChild(sigma_dialog);
}


void d2gaussian_filter(float sigma){
  int i,j,length_d2gaussian,lon,start;
  float rmax, rmin, rng, value, mask_d2gaussian[SIZE2][SIZE2];
  float d2gaussian[SIZE1];
  matrix m,resul,d2erix,d2eriy;

  /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  /* Storing the matrix we want to process in m */
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  get_2derigaussian(sigma, d2gaussian, &length_d2gaussian);
  
  fprintf(stderr,"la long es %i \n",length_d2gaussian);

  /* Only the values higher than 0.01 are desirable */
  start=0;
  value=pow(d2gaussian[0],2);
  fprintf(stderr,"el top es %f\n",top);

  while (value<0.01*pow(top,2)){
        start += 1;
  	value = pow(d2gaussian[start],2);
  }
  
     
  for(i=start;i<=length_d2gaussian-start;i++)
     for(j=start;j<=length_d2gaussian-start;j++){
	   mask_d2gaussian[i-start][j-start]= d2gaussian[i]*d2gaussian[j];	  
           fprintf(stderr,"la %i,%i es %f, \n",i-start,j-start,
		mask_d2gaussian[i-start][j-start]);
	 }

  length_d2gaussian = length_d2gaussian - (2*start);
  fprintf(stderr,"la long es %i \n",length_d2gaussian);
  convolution(m,mask_d2gaussian,length_d2gaussian,&rmax,&rmin,resul);


  /* convo_vectorx(m,d2gaussian,length_d2gaussian,&rmax,&rmin,d2erix);
  convo_vectory(m,d2gaussian,length_d2gaussian,&rmax,&rmin,d2eriy);
  for(i=0;i<IMAGE_WIDTH;i++)  
    for(j=0;j<IMAGE_HEIGHT;j++){
	resul[i][j]=sqrt(pow(d2erix[i][j],2)+pow(d2eriy[i][j],2));
	if (i==0 && j==0){rmax=resul[0][0];
			  rmin=rmax;}
        if (resul[i][j]>rmax) rmax=resul[i][j];
        if (resul[i][j]<rmin) rmin=resul[i][j];
  }
  rng=(rmax-rmin);  
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
        XPutPixel(theXImage_2,i,j,(unsigned long)
		 (((float)(resul[i][j]-rmin)/rng)*255.0));*/
  
 
  rng = (float) (rmax - rmin);
  lon = length_d2gaussian/2;
  
  /* Now compute the convolution, scaling. */
  for (i=lon; i<IMAGE_WIDTH-lon; i++)
   for (j=lon; j<IMAGE_HEIGHT-lon; j++)
        XPutPixel(theXImage_2,i,j,
		(unsigned long) (((float)(resul[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap. */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
	0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
  
 
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);
}



/*Definition of dgaux_gauy functions*/
void activate_dgaux_gauy_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
  if (file_not_loaded) return;
  selection = DGAUXGAUY;
  filter = f4;
  action = SIGMAVALUE;
  refresh_action();
  XtManageChild(sigma_dialog);
}


void dgaux_gauy_filter(float sigma){
  int i,j,k,length_gaussian,length_deriv;
  float gaussian[SIZE1],deriv_gaussian[SIZE1];
  matrix dgaux, dgaux_gauy, total_resul, m;
  float rmin,rmax,rng;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);
  
  get_gaussian(sigma,gaussian,&length_gaussian);
  fprintf(stderr,"long de gaussian es %i \n",length_gaussian);
  for(i=0;i<=length_gaussian;i++)
  fprintf(stderr,"el gaussian de [%i] es %f \n",i,gaussian[i]);


  get_derigaussian(sigma,deriv_gaussian,&length_deriv);
  fprintf(stderr,"long de deriv gaussian es %i \n",length_deriv);
  for(i=0;i<=length_deriv;i++)
  fprintf(stderr,"el deriv_gaussian de [%i] es %f \n",i,deriv_gaussian[i]);

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  /* calculates the convolution of the image with the derivative of a */
  /* gaussian for the rows */
  convo_vectorx(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgaux);

  /* calculate the smoothing of a gaussian(x) in y direction.*/
  convo_vectory(dgaux,gaussian,length_gaussian,&rmax,&rmin,
		dgaux_gauy);

  fprintf(stderr,"Maximum is %f, Minimum is %f\n",rmax,rmin);
  rng=(rmax-rmin);

  for(i=0;i<IMAGE_WIDTH;i++)
	for(j=0;j<IMAGE_HEIGHT;j++)
		total_resul[i][j]=sqrt(pow(dgaux_gauy[i][j],2));
  
  for(i=0;i<IMAGE_WIDTH;i++)
	for(j=0;j<IMAGE_HEIGHT;j++)
	XPutPixel(theXImage_2,i,j,(unsigned long)
	         (((float)(total_resul[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
    
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}



/*Definition of dgauy_gaux functions*/
void activate_dgauy_gaux_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
  if (file_not_loaded) return;
  selection = DGAUYGAUX;
  filter = f5;
  action = SIGMAVALUE;
  refresh_action();
  XtManageChild(sigma_dialog);
}


void dgauy_gaux_filter(float sigma){
  int i,j,k,length_gaussian,length_deriv;
  float gaussian[SIZE1],deriv_gaussian[SIZE1];
  matrix dgauy, dgauy_gaux, total_resul, m; 
  float rmin,rmax,rng;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);
  
  get_gaussian(sigma,gaussian,&length_gaussian);

  get_derigaussian(sigma,deriv_gaussian,&length_deriv);
 
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  /* Calculates convolution of the image with the derivative of a */
  convo_vectory(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgauy);


  /*Calculate the smoothing of a Gaussian(y) in x direction */
  convo_vectorx(dgauy,gaussian,length_gaussian,&rmax,&rmin,
		dgauy_gaux);

  rng=(rmax-rmin);

  for(i=0;i<IMAGE_WIDTH;i++)
	for(j=0;j<IMAGE_HEIGHT;j++)
		total_resul[i][j]=sqrt(pow(dgauy_gaux[i][j],2));		

  for(i=0;i<IMAGE_WIDTH;i++)
	for(j=0;j<IMAGE_HEIGHT;j++)
        XPutPixel(theXImage_2,i,j,(unsigned long)
		 (((float)(total_resul[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
  
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}


/*Definition of total_gradient functions*/
void activate_total_gradient_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
int a,b;
FILE *p_file;
  if (yhs_files_open == 0 || yhs_filename[0] == 0 || yhs_files_open > 19 || running == 1) return;
  selection = TOTALGRADIENT;
  filter = f6;
  action = SIGMAVALUE;
  refresh_action();
  if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file);array2[a][b]=0; }}
    fclose(p_file);
  XtManageChild(sigma_dialog);
}


/* total Gradient= sqrt(pow(dgauxgauy,2)+pow(dgauygaux,2)) */
void total_gradient_filter(float sigma){
  int i,j,k,length_gaussian,length_deriv;
  float gaussian[SIZE1],deriv_gaussian[SIZE1];
  matrix dgaux, dgauy, dgaux_gauy, dgauy_gaux,
	 total_gradient, m;
  float rmin,rmax,rng;
  if (yhs_files_open == 0 || yhs_filename[0] == 0 || yhs_files_open > 19 || running == 1) return;
running=1;
XtUnmapWidget(sigma_dialog);
XtUnmanageChild(sigma_dialog);
XFlush(XtDisplay(sigma_dialog));

fprintf(stderr,"\nProcessing filter...");
  get_gaussian(sigma,gaussian,&length_gaussian);

  get_derigaussian(sigma,deriv_gaussian,&length_deriv);

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= array1[i][j]; 

  /* calculates the convolution of the image with the derivative of a */
  /* Gaussian for the rows */
  convo_vectorx(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgaux);

  /*convo_vectorx(m,gaussian,length_gaussian,&rmax,&rmin,gaussian_x);
  convo_vectory(gaussian_x,deriv_gaussian,length_deriv,&rmax,&rmin,final_gaussian_x);*/

  /* calculate the smoothing of a gaussian(x) in y direction. */
  convo_vectory(dgaux,gaussian,length_gaussian,&rmax,&rmin,
		dgaux_gauy);

  fprintf(stderr,"Maximum is %f, Minimum is %f..",rmax,rmin);
 
  /* calculates the convolution of the image with the derivative of a */
  /* gaussian for the columns */
  convo_vectory(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgauy);

   /* convo_vectory(m,gaussian,length_gaussian,&rmax,&rmin,gaussian_y);
    convo_vectorx(gaussian_y,deriv_gaussian,length_deriv,&rmax,&rmin,final_gaussian_y);*/


  /* calculates the smoothing of a gaussian(y) in x direction */
  convo_vectorx(dgauy, gaussian, length_gaussian, &rmax, &rmin,
		dgauy_gaux);

  for(i=0;i<IMAGE_WIDTH;i++)  
    for(j=0;j<IMAGE_HEIGHT;j++){
	total_gradient[i][j]=sqrt(pow(dgaux_gauy[i][j],2)+
				  pow(dgauy_gaux[i][j],2));
	if (i==0 && j==0){rmax = total_gradient[0][0];
			  rmin = rmax;}
        if (total_gradient[i][j]>rmax) rmax=total_gradient[i][j];
        if (total_gradient[i][j]<rmin) rmin=total_gradient[i][j];
  }
  
  rng=(rmax-rmin);  

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
        array2[i][j]=(int)(((float)(total_gradient[i][j]-rmin)/rng)*255.0);
  
   /* Contorno de la Imagen a negro */
     for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j< IMAGE_HEIGHT; j++) {
		array2[i][0]=0;
		array2[i][1]=0;
		array2[i][511]=0;
		array2[0][j]=0;
		array2[1][j]=0;
		array2[511][j]=0;
	  };
fprintf(stderr,"..completed.\nProcessing watershed...");
watershed_callback(NULL,NULL,NULL);
fprintf(stderr,"..completed.\n");
running=0;
}



/*Definition of gradient_x_y functions*/
void activate_gradient_x_y_dialog(Widget w, XtPointer client_data, 
                               XmAnyCallbackStruct *call_data){
  if (file_not_loaded) return;
  selection = GRADIENTXY;
  filter = f7;
  action = SIGMAVALUE;
  refresh_action();
  XtManageChild(sigma_dialog);
}

/* Gradient_x_y = sqrt(pow(gaussian_x,2)+pow(gaussian_y,2) */
void gradient_x_y_filter(float sigma){
  int i,j,k,length_deriv;
  float deriv_gaussian[SIZE1];
  matrix dgaux, dgauy, gradient_x_y, m;
  float rmin,rmax,rng;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);
  
  get_derigaussian(sigma,deriv_gaussian,&length_deriv);
  
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  /* Apply convolution with derivative of a gaussian to the rows */
  convo_vectorx(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgaux);

  /* Apply convolution with derivative of a gaussian to the columns */
  convo_vectory(m,deriv_gaussian,length_deriv,&rmax,&rmin,dgauy);
  
  
  for(i=0;i<IMAGE_WIDTH;i++)  
    for(j=0;j<IMAGE_HEIGHT;j++){
	gradient_x_y[i][j]=sqrt(pow(dgaux[i][j],2)+
				  pow(dgauy[i][j],2));
	if (i==0 && j==0){rmax=gradient_x_y[0][0];
			  rmin=rmax;}
        if (gradient_x_y[i][j]>rmax) rmax=gradient_x_y[i][j];
        if (gradient_x_y[i][j]<rmin) rmin=gradient_x_y[i][j];
  }
  
  rng=(rmax-rmin);  

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
        XPutPixel(theXImage_2,i,j,(unsigned long)
		 (((float)(gradient_x_y[i][j]-rmin)/rng)*255.0));
  
  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
  
  
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}



/* Taking differences between the values of horizontally adjacent pixels */
void differencex_filter(){
  float mask[2]={1,-1};
  float rmax, rmin, rng;
  int i,j;
  matrix m,dx;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
    for(j=0; j<IMAGE_HEIGHT; j++)
      XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */ 
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  convo_vectorx(m, mask, 1, &rmax, &rmin, dx);

  rng = (rmax-rmin);

  for(i=0;i<512;i++)
    for(j=0;j<511;j++){
	XPutPixel(theXImage_2,i,j,(unsigned long)
		(((float)(dx[i][j]-rmin)/rng)*255.0));
  }

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}


/* Taking differences between the values of vertical adjacent pixels */
void differencey_filter(){
  float mask[2]={1,-1};
  float rmax, rmin, rng;
  int i,j;
  matrix m, dy;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
    for(j=0; j<IMAGE_HEIGHT; j++)
      XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  convo_vectory(m, mask, 1, &rmax, &rmin, dy);

  rng = (rmax-rmin);

  for(i=0;i<512;i++)
    for(j=0;j<511;j++)
      XPutPixel(theXImage_2,i,j,(unsigned long)
		(((float)(dy[i][j]-rmin)/rng)*255.0));

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);

}


/* pow(gradient,2) = pow(differencex,2)+pow(differencey,2) */
void gradient_filter(){
  int i,j,first;
  matrix m; 
  float mask[2]={1,-1};
  matrix dx, dy, gradient; 
  float rmax, rmin, rng;

  /* Fill in the image with background color */
  for(i=0; i<IMAGE_WIDTH; i++)
    for(j=0; j<IMAGE_HEIGHT; j++)
      XPutPixel(theXImage_2,i,j,bg);

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  /* Storing the image in a matrix */
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++)
       m[i][j]= XGetPixel(theXImage_1,i,j);

  /* Taking differences between the values of horizontally adjacent pixels */
  convo_vectorx(m, mask, 1, &rmax, &rmin, dx);

  /* Taking differences between the values of vertical adjacent pixels */
  convo_vectory(m, mask, 1, &rmax, &rmin, dy);

  first=1;
  for(i=0;i<IMAGE_WIDTH;i++)
    for(j=0;j<IMAGE_HEIGHT;j++){
	gradient[i][j] = sqrt(pow(dx[i][j],2)+pow(dy[i][j],2));
	if (first==1){ rmax = gradient[i][j];
		       rmin = rmax;
		       first = 2;}
	if (gradient[i][j]<rmin) rmin = gradient[i][j];
	if (gradient[i][j]>rmax) rmax = gradient[i][j];
  }

  rng = (rmax-rmin);

  for(i=0;i<512;i++)
    for(j=0;j<511;j++)
      XPutPixel(theXImage_2,i,j,(unsigned long)
		(((float)(gradient[i][j]-rmin)/rng)*255.0));

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);
}		
