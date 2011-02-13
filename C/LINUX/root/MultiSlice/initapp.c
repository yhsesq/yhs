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
/*  File: initapp.c
 *  
 *  Function to process command-line arguments, connect to the
 *  X display, and set up the main window of a generic
 *  X application.
 *
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xresource.h>

#define  DEF_APP_GLOB    /* so that globals get defined */
#include "app_glob.c"

#define max(x,y)     (((x) > (y)) ? (x) : (y))

#define DEFAULT_BGCOLOR         "white"
#define DEFAULT_FGCOLOR         "black"
#define DEFAULT_BDWIDTH         1
#define DEFAULT_FONT            "fixed"

typedef struct APP_PARAMS
{
    char *name;
    char *cname;
    char **p_value_string;
} APP_PARAMS;

/* Default parameter values (as strings) */
char *theBGcolor     = DEFAULT_BGCOLOR,
     *theFGcolor     = DEFAULT_FGCOLOR,
     *theFont        = DEFAULT_FONT,
     *theGeom_rsrc   = NULL,
     *display_name   = NULL,
     *theGeom_cline  = NULL,
     *theGeom        = NULL;

/* List of user-customizable resources */

APP_PARAMS app_resources[] =
{
    "background", "Background",   &theBGcolor,  
    "foreground", "Foreground",   &theFGcolor,
    "font",       "Font",         &theFont,
    "geometry",   "Geometry",     &theGeom_rsrc,
};

int num_resources = sizeof(app_resources) / sizeof(APP_PARAMS);

/* List of command-line options */

APP_PARAMS app_options[] =
{
    "-display",  "-d", &display_name,
    "-geometry", "-g", &theGeom_cline
};

int num_options = sizeof(app_options) / sizeof(APP_PARAMS);

void usage();
static char rname[80];
/*-------------------------------------------------------------*/
void initapp(argc, argv, x, y, width, height)
int argc, x, y;
char **argv; 
unsigned width,  height;
{
    int                  i, j;
    char                 *tmpstr;
    Colormap             default_cmap;
    XColor               color;
    int                  bitmask;
    XGCValues            gcv;
    XSetWindowAttributes xswa;
    char                 default_geometry[80];
    char                 rfilename[80];
    char                 *type;
    XrmValue             value;
    XrmDatabase          rdb;

    theAppName = argv[0];
    AppDone = 0;
    
/* Parse command-line options. Since each option has a value,
 * they are processed in pairs.
 */
    for (i = 1; i < argc; i += 2) 
    {
        for(j = 0; j < num_options; j++)
        {
            if(strcmp(app_options[j].name, argv[i]) == 0 ||
               strcmp(app_options[j].cname, argv[i]) == 0)
            {
                *app_options[j].p_value_string = argv[i+1];
                break;
            }
        }
        if(j >= num_options)
            usage();
    }

/*  Open connection to display selected by user */

    if ((theDisplay = XOpenDisplay(display_name)) == NULL) 
    {
        fprintf(stderr, "%s: can't open display named %s\n", 
                argv[0], XDisplayName(display_name));
        exit(1);
    }

/* Get resources from the resource file */
    XrmInitialize();
    strcpy(rfilename, getenv("HOME"));
    strcat(rfilename, "/.Xdefaults");
    rdb = XrmGetFileDatabase(rfilename);
    if(rdb != NULL) 
    {
        for (i = 0; i < num_resources; i++)
        {
/* Construct a complete resource name by appending the
 * resource name to the application's name.
 */
            strcpy(rname, theAppName);
            strcat(rname, "*");
            strcat(rname, app_resources[i].name);

            if(XrmGetResource(rdb, rname, rname,
                              &type, &value))
            {
                *app_resources[i].p_value_string = value.addr;
	    }
        }
    }

/* Set up colors and fonts */

    if ((theFontStruct = XLoadQueryFont(theDisplay, 
                                           theFont)) == NULL) 
    {
        fprintf(stderr, "%s: display %s cannot load font %s\n",
                theAppName, DisplayString(theDisplay), theFont);
        exit(1);
    }

/* Now select the colors using the default colormap */

    default_cmap = DefaultColormap(theDisplay, 
                                   DefaultScreen(theDisplay));

/* Main window's background color */
    if (XParseColor(theDisplay, default_cmap, theBGcolor, 
                    &color) == 0 ||
        XAllocColor(theDisplay, default_cmap, &color) == 0)

/* Use white background in case of failure */
        theBGpix = WhitePixel(theDisplay, 
                              DefaultScreen(theDisplay));
    else
        theBGpix = color.pixel;

/* Main window's foreground color */
    if (XParseColor(theDisplay, default_cmap, theFGcolor, 
                    &color) == 0 ||
        XAllocColor(theDisplay, default_cmap, &color) == 0)
/* Use black foreground in case of failure */
        theFGpix = BlackPixel(theDisplay, 
                              DefaultScreen(theDisplay));
    else
        theFGpix = color.pixel;

/* Fill out a XsizeHints structure to inform the window manager
 * of desired size and location of main window.
 */
    if((p_XSH = XAllocSizeHints()) == NULL)
    {
        fprintf(stderr, "Error allocating size hints!\n");
        exit(1);
    }
    p_XSH->flags = (PPosition | PSize | PMinSize);
    p_XSH->height = height;
    p_XSH->min_height = p_XSH->height;
    p_XSH->width = width;
    p_XSH->min_width = p_XSH->width;
    p_XSH->x = x;
    p_XSH->y = y;

/* Construct a default geometry string */
    sprintf(default_geometry, "%dx%d+%d+%d", p_XSH->width,
            p_XSH->height, p_XSH->x, p_XSH->y);
    theGeom = default_geometry;

/* Override the geometry, if necessary */
    if(theGeom_cline != NULL) theGeom = theGeom_cline;
    else
        if(theGeom_rsrc != NULL) theGeom = theGeom_rsrc;

/* Process the geometry specification */
    bitmask =  XGeometry(theDisplay, DefaultScreen(theDisplay), 
                   theGeom, default_geometry, DEFAULT_BDWIDTH, 
                   1, 1, 0, 0, &(p_XSH->x), &(p_XSH->y),
                   &(p_XSH->width), &(p_XSH->height));
        
/* Check bitmask and set flags in XSizeHints structure */
    if (bitmask & (XValue | YValue)) p_XSH->flags |= USPosition;
    if (bitmask & (WidthValue | HeightValue))
                                     p_XSH->flags |= USSize;

/* Create the main window using the position and size 
 * information derived above. For border color, use the 
 * foreground color.
 */
    theMain = XCreateSimpleWindow(theDisplay, 
                    DefaultRootWindow(theDisplay), 
                    p_XSH->x, p_XSH->y, p_XSH->width, p_XSH->height, 
                    DEFAULT_BDWIDTH, theFGpix, theBGpix);

/* Set up class hint */
    if((p_CH = XAllocClassHint()) == NULL)
    {
        fprintf(stderr, "Error allocating class hint!\n");
        exit(1);
    }
    p_CH->res_name = theAppName;
    p_CH->res_class = theAppName;

/* Set up XTextProperty for window name and icon name */
    if(XStringListToTextProperty(&theAppName, 1, &WName) == 0)
    {
        fprintf(stderr, "Error creating XTextProperty!\n");
        exit(1);
    }
    if(XStringListToTextProperty(&theAppName, 1, &IName) == 0)
    {
        fprintf(stderr, "Error creating XTextProperty!\n");
        exit(1);
    }

    if((p_XWMH = XAllocWMHints()) == NULL)
    {
        fprintf(stderr, "Error allocating Window Manager hints!\n");
        exit(1);
    }
    p_XWMH->flags = (InputHint|StateHint);
    p_XWMH->input = False;
    p_XWMH->initial_state = NormalState;
    XSetWMProperties(theDisplay, theMain, &WName, &IName, argv, argc,
                     p_XSH, p_XWMH, p_CH);

/* Finally, create a graphics context for the main window */    

    gcv.font = theFontStruct->fid;
    gcv.foreground = theFGpix;
    gcv.background = theBGpix;
    theGC = XCreateGC(theDisplay, theMain, 
                (GCFont | GCForeground | GCBackground), &gcv);

/* Set main window's attributes (colormap, bit_gravity) */

    xswa.colormap = DefaultColormap(theDisplay, 
                                    DefaultScreen(theDisplay));
    xswa.bit_gravity = NorthWestGravity;
    XChangeWindowAttributes(theDisplay, theMain, (CWColormap | 
                            CWBitGravity), &xswa);

/* Select Exposure events for the main window */

    XSelectInput(theDisplay, theMain, ExposureMask);

/* Map the main window */

    XMapWindow(theDisplay, theMain);
    XFlush(theDisplay);
    if(XGetWindowAttributes(theDisplay, theMain, &MainXWA) == 0)
    {
        fprintf(stderr, "Error getting attributes of Main.\n");
        exit(2);
    }
}
/*-------------------------------------------------------------*/
void usage ()
{
    fprintf (stderr, "%s [-display name] [-geometry geom]\n",
             theAppName);
/*    exit (1); */
}


