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


/* $Revision: 1.5 $ */
/* compile: cc -o textfun textfun.c -lXm -lXt -lGL -lXext -lX11 -lm */

/*
 * textfun demonstrates pulling X bitmap fonts from the X server into an
 * OpenGL client and converting the bitmaps into OpenGL display lists with
 * transformable geometry.  Text can then be displayed from any perspective
 * in 3D.
 * 
 * Motif is used for the user interface.  The program renders OpenGL into a
 * standard Motif drawing area and does not use any special OpenGL widget.
 * Pull down menus with toggles and radio buttons are used.  The animation is
 * controled by X Toolkit work procs; iconfiying textfun will stop the work
 * proc and resume it when the program is uniconified.
 * 
 * Various fonts can be switched between.  A number of the fonts are X scalable
 * fonts demonstrating how the blocky nature of the text can be minimized
 * with higher resolution fonts.
 * 
 * Mark J. Kilgard
 * mjk@sgi.com
 * Silicon Graphics, Inc.
 * March 7, 1994
 */

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <math.h>
#ifdef IRIX_5_1_MOTIF_BUG_WORKAROUND
#include <sys/utsname.h>
#endif
#include <Xm/MainW.h>
#include <Xm/RowColumn.h>
#include <Xm/PushB.h>
#include <Xm/ToggleB.h>
#include <Xm/CascadeB.h>
#include <Xm/Frame.h>
#include <Xm/DrawingA.h>
#include <X11/keysym.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glx.h>

#ifdef DEBUG
#define GL_ERROR_CHECK() \
    { /* for help debugging, report any OpenGL errors that occur per frame */ \
        GLenum error; \
        while((error = glGetError()) != GL_NO_ERROR) \
            fprintf(stderr, "GL error: %s, line %d\n", gluErrorString(error), __LINE__); \
    }
#else
#define GL_ERROR_CHECK() { /* nothing */ }
#endif

typedef struct {
    short           width;
    short           height;
    short           xoffset;
    short           yoffset;
    short           advance;
    char           *bitmap;
}               PerCharInfo, *PerCharInfoPtr;

typedef struct {
    int             min_char;
    int             max_char;
    int             max_ascent;
    int             max_descent;
    GLuint          dlist_base;
    PerCharInfo     glyph[1];
}               FontInfo, *FontInfoPtr;

typedef struct {
    char           *name;
    char           *xlfd;
    XFontStruct    *xfont;
    FontInfoPtr     fontinfo;
}               FontEntry, *FontEntryPtr;

static int      dblBuf[] =
{
    GLX_DOUBLEBUFFER, GLX_RGBA, GLX_DEPTH_SIZE, 16,
    GLX_RED_SIZE, 1, GLX_GREEN_SIZE, 1, GLX_BLUE_SIZE, 1,
    None
};
static int     *snglBuf = &dblBuf[1];

static String   fallbackResources[] =
{
    "*sgiMode: true",		/* try to enable IRIX 5.2+ look & feel */
    "*useSchemes: all",		/* and SGI schemes */
    "*title: OpenGL text transformation",
    "*glxarea*width: 300", "*glxarea*height: 300", NULL
};

static FontEntry fontEntry[] =
{
    {"Fixed", "fixed", NULL, NULL},
    {"Utopia", "-adobe-utopia-medium-r-normal--20-*-*-*-p-*-iso8859-1", NULL, NULL},
    {"Schoolbook", "-adobe-new century schoolbook-bold-i-normal--20-*-*-*-p-*-iso8859-1", NULL, NULL},
    {"Rock", "-sgi-rock-medium-r-normal--20-*-*-*-p-*-iso8859-1", NULL, NULL},
    {"Rock (hi-res)", "-sgi-rock-medium-r-normal--50-*-*-*-p-*-iso8859-1", NULL, NULL},
    {"Curl", "-sgi-curl-medium-r-normal--20-*-*-*-p-*-*-*", NULL, NULL},
    {"Curl (hi-res)", "-sgi-curl-medium-r-normal--50-*-*-*-p-*-*-*", NULL, NULL},
    {"Dingbats", "-adobe-itc zapf dingbats-medium-r-normal--35-*-*-*-p-*-adobe-fontspecific", NULL, NULL}
};
#define NUM_FONT_ENTRIES sizeof(fontEntry)/sizeof(FontEntry)

static char    *defaultMessage[] =
{"MultiSlice ", "RTP", "with OpenGL!!"};
#define NUM_DEFAULT_MESSAGES sizeof(defaultMessage)/sizeof(char*)

Display        *dpy;
GLboolean       doubleBuffer = GL_TRUE, motion = GL_FALSE, rotation = GL_FALSE,
                wobbling = GL_FALSE, made_current = GL_FALSE, dollying = GL_TRUE;
XtAppContext    app;
XtWorkProcId    workId = 0;
Widget          toplevel = NULL;
Widget		mainw, menubar, menupane, btn, cascade, frame, glxarea;
GLXContext      cx;
XVisualInfo    *vi;
Colormap        cmap;
Arg             menuPaneArgs[4], args[1];
GLfloat         theta = 0, delta = 0;
GLfloat         distance = 19, angle = 0, wobble_angle = 0;
GLuint          base;
int             numMessages;
char          **messages;

void 
draw(Widget w)
{
    GLfloat         red, green, blue;
    int             i;

    glClear(GL_DEPTH_BUFFER_BIT);

    /* paint black to blue smooth shaded polygon for background */
    glDisable(GL_DEPTH_TEST);
    glShadeModel(GL_SMOOTH);
    glBegin(GL_POLYGON);
    glColor3f(1.0, 1.0, 1.0);
    glVertex3f(-20, 20, -19);
    glVertex3f(20, 20, -19);
    glColor3f(0.0, 0.0, 1.0);
    glVertex3f(20, -20, -19);
    glVertex3f(-20, -20, -19);
    glEnd();

    glEnable(GL_DEPTH_TEST);
    glShadeModel(GL_FLAT);

    glPushMatrix();
    glTranslatef(0, 0, -distance);
    glRotatef(angle, 0, 0, 1);
    glRotatef(wobble_angle, 0, 1, 0);
    glCallList(base);
    glPopMatrix();

    if (doubleBuffer)
	glXSwapBuffers(dpy, XtWindow(w));
    if (!glXIsDirect(dpy, cx))
	glFinish();		/* avoid indirect rendering latency from
				 * queuing */
    GL_ERROR_CHECK();
}

void 
resize(Widget w, XtPointer data, XtPointer callData)
{
    Dimension       width, height;

    /*
     * It is possible for a drawing area widget's resize callback to be
     * called before the window is realized, and therefore before we have
     * made our OpenGL context to the window ID.  So only let the glViewPort
     * call happen if we really have "made_current".
     */
    if (made_current) {
	XtVaGetValues(w, XmNwidth, &width, XmNheight, &height, NULL);
	glViewport(0, 0, (GLint) width, (GLint) height);
    }
}

void 
tick(void)
{
    if (dollying) {
	theta += 0.1;
	distance = cos(theta) * 7 + 12;
    }
    if (rotation)
	angle -= 6;
    if (wobbling) {
	delta += 0.1;
	wobble_angle = sin(delta) * 40;
    }
}

Boolean 
animate(XtPointer data)
{
    tick();
    draw(glxarea);
    return False;		/* leave work proc active */
}

void 
syncstate(void)
{
    if (motion && (dollying || rotation || wobbling)) {
	if (workId == 0)
	    workId = XtAppAddWorkProc(app, animate, NULL);
    } else if (workId != 0) {
	XtRemoveWorkProc(workId);
	workId = 0;
    }
}

void 
toggle(void)
{
    motion = !motion;		/* toggle */
    syncstate();
}

void 
dolly(void)
{
    dollying = !dollying;	/* toggle */
    syncstate();
}

void 
rotate(void)
{
    rotation = !rotation;	/* toggle */
    syncstate();
}

void 
wobble(void)
{
    wobbling = !wobbling;	/* toggle */
    syncstate();
}

void 
quit(Widget w, XtPointer data, XtPointer callData)
{
    exit(0);
}

void 
input(Widget w, XtPointer data, XtPointer callData)
{
    XmDrawingAreaCallbackStruct *cd = (XmDrawingAreaCallbackStruct *) callData;
    char            buf[1];
    KeySym          keysym;
    int             rc;

    if (cd->event->type == KeyPress)
	if (XLookupString((XKeyEvent *) cd->event, buf, 1, &keysym, NULL) == 1)
	    switch (keysym) {
	    case XK_space:
		if (!motion) {	/* advance one frame if not in motion */
		    tick();
		    draw(w);
		}
		break;
	    case XK_Escape:
		exit(0);
	    }
}

void 
map_state_changed(Widget w, XtPointer data, XEvent * event, Boolean * cont)
{
    switch (event->type) {
	case MapNotify:
	syncstate();
	break;
    case UnmapNotify:
	if (motion) {
	    XtRemoveWorkProc(workId);
	    workId = 0;
        }
	break;
    }
}

/* #define REPORT_GLYPHS */
#ifdef REPORT_GLYPHS
#define DEBUG_GLYPH4(msg,a,b,c,d) printf(msg,a,b,c,d)
#define DEBUG_GLYPH(msg) printf(msg)
#else
#define DEBUG_GLYPH4(msg,a,b,c,d) { /* nothing */ }
#define DEBUG_GLYPH(msg) { /* nothing */ }
#endif

#define MAX_GLYPHS_PER_GRAB 512 /* this is big enough for 2^9 glyph character sets */

FontInfoPtr
SuckGlyphsFromServer(Display * dpy, Font font)
{
    Pixmap          offscreen;
    XFontStruct    *fontinfo;
    XImage         *image;
    GC              xgc;
    XGCValues       values;
    int             numchars;
    int             width, height, pixwidth;
    int             i, j;
    XCharStruct    *charinfo;
    XChar2b         character;
    char           *bitmapData;
    int             x, y;
    int             spanLength;
    int             charWidth, charHeight, maxSpanLength;
    int             grabList[MAX_GLYPHS_PER_GRAB];
    int             glyphsPerGrab = MAX_GLYPHS_PER_GRAB;
    int             numToGrab, thisglyph;
    FontInfoPtr     myfontinfo;

    fontinfo = XQueryFont(dpy, font);
    if (!fontinfo)
	return NULL;

    numchars = fontinfo->max_char_or_byte2 - fontinfo->min_char_or_byte2 + 1;
    if (numchars < 1)
	return NULL;

    myfontinfo = (FontInfoPtr) malloc(sizeof(FontInfo) + (numchars - 1) * sizeof(PerCharInfo));
    if (!myfontinfo)
	return NULL;

    myfontinfo->min_char = fontinfo->min_char_or_byte2;
    myfontinfo->max_char = fontinfo->max_char_or_byte2;
    myfontinfo->max_ascent = fontinfo->max_bounds.ascent;
    myfontinfo->max_descent = fontinfo->max_bounds.descent;
    myfontinfo->dlist_base = 0;

    width = fontinfo->max_bounds.rbearing - fontinfo->min_bounds.lbearing;
    height = fontinfo->max_bounds.ascent + fontinfo->max_bounds.descent;

    maxSpanLength = (width + 7) / 8;
    /*
     * Be careful determining the width of the pixmap; the X protocol allows
     * pixmaps of width 2^16-1 (unsigned short size) but drawing coordinates
     * max out at 2^15-1 (signed short size).  If the width is too large,
     * we need to limit the glyphs per grab.
     */
    if ((glyphsPerGrab * 8 * maxSpanLength) >= (1 << 15)) {
	glyphsPerGrab = (1 << 15) / (8 * maxSpanLength);
    }
    pixwidth = glyphsPerGrab * 8 * maxSpanLength;
    offscreen = XCreatePixmap(dpy, RootWindow(dpy, DefaultScreen(dpy)),
			      pixwidth, height, 1);

    values.font = font;
    values.background = 0;
    values.foreground = 0;
    xgc = XCreateGC(dpy, offscreen, GCFont | GCBackground | GCForeground, &values);

    XFillRectangle(dpy, offscreen, xgc, 0, 0, 8 * maxSpanLength * glyphsPerGrab, height);
    XSetForeground(dpy, xgc, 1);

    numToGrab = 0;
    if (fontinfo->per_char == NULL) {
	charinfo = &(fontinfo->min_bounds);
	charWidth = charinfo->rbearing - charinfo->lbearing;
	charHeight = charinfo->ascent + charinfo->descent;
	spanLength = (charWidth + 7) / 8;
    }
    for (i = 0; i < numchars; i++) {
	if (fontinfo->per_char != NULL) {
	    charinfo = &(fontinfo->per_char[i]);
	    charWidth = charinfo->rbearing - charinfo->lbearing;
	    charHeight = charinfo->ascent + charinfo->descent;
	    if (charWidth == 0 || charHeight == 0) {
		/* Still must move raster pos even if empty character */
		myfontinfo->glyph[i].width = 0;
		myfontinfo->glyph[i].height = 0;
		myfontinfo->glyph[i].xoffset = 0;
		myfontinfo->glyph[i].yoffset = 0;
		myfontinfo->glyph[i].advance = charinfo->width;
		myfontinfo->glyph[i].bitmap = NULL;
		goto PossiblyDoGrab;
	    }
	}
	grabList[numToGrab] = i;

	/* XXX is this right for large fonts? */
	character.byte2 = (i + fontinfo->min_char_or_byte2) & 255;
	character.byte1 = (i + fontinfo->min_char_or_byte2) >> 8;

	/*
	 * XXX we could use XDrawImageString16 which would also paint the
	 * backing rectangle but X server bugs in some scalable font
	 * rasterizers makes it more effective to do XFillRectangles to clear
	 * the pixmap and XDrawImage16 for the text.
	 */
	XDrawString16(dpy, offscreen, xgc,
		      -charinfo->lbearing + 8 * maxSpanLength * numToGrab,
		      charinfo->ascent, &character, 1);

	numToGrab++;

      PossiblyDoGrab:

	if (numToGrab >= glyphsPerGrab || i == numchars - 1) {
	    image = XGetImage(dpy, offscreen,
		  0, 0, pixwidth, height, 1, XYPixmap);
	    for (j = 0; j < numToGrab; j++) {
		thisglyph = grabList[j];
		if (fontinfo->per_char != NULL) {
		    charinfo = &(fontinfo->per_char[thisglyph]);
		    charWidth = charinfo->rbearing - charinfo->lbearing;
		    charHeight = charinfo->ascent + charinfo->descent;
		    spanLength = (charWidth + 7) / 8;
		}
		bitmapData = calloc(height * spanLength, sizeof(char));
		if (!bitmapData)
		    goto FreeFontAndReturn;
                DEBUG_GLYPH4("index %d, glyph %d (%d by %d)\n",
		    j, thisglyph + fontinfo->min_char_or_byte2, charWidth, charHeight);
		for (y = 0; y < charHeight; y++) {
		    for (x = 0; x < charWidth; x++) {
			/*
			 * XXX The algorithm used to suck across the font ensures
			 * that each glyph begins on a byte boundary.  In theory
			 * this would make it convienent to copy the glyph into
			 * a byte oriented bitmap.  We actually use the XGetPixel
			 * function to extract each pixel from the image which is
			 * not that efficient.  We could either do tighter packing
			 * in the pixmap or more efficient extraction from the
			 * image.  Oh well.
			 */
			if (XGetPixel(image, j * maxSpanLength * 8 + x, charHeight - 1 - y)) {
			    DEBUG_GLYPH("x");
			    bitmapData[y * spanLength + x / 8] |= (1 << (x & 7));
			} else {
			    DEBUG_GLYPH(" ");
			}
		    }
		    DEBUG_GLYPH("\n");
		}
		myfontinfo->glyph[thisglyph].width = charWidth;
		myfontinfo->glyph[thisglyph].height = charHeight;
		myfontinfo->glyph[thisglyph].xoffset = -charinfo->lbearing;
		myfontinfo->glyph[thisglyph].yoffset = charinfo->descent;
		myfontinfo->glyph[thisglyph].advance = charinfo->width;
		myfontinfo->glyph[thisglyph].bitmap = bitmapData;
	    }
	    XDestroyImage(image);
	    numToGrab = 0;
	    /* do we need to clear the offscreen pixmap to get more? */
	    if (i < numchars - 1) {
		XSetForeground(dpy, xgc, 0);
		XFillRectangle(dpy, offscreen, xgc, 0, 0, 8 * maxSpanLength * glyphsPerGrab, height);
		XSetForeground(dpy, xgc, 1);
	    }
	}
    }
    XFreeGC(dpy, xgc);
    XFreePixmap(dpy, offscreen);
    return myfontinfo;

  FreeFontAndReturn:
    XDestroyImage(image);
    XFreeGC(dpy, xgc);
    XFreePixmap(dpy, offscreen);
    for (j = i - 1; j >= 0; j--) {
	if (myfontinfo->glyph[j].bitmap)
	    free(myfontinfo->glyph[j].bitmap);
    }
    free(myfontinfo);
    return NULL;
}

void
MakeCube(void)
{
    /*
     * No back side to the cube is drawn since the animation makes sure the
     * back side can never be visible.  The "wobble" function is constrained
     * so not to rotate far enough around to reveal the back side.
     */
    glNewList(1, GL_COMPILE);
    glBegin(GL_QUAD_STRIP);
    /* back left post */
    glColor3f(6.0, 0.5, 0.5);
    glVertex3f(0, 0, 0);
    glVertex3f(0, 1, 0);
    /* front left post */
    glVertex3f(0, 0, 1);
    glVertex3f(0, 1, 1);
    glColor3f(1.0, 0.0, 0.0);
    /* front right post */
    glVertex3f(1, 0, 1);
    glVertex3f(1, 1, 1);
    /* back right post */
    glColor3f(6.0, 0.5, 0.5);
    glVertex3f(1, 0, 0);
    glVertex3f(1, 1, 0);
    glEnd();
    glBegin(GL_QUADS);
    /* top face */
    glVertex3f(1, 1, 0);
    glVertex3f(1, 1, 1);
    glVertex3f(0, 1, 1);
    glVertex3f(0, 1, 0);
    /* bottom face */
    glVertex3f(1, 0, 0);
    glVertex3f(1, 0, 1);
    glVertex3f(0, 0, 1);
    glVertex3f(0, 0, 0);
    glEnd();
    glEndList();
}

void
MakeGlyphDisplayList(FontInfoPtr font, int c)
{
    PerCharInfoPtr  glyph;
    char           *bitmapData;
    int             width, height, spanLength;
    int             x, y;

    if (c < font->min_char || c > font->max_char)
	return;
    if (font->dlist_base == 0) {
	font->dlist_base = glGenLists(font->max_char - font->min_char + 1);
	if (font->dlist_base == 0)
	    XtAppError(app, "could not generate font display lists");
    }
    glyph = &font->glyph[c - font->min_char];
    glNewList(c - font->min_char + font->dlist_base, GL_COMPILE);
    bitmapData = glyph->bitmap;
    if (bitmapData) {
	int             oldx = 0, oldy = 0;

	glPushMatrix();
	glTranslatef(-glyph->xoffset, -glyph->yoffset, 0);
	width = glyph->width;
	spanLength = (width + 7) / 8;
	height = glyph->height;
	for (x = 0; x < width; x++) {
	    for (y = 0; y < height; y++) {
		if (bitmapData[y * spanLength + x / 8] & (1 << (x & 7))) {
		    int             y1, count;

                    /*
		     * Fonts tend to have good vertical repetion.  If we find that
		     * the vertically adjacent  pixels in the glyph bitmap are also enabled,
		     * we can scale a single cube instead of drawing a cube per pixel.
		     */
		    for (y1 = y + 1, count = 1; y < height; y1++, count++) {
			if (!(bitmapData[y1 * spanLength + x / 8] & (1 << (x & 7))))
			    break;
		    }
		    glTranslatef(x - oldx, y - oldy, 0);
		    oldx = x;
		    oldy = y;
		    if (count > 1) {
			glPushMatrix();
			glScalef(1, count, 1);
			glCallList(1);
			glPopMatrix();
			y += count - 1;
		    } else {
			glCallList(1);
		    }
		}
	    }
	}
	glPopMatrix();
    }
    glTranslatef(glyph->advance, 0, 0);
    glEndList();
}

GLuint
GetGlyphDisplayList(FontInfoPtr font, int c)
{
    PerCharInfoPtr  glyph;

    if (c < font->min_char || c > font->max_char)
	return 0;
    if (font->dlist_base == 0)
	XtAppError(app, "font not display listed");
    return c - font->min_char + font->dlist_base;
}

MakeStringDisplayList(FontInfoPtr font, unsigned char *message, GLuint dlist)
{
    unsigned char  *c;

    for (c = message; *c != '\0'; c++) {
	MakeGlyphDisplayList(font, *c);
    }
    glNewList(dlist, GL_COMPILE);
    for (c = message; *c != '\0'; c++) {
	glCallList(GetGlyphDisplayList(font, *c));
    }
    glEndList();
}

int
GetStringLength(FontInfoPtr font, unsigned char *message)
{
    unsigned char  *c;
    int             ch;
    int             width = 0;

    for (c = message; *c != '\0'; c++) {
	ch = *c;
	if (ch >= font->min_char && ch <= font->max_char) {
	    width += font->glyph[ch - font->min_char].advance;
	}
    }
    return width;
}

SetupMessageDisplayList(FontEntryPtr fontEntry, int num, char *message[])
{
    FontInfoPtr     fontinfo = fontEntry->fontinfo;
    GLfloat         scaleFactor;
    int             totalHeight, maxWidth, height, width;
    int             i;

    if (!fontinfo) {
	fontinfo = SuckGlyphsFromServer(dpy, fontEntry->xfont->fid);
	fontEntry->fontinfo = fontinfo;
    }
    height = fontinfo->max_ascent + fontinfo->max_descent;
    maxWidth = 0;
    for (i = 0; i < num; i++) {
	MakeStringDisplayList(fontinfo, message[i], base + i + 1);
	width = GetStringLength(fontinfo, message[i]);
	if (width > maxWidth)
	    maxWidth = width;
    }

#define SHRINK_FACTOR 25.0	/* empirical */

    totalHeight = height * num - fontinfo->max_descent;
    if (maxWidth > totalHeight) {
	scaleFactor = SHRINK_FACTOR / maxWidth;
    } else {
	scaleFactor = SHRINK_FACTOR / totalHeight;
    }

    glNewList(base, GL_COMPILE);
    glScalef(scaleFactor, scaleFactor, 1); /* 1 in Z gives glyphs constant depth */
    for (i = 0; i < num; i++) {
	glPushMatrix();
	width = GetStringLength(fontinfo, message[i]);
	glTranslatef(-width / 2.0, height * (num - i - 1) - totalHeight / 2.0, 0);
	glCallList(base + i + 1);
	glPopMatrix();
    }
    glEndList();
}

void
fontSelect(Widget widget, XtPointer client_data, XmRowColumnCallbackStruct * cbs)
{
    XmToggleButtonCallbackStruct *state = (XmToggleButtonCallbackStruct *) cbs->callbackstruct;
    FontEntryPtr    fontEntry = (FontEntryPtr) cbs->data;

    if (state->set) {
	SetupMessageDisplayList(fontEntry, numMessages, messages);
	if (!motion)
	    draw(glxarea);
    }
}

void
neverCalled(void)
{
}

void
main(int argc, char *argv[])
{
    int             i;
#ifdef IRIX_5_1_MOTIF_BUG_WORKAROUND
    /*
     * XXX Unfortunately a bug in the IRIX 5.1 Motif shared library
     * causes a BadMatch X protocol error if the SGI look&feel
     * is enabled for this program.  If we detect we are on an
     * IRIX 5.1 system, skip the first two fallback resources which
     * specify using the SGI look&feel.
     */
    struct utsname versionInfo;

    if(uname(&versionInfo) >= 0) {
        if(!strcmp(versionInfo.sysname, "IRIX") &&
	   !strncmp(versionInfo.release, "5.1", 3)) {
    	    toplevel = XtAppInitialize(&app, "Textfun", NULL, 0, &argc, argv,
				       &fallbackResources[2], NULL, 0);
        }
    }
    if(toplevel == NULL) {
        toplevel = XtAppInitialize(&app, "Textfun", NULL, 0, &argc, argv,
			           fallbackResources, NULL, 0);
    }
#else
    toplevel = XtAppInitialize(&app, "Textfun", NULL, 0, &argc, argv,
			       fallbackResources, NULL, 0);
#endif
    dpy = XtDisplay(toplevel);
    /* find an OpenGL-capable RGB visual with depth buffer */
    vi = glXChooseVisual(dpy, DefaultScreen(dpy), dblBuf);
    if (vi == NULL) {
	vi = glXChooseVisual(dpy, DefaultScreen(dpy), snglBuf);
	if (vi == NULL)
	    XtAppError(app, "no RGB visual with depth buffer");
	doubleBuffer = GL_FALSE;
    }
    for (i = 0; i < NUM_FONT_ENTRIES; i++) {
	fontEntry[i].xfont = XLoadQueryFont(dpy, fontEntry[i].xlfd);
	if (i == 0 && !fontEntry[i].xfont)
	    XtAppError(app, "could not get basic font");
    }

    fontEntry[0].fontinfo = SuckGlyphsFromServer(dpy, fontEntry[0].xfont->fid);
    if (!fontEntry[0].fontinfo)
	XtAppError(app, "could not get font glyphs");

    /* create an OpenGL rendering context */
    cx = glXCreateContext(dpy, vi, /* no display list sharing */ None,
			   /* favor direct */ GL_TRUE);
    if (cx == NULL)
	XtAppError(app, "could not create rendering context");

    /* create an X colormap since probably not using default visual */
    cmap = XCreateColormap(dpy, RootWindow(dpy, vi->screen),
			   vi->visual, AllocNone);
    XtVaSetValues(toplevel, XtNvisual, vi->visual, XtNdepth, vi->depth,
		  XtNcolormap, cmap, NULL);
    XtAddEventHandler(toplevel, StructureNotifyMask, False,
		      map_state_changed, NULL);
    mainw = XmCreateMainWindow(toplevel, "mainw", NULL, 0);
    XtManageChild(mainw);

    /* create menu bar */
    menubar = XmCreateMenuBar(mainw, "menubar", NULL, 0);
    XtManageChild(menubar);
    /* hack around Xt's ignorance of visuals */
    XtSetArg(menuPaneArgs[0], XmNdepth, DefaultDepthOfScreen(XtScreen(mainw)));
    XtSetArg(menuPaneArgs[1],
	     XmNcolormap, DefaultColormapOfScreen(XtScreen(mainw)));

    /* create File pulldown menu: Quit */
    menupane = XmCreatePulldownMenu(menubar, "menupane", menuPaneArgs, 2);
    btn = XmCreatePushButton(menupane, "Quit", NULL, 0);
    XtAddCallback(btn, XmNactivateCallback, quit, NULL);
    XtManageChild(btn);
    XtSetArg(args[0], XmNsubMenuId, menupane);
    cascade = XmCreateCascadeButton(menubar, "File", args, 1);
    XtManageChild(cascade);

    /* create Options pulldown menu: Motion, Dolly, Rotate, Wobble */
    menupane = XmCreatePulldownMenu(menubar, "menupane", menuPaneArgs, 2);
    btn = XmCreateToggleButton(menupane, "Motion", NULL, 0);
    XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc) toggle, NULL);
    XtManageChild(btn);
    btn = XmCreateToggleButton(menupane, "Dolly", NULL, 0);
    XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc) dolly, NULL);
    XtVaSetValues(btn, XmNset, True, NULL);
    XtManageChild(btn);
    btn = XmCreateToggleButton(menupane, "Rotate", NULL, 0);
    XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc) rotate, NULL);
    XtManageChild(btn);
    btn = XmCreateToggleButton(menupane, "Wobble", NULL, 0);
    XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc) wobble, NULL);
    XtManageChild(btn);
    XtSetArg(args[0], XmNsubMenuId, menupane);
    cascade = XmCreateCascadeButton(menubar, "Options", args, 1);
    XtManageChild(cascade);

    XtSetArg(menuPaneArgs[2], XmNradioBehavior, True);
    XtSetArg(menuPaneArgs[3], XmNradioAlwaysOne, True);
    menupane = XmCreatePulldownMenu(menubar, "menupane", menuPaneArgs, 4);
    XtAddCallback(menupane, XmNentryCallback, (XtCallbackProc) fontSelect, NULL);
    for (i = 0; i < NUM_FONT_ENTRIES; i++) {
	btn = XmCreateToggleButton(menupane, fontEntry[i].name, NULL, 0);
	XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc) neverCalled, &fontEntry[i]);
	if (i == 0)
	    XtVaSetValues(btn, XmNset, True, NULL);
        if (!fontEntry[i].xfont)
	    XtSetSensitive(btn, False);
	XtManageChild(btn);
    }
    XtSetArg(args[0], XmNsubMenuId, menupane);
    cascade = XmCreateCascadeButton(menubar, "Font", args, 1);
    XtManageChild(cascade);

    /* create framed drawing area for OpenGL rendering */
    frame = XmCreateFrame(mainw, "frame", NULL, 0);
    XtManageChild(frame);
    glxarea = XtCreateManagedWidget("glxarea", xmDrawingAreaWidgetClass,
				    frame, NULL, 0);
    XtAddCallback(glxarea, XmNexposeCallback, (XtCallbackProc) draw, NULL);
    XtAddCallback(glxarea, XmNresizeCallback, resize, NULL);
    XtAddCallback(glxarea, XmNinputCallback, input, NULL);
    /* set up application's window layout */
    XmMainWindowSetAreas(mainw, menubar, NULL, NULL, NULL, frame);
    XtRealizeWidget(toplevel);

    /*
     * Once widget is realized (ie, associated with a created X window), we
     * can bind the OpenGL rendering context to the window.
     */
    glXMakeCurrent(dpy, XtWindow(glxarea), cx);
    made_current = GL_TRUE;
    /* setup OpenGL state */
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    glClearDepth(1.0);
    glMatrixMode(GL_PROJECTION);
    glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 80);
    glMatrixMode(GL_MODELVIEW);

    MakeCube();

    if (argv[1] != NULL) {
	numMessages = argc - 1;
	messages = &argv[1];
    } else {
	numMessages = NUM_DEFAULT_MESSAGES;
	messages = defaultMessage;
    }

    base = glGenLists(numMessages + 1);
    SetupMessageDisplayList(&fontEntry[0], numMessages, messages);

    tick();

    /* start event processing */
    XtAppMainLoop(app);
}
