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


/* $Revision: 1.6 $ */
/* compile: cc -o glxsimple glxsimple.c -lGL -lX11 */
#include <stdio.h>
#include <stdlib.h>
#include <GL/glx.h>		/* this includes the necessary X headers */
#include <GL/gl.h>

static int snglBuf[] = {GLX_RGBA, GLX_DEPTH_SIZE, 16, None};
static int dblBuf[] = {GLX_RGBA, GLX_DEPTH_SIZE, 16, GLX_DOUBLEBUFFER, None};

Display        *dpy;
Window          win;
GLfloat         xAngle = 42.0, yAngle = 82.0, zAngle = 112.0;
GLboolean	doubleBuffer = GL_TRUE;

void
fatalError(char *message)
{
    fprintf(stderr, "glxsimple: %s\n", message);
    exit(1);
}

void
redraw(void)
{
    static GLboolean   displayListInited = GL_FALSE;

    if (displayListInited) {
	/* if display list already exists, just execute it */
	glCallList(1);
    } else {
	/* otherwise compile and execute to create the display list */
	glNewList(1, GL_COMPILE_AND_EXECUTE);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	/* front face */
	glBegin(GL_QUADS);
	glColor3f(0.0, 0.7, 0.1);	/* green */
	glVertex3f(-1.0, 1.0, 1.0);
	glVertex3f(1.0, 1.0, 1.0);
	glVertex3f(1.0, -1.0, 1.0);
	glVertex3f(-1.0, -1.0, 1.0);
	/* back face */
	glColor3f(0.9, 1.0, 0.0);	/* yellow */
	glVertex3f(-1.0, 1.0, -1.0);
	glVertex3f(1.0, 1.0, -1.0);
	glVertex3f(1.0, -1.0, -1.0);
	glVertex3f(-1.0, -1.0, -1.0);
	/* top side face */
	glColor3f(0.2, 0.2, 1.0);	/* blue */
	glVertex3f(-1.0, 1.0, 1.0);
	glVertex3f(1.0, 1.0, 1.0);
	glVertex3f(1.0, 1.0, -1.0);
	glVertex3f(-1.0, 1.0, -1.0);
	/* bottom side face */
	glColor3f(0.7, 0.0, 0.1);	/* red */
	glVertex3f(-1.0, -1.0, 1.0);
	glVertex3f(1.0, -1.0, 1.0);
	glVertex3f(1.0, -1.0, -1.0);
	glVertex3f(-1.0, -1.0, -1.0);
	glEnd();
	glEndList();
	displayListInited = GL_TRUE;
    }
    if(doubleBuffer) glXSwapBuffers(dpy, win); /* buffer swap does implicit glFlush */
       else glFlush(); /* explicit flush for single buffered case */
}

void
main(int argc, char **argv)
{
    XVisualInfo    *vi;
    Colormap        cmap;
    XSetWindowAttributes swa;
    GLXContext      cx;
    XEvent          event;
    GLboolean       needRedraw = GL_FALSE, recalcModelView = GL_TRUE;
    int		    dummy;

    /*** (1) open a connection to the X server ***/

    dpy = XOpenDisplay(NULL);
    if (dpy == NULL) fatalError("could not open display");

    /*** (2) make sure OpenGL's GLX extension supported ***/

    if(!glXQueryExtension(dpy, &dummy, &dummy)) fatalError("X server has no OpenGL GLX extension");

    /*** (3) find an appropriate visual ***/

    /* find an OpenGL-capable RGB visual with depth buffer */
    vi = glXChooseVisual(dpy, DefaultScreen(dpy), dblBuf);
    if (vi == NULL) {
       vi = glXChooseVisual(dpy, DefaultScreen(dpy), snglBuf);
       if (vi == NULL) fatalError("no RGB visual with depth buffer");
       doubleBuffer = GL_FALSE;
    }
    if(vi->class != TrueColor) fatalError("TrueColor visual required for this program");

    /*** (4) create an OpenGL rendering context  ***/

    /* create an OpenGL rendering context */
    cx = glXCreateContext(dpy, vi, /* no sharing of display lists */ None,
			  /* direct rendering if possible */ GL_TRUE);
    if (cx == NULL) fatalError("could not create rendering context");

    /*** (5) create an X window with the selected visual ***/

    /* create an X colormap since probably not using default visual */
    cmap = XCreateColormap(dpy, RootWindow(dpy, vi->screen), vi->visual, AllocNone);
    swa.colormap = cmap;
    swa.border_pixel = 0;
    swa.event_mask = ExposureMask | ButtonPressMask | StructureNotifyMask;
    win = XCreateWindow(dpy, RootWindow(dpy, vi->screen), 0, 0, 300, 300, 0, vi->depth,
                        InputOutput, vi->visual, CWBorderPixel | CWColormap | CWEventMask, &swa);
    XSetStandardProperties(dpy, win, "glxsimple", "glxsimple", None, argv, argc, NULL);

    /*** (6) bind the rendering context to the window ***/

    glXMakeCurrent(dpy, win, cx);

    /*** (7) request the X window to be displayed on the screen ***/

    XMapWindow(dpy, win);

    /*** (8) configure the OpenGL context for rendering ***/

    glEnable(GL_DEPTH_TEST); /* enable depth buffering */
    glDepthFunc(GL_LESS);    /* pedantic, GL_LESS is the default */
    glClearDepth(1.0);       /* pedantic, 1.0 is the default */
    /* frame buffer clears should be to black */
    glClearColor(0.0, 0.0, 0.0, 0.0);
    /* set up projection transform */
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 10.0);
    /* establish initial viewport */
    glViewport(0, 0, 300, 300); /* pedantic, full window size is default viewport */

    /*** (9) dispatch X events ***/

    while (1) {
	do {
	    XNextEvent(dpy, &event);
	    switch (event.type) {
	    case ButtonPress:
		recalcModelView = GL_TRUE;
		switch (event.xbutton.button) {
		case 1: xAngle += 10.0; break;
		case 2: yAngle += 10.0; break;
		case 3: zAngle += 10.0; break;
		}
		break;
	    case ConfigureNotify:
		glViewport(0, 0, event.xconfigure.width, event.xconfigure.height);
		/* fall through... */
	    case Expose:
		needRedraw = GL_TRUE;
		break;
	    }
	} while(XPending(dpy)); /* loop to compress events */
	if (recalcModelView) {
	    glMatrixMode(GL_MODELVIEW);
	    /* reset modelview matrix to the identity matrix */
	    glLoadIdentity();
	    /* move the camera back three units */
	    glTranslatef(0.0, 0.0, -3.0);
	    /* rotate by X, Y, and Z angles */
	    glRotatef(xAngle, 0.1, 0.0, 0.0);
	    glRotatef(yAngle, 0.0, 0.1, 0.0);
	    glRotatef(zAngle, 0.0, 0.0, 1.0);
	    recalcModelView = GL_FALSE;
	    needRedraw = GL_TRUE;
	}
	if (needRedraw) {
	    redraw();
	    needRedraw = GL_FALSE;
	}
    }
}
