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
/*     File: slice.c                                           */
/*                                                             */
/*     CT Image Application Program                            */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*     V1.0 March 1996                                         */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"
#include "icon.h"

#include <Xm/Xm.h>
#include <Xm/Form.h>
#include <Xm/Frame.h>
#include <X11/Shell.h>
#include <Xm/DrawingA.h>
#include <X11/cursorfont.h>
#include <Xm/BulletinB.h>
#include <Xm/PushB.h>
#include <Xm/Label.h>
#include <Xm/RowColumn.h>
#include <string.h>
#include <stdlib.h>
#include <sys/utsname.h>
#include <sys/types.h>
#include <unistd.h>

#define WIDTH           900 /*1000*/
#define HEIGHT          38 /*700*/
#define APP_TITLE  "MultiSlice_RTP Environment (CT App Ver.2.1.4c+)"
#define ICON_TITLE "MultiSliceRTP"


/* Global Variables */

struct utsname mach;
struct utsname *machptr;
unsigned long  fg, bg;
FILE     *r_file;
int     time=0;
Cursor	theCursor;
GC	image_gc_1;
GC	image_gc_2;
int     a,b;
int     curfile=1,resize=0;
int     array1[512][512];
int     array2[512][512];
GC      theGC;
GC      ystheGC[21];
GC      xorGC;
GC      ysxorGC[21];
Pixmap	thePixmap_1;
Pixmap	thePixmap_2;
int     run_once=0;
int 	running=0;
XImage	*theXImage_1 = NULL;
XImage	*theXImage_2 = NULL;
char	*action = LOAD;		/* Action to perform */
char    *file_command;
char    *file_load;
REGIONPTR region_list;		/* List of regions which are going to be defined */
Visual	*theVisual;
Colormap ysmap;
int	vis_depth;
char    *yhs_file1="Empty file name ......................................";
char    *yhs_file2="Empty file name ......................................";
char    *yhs_file3="Empty file name ......................................";
char    *yhs_file4="Empty file name ......................................";
char    *yhs_file5="Empty file name ......................................";
char    *yhs_file6="Empty file name ......................................";
char    *yhs_file7="Empty file name ......................................";
char    *yhs_file8="Empty file name ......................................";
char    *yhs_file9="Empty file name ......................................";
char    *yhs_file10="Empty file name ......................................";
char    *yhs_file11="Empty file name ......................................";
char    *yhs_file12="Empty file name ......................................";
char    *yhs_file13="Empty file name ......................................";
char    *yhs_file14="Empty file name ......................................";
char    *yhs_file15="Empty file name ......................................";
char    *yhs_file16="Empty file name ......................................";
char    *yhs_file17="Empty file name ......................................";
char    *yhs_file18="Empty file name ......................................";
char    *yhs_file19="Empty file name ......................................";
char    *yhs_file20="Empty file name ......................................";
char    *tempfileold="Empty file name .....................................";
char    *tempfilenew="Empty file name .....................................";
char    *addcharac="Z";
int     one=1;
int     two=2;
int     three=3;
int     four=4;
int     five=5;
int     six=6;
int     seven=7;
int     eight=8;
int     nine=9;
int     ten=10;
int     eleven=11;
int     twelve=12;
int     thirteen=13;
int     fourteen=14;
int     fifteen=15;
int     sixteen=16;
int     seventeen=17;
int     eighteen=18;
int     nineteen=19;
int     twenty=20;
int     file_loader=0;
int     yhs_files_open;
int     yhs_filename[11];
int     squash[21];
char    *yhs_filename1="Empty file name xxx...................................";
char    *yhs_filename2="Empty file name xxx...................................";
char    *yhs_filename3="Empty file name xxx...................................";
char    *yhs_filename4="Empty file name xxx...................................";
char    *yhs_filename5="Empty file name xxx...................................";
char    *yhs_filename6="Empty file name xxx...................................";
char    *yhs_filename7="Empty file name xxx...................................";
char    *yhs_filename8="Empty file name xxx...................................";
char    *yhs_filename9="Empty file name xxx...................................";
char    *yhs_filename10="Empty file name xxx...................................";
int     engage_false_colour=0;
int     yhs;
GC      arrayGC[21];
Pixmap  thePixmap_y[21];
XImage  *theXImage_y[21]; 
int	selection = NONE,
	remove_images = False,
	file_not_loaded = True;
char    *file_yhs="File not loaded";
XtAppContext app;

/* Set of Widgets used in this file */
Widget	top_level, main_window, menu_bar,
	draw_1, draw_2,	frame_1, frame_2,
	frame_3, frame_4, frame_5, frame_6, frame_7, view_image;
Widget  manager[21];
Widget  manage1,manage2,manage3,manage4,manage5,manage6,manage7,manage8,manage9,manage10;
Widget  manage11,manage12,manage13,manage14,manage15,manage16,manage17,manage18,manage19,manage20;
Widget  view[21];

/* Local Variables */
static char 	*theAppName;			/* Name of application */
static Pixmap	app_icon = (Pixmap) NULL;	/* Application icon    */
static int	left_button=NONE,  /*Operation to be performed by clicking left_button */
		right_button=NONE; /*Operation to be performed by clicking right_button*/


/* Variables for setting resources */
static Arg	args[MAXARGS];
static Cardinal argcount;


/* Function prototypes */
void create_main_menu(Widget parent);
extern void update_time();
void create_labels(Widget w1, Widget w2, Widget w3, Widget w4, Widget w5);

void handle_expose_1(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
void handle_expose_2(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);

void mouse_track_1(Widget w, XtPointer client_data, XEvent *event);
void mouse_track_2(Widget w, XtPointer client_data, XEvent *event);

void refresh_action(void);
void refresh_coord(int x, int y, int v);
void sq(int b);

void prepare_handlers(int sel);
void remove_event_handlers(int sel);

void setup_display(Widget w);
void print_callback(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);
extern void start_tracking(Widget w, XtPointer data, XEvent *p_event);
extern void continue_tracking(Widget w, XtPointer data, XEvent *p_event);
extern void end_tracking(Widget w, XtPointer data, XEvent *p_event);

extern void start_rubberband(Widget w, XtPointer data, XEvent *p_event);
extern void continue_rubberband(Widget w, XtPointer data, XEvent *p_event);
extern void end_rubberband(Widget w, XtPointer data, XEvent *p_event);

extern void start_growing(Widget w, XtPointer client_data, XEvent *event);
extern void start_cut(Widget w, XtPointer client_data, XEvent *event);

extern void start_zoom(Widget w, XtPointer client_data, XEvent *event);
extern void move_zoom(Widget w, XtPointer data, XEvent *p_event);
extern void handle_zoom_expose (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void multi_window_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void cascade_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void tile_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void delete_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void reduce_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
void red_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void expand_callback (Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data);
extern void display_regions(REGIONPTR region_list);
extern void create_region_list(REGIONPTR *list);

XtCallbackProc handle_expose_y(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
XtCallbackProc squish(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);

/*-----------------------------------------------------------------------*/
/*-----------------------------------------------------------------------*/
/*----------------------------  MAIN PROGRAM ----------------------------*/
/*-----------------------------------------------------------------------*/
/*-----------------------------------------------------------------------*/

void main(int argc, char **argv)
{
      int i=21,forcer=0; /* init variables. */
      Display *p_disp;
      FILE *fpo;
      char text[]="MultiSliceRTPsession0";

    /* Create the top-level shell widget and initialize the toolkit */
    argcount = 0;
    XtSetArg(args[argcount], XmNallowShellResize, False); argcount++;
    XtSetArg(args[argcount], XmNtitle,       APP_TITLE); argcount++;
    XtSetArg(args[argcount], XmNiconName,   ICON_TITLE); argcount++;
    top_level = XtAppInitialize(&app, "Slice", NULL, 0,
                                &argc, argv, NULL, args, argcount);
    theAppName = argv[0];

    #include "command_slice.c"
 
    /* Create the main window widget */
    argcount = 0;
/*    XtSetArg(args[argcount], XmNwidth ,  WIDTH); argcount++;
      XtSetArg(args[argcount], XmNheight, HEIGHT); argcount++; */
    main_window = XmCreateRowColumn(top_level, "Main", args, argcount);
    XtManageChild(main_window);
    p_disp=XtDisplay(main_window);
machptr=&mach;
uname(machptr);

if(forcer != 1)
{
/*      if(XFetchBytes(p_disp,&i) != NULL){ if(i==21){
      fprintf(stderr,"\nOnly one copy of MultiSlice can run at one time...\nslice -forceload will forcibly load a second copy.\nSelf-Destruct Initialised...\nSecond copy destructed...OK\n");
      XtCloseDisplay(XtDisplay(main_window));exit(-1);}}
      i=21; XStoreBytes(p_disp,text,i); */
if((fopen("slicetempyhsTN","r")) != 0){
      fprintf(stderr,"\nOnly one copy of MultiSlice RTP can run at one time...\nslice -forceload will forcibly load a second copy if \n the first one was terminated incorrectly.\n");
      system("cat slicetempyhsTN");
      fprintf(stderr,"\nSelf-destruct initialised...\nSecond copy destructed...OK\n");
      XtCloseDisplay(XtDisplay(main_window));exit(-1);}}
      i=0;
      strcpy(tempfileold,"slicetempyhsT");
      strcpy(tempfilenew,"slicetempyhsT");
      addcharac[1]='\0';
      strcat(tempfilenew,addcharac);
      system("echo First copy of MultiSliceRTP started at :- >slicetempyhsTN");
      system("date >>slicetempyhsTN");
      system("printenv USER >>slicetempyhsTN");
      system("echo System dump ------------- >>slicetempyhsTN");
      system("ps -ef | grep slice >>slicetempyhsTN");
      for(i=0;i<11;i++){yhs_filename[i]=0;squash[i]=0;squash[i+10]=0;} 
      i=0;
fprintf(stderr,"\n\n-------------------MultiSlice RTP Status Messages-------------------");
fprintf(stderr,  "\nChecking system...OK .. Checking user..."); system("printenv USER");
fprintf(stderr,"[1] OS name : %s \t",machptr->sysname);
fprintf(stderr,"[2] Node name : %s \t",machptr->nodename);
fprintf(stderr,"[3] CPU type: %s \n",machptr->machine);
fprintf(stderr,"[4] Machine type : %s \t",machptr->m_type);
fprintf(stderr,"[5] OS Version : %s \t",machptr->release);
fprintf(stderr,"[6] OS Release : %s \n",machptr->version);
fprintf(stderr,"[7] Motif Version : %s ", XmVERSION_STRING);
fprintf(stderr,"[8] MultiSlice/PID : %d\n",getpid());
fprintf(stderr,"Launching application...");
system("date");
fprintf(stderr,"Load and select file(s) to process...\n");

    /* Create the main menu */ 
    create_main_menu(main_window);

 
    /* Create the drawing area 1 */
    argcount = 0;
    XtSetArg(args[argcount], XmNwidth ,  IMAGE_WIDTH); argcount++;
    XtSetArg(args[argcount], XmNheight, IMAGE_HEIGHT); argcount++;
    draw_1 = XmCreateDrawingArea(top_level, "draw_1", args, argcount);
/*    XtManageChild(draw_1); */
 
    /* Create the drawing area 2 */
    argcount = 0;
    XtSetArg(args[argcount], XmNwidth ,  IMAGE_WIDTH); argcount++;
    XtSetArg(args[argcount], XmNheight, IMAGE_HEIGHT); argcount++;
    draw_2 = XmCreateDrawingArea(top_level, "draw_2", args, argcount);
/*    XtManageChild(draw_2); */
 
    /* Create a watch cursor */
    theCursor = XCreateFontCursor(XtDisplay(main_window), XC_watch);

    /* Create the icon window for the application */
    app_icon = XCreateBitmapFromData(XtDisplay(top_level),
                                     DefaultRootWindow(XtDisplay(top_level)),
                                     icon_bits, icon_width, icon_height);
    argcount = 0;
    XtSetArg(args[argcount], XmNiconPixmap, app_icon); argcount++;
    XtSetValues(top_level, args, argcount);

  
    XtAppAddTimeOut(app,2*1000,update_time,NULL); 
 
    /* Realize all widgets */
    XtRealizeWidget(top_level);

    /* Make some initializations */
    setup_display (top_level);
    create_region_list(&region_list);


    /* Event handling loop--keep processing events until done */
    XtAppMainLoop(app);


}


XtCallbackProc handle_expose_y(Widget w, XtPointer client_data,
                     XmDrawingAreaCallbackStruct *call_data)
{
    int a,b;
    XEvent *theEvent = call_data->event;
    a = *((int *) client_data);
    XCopyArea(XtDisplay(w), thePixmap_y[a], XtWindow(w), arrayGC[a], 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 0, 0);   

}
void red_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
int a,b,c,d;
char *temp=".....................................................................................................";
b=yhs_files_open;
c=curfile;
strcpy(temp,yhs_filename1);
while (b > 0)
{
sq(b);
b--;
}
strcpy(yhs_filename1,temp);
curfile=c;
}

XtCallbackProc squish(Widget w, XtPointer client_data,
                     XmAnyCallbackStruct *call_data)
{
int a,b;
a=2;
b=(*((int *) client_data));
sq(b);
}

void sq(int b)
{
int a,c;
char *temp_sq="................................................................................................";
strcpy(temp_sq,yhs_filename1);
c=curfile;
a=2;
if(b==1 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file1);
XtManageChild(manage1); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==1 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file1); 
XtManageChild(manage1); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==2 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file2);
XtManageChild(manage2); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==2 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file2); 
XtManageChild(manage2); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==3 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file3);
XtManageChild(manage3); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==3 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file3); 
XtManageChild(manage3); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==4 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file4);
XtManageChild(manage4); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==4 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file4); 
XtManageChild(manage4); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==5 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file5);
XtManageChild(manage5); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==5 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file5); 
XtManageChild(manage5); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==6 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file6);
XtManageChild(manage6); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==6 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file6); 
XtManageChild(manage6); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==7 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file7);
XtManageChild(manage7); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==7 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file7); 
XtManageChild(manage7); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==8 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file8);
XtManageChild(manage8); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==8 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file8); 
XtManageChild(manage8); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==9 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file9);
XtManageChild(manage9); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==9 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file9); 
XtManageChild(manage9); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==10 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file10);
XtManageChild(manage10); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==10 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file10); 
XtManageChild(manage10); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==11 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file11);
XtManageChild(manage11); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==11 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file11); 
XtManageChild(manage11); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==12 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file12);
XtManageChild(manage12); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==12 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file12); 
XtManageChild(manage12); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==13 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file13);
XtManageChild(manage13); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==13 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file13); 
XtManageChild(manage13); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==14 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file14);
XtManageChild(manage14); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==14 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file14); 
XtManageChild(manage14); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==15 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file15);
XtManageChild(manage15); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==15 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file15); 
XtManageChild(manage15); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==16 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file16);
XtManageChild(manage16); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==16 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file16); 
XtManageChild(manage16); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==17 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file17);
XtManageChild(manage17); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==17 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file17); 
XtManageChild(manage17); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==18 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file18);
XtManageChild(manage18); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==18 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file18); 
XtManageChild(manage18); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==19 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file19);
XtManageChild(manage19); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==19 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file19); 
XtManageChild(manage19); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

if(b==20 && squash[b]<=0 && a==2){ curfile=b; a--; squash[b]++; 
strcpy(yhs_filename1,yhs_file20);
XtManageChild(manage20); 
XtManageChild(manager[b]); XtManageChild(view[b]);reduce_callback(NULL,NULL,NULL);}
if(b==20 && squash[b]>=1 && a==2){ curfile=b; a--; squash[b]--; 
strcpy(yhs_filename1,yhs_file20); 
XtManageChild(manage20); 
XtManageChild(manager[b]); XtManageChild(view[b]); expand_callback(NULL,NULL,NULL);}

curfile=c;
strcpy(yhs_filename1,temp_sq);

}

/*-------------------------------------------------------------*/
/*  h a n d l e _ e x p o s e _ 1                              */
/*                                                             */
/*  Expose event-handler for the drawing area 1                */
/*-------------------------------------------------------------*/
void handle_expose_1(Widget w, XtPointer client_data,
                     XmDrawingAreaCallbackStruct *call_data)
{
    XEvent *theEvent = call_data->event;

    if (file_not_loaded) return;

    if (theEvent->xexpose.count == 0)
    {
	/* Copy the exposed area from the pixmap to the window */
	XCopyArea(XtDisplay(w), thePixmap_1, XtWindow(w), image_gc_1,
		0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 0, 0);

	/* Redraw the polygons (regions) defined on input window */
	display_regions (region_list);
    }
}


/*-------------------------------------------------------------*/
/*  h a n d l e _ e x p o s e _ 2                              */
/*                                                             */
/*  Expose event-handler for the drawing area 2                */
/*-------------------------------------------------------------*/
void handle_expose_2(Widget w, XtPointer client_data,
                     XmDrawingAreaCallbackStruct *call_data)
{
    XEvent *theEvent = call_data->event;
  
    if (file_not_loaded) return;

    if (theEvent->xexpose.count == 0)
    {
       /* Copy the exposed area from the pixmap to the window */
       XCopyArea(XtDisplay(w), thePixmap_2, XtWindow(w), image_gc_2,
                 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 0, 0);
    }
}


/*-------------------------------------------------------------*/
/*  m o u s e _ t r a c k _ 1                                  */
/*                                                             */
/*  Mouse track event-handler for drawing area 1               */
/*-------------------------------------------------------------*/
void mouse_track_1(Widget w, XtPointer client_data, XEvent *event)
{
   int mouse_x, mouse_y;

   if (yhs_files_open==0) return;

   /* Get actual co-ordinates of the mouse */
   mouse_x = event->xmotion.x;
   mouse_y = event->xmotion.y; 

   /* Refresh the actual co-ordinates of the mouse on the screen */
/*   refresh_coord(mouse_x, mouse_y,
                 (int) XGetPixel(view[curfile], mouse_x, mouse_y)); */
}


/*--------------------------------------------------------------*/
/*  m o u s e _ t r a c k _ 2					*/
/*								*/
/*  Mouse track event-handler for drawing area 2		*/
/*--------------------------------------------------------------*/

void mouse_track_2(Widget w, XtPointer client_data, XEvent *event)
{
   int mouse_x, mouse_y;

   if (file_not_loaded) return;

   /* Get actual co-ordinates of the mouse */
   mouse_x = event->xmotion.x;
   mouse_y = event->xmotion.y;

   /* Refresh the actual co-ordinates of the mouse on the screen */
   refresh_coord(mouse_x, mouse_y,
                 (int) XGetPixel(theXImage_2, mouse_x, mouse_y)); 
}


/*--------------------------------------------------------------*/
/*  p r e p a r e _ h a n d l e r s				*/
/*								*/
/*  Prepare mouse input event-handlers according to the		*/
/*  operation the user want to perform				*/
/*--------------------------------------------------------------*/

void prepare_handlers (int sel)
{
    if (yhs_files_open == 0) return;

    switch (sel)
    {
	case REGION_GROWING :
		remove_event_handlers(right_button);
		remove_event_handlers(left_button);
		left_button=sel; 
		XtAddEventHandler(view[curfile], ButtonPressMask, False,
		    (XtEventHandler) start_growing, NULL);
		break;
	case REGION_CUT	:
/*		remove_event_handlers(left_button);
		left_button=sel; 
		XtAddEventHandler(draw_2, ButtonPressMask, False,
		    (XtEventHandler) start_tracking, NULL);
    	  	XtAddEventHandler(draw_2, Button3MotionMask, False,
		    (XtEventHandler) continue_tracking, NULL);
    	  	XtAddEventHandler(draw_2, ButtonReleaseMask, False,
		    (XtEventHandler) end_tracking, NULL);
		XtAddEventHandler(draw_2, ButtonReleaseMask, False,
		    (XtEventHandler) start_cut, NULL); */
		break;
	case TRACKING	: 
/*		remove_event_handlers(right_button);
		right_button=sel;
		XtAddEventHandler(draw_1, ButtonPressMask, False,
		    (XtEventHandler) start_tracking, NULL);
    	  	XtAddEventHandler(draw_1, Button3MotionMask, False,
		    (XtEventHandler) continue_tracking, NULL);
    	  	XtAddEventHandler(draw_1, ButtonReleaseMask, False,
		    (XtEventHandler) end_tracking, NULL); */
		break;
	case RECTANGLES	:
	case ELLIPSES	:
	case CIRCLES	:
		remove_event_handlers(right_button);
		right_button=sel;
		XtAddEventHandler(view[curfile], ButtonPressMask, False,
		    (XtEventHandler) start_rubberband, NULL);
           	XtAddEventHandler(view[curfile], Button3MotionMask, False,
		    (XtEventHandler) continue_rubberband, NULL);
           	XtAddEventHandler(view[curfile], ButtonReleaseMask, False,
		    (XtEventHandler) end_rubberband, NULL);
		break;
	case ZOOM	:
		remove_event_handlers(left_button);
		remove_event_handlers(right_button);
		right_button=sel; 
		XtAddEventHandler(view[curfile], ButtonPressMask, False,
		    (XtEventHandler) start_zoom, NULL);
		XtAddEventHandler(view[curfile], PointerMotionMask, False,
		    (XtEventHandler) move_zoom, NULL);
		XtAddCallback(view[curfile], XmNexposeCallback,
		    (XtCallbackProc) handle_zoom_expose, (XtPointer) NULL); 
		break; 

	default		: break;
     }
}


/*--------------------------------------------------------------*/
/*  r e m o v e _ e v e n t _ h a n d l e r s			*/
/*								*/
/*  Remove the set of handlers for used the previous operation 	*/
/*  performed by the user					*/
/*--------------------------------------------------------------*/

void remove_event_handlers(int sel)
{
   switch (sel)
   {
	case REGION_GROWING :
		XtRemoveEventHandler(view[curfile], XtAllEvents, False,
		    (XtEventHandler) start_growing, NULL);
		break;
	case REGION_CUT  :
		XtRemoveEventHandler(draw_2, XtAllEvents, False,
		    (XtEventHandler) start_tracking, NULL);
    	  	XtRemoveEventHandler(draw_2, XtAllEvents, False,
		    (XtEventHandler) continue_tracking, NULL);
    	  	XtRemoveEventHandler(draw_2, XtAllEvents, False,
		    (XtEventHandler) end_tracking, NULL);
		XtRemoveEventHandler(draw_2, XtAllEvents, False,
		    (XtEventHandler) start_cut, NULL);
		break;
	case TRACKING    :
		XtRemoveEventHandler(draw_1, XtAllEvents, False,
		    (XtEventHandler) start_tracking, NULL);
		XtRemoveEventHandler(draw_1, XtAllEvents, False,
		    (XtEventHandler) continue_tracking, NULL);
		XtRemoveEventHandler(draw_1, XtAllEvents, False,
		    (XtEventHandler) end_tracking, NULL);
		break;
       case RECTANGLES  : 
       case ELLIPSES    :
       case CIRCLES     :
		XtRemoveEventHandler (view[curfile], XtAllEvents, False,
		    (XtEventHandler) start_rubberband, NULL);
   		XtRemoveEventHandler (view[curfile], XtAllEvents, False,
		    (XtEventHandler) continue_rubberband, NULL);
		XtRemoveEventHandler (view[curfile], XtAllEvents, False,
		    (XtEventHandler) end_rubberband, NULL);
		break;
	case ZOOM	:
		XtRemoveEventHandler(view[curfile], XtAllEvents, False,
		    (XtEventHandler) start_zoom, NULL);
		XtRemoveEventHandler(view[curfile], XtAllEvents, False,
		    (XtEventHandler) move_zoom, NULL);
		XtRemoveCallback(view[curfile], XmNexposeCallback,
		    (XtCallbackProc) handle_zoom_expose, (XtPointer) NULL);
		break;
	default		: break;
   }
}



/*-------------------------------------------------------------*/
/*   s e t u p _ d i s p l a y                                 */
/*                                                             */
/*   Setup display. Selects a visual, creates the colormap     */
/*   and creates a GC for displaying images.                   */
/*-------------------------------------------------------------*/
void setup_display(Widget w)
{
    int i,j, cindex;
    int theScreen;
    XColor *colors = NULL;
    XGCValues     xgcv;
    Colormap      theColormap;
    XVisualInfo   *p_visinfo;
    XVisualInfo   *vis_list;
    XVisualInfo   vis_template;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */

    theScreen = DefaultScreen(XtDisplay(w));
    vis_template.screen = theScreen;
    vis_list = XGetVisualInfo(XtDisplay(w),VisualScreenMask, &vis_template, &j);
    for (i=0, p_visinfo = vis_list; i<j;i++, p_visinfo++)
    {
    if(p_visinfo->class == PseudoColor && p_visinfo->depth == 8)
    { theVisual = p_visinfo->visual;
      vis_depth = p_visinfo->depth;
      break;
    }
    }

    theVisual = DefaultVisual(XtDisplay(w),theScreen);
    vis_depth = 8;  
    /* Create the XColor entries for the colormap */
    if((colors = (XColor *)calloc(COLORMAP_SIZE, sizeof(XColor))) == NULL) {
	fprintf(stderr, "No memory for setting up colormap\n");
	exit(1);
    }

    /* Set up the color cells to greyscale */
    for(cindex = 0; cindex < COLORMAP_SIZE; cindex++) {
	colors[cindex].red   = CSCALE * cindex;
	colors[cindex].green = CSCALE * cindex;
	colors[cindex].blue  = CSCALE * cindex;
	colors[cindex].pixel = cindex;
	colors[cindex].flags = DoRed | DoGreen | DoBlue;
    }

    /* Create the colormap */
    theColormap = XCreateColormap(XtDisplay(top_level), RootWindow(XtDisplay(top_level), theScreen),
				 theVisual, AllocAll);

    /* Store the colors into the colormap */
    XStoreColors(XtDisplay(top_level), theColormap, colors, COLORMAP_SIZE); 

    /* Set the background and foreground colors before free(colors) */
    bg = colors[0].pixel;
    fg = colors[255].pixel; 

    ysmap=XCopyColormapAndFree(XtDisplay(top_level), theColormap);

    /* Now we can release the memory used by the colors   */
    /* because the X server already has this information. */
    free(colors);

    /* Set background, foreground, visual_depth and colormap for draw_1 */
    argcount = 0;
    XtSetArg(args[argcount], XmNforeground, fg         ); argcount++;
    XtSetArg(args[argcount], XmNbackground, bg         ); argcount++;
    XtSetArg(args[argcount], XmNdepth     , vis_depth  ); argcount++;
    XtSetArg(args[argcount], XmNcolormap  , theColormap); argcount++; 
    XtSetValues(draw_1, args, argcount);

    /* Set background, foreground, visual_depth and colormap for draw_2 */
    argcount = 0;
    XtSetArg(args[argcount], XmNforeground, fg         ); argcount++;
    XtSetArg(args[argcount], XmNbackground, bg         ); argcount++;
    XtSetArg(args[argcount], XmNdepth     , vis_depth  ); argcount++;
    XtSetArg(args[argcount], XmNcolormap  , theColormap); argcount++; 
    XtSetValues(draw_2, args, argcount);
    
    /* Set the WM_COLORMAP_WINDOWS property so that the Motif */ 
    /* window manager knows about the windows that have their */
    /* own colormap.                                          */
    if(theColormap != DefaultColormap(XtDisplay(top_level), theScreen)) {
	list_w = (Widget *) malloc(2*sizeof(Widget));
	list_w[0] = draw_1;
	list_w[1] = draw_2;
	XtSetWMColormapWindows(top_level, list_w, (Cardinal) 2);
    } 
    
    /* Define a GC for both drawing areas with these colors */
    xgcv.foreground = fg;
    xgcv.background = bg;
    image_gc_1 = XCreateGC(XtDisplay(w), XtWindow(draw_1),
				(GCForeground | GCBackground), &xgcv);
    image_gc_2 = XCreateGC(XtDisplay(w), XtWindow(draw_2),
				(GCForeground | GCBackground), &xgcv);

    /* Define a GC to be used in editing image operations*/
    xgcv.foreground = fg ^ bg;
    xgcv.background = bg;
    xgcv.function = GXxor;
    xorGC = XtGetGC(draw_1, GCForeground | GCBackground | GCFunction, &xgcv);

    xgcv.foreground = fg;
    xgcv.background = bg;
    theGC = XtGetGC(draw_1, GCForeground | GCBackground, &xgcv);
}
