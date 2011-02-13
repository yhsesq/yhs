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


/* $Revision: 1.17 $ */
/* compile: cc -o glxdino glxdino.c -lGLU -lGL -lXmu -lX11 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>		/* for cos(), sin(), and sqrt() */
#include <GL/glx.h>		/* this includes X and gl.h headers */
#include <GL/glu.h>		/* gluPerspective(), gluLookAt(), GLU polygon
				 * tesselator */
#include <X11/Xatom.h>		/* for XA_RGB_DEFAULT_MAP atom */
#include <X11/Xmu/StdCmap.h>	/* for XmuLookupStandardColormap() */
#include <X11/keysym.h>		/* for XK_Escape keysym */

typedef enum {
    RESERVED, BODY_SIDE, BODY_EDGE, BODY_WHOLE, ARM_SIDE, ARM_EDGE, ARM_WHOLE,
    LEG_SIDE, LEG_EDGE, LEG_WHOLE, EYE_SIDE, EYE_EDGE, EYE_WHOLE, DINOSAUR
}               displayLists;

Display *dpy;
Window win;
GLfloat angle = -150;	/* in degrees */
GLboolean doubleBuffer = GL_TRUE, iconic = GL_FALSE, keepAspect = GL_FALSE;
int W = 300, H = 300;
XSizeHints sizeHints = {0};
GLdouble bodyWidth = 2.0;
int configuration[] = {GLX_DOUBLEBUFFER, GLX_RGBA, GLX_DEPTH_SIZE, 16, None};
GLfloat body[][2] = { {0, 3}, {1, 1}, {5, 1}, {8, 4}, {10, 4}, {11, 5},
    {11, 11.5}, {13, 12}, {13, 13}, {10, 13.5}, {13, 14}, {13, 15}, {11, 16},
    {8, 16}, {7, 15}, {7, 13}, {8, 12}, {7, 11}, {6, 6}, {4, 3}, {3, 2},
    {1, 2} };
GLfloat arm[][2] = { {8, 10}, {9, 9}, {10, 9}, {13, 8}, {14, 9}, {16, 9},
    {15, 9.5}, {16, 10}, {15, 10}, {15.5, 11}, {14.5, 10}, {14, 11}, {14, 10},
    {13, 9}, {11, 11}, {9, 11} };
GLfloat leg[][2] = { {8, 6}, {8, 4}, {9, 3}, {9, 2}, {8, 1}, {8, 0.5}, {9, 0},
    {12, 0}, {10, 1}, {10, 2}, {12, 4}, {11, 6}, {10, 7}, {9, 7} };
GLfloat eye[][2] = { {8.75, 15}, {9, 14.7}, {9.6, 14.7}, {10.1, 15},
    {9.6, 15.25}, {9, 15.25} };
GLfloat lightZeroPosition[] = {10.0, 4.0, 10.0, 1.0};
GLfloat lightZeroColor[] = {0.8, 1.0, 0.8, 1.0}; /* green-tinted */
GLfloat lightOnePosition[] = {-1.0, -2.0, 1.0, 0.0};
GLfloat lightOneColor[] = {0.6, 0.3, 0.2, 1.0};  /* red-tinted */
GLfloat skinColor[] = {0.1, 1.0, 0.1, 1.0}, eyeColor[] = {1.0, 0.2, 0.2, 1.0};

void
fatalError(char *message)
{
    fprintf(stderr, "glxdino: %s\n", message);
    exit(1);
}

Colormap
getColormap(XVisualInfo * vi)
{
    Status          status;
    XStandardColormap *standardCmaps;
    Colormap        cmap;
    int             i, numCmaps;

    /* be lazy; using DirectColor too involved for this example */
    if (vi->class != TrueColor)
        fatalError("no support for non-TrueColor visual");
    /* if no standard colormap but TrueColor, just make an unshared one */
    status = XmuLookupStandardColormap(dpy, vi->screen, vi->visualid,
        vi->depth, XA_RGB_DEFAULT_MAP, /* replace */ False, /* retain */ True);
    if (status == 1) {
	status = XGetRGBColormaps(dpy, RootWindow(dpy, vi->screen),
			     &standardCmaps, &numCmaps, XA_RGB_DEFAULT_MAP);
	if (status == 1)
	    for (i = 0; i < numCmaps; i++)
		if (standardCmaps[i].visualid == vi->visualid) {
		    cmap = standardCmaps[i].colormap;
		    XFree(standardCmaps);
		    return cmap;
		}
    }
    cmap = XCreateColormap(dpy, RootWindow(dpy, vi->screen),
        vi->visual, AllocNone);
    return cmap;
}

void
extrudeSolidFromPolygon(GLfloat data[][2], unsigned int dataSize,
    GLdouble thickness, GLuint side, GLuint edge, GLuint whole)
{
    static GLUtriangulatorObj *tobj = NULL;
    GLdouble        vertex[3], dx, dy, len;
    int             i;
    int             count = dataSize / (2 * sizeof(GLfloat));

    if (tobj == NULL) {
	tobj = gluNewTess();	/* create and initialize a GLU polygon
				 * tesselation object */
	gluTessCallback(tobj, GLU_BEGIN, glBegin);
	gluTessCallback(tobj, GLU_VERTEX, glVertex2fv);	/* semi-tricky */
	gluTessCallback(tobj, GLU_END, glEnd);
    }
    glNewList(side, GL_COMPILE);
        glShadeModel(GL_SMOOTH); /* smooth minimizes seeing tessellation */
        gluBeginPolygon(tobj);
            for (i = 0; i < count; i++) {
	        vertex[0] = data[i][0];
	        vertex[1] = data[i][1];
	        vertex[2] = 0;
	        gluTessVertex(tobj, vertex, &data[i]);
            }
        gluEndPolygon(tobj);
    glEndList();
    glNewList(edge, GL_COMPILE);
        glShadeModel(GL_FLAT);	/* flat shade keeps angular hands from being
				 * "smoothed" */
        glBegin(GL_QUAD_STRIP);
        for (i = 0; i <= count; i++) {
	    /* mod function handles closing the edge */
	    glVertex3f(data[i % count][0], data[i % count][1], 0.0);
	    glVertex3f(data[i % count][0], data[i % count][1], thickness);
	    /* Calculate a unit normal by dividing by Euclidean distance. We
	     * could be lazy and use glEnable(GL_NORMALIZE) so we could pass in
	     * arbitrary normals for a very slight performance hit. */
	    dx = data[(i + 1) % count][1] - data[i % count][1];
	    dy = data[i % count][0] - data[(i + 1) % count][0];
	    len = sqrt(dx * dx + dy * dy);
	    glNormal3f(dx / len, dy / len, 0.0);
        }
        glEnd();
    glEndList();
    glNewList(whole, GL_COMPILE);
        glFrontFace(GL_CW);
        glCallList(edge);
        glNormal3f(0.0, 0.0, -1.0); /* constant normal for side */
        glCallList(side);
        glPushMatrix();
            glTranslatef(0.0, 0.0, thickness);
            glFrontFace(GL_CCW);
            glNormal3f(0.0, 0.0, 1.0); /* opposite normal for other side */
            glCallList(side);
        glPopMatrix();
    glEndList();
}

void
makeDinosaur(void)
{
    GLfloat         bodyWidth = 3.0;

    extrudeSolidFromPolygon(body, sizeof(body), bodyWidth,
        BODY_SIDE, BODY_EDGE, BODY_WHOLE);
    extrudeSolidFromPolygon(arm, sizeof(arm), bodyWidth / 4,
        ARM_SIDE, ARM_EDGE, ARM_WHOLE);
    extrudeSolidFromPolygon(leg, sizeof(leg), bodyWidth / 2,
        LEG_SIDE, LEG_EDGE, LEG_WHOLE);
    extrudeSolidFromPolygon(eye, sizeof(eye), bodyWidth + 0.2,
        EYE_SIDE, EYE_EDGE, EYE_WHOLE);
    glNewList(DINOSAUR, GL_COMPILE);
        glMaterialfv(GL_FRONT, GL_DIFFUSE, skinColor);
        glCallList(BODY_WHOLE);
        glPushMatrix();
            glTranslatef(0.0, 0.0, bodyWidth);
            glCallList(ARM_WHOLE);
            glCallList(LEG_WHOLE);
            glTranslatef(0.0, 0.0, -bodyWidth - bodyWidth / 4);
            glCallList(ARM_WHOLE);
            glTranslatef(0.0, 0.0, -bodyWidth / 4);
            glCallList(LEG_WHOLE);
            glTranslatef(0.0, 0.0, bodyWidth / 2 - 0.1);
            glMaterialfv(GL_FRONT, GL_DIFFUSE, eyeColor);
            glCallList(EYE_WHOLE);
        glPopMatrix();
    glEndList();
}

void
redraw(void)
{
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glCallList(DINOSAUR);
    if (doubleBuffer)
	glXSwapBuffers(dpy, win);	/* buffer swap does implicit glFlush */
    else glFlush();		/* explicit flush for single buffered case */
}

void
main(int argc, char **argv)
{
    XVisualInfo    *vi;
    Colormap        cmap;
    XSetWindowAttributes swa;
    XWMHints       *wmHints;
    Atom            wmDeleteWindow;
    GLXContext      cx;
    XEvent          event;
    KeySym          ks;
    GLboolean       needRedraw = GL_FALSE, recalcModelView = GL_TRUE;
    char           *display = NULL, *geometry = NULL;
    int             flags, x, y, width, height, lastX, i;

    /*** (1) process normal X command line arguments ***/
    for (i = 1; i < argc; i++) {
	if (!strcmp(argv[i], "-geometry")) {
	    if (++i >= argc)
		fatalError("follow -geometry option with geometry parameter");
	    geometry = argv[i];
	} else if (!strcmp(argv[i], "-display")) {
	    if (++i >= argc)
		fatalError("follow -display option with display parameter");
	    display = argv[i];
	} else if (!strcmp(argv[i], "-iconic")) iconic = GL_TRUE;
	else if (!strcmp(argv[i], "-keepaspect")) keepAspect = GL_TRUE;
	else if (!strcmp(argv[i], "-single")) doubleBuffer = GL_FALSE;
	else fatalError("bad option");
    }

    /*** (2) open a connection to the X server ***/
    dpy = XOpenDisplay(display);
    if (dpy == NULL) fatalError("could not open display");

    /*** (3) make sure OpenGL's GLX extension supported ***/
    if (!glXQueryExtension(dpy, NULL, NULL))
	fatalError("X server has no OpenGL GLX extension");

    /*** (4) find an appropriate visual and a colormap for it ***/
    /* find an OpenGL-capable RGB visual with depth buffer */
    if (!doubleBuffer) goto SingleBufferOverride;
    vi = glXChooseVisual(dpy, DefaultScreen(dpy), configuration);
    if (vi == NULL) {
      SingleBufferOverride:
	vi = glXChooseVisual(dpy, DefaultScreen(dpy), &configuration[1]);
	if (vi == NULL)
	    fatalError("no appropriate RGB visual with depth buffer");
	doubleBuffer = GL_FALSE;
    }
    cmap = getColormap(vi);

    /*** (5) create an OpenGL rendering context  ***/
    /* create an OpenGL rendering context */
    cx = glXCreateContext(dpy, vi, /* no sharing of display lists */ NULL,
			   /* direct rendering if possible */ GL_TRUE);
    if (cx == NULL) fatalError("could not create rendering context");

    /*** (6) create an X window with selected visual and right properties ***/
    flags = XParseGeometry(geometry, &x, &y,
	(unsigned int *) &width, (unsigned int *) &height);
    if (WidthValue & flags) {
	sizeHints.flags |= USSize;
	sizeHints.width = width;
	W = width;
    }
    if (HeightValue & flags) {
	sizeHints.flags |= USSize;
	sizeHints.height = height;
	H = height;
    }
    if (XValue & flags) {
	if (XNegative & flags)
	    x = DisplayWidth(dpy, DefaultScreen(dpy)) + x - sizeHints.width;
	sizeHints.flags |= USPosition;
	sizeHints.x = x;
    }
    if (YValue & flags) {
	if (YNegative & flags)
	    y = DisplayHeight(dpy, DefaultScreen(dpy)) + y - sizeHints.height;
	sizeHints.flags |= USPosition;
	sizeHints.y = y;
    }
    if (keepAspect) {
	sizeHints.flags |= PAspect;
	sizeHints.min_aspect.x = sizeHints.max_aspect.x = W;
	sizeHints.min_aspect.y = sizeHints.max_aspect.y = H;
    }
    swa.colormap = cmap;
    swa.border_pixel = 0;
    swa.event_mask = ExposureMask | StructureNotifyMask |
	ButtonPressMask | Button1MotionMask | KeyPressMask;
    win = XCreateWindow(dpy, RootWindow(dpy, vi->screen),
                        sizeHints.x, sizeHints.y, W, H,
			0, vi->depth, InputOutput, vi->visual,
			CWBorderPixel | CWColormap | CWEventMask, &swa);
    XSetStandardProperties(dpy, win, "OpenGLosaurus", "glxdino",
        None, argv, argc, &sizeHints);
    wmHints = XAllocWMHints();
    wmHints->initial_state = iconic ? IconicState : NormalState;
    wmHints->flags = StateHint;
    XSetWMHints(dpy, win, wmHints);
    wmDeleteWindow = XInternAtom(dpy, "WM_DELETE_WINDOW", False);
    XSetWMProtocols(dpy, win, &wmDeleteWindow, 1);

    /*** (7) bind the rendering context to the window ***/
    glXMakeCurrent(dpy, win, cx);

    /*** (8) make the desired display lists ***/
    makeDinosaur();

    /*** (9) configure the OpenGL context for rendering ***/
    glEnable(GL_CULL_FACE);	/* ~50% better perfomance than no back-face
				 * culling on Entry Indigo */
    glEnable(GL_DEPTH_TEST);	/* enable depth buffering */
    glEnable(GL_LIGHTING);	/* enable lighting */
    glMatrixMode(GL_PROJECTION);/* set up projection transform */
    gluPerspective( /* field of view in degree */ 40.0, /* aspect ratio */ 1.0,
		    /* Z near */ 1.0, /* Z far */ 40.0);
    glMatrixMode(GL_MODELVIEW);	/* now change to modelview */
    gluLookAt(0.0, 0.0, 30.0,	/* eye is at (0,0,30) */
	      0.0, 0.0, 0.0,	/* center is at (0,0,0) */
	      0.0, 1.0, 0.);	/* up is in postivie Y direction */
    glPushMatrix();		/* dummy push so we can pop on model recalc */
    glLightModeli(GL_LIGHT_MODEL_LOCAL_VIEWER, 1);
    glLightfv(GL_LIGHT0, GL_POSITION, lightZeroPosition);
    glLightfv(GL_LIGHT0, GL_DIFFUSE, lightZeroColor);
    glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 0.1);
    glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 0.05);
    glLightfv(GL_LIGHT1, GL_POSITION, lightOnePosition);
    glLightfv(GL_LIGHT1, GL_DIFFUSE, lightOneColor);
    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHT1);	/* enable both lights */

    /*** (10) request the X window to be displayed on the screen ***/
    XMapWindow(dpy, win);

    /*** (11) dispatch X events ***/
    while (1) {
	do {
	    XNextEvent(dpy, &event);
	    switch (event.type) {
	    case ConfigureNotify:
		glViewport(0, 0,
		    event.xconfigure.width, event.xconfigure.height);
		/* fall through... */
	    case Expose:
		needRedraw = GL_TRUE;
		break;
	    case MotionNotify:
		recalcModelView = GL_TRUE;
		angle -= (lastX - event.xmotion.x);
	    case ButtonPress:
		lastX = event.xbutton.x;
		break;
	    case KeyPress:
		ks = XLookupKeysym((XKeyEvent *) & event, 0);
		if (ks == XK_Escape) exit(0);
		break;
	    case ClientMessage:
		if (event.xclient.data.l[0] == wmDeleteWindow) exit(0);
		break;
	    }
	} while (XPending(dpy));/* loop to compress events */
	if (recalcModelView) {
	    glPopMatrix();	/* pop old rotated matrix (or dummy matrix if
				 * first time) */
	    glPushMatrix();
	    glRotatef(angle, 0.0, 1.0, 0.0);
	    glTranslatef(-8, -8, -bodyWidth / 2);
	    recalcModelView = GL_FALSE;
	    needRedraw = GL_TRUE;
	}
	if (needRedraw) {
	    redraw();
	    needRedraw = GL_FALSE;
	}
    }
}
