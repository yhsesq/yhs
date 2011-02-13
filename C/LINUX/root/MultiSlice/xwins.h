/*  File: xwins.h
 *
 *  Defines data structures for windows
 *
 */
#ifndef XWINS_H
#define XWINS_H

#include <stdlib.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xresource.h>

typedef struct XWIN
{
    Window   xid;
    Window   parent;
    void     *data;
    int      (*event_handler)();
} XWIN;

/* Function prototypes */
void xwin_init(/* void */);
XWIN *MakeXButton(/* int x, int y,
      unsigned width, unsigned height, unsigned bdwidth,
      unsigned long bdcolor, unsigned long bgcolor,
      Window parent, char *text, int (*button_action)(),
      caddr_t action_data */);

XWIN *MakeXMenu(/*int x, int y, unsigned bdwidth,
      unsigned long bdcolor, unsigned long bgcolor,
      Window parent, int style,...*/);

/* Global variable */
#ifdef XWIN_UTIL

XContext      xwin_context;

#else

extern XContext       xwin_context;

#endif /* #ifdef XWIN_UTIL */

#endif /* #ifndef XWINS_H */
