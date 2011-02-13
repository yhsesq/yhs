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
/*     File: open.c                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"
#include "log.h"

#include <X11/Xlib.h>
#include <X11/Xatom.h>
#include <X11/Xutil.h>
#include <X11/cursorfont.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#include <Xm/Xm.h>
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

/* Extern variables */
extern XtAppContext  app;
extern GC            image_gc_1;
extern GC            image_gc_2;
extern int	     running;
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
extern long          fg, bg;
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
extern Pixmap        cov;
extern Widget        flasher_button,menu_bar;
extern XImage        *theXImage_y[21];
extern int           curfile,resize;
int                  savefile;
extern Widget        manage1,manage2,manage3,manage4,manage5,manage6,manage7,manage8,manage9,manage10;
extern Widget        manage11,manage12,manage13,manage14,manage15,manage16,manage17,manage18,manage19,manage20;

char	*file_name = "File not loaded";

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

/* Local variables */
static Widget open_dialog = (Widget) NULL;
static Widget view_dialog = (Widget) NULL;
static XGCValues     xgcv;
static unsigned char *image_1 = NULL;
static unsigned char *image_2 = NULL;
static unsigned char *image_y = NULL;

Atom ATOM_WM_COLMAP_WIN;

extern Widget manager[21];
Widget da,db,dc,dd,de,df,dg,dh,di,dj,dk,dl;
Widget dm,dn,dq,dr,ds,dt,du,dv,l,m,n,o;
unsigned long fgy,bgy;
int once=0;

/* Function prototypes */
extern XtCallbackProc handle_expose_y(Widget w, XtPointer client_data,
			XmDrawingAreaCallbackStruct *call_data);
extern XtCallbackProc squish(Widget w, XtPointer client_data,
			XmAnyCallbackStruct *call_data);
void sbox_set(Widget d, XtPointer client_data,XmToggleButtonCallbackStruct *call_data);
void pbox_cb(Widget m, XtPointer client_data, XmSelectionBoxCallbackStruct *call_data);
void update_time();
void create_open_dialog(Widget parent);
void create_view_dialog(Widget parent);
void activate_open_dialog(Widget w, XtPointer client_data, 
                          XmAnyCallbackStruct *call_data);
void activate_view_dialog(Widget w, XtPointer client_data,
                          XmAnyCallbackStruct *call_data);
void deactivate_open_dialog(void);
void deactivate_view_dialog(void);
extern void mouse_track_1(Widget w, XtPointer client_data, XEvent *event);
static void fs_ok(Widget w, XtPointer client_data,
                  XmSelectionBoxCallbackStruct *call_data);
static void fs_cancel(Widget w, XtPointer client_data,
                      XmSelectionBoxCallbackStruct *call_data);

static void fv_ok(Widget w, XtPointer client_data,
                  XmSelectionBoxCallbackStruct *call_data);
static void fv_cancel(Widget w, XtPointer client_data,
                      XmSelectionBoxCallbackStruct *call_data);

void load_file(char *filename, Widget w);

static void setup_ximage();

void close_me_callback(Widget w, XtPointer client_data,
                         XmDrawingAreaCallbackStruct *call_data);
void multi_window_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void multi_merge_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void inverse_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void reduce_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
extern void red_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void expand_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void cascade_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void tile_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
void operate_callback(Widget w, XtPointer client_data,
                         XmAnyCallbackStruct *call_data);
extern void sq();
void resize_callback();
void reduction();
void delreload();
void deletestring();

/* Function definition */
void create_open_dialog(Widget parent) {

  XmString  title;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  title = XmStringLtoRCreate("Load....", XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNfilterLabelString, title);    argcount++; 
  open_dialog = XmCreateFileSelectionDialog(temp_widget, "open dialog",
                                            args, argcount);

  /* Remove the help button from the file selection dialog box */
  temp_widget = XmFileSelectionBoxGetChild(open_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add callbacks for the "OK" and "Cancel" buttons */
  XtAddCallback(open_dialog, XmNokCallback,
                (XtCallbackProc) fs_ok, (XtPointer) NULL);
  XtAddCallback(open_dialog, XmNcancelCallback,
                (XtCallbackProc) fs_cancel, (XtPointer) NULL);

  XmStringFree(title);
}

/* Function definition */
void create_view_dialog(Widget parent) {

  XmString  title;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

if (yhs_files_open < 20)
{
  title = XmStringLtoRCreate("Load....", XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNfilterLabelString, title);    argcount++; 
  view_dialog = XmCreateFileSelectionDialog(temp_widget, "view dialog",
                                            args, argcount);

  /* Remove the help button from the file selection dialog box */
  temp_widget = XmFileSelectionBoxGetChild(view_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add callbacks for the "OK" and "Cancel" buttons, note that fs_ok and fv_ok are different. */
  XtAddCallback(view_dialog, XmNokCallback,
                (XtCallbackProc) fv_ok, (XtPointer) NULL);
  XtAddCallback(view_dialog, XmNcancelCallback,
                (XtCallbackProc) fv_cancel, (XtPointer) NULL);

  XmStringFree(title);
}
}

void activate_open_dialog(Widget w, XtPointer client_data,
                          XmAnyCallbackStruct *call_data) {

XmString dummy;
if (yhs_files_open < 11)
{
  selection = OPEN_FILE;
  action = SELECT;
  refresh_action();
  dummy = XmStringCreate("*.study", XmSTRING_DEFAULT_CHARSET ); /* .study later */
  XmFileSelectionDoSearch(open_dialog, dummy); 
  XmStringFree(dummy);
  XtManageChild(open_dialog);
}
}

void deactivate_open_dialog(void) {
  XtUnmanageChild(open_dialog);
}

void activate_view_dialog(Widget w, XtPointer client_data,
                          XmAnyCallbackStruct *call_data) {
  XmString dummy;
if (yhs_files_open < 20)
{
  selection = OPEN_FILE;
  action = SELECT;
  refresh_action();
  dummy = XmStringCreate("*.raw", XmSTRING_DEFAULT_CHARSET );
  XmFileSelectionDoSearch(view_dialog, dummy); 
  XmStringFree(dummy);
  XtManageChild(view_dialog);
}
}

void deactivate_view_dialog(void) {
  XtUnmanageChild(view_dialog);
}

/*-------------------------------------------------------------*/
/*  f s _ o k                                                  */
/*                                                             */
/*  Callback for the "OK" button on the file selection box     */
/*-------------------------------------------------------------*/
static void fs_ok(Widget w, XtPointer client_data,
                  XmSelectionBoxCallbackStruct *call_data) {

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
    int i,a,b;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */ 
   FILE *cfil;
    char tempfile[255];
int tempo=0;
   /* Get file name from user's selection */
   if(XmStringGetLtoR(call_data->value, 
                      XmSTRING_DEFAULT_CHARSET, &file_name))
   {
if ((cfil=fopen(file_name,"r"))==NULL) {fprintf(stderr,"Cannot open case study");exit(1);}
fgets(file_name,254,cfil);
strncpy(tempfile,file_name,(strlen(file_name)-1));
tempfile[strlen(file_name)-1]='\0';
tempfile[strlen(file_name)]='\0';
strncpy(file_name,tempfile,strlen(file_name));
for (tempo=1; tempo < 11; tempo++)
{ 
    #include "file_viewer.c"
sq(yhs_files_open);
fgets(file_name,254,cfil);
strncpy(tempfile,file_name,(strlen(file_name)-1));
tempfile[strlen(file_name)-1]='\0';
tempfile[strlen(file_name)]='\0';
strncpy(file_name,tempfile,strlen(file_name));
}
fclose(cfil);
   }
   else
   {
     printf("Nothing selected\n");
   }
   deactivate_open_dialog();
}

/*-------------------------------------------------------------*/
/*  f s _ c a n c e l                                          */
/*                                                             */
/*  Callback for the "Cancel" button in the file selection box */
/*-------------------------------------------------------------*/
static void fs_cancel(Widget w, XtPointer client_data,
                      XmSelectionBoxCallbackStruct *call_data) {
  deactivate_open_dialog();
}

void update_time(w)
Widget w;
{   Arg al[10];
    int ac;
    Display *display;
    Display *d2;
    Screen  *screen;
    FILE *pfile;
    char *file_name="File_not_loaded.......................................................................";
    Widget view_image;
    Window root;
    Window window;
    Window new_win;
    unsigned int image_bytes;
    int i,a,b;
    int cindex;
    char addc,ccd='A';
    int theScreen;
    Visual *new_visual;
    static unsigned char *image_y = NULL;
    static XGCValues xgcv;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */
    if (time > 30000)
    { fprintf(stderr,"\n\n Automatic Timeout : System executing for 8.23 HOURS.\n Probable lockout. Shutting system down...%d",time);
     exit(1); /* try normal shutdown */
      fprintf(stderr,"\n Unable to close normally...trying other methods...");
     close(); /* not yet closed ? shut file stream down */
     XtUnmanageChild(top_level); /* shut main window down */
     exit(0);  /* try AGAIN -- really bad lockout */
     sleep(20); /* wait for system to purge if system is locked */
     exit(-1);  /* try a shutdown again */
     time=30001; /* lock system timer & repeat every second until shutdown occurs */
    }
    if(running==0)
{
if(curfile==0) {curfile=1;}
    d2=XtDisplay(menu_bar); 
      cov = XCreatePixmapFromBitmapData(d2,
                       DefaultRootWindow(d2),
                       log_bits, log_width, log_height,
                       (long) (BlackPixel(d2,DefaultScreen(d2))-(long) random()),
                       WhitePixel(d2,DefaultScreen(d2)),
                       DefaultDepth(d2,DefaultScreen(d2)));
    argcount = 0;
    XtSetArg(args[argcount], XmNlabelPixmap, cov); argcount++;
    XtSetValues(flasher_button,args,argcount);
    argcount=0;
 
    if(running==0){
    if (file_loader==1){
    if (run_once==0) {
    strcpy(file_name,file_yhs);
    #include "file_viewer.c" 
    run_once=1;     
    if(resize==1){resize=0; i=curfile; curfile=yhs_files_open; resize_callback(); curfile=i; } }
    file_loader=0;  
    fprintf(stderr,".");
    }
if( (int) strlen(tempfilenew) > 23)
{ 
if(addcharac[0]!=ccd)
{
      strcpy(tempfileold,"slicetempyhsT");
      strcpy(tempfilenew,"slicetempyhsT");
      ccd=addcharac[0];
      addc=ccd-1;
      addcharac[0]=addc; 
      addcharac[1]='\0';
      strcpy(tempfileold,tempfilenew); /* copy the new filename to the old file i.e. save the old file */
      strcat(tempfilenew,addcharac); /* new free temp file */
      fprintf(stderr,"\nChanged temporary file extensions..to %s.\n",addcharac);
}}}}

    time++;
    XtAppAddTimeOut(app,1*1000,(XtTimerCallbackProc)update_time,NULL); 
}

/*-------------------------------------------------------------*/
/*  f v _ o k                                                  */
/*                                                             */
/*  Callback for the "OK" button on the view selection box     */
/*-------------------------------------------------------------*/
static void fv_ok(Widget w, XtPointer client_data,
                  XmSelectionBoxCallbackStruct *call_data)
 {
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
    int i,a,b;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */


   /* Get file name from user's selection */
   if(XmStringGetLtoR(call_data->value, 
                      XmSTRING_DEFAULT_CHARSET, &file_name))
{
    #include "file_viewer.c"
    }
   else
   {
     printf("Nothing selected\n");
   }
   deactivate_view_dialog();
}

void operate_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
int ac=0;
Arg al[10];
char *big_label="File Number [";
savefile=curfile;
ac=0;
argcount=0;
if (once==0)
{
l=XmCreateBulletinBoardDialog(top_level,"File_Select",al,ac);
XtSetArg(args[argcount],XmNorientation,XmVERTICAL);argcount++; 
o=XmCreateRowColumn(l,"File Selector",args,argcount);
XtManageChild(o);
argcount=0;
XtSetArg(args[argcount],XmNmessageString,XmStringCreateSimple("Enter file number(s) you want to operate on [MAX 10] :-"));argcount++;
n=XmCreateMessageBox(o,"File_Selector",args,argcount);
XtManageChild(n);
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
(void) XtVaCreateManagedWidget("Currently loaded files are :-",xmLabelWidgetClass,o,NULL);
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "1] : "); strcat(big_label,yhs_file1); da=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL); 
XtAddCallback(da,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)one);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "2] : "); strcat(big_label,yhs_file2); db=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(db,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)two);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "3] : "); strcat(big_label,yhs_file3); dc=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dc,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)three);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "4] : "); strcat(big_label,yhs_file4); dd=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dd,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)four);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "5] : "); strcat(big_label,yhs_file5); de=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(de,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)five);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "6] : "); strcat(big_label,yhs_file6); df=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(df,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)six);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "7] : "); strcat(big_label,yhs_file7); dg=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dg,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)seven);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "8] : "); strcat(big_label,yhs_file8); dh=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dh,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)eight);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "9] : "); strcat(big_label,yhs_file9); di=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(di,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)nine);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "10] : "); strcat(big_label,yhs_file10); dj=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dj,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)ten);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "11] : "); strcat(big_label,yhs_file11); dk=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dk,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)eleven);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "12] : "); strcat(big_label,yhs_file12); dl=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dl,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)twelve);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "13] : "); strcat(big_label,yhs_file13); dm=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dm,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)thirteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "14] : "); strcat(big_label,yhs_file14); dn=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dn,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)fourteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "15] : "); strcat(big_label,yhs_file15); du=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(du,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)fifteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "16] : "); strcat(big_label,yhs_file16); dv=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dv,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)sixteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "17] : "); strcat(big_label,yhs_file17); dq=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dq,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)seventeen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "18] : "); strcat(big_label,yhs_file18); dr=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dr,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)eighteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "19] : "); strcat(big_label,yhs_file19); ds=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(ds,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)nineteen);
strcpy(big_label,"File Number [");
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
strcat(big_label, "20] : "); strcat(big_label,yhs_file20); dt=XtVaCreateManagedWidget(big_label,xmToggleButtonWidgetClass,o,
XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET),NULL);
XtAddCallback(dt,XmNvalueChangedCallback,(XtCallbackProc)sbox_set,(XtPointer)twenty);
(void) XtVaCreateManagedWidget("Separator",xmSeparatorWidgetClass,o,NULL);
XtAddCallback(n,XmNcancelCallback,(XtCallbackProc) pbox_cb,(XtPointer) NULL);
XtAddCallback(n,XmNokCallback,(XtCallbackProc) pbox_cb,(XtPointer) NULL);
XtAddCallback(n,XmNhelpCallback, (XtCallbackProc) pbox_cb,(XtPointer) NULL);
once=1;
XtManageChild(l);
}
else {
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "1] : "); strcat(big_label,yhs_file1);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(da,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "2] : "); strcat(big_label,yhs_file2);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(db,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "3] : "); strcat(big_label,yhs_file3);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dc,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "4] : "); strcat(big_label,yhs_file4);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dd,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "5] : "); strcat(big_label,yhs_file5);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(de,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "6] : "); strcat(big_label,yhs_file6);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(df,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "7] : "); strcat(big_label,yhs_file7);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dg,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "8] : "); strcat(big_label,yhs_file8);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dh,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "9] : "); strcat(big_label,yhs_file9);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(di,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "10] : "); strcat(big_label,yhs_file10);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dj,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "11] : "); strcat(big_label,yhs_file11);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dk,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "12] : "); strcat(big_label,yhs_file12);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dl,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "13] : "); strcat(big_label,yhs_file13);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dm,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "14] : "); strcat(big_label,yhs_file14);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dn,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "15] : "); strcat(big_label,yhs_file15);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(du,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "16] : "); strcat(big_label,yhs_file16);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dv,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "17] : "); strcat(big_label,yhs_file17);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dq,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "18] : "); strcat(big_label,yhs_file18);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dr,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "19] : "); strcat(big_label,yhs_file19);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(ds,al,ac); 
strcpy(big_label,"File Number ["); ac=0; strcat(big_label, "20] : "); strcat(big_label,yhs_file20);
XtSetArg(al[ac],XmNlabelString, XmStringCreateLtoR(big_label,XmSTRING_DEFAULT_CHARSET));ac++;
XtSetValues(dt,al,ac); 
XtManageChild(l);}
}

void pbox_cb(Widget m,XtPointer client_data,XmSelectionBoxCallbackStruct *call_data)
{
int temp;
   switch(call_data->reason)
{   case XmCR_OK:
           for(temp=1;temp < 11;temp++){
           if(yhs_filename[temp]==1 && temp==1){ yhs_filename1=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==1){ yhs_filename1=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==1){ yhs_filename1=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==1){ yhs_filename1=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==1){ yhs_filename1=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==1){ yhs_filename1=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==1){ yhs_filename1=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==1){ yhs_filename1=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==1){ yhs_filename1=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==1){ yhs_filename1=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==1){ yhs_filename1=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==1){ yhs_filename1=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==1){ yhs_filename1=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==1){ yhs_filename1=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==1){ yhs_filename1=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==1){ yhs_filename1=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==1){ yhs_filename1=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==1){ yhs_filename1=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==1){ yhs_filename1=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==1){ yhs_filename1=yhs_file20; }

           if(yhs_filename[temp]==1 && temp==2){ yhs_filename2=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==2){ yhs_filename2=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==2){ yhs_filename2=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==2){ yhs_filename2=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==2){ yhs_filename2=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==2){ yhs_filename2=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==2){ yhs_filename2=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==2){ yhs_filename2=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==2){ yhs_filename2=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==2){ yhs_filename2=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==2){ yhs_filename2=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==2){ yhs_filename2=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==2){ yhs_filename2=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==2){ yhs_filename2=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==2){ yhs_filename2=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==2){ yhs_filename2=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==2){ yhs_filename2=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==2){ yhs_filename2=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==2){ yhs_filename2=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==2){ yhs_filename2=yhs_file20; }

           if(yhs_filename[temp]==1 && temp==3){ yhs_filename3=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==3){ yhs_filename3=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==3){ yhs_filename3=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==3){ yhs_filename3=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==3){ yhs_filename3=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==3){ yhs_filename3=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==3){ yhs_filename3=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==3){ yhs_filename3=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==3){ yhs_filename3=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==3){ yhs_filename3=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==3){ yhs_filename3=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==3){ yhs_filename3=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==3){ yhs_filename3=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==3){ yhs_filename3=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==3){ yhs_filename3=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==3){ yhs_filename3=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==3){ yhs_filename3=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==3){ yhs_filename3=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==3){ yhs_filename3=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==3){ yhs_filename3=yhs_file20; }

           if(yhs_filename[temp]==1 && temp==4){ yhs_filename4=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==4){ yhs_filename4=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==4){ yhs_filename4=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==4){ yhs_filename4=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==4){ yhs_filename4=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==4){ yhs_filename4=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==4){ yhs_filename4=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==4){ yhs_filename4=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==4){ yhs_filename4=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==4){ yhs_filename4=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==4){ yhs_filename4=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==4){ yhs_filename4=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==4){ yhs_filename4=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==4){ yhs_filename4=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==4){ yhs_filename4=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==4){ yhs_filename4=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==4){ yhs_filename4=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==4){ yhs_filename4=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==4){ yhs_filename4=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==4){ yhs_filename4=yhs_file20; }

           if(yhs_filename[temp]==1 && temp==5){ yhs_filename5=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==5){ yhs_filename5=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==5){ yhs_filename5=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==5){ yhs_filename5=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==5){ yhs_filename5=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==5){ yhs_filename5=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==5){ yhs_filename5=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==5){ yhs_filename5=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==5){ yhs_filename5=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==5){ yhs_filename5=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==5){ yhs_filename5=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==5){ yhs_filename5=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==5){ yhs_filename5=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==5){ yhs_filename5=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==5){ yhs_filename5=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==5){ yhs_filename5=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==5){ yhs_filename5=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==5){ yhs_filename5=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==5){ yhs_filename5=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==5){ yhs_filename5=yhs_file20; }
 
           if(yhs_filename[temp]==1 && temp==6){ yhs_filename6=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==6){ yhs_filename6=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==6){ yhs_filename6=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==6){ yhs_filename6=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==6){ yhs_filename6=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==6){ yhs_filename6=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==6){ yhs_filename6=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==6){ yhs_filename6=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==6){ yhs_filename6=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==6){ yhs_filename6=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==6){ yhs_filename6=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==6){ yhs_filename6=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==6){ yhs_filename6=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==6){ yhs_filename6=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==6){ yhs_filename6=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==6){ yhs_filename6=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==6){ yhs_filename6=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==6){ yhs_filename6=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==6){ yhs_filename6=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==6){ yhs_filename6=yhs_file20; }
 
           if(yhs_filename[temp]==1 && temp==7){ yhs_filename7=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==7){ yhs_filename7=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==7){ yhs_filename7=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==7){ yhs_filename7=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==7){ yhs_filename7=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==7){ yhs_filename7=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==7){ yhs_filename7=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==7){ yhs_filename7=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==7){ yhs_filename7=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==7){ yhs_filename7=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==7){ yhs_filename7=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==7){ yhs_filename7=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==7){ yhs_filename7=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==7){ yhs_filename7=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==7){ yhs_filename7=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==7){ yhs_filename7=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==7){ yhs_filename7=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==7){ yhs_filename7=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==7){ yhs_filename7=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==7){ yhs_filename7=yhs_file20; }
   
           if(yhs_filename[temp]==1 && temp==8){ yhs_filename8=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==8){ yhs_filename8=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==8){ yhs_filename8=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==8){ yhs_filename8=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==8){ yhs_filename8=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==8){ yhs_filename8=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==8){ yhs_filename8=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==8){ yhs_filename8=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==8){ yhs_filename8=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==8){ yhs_filename8=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==8){ yhs_filename8=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==8){ yhs_filename8=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==8){ yhs_filename8=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==8){ yhs_filename8=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==8){ yhs_filename8=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==8){ yhs_filename8=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==8){ yhs_filename8=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==8){ yhs_filename8=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==8){ yhs_filename8=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==8){ yhs_filename8=yhs_file20; }
 
           if(yhs_filename[temp]==1 && temp==9){ yhs_filename9=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==9){ yhs_filename9=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==9){ yhs_filename9=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==9){ yhs_filename9=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==9){ yhs_filename9=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==9){ yhs_filename9=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==9){ yhs_filename9=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==9){ yhs_filename9=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==9){ yhs_filename9=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==9){ yhs_filename9=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==9){ yhs_filename9=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==9){ yhs_filename9=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==9){ yhs_filename9=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==9){ yhs_filename9=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==9){ yhs_filename9=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==9){ yhs_filename9=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==9){ yhs_filename9=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==9){ yhs_filename9=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==9){ yhs_filename9=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==9){ yhs_filename9=yhs_file20; }
 
           if(yhs_filename[temp]==1 && temp==10){ yhs_filename10=yhs_file1; } 
           if(yhs_filename[temp]==2 && temp==10){ yhs_filename10=yhs_file2; }                                
           if(yhs_filename[temp]==3 && temp==10){ yhs_filename10=yhs_file3; } 
           if(yhs_filename[temp]==4 && temp==10){ yhs_filename10=yhs_file4; }                                
           if(yhs_filename[temp]==5 && temp==10){ yhs_filename10=yhs_file5; } 
           if(yhs_filename[temp]==6 && temp==10){ yhs_filename10=yhs_file6; }                                
           if(yhs_filename[temp]==7 && temp==10){ yhs_filename10=yhs_file7; } 
           if(yhs_filename[temp]==8 && temp==10){ yhs_filename10=yhs_file8; }                                
           if(yhs_filename[temp]==9 && temp==10){ yhs_filename10=yhs_file9; } 
           if(yhs_filename[temp]==10 && temp==10){ yhs_filename10=yhs_file10; }                                
           if(yhs_filename[temp]==11 && temp==10){ yhs_filename10=yhs_file11; } 
           if(yhs_filename[temp]==12 && temp==10){ yhs_filename10=yhs_file12; }                                
           if(yhs_filename[temp]==13 && temp==10){ yhs_filename10=yhs_file13; } 
           if(yhs_filename[temp]==14 && temp==10){ yhs_filename10=yhs_file14; }                                
           if(yhs_filename[temp]==15 && temp==10){ yhs_filename10=yhs_file15; } 
           if(yhs_filename[temp]==16 && temp==10){ yhs_filename10=yhs_file16; }                                
           if(yhs_filename[temp]==17 && temp==10){ yhs_filename10=yhs_file17; } 
           if(yhs_filename[temp]==18 && temp==10){ yhs_filename10=yhs_file18; }                                
           if(yhs_filename[temp]==19 && temp==10){ yhs_filename10=yhs_file19; } 
           if(yhs_filename[temp]==20 && temp==10){ yhs_filename10=yhs_file20; }

                                       }
           curfile=yhs_filename[1]; /* first entry is the current file */
           XtUnmanageChild(l);
           break;
    case XmCR_CANCEL:
           XtUnmanageChild(l);
           break;
    case XmCR_HELP:
           XtUnmanageChild(l);
           system("wwwhelpmoi &");
           break;
}
}

void sbox_set(Widget d, XtPointer client_data, XmToggleButtonCallbackStruct *call_data)
{
int temp=0;
if(((int) call_data->set) == 1 && yhs_filename[0] < 10 && ((int) client_data) < yhs_files_open+1){ yhs_filename[0]++;
yhs_filename[(yhs_filename[0])]=((int) client_data); }
else { 
argcount=0;
XtSetArg(args[argcount],XmNset,False);argcount++;
if ((int) client_data == 1) XtSetValues(da,args,argcount);
if ((int) client_data == 2) XtSetValues(db,args,argcount);
if ((int) client_data == 3) XtSetValues(dc,args,argcount);
if ((int) client_data == 4) XtSetValues(dd,args,argcount);
if ((int) client_data == 5) XtSetValues(de,args,argcount);
if ((int) client_data == 6) XtSetValues(df,args,argcount);
if ((int) client_data == 7) XtSetValues(dg,args,argcount);
if ((int) client_data == 8) XtSetValues(dh,args,argcount);
if ((int) client_data == 9) XtSetValues(di,args,argcount);
if ((int) client_data == 10) XtSetValues(dj,args,argcount);
if ((int) client_data == 11) XtSetValues(dk,args,argcount);
if ((int) client_data == 12) XtSetValues(dl,args,argcount);
if ((int) client_data == 13) XtSetValues(dm,args,argcount);
if ((int) client_data == 14) XtSetValues(dn,args,argcount);
if ((int) client_data == 15) XtSetValues(du,args,argcount);
if ((int) client_data == 16) XtSetValues(dv,args,argcount);
if ((int) client_data == 17) XtSetValues(dq,args,argcount);
if ((int) client_data == 18) XtSetValues(dr,args,argcount);
if ((int) client_data == 19) XtSetValues(ds,args,argcount);
if ((int) client_data == 20) XtSetValues(dt,args,argcount);
}
if(((int) call_data->set) == 0 && ((int) client_data) < yhs_files_open+1){
for(temp=1;temp<yhs_filename[0]+1;temp++)
{ if(((int) client_data) == yhs_filename[temp]){yhs_filename[temp]=yhs_filename[yhs_filename[0]];
                                            yhs_filename[0]--; yhs_filename[yhs_filename[0]+1]=0; } }
}
}

void cascade_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
if (yhs_files_open > 0) { XMoveWindow(XtDisplay(manage1),XtWindow(manage1),50,150); }
if (yhs_files_open > 1) { XMoveWindow(XtDisplay(manage2),XtWindow(manage2),70,170);  }
if (yhs_files_open > 2) { XMoveWindow(XtDisplay(manage3),XtWindow(manage3),90,190);  }
if (yhs_files_open > 3) { XMoveWindow(XtDisplay(manage4),XtWindow(manage4),110,210);  }
if (yhs_files_open > 4) { XMoveWindow(XtDisplay(manage5),XtWindow(manage5),130,230);  }
if (yhs_files_open > 5) { XMoveWindow(XtDisplay(manage6),XtWindow(manage6),150,250);  }
if (yhs_files_open > 6) { XMoveWindow(XtDisplay(manage7),XtWindow(manage7),170,270);  }
if (yhs_files_open > 7) { XMoveWindow(XtDisplay(manage8),XtWindow(manage8),200,300);  }
if (yhs_files_open > 8) { XMoveWindow(XtDisplay(manage9),XtWindow(manage9),220,320);  }
if (yhs_files_open > 9) { XMoveWindow(XtDisplay(manage10),XtWindow(manage10),240,340);  }
if (yhs_files_open > 10) { XMoveWindow(XtDisplay(manage11),XtWindow(manage11),260,360);  }
if (yhs_files_open > 11) { XMoveWindow(XtDisplay(manage12),XtWindow(manage12),280,380);  }
if (yhs_files_open > 12) { XMoveWindow(XtDisplay(manage13),XtWindow(manage13),300,400);  }
if (yhs_files_open > 13) { XMoveWindow(XtDisplay(manage14),XtWindow(manage14),320,420);  }
if (yhs_files_open > 14) { XMoveWindow(XtDisplay(manage15),XtWindow(manage15),340,440);  }
if (yhs_files_open > 15) { XMoveWindow(XtDisplay(manage16),XtWindow(manage16),360,460);  }
if (yhs_files_open > 16) { XMoveWindow(XtDisplay(manage17),XtWindow(manage17),380,480);  }
if (yhs_files_open > 17) { XMoveWindow(XtDisplay(manage18),XtWindow(manage18),400,500);  }
if (yhs_files_open > 18) { XMoveWindow(XtDisplay(manage19),XtWindow(manage19),420,520);  }
if (yhs_files_open > 19) { XMoveWindow(XtDisplay(manage20),XtWindow(manage20),440,540);  }
}

void resize_callback()
{
if (curfile==1){ XResizeWindow(XtDisplay(manage1),XtWindow(manage1),128,128);}
if (curfile==2){ XResizeWindow(XtDisplay(manage2),XtWindow(manage2),128,128);}
if (curfile==3){ XResizeWindow(XtDisplay(manage3),XtWindow(manage3),128,128);}
if (curfile==4){ XResizeWindow(XtDisplay(manage4),XtWindow(manage4),128,128);}
if (curfile==5){ XResizeWindow(XtDisplay(manage5),XtWindow(manage5),128,128);}
if (curfile==6){ XResizeWindow(XtDisplay(manage6),XtWindow(manage6),128,128);}
if (curfile==7){ XResizeWindow(XtDisplay(manage7),XtWindow(manage7),128,128);}
if (curfile==8){ XResizeWindow(XtDisplay(manage8),XtWindow(manage8),128,128);}
if (curfile==9){ XResizeWindow(XtDisplay(manage9),XtWindow(manage9),128,128);}
if (curfile==10){ XResizeWindow(XtDisplay(manage10),XtWindow(manage10),128,128);}
if (curfile==11){ XResizeWindow(XtDisplay(manage11),XtWindow(manage11),128,128);}
if (curfile==12){ XResizeWindow(XtDisplay(manage12),XtWindow(manage12),128,128);}
if (curfile==13){ XResizeWindow(XtDisplay(manage13),XtWindow(manage13),128,128);}
if (curfile==14){ XResizeWindow(XtDisplay(manage14),XtWindow(manage14),128,128);}
if (curfile==15){ XResizeWindow(XtDisplay(manage15),XtWindow(manage15),128,128);}
if (curfile==16){ XResizeWindow(XtDisplay(manage16),XtWindow(manage16),128,128);}
if (curfile==17){ XResizeWindow(XtDisplay(manage17),XtWindow(manage17),128,128);}
if (curfile==18){ XResizeWindow(XtDisplay(manage18),XtWindow(manage18),128,128);}
if (curfile==19){ XResizeWindow(XtDisplay(manage19),XtWindow(manage19),128,128);}
if (curfile==20){ XResizeWindow(XtDisplay(manage20),XtWindow(manage20),128,128);}
}

void deletestring()
{

if(yhs_files_open == 20){ yhs_filename1=yhs_file20; 
/* wipeout yhs_file.curfile */ 
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1); }
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1); }
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1); }
if(curfile == 16) { strcpy(yhs_file16,yhs_filename1); } 
if(curfile == 17) { strcpy(yhs_file17,yhs_filename1); }
if(curfile == 18) { strcpy(yhs_file18,yhs_filename1); }
if(curfile == 19) { strcpy(yhs_file19,yhs_filename1); }
if(curfile == 20) { strcpy(yhs_file20,yhs_filename1); }
strcpy(yhs_file20," -- Deleted file -- ");
} /* swap current filename */

if(yhs_files_open == 19){ yhs_filename1=yhs_file19; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1);}
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1);}
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1);}
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1);}
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1);}
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1);}
if(curfile == 16) { strcpy(yhs_file16,yhs_filename1);} 
if(curfile == 17) { strcpy(yhs_file17,yhs_filename1);}
if(curfile == 18) { strcpy(yhs_file18,yhs_filename1);}
if(curfile == 19) { strcpy(yhs_file19,yhs_filename1);}
strcpy(yhs_file19," -- Deleted file -- ");
}

if(yhs_files_open == 18){ yhs_filename1=yhs_file18; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1);}
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1);}
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1);}
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1);}
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1);}
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1);}
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1);}
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1);}
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1);}
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1);}
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1);}
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1);}
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1);}
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1);}
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1);}
if(curfile == 16) { strcpy(yhs_file16,yhs_filename1);} 
if(curfile == 17) { strcpy(yhs_file17,yhs_filename1);}
if(curfile == 18) { strcpy(yhs_file18,yhs_filename1);}
strcpy(yhs_file18," -- Deleted file -- ");
}

if(yhs_files_open == 17){ yhs_filename1=yhs_file17; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1);}
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1);}
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1);}
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1);}
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1);}
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1);}
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1);}
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1);}
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1);}
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1); }
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1); }
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1); }
if(curfile == 16) { strcpy(yhs_file16,yhs_filename1); } 
if(curfile == 17) { strcpy(yhs_file17,yhs_filename1); }
strcpy(yhs_file17," -- Deleted file -- "); }

if(yhs_files_open == 16){ yhs_filename1=yhs_file16; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1); }
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1); }
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1); }
if(curfile == 16) { strcpy(yhs_file16,yhs_filename1); }
strcpy(yhs_file16," -- Deleted file -- "); }

if(yhs_files_open == 15){ yhs_filename1=yhs_file15; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1);}
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1);}
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1);}
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1);}
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1);}
if(curfile == 15) { strcpy(yhs_file15,yhs_filename1);}
strcpy(yhs_file15," -- Deleted file -- "); }

if(yhs_files_open == 14){ yhs_filename1=yhs_file14; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1); }
if(curfile == 14) { strcpy(yhs_file14,yhs_filename1); }
strcpy(yhs_file14," -- Deleted file -- "); }

if(yhs_files_open == 13){ yhs_filename1=yhs_file13; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
if(curfile == 13) { strcpy(yhs_file13,yhs_filename1); }
strcpy(yhs_file13," -- Deleted file -- "); }

if(yhs_files_open == 12){ yhs_filename1=yhs_file12; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
if(curfile == 12) { strcpy(yhs_file12,yhs_filename1); }
strcpy(yhs_file12," -- Deleted file -- "); }

if(yhs_files_open == 11){ yhs_filename1=yhs_file11; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1); }
if(curfile == 11) { strcpy(yhs_file11,yhs_filename1); }
strcpy(yhs_file11," -- Deleted file -- "); }

if(yhs_files_open == 10){ yhs_filename1=yhs_file10; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
if(curfile == 10) { strcpy(yhs_file10,yhs_filename1);}
strcpy(yhs_file10," -- Deleted file -- "); }

if(yhs_files_open == 9){ yhs_filename1=yhs_file9; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
if(curfile == 9) { strcpy(yhs_file9,yhs_filename1); }
strcpy(yhs_file9," -- Deleted file -- "); }

if(yhs_files_open == 8){ yhs_filename1=yhs_file8; 

/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
if(curfile == 8) { strcpy(yhs_file8,yhs_filename1); }
strcpy(yhs_file8," -- Deleted file -- "); }

if(yhs_files_open == 7){ yhs_filename1=yhs_file7; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
if(curfile == 7) { strcpy(yhs_file7,yhs_filename1); }
strcpy(yhs_file7," -- Deleted file -- "); }

if(yhs_files_open == 6){ yhs_filename1=yhs_file6; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
if(curfile == 6) { strcpy(yhs_file6,yhs_filename1); }
strcpy(yhs_file6," -- Deleted file -- "); }

if(yhs_files_open == 5){ yhs_filename1=yhs_file5; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
if(curfile == 5) { strcpy(yhs_file5,yhs_filename1); }
strcpy(yhs_file5," -- Deleted file -- "); }

if(yhs_files_open == 4){ yhs_filename1=yhs_file4; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
if(curfile == 4) { strcpy(yhs_file4,yhs_filename1); }
strcpy(yhs_file4," -- Deleted file -- "); }

if(yhs_files_open == 3){ yhs_filename1=yhs_file3; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
if(curfile == 3) { strcpy(yhs_file3,yhs_filename1); }
strcpy(yhs_file3," -- Deleted file -- "); }

if(yhs_files_open == 2){ yhs_filename1=yhs_file2; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }
if(curfile == 2) { strcpy(yhs_file2,yhs_filename1); }
strcpy(yhs_file2," -- Deleted file -- "); }

if(yhs_files_open == 1){ yhs_filename1="No file loaded..."; 
/* wipeout yhs_file.curfile */
if(curfile == 1) { strcpy(yhs_file1,yhs_filename1); }

strcpy(yhs_file1," -- Deleted file -- "); }
yhs_files_open--;
}

void delreload()
{
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
    int i,a,b;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */
int tempx;

/* mass_delete */
for (tempx=1;tempx<yhs_files_open+2;tempx++)
{ XtDestroyWidget(manager[tempx]);squash[tempx]=0;}
/* mass_reload */
if (yhs_files_open > 0) {  tempx=yhs_files_open; yhs_files_open=0; strcpy(file_name,yhs_file1);

#include "file_viewer.c" 

yhs_files_open=tempx; }
if (yhs_files_open > 1) {  tempx=yhs_files_open; yhs_files_open=1; strcpy(file_name,yhs_file2);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 2) {  tempx=yhs_files_open; yhs_files_open=2; strcpy(file_name,yhs_file3);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 3) {  tempx=yhs_files_open; yhs_files_open=3; strcpy(file_name,yhs_file4);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 4) {  tempx=yhs_files_open; yhs_files_open=4; strcpy(file_name,yhs_file5);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 5) {  tempx=yhs_files_open; yhs_files_open=5; strcpy(file_name,yhs_file6);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 6) {  tempx=yhs_files_open; yhs_files_open=6; strcpy(file_name,yhs_file7);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 7) {  tempx=yhs_files_open; yhs_files_open=7; strcpy(file_name,yhs_file8);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 8) {  tempx=yhs_files_open; yhs_files_open=8; strcpy(file_name,yhs_file9);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 9) {  tempx=yhs_files_open; yhs_files_open=9; strcpy(file_name,yhs_file10);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 10) {  tempx=yhs_files_open; yhs_files_open=10; strcpy(file_name,yhs_file11);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 11) {  tempx=yhs_files_open; yhs_files_open=11; strcpy(file_name,yhs_file12);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 12) {  tempx=yhs_files_open; yhs_files_open=12; strcpy(file_name,yhs_file13);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 13) {  tempx=yhs_files_open; yhs_files_open=13; strcpy(file_name,yhs_file14);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 14) {  tempx=yhs_files_open; yhs_files_open=14; strcpy(file_name,yhs_file15);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 15) {  tempx=yhs_files_open; yhs_files_open=15; strcpy(file_name,yhs_file16);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 16) {  tempx=yhs_files_open; yhs_files_open=16; strcpy(file_name,yhs_file17);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 17) {  tempx=yhs_files_open; yhs_files_open=17; strcpy(file_name,yhs_file18);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 18) {  tempx=yhs_files_open; yhs_files_open=18; strcpy(file_name,yhs_file19);

#include "file_viewer.c" 

yhs_files_open=tempx; }

if (yhs_files_open > 19) {  tempx=yhs_files_open; yhs_files_open=19; strcpy(file_name,yhs_file20);

#include "file_viewer.c" 

yhs_files_open=tempx; }
}

void dump_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
fprintf(stderr,"\nDumping system data buffers & restarting...");
system("slice -forceload &");
sleep(2);
fprintf(stderr,"Restarted...OK\n");
exit(0);
}

void multi_merge_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
 {
    Arg al[10];
    int ac;
    int current;
    FILE *p_file;
    int i;
    int a,b;
    for (a=0;a<512;a++){for (b=0;b<512;b++) { array1[a][b]=0; array2[a][b]=0; }}
    if (yhs_files_open >=2 && yhs_files_open < 20 && yhs_filename[0] >= 2)
{
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);

if((p_file = fopen(yhs_filename2,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename2);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array2[a][b]=getc(p_file); }}
    fclose(p_file);

        for (a=0; a<512; a++) {
	for (b=0; b<512; b++) {
	  if (array2[a][b] != array1[a][b])
     		array2[a][b]=((array2[a][b]+array1[a][b])/2);
              else 
		array2[a][b]=array2[a][b];
	                      }}

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
}


void delete_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
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
    int i,a,b;
    int cindex;
    int theScreen;
    Visual *new_visual;
    Widget *list_w = NULL;    /* List of widgets with its own colormap */
int tempx;
if (yhs_files_open != 0 && yhs_filename[0] != 0)
{

deletestring();
delreload();
strcpy(yhs_filename1,yhs_file1);
if (yhs_files_open==1){strcpy(yhs_file2," -- Deleted File -- ");}
if (yhs_files_open==2){strcpy(yhs_file3," -- Deleted File -- ");}
if (yhs_files_open==3){strcpy(yhs_file4," -- Deleted File -- ");}
if (yhs_files_open==4){strcpy(yhs_file5," -- Deleted File -- ");}
if (yhs_files_open==5){strcpy(yhs_file6," -- Deleted File -- ");}
if (yhs_files_open==6){strcpy(yhs_file7," -- Deleted File -- ");}
if (yhs_files_open==7){strcpy(yhs_file8," -- Deleted File -- ");}
if (yhs_files_open==8){strcpy(yhs_file9," -- Deleted File -- ");}
if (yhs_files_open==9){strcpy(yhs_file10," -- Deleted File -- ");}
if (yhs_files_open==10){strcpy(yhs_file11," -- Deleted File -- ");}
if (yhs_files_open==11){strcpy(yhs_file12," -- Deleted File -- ");}
if (yhs_files_open==12){strcpy(yhs_file13," -- Deleted File -- ");}
if (yhs_files_open==13){strcpy(yhs_file14," -- Deleted File -- ");}
if (yhs_files_open==14){strcpy(yhs_file15," -- Deleted File -- ");}
if (yhs_files_open==15){strcpy(yhs_file16," -- Deleted File -- ");}
if (yhs_files_open==16){strcpy(yhs_file17," -- Deleted File -- ");}
if (yhs_files_open==17){strcpy(yhs_file18," -- Deleted File -- ");}
if (yhs_files_open==18){strcpy(yhs_file19," -- Deleted File -- ");}
if (yhs_files_open==19){strcpy(yhs_file20," -- Deleted File -- ");}
curfile=1;
for (i=0;i<21;i++) yhs_filename[i]=0;
argcount=0;
XtSetArg(args[argcount],XmNset,False);argcount++;
XtSetValues(da,args,argcount);
XtSetValues(db,args,argcount);
XtSetValues(dc,args,argcount);
XtSetValues(dd,args,argcount);
XtSetValues(de,args,argcount);
XtSetValues(df,args,argcount);
XtSetValues(dg,args,argcount);
XtSetValues(dh,args,argcount);
XtSetValues(di,args,argcount);
XtSetValues(dj,args,argcount);
XtSetValues(dk,args,argcount);
XtSetValues(dl,args,argcount);
XtSetValues(dm,args,argcount);
XtSetValues(dn,args,argcount);
XtSetValues(du,args,argcount);
XtSetValues(dv,args,argcount);
XtSetValues(dq,args,argcount);
XtSetValues(dr,args,argcount);
XtSetValues(ds,args,argcount);
XtSetValues(dt,args,argcount);
}
}
void tile_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
{
if (yhs_files_open > 0) { XMoveWindow(XtDisplay(manage1),XtWindow(manage1),10,150);  }
if (yhs_files_open > 1) { XMoveWindow(XtDisplay(manage2),XtWindow(manage2),10,300);  }
if (yhs_files_open > 2) { XMoveWindow(XtDisplay(manage3),XtWindow(manage3),10,450);  }
if (yhs_files_open > 3) { XMoveWindow(XtDisplay(manage4),XtWindow(manage4),10,600);  }
if (yhs_files_open > 4) { XMoveWindow(XtDisplay(manage5),XtWindow(manage5),250,150);  }
if (yhs_files_open > 5) { XMoveWindow(XtDisplay(manage6),XtWindow(manage6),250,300);  }
if (yhs_files_open > 6) { XMoveWindow(XtDisplay(manage7),XtWindow(manage7),250,450);  }
if (yhs_files_open > 7) { XMoveWindow(XtDisplay(manage8),XtWindow(manage8),250,600);  }
if (yhs_files_open > 8) { XMoveWindow(XtDisplay(manage9),XtWindow(manage9),500,150);  }
if (yhs_files_open > 9) { XMoveWindow(XtDisplay(manage10),XtWindow(manage10),500,300);  }
if (yhs_files_open > 10) { XMoveWindow(XtDisplay(manage11),XtWindow(manage11),500,450);  }
if (yhs_files_open > 11) { XMoveWindow(XtDisplay(manage12),XtWindow(manage12),500,600);  }
if (yhs_files_open > 12) { XMoveWindow(XtDisplay(manage13),XtWindow(manage13),750,150);  }
if (yhs_files_open > 13) { XMoveWindow(XtDisplay(manage14),XtWindow(manage14),750,300);  }
if (yhs_files_open > 14) { XMoveWindow(XtDisplay(manage15),XtWindow(manage15),750,450);  }
if (yhs_files_open > 15) { XMoveWindow(XtDisplay(manage16),XtWindow(manage16),750,600);  }
if (yhs_files_open > 16) { XMoveWindow(XtDisplay(manage17),XtWindow(manage17),1000,150);  }
if (yhs_files_open > 17) { XMoveWindow(XtDisplay(manage18),XtWindow(manage18),1000,300);  }
if (yhs_files_open > 18) { XMoveWindow(XtDisplay(manage19),XtWindow(manage19),1000,450);  }
if (yhs_files_open > 19) { XMoveWindow(XtDisplay(manage20),XtWindow(manage20),1000,600);  }
}

void reduce_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
 {
    Arg al[10];
    long int  cont_byte, row_cont;
    int ac;
    int current;
    FILE *pfile;
    char *tempfile="...........................................................................................";
    int i=-1,value=0;
    int a,b,row,col;
    if (yhs_files_open >=1 && yhs_files_open < 21)
{
    reduction();
    i=0;
    for (a=0;a<512;a++){ for (b=0;b<512;b++) {
    XPutPixel(theXImage_y[curfile],b,a,array2[a][b]); array2[a][b]=0; 
    i++;}}
    XPutImage(XtDisplay(view[curfile]), thePixmap_y[curfile], arrayGC[curfile], theXImage_y[curfile], 
		0, 0, 0, 0, theXImage_y[curfile]->width, theXImage_y[curfile]->height); 
    XClearArea(XtDisplay(view[curfile]),XtWindow(view[curfile]),0,0,0,0,True);
    resize_callback();
}
}

void expand_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
 {
    Arg al[10];
    long int  cont_byte, row_cont;
    int ac;
    int current;
    FILE *pfile;
    char *tempfile="................................................................................................";
    int i=-1,value=0;
    int a,b,row,col;
    if (yhs_files_open >=1 && yhs_files_open < 21)
{
    if((pfile = fopen(yhs_filename1,"r")) == NULL)
    {fprintf(stderr, "Cannot open: %s\n", yhs_filename1);   exit(1);}
     i=0;
    for (a=0;a<512;a++){ for (b=0;b<512;b++) {
    XPutPixel(theXImage_y[curfile],b,a,getc(pfile)); array2[a][b]=0; 
    i++;}}
    fclose(pfile);
    XPutImage(XtDisplay(view[curfile]), thePixmap_y[curfile], arrayGC[curfile], theXImage_y[curfile], 
		0, 0, 0, 0, theXImage_y[curfile]->width, theXImage_y[curfile]->height); 
    XClearArea(XtDisplay(view[curfile]),XtWindow(view[curfile]),0,0,0,0,True);
if (curfile==1){ XResizeWindow(XtDisplay(manage1),XtWindow(manage1),512,512);}
if (curfile==2){ XResizeWindow(XtDisplay(manage2),XtWindow(manage2),512,512);}
if (curfile==3){ XResizeWindow(XtDisplay(manage3),XtWindow(manage3),512,512);}
if (curfile==4){ XResizeWindow(XtDisplay(manage4),XtWindow(manage4),512,512);}
if (curfile==5){ XResizeWindow(XtDisplay(manage5),XtWindow(manage5),512,512);}
if (curfile==6){ XResizeWindow(XtDisplay(manage6),XtWindow(manage6),512,512);}
if (curfile==7){ XResizeWindow(XtDisplay(manage7),XtWindow(manage7),512,512);}
if (curfile==8){ XResizeWindow(XtDisplay(manage8),XtWindow(manage8),512,512);}
if (curfile==9){ XResizeWindow(XtDisplay(manage9),XtWindow(manage9),512,512);}
if (curfile==10){ XResizeWindow(XtDisplay(manage10),XtWindow(manage10),512,512);}
if (curfile==11){ XResizeWindow(XtDisplay(manage11),XtWindow(manage11),512,512);}
if (curfile==12){ XResizeWindow(XtDisplay(manage12),XtWindow(manage12),512,512);}
if (curfile==13){ XResizeWindow(XtDisplay(manage13),XtWindow(manage13),512,512);}
if (curfile==14){ XResizeWindow(XtDisplay(manage14),XtWindow(manage14),512,512);}
if (curfile==15){ XResizeWindow(XtDisplay(manage15),XtWindow(manage15),512,512);}
if (curfile==16){ XResizeWindow(XtDisplay(manage16),XtWindow(manage16),512,512);}
if (curfile==17){ XResizeWindow(XtDisplay(manage17),XtWindow(manage17),512,512);}
if (curfile==18){ XResizeWindow(XtDisplay(manage18),XtWindow(manage18),512,512);}
if (curfile==19){ XResizeWindow(XtDisplay(manage19),XtWindow(manage19),512,512);}
if (curfile==20){ XResizeWindow(XtDisplay(manage20),XtWindow(manage20),512,512);}
}
}

void reduction()
{
   Arg al[10];
    long int  cont_byte, row_cont;
    int ac;
    int current;
    FILE *pfile;
    int i=-1,value=0;
    int a,b,row,col;

   if((pfile = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(pfile); array2[a][b]=0; }}
    fclose(pfile);
        row=0;col=0;
	for (a=0;a<512;a=a+4)
              { 
        for (b=0;b<512;b=b+4)
             { 
               array2[row][col]=((array1[a][b]+array1[a+1][b]+array1[a][b+1]+
               array1[a+1][b+1]+array1[a+2][b]+array1[a+2][b+1]+array1[a+2][b+2]+
               array1[a+1][b+2]+array1[a][b+2]+array1[a+3][b]+array1[a+3][b+1]+
               array1[a+3][b+2]+array1[a+3][b+3]+array1[a+2][b+3]+array1[a+1][b+3]+
               array1[a][b+3])/16);
    	         col++;
  		}row++;col=0;} 
}


void multi_window_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
 {
    Arg al[10];
    int ac;
    int current;
    FILE *p_file;
    int i;
    int a,b;
    for (a=0;a<512;a++){for (b=0;b<512;b++) { array1[a][b]=0; array2[a][b]=0; }}
    if (yhs_files_open >=2 && yhs_files_open < 20 && yhs_filename[0] >= 2)
{
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);

if((p_file = fopen(yhs_filename2,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename2);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array2[a][b]=getc(p_file); }}
    fclose(p_file);

        for (a=0; a<512; a++) {
	for (b=0; b<512; b++) {
	  if (array2[a][b] == array1[a][b])
     		array2[a][b]=0;
              else 
		array2[a][b]=255;
	                      }}

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
}
void inverse_callback(Widget w, XtPointer client_data,
                  XmAnyCallbackStruct *call_data)
 {
    Arg al[10];
    int ac;
    int current;
    FILE *p_file;
    int i;
    int a,b;
    for (a=0;a<512;a++){for (b=0;b<512;b++) { array1[a][b]=0; array2[a][b]=0; }}
    if (yhs_files_open >=1 && yhs_files_open < 20 && yhs_filename[0] >= 1)
{
   if((p_file = fopen(yhs_filename1,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n",yhs_filename1);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=getc(p_file); }}
    fclose(p_file);
        for (a=0; a<512; a++) {
	for (b=0; b<512; b++) {
	  array2[a][b] = 255-array1[a][b];
	                      }}

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
}

/*-------------------------------------------------------------*/
/*  f v _ c a n c e l                                          */
/*                                                             */
/*  Callback for the "Cancel" button in the view selection box */
/*-------------------------------------------------------------*/
static void fv_cancel(Widget w, XtPointer client_data,
                      XmSelectionBoxCallbackStruct *call_data) {
  deactivate_view_dialog();
}

/*-------------------------------------------------------------*/
/*  l o a d _ f i l e                                          */
/*                                                             */
/*  Read in file to be displayed                               */
/*                                                             */
/*-------------------------------------------------------------*/
void load_file(char *filename, Widget w)
{
    int i;
    FILE *p_file;
    unsigned int image_bytes;

    /* Compute some sizes */
    image_bytes = IMAGE_WIDTH * IMAGE_HEIGHT;

    /* Clean memory allocated by former resources before */
    /* creating the new ones                             */
    if (remove_images) {
       XDestroyImage(theXImage_1);   
       XDestroyImage(theXImage_2);
       XFreePixmap(XtDisplay(w), thePixmap_1);
       XFreePixmap(XtDisplay(w), thePixmap_2);
    }

    /* Open the file */
    if((p_file = fopen(filename,"r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n", filename);
        exit(1);
    }

    /* Allocate room for image_1's data */
    if((image_1 = (unsigned char *) calloc(image_bytes,
                              sizeof(unsigned char) )) == NULL)
    {
        fprintf(stderr, "Error allocating room for image...\n");
        exit(1);
    }

    /* Handle 8-bit images in straightforward manner */
    for(i=0; i<image_bytes; i++)
      image_1[i] = getc(p_file);

    /* Allocate room for image_2's data */
    if((image_2 = (unsigned char *) calloc(image_bytes,
                              sizeof(unsigned char) )) == NULL)
    {
        fprintf(stderr, "Error allocating room for image...\n");
        exit(1);
    }

    /* Handle 8-bit images in straightforward manner */
    for(i = 0; i < image_bytes; i++)
      image_2[i] = BlackPixel(XtDisplay(w), DefaultScreen(XtDisplay(w)));

    remove_images   = True;
    file_not_loaded = False;

    /* Close file and return */
    fclose(p_file);
}



/*----------------------------------------------------------------*/
/*  s e t u p _ x i m a g e                                       */
/*                                                                */
/*  This function sets up an XImage in ZPixmap format.            */
/*  It creates a Pixmap in the server and copies the image over   */
/*  using XPutImage. Once this is done, the image can be drawn by */
/*  copying from the pixmap into the window (using XCopyArea).    */
/*----------------------------------------------------------------*/
void setup_ximage()
{
    /* Set up an XImage structure in ZPixmap format */
    theXImage_1 = XCreateImage(XtDisplay(draw_1), theVisual, vis_depth, 
                    ZPixmap, 0, (char *)image_1, IMAGE_WIDTH,
                    IMAGE_HEIGHT, 8, 0);

    theXImage_1->byte_order = MSBFirst;

    /* Create Pixmap 1 and copy image into it */
    thePixmap_1 = XCreatePixmap(XtDisplay(draw_1), XtWindow(draw_1),
                       theXImage_1->width, theXImage_1->height, vis_depth);

    XPutImage(XtDisplay(draw_1), thePixmap_1, image_gc_1, theXImage_1, 
              0, 0, 0, 0, theXImage_1->width, theXImage_1->height);

    /* Set up an XImage structure in ZPixmap format */
    theXImage_2 = XCreateImage(XtDisplay(draw_2), theVisual, vis_depth, 
                    ZPixmap, 0, (char *)image_2, IMAGE_WIDTH,
                    IMAGE_HEIGHT, 8, 0);

    theXImage_2->byte_order = MSBFirst;

    /* Create Pixmap 2 and copy image into it */
    thePixmap_2 = XCreatePixmap(XtDisplay(draw_2), XtWindow(draw_2),
                       theXImage_2->width, theXImage_2->height, vis_depth);

    XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2, 
              0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
}
