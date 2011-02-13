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
/*	File: editing_tools.c					*/
/*								*/
/*	OSF/Motif version.					*/
/*								*/
/*	December 1996						*/
/*								*/
/*--------------------------------------------------------------*/

#include "main.h"
#include <math.h>

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

typedef struct FIGURE
{   int     type;     /* type of the figure		*/
    short   x1, y1;   /* Corners of bounding rectangle	*/
    short   x2, y2;   /* or end-points of line		*/
} FIGURE;
FIGURE  figures;

/* Extern variables */
extern char 	*action;
extern int 	selection;
extern GC	theGC;
extern GC       ystheGC[21];
extern GC	xorGC;
extern GC       arrayGC[21];
extern GC       ysxorGC[21];
extern int      yhs_files_open;
extern int	yhs_filename[11];
extern Widget	draw_1,manager[21],manage1,view[21];
extern int      curfile;
extern XImage 	*theXImage_1;
extern REGIONPTR region_list;

extern void refresh_action(void);
extern void prepare_handlers(int sel);
extern NODEPTR insert_region (REGIONPTR *region_list,
			 XPoint *points, int num_points, int label);
extern void delete_region (REGIONPTR *region_list, int type);



/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;
static Widget tools_window = (Widget) NULL;

static float	radius;
static Cursor	otherCursor;
static XPoint	points[MAXPOINTS]; /* array of points which define a polygon */
static int	xc, yc,
		figure = NONE,
		num_points;


/* Function prototypes */
void create_editing_tools_window(Widget parent);
void activate_editing_tools_window(Widget w, XtPointer client_data, 
                             XmAnyCallbackStruct *call_data);

int tracking_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
int rectangles_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
int circles_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
int ellipses_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
int clear_figure_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
int quit_me_callback(Widget w, XtPointer client_data,
                        XmDrawingAreaCallbackStruct *call_data);

void start_tracking(Widget w, XtPointer data, XEvent *p_event);
void continue_tracking(Widget w, XtPointer data, XEvent *p_event);
void end_tracking(Widget w, XtPointer data, XEvent *p_event);

void start_rubberband(Widget w, XtPointer data, XEvent *p_event);
void continue_rubberband(Widget w, XtPointer data, XEvent *p_event);
void end_rubberband(Widget w, XtPointer data, XEvent *p_event);

void draw_figure(Display *d, Window  w, GC gc);
void show_region(NODEPTR aux);

void get_point_list_of_polygon(XPoint *points, int *numpoints);


/* Function definition */

/*--------------------------------------------------------------*/
/*  c r e a t e _ e d i t i n g _ t o o l s _ w i n d o w	*/
/*								*/
/*  Create the prompt dialog for the threshold button in the	*/
/*  tools menu							*/
/*--------------------------------------------------------------*/

void create_editing_tools_window(Widget parent)
{
    Display *display;
    Widget temp_widget = parent;
    Widget rowcol, tool1, tool2, tool3, tool4, tool5, tool6;
    int argcount=0;
    static Pixmap app_icon = (Pixmap) NULL;

    /* Ensure the parent of the dialog is a shell widget */
    while ( !XtIsShell(temp_widget) ) {
	temp_widget = XtParent(temp_widget);
    }

    tools_window = XmCreateFormDialog(temp_widget, "tools window", NULL, 0);

    rowcol = XtVaCreateManagedWidget ("rowcol", xmRowColumnWidgetClass,
		tools_window, NULL, 0);
    /*Add all the buttons to the tools window*/
    tool2 = XtVaCreateManagedWidget ("Rectangle", xmPushButtonWidgetClass,
		rowcol, XmNwidth, 500, XmNheight, 500, NULL);
    tool3 = XtVaCreateManagedWidget ("Circle", xmPushButtonWidgetClass,
		rowcol, XmNwidth, 500, XmNheight, 500, NULL);
    tool4 = XtVaCreateManagedWidget ("Ellipse", xmPushButtonWidgetClass,
		rowcol, XmNwidth, 500, XmNheight, 500, NULL);
    tool5 = XtVaCreateManagedWidget ("Clear figure", xmPushButtonWidgetClass,
		rowcol, XmNwidth, 500, XmNheight, 500, NULL);
    tool6 = XtVaCreateManagedWidget ("Exit Popup", xmPushButtonWidgetClass,
                rowcol, XmNwidth, 500, XmNheight, 500, NULL);

    /*Add funtion call to each button defined*/
/*    XtManageChild (tool1); */
    /* XtAddCallback(tool1, XmNactivateCallback,
		(XtCallbackProc) tracking_callback, (XtPointer) NULL); */
    XtManageChild (tool2);
    XtAddCallback(tool2, XmNactivateCallback,
		(XtCallbackProc) rectangles_callback, (XtPointer) NULL);
    XtManageChild (tool3);
    XtAddCallback(tool3, XmNactivateCallback,
		(XtCallbackProc) circles_callback, (XtPointer) NULL);
    XtManageChild (tool4);
    XtAddCallback(tool4, XmNactivateCallback,
		(XtCallbackProc) ellipses_callback, (XtPointer) NULL);
    XtManageChild (tool5);
    XtAddCallback(tool5, XmNactivateCallback,
		(XtCallbackProc) clear_figure_callback, (XtPointer) NULL);
    XtManageChild (tool6);
    XtAddCallback(tool6, XmNactivateCallback,
                 (XtCallbackProc) quit_me_callback, (XtPointer) NULL);
}



/*-------------------------------------------------------------*/
/*  a c t i v a t e _ t o o l s _ w i n d o w                  */
/*                                                             */
/*  Activate the tools   dialog box                            */
/*-------------------------------------------------------------*/
void activate_editing_tools_window(Widget w, XtPointer client_data,
		XmAnyCallbackStruct *call_data)
{
if (yhs_files_open > 0 && yhs_filename[0] != 0)
{
  XtManageChild(tools_window);
}}



/*------------------------------------------------------------- */
/* r e c t a n g l e s _ c a l l b a c k 			*/
/*								*/
/* Callback for the "Rectangles" button on the polygons menu.	*/
/* This function draws the polygons in the Input window.	*/
/*--------------------------------------------------------------*/

int rectangles_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{
    /*Prepare the widgets to be able to receive the correct events*/
    prepare_handlers(RECTANGLES);

    /*Indicate that the current figure is a rectangle*/ 
    figure = RECTANGLES;

    selection= RECTANGLES;
    action = DRAW;
    refresh_action();


     /* Create a cross-hair cursor for the drawing area */
    otherCursor = XCreateFontCursor(XtDisplay(view[curfile]),  XC_crosshair);

    /* Set up a grab so that the cursor changes to a cross-hair and */
    /* is confined to the drawing_area while the mouse button is    */
    /* pressed. This is done through what is known as a "grab"      */
    XGrabButton(XtDisplay(view[curfile]), Button3, AnyModifier, XtWindow(view[curfile]), True,
		ButtonPressMask | Button3MotionMask | ButtonReleaseMask, 
                GrabModeAsync, GrabModeAsync, XtWindow(view[curfile]), otherCursor);
    return(selection);
}


/*------------------------------------------------------------- */
/* e l l i p s e s _ c a l l b a c k 		        	*/
/*								*/
/* Callback for the "Ellipses" button on the polygons menu.	*/
/* This function draws the polygons in the Input window.	*/
/*--------------------------------------------------------------*/

int ellipses_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{
    /*Prepare the widgets to be able to receive the corret events*/
    prepare_handlers(ELLIPSES);

    /*Indicate that the current figure is an ellipse*/ 
    figure = ELLIPSES;
    
    selection= ELLIPSES;
    action = DRAW;
    refresh_action();
    
    /* Create a cross-hair cursor for the drawing area */
    otherCursor = XCreateFontCursor(XtDisplay(view[curfile]),  XC_crosshair); 
   
    /* Set up a grab so that the cursor changes to a cross-hair and
     * is confined to the drawing_area while the mouse button is
     * pressed. This is done through what is known as a "grab" */
    XGrabButton(XtDisplay(view[curfile]), Button3, AnyModifier, XtWindow(view[curfile]), True,
		ButtonPressMask | Button3MotionMask | ButtonReleaseMask, 
                GrabModeAsync, GrabModeAsync, XtWindow(view[curfile]), otherCursor);


    return(selection);
}


/*------------------------------------------------------------- */
/* c i r c l e s _ c a l l b a c k 		        	*/
/*								*/
/* Callback for the "Circles" button on the polygons menu.	*/
/* This function draws the polygons in the Input window.	*/
/*--------------------------------------------------------------*/

int circles_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{
    /*Prepare the widgets to be able to receive the corret events*/
    prepare_handlers(CIRCLES);

    /*Indicate that the current figure is a circle*/ 
    figure = CIRCLES;

    selection= CIRCLES;
    action = DRAW;
    refresh_action();
    
    /* Create a cross-hair cursor for the drawing area */
    otherCursor = XCreateFontCursor(XtDisplay(view[curfile]),  XC_crosshair); 

    /* Set up a grab so that the cursor changes to a cross-hair and
     * is confined to the drawing_area while the mouse button is
     * pressed. This is done through what is known as a "grab" */
    XGrabButton(XtDisplay(view[curfile]), Button3, AnyModifier, XtWindow(view[curfile]), True,
		ButtonPressMask | Button3MotionMask | ButtonReleaseMask, 
                GrabModeAsync, GrabModeAsync, XtWindow(view[curfile]), otherCursor);

    return(selection);   
}


/*--------------------------------------------------------------*/
/*  s t a r t _ r u b b e r b a n d				*/
/*								*/
/*  Start of rubber-band figure					*/
/*--------------------------------------------------------------*/

void start_rubberband(Widget w, XtPointer data, XEvent *p_event)
{
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y;

    /*Rubberband only allowed using third button*/
    if (p_event->xbutton.button == Button3)
    {
	/*Set all the needed values for figure representation*/
	figures.type = figure;
	figures.x1 = x;
	figures.y1 = y;
	figures.x2 = x;
	figures.y2 = y;
	/*Draw the figure using XOR mask*/
	draw_figure(XtDisplay(view[curfile]), XtWindow(view[curfile]), ysxorGC[curfile]);
    }
}


/*--------------------------------------------------------------*/
/*  c o n t i n u e _ r u b b e r b a n d			*/
/*								*/
/*  Handle mouse movement while drawing a rubber-band figure	*/
/*--------------------------------------------------------------*/

void continue_rubberband(Widget w, XtPointer data, XEvent *p_event)
{
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y;

    /* Draw once at old location (to erase figure) */
    draw_figure(XtDisplay(view[curfile]), XtWindow(view[curfile]), ysxorGC[curfile]);

    /* Now update end-point and redraw */
    figures.x2 = x;
    figures.y2 = y;
    draw_figure(XtDisplay(view[curfile]), XtWindow(view[curfile]), ysxorGC[curfile]);
}


/*--------------------------------------------------------------*/
/*  e n d _ r u b b e r b a n d					*/
/*								*/
/*  End of rubber-band drawing					*/
/*--------------------------------------------------------------*/

void end_rubberband(Widget w, XtPointer data, XEvent *p_event)
{
    XPoint points[MAXPOINTS];
    NODEPTR aux;
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y,
    	numpoints, i;
 
    if (p_event->xbutton.button == Button3)
    {
	/* Draw once at old location (to erase figure) */
	draw_figure(XtDisplay(view[curfile]), XtWindow(view[curfile]), ysxorGC[curfile]);

	/* Now update end-point and redraw in normal GC */
	figures.x2 = x;
	figures.y2 = y;

	/*Include the points that define the figure in the array of points*/
	points[0].x = figures.x1; points[0].y = figures.y1;
	points[1].x = figures.x2; points[1].y = figures.y2;
	
	get_point_list_of_polygon(points, &numpoints);
	aux = insert_region(&region_list, points, numpoints, figure);
	show_region(aux);

	action = SELECT;
	refresh_action();
    }
}



/*----------------------------------------------------------------------*/
/*  d r a w _ f i g u r e						*/
/*									*/
/*  Draw a specified figure while the user keeps pressed the button 	*/
/*----------------------------------------------------------------------*/

void draw_figure(Display *d, Window  w, GC gc)
{
    int x1 = figures.x1, y1 = figures.y1,
        x2 = figures.x2, y2 = figures.y2,
	type = figures.type, t;
        
    /* Make sure x2 >= x1 and y2 >= y1 */
    if(x1 > x2) { t=x1;  x1=x2;  x2=t; }
    if(y1 > y2) { t=y1;  y1=y2;  y2=t; }

    /* Draw figure according to its type */
    switch (type)
    {
    case RECTANGLES:
	XDrawRectangle(d, w, gc, x1, y1, x2-x1, y2-y1);
	break;

    case ELLIPSES:
	xc = (x2-x1)/2 + x1;
	yc = (y2-y1)/2 + y1;
	XDrawArc(d, w, gc, x1, y1, x2-x1, y2-y1,0, 360*64);
	break;

    case CIRCLES:
	if(x2-x1 > y2-y1) {
	    xc = (x2-x1)/2 + x1;
	    yc = (x2-x1)/2 + y1;
	    radius =  (x2-x1)/2;
    	    XDrawArc(d, w, gc, x1, y1, x2-x1, x2-x1,0, 360*64);
	} else {
	    xc = (y2-y1)/2 + x1;
	    yc = (y2-y1)/2 + y1;
	    radius =  (y2-y1)/2;
	    XDrawArc(d, w, gc, x1, y1, y2-y1, y2-y1,0, 360*64);   
	}
	break;
   
    default : break;
    }
}



/*--------------------------------------------------------------*/
/*  g e t _ p o i n t _ l i s t _ o f _ p o l y g o n		*/
/*								*/
/*  Gets a list of points from the array of points		*/
/*--------------------------------------------------------------*/

void get_point_list_of_polygon(XPoint *points, int *numpoints)
{
    int i, a ,b, aux;
    float a2, b2, ab,
	  rad, seno, cose;

    switch (figures.type)
    {
    case RECTANGLES:
	/*The four vertexes define the rectangle*/
	points[0].x = figures.x1;
	points[0].y = figures.y1;
	points[1].x = figures.x1;
	points[1].y = figures.y2;
	points[2].x = figures.x2;
	points[2].y = figures.y2;
	points[3].x = figures.x2;
	points[3].y = figures.y1;
	*numpoints = 4;
	break;

    case CIRCLES:
	/*Get a sample of points that define the circle*/
	*numpoints = (radius*2);
	for (i=0; i<(*numpoints); i++)
	{
	    rad = (2*PI*i)/(*numpoints);
	    points[i].x = (cos(rad)*radius) + xc;
	    points[i].y = (sin(rad)*radius) + yc;
	}
	break;

    case ELLIPSES:
	/*Get a sample of points that define the circle*/
	a = figures.x2 - figures.x1;
	b = figures.y2 - figures.y1;
	a2 = a*a;
	b2 = b*b;
	ab = a*b;
       	for (i=0; i<160; i++)
	{
	    rad = (2*PI*i)/160;
	    seno = sin(rad);
	    cose = cos(rad);
	    radius = ab / (sqrt ( a2*pow(seno,2) + b2*pow(cose,2) ));
	    points[i].x = (cos(rad)*radius*0.5) + xc;
	    points[i].y = (sin(rad)*radius*0.5) + yc;
	}
	*numpoints = 160;
	break;
    }
}



/*------------------------------------------------------------- */
/* t r a c k i n g _ c a l l b a c k 	          		*/
/*								*/
/* Callback for the "Tracking" button on the segmentation menu.	*/
/* This function draws the mouse tracking in the Input window.	*/
/*--------------------------------------------------------------*/

int tracking_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{   
    /*Prepare the widgets to be able to receive the corret events*/
    prepare_handlers(TRACKING);

    /*Indicate that the current figure is a circle*/ 
    selection = TRACKING;

    action = TRACK;
    refresh_action();
    return(selection);
}



/*--------------------------------------------------------------*/
/*  s t a r t _ t r a c k i n g					*/
/*								*/
/*  Start of mouse tracking					*/
/*--------------------------------------------------------------*/

void start_tracking(Widget w, XtPointer data, XEvent *p_event)
{
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y;
    TPINFO info;

    if (p_event->xbutton.button == Button3)
    {
	/*Set first point as origin figure co-ordinates*/
	num_points = 1;
	points[0].x = x;
	points[0].y = y;

	/*Draw point on window*/
	XDrawPoint(XtDisplay(view[curfile]), XtWindow(view[curfile]), ystheGC[curfile], x, y);

	/*Store information for node structure*/
	info.coord_x = x;
	info.coord_y = y;
	info.grey_level = XGetPixel(theXImage_1,info.coord_x,info.coord_y);
    }
}


/*--------------------------------------------------------------*/
/*  c o n t i n u e _ t r a c k i n g				*/
/*								*/
/* Handle mouse movement while drawing the mouse tracking	*/
/*--------------------------------------------------------------*/

void continue_tracking(Widget w, XtPointer data, XEvent *p_event)
{
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y;
    TPINFO info;

    num_points++;
    /*Save all the retrieved co-ordinates in the array*/
    points[num_points-1].x = x;
    points[num_points-1].y = y;

    /*Draw point on window*/
    XDrawLine(XtDisplay(view[curfile]), XtWindow(view[curfile]), ystheGC[curfile],
		points[num_points-2].x, points[num_points-2].y,
		points[num_points-1].x, points[num_points-1].y);

    /*Store information for node structure*/
    info.coord_x = x;
    info.coord_y = y;
/*    info.grey_level = XGetPixel(theXImage_1,info.coord_x,info.coord_y); */
}


/*--------------------------------------------------------------*/
/*  e n d _ t r a c k i n g					*/
/*								*/
/*  End of mouse tracking					*/
/*--------------------------------------------------------------*/

void end_tracking(Widget w, XtPointer data, XEvent *p_event)
{
    int x = p_event->xbutton.x,
        y = p_event->xbutton.y;
    TPINFO info;
 
    if (p_event->xbutton.button == Button3)
    {
	num_points++;
	/*Save the last retrieved co-ordinate*/
	points[num_points-1].x = x;
	points[num_points-1].y = y;

	/*Draw point on window*/
	XDrawLine(XtDisplay(view[curfile]), XtWindow(view[curfile]), ystheGC[curfile],
		points[num_points-1].x, points[num_points-1].y,
		points[0].x, points[0].y);

	/*Store information for node structure*/
	info.coord_x = x;
	info.coord_y = y;
/*	info.grey_level = XGetPixel(theXImage_1,info.coord_x,info.coord_y); */

	insert_region (&region_list, points, num_points, TRACKING);
	action = SELECT;
	refresh_action();
    }
}


/*--------------------------------------------------------------*/
/*  s h o w _ r e g i o n					*/
/*								*/
/*  Draw the region boundaries defined in the point list	*/
/*--------------------------------------------------------------*/

void show_region(NODEPTR point_list)
{
    int x1, y1, x2, y2;
    NODEPTR aux;

    aux = point_list;
    if (aux != NULL) {
	x1 = aux->infolist.coord_x;
	y1 = aux->infolist.coord_y;
    }
    aux = aux->next;
    while (aux != NULL) {
	x2 = aux->infolist.coord_x;
	y2 = aux->infolist.coord_y;	
	/*Draw the segments that define the contour of the figure*/
	XDrawLine(XtDisplay(view[curfile]), XtWindow(view[curfile]),
			ystheGC[curfile], x1, y1, x2, y2);
	x1 = x2;
	y1 = y2;
	aux = aux->next;
    }
    x2 = point_list->infolist.coord_x;
    y2 = point_list->infolist.coord_y;
    /*Draw last segment*/
    XDrawLine(XtDisplay(view[curfile]), XtWindow(view[curfile]), ystheGC[curfile], x1, y1, x2, y2);
}



/*------------------------------------------------------------- */
/* c l e a r _ f i g u r e _ c a l l b a c k			*/
/*								*/
/* Callback for the "Clear figure" button on the options menu.	*/
/* This function deletes the latest region defined.		*/
/*--------------------------------------------------------------*/

int clear_figure_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{   
    delete_region (&region_list, 1);

    /*Delete the draw_1 widget contents and provoke an expose event in */
    /*draw_1 widget so expose_handle_1 funtion defined in slice.c will */
    /* redraw the screen show the rest of the polygons defined */
    XClearArea(XtDisplay(view[curfile]), XtWindow(view[curfile]), 0, 0, 0, 0, True);
}

/*------------------------------------------------------------- */
/* q u i t _ m e _ c a l l b a c k	   			*/
/*								*/
/* Callback for the "Exit" button on the options menu.		*/
/* This function deletes the options menu.			*/
/*--------------------------------------------------------------*/

int quit_me_callback(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data)
{   
   XtUnmanageChild(tools_window);
}
