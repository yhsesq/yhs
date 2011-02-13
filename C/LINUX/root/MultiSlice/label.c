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
/*     File: label.c                                           */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>
#include <Xm/Label.h>

#define INPUT_WINDOW_MESSAGE   "INPUT WINDOW"
#define OUTPUT_WINDOW_MESSAGE  "OUTPUT WINDOW"
#define FILE_WINDOW_MESSAGE    "  CURRENT FILE: "
#define ACTION_WINDOW_MESSAGE  "  ACTION TO PERFORM: "
#define COORD_WINDOW_X         "   X = "
#define COORD_WINDOW_Y         "\n   Y = "
#define COORD_WINDOW_VALUE     "\n   VALUE = "

/* Extern variables */
extern char *action;
extern char *file_name;

/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

static Widget file_label   = NULL,
              action_label = NULL,
              coord_label  = NULL;

static char   buffer[MAX_BUFFER_SIZE];

/* Function definition */
void create_labels(Widget w1, Widget w2, Widget w3, Widget w4, Widget w5)
{
  Widget input_label, output_label;
  XmString input_text, output_text, file_text, action_text, coord_text;

  /* Create the label widget input_label */
  input_text = XmStringLtoRCreate(INPUT_WINDOW_MESSAGE,
                                  XmSTRING_DEFAULT_CHARSET);
  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, input_text); argcount++;
  input_label = XmCreateLabel(w1, "input label", args, argcount);
  XtManageChild(input_label);
  XmStringFree(input_text);

  /* Create the label widget output_label */
  output_text = XmStringLtoRCreate(OUTPUT_WINDOW_MESSAGE,
                                   XmSTRING_DEFAULT_CHARSET);
  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, output_text); argcount++;
  output_label = XmCreateLabel(w2, "output label", args, argcount);
  XtManageChild(output_label);
  XmStringFree(output_text);

  /* Create the label widget file_label */
  strcpy(buffer, FILE_WINDOW_MESSAGE);
  strcat(buffer, file_name);

  file_text = XmStringLtoRCreate(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, file_text);           argcount++;
  XtSetArg(args[argcount], XmNalignment, XmALIGNMENT_BEGINNING); argcount++;
  file_label = XmCreateLabel(w3, "file label", args, argcount);
  XtManageChild(file_label);
  XmStringFree(file_text);

  /* Create the label widget action_label */
  strcpy(buffer, ACTION_WINDOW_MESSAGE);
  strcat(buffer, action);

  action_text = XmStringLtoRCreate(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, action_text);         argcount++;
  XtSetArg(args[argcount], XmNalignment, XmALIGNMENT_BEGINNING); argcount++;
  action_label = XmCreateLabel(w4, "action label", args, argcount);
  XtManageChild(action_label);
  XmStringFree(action_text);

  /* Create the label widget coord_label */
  strcpy(buffer, COORD_WINDOW_X);
  strcat(buffer, "  0");
  strcat(buffer, COORD_WINDOW_Y);
  strcat(buffer, "  0");
  strcat(buffer, COORD_WINDOW_VALUE);
  strcat(buffer, "  0");

  coord_text = XmStringLtoRCreate(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, coord_text);          argcount++;
  XtSetArg(args[argcount], XmNalignment, XmALIGNMENT_BEGINNING); argcount++;
  coord_label = XmCreateLabel(w5, "coord label", args, argcount);
  XtManageChild(coord_label);
  XmStringFree(coord_text);
}

void refresh_filename(void)
{
  XmString current_file;

  strcpy(buffer, FILE_WINDOW_MESSAGE);
  strcat(buffer, file_name);

  current_file = XmStringCreateLtoR(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, current_file); argcount++;
  XtSetValues(file_label, args, argcount);

  XmStringFree(current_file);
}

void refresh_action(void)
{
/*  XmString current_action;

  strcpy(buffer, ACTION_WINDOW_MESSAGE);
  strcat(buffer, action);

  current_action = XmStringCreateLtoR(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, current_action); argcount++;
  XtSetValues(action_label, args, argcount);

  XmStringFree(current_action);
*/
}

void refresh_coord(int x, int y, int v)
{
  char cx[2], cy[2], cv[2];
  XmString current_coord;
/*
  strcpy(buffer, COORD_WINDOW_X);
  sprintf(cx, "%3d", x);
  strcat(buffer, cx);
  strcat(buffer, COORD_WINDOW_Y);
  sprintf(cy, "%3d", y);
  strcat(buffer, cy);
  strcat(buffer, COORD_WINDOW_VALUE);
  sprintf(cv, "%3d", v);
  strcat(buffer, cv);

  current_coord = XmStringCreateLtoR(buffer, XmSTRING_DEFAULT_CHARSET);

  argcount = 0;
  XtSetArg(args[argcount], XmNlabelString, current_coord); argcount++;
  XtSetValues(coord_label, args, argcount);

  XmStringFree(current_coord);
*/
}
