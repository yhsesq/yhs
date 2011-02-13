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


/*  File:  xwinutil.c
 *
 *  Utility functions for Xlib programming.
 */
 
#define XWIN_UTIL

#include "xwins.h"
#include "app_glob.c"

/*-------------------------------------------------------------*/
void xwin_init()
{
/* Create a unique context to reference data associated
 * with the windows
 */
    xwin_context = XUniqueContext();

}
/*-------------------------------------------------------------*/
/*  A d d N e w E v e n t
 *
 *  Add a new event to a window's event mask.
 *
 */
void AddNewEvent(xid, event_mask)
Window xid;
long event_mask;
{
    XWindowAttributes     xwa;
    XSetWindowAttributes  xswa;

    if (XGetWindowAttributes(theDisplay, xid, &xwa) != 0)
    {
        xswa.event_mask = xwa.your_event_mask | event_mask;
                             
        XChangeWindowAttributes(theDisplay, xid,
                                CWEventMask, &xswa);
    }
}
/*-------------------------------------------------------------*/
/*  I g n o r e E v e n t
 *
 *  Stop handling an event (by altering the window's mask)
 *
 */
void IgnoreEvent(xid, event_mask)
Window xid;
long event_mask;
{
    XWindowAttributes     xwa;
    XSetWindowAttributes  xswa;

    if (XGetWindowAttributes(theDisplay, xid, &xwa) != 0)
    {
        xswa.event_mask = xwa.your_event_mask & (~event_mask);

        XChangeWindowAttributes(theDisplay, xid,
                                CWEventMask, &xswa);

    }
}
