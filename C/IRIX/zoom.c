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


/*----------------------------------------------------------------------*/
/*									*/
/*	File: zoom.c       						*/
/*									*/
/*	December 1996							*/
/*									*/
/*	OSF/Motif version.						*/
/*									*/
/*----------------------------------------------------------------------*/

#include "main.h"
#include <Xm/SelectioB.h>

#define ZOOM_MESSAGE "Select zoom level (0-50):"


/* Extern variables */
extern char	*action;
extern int	selection;
extern XImage	*theXImage_1;
extern XImage	*theXImage_2;
extern Pixmap	thePixmap_2;
extern Widget   draw_1;
extern Widget	draw_2;
extern GC	theGC, xorGC, image_gc_2;
extern GC       arrayGC[21];
extern GC       ysxorGC[21];
extern int      yhs_files_open;
extern int	yhs_filename[11];
extern int 	curfile;
extern char	*yhs_filename1;
extern int	yhs_filename[11];
extern char	*file_yhs;
extern int	run_once;
extern int	file_loader;
extern int	array1[512][512];
extern int	array2[512][512];
extern Widget	view[21];
extern char     *tempfileold;
extern char     *tempfilenew;
extern char     *addcharac;
extern int 	squash[21];

/* Variables for setting resources */
static Arg 	args[MAXARGS];
static Cardinal	argcount;
static Widget	zoom_dialog = (Widget) NULL;

/* Other variables */
static short	zoom_level;
static int 	offset_x = 0,
		offset_y = 0,
		old_offset_x = 0,
		old_offset_y = 0,
		x, y,
		old_x, old_y,
		rectangle_on_screen=False;


/* Function prototypes */
void create_zoom_dialog(Widget parent);
void activate_zoom_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
static void ok_button_callback(Widget w, XtPointer client_data,
                               XmSelectionBoxCallbackStruct *call_data);
void start_zoom(Widget w, XtPointer client_data, XEvent *event);
void move_zoom(Widget w, XtPointer data, XEvent *p_event);

extern void prepare_handlers (int sel);
extern void refresh_action(void);


/* Function definition */

/*------------------------------------------------------------------------------*/
/*  c r e a t e _ z o o m _ d i a l o g						*/
/*										*/
/*  Create the zoom level prompt dialog for the zoom button in the tools menu	*/
/*------------------------------------------------------------------------------*/

void create_zoom_dialog(Widget parent)
{
    XmString message;
    Widget temp_widget = parent;

    /* Ensure the parent of the dialog is a shell widget */
    while ( !XtIsShell(temp_widget) ) {
	temp_widget = XtParent(temp_widget);
    }

    message = XmStringLtoRCreate(ZOOM_MESSAGE, XmSTRING_DEFAULT_CHARSET);

    argcount = 0;
    XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
    zoom_dialog = XmCreatePromptDialog(temp_widget, "zoom dialog",
				args, argcount);

    /* Remove the help button from the dialog */
    temp_widget = XmSelectionBoxGetChild(zoom_dialog, XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(temp_widget);

    /* Add the actions to the buttons */
    XtAddCallback(zoom_dialog, XmNokCallback,
		(XtCallbackProc) ok_button_callback, (XtPointer) NULL);

    XmStringFree(message);
}



/*-----------------------------------------------------------------------------*/
/*  a c t i v a t e _ z o o m _ d i a l o g             		       */
/*-----------------------------------------------------------------------------*/
void activate_zoom_dialog(Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data)
{
    selection = ZOOM;
    action = CLICK;
    refresh_action();
    XtManageChild(zoom_dialog);
}



/*-----------------------------------------------------------------------------*/
/*  o k _ b u t t o n _ c a l l b a c k                      		       */
/*                                                           		       */
/*  Callback for the "OK" button						*/
/*-----------------------------------------------------------------------------*/
static void ok_button_callback(Widget w, XtPointer client_data,
                               XmSelectionBoxCallbackStruct *call_data)
{
    int t;
    char *zoom;
FILE *p_file;
int a,b;

    /* Get threshold value from user's selection */
    XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &zoom);
if(squash[curfile] != 1 && yhs_filename[0] !=0 && yhs_files_open > 0 && yhs_files_open < 20)
{
    t = atoi(zoom);

    if (!(t>0 && t<=50) ) XBell(XtDisplay(w),100);
    else {

   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); array2[a][b]=0; }}
    fclose(p_file);

	/*Set the scale zooming*/
	zoom_level = t;
	/* Prepare the widgets to be able to receive the corret events */
	prepare_handlers(ZOOM);
	/* Get the correct scale settings */
	old_offset_x = offset_x;
	old_offset_y = offset_y;
	offset_x = 512/zoom_level;
	offset_y = 512/zoom_level;
    }
}
}


/*----------------------------------------------------------------------*/
/*  s t a r t  _  z o o m 						*/
/*----------------------------------------------------------------------*/

void start_zoom(Widget w, XtPointer client_data, XEvent *event)
{
    int i, j, color, x1, y1,z;
int a,b;
FILE *p_file;

z=y;
y=x;
x=z;
    if (event->xbutton.button != Button3) return;
remove_event_handlers(ZOOM);
    /* Fill in the image with zoomed partition */
    for(x1=x; x1<(x+offset_x); x1++)
	for(y1=y; y1<(y+offset_y); y1++) {
	    color = array1[x1][y1];
	    for(i=0; i<zoom_level; i++)
		for(j=0; j<zoom_level; j++) {
		   array2[((x1-x)*zoom_level)+i][((y1-y)*zoom_level)+j]=color;
		};
	}
fprintf(stderr,"\nZooming : x1=%d y1=%d x=%d y=%d offset_x=%d offset_y=%d\n",x1,y1,x,y,offset_x,offset_y);
    /* Flush the request buffer and wait for all events */
    /* and errors to be processed by the server.        */
/*    XSync(XtDisplay(w), False); */
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



/*----------------------------------------------------------------------*/
/*  m o v e _  z o o m							*/
/*----------------------------------------------------------------------*/

void move_zoom(Widget w, XtPointer data, XEvent *p_event)
{
    x = p_event->xbutton.x;
    y = p_event->xbutton.y;

    /* If rectangle on screen, redraw it again using XOR mask to remove it */
    if (rectangle_on_screen)
	XDrawRectangle(XtDisplay(view[curfile]),XtWindow(view[curfile]),ysxorGC[curfile],old_x,old_y,
				old_offset_x, old_offset_y);

    /*The rectangle can't be moved outside the display window */
    if ( x>( IMAGE_WIDTH-offset_x) ) x=(IMAGE_WIDTH-offset_x);
    if ( y>(IMAGE_HEIGHT-offset_y) ) y=(IMAGE_HEIGHT-offset_y);

    /* Draw the rectangle in the new position in screen */
    XDrawRectangle(XtDisplay(view[curfile]), XtWindow(view[curfile]), ysxorGC[curfile], x, y, offset_x, offset_y);

    /* Save values for the next updating rectangle */
    old_x = x;
    old_y = y;
    old_offset_x = offset_x;
    old_offset_y = offset_y;

    rectangle_on_screen=True;
}



/*----------------------------------------------------------------------*/
/*  h a n d l e _ z o o m _ e x p o s e					*/
/*----------------------------------------------------------------------*/

void handle_zoom_expose (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data)
{
    /* If an expose event has happen in draw_1 widget, the rectangle has */
    /* been deleted so set correct value to the variable */
    rectangle_on_screen=False;
}
