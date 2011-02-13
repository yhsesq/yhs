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


/*-----------------------------------------------------------------------------*/
/*                                                		               */
/*     File: region.c                              	     	               */
/*                                                   		               */
/*     Definition of a threshold prompt dialog widget.		               */
/*     The region growing and region cut algorithms are applied to the region  */
/*     chosen in the Input Window, starting with the seed selected by the user */
/*     with the mouse.							       */
/*									       */
/*     OSF/Motif version.      		                                       */
/*                                     		                               */
/*-----------------------------------------------------------------------------*/

#include "main.h"
#include <string.h>

#include <Xm/Xm.h>
#include <Xm/SelectioB.h>
#include <Xm/Form.h>
#include <Xm/Frame.h>
#include <Xm/DrawingA.h>
#include <Xm/PushB.h>
#include <Xm/CascadeB.h>
#include <Xm/Separator.h>
#include <Xm/RowColumn.h>
#include <Xm/Label.h>
#include <Xm/Xm.h>
#include <Xm/FileSB.h>
#include <Xm/DialogS.h>
#include <Xm/MessageB.h>
#include <Xm/Scale.h>
#include <Xm/BulletinB.h>
#include <Xm/ToggleB.h>

#define REGION_MESSAGE "Enter threshold value (0-255):"
#define INIT	0
#define VISITED 1
#define CONTOUR 2

/*Structure for the histogram*/
typedef struct
{	int grey_level;
	long  frequency;
} HISTO_ARRAY;

/* Extern variables */
extern char	     *action;
extern unsigned	     long bg;
extern REGIONPTR     region_list;
extern Cursor	     theCursor;
extern GC	     theGC, image_gc_1, image_gc_2;
extern XtAppContext  app;
extern int           file_loader;
extern int           squash[21];
extern int           run_once;
extern int           array2[512][512];
extern int           array1[512][512];
extern int           time;
extern char          *file_yhs;
extern GC            arrayGC[21];
extern GC            ysxorGC[21];
extern GC            ystheGC[21];
extern int	     running;
extern int           yhs_filename[11];
extern char          *yhs_filename1;
extern char          *yhs_filename2;
extern char          *yhs_filename3;
extern char          *yhs_filename4;
extern char          *yhs_filename5;
extern char          *yhs_filename6;
extern char          *yhs_filename7;
extern char          *yhs_filename8;
extern char          *yhs_filename9;
extern char          *yhs_filename10;
extern char          *tempfileold;
extern char          *tempfilenew;
extern char          *addcharac;
extern char          *yhs_file1;
extern char          *yhs_file2;
extern char          *yhs_file3;
extern char          *yhs_file4;
extern char          *yhs_file5;
extern char          *yhs_file6;
extern char          *yhs_file7;
extern char          *yhs_file8;
extern char          *yhs_file9;
extern char          *yhs_file10;
extern char          *yhs_file11;
extern char          *yhs_file12;
extern char          *yhs_file13;
extern char          *yhs_file14;
extern char          *yhs_file15;
extern char          *yhs_file16;
extern char          *yhs_file17;
extern char          *yhs_file18;
extern char          *yhs_file19;
extern char          *yhs_file20;
extern long          fg;
extern Pixmap        thePixmap_1;
extern Pixmap        thePixmap_2;
extern Colormap      ysmap;
extern XImage        *theXImage_1;
extern XImage        *theXImage_2;
extern Widget        view[21];
extern int           selection;
extern char          *action;
extern Widget        top_level, draw_1, draw_2, main_window, view_image;
extern void          refresh_action(void);
extern void          refresh_filename(void);
extern int           remove_images;
extern int           file_not_loaded;
extern int           vis_depth;
extern int           yhs_files_open;
extern int           one,two,three,four,five,six,seven,eight,nine,ten;
extern int           eleven,twelve,thirteen,fourteen,fifteen,sixteen,seventeen,eighteen,nineteen,twenty;
extern Visual        *theVisual;
extern int           engage_false_colour;
extern Pixmap        thePixmap_y[21];
extern XImage        *theXImage_y[21];
extern int           curfile,resize;
extern int           savefile;
extern Widget        manage1,manage2,manage3,manage4,manage5,manage6,manage7,manage8,manage9,manage10;
extern Widget        manage11,manage12,manage13,manage14,manage15,manage16,manage17,manage18,manage19,manage20;
extern Widget manager[21];
static unsigned char *image_1 = NULL;
static unsigned char *image_2 = NULL;
static unsigned char *image_y = NULL;
static XGCValues     xgcv;
extern char	     *file_name;


/* Internal variables */
/* matrix to implement the high speed way of looking for a grown pixel */
static short	pixel[IMAGE_WIDTH][IMAGE_HEIGHT]; 
static NODEPTR	list_region; /*list of points which define a grown region */
static Region	region; /* region mask to be used on region growing or region cut */
static int	threshold;

/* Variables for setting resources */
static Arg 	args[MAXARGS];
static Cardinal	argcount;
static Widget	region_dialog = (Widget) NULL;

/* Function prototypes */
void create_region_dialog(Widget parent);
void activate_region_dialog(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void activate_image_dialog(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);
void deactivate_region_dialog(void);
static void ok_button_callback(Widget w, XtPointer client_data, 
			XmSelectionBoxCallbackStruct *call_data);
static void cancel_button_callback(Widget w, XtPointer client_data, 
			XmSelectionBoxCallbackStruct *call_data);

void start_growing(Widget w, XtPointer client_data, XEvent *event);
static void grow(int x, int y, unsigned long threshold);
static void add_neighbours(NODEPTR *list_pixels, NODEPTR list_region,
		TPINFO *info, unsigned long mean, unsigned long threshold);

void start_image_growing(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data);
static void image_grow (unsigned long threshold);
extern XtCallbackProc handle_expose_y(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
extern XtCallbackProc squish(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);

int region_cut_callback(Widget w, XtPointer client_data, 
			XmAnyCallbackStruct *call_data);
void start_cut(Widget w, XtPointer client_data, XEvent *event);
static void cut();

static void create(NODEPTR *list);
static int list_empty(NODEPTR list);
static void insert(NODEPTR *list, TPINFO *info);
static TPINFO delete_first(NODEPTR *list);
static unsigned int num_elem(NODEPTR list);
static int search(int x, int y);
static void free_list(NODEPTR *list);
static unsigned long calculate_mean(NODEPTR list);

static int inside_limits(int x, int y);
static int inside_region(int x, int y);
static int inside_threshold(unsigned long pixel, unsigned long mean,
			unsigned long threshold);

static void view_pixel(NODEPTR list, unsigned long mean);
extern void delete_region (REGIONPTR *region_list, int type);
extern void refresh_action(void);
extern void prepare_handlers (int sel);
extern Region get_region (REGIONPTR *region_list, int x, int y);
extern Region get_first_region (REGIONPTR *region_list);

/* Function for sorting am array */
static void sort_array(HISTO_ARRAY *A);


/* Function definition */

/*----------------------------------------------------------------------*/
/*  c r e a t e _ r e g i o n _ d i a l o g				*/
/*									*/
/*  Create the threshold prompt dialog for the region growing		*/
/*  button in the tools menu						*/
/*----------------------------------------------------------------------*/

void create_region_dialog(Widget parent)
{
    XmString message;
    Widget temp_widget = parent;

    /* Ensure the parent of the dialog is a shell widget */
    while ( !XtIsShell(temp_widget) ) {
	temp_widget = XtParent(temp_widget);
    }

    message = XmStringLtoRCreate(REGION_MESSAGE, XmSTRING_DEFAULT_CHARSET);

    argcount = 0;
    XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
    region_dialog = XmCreatePromptDialog(temp_widget, "region dialog",
				args, argcount);

    /* Remove the help button from the dialog */
    temp_widget = XmSelectionBoxGetChild(region_dialog, XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(temp_widget);

    /* Add the actions to the buttons */
    XtAddCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) ok_button_callback, (XtPointer) NULL);
    XtAddCallback(region_dialog, XmNcancelCallback,
		(XtCallbackProc) cancel_button_callback, (XtPointer) NULL);

    XmStringFree(message);
}



/*----------------------------------------------------------------------*/
/*  a c t i v a t e _ r e g i o n _ d i a l o g				*/
/*									*/
/*  Activate the threshold prompt dialog box				*/
/*----------------------------------------------------------------------*/

void activate_region_dialog(Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data)
{
    XtRemoveCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) ok_button_callback, (XtPointer) NULL);
    XtRemoveCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) start_image_growing, (XtPointer) NULL);
    XtAddCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) ok_button_callback, (XtPointer) NULL);
    selection = REGION_GROWING;
    action = CLICK;
    refresh_action();
    XtManageChild(region_dialog);
}



/*----------------------------------------------------------------------*/
/*  a c t i v a t e _ i m a g e _ d i a l o g				*/
/*									*/
/*  Activate the threshold prompt dialog box				*/
/*----------------------------------------------------------------------*/

void activate_image_dialog(Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data)
{
    if (yhs_files_open < 1 || yhs_files_open > 19) return;

    XtRemoveCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) ok_button_callback, (XtPointer) NULL);
    XtRemoveCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) start_image_growing, (XtPointer) NULL);
    XtAddCallback(region_dialog, XmNokCallback,
		(XtCallbackProc) start_image_growing, (XtPointer) NULL);

    XtManageChild(region_dialog);
  
}



/*----------------------------------------------------------------------*/
/*  d e a c t i v a t e _ r e g i o n _ d i a l o g			*/
/*									*/
/*  Deactivate the threshold prompt dialog box				*/
/*----------------------------------------------------------------------*/

void deactivate_region_dialog(void) {
    /* null - no actions at present dialog is auto unmanaged */
    /* whenever any of its buttons are pressed.              */

}



/*----------------------------------------------------------------------*/
/*  o k _ b u t t o n _ c a l l b a c k					*/
/*									*/
/*  Callback for the "OK" button on the threshold dialog		*/
/*----------------------------------------------------------------------*/

static void ok_button_callback(Widget w, XtPointer client_data,
                               XmSelectionBoxCallbackStruct *call_data)
{
    int t;
    char *thres;

    /* Get threshold value from user's selection */
    XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thres);

    t = atoi(thres);
    XtUnmanageChild(region_dialog);
    XFlush(XtDisplay(region_dialog));

    if (!(t>=0 && t<=255) ) XBell(XtDisplay(w),100);
    else {
	threshold = t;
	prepare_handlers(REGION_GROWING); 
        fprintf(stderr,"\nUse selector to select the file(s).\nEnlarge the first file selected (if not already enlarged).\nSelect a point to grow in 3D on the first image selected...\n");
    }
}



/*----------------------------------------------------------------------*/
/*  s t a r t  _  g r o w i n g						*/
/*									*/
/*  Event handler to start region growing procedure			*/
/*----------------------------------------------------------------------*/

void start_growing(Widget w, XtPointer client_data, XEvent *event)
{
    int i,j,a,b;
    int t;
    char *thres;
    FILE *p_file;
    Arg al[10];
    int ac;
    int current;
    Display *display;
    Screen  *screen;
    FILE *pfile;
    Window root;
    Window window;
    Window new_win;
    unsigned int image_bytes;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */ 
    FILE *cfil;
    char tempfile[255];
    int tempo=0;
    char *tfile=".......................................................";
    int mouse_x, mouse_y;
    Region region_to_grow;

if (yhs_files_open < (21-yhs_filename[0]) && yhs_files_open > (yhs_filename[0]-1) && yhs_filename[0] > 0 && running == 0 && squash[curfile] == 0)
{
 
    if (event->xbutton.button != Button1) return;

    running=1;
    strcpy(tfile,yhs_filename1);
    /* Get actual co-ordinates of the mouse */
    mouse_y = event->xbutton.x;
    mouse_x = event->xbutton.y;

fprintf(stderr,"\n3D Processing ..Regions..");
    /* Associate the watch cursor with the main window */
    XDefineCursor(XtDisplay(view[curfile]), XtWindow(view[curfile]), theCursor); 

/* begin */

for (tempo=1; tempo < yhs_filename[0]+1; tempo++)
{ 

   if((pfile = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(pfile); }}
    fclose(pfile);

   /* Initialise the matrix to INIT label */
    for (i=0; i<IMAGE_WIDTH; i++) {
	for (j=0; j<IMAGE_HEIGHT; j++) { pixel[i][j] = INIT;}
    }

    /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    array2[i][j]=bg;

    /* Get union of region masks to be used in region growing algorithm */
    region = (Region)get_region(&region_list, mouse_x, mouse_y);

    /* Grow region */
    grow(mouse_x, mouse_y, (unsigned long) threshold);

    /* Release the memory ocupied by the region */
    XDestroyRegion(region);

    strcpy(tempfileold,tempfilenew);
    strcat(tempfilenew,addcharac);
   if((pfile = fopen(tempfileold,"w")) == NULL)
    {
        fprintf(stderr, "Cannot open: temporary file %s.\n",tempfileold);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { putc(array2[a][b],pfile); }}
    fclose(pfile);
remove_event_handlers(REGION_GROWING);
strcpy(file_name,tempfileold);

	#include <file_viewer.c>

if (tempo==1) strcpy(yhs_filename1,yhs_filename2);
if (tempo==2) strcpy(yhs_filename1,yhs_filename3);
if (tempo==3) strcpy(yhs_filename1,yhs_filename4);
if (tempo==4) strcpy(yhs_filename1,yhs_filename5);
if (tempo==5) strcpy(yhs_filename1,yhs_filename6);
if (tempo==6) strcpy(yhs_filename1,yhs_filename7);
if (tempo==7) strcpy(yhs_filename1,yhs_filename8);
if (tempo==8) strcpy(yhs_filename1,yhs_filename9);
if (tempo==9) strcpy(yhs_filename1,yhs_filename10);

}
strcpy(yhs_filename1,tfile);

/* end */

    /* Disassociate the watch cursor from the main window */
    XUndefineCursor(XtDisplay(view[curfile]), XtWindow(view[curfile])); 

remove_event_handlers(REGION_GROWING);
running=0;
fprintf(stderr,"..completed.\n");
}
}



/*----------------------------------------------------------------------*/
/*  s t a r t  _  i m a g e _ g r o w i n g				*/
/*									*/
/*  Event handler to start region growing procedure			*/
/*----------------------------------------------------------------------*/

void start_image_growing(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data)
{
    int i,j,a,b;
    int t;
    char *thres;
    FILE *p_file;
    Arg al[10];
    int ac;
    int current;
    Display *display;
    Screen  *screen;
    FILE *pfile;
    Window root;
    Window window;
    Window new_win;
    unsigned int image_bytes;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */ 
    FILE *cfil;
    char tempfile[255];
    int tempo=0;
    char *tfile=".......................................................";

    /* Get threshold value from user's selection */
    XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &thres);
 
    t = atoi(thres);
fprintf(stderr,"\nUse the selector to select files to process in 3D...\n");
if (yhs_files_open < (21-yhs_filename[0]) && yhs_files_open > (yhs_filename[0]-1) && yhs_filename[0] > 0 && running==0){
 
   if ( !(t>=0 && t<=255) )
    { XBell(XtDisplay(w),100); t=128; } /* default */

        XtUnmanageChild(region_dialog);
        XFlush(XtDisplay(region_dialog));
running=1;
fprintf(stderr,"\nProcessing...");
strcpy(tfile,yhs_filename1);
	threshold = t;
for (tempo=1; tempo < yhs_filename[0]+1; tempo++)
{ 
	/* Fill in the image with background color */
	for(i=0; i<IMAGE_WIDTH; i++)
	    for(j=0; j<IMAGE_HEIGHT; j++)
		array2[i][j]=bg; 

   if((pfile = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(pfile); }}
    fclose(pfile);

	/* Associate the watch cursor with the main window */
	XDefineCursor(XtDisplay(view[curfile]), XtWindow(view[curfile]), theCursor); 

	/* Initialise the matrix to INIT label */
	for (i=0; i<IMAGE_WIDTH; i++) {
	    for (j=0; j<IMAGE_HEIGHT; j++) { pixel[i][j] = INIT; }
	}

	/* Create a NULL region masks to be used in region growing algorithm */
	region = XCreateRegion(); 

	/* Grow full image */        
	image_grow ( (unsigned long) threshold);

	/* Release the memory ocupied by the region */
	XDestroyRegion(region); 
	
	/* Disassociate the watch cursor from the main window */
	XUndefineCursor(XtDisplay(view[curfile]), XtWindow(view[curfile])); 

    strcpy(tempfileold,tempfilenew);
    strcat(tempfilenew,addcharac);
   if((pfile = fopen(tempfileold,"w")) == NULL)
    {
        fprintf(stderr, "Cannot open: temporary file %s.\n",tempfileold);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { putc(array2[a][b],pfile); }}
    fclose(pfile);
remove_event_handlers(REGION_GROWING);
strcpy(file_name,tempfileold);

	#include <file_viewer.c>

if (tempo==1) strcpy(yhs_filename1,yhs_filename2);
if (tempo==2) strcpy(yhs_filename1,yhs_filename3);
if (tempo==3) strcpy(yhs_filename1,yhs_filename4);
if (tempo==4) strcpy(yhs_filename1,yhs_filename5);
if (tempo==5) strcpy(yhs_filename1,yhs_filename6);
if (tempo==6) strcpy(yhs_filename1,yhs_filename7);
if (tempo==7) strcpy(yhs_filename1,yhs_filename8);
if (tempo==8) strcpy(yhs_filename1,yhs_filename9);
if (tempo==9) strcpy(yhs_filename1,yhs_filename10);

}
running=0;
strcpy(yhs_filename1,tfile);
fprintf(stderr,"..completed.\n");
}
}


/*----------------------------------------------------------------------*/
/*  s t a r t  _  c u t 						*/
/*									*/
/*  Event handler to start cut growing procedure			*/
/*----------------------------------------------------------------------*/

void start_cut(Widget w, XtPointer client_data, XEvent *event)
{
    int i,j;

    /* Get the region mask to be cut. It is the last region defined */ 
    region = (Region)get_first_region(&region_list);

    /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

    /* Associate the watch cursor with the main window */
    XDefineCursor(XtDisplay(w), XtWindow(main_window), theCursor);
    /* Flush the request buffer and wait for all events */
    /* and errors to be processed by the server.        */
    XSync(XtDisplay(w), False);
    /* Cut region */        
    cut();
    /* Disassociate the watch cursor from the main window */
    XUndefineCursor(XtDisplay(w), XtWindow(main_window));
    /* Copy image into pixmap */
    XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2, 
		0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

    /* Delete the last region defined because it is only used to select */
    /* which part of the region we want to cut */
    delete_region (&region_list, 0);
}



/*----------------------------------------------------------------------*/
/*  c a n c e l _ b u t t o n _ c a l l b a c k				*/
/*									*/
/*  Callback for the "Cancel" button on the threshold dialog 		*/
/*----------------------------------------------------------------------*/
static void cancel_button_callback(Widget w, XtPointer client_data,
                                   XmSelectionBoxCallbackStruct *call_data)
{
    deactivate_region_dialog();
}



/*----------------------------------------------------------------------*/
/*  c r e a t e								*/
/*									*/
/*  Initialise the list							*/
/*----------------------------------------------------------------------*/

void create(NODEPTR *list)
{
    *list = NULL;
}



/*----------------------------------------------------------------------*/
/*  n u m _ e l e m							*/
/*									*/
/*  Number of items in the list						*/
/*----------------------------------------------------------------------*/

unsigned int num_elem(NODEPTR list)
{
    unsigned int i = 0;
    NODEPTR p;

    p = list;
    while (p != NULL) {
	i++;
	p = p->next;
    }
    return (i);
}



/*----------------------------------------------------------------------*/
/*  s e a r c h								*/
/*									*/
/*  Looks for the given pixel in the matrix. If it has already been	*/                           
/*  visited the function returns 1, otherwise it returns 0.		*/
/*----------------------------------------------------------------------*/

int search(int x, int y)
{
    return ( (pixel[x][y]>INIT) );
}



/*----------------------------------------------------------------------*/
/*  i n s e r t								*/
/*									*/
/*  Insert a new node in the list					*/
/*----------------------------------------------------------------------*/

void insert(NODEPTR *list, TPINFO *info)
{
    NODEPTR newone;
    int x, y;

    newone = (NODEPTR) malloc(sizeof(NODE)); 
    newone->infolist.coord_x = info->coord_x;
    newone->infolist.coord_y = info->coord_y;
    newone->infolist.grey_level = info->grey_level;
    newone->next = NULL;

    if (*list == NULL) { *list = newone; }
    else { newone->next = *list;
     	   *list = newone; }
}



/*----------------------------------------------------------------------*/
/*  d e l e t e _ f i r s t						*/
/*									*/
/*  Delete and return the first node in the list			*/
/*----------------------------------------------------------------------*/

TPINFO delete_first(NODEPTR *list)
{
    NODEPTR top;
    TPINFO deleted;

    if (*list != NULL) {
	top = *list;
	deleted.coord_x = top->infolist.coord_x;
	deleted.coord_y = top->infolist.coord_y;
	deleted.grey_level = top->infolist.grey_level;
	*list = (*list)->next;
	free(top);
    }
    return(deleted);
}



/*----------------------------------------------------------------------*/
/*  i n s i d e _ t h r e s h o l d					*/
/*									*/
/*  Look if the pixel value is inside the given threshold		*/
/*----------------------------------------------------------------------*/

int inside_threshold(unsigned long pixel, unsigned long mean,
                     unsigned long threshold)
{
    return( ( abs(pixel-mean)<=threshold ) );
}



/*----------------------------------------------------------------------*/
/*  i n s i d e _ r e g i o n						*/
/*									*/
/* Look if the pixel value is inside the limits of a defined region	*/
/* If the pixel is inside the function returns 1, otherwise it returns 0*/
/*----------------------------------------------------------------------*/

int inside_region(int x, int y)
{
    if ( XEmptyRegion(region) ) { return(1); }
    else { return( XPointInRegion(region, x, y) ); }
}



/*----------------------------------------------------------------------*/
/*  i n s i d e _ l i m i t s						*/
/*									*/
/*  Look if the pixel value is inside the limits of the image  		*/
/*----------------------------------------------------------------------*/

int inside_limits(int x, int y)
{
    return( (x!=0)&&(x!=IMAGE_WIDTH)&&(y!=0)&&(y!=IMAGE_HEIGHT) );
}



/*----------------------------------------------------------------------*/
/*  c a l c u l a t e _ m e a n						*/
/*									*/
/*  Calculate the mean value of all the pixels that define the		*/
/*  region.								*/
/*----------------------------------------------------------------------*/

unsigned long calculate_mean(NODEPTR list)
{
    int i;
    NODEPTR p;
    unsigned long sum = 0;
    unsigned int num_pixels;

    p = list;
    if ( p!=NULL ) {
	num_pixels = num_elem(p);
	for (i=1; i<=num_pixels; i++) {
	    sum = sum+(p->infolist.grey_level);
	    p = p->next;
	}
	return(sum/num_pixels);
    } else {return(sum);}
}



/*----------------------------------------------------------------------*/
/*  l i s t _ e m p t y							*/
/*									*/
/*  Returns True if the list is empty, otherwise returns False		*/
/*----------------------------------------------------------------------*/

int list_empty(NODEPTR list)
{
   return(list==NULL);
}



/*----------------------------------------------------------------------*/
/*  v i e w _ p i x e l							*/
/*									*/
/*  Copy the pixels that define the new region to the pixmap		*/
/*----------------------------------------------------------------------*/

void view_pixel(NODEPTR list, unsigned long mean)
{
    NODEPTR p;
    int x, y;

    p = list;
    while (p!=NULL) {
	x = p->infolist.coord_x;
	y = p->infolist.coord_y;
	mean = p->infolist.grey_level;
	array2[x][y]=mean;

	if ( (pixel[x-1][y]==INIT)||(pixel[x+1][y]==INIT) ||
	     (pixel[x][y-1]==INIT)||(pixel[x][y+1]==INIT) ) {
	  array2[x][y]=255;
	  pixel[x][y]=CONTOUR;
	}

	p = p->next;
    }
}



/*----------------------------------------------------------------------*/
/*  f r e e _ l i s t							*/
/*									*/
/*  Release the memory allocated by the list				*/
/*----------------------------------------------------------------------*/

void free_list(NODEPTR *list)
{
    NODEPTR p;

    while (*list!=NULL) {
	p = *list;
	*list = (*list)->next;
	free(p);
    }
}



/*----------------------------------------------------------------------*/
/*  r e g i o n _ c u t _ c a l l b a c k				*/
/*									*/
/*  Callback for the "Cut" button					*/
/*----------------------------------------------------------------------*/

int region_cut_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data)
{
    /*Prepare the widgets to be able to receive the corret events*/
    prepare_handlers(REGION_CUT);

    selection = REGION_CUT;
    action = CUT;
    refresh_action();
    return(selection);
}



/*----------------------------------------------------------------------*/
/*  a d d _ n e i g h b o u r s						*/
/*									*/
/*  Look for the four neighbours of the given pixel and			*/
/*  decide if they are added to the region or not.			*/
/*----------------------------------------------------------------------*/

void add_neighbours(NODEPTR *list_pixels, NODEPTR list_region,
		 TPINFO *info, unsigned long mean, unsigned long threshold)
{
    TPINFO cpinfo;
    NODEPTR pix;
    int x, y;

    pix = *list_pixels;

    /* Upper neighbour */
    cpinfo.coord_x = info->coord_x;
    cpinfo.coord_y = info->coord_y - 1;
    cpinfo.grey_level = array1[cpinfo.coord_x][cpinfo.coord_y];
    if ( inside_limits(cpinfo.coord_x,cpinfo.coord_y) )
	if ( inside_threshold(cpinfo.grey_level, mean, threshold) )
	    if ( !search(cpinfo.coord_x, cpinfo.coord_y) )
	/*	if (inside_region (cpinfo.coord_x,cpinfo.coord_y) ) */ {
		    insert(&pix, &cpinfo); 
		    /* Mark the pixel in the matrix as visited */
		    pixel[cpinfo.coord_x][cpinfo.coord_y] = VISITED;
		}

    /* Lower neighbour */
    cpinfo.coord_x = info->coord_x;
    cpinfo.coord_y = info->coord_y + 1;
    cpinfo.grey_level = array1[cpinfo.coord_x][cpinfo.coord_y];
    if ( inside_limits(cpinfo.coord_x,cpinfo.coord_y) )
	if ( inside_threshold(cpinfo.grey_level, mean, threshold) )
	    if ( !search(cpinfo.coord_x, cpinfo.coord_y) )
	/*	 if (inside_region (cpinfo.coord_x,cpinfo.coord_y) ) */ {
		    insert(&pix, &cpinfo);
		    /* Mark the pixel in the matrix as visited */
		    pixel[cpinfo.coord_x][cpinfo.coord_y] = VISITED;
		}

    /* Right neighbour */
    cpinfo.coord_x = info->coord_x + 1;
    cpinfo.coord_y = info->coord_y;
    cpinfo.grey_level = array1[cpinfo.coord_x][cpinfo.coord_y];
    if ( inside_limits(cpinfo.coord_x,cpinfo.coord_y) )
	if ( inside_threshold(cpinfo.grey_level, mean, threshold) )
	    if ( !search(cpinfo.coord_x, cpinfo.coord_y) )
	/*	if (inside_region (cpinfo.coord_x,cpinfo.coord_y) ) */  {
		    insert(&pix, &cpinfo);
		    /* Mark the pixel in the matrix as visited */
		    pixel[cpinfo.coord_x][cpinfo.coord_y] = VISITED;
		}

    /* Left neighbour */
    cpinfo.coord_x = info->coord_x - 1;
    cpinfo.coord_y = info->coord_y;
    cpinfo.grey_level = array1[cpinfo.coord_x][cpinfo.coord_y];
    if ( inside_limits(cpinfo.coord_x,cpinfo.coord_y) )
	if ( inside_threshold(cpinfo.grey_level, mean, threshold) )
	    if ( !search(cpinfo.coord_x, cpinfo.coord_y) )
	/*	if (inside_region (cpinfo.coord_x,cpinfo.coord_y) ) */ {
		    insert(&pix, &cpinfo);
		    /* Mark the pixel in the matrix as visited */
		    pixel[cpinfo.coord_x][cpinfo.coord_y] = VISITED;
		}

    *list_pixels = pix;
}



/*----------------------------------------------------------------------*/
/*   g r o w								*/
/*									*/
/*   Given a pixel it starts growing a region				*/
/*----------------------------------------------------------------------*/

void grow(int x, int y, unsigned long threshold)
{
    TPINFO info;
    int i, j;
    unsigned long mean;
    NODEPTR list_pixels; 
    info.coord_x = x;
    info.coord_y = y;
    info.grey_level = array1[info.coord_x][info.coord_y];

    /* release the memory of list_region because another one is going to be generated */
    free_list(&list_region);

    /* main loop of region growing */
    if ( inside_limits(info.coord_x,info.coord_y) ) 
    {
	create(&list_pixels);  
	create(&list_region);
	insert(&list_pixels, &info);
	mean = info.grey_level;
	while ( !list_empty(list_pixels) ) {
	    info = delete_first(&list_pixels);
	    insert(&list_region, &info);
	    /* XDrawPoint (XtDisplay(draw_2), XtWindow(draw_2), theGC,
				info.coord_x, info.coord_y); */
	    add_neighbours(&list_pixels, list_region, &info, mean, threshold);
	}
	mean = calculate_mean(list_region);
	view_pixel(list_region, mean);
	free_list(&list_pixels);
    } 
}



/*----------------------------------------------------------------------*/
/*  c u t								*/
/*									*/
/*   Given a tracking it starts cuting the pixels inside the tracking	*/
/*----------------------------------------------------------------------*/

void cut()
{
    int i, j, x, y, level, mean;
    NODEPTR aux1, aux_list;
    TPINFO info;

    create (&aux_list);

    aux1 = list_region;

    /* create a list without the pixels we want to cut*/
    while (aux1 != NULL) {
	x = aux1->infolist.coord_x;
    	y = aux1->infolist.coord_y;
	level = aux1->infolist.grey_level;

	info.coord_x = x;
	info.coord_y = y;
	info.grey_level = level;

	if (!XPointInRegion(region, x, y)) {
	    insert (&aux_list, &info);
	} else { pixel[x][y]=INIT; }

	aux1 = aux1->next;
    }

    free_list (&list_region);
    list_region = aux_list;
    mean = calculate_mean(list_region);
    view_pixel(list_region, mean);
}



/*----------------------------------------------------------------------*/
/*   i m a g e _ g r o w						*/
/*                                                          		*/
/*   Grow the whole image						*/
/*----------------------------------------------------------------------*/

void image_grow (unsigned long threshold)
{
    NODEPTR array_pixels[256];
    int	n, i, j, x, y;
    HISTO_ARRAY hist[256];
    TPINFO info, valores;
    unsigned long num_reg;

    /* initialise the array of pointers and the array of frequency */
    for (n=0; n<256; n++) {
	create(&array_pixels[n]);
	hist[n].grey_level = n;	
	hist[n].frequency = 0;
    }

    /* make the frequency histogram*/ 
    for (i=0; i<IMAGE_WIDTH; i++)
	for (j=0; j<IMAGE_HEIGHT; j++) {
	    info.grey_level = array1[i][j];
	    info.coord_x = i;
	    info.coord_y = j;
	    insert(&array_pixels[info.grey_level],&info);
	    hist[info.grey_level].frequency+=1;
	    pixel[i][j] = INIT;
	}

    /* Sort the histogram array in crescending order or frequency */
    sort_array (hist);

    /* Compute the image growing */
    num_reg=0;
    for (n=255; n>=0; n--)
	while ( array_pixels[ hist[n].grey_level ]!=NULL ) {
	    valores = delete_first(&array_pixels[ hist[n].grey_level ]);
	    x = valores.coord_x;
	    y = valores.coord_y;
	    if (!search(x,y)) {
		grow(x,y,threshold);
		num_reg++;
	    }
	}
    fprintf(stderr,".%i.",num_reg);
}



/*----------------------------------------------------------------------*/
/*   s o r t _ a r r a y						*/
/*									*/
/*   Sort an array in ascending order					*/
/*----------------------------------------------------------------------*/

void sort_array(HISTO_ARRAY *A)
{
    int i, j;
    HISTO_ARRAY aux;

    for (i=1; i<=255; i++)
	for (j=255; j>=i; j--)
	    if (A[j].frequency < A[j-1].frequency)
		{
		    aux.frequency = A[j-1].frequency;
		    aux.grey_level = A[j-1].grey_level;
		    A[j-1].frequency=A[j].frequency;
		    A[j-1].grey_level=A[j].grey_level;
		    A[j].frequency = aux.frequency;
		    A[j].grey_level = aux.grey_level;
		}
}
