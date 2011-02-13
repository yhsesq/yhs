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
/*		File: realsnake.c				*/
/*								*/
/*		C-T Image Application Program			*/
/*								*/
/*		OSF/Motif Version				*/
/*								*/
/*--------------------------------------------------------------*/

#include "main.h"
#include <Xm/Xm.h>
#include <Xm/Text.h>
#include <Xm/Form.h>
#include <Xm/PushB.h>
#include <Xm/Label.h>
#include <Xm/CascadeB.h>
#include <Xm/Separator.h>
#include <Xm/RowColumn.h>
#include <Xm/SelectioB.h>
#include <X11/cursorfont.h>

#define CHANGEPOINTS_MESSAGE "Change control points (y/n)?:"

#define GETCPOINTS 	"Right draw control point, left stop"
#define CHANGE  	"Changing control points"
#define ASKING		"Asking values for energy calculation"
#define DRAWINGSNK	"Right draw next iteration, left stop"

#define NUMPIXELS 9
#define RANGE 81
#define MAXIMUM 150
#define vali(a) (OFF[a%3])
#define valj(b) (OFF1[b])
#define SIZE1	100
#define SIZE2	50

extern GC       ystheGC[21];
extern GC       arrayGC[21];
extern GC       ysxorGC[21];
extern int      yhs_files_open;
extern Widget	draw_1,manager[21],manage1,view[21];
extern int      curfile;
extern int array1[512][512];
extern int array2[512][512];
extern char *yhs_filename1;
extern int run_once;
extern int file_loader;
extern char          *file_yhs;
extern char          *tempfileold;
extern char          *tempfilenew;
extern char          *addcharac;

extern char	*action;
extern int	selection;
extern Widget   draw_1;
extern Widget	draw_2;
extern XImage	*theXImage_1;
extern XImage	*theXImage_2;
extern unsigned	long bg;
extern Pixmap	thePixmap_1;
extern Pixmap	thePixmap_2;
extern Widget   main_window;
extern GC	theGC, image_gc_1, image_gc_2;
extern int 	file_not_loaded;
extern int	figure;
extern Cursor	theCursor;
extern REGIONPTR region_list;

Cursor othercursor;
int cornerx1,cornery1,cornerx2,cornery2;
int number_points;
XPoint cpoints[MAXIMUM],snpoints[MAXIMUM],sn1points[MAXIMUM];
XPoint zoomstart,zoomend;
int redraw=0;
float optimal_matrix[RANGE][MAXIMUM];
int numiter;
float energy,lastenergy;

/*Initialized variables*/
int OFF[3]={1,-1,0};
int OFF1[10]={1,-1,-1,-1,0,0,0,1,1};

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;
static Widget changepoints_dialog = (Widget) NULL;
static Widget ask_values_dialog = (Widget) NULL;
Widget text1, text2, text3, text4, text5;
float wline, wedge, wterm, alpha, beta;

/*Variables para el procedure start Snake*/
float edgemat[IMAGE_WIDTH][IMAGE_HEIGHT],
		linemat[IMAGE_WIDTH][IMAGE_HEIGHT],
		termmat[IMAGE_WIDTH][IMAGE_HEIGHT],
		gaussianmat[IMAGE_WIDTH][IMAGE_HEIGHT];


int selection_callback(Widget w, XtPointer client_data,
	XmDrawingAreaCallbackStruct *call_data);

void zoomed_callback(Widget w, XtPointer client_data, XEvent *event);

void start_zooming(Widget w, XtPointer client_data, XEvent *event);

/* void cpoints_callback(Widget w, XtPointer client_data,
	XmAnyCallbackStruct *call_data);

void getting_points(Widget w, XtPointer client_data, XEvent *event);

void stop_getting_points(Widget w, XtPointer client_data, XEvent *event);

void create_changepoints_dialog(Widget parent);

void activate_changepoints_dialog(Widget w, XtPointer client_data);

void deactivate_changepoints_dialog(void);

void ok_changepoints_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data);

void cancel_changepoints_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data);

void WlineEline(float w, XImage *theimage);

void WegdeEedge(float w, XImage *theimage);

void WtermEterm(float w, XImage *theimage);

void create_ask_values_dialog(Widget parent);

void activate_ask_values_dialog(Widget w, XtPointer client_data);

void deactivate_ask_values_dialog(void);

void ok_ask_values_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data);

void cancel_ask_values_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data);

void ssnake_callback(Widget w, XtPointer client_data,
	XmAnyCallbackStruct *call_data);

void draw_one(Widget w, XtPointer client_data, XmAnyCallbackStruct *call_data);

void interdraw(Widget w, XtPointer client_data, XEvent *event);

void lastdraw (Widget w, XtPointer client_data, XEvent *event); */


/*Extern procedures used in this file*/
extern void refresh_action(void);
extern void prepare_handlers (int sel);
extern void remove_event_handlers(int sel);
extern void get_gaussian(float s, float *y, int *len);


/*Selection a rectangle on the screen to be zoomed*/
int selection_callback(Widget w, XtPointer client_data,
		XmDrawingAreaCallbackStruct *call_data)
{
  if (yhs_files_open == 0) return;

  /*Delete the last selection if there was any*/
  delete_region (&region_list, 1);

  /* Redraw the image*/
/*  XClearArea(XtDisplay(draw_1), XtWindow(draw_1), 0, 0, 0, 0, True); */

  prepare_handlers(RECTANGLES);
  figure = RECTANGLES;
  selection= RECTANGLES;
  action = DRAW;
  refresh_action();

  /* Create a cross-hair cursor for the drawing area */
  othercursor = XCreateFontCursor(XtDisplay(view[curfile]),  XC_crosshair);

  /* Set up a grab so that the cursor changes to a cross-hair and */
  /* is confined to the drawing_area while the mouse button is    */
  /* pressed. This is done through what is known as a "grab"      */
  XGrabButton(XtDisplay(view[curfile]), Button3, AnyModifier, XtWindow(view[curfile]), True,
	ButtonPressMask | Button3MotionMask | ButtonReleaseMask,
	GrabModeAsync, GrabModeAsync, XtWindow(view[curfile]), othercursor);

  return(selection);

}


/*Prepare handlers for the zoom*/
void zoomed_callback(Widget w, XtPointer client_data, XEvent *event){

	 selection = ZOOM_SNAKE;
	 action = CLICK;
	 refresh_action();
	 prepare_handlers(ZOOM_SNAKE);

}


/*Do the zoom of the selected area when the left button of the mouse
is pressed.*/
void start_zooming(Widget w, XtPointer client_data, XEvent *event){
	 int i,j,k,l;
	 int color,pixelw,pixelh;
	 int lenghtx,heighty;
	 int modw, modh;
FILE *p_file;
int a,b;

	 if (event->xbutton.button != Button1) return;

	 /* Fill in the image with background color */
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);

	 /* Clear the drawing window so the image is displayed again */
/*	 XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True); */

	 /* Associate the watch cursor with the main window */
/*	 XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor); */

	 /* Flush the request buffer and wait for all events */
	 /* and errors to be processed by the server.        */
/*	 XSync(XtDisplay(draw_1), False); */

	 /*Order points of the rectangle*/
	 if (cornerx2<cornerx1) {lenghtx = cornerx1;
				 cornerx1 = cornerx2;
				 cornerx2 = lenghtx;}
	 lenghtx = cornerx2 - cornerx1;

	 if (cornery2<cornery1) {heighty = cornery1;
				 cornery1 = cornery2;
				 cornery2 = heighty;}
	 heighty = cornery2 - cornery1;

	 /*Each pixel has to be zoomed with a certain width and height */
	 pixelw = IMAGE_WIDTH / lenghtx;
	 pixelh = IMAGE_HEIGHT / heighty;

	 /*Get an image centered in the screen*/
	 modw = (IMAGE_WIDTH % lenghtx)/2;
	 modh = (IMAGE_HEIGHT % heighty)/2;

	 for (i=cornerx1;i <= cornerx2;i++)
		for (j=cornery1;j<=cornery2;j++){
		 color = array1[i][j];
		 for(k=0; k<pixelw; k++)
			for(l=0; l<pixelh; l++) {
			  array2[(pixelw*(i-cornerx1))+k+modw][(pixelh*(j-cornery1))+l+modh]=color;
		}
	 }

	 zoomstart.x=modw;
	 zoomstart.y=modh;
	 zoomend.x=pixelw*(cornerx2-cornerx1)+(pixelw-1)+modw;
	 zoomend.y=pixelh*(cornery2-cornery1)+(pixelh-1)+modh;

	 /* Disassociate the watch cursor from the main window */
/*	 XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window)); */

	 /* Copy image into pixmap */
/*	 XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height); */

	 /* Clear the drawing window so the image is displayed again */
/*	 XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True); */

	 /*Remove Events for zoom*/
	 remove_event_handlers(ZOOM_SNAKE);

	 action=SELECT;
	 refresh_action();
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
}


/*Get control points in the zoomed image on drawing area 2*/
/* void cpoints_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data){
	int i;

	for(i=0;i<MAXIMUM;i++){
	   cpoints[i].x=0;
	   cpoints[i].y=0;
	   snpoints[i].x=0;
	   snpoints[i].y=0;
	   sn1points[i].x=0;
	   sn1points[i].y=0;
  	}

	 prepare_handlers(CPOINTS);
	 selection = CPOINTS;
	 action = GETCPOINTS;
	 refresh_action();
	 number_points = 0;
}

*/
/*Procedure to get the control points of the initial snake.*/
void getting_points(Widget w, XtPointer client_data, XEvent *event){

  TPINFO info;
  int 	x = event->xbutton.x,
	y = event->xbutton.y;
  
  if (event->xbutton.button == Button3)
    {
/*	if (number_points==0){
		cpoints[0].x = x;
		cpoints[0].y = y;
	}
	fprintf(stderr," la x es %i , la y es %i \n",x,y);
	number_points = number_points + 1;
	cpoints[number_points].x = x;
	cpoints[number_points].y = y;
	fprintf(stderr," el numero de puntos es %i \n",number_points);

	/*Draw point on window 2*/
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, x, y);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2 , x+1, y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, x-1, y-1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, x-1, y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, x+1, y-1);
*/
	/*Draw line between each pair of points*/
	XDrawLine(XtDisplay(w), XtWindow(w), image_gc_2,
		cpoints[number_points-1].x, cpoints[number_points-1].y,
		cpoints[number_points].x, cpoints[number_points].y);

	/*Store information for node structure*/
	info.coord_x = x;
	info.coord_y = y;
	info.grey_level = XGetPixel(theXImage_2,info.coord_x,info.coord_y);
    }
}


/*Procedure to stop getting control points of the initial snake.*/
void stop_getting_points(Widget w, XtPointer client_data, XEvent *event){

	  if (event->xbutton.button == Button1){
	  XtRemoveEventHandler (draw_2, XtAllEvents, False,
			 (XtEventHandler) getting_points, NULL);

	  activate_changepoints_dialog(w,client_data);
	  }

}


/*Procedures to create change points dialog.*/
void create_changepoints_dialog(Widget parent){

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
	 temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(CHANGEPOINTS_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
  changepoints_dialog = XmCreatePromptDialog(temp_widget, "changepoints dialog",
		args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(changepoints_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(changepoints_dialog, XmNokCallback,
		(XtCallbackProc) ok_changepoints_callback, (XtPointer) NULL);
  XtAddCallback(changepoints_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_changepoints_callback, (XtPointer) NULL);

  XmStringFree(message);
}


void activate_changepoints_dialog(Widget w, XtPointer client_data){
  
  selection = CHANGEPOINTS;
  action = CHANGE;
  refresh_action();
  redraw=1;
  XtManageChild(changepoints_dialog);

}



void deactivate_changepoints_dialog(void){

 /* null - no actions at present dialog is auto unmanaged
    whenever any of its buttons are pressed.           */
		
 /* int i,j=0;

  if (redraw==1){
	for (i=0;i<number_points;i++)
	XDrawLine(XtDisplay(draw_2), thePixmap_2 , image_gc_2,
		cpoints[i-1].x, cpoints[i-1].y,
		cpoints[i].x, cpoints[i].y);}
	for(i=0;i<10000;i++) fprintf(stderr,"%i",i);
	XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);

	XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);
  */
}


void ok_changepoints_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data){
  char *change;

  /* Get sigma value from user's selection */
  XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &change);

  remove_event_handlers(CPOINTS);

  if (change[0] == 'n' || change[0] == 'N'){
	action=SELECT;
	refresh_action();
	redraw=1;
  }
  else if ( change[0]=='y' || change[0]=='Y' ){
		cpoints_callback(w,client_data,NULL);
		redraw=0;
	}
	else {  XBell(XtDisplay(w),100);
		activate_changepoints_dialog(w,client_data);
		redraw=1;
	     }
}


void cancel_changepoints_callback(Widget w, XtPointer client_data,
						  XmSelectionBoxCallbackStruct *call_data){
	 deactivate_changepoints_dialog();
}


/*******PROCEDURE START SNAKE ************/


/*ESTO ES PARA EL WLINE*INTENSIDAD DE LOS PIXELS DE LA IMAGEN.*/
void WlineEline(float w, XImage *theimage){
  int i,j;
  
  fprintf(stderr,"estoy dentro del Wline");
  /*AQUI HE EMPEZADO DESDE 0 PORQUE NO TENGO QUE HACER NINGUN I-1.*/
  for (i=zoomstart.x; i<=zoomend.x; i++)
    for (j=zoomstart.y; j<=zoomend.y; j++)
	linemat[i][j] = w * XGetPixel(theimage,i,j);
  fprintf(stderr,"el wline es dentro %f",w);
}/* end WlineEline */


/*ESTO ES PARA EL WEDGE*EDGE_SOBEL.*/
void WegdeEedge(float w, XImage *theimage){
  int i,j,n,m,k,rmax,rmin,rng;
  fprintf(stderr,"estoy dentro del wedge");
  /*Locate MAX and MIN for rescaling.*/
  rmax = XGetPixel(theimage,zoomstart.x,zoomstart.y);
  rmin = rmax;
  for (i=zoomstart.x+1; i<zoomend.x; i++)
    for (j=zoomstart.y+1; j<zoomend.y; j++)
    {
	n = ( XGetPixel(theimage,i-1,j+1) +
		 2*XGetPixel(theimage,i,j+1) +
		 XGetPixel(theimage,i+1,j+1) ) -
	   	( XGetPixel(theimage,i-1,j-1) +
		 2*XGetPixel(theimage,i,j-1) +
		 XGetPixel(theimage,i+1,j-1) );
	m = ( XGetPixel(theimage,i+1,j-1) +
		 2*XGetPixel(theimage,i+1,j) +
		 XGetPixel(theimage,i+1,j+1) ) -
		( XGetPixel(theimage,i-1,j-1) +
		 2*XGetPixel(theimage,i-1,j) +
		 XGetPixel(theimage,i-1,j+1) );
	k = abs(n)+abs(m);
	if (k>rmax) rmax = k;
	if (k<rmin) rmin = k;
    }/* end doble loop for */

  rng = (float)(rmax-rmin);

  /*Now compute the convolution, scaling.*/
  for (i=zoomstart.x+1; i<zoomend.x; i++)
    for (j=zoomstart.y+1; j<zoomend.y; j++)
    {
	n = ( XGetPixel(theimage,i-1,j+1) +
		2*XGetPixel(theimage,i,j+1) +
		XGetPixel(theimage,i+1,j+1) ) -
		( XGetPixel(theimage,i-1,j-1) +
		2*XGetPixel(theimage,i,j-1) +
		XGetPixel(theimage,i+1,j-1) );
	m = ( XGetPixel(theimage,i+1,j-1) +
		2*XGetPixel(theimage,i+1,j) +
		XGetPixel(theimage,i+1,j+1) ) -
		( XGetPixel(theimage,i-1,j-1) +
		2*XGetPixel(theimage,i-1,j) +
		XGetPixel(theimage,i-1,j+1) );

	k = abs(n)+abs(m);

	edgemat[i][j]= w * (int)(((float)(k-rmin)/rng)*255.0);
    }/* end doble loop for */

}/* end WedgeEedge */


/*ESTO ES PARA EL WTERM*POR LA GAUSSIANA*/
void WtermEterm(float w, XImage *theimage){

  /*Esta es la primera gaussiana, hay que poner la segunda gaussiana.*/
  int i, j, k, n, m, start, length_gaussian, lon;
  float sigma, value, mask_gaussian[SIZE2][SIZE2];
  float gaussian[SIZE1];
  float rng,convo[IMAGE_WIDTH][IMAGE_HEIGHT],rmax,rmin;

  fprintf(stderr,"estoy dentro del wterm");

  /* Finding out the length of the vector */
  sigma=1;
  get_gaussian(sigma, gaussian, &length_gaussian);

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
      
      fprintf(stderr,"la %i,%i es %f \n",i-start,j-start,mask_gaussian[i-start][j-start]);

	 }

  length_gaussian = length_gaussian - (2*start);

  fprintf(stderr,"la long es %i \n",length_gaussian);

  /* Convolution of the image with the gaussian mask */

  /*Locate MAX and MIN for rescaling*/
  lon = length_gaussian/2;
  rmax = XGetPixel(theimage, zoomstart.x, zoomstart.y);
  rmin = rmax;
  for (i=lon+zoomstart.x; i<=zoomend.x-lon; i++)
    for (j=lon+zoomstart.y; j<=zoomend.y-lon; j++) {
	k = 0;
	for (n=0; n<=length_gaussian; n++)
	  for (m=0; m<=length_gaussian; m++)
		k += mask_gaussian[n][m]*XGetPixel(theimage,i+n-lon,j+m-lon);

	/* Updating the values of ramx and rmin */
	if (k>rmax) rmax = k;
	if (k<rmin) rmin = k;

	/* Store the values of the gaussian in a matrix. */
	convo[i][j]=k;
  }

  fprintf(stderr,"el lon + zoomstart.x es %i\n",lon+zoomstart.x);

  rng = (float)(rmax-rmin);

  /* Now compute the convolution, scaling */
  for (i=lon+zoomstart.x; i<=zoomend.x-lon; i++)
    for (j=lon+zoomstart.y; j<=zoomend.y-lon; j++) {

      termmat[i][j] = -(int)(((float)(convo[i][j]-rmin)/rng)*255.0)*w;
      gaussianmat[i][j] = (unsigned long)(((float)(convo[i][j]-rmin)/rng)*255.0);

  }/* end for */

}/* end WtermEterm */

/************************************************************************/

/*Procedures to create ask values dialog.*/
void create_ask_values_dialog(Widget parent){

  Widget temp_widget = parent;
  Widget box;
  XmString okstring, cancelstring;


  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
	 temp_widget = XtParent(temp_widget);
  }

  ask_values_dialog = XmCreatePromptDialog(temp_widget, "ask_values dialog",
		NULL,0);
  
  box = XtVaCreateManagedWidget ("rowcol", xmRowColumnWidgetClass,
			ask_values_dialog, NULL, 0);

  /* Add five labels with five text boxes to introduce the values */
  
  (void) XtVaCreateManagedWidget("Wline (1-6):",xmLabelWidgetClass, 
 		box, NULL);
  
  text1 = XtVaCreateManagedWidget("Wline",xmTextWidgetClass,
		box, XmNeditable, True, XmNcolumns, 20,
		XmNrows, 1, NULL);
  
  (void) XtVaCreateManagedWidget("Wedge (1-6):",xmLabelWidgetClass, 
 		box, NULL);

  text2 = XtVaCreateManagedWidget("Wedge",xmTextWidgetClass,
		box, XmNeditable, True, XmNcolumns, 20,
		XmNrows, 1, NULL);

  (void) XtVaCreateManagedWidget("Wterm (1-6):",xmLabelWidgetClass, 
		box, NULL);

  text3 = XtVaCreateManagedWidget("Wterm",xmTextWidgetClass,
		box, XmNeditable, True, XmNcolumns, 20,
		XmNrows, 1, NULL);

  (void) XtVaCreateManagedWidget("Alpha (0-1):",xmLabelWidgetClass, 
		box, NULL);

  text4 = XtVaCreateManagedWidget("Alpha",xmTextWidgetClass,
		box, XmNeditable, True, XmNcolumns, 20,
		XmNrows, 1, NULL);

  (void) XtVaCreateManagedWidget("Beta (0-1):",xmLabelWidgetClass, 
		box, NULL);

  text5 = XtVaCreateManagedWidget("Beta",xmTextWidgetClass,
		box, XmNeditable, True, XmNcolumns, 20,
		XmNrows, 1, NULL);
  
  /* Remove the help button from the dialog */
  temp_widget = XmSelectionBoxGetChild(ask_values_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);
  
  /* Remove the default selection label */
  temp_widget = XmSelectionBoxGetChild(ask_values_dialog, XmDIALOG_SELECTION_LABEL);
  XtUnmanageChild(temp_widget);

  /* Remove the default dialog text */ 
  temp_widget = XmSelectionBoxGetChild(ask_values_dialog, XmDIALOG_TEXT);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(ask_values_dialog, XmNokCallback,
		(XtCallbackProc) ok_ask_values_callback, (XtPointer) NULL);
  XtAddCallback(ask_values_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_ask_values_callback, (XtPointer) NULL);
}


void activate_ask_values_dialog(Widget w, XtPointer client_data){

  action = ASKING;
  refresh_action();
  redraw=1;
  XtManageChild(ask_values_dialog);

}


void deactivate_ask_values_dialog(void){

 /* null - no actions at present dialog is auto unmanaged
    whenever any of its buttons are pressed.           */
    redraw=1;			
}


void ok_ask_values_callback(Widget w, XtPointer client_data,
	XmSelectionBoxCallbackStruct *call_data){
  
  float v1,v2,v3,v4,v5;
  char *content1, *content2, *content3, *content4, *content5;
  
  /* Get wline value from user's selection */
  content1 = XmTextGetString(text1);
  wline = atof(content1);
   
  /* Get wedge value from user's selection */
  content2 = XmTextGetString(text2);
  wedge = atof(content2);

  /* Get wterm value from user's selection */
  content3 = XmTextGetString(text3);
  wterm = atof(content3);

  /* Get alpha value from user's selection */
  content4 = XmTextGetString(text4);
  alpha = atof(content4);

  /* Get beta value from user's selection */
  content5 = XmTextGetString(text5);
  beta = atof(content5);

  
  ssnake_callback(w,client_data,NULL);

}


void cancel_ask_values_callback(Widget w, XtPointer client_data,
		XmSelectionBoxCallbackStruct *call_data){
	 deactivate_ask_values_dialog();
}




/**************************************************************************/

/*Procedure that calculates the matrices that are going to be used to */
/*calculate the energy of the snake */
void ssnake_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data){
 
  int i,j;

  if (file_not_loaded) return;

  action = DRAWINGSNK;
  refresh_action();

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

  /* Associate the watch cursor with the main window */
  XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

  /* Flush the request buffer and wait for all events */
  /* and errors to be processed by the server.        */
  XSync(XtDisplay(draw_1), False);

  /* Keeping the original snake in the vecotr cpoints and using snpoints */
  /* for remainders operations */
  for(i=0;i<=number_points;i++){
	  snpoints[i]=cpoints[i];}

  /* Image intensity multiply by wline and store in a matrix called */
  /* linemat */
  WlineEline(wline, theXImage_2);
  fprintf(stderr,"el wline es %f/n",wline);

  /* Sobel aplicated to an image multiply by wedge ans store in a matrix*/
  /* called edgemat */
  WegdeEedge(wedge, theXImage_2);

  /* Aply the convolution with a gaussian mask to the image multiply by */
  /* wterm and store it in a matrix called termmat */
  WtermEterm(wterm, theXImage_2);

  /* Initializing optimal_matrix to zero */
  for(i=0;i<RANGE;i++)
	 for(j=0;j<MAXIMUM;j++){
		  optimal_matrix[i][j]=0;
  }

  numiter=0;

  /* Copy image into pixmap */
  XPutImage(XtDisplay(draw_2), thePixmap_2,image_gc_2, theXImage_2,
		 0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
    
  /* Disassociate the watch cursor from the main window */
  XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

  /* Clear the drawing window so the image is displayed again */
  XClearArea(XtDisplay(draw_2),XtWindow(draw_2),0,0,0,0,True);


  draw_one(w, client_data, call_data);

}/* end ssnake_callback */


/* Calculate all the energies and draw the first snake */
/* Add Event Handlers to continue drawing the snake */ 
void draw_one (Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data){
  int i,j,k,v0,v1,v2,v;
  int first,final_position;
  float minvalor,min;
  int index_matrix[RANGE][MAXIMUM],position;

  numiter=numiter+1;

  lastenergy=energy;

  fprintf(stderr,"paso1\n");
  for(v0=1;v0<=RANGE;v0++){
	 optimal_matrix[0][v0-1] =
	-(gaussianmat[snpoints[1].x + vali(v0)][snpoints[1].y + valj(v0%9)]);
	 fprintf(stderr,"el v. optimal_matrix %f \n",optimal_matrix[0][v0-1]);
  }

  fprintf(stderr,"paso2\n");
  for(k=2;k<number_points;k++){
    /* Loop for the nine neighbours of the third point considered */
    for(v2=1;v2<=9;v2++)
    {
      /* Loop for the nine neighbours of the second point considered */
      for(v1=1;v1<=9;v1++)
      {
	/* Loop for the nine neighbours of the first point considered */
	first=1;
	for(v0=1;v0<=9;v0++)
	{ 

	  /* The value and the position have to be stored if they are minimum */

	  /* k-2 because optimal matrix starts in zero */
	  min=optimal_matrix[k-2][(v1-1)*NUMPIXELS + (v0-1)]

	  /* Intensity value of the point multiplied by wline */
	  + linemat[snpoints[k].x + vali(v1)][snpoints[k].y + valj(v1)]

	  /* Sobel value of the points multiplied by wedge */
	  + edgemat[snpoints[k].x + vali(v1)][snpoints[k].y + valj(v1)]

	  /* Gaussian value of the point multiplied by wterm */
	  + termmat[snpoints[k].x + vali(v1)][snpoints[k].y + valj(v1)]

	  /* Alpha is multiplied for the distance between two points */
	  + alpha*(pow(((snpoints[k].x + vali(v1))
			-(snpoints[k-1].x + vali(v0))),2) +
		   pow(((snpoints[k].y + valj(v1))
			-(snpoints[k-1].y + valj(v0))),2))

	  /* Beta is multiplied for the distance between the three points */
	  + beta*(pow(((snpoints[k+1].x + vali(v2)) 
			- 2*(snpoints[k].x + vali(v1)) +
			(snpoints[k-1].x + vali(v0))),2) +
		  pow(((snpoints[k+1].y + valj(v2)) 
			- 2*(snpoints[k].y + valj(v1)) +
			(snpoints[k-1].y + valj(v0))),2));

	  /*  Keeping the first value for next comparations */
	  if (first==1){minvalor=min;
			  first=2;
			  position=v0;}
	  /* Storing the value and position of the lower energy value */
	  if (min<minvalor){minvalor=min;
			    position=v0;}

	}/* end for loop v0 */

	optimal_matrix[k-1][(v2-1)*NUMPIXELS+(v1-1)]=minvalor -
		gaussianmat[snpoints[k].x+vali(v1)][snpoints[k].y+valj(v1)];

	index_matrix[k-1][(v2-1)*NUMPIXELS+(v1-1)]=position;

      }/* end for loop v1 */

    }/* end for loop v2 */

  }/* end for loop k */

  fprintf(stderr,"paso3\n");


  /*The previous loop is not able to calculate the alpha term between the */
  /*last point n and the previous n-1 and the associated with it intensity*/
  /*and gradient, so the next loop calculates it. */

  /* Loop for the nine neighbours of the second point considered */
  for(v1=1;v1<=9;v1++)
  {

    /* Loop for the nine neighbours of the first point considered */
    first=1;
    for(v0=1;v0<=9;v0++)
      {
	/* The value and the position have to be stored if they are minimum */
	min = optimal_matrix[number_points-2][(v1-1)*NUMPIXELS+(v0-1)]

	/* Intensity value of the point multiplied by wline */
	+ linemat[snpoints[number_points].x+vali(v1)][snpoints[number_points].y+valj(v1)]

	/* Sobel value of the points multiplied by wedge */
	+ edgemat[snpoints[k].x + vali(v1)][snpoints[k].y + valj(v1)]

	/* Gaussian value of the point multiplied by wterm */
	+ termmat[snpoints[number_points].x+vali(v1)][snpoints[number_points].y+valj(v1)]

	/* Alpha is multiplied for the distance between two points */
	+ alpha*(pow(((snpoints[number_points].x + vali(v1)) -
		(snpoints[number_points-1].x + vali(v0))),2) +
		pow(((snpoints[number_points].y + valj(v1)) -
		(snpoints[number_points-1].y + valj(v0))),2));

	/* Keeping the first value for next comparations */
	if (first==1){minvalor=min;
			first=2;
			position=v0;}

	/* Storing the value and position of the lower energy value */
	if (min<minvalor){minvalor=min;
			  position=v0;}

      }/* end for v0 */

    optimal_matrix[number_points-1][v1-1]= minvalor -
	gaussianmat[snpoints[number_points].x+vali(v1)][snpoints[number_points].y+valj(v1)];

    index_matrix[number_points-1][v1-1]=position;

  }/* end for v1 */

  fprintf(stderr,"paso4\n");

/******************BACKWARD PROCEDURE *************/

  min=optimal_matrix[number_points-1][0];
  final_position=1;

  /* Calculating the min energy of the snake */
  for (i=1;i<NUMPIXELS;i++){
	if (optimal_matrix[number_points-1][i]<min){
	  min=optimal_matrix[number_points-1][i];
	  final_position=i+1;
	 }/* end if */
  }/* end for */

  energy=min;
  if (numiter==1) lastenergy=energy+1;

  fprintf(stderr,"el numero de iteraciones es %i\n",numiter);
  fprintf(stderr,"paso5\n");

  /* Determining the new position for the last point */
  sn1points[number_points].x =
		snpoints[number_points].x + vali(final_position);
  sn1points[number_points].y =
		snpoints[number_points].y + valj(final_position);

  /* Determining the position of the remainder points */
  v1=final_position;
  v2=1;
  for(k=number_points-2;k>=0;k--){
    v=index_matrix[k+1][(v2-1)*NUMPIXELS + (v1-1)];
    v2=v1;
    v1=v;
    sn1points[k+1].x=snpoints[k+1].x + vali(v1);
    sn1points[k+1].y=snpoints[k+1].y + valj(v1);
  }
  sn1points[0]=sn1points[1];

  fprintf(stderr,"paso6\n");
  fprintf(stderr,"energia %f\n",energy);
  fprintf(stderr,"lastenergy %f\n",lastenergy);

  /* Initializing the snake with the new less energy snake */
  if (energy<lastenergy)
    for (i=0;i<=number_points;i++){
 	
	fprintf(stderr,"el pto %i newx %i newy %i\n",i,sn1points[i].x,sn1points[i].y);

	 snpoints[i]=sn1points[i];
  }; /* end if */

  XtAddEventHandler(draw_2, ButtonPressMask, False,
	(XtEventHandler) interdraw, NULL);

  XtAddEventHandler(draw_2, ButtonPressMask, False,
	(XtEventHandler) lastdraw, NULL);

}/* end start_snake */


/* Draw the snake in the actual iteration */
void interdraw(Widget w, XtPointer client_data, XEvent *event){
  int i;

  /* When the right button of the mouse is pressed */
  if (event->xbutton.button == Button3){

    /* Clear the drawing window so the image is displayed again */
    /*XClearArea(XtDisplay(w),XtWindow(w),0,0,0,0,False);*/

    /* Copy image into pixmap */
    /*XPutImage(XtDisplay(w), XtWindow(w), image_gc_2, theXImage_2,
	0, 0, 0, 0, theXImage_2->width, theXImage_2->height);*/

    /* Draw snake for the current iteration */
    for (i=1; i<=number_points; i++)
    {

	/*Draw point on window 2*/
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x, snpoints[i].y);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x+1, snpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x-1, snpoints[i].y-1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x-1, snpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x+1, snpoints[i].y-1);

	/*Draw line between each pair of points*/
	XDrawLine(XtDisplay(w), XtWindow(w), image_gc_2,
		snpoints[i-1].x, snpoints[i-1].y,
		snpoints[i].x, snpoints[i].y);

    }/* end for */

    XtRemoveEventHandler (draw_2, XtAllEvents, False, (XtEventHandler) interdraw, NULL);
    draw_one( w, client_data, NULL);

  }/* end if */

  else XtRemoveEventHandler(draw_2, XtAllEvents, False, (XtEventHandler) interdraw,NULL);

}/* end interdraw */


/* Draw the first snake and the last snake in the drawing area 2 */
void lastdraw(Widget w, XtPointer client_data, XEvent *event){
  int i;

  /* When the left button of the mouse is pressed */
  if (event->xbutton.button == Button1){

    for (i=1;i<=number_points;i++){

	/* Drawing the first snake in the drawing area 2 */
	XDrawLine(XtDisplay(w), XtWindow(w) , image_gc_2,
		cpoints[i-1].x, cpoints[i-1].y,
		cpoints[i].x, cpoints[i].y);

	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, cpoints[i].x, cpoints[i]. y);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, cpoints[i].x+1, cpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, cpoints[i].x-1, cpoints[i].y-1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, cpoints[i].x-1, cpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, cpoints[i].x+1, cpoints[i].y-1);

	/* Drawing the last snake in the drawing area 2 */
	XDrawLine(XtDisplay(w), XtWindow(w) , image_gc_2,
		snpoints[i-1].x, snpoints[i-1].y,
		snpoints[i].x, snpoints[i].y);

	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x, snpoints[i].y);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x+1, snpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x-1, snpoints[i].y-1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x-1, snpoints[i].y+1);
	XDrawPoint(XtDisplay(w), XtWindow(w), image_gc_2, snpoints[i].x+1, snpoints[i].y-1);

    }/* end for */
    
    XDrawLine(XtDisplay(w), XtWindow(w), image_gc_2,
		snpoints[1].x, snpoints[1].y,
		snpoints[number_points].x, snpoints[number_points].y);
    
    XtRemoveEventHandler(draw_2, XtAllEvents, False,(XtEventHandler)lastdraw, NULL);
    action = SELECT;
    refresh_action();

  }/* end if */
  
  else {
	XtRemoveEventHandler(draw_2, XtAllEvents, False,(XtEventHandler)lastdraw, NULL);
        XtAddEventHandler(draw_2, ButtonPressMask, False,
	(XtEventHandler) lastdraw, NULL);}
  
}/* end lastdraw */
