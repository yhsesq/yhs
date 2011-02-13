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
/*     File: exit.c                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>
#include <Xm/MessageB.h>

#define EXIT_MESSAGE "Are you sure you want to exit?"

/* Extern variables */
extern char *action;
extern int selection;
extern int time;
extern void refresh_action(void);
extern Widget main_window;

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

static Widget exit_dialog = (Widget) NULL;

/* Function prototypes */
void create_exit_dialog(Widget parent);

void activate_exit_dialog(Widget w, XtPointer client_data, 
                          XmAnyCallbackStruct *call_data);

void deactivate_exit_dialog(void);

static void exit_button_callback(Widget w, XtPointer client_data, 
                                 XmAnyCallbackStruct *call_data);

static void cancel_button_callback(Widget w, XtPointer client_data, 
                                   XmAnyCallbackStruct *call_data);

/* Function definition */
void create_exit_dialog(Widget parent) {

  XmString message;
  Widget temp_widget = parent;

  /* Ensure the parent of the dialog is a shell widget */
  while ( !XtIsShell(temp_widget) ) {
    temp_widget = XtParent(temp_widget);
  }

  message = XmStringLtoRCreate(EXIT_MESSAGE, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNmessageString, message); argcount++;
  exit_dialog = XmCreateQuestionDialog(temp_widget, "Exit Slice",
                                       args, argcount);

  /* Remove the help button from the dialog */
  temp_widget = XmMessageBoxGetChild(exit_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp_widget);

  /* Add the actions to the buttons */
  XtAddCallback(exit_dialog, XmNokCallback,
                (XtCallbackProc) exit_button_callback, (XtPointer) NULL);
  XtAddCallback(exit_dialog, XmNcancelCallback,
                (XtCallbackProc) cancel_button_callback, (XtPointer) NULL);

  XmStringFree(message);
}

void activate_exit_dialog(Widget w, XtPointer client_data,
                          XmAnyCallbackStruct *call_data) {
  selection = EXIT;
  action = SELECT;
  refresh_action();
  XtManageChild(exit_dialog);
}

void deactivate_exit_dialog(void) {
  /* null - no actions at present dialog is auto unmanaged */
  /* whenever any of its buttons are pressed.              */
}

static void exit_button_callback(Widget w, XtPointer client_data,
                                 XmAnyCallbackStruct *call_data) {
  system("rm slicetempyhsT*");
  fprintf(stderr,"\nNormal program termination ... Run Time : %d seconds\n",time);
  fprintf(stderr,"--------------------------MultiSlice RTP----------------------------\n");
  system("echo end_run >>slicetempyhsTN");
  system("rm slicetempyhsT*");
  XtCloseDisplay(XtDisplay(w));
  exit(0);
}

static void cancel_button_callback(Widget w, XtPointer client_data,
                                   XmAnyCallbackStruct *call_data) {
  deactivate_exit_dialog();
}
