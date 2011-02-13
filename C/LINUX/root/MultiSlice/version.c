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
/*     File: version.c                                         */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"
#include "logo.h"

#include <Xm/Xm.h>
#include <Xm/Form.h>
#include <Xm/Frame.h>
#include <Xm/Label.h>
#include <Xm/PushB.h>

#define TITLE  "\nC-T IMAGE APPLICATION PROGRAM [SLICE] Version [2.1.4.c+]\n"
#define GROUP  "\nMVRG & CTAC\n" \
	       "\nGloria Bueno\n" \
               "\nMultiSlice System by :-\n Yohann Sulaiman,\n B.Eng. (hons) [EEE] 1997-98 \n 399EE Project \n" \
	       "\nPhD project 1996-1997\n"

/* Extern variables */
extern char *action;
extern int selection;
extern int curfile;
extern void refresh_action(void);

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

static Widget version_dialog = (Widget) NULL;
static Pixmap cov_pix = (Pixmap) NULL;

/* Function prototypes */
void create_version_dialog(Widget parent);

void activate_version_dialog(Widget w, XtPointer client_data, 
                             XmAnyCallbackStruct *call_data);
void help_moi(Widget w, XtPointer client_data, 
                             XmAnyCallbackStruct *call_data);

void deactivate_version_dialog(void);

static void button_callback(Widget w, XtPointer client_data, 
                            XmAnyCallbackStruct *call_data);

/* Function definition */
void create_version_dialog(Widget parent) {

  Display *display;
  Widget temp_widget = parent;
  Widget version_title, version_pixmap, version_group, version_button;
  XmString title_string, group_string;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  version_dialog = XmCreateFormDialog(temp_widget, "version dialog",
                                      NULL, 0);

  /* Create the version title text widget */
  title_string = XmStringCreateLtoR(TITLE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNtopAttachment,    XmATTACH_FORM     ); argcount++;  
  XtSetArg(args[argcount], XmNleftAttachment,   XmATTACH_POSITION ); argcount++;
  XtSetArg(args[argcount], XmNleftPosition,     5                 ); argcount++;
  XtSetArg(args[argcount], XmNrightAttachment,  XmATTACH_POSITION ); argcount++;  
  XtSetArg(args[argcount], XmNrightPosition,    95                ); argcount++;
  XtSetArg(args[argcount], XmNlabelString,      title_string      ); argcount++;
  XtSetArg(args[argcount], XmNalignment,        XmALIGNMENT_CENTER); argcount++;
  version_title = XmCreateLabel(version_dialog, "version title", args, argcount);
  XtManageChild(version_title);

  XmStringFree(title_string);

  /* Create the pixmap and install into the version */
  /* bitmap widget as it is created.                */
  display = XtDisplay(parent);
  cov_pix = XCreatePixmapFromBitmapData(display,
                       DefaultRootWindow(display),
                       logo_bits, logo_width, logo_height,
                       BlackPixel(display,DefaultScreen(display)),
                       WhitePixel(display,DefaultScreen(display)),
                       DefaultDepth(display,DefaultScreen(display)));

  argcount = 0;
  XtSetArg(args[argcount], XmNtopAttachment,    XmATTACH_WIDGET  ); argcount++;  
  XtSetArg(args[argcount], XmNtopWidget,        version_title    ); argcount++;
  XtSetArg(args[argcount], XmNleftAttachment,   XmATTACH_POSITION); argcount++;
  XtSetArg(args[argcount], XmNleftPosition,     5                ); argcount++;
  XtSetArg(args[argcount], XmNrightAttachment,  XmATTACH_POSITION); argcount++;  
  XtSetArg(args[argcount], XmNrightPosition,    95               ); argcount++;
  XtSetArg(args[argcount], XmNlabelType,        XmPIXMAP         ); argcount++;
  XtSetArg(args[argcount], XmNlabelPixmap,      cov_pix          ); argcount++;
  version_pixmap = XmCreateLabel(version_dialog, "version pixmap",
                                 args, argcount);
  XtManageChild(version_pixmap);

  /* Create the version group text widget */
  group_string = XmStringCreateLtoR(GROUP, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNtopAttachment,    XmATTACH_WIDGET   ); argcount++;  
  XtSetArg(args[argcount], XmNtopWidget,        version_pixmap    ); argcount++;
  XtSetArg(args[argcount], XmNleftAttachment,   XmATTACH_POSITION ); argcount++;
  XtSetArg(args[argcount], XmNleftPosition,     5                 ); argcount++;
  XtSetArg(args[argcount], XmNrightAttachment,  XmATTACH_POSITION ); argcount++;  
  XtSetArg(args[argcount], XmNrightPosition,    95                ); argcount++;
  XtSetArg(args[argcount], XmNlabelString,      group_string      ); argcount++;
  XtSetArg(args[argcount], XmNalignment,        XmALIGNMENT_CENTER); argcount++;
  version_group = XmCreateLabel(version_dialog, "version group", args, argcount);
  XtManageChild(version_group);

  XmStringFree(group_string);
 
  /* Create the version pushbutton widget */
  argcount = 0;
  XtSetArg(args[argcount], XmNtopAttachment,    XmATTACH_WIDGET  ); argcount++;  
  XtSetArg(args[argcount], XmNtopWidget,        version_group    ); argcount++;
  XtSetArg(args[argcount], XmNleftAttachment,   XmATTACH_POSITION); argcount++;
  XtSetArg(args[argcount], XmNleftPosition,     25               ); argcount++;
  XtSetArg(args[argcount], XmNrightAttachment,  XmATTACH_POSITION); argcount++;  
  XtSetArg(args[argcount], XmNrightPosition,    75               ); argcount++;
  XtSetArg(args[argcount], XmNbottomAttachment, XmATTACH_POSITION); argcount++;
  XtSetArg(args[argcount], XmNbottomPosition,   98               ); argcount++;
  version_button = XmCreatePushButton(version_dialog, "OK",
                                      args, argcount);
  XtManageChild(version_button);

  /* Add the actions to the buttons */
  XtAddCallback(version_button, XmNactivateCallback,
                (XtCallbackProc) button_callback, (XtPointer) NULL);
}

void activate_version_dialog(Widget w, XtPointer client_data,
                             XmAnyCallbackStruct *call_data) {
  selection = VERSION;
  action = SELECT;
  refresh_action();
  XtManageChild(version_dialog);
}
void help_moi(Widget w, XtPointer client_data,
                             XmAnyCallbackStruct *call_data) {
fprintf(stderr,"\nYou must have a web browser for help..\nPlease wait...\n");
system("wwwhelpmoi &");
}

void deactivate_version_dialog(void) {
  fprintf(stdout, "\nMotif Version is %d\n", XmVERSION);
  fprintf(stdout, "Motif Revision is %d\n", XmREVISION);
  fprintf(stdout, "Motif Version/Revision is %d\n", XmVersion);
  fprintf(stdout, "Motif Update Level is %d\n", XmUPDATE_LEVEL);
  fprintf(stdout, "Motif Version/Revision/Update is %s\n", XmVERSION_STRING);
  fprintf(stdout, "Current file lock is %d\n",curfile);
  XtUnmanageChild(version_dialog);
}

static void button_callback(Widget w, XtPointer client_data,
                            XmAnyCallbackStruct *call_data) {
  deactivate_version_dialog();
}
