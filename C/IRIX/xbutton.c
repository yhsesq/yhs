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
/*  File: xbutton.c
 *  
 *  Implement a "button window"--a window that displays
 *  a message and calls an "action" routine when the
 *  user presses any mouse button inside the window.
 *  (Uses Xlib's context management routines)
 *
 */
/*-------------------------------------------------------------*/
#include <stdio.h>
#include "xwins.h"
#include "app_glob.c"

typedef struct D_BUTTON
{
    char     *text;
    int      (*action)(/* caddr_t */);
    caddr_t  action_args;
} D_BUTTON;

static int button_handler(/* XWIN * */);
/*-------------------------------------------------------------*/
/*  M a k e X B u t t o n
 *
 *  A labeled "button" that you can press to perform an "action"
 *
 */
XWIN *MakeXButton(x, y, width, height, bdwidth, bdcolor,
                  bgcolor, parent, text, button_action,
                  action_data)
int           x, y;
unsigned      width, height, bdwidth;
unsigned long bdcolor, bgcolor;
Window        parent; 
char          *text;
int           (*button_action)();
caddr_t       action_data;
{
    XWIN     *new_button;
    D_BUTTON *p_data;
    
/* Allocate button-specific data */
    if((p_data = (D_BUTTON *)calloc(1, sizeof(D_BUTTON))) 
        == NULL)
    {
        fprintf(stderr, "No memory for button's data");
        exit(1);
    } 

/* Allocate a XWIN structure */
    if((new_button = (XWIN *)calloc(1, sizeof(XWIN))) == NULL)
    {
        fprintf(stderr, "No memory for button");
        exit(1);
    } 

/* Initialize button's data and save pointer in new_button */
    p_data->action = button_action;
    p_data->action_args = action_data;
    p_data->text = text;
    new_button->data = p_data;
    new_button->event_handler = button_handler;
    new_button->parent = parent;
    new_button->xid = XCreateSimpleWindow(theDisplay, parent,
                      x, y, width, height, bdwidth, bdcolor, 
                      bgcolor);

/* Save new_button as data associated with this window id
 * and the context "xwin_context"
 */
    if(XSaveContext(theDisplay, new_button->xid, xwin_context,
                 (caddr_t) new_button) != 0)
    {
        fprintf(stderr, "Error saving xwin_context data");
        exit(1);
    }
                                
    XSelectInput(theDisplay, new_button->xid, 
        OwnerGrabButtonMask | ExposureMask | ButtonPressMask);
                               
    XMapWindow(theDisplay, new_button->xid);
    return (new_button);
}
/*-------------------------------------------------------------*/
/*  b u t t o n _ h a n d l e r
 *  
 *  Event handler for the "button" 
 */
static int button_handler(p_xwin)
XWIN *p_xwin;
{
    D_BUTTON *p_data = (D_BUTTON *) p_xwin->data;
    
/* Handle events occurring in this window */
    if(theEvent.xany.window == p_xwin->xid)
    {
        switch(theEvent.type)
        {
            case Expose:
                if (theEvent.xexpose.count == 0)
                {
                    XClearWindow(theDisplay, p_xwin->xid);
                    XDrawString(theDisplay, p_xwin->xid,
                        theGC, 
                        theFontStruct->max_bounds.width / 2,
                        theFontStruct->max_bounds.ascent + 
                        theFontStruct->max_bounds.descent,
                        p_data->text, strlen(p_data->text));
                }
                break;
            case ButtonPress:
/* Call the action routine, PROVIDED it is valid */
                if(p_data->action != NULL)
                    (*p_data->action)(p_data->action_args);
                break;
        }
    }
    return 0;
}

