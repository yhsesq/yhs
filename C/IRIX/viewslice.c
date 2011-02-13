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


#include <stdio.h>
#include <string.h>            /* ANSI standard string library */
#include <stdlib.h>            /* Prototype of "calloc"        */

#include <X11/Xlib.h>
#include <X11/Xatom.h>         /* For definition of XA_WINDOW  */

#include "xwins.h"
#include "app_glob.c"


/* Scaling factor for colormap entries */
#define  CSCALE 255    

typedef char     *SCANLINE;
typedef SCANLINE *PLANE;

unsigned char  nplanes = 1;
unsigned short image_width  = 512,
               image_height = 512;
PLANE          *image = NULL;
int            bits_per_pixel = 8;
unsigned short bytes_per_line = 512;
unsigned char  bits_per_pixel_per_plane = 8;

/* Screen, visual and colormap */
int         theScreen;
Visual      *theVisual;
XVisualInfo *vis_list;
int         num_visuals,
            vis_depth;
int         colormap_size = 256;
Colormap    theColormap;

static GC   image_gc;

/* Image to be displayed by X server */
XImage         *theXImage = NULL;
unsigned char  *image_data = NULL;
XColor         *colors = NULL;
Pixmap         thePixmap;

/* File to be opened */
char *file_name;

/* Variables for the "quit" button */
static  char     *quit_label = "Quit";
static  unsigned quit_width, quit_height;

/* Function prototypes */

void initapp(/* int, char **, int, int, unsigned, unsigned */);
static int  quit_action(/* caddr_t */);
static void process_event();

static void swap2bytes(/* unsigned short *x */);
static int  adjust_index(/* int *bi, int *li, int *pi */);

static void load_file(/* char *filename */);
static void setup_disp();
static int  pick_visual(/* int depth_wanted, int class_wanted */);
static void setup_ximage();

/* Windows in the application */
Window dWin;

/*-------------------------------------------------------------*/
main(argc, argv)
int argc;
char **argv;
{
    XWIN                 *QuitButton, *which_xwin;
    XGCValues            xgcv;
    XSetWindowAttributes xswa;
    int                  i, win_depth;
    Atom                 ATOM_WM_COLMAP_WIN;
    
    xwin_init();
        
    initapp(argc, argv, 20, 20, 500, 400);

/* Get file name from command line */    
    for (i = 1; i < argc; i += 2) 
    {
        if(strcmp("-slice", argv[i]) == 0)
        {
            file_name = argv[i+1];
            break;
        }
    }
    if(i >= argc)
    {
        fprintf(stderr, "This file cannot be run from the command line.\n");
        exit(1);
    }

/* Load the image file */
    load_file(file_name);
    
/* Exit if we are not prepared to handle the image */
/* We only handle 1, 4, or 8-bit images */

    if(bits_per_pixel != 1 &&
       bits_per_pixel != 4 &&
       bits_per_pixel != 8) 
    {
        fprintf(stderr, "Cannot handle %d bits per pixel\n",
                bits_per_pixel);
        exit(1);
    }

/* Select a Visual and colormap for displaying the image */
    setup_disp();
    
/* Create the "Quit" button */
    quit_width = XTextWidth(theFontStruct, quit_label, 
                            strlen(quit_label)) + 4;
    quit_height = theFontStruct->max_bounds.ascent + 
                  theFontStruct->max_bounds.descent + 4;

    QuitButton = MakeXButton(1, 1, quit_width, quit_height,
                             1, theFGpix+1, theBGpix+5, theMain, 
                             quit_label, quit_action, NULL);

/* Create and map a window for displaying the image */
    if(bits_per_pixel == 1) 
        win_depth = CopyFromParent;
    else 
        win_depth = vis_depth;

    xswa.colormap = theColormap;
    xswa.background_pixel = theBGpix;
    xswa.border_pixel = theFGpix;

    dWin = XCreateWindow(theDisplay, theMain, 1,
           quit_height+8, image_width, image_height, 1, 
           win_depth, InputOutput, theVisual,
           CWColormap | CWBackPixel | CWBorderPixel,
           &xswa);

    XSelectInput(theDisplay, dWin, ExposureMask);

    XMapRaised(theDisplay, dWin);

/* Set the WM_COLORMAP_WINDOWS property so that the Motif 
 * window manager knows about the windows that have their
 * own colormap. In this case, we have only one such window.
 */
    if(theColormap != DefaultColormap(theDisplay, theScreen))
    {
        ATOM_WM_COLMAP_WIN = XInternAtom(theDisplay, 
                               "WM_COLORMAP_WINDOWS", False);
        XChangeProperty(theDisplay, theMain, ATOM_WM_COLMAP_WIN,
                        XA_WINDOW, 32, PropModeReplace,
                        (unsigned char *)&dWin, 1);
    }
        
/* Set up a GC to display image */
    image_gc = XCreateGC(theDisplay, dWin, 0, 0);
    XSetForeground(theDisplay, image_gc, theFGpix);
    XSetBackground(theDisplay, image_gc, theBGpix);

/* Prepare the image */
    setup_ximage();

/* Event handling loop--keep processing events until done */

    while (!AppDone)
        process_event();

/* Close connection to display and exit */

    XCloseDisplay(theDisplay);
    exit(0);
}
/*-------------------------------------------------------------*/
/*  p r o c e s s _ e v e n t 
 *  
 *  Retrieve an event and process it...
 */
static void process_event()
{
    XWIN     *which_xwin;

/* Get the next event from Xlib */
    XNextEvent(theDisplay, &theEvent);

/* Handle events for the dWin, the text display window */
    if(theEvent.xany.window == dWin)
    {
        switch (theEvent.type)
        {
            case Expose:
            {
/* Copy the exposed area from the pixmap to the window */
                XCopyArea(theDisplay, thePixmap, dWin, image_gc,
                    theEvent.xexpose.x, theEvent.xexpose.y,
                    theEvent.xexpose.width, 
                    theEvent.xexpose.height,
                    theEvent.xexpose.x, theEvent.xexpose.y);
                break;
            }
        }
    }
/************** Process events for other windows  *************/
/* Retrieve the XWIN data from Xlib using the context 
 * management routine XFindContext
 */
    if(XFindContext(theDisplay, theEvent.xany.window,
           xwin_context, (caddr_t *) &which_xwin) == 0)
    {
/* Call the event handler of this XWIN structure */
        if (*(which_xwin->event_handler) != NULL)
            (*(which_xwin->event_handler))(which_xwin);
    }
}
/*-------------------------------------------------------------*/
/*  q u i t _ a c t i o n
 *
 *  This routine is called when a ButtonPress event occurs in
 *  the quit window.
 */
static int quit_action(data)
caddr_t data;
{
/* Set the done flag */
    AppDone = 1;
    return 1;
}

static void load_file(filename)
char *filename;
{
    FILE     *p_file;
    int      numread=0, byte, i, j, 
             byteindex, lineindex, planeindex, count;
    long     imbytes, pcx_image_size;
    int cindex;

/* Open the file */
    if((p_file = fopen(filename, "r")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s\n", filename);
        exit(1);
    }


/* Allocate storage for decoding image */
    pcx_image_size = (long) nplanes *
                          (long) image_height *
                          (long) bytes_per_line;

    if((image = (PLANE *) calloc(nplanes, 
                      sizeof(PLANE))) == NULL)
    {
        fprintf(stderr,
                "Error allocating image...%d planes, %d bytes\n",
                nplanes, sizeof(PLANE));
        exit(1);
    }
    for(i = 0; i < nplanes; i++)
    {
        if((image[i] = (SCANLINE *) calloc(image_height, 
                            sizeof(SCANLINE))) == NULL)
        {
            fprintf(stderr, "Error allocating plane %d...\n", i);
            exit(1);
        }

        for(j = 0; j < image_height; j++)
        {
            if((image[i][j] = (SCANLINE) 
                      calloc(bytes_per_line, 
                             sizeof(unsigned char))) == NULL)
            {
                fprintf(stderr, 
                        "Error allocating line %d in plane %d\n", 
                        j, i);
                exit(1);
            }
        }
    }

/* Decode run-length encoded image data */
    lineindex = 0;
    byteindex = 0;
    planeindex = 0;
    imbytes = 0;
    while ((byte = getc(p_file)) != EOF)
    {
       image[planeindex][lineindex][byteindex] = byte;
       imbytes++;
       if (adjust_index(&byteindex, &lineindex, &planeindex))
          break;
       if(imbytes >= pcx_image_size) break;
    }
 
/* Create the XColor entries for the colormap */
        if((colors = (XColor *)calloc(colormap_size,
                              sizeof(XColor))) == NULL)
        {
            fprintf(stderr, "No memory for setting up colormap\n");
            exit(1);
        }
        for(cindex = 0; cindex < colormap_size; cindex++)
        {
                colors[cindex].red       = CSCALE * cindex;
                colors[cindex].green     = CSCALE * cindex;
                colors[cindex].blue      = CSCALE * cindex;
                colors[cindex].pixel     = cindex;
                colors[cindex].flags     = DoRed | DoGreen | DoBlue;
        }

/* Close file and return */
    fclose(p_file);
}

/*-------------------------------------------------------------*/
/*  a d j u s t _ i n d e x
 *
 *  Adjusts the indices as image data is decoded.
 *
 */
static int adjust_index(bi, li, pi)
int *bi, *li, *pi;
{
    int status = 0;
    *bi += 1;
    if(*bi == bytes_per_line)
    {
        *bi = 0;
        *pi += 1;
        if(*pi == nplanes)
        {
            *pi = 0;
            *li += 1;
            if(*li == image_height)
            {
                printf("%d raster lines read.\n", *li);
                status = 1;
            }
        }
    }
    return (status);
}
/*-------------------------------------------------------------*/
static void setup_disp()
{
    XVisualInfo vis_template;
    int         i;
    
    theScreen = DefaultScreen(theDisplay);
    
    if(bits_per_pixel != 1) 
    {
/* Get a list of all visuals for this screen */
        vis_template.screen = theScreen;
        vis_list = XGetVisualInfo(theDisplay, 
                          VisualScreenMask, &vis_template,
                          &num_visuals);
        if(num_visuals == 0)
        {
            fprintf(stderr, "No visuals found!\n");
            exit(0);
        }

/* Search for a PseudoColor visual with depth >= image depth.
 * You may want other strategies here--for visuals such
 * as "DirectColor" or for StaticColor with 8 or more
 * bit planes. 
 */
        if(!pick_visual(bits_per_pixel, PseudoColor))
        {
            fprintf(stderr, "No appropriate visual...Exiting\n");
            exit(0);
        }

/* Create the colormap */
        theColormap = XCreateColormap(theDisplay, 
                          RootWindow(theDisplay, theScreen),
                          theVisual, AllocAll);

/* Store the colors into the colormap */
         XStoreColors(theDisplay, theColormap, colors, 
                      colormap_size); 

/* Now we can release the memory used by the colors
 * because the X server already has this information.
 */
         free(colors);
    }
    else
    {
/* Image is monochrome. Handle using default visual */
        theVisual = DefaultVisual(theDisplay, theScreen);
        theColormap = DefaultColormap(theDisplay, theScreen);
        vis_depth = DefaultDepth(theDisplay, theScreen);
    }
}
/*-------------------------------------------------------------*/
/*  p i c k _ v i s u a l
 *
 *  Select a visual of appropriate "class" and "depth" from
 *  the list of visuals. Return 1 if successful, 0 if no
 *  matching visuals found.
 */
static int pick_visual(depth_wanted, class_wanted)
int depth_wanted, class_wanted;
{
    XVisualInfo *p_visinfo;
    int         i, status = 0;
    
    for(i = 0, p_visinfo = vis_list; i < num_visuals; 
        i++, p_visinfo++)
    {
        if(p_visinfo->class == class_wanted  &&
           p_visinfo->depth >= depth_wanted)
        {
            theVisual = p_visinfo->visual;
            vis_depth = p_visinfo->depth;
            status = 1;
            break;
        }
    }
    return (status);
}
/*-------------------------------------------------------------*/
/*  s e t u p _ x i m a g e
 *
 *  Yes, we already have the decoded image in memory, but now
 *  we need it in a form usable by Xlib. This function creates
 *  the image data and sets up an XImage. It creates a Pixmap
 *  in the server and copies the image over using XPutImage.
 *  Once this is done, the image can be drawn by copying from
 *  the pixmap into the window (using XCopyArea).
 * 
 *  Note that whether monochrome or not, the image is created
 *  at the depth of the selected visual.
 *
 */
static void setup_ximage()
{
    int           i, j, k, l, planeindex, select,
                  bytes_per_vis_depth, pixval_per_byte, 
                  xim_width, pixpac_count = 0, curbyte = 0, 
                  bits_copied = 0;
    unsigned long pixval;
    unsigned int  image_bytes;

/* Compute some sizes */
    bytes_per_vis_depth = (vis_depth + 7) / 8;
    pixval_per_byte = (8/vis_depth);    
    xim_width = 8 * ((image_width+7)/8);
    image_bytes = xim_width * bytes_per_vis_depth * image_height;

    if(pixval_per_byte > 1) image_bytes /= pixval_per_byte;
    
/* Allocate room for image's data */
    if((image_data = (unsigned char *) 
                     calloc(image_bytes,
                            sizeof(unsigned char) )) == NULL)
    {
        fprintf(stderr, "Error allocating room for image...\n");
        exit(1);
    }

/* Set up image data in MSBFirst, ZPixmap format */
    if(nplanes == 1 && 
       bits_per_pixel_per_plane == 8)
    {
/* Handle 8-bit images in straightforward manner */
        for(j=0; j < image_height; j++)
	{
            for(i = 0; i < image_width; i++)
	    {
                image_data[curbyte] = image[0][j][i];
                curbyte++;
	    }
            if(xim_width > image_width)
            {
                for(k = image_width; k < xim_width; k++)
                    curbyte++;
	    }
	}
    }
    else
    {
        for(j = 0; j < image_height; j++)
        {
            for(i = 0; i< (image_width+7)/8; i++)
            {
                select = 0x80;
/* The following for loop extracts bits from each byte */
                for(k = 0; k < 8; k++)
                {
/* First construct the pixel value */
                    pixval =  (image[0][j][i] & select) > 0;
    
                    if(nplanes  == 1)
                    {
/* For monochrome images, translate 1 and 0 into White and
 * Black pixel values
 */
                        if(pixval == 0)
                            pixval = theFGpix;
                        else
                            pixval = theBGpix;
                    }
                    if(nplanes  > 1)
                    {
                        for(planeindex=1; 
                            planeindex < nplanes; 
                            planeindex++)
                        {
/* Add other planes to the pixel value */
                             pixval = pixval << 1;
                             pixval |=  (image[planeindex][j][i] & 
                                        select) > 0;
                        }
                   }

/* Copy pixel value into image's data array */
                    for(l = 0; l < bytes_per_vis_depth; l++)
                    {
                        if(pixval_per_byte > 1)
                        {
/* CASE A: more than one pixel value fits in a byte */
                            image_data[curbyte] |= 
                                (pixval & 0xff) << 
                                (8 - pixpac_count * vis_depth);
printf("'");
                            pixpac_count++;
                            if(pixpac_count == pixval_per_byte)
                            {
                               pixpac_count = 0;
                                curbyte++;
                            }
                        }
                        else
                        {
/* CASE B: more than one byte per pixel value */
                            if(bits_copied + 8 <= vis_depth)
                            {
                                image_data[curbyte] = pixval & 0xff;
                                bits_copied += 8;
                            }
                            else
                            {
                                image_data[curbyte] = 
                                    (pixval & 0xff) << 
                                    (vis_depth - bits_copied);
                                bits_copied = 0;
                            }
                            pixval >>= 8;
                            curbyte++;
                        }
                    }
/* Go to next bit in current byte from each bit plane */
                    select = select >> 1;
                }
            }
        }
    }
    
/* Set up an XImage structure in ZPixmap format */
    theXImage = XCreateImage(theDisplay, theVisual, vis_depth, 
                    ZPixmap, 0, (char *)image_data, xim_width,
                    image_height, 8, 0);

    theXImage->byte_order = MSBFirst;

/* Create a Pixmap and copy image into it */
    thePixmap = XCreatePixmap(theDisplay, dWin, theXImage->width,
                    theXImage->height, vis_depth);

    XPutImage(theDisplay, thePixmap, image_gc, theXImage, 
              0, 0, 0, 0, theXImage->width, theXImage->height);

/* Now we can destroy the image because the information is in
 * the pixmap in the X server 
 */
    XDestroyImage(theXImage);
}
