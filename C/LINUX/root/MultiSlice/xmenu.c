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
/*  File: xmenu.c
 *  
 *  Implement a "menu"--a window that displays a number
 *  of buttons and calls an "action" routine when the
 *  user presses any mouse button inside one of the buttons.
 *
 */
/*-------------------------------------------------------------*/
#include <stdio.h>
#include <varargs.h>  /*  Variable-length argument list macros */
#include "xwins.h"
#include "app_glob.c"

#define GAP    2     /* Separation between buttons, in pixels  */

typedef int    (*P_FUNC)();
typedef char   *P_CHAR;

typedef struct D_ITEM
{
    int           item_number; /* This item's index    */
    int           width;       /* Width of button      */
    int           height;      /* Height of button     */
    char          *item_text;  /* Label for this item  */
    P_FUNC        item_action; /* Ptr to Action routine*/
    caddr_t       action_args; /* Argument for action  */
    XWIN          *p_button;   /* This item's window   */
    struct D_ITEM *next;       /* Pointer to next item */
} D_ITEM;

typedef struct D_MENU
{
    int      nitems;          /* How many items in menu */
    int      max_item_width;  /* Max. width in pixels   */
    int      max_item_height; /* Max. height in pixels  */
    int      style;           /* Menu style             */
#define MENUBAR_STYLE  0
#define PULLDOWN_STYLE 1
    D_ITEM   *item_list;      /* Start of list of items */
} D_MENU;

static int menu_handler();           /* Event handler for menu */
/*-------------------------------------------------------------*/
/*  M a k e X m e n u
 *
 *  A "menu" with a number of items (each implemented as a
 *  button that you can press to perform an "action")
 *  Uses the the UNIX calling convention for functions
 *  requiring a variable number of arguments.
 *
 *  In this function,   ...    represents a list of:
 *
 *  char *item_text, int (*item_action)(), caddr_t action_args
 *
 *  A NULL marks end of list.
 */
XWIN *MakeXMenu(va_alist)
va_dcl  /* Macro must appear without semicolon */
{
    va_list       argp;         /* Used to access arguments */
    XWIN          *new_menu;
    D_MENU        *p_data;
    D_ITEM        *p_item, *p_i;
    char          *item_text;
    int           xb, yb, char_height;
    unsigned      width, height, bdwidth;
    int           x, y, style;
    unsigned long bdcolor, bgcolor;
    Window        parent;

    va_start(argp);
    x = va_arg(argp, int);
    y = va_arg(argp, int);
    bdwidth = va_arg(argp, unsigned int);
    bdcolor = va_arg(argp, unsigned long);
    bgcolor = va_arg(argp, unsigned long);
    parent = va_arg(argp, Window);
    style = va_arg(argp, int);

    char_height = theFontStruct->max_bounds.ascent + 
                  theFontStruct->max_bounds.descent + 4;

/* Allocate memory for the menu */
    if((p_data = (D_MENU*)calloc(1, sizeof(D_MENU))) == NULL)
    {
        fprintf(stderr, "No memory for menu's data");
        exit(1);
    } 

/* Allocate a XWIN structure */
    if((new_menu = (XWIN*)calloc(1, sizeof(XWIN))) == NULL)
    {
        fprintf(stderr, "No memory for the menu");
        exit(1);
    } 

/* Initialize the menu's data */
    p_data->max_item_height = char_height;
    p_data->style = style; 
    
/* Get rest of the items one by one and compute sizes of buttons */
    while((item_text = va_arg(argp, P_CHAR)) != NULL)
    {
/* Allocate memory for this menu item */
        if((p_item = (D_ITEM*)calloc(1, sizeof(D_ITEM))) == NULL)
        {
            fprintf(stderr, "No memory for menu items");
            exit(1);
        } 
/* Save the pointer in the list */
        if(p_data->item_list == NULL)
            p_data->item_list = p_item;
        else
        {
/* Got to the end of the list using an empty for loop */
            for(p_i = p_data->item_list; p_i->next != NULL;
                p_i = p_i->next) ;
            p_i->next = p_item;
        }
/* Set up contents of item data structure */
        p_item->item_number = p_data->nitems++;
        p_item->item_text = item_text;
        p_item->width = XTextWidth(theFontStruct, item_text, 
                            strlen(item_text)) + 4;
        p_item->height = char_height;
        if(p_data->max_item_width < p_item->width)
            p_data->max_item_width = p_item->width;

        p_item->item_action = va_arg(argp, P_FUNC);
        p_item->action_args = va_arg(argp, caddr_t);
    }
    va_end(argp);

    if(style == MENUBAR_STYLE)
    {
        height = p_data->max_item_height + 2 * GAP;
        width  = GAP;
        for(p_i = p_data->item_list; p_i != NULL; 
            p_i = p_i->next) width += p_i->width + GAP;
    }
    if(style == PULLDOWN_STYLE)
    {
        width = p_data->max_item_width + 2 * GAP;
        height = GAP;
        for(p_i = p_data->item_list; p_i != NULL; 
            p_i = p_i->next) height += p_i->height + GAP;
    }

/* Create the menu shell--the outer window */
    new_menu->xid = XCreateSimpleWindow(theDisplay, parent,
                      x, y, width, height, bdwidth, bdcolor, 
                      bgcolor);
    new_menu->data = p_data;
    new_menu->event_handler = menu_handler;
    new_menu->parent = parent;

/* Save new_menu as data associated with this window id
 * and the context "xwin_context"
 */
    if(XSaveContext(theDisplay, new_menu->xid, xwin_context,
                 (caddr_t) new_menu) != 0)
    {
        fprintf(stderr, "Error saving xwin_context data");
        exit(1);
    }
/* Select ButtonRelease events. No need to select
 * Expose events because we don't draw in the menu shell.
 */
    XSelectInput(theDisplay, new_menu->xid, ButtonReleaseMask);
                               
/* Create the buttons for all menu items */
    xb = GAP;
    yb = GAP;
    for(p_i = p_data->item_list; p_i != NULL; p_i = p_i->next)
    {
        if(style == PULLDOWN_STYLE) 
            width = p_data->max_item_width;
        else 
            width = p_i->width;
        p_i->p_button = MakeXButton(xb, yb, width-1, 
               p_i->height-1, 1, bdcolor, bgcolor, new_menu->xid, 
               p_i->item_text, p_i->item_action, 
               p_i->action_args);
        if(style == MENUBAR_STYLE) xb += p_i->width +  GAP;
        if(style == PULLDOWN_STYLE) yb += p_i->height + GAP;

/* Select additional events for each button */        
        AddNewEvent(p_i->p_button->xid,
                 EnterWindowMask | LeaveWindowMask | 
                 OwnerGrabButtonMask);
    }

/* Map menubars, but not pull-down menus */
    if(style == MENUBAR_STYLE)
        XMapWindow(theDisplay, new_menu->xid);

    return (new_menu);
}
/*-------------------------------------------------------------*/
/*  m e n u _ h a n d l e r
 *  
 *  Event handler for the "menu" 
 */
static int menu_handler(p_xwin)
XWIN *p_xwin;
{
    int    real_enter;
    D_ITEM *p_i;
    D_MENU *p_data = (D_MENU *) p_xwin->data;
    
/* Handle events delivered to this window */
    switch(theEvent.type)
    {
        case ButtonRelease:
            if(p_data->style == PULLDOWN_STYLE &&
               theEvent.xbutton.subwindow != None)
            {
                for(p_i = p_data->item_list; p_i != NULL;
                    p_i = p_i->next)
                {
                    if(p_i->p_button->xid ==
                       theEvent.xbutton.subwindow)
                    {
                        if (*(p_i->item_action) != NULL)
                            (*(p_i->item_action))
                                     (p_i->action_args);
                    }
                }
            }
            break;
    }
    return 0;
}
