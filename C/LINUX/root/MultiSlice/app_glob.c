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
/* File: app_glob.c
 *
 * Declare the common global variables for X applications.
 *
 */

#ifndef APP_GLOB_C  /* To ensure that it is included once */
#define APP_GLOB_C

#ifdef DEF_APP_GLOB      /* Defined in the initapp.c file */

XWMHints      *p_XWMH;     /* Hints for the window manager  */
XSizeHints    *p_XSH;      /* Size hints for window manager */
XClassHint    *p_CH;       /* Class hint for window manager */
XTextProperty WName;       /* Window name for title bar     */
XTextProperty IName;       /* Icon name for icon label      */
Display       *theDisplay; /* Connection to X display     */
GC            theGC;     /* The graphics context for main */
int           AppDone = 0;/* Flag to indicate when done   */
XEvent        theEvent;  /* Structure for current event   */
XFontStruct   *theFontStruct; /* Info on the default font */
unsigned long theBGpix,    /* Background and foreground   */ 
              theFGpix;    /* pixel values of main window */
char          *theAppName = " "; /* Application's name    */
Window        theMain;       /* Application's main window */
XWindowAttributes  
              MainXWA;       /* Attributes of main window */
#else

extern XWMHints       *p_XWMH;
extern XSizeHints     *p_XSH;
extern XClassHint     *p_CH;
extern XTextProperty  WName;
extern XTextProperty  IName;
extern  Display       *theDisplay;
extern  GC            theGC;
extern  int           AppDone;
extern  XEvent        theEvent;
extern  XFontStruct   *theFontStruct;   
extern  unsigned long theBGpix,      
                      theFGpix; 
extern  char          *theAppName;
extern  Window        theMain;
extern  XWindowAttributes 
                      MainXWA;

#endif    /* #ifdef DEF_APP_GLOB */
#endif    /* #ifndef APP_GLOB_C  */

