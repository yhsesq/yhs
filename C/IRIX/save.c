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
/*     File: save.c                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>
#include <Xm/FileSB.h>
#include <Xm/MessageB.h>

#define MAX_FILE_NAME_SIZE   255

/* Extern variables */
extern char *action;
extern int selection;
extern int yhs_files_open;
extern char *yhs_filename1;
extern int file_not_loaded;
extern XImage *theXImage_2;
extern void refresh_action(void);

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

/* Local variables */
static Widget file_select      = NULL;
static Widget overwrite_dialog = NULL;
static Widget save_fail_dialog = NULL;
static char filename[MAX_FILE_NAME_SIZE] = "\0";
static char temp_filename[MAX_FILE_NAME_SIZE] = "\0";

/* Function prototypes */
void create_save_as_interface(Widget parent);
void exit_save_as_dialog(void);
void save_as_callback(Widget w, XtPointer client_data,
                      XmAnyCallbackStruct *call_data);

static Widget create_file_select_dialog(Widget parent);
static void activate_file_select_dialog(void);
static void deactivate_file_select_dialog(void);
static void fs_ok_callback(Widget w, XtPointer client_data,
                           XmFileSelectionBoxCallbackStruct *call_data);
static void fs_cancel_callback(Widget w, XtPointer client_data,
                               XmFileSelectionBoxCallbackStruct *call_data);

static void create_overwrite_dialog(Widget parent);
static void activate_overwrite_dialog(void);
static void deactivate_overwrite_dialog(void);
static void overwrite_yes_callback(Widget w, XtPointer client_data,
                                   XmAnyCallbackStruct *call_data);
static void overwrite_no_callback(Widget w, XtPointer client_data,
                                  XmAnyCallbackStruct *call_data);

static void create_save_fail_dialog(Widget parent);
static void activate_save_fail_dialog(void);
static void deactivate_save_fail(void);
static void save_fail_ok_callback(Widget w, XtPointer client_data,
                                  XmAnyCallbackStruct *call_data);

static int save_app_workspace(char *filename);
static int does_file_exist(char *filename);
static void do_save(void);
static char *set_temp_filename(char *new_filename);
static char *get_temp_filename();
static char *set_filename(char *new_filename);
static char *get_filename();

/* Function definition */
void create_save_as_interface(Widget parent)
{
  Widget parent_shell, temp_parent;

  /* Derive the parent shell from the supplied widget */
  parent_shell = parent;
  while ( ! XtIsShell(parent_shell) ) {
    parent_shell = XtParent(parent_shell);
  }

  temp_parent = create_file_select_dialog(parent_shell);

  create_overwrite_dialog(temp_parent);

  create_save_fail_dialog(temp_parent);
}

void exit_save_as_dialog(void)
{
  deactivate_file_select_dialog();
}

void save_as_callback(Widget w, XtPointer client_data,
                      XmAnyCallbackStruct *call_data)
{
  if (yhs_files_open == 0) return;
  selection = SAVE_AS;
  action = SELECT;
  refresh_action();
  activate_file_select_dialog();
}

static Widget create_file_select_dialog(Widget parent)
{
  Widget temp;

  file_select = XmCreateFileSelectionDialog(parent, "file select", NULL, 0);

  temp = XmFileSelectionBoxGetChild(file_select, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp);

  temp = XmFileSelectionBoxGetChild(file_select, XmDIALOG_DIR_LIST);
  XtUnmanageChild(temp);

  temp = XmFileSelectionBoxGetChild(file_select, XmDIALOG_DIR_LIST_LABEL);
  XtUnmanageChild(temp);

  /* Install the callbacks */
  XtAddCallback(file_select, XmNokCallback,
                (XtCallbackProc) fs_ok_callback, (XtPointer) NULL);
  XtAddCallback(file_select, XmNcancelCallback,
                (XtCallbackProc) fs_cancel_callback, (XtPointer) NULL);

  return file_select;
}

static void activate_file_select_dialog(void)
{
  XmString dummy;

  dummy = XmStringCreate("*.raw", XmSTRING_DEFAULT_CHARSET );
  XmFileSelectionDoSearch(file_select, dummy); 
  XmStringFree(dummy);
  XtManageChild(file_select);
}

static void deactivate_file_select_dialog(void)
{
  XtUnmanageChild(file_select);
}

static void fs_ok_callback(Widget w, XtPointer client_data,
                           XmFileSelectionBoxCallbackStruct *call_data)
{
  char *filename;
  XmString x_filename;

  x_filename = call_data->value;
  XmStringGetLtoR(x_filename, XmSTRING_DEFAULT_CHARSET, &filename);

  set_temp_filename(filename);
  XtFree(filename);
  if (does_file_exist(get_temp_filename())) {
    activate_overwrite_dialog();
  } else {
    do_save();
  }
}

static void fs_cancel_callback(Widget w, XtPointer client_data,
                               XmFileSelectionBoxCallbackStruct *call_data)
{
  exit_save_as_dialog();
}

static void create_overwrite_dialog(Widget parent)
{
  Widget temp;

  overwrite_dialog = XmCreateQuestionDialog(parent, "overwrite dialog", NULL, 0);

  temp = XmMessageBoxGetChild(overwrite_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp);

  /* Install the callbacks */
  XtAddCallback(overwrite_dialog, XmNokCallback, 
                (XtCallbackProc) overwrite_yes_callback, (XtPointer) NULL);
  XtAddCallback(overwrite_dialog, XmNcancelCallback, 
                (XtCallbackProc) overwrite_no_callback, (XtPointer) NULL);
}

static void activate_overwrite_dialog(void)
{
  char       buffer[MAX_BUFFER_SIZE];
  XmString   question;

  strcpy(buffer, "Overwrite the file \n");
  strcat(buffer, get_temp_filename());
  strcat(buffer, " ?");

  question = XmStringCreateLtoR(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNmessageString, question);  argcount++;
  XtSetValues(overwrite_dialog, args, argcount);

  XmStringFree(question);

  XtManageChild(overwrite_dialog);
}

static void deactivate_overwrite_dialog(void)
{
  /* null - currently autounmanage */
}

static void overwrite_yes_callback(Widget w, XtPointer client_data,
                                   XmAnyCallbackStruct *call_data)
{
  do_save();
}

static void overwrite_no_callback(Widget w, XtPointer client_data,
                                  XmAnyCallbackStruct *call_data)
{
  /* null - currently autounmanage */
}

static void create_save_fail_dialog(Widget parent)
{
  Widget temp;

  save_fail_dialog = XmCreateWarningDialog(parent, "save fail", NULL, 0);

  temp = XmMessageBoxGetChild(save_fail_dialog, XmDIALOG_HELP_BUTTON);
  XtUnmanageChild(temp);
  temp = XmMessageBoxGetChild(save_fail_dialog, XmDIALOG_CANCEL_BUTTON);
  XtUnmanageChild(temp);

  /* Install the callback */
  XtAddCallback(save_fail_dialog, XmNokCallback, 
                (XtCallbackProc) save_fail_ok_callback, (XtPointer) NULL);
}

static void activate_save_fail_dialog(void)
{
  char       buffer[MAX_BUFFER_SIZE];
  XmString   x_message;

  strcpy(buffer, "An error occured while\nwriting the file \n");
  strcat(buffer, get_temp_filename());

  x_message = XmStringCreateLtoR(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNmessageString, x_message);  argcount++;
  XtSetValues(save_fail_dialog, args, argcount); 

  XmStringFree(x_message);

  XtManageChild(save_fail_dialog);
}

static void deactivate_save_fail(void)
{
  /* null - currently autounmanage */
}

static void save_fail_ok_callback(Widget w, XtPointer client_data,
                                  XmAnyCallbackStruct *call_data)
{
  /* null - currently autounmanage */
}

static int save_app_workspace(char *filename)
{
  FILE *fp;
  FILE *wp;
  int i, j;
  int state;
  if ((wp = fopen(yhs_filename1,"r")) != NULL) {
  if ((fp = fopen(filename,"w")) != NULL) {
    for(i=0; i<IMAGE_WIDTH; i++)
      for(j=0; j<IMAGE_HEIGHT; j++)
        putc((unsigned char) getc(wp), fp);
    fclose(fp);
    fclose(wp);
    state = 1;
  } else {
    state = 0;
  }  
}
  return state;
}

static int does_file_exist(char *filename)
{
  int  it_does;
  FILE *temp_file;

  it_does = ((temp_file = fopen(filename,"r")) != NULL);
  if (it_does) fclose(temp_file);
  return it_does;
}

static void do_save(void)
{
  if (save_app_workspace(get_temp_filename())) {
    /* save was sucessful */
    set_filename(get_temp_filename());
    exit_save_as_dialog();
  } else {
    /* save was unsucessful */
    activate_save_fail_dialog();
  }
}

static char *set_temp_filename(char *new_filename)
{
  strncpy(temp_filename, new_filename, (size_t) MAX_FILE_NAME_SIZE);
  return temp_filename;
}

static char *get_temp_filename()
{
  return temp_filename;
}

static char *set_filename(char *new_filename)
{
  strncpy(filename, new_filename, (size_t) MAX_FILE_NAME_SIZE);
  return filename;
}

static char *get_filename()
{
  return filename;
}
