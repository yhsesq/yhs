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


/* $Revision: 1.8 $ */
/* compile: cc -o glxmotif glxmotif.c -lGLU -lGL -lXm -lXt -lX11 */
#include <stdlib.h>
#include <stdio.h>
#include <Xm/Form.h>
#include <Xm/Frame.h>
#include <Xm/DrawingA.h>
#include <X11/keysym.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glx.h>

static int snglBuf[] = {GLX_RGBA, GLX_DEPTH_SIZE, 16, None};
static int dblBuf[] = {GLX_RGBA, GLX_DEPTH_SIZE, 16, GLX_DOUBLEBUFFER, None};
static String   fallbackResources[] = {
    "*glxarea*width: 300", "*glxarea*height: 300",
    "*frame*x: 20", "*frame*y: 20",
    "*frame*topOffset: 20", "*frame*bottomOffset: 20",
    "*frame*rightOffset: 20", "*frame*leftOffset: 20",
    "*frame*shadowType: SHADOW_IN",
    NULL
};

Display        *dpy;
GLboolean       doubleBuffer = GL_TRUE, viewportUpdateNeeded = GL_TRUE, spinning = GL_FALSE;
XtAppContext    app;
XtWorkProcId    workId = 0;
Widget          toplevel, form, frame, glxarea;

void
updateViewport(Widget w)
{
    Dimension width, height;

    XtVaGetValues(w, XmNwidth, &width, XmNheight, &height, NULL);
    glViewport(0, 0, (GLint) width, (GLint) height);
    viewportUpdateNeeded = GL_FALSE;
}

void
draw(Widget w)
{
    if (viewportUpdateNeeded) updateViewport(w);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glBegin(GL_POLYGON);
    glColor3f(0.0, 0.0, 0.0); glVertex3f(-10.0, -10.0, 0.0);
    glColor3f(0.7, 0.7, 0.7); glVertex3f(10.0, -10.0, 0.0);
    glColor3f(1.0, 1.0, 1.0); glVertex3f(-10.0, 10.0, 0.0);
    glEnd();
    glBegin(GL_POLYGON);
    glColor3f(1.0, 1.0, 0.0); glVertex3f(0.0, -10.0, -10.0);
    glColor3f(0.0, 1.0, 0.7); glVertex3f(0.0, -10.0, 10.0);
    glColor3f(0.0, 0.0, 1.0); glVertex3f(0.0, 5.0, -10.0);
    glEnd();
    glBegin(GL_POLYGON);
    glColor3f(1.0, 1.0, 0.0); glVertex3f(-10.0, 6.0, 4.0);
    glColor3f(1.0, 0.0, 1.0); glVertex3f(-10.0, 3.0, 4.0);
    glColor3f(0.0, 0.0, 1.0); glVertex3f(4.0, -9.0, -10.0);
    glColor3f(1.0, 0.0, 1.0); glVertex3f(4.0, -6.0, -10.0);
    glEnd();
    if (doubleBuffer) glXSwapBuffers(dpy, XtWindow(w));
    glFlush();
}

void
expose(Widget w, XtPointer clientData, XtPointer callData)
{
    draw(w);
}

void
resize(Widget w, XtPointer clientData, XtPointer callData)
{
    XmDrawingAreaCallbackStruct *cd = (XmDrawingAreaCallbackStruct *) callData;

    /* don't try OpenGL until window is realized! */
    if (XtIsRealized(w)) updateViewport(w);
        else viewportUpdateNeeded = GL_TRUE;
}

Boolean
spin(XtPointer clientData)
{
    glRotatef(2.5, 1.0, 0.0, 0.0);
    draw(glxarea);
    return False; /* leave work proc active */
}

void
input(Widget w, XtPointer clientData, XtPointer callData)
{
    XmDrawingAreaCallbackStruct *cd = (XmDrawingAreaCallbackStruct *) callData;
    char            buffer[1];
    KeySym          keysym;
    int             rc;

    switch (cd->event->type) {
    case KeyRelease:
	/*
	 * It is necessary to convert the keycode to a keysym before it is
	 * possible to check if it is an escape
	 */
	rc = XLookupString((XKeyEvent *) cd->event, buffer, 1, &keysym, NULL);
	switch (keysym) {
	case XK_Up:
	    glRotatef(10.0, 0.0, 0.0, 1.0);
	    if (!spinning) draw(w);
	    break;
	case XK_Down:
	    glRotatef(-10.0, 0.0, 0.0, 1.0);
	    if (!spinning) draw(w);
	    break;
	case XK_Left:
	    glRotatef(-10.0, 0.0, 1.0, 0.0);
	    if (!spinning) draw(w);
	    break;
	case XK_Right:
	    glRotatef(10.0, 0.0, 1.0, 0.0);
	    if (!spinning) draw(w);
	    break;
	case XK_S: case XK_s: /* the S key */
	    if (spinning) {
		XtRemoveWorkProc(workId);
		spinning = GL_FALSE;
	    } else {
		workId = XtAppAddWorkProc(app, spin, NULL);
		spinning = GL_TRUE;
	    }
	    break;
	case XK_Escape:
	    exit(0);
	}
	break;
    }
}

void
map_state_changed(Widget w, XtPointer clientData, XEvent * event, Boolean * cont)
{
    switch (event->type) {
    case MapNotify:
	if (spinning && workId != 0) workId = XtAppAddWorkProc(app, spin, NULL);
	break;
    case UnmapNotify:
	if (spinning) XtRemoveWorkProc(workId);
	break;
    }
}

main(int argc, char *argv[])
{
    XVisualInfo    *vi;
    Colormap        cmap;
    GLXContext      cx;
    int		    saved_argc;
    String	   *saved_argv;

    toplevel = XtAppInitialize(&app, "Glxmotif", NULL, 0, &argc, argv,
                               fallbackResources, NULL, 0);
    dpy = XtDisplay(toplevel);

    /* find an OpenGL-capable RGB visual with depth buffer */
    vi = glXChooseVisual(dpy, DefaultScreen(dpy), dblBuf);
    if (vi == NULL) {
	vi = glXChooseVisual(dpy, DefaultScreen(dpy), snglBuf);
	if (vi == NULL) XtAppError(app, "no RGB visual with depth buffer");
	doubleBuffer = GL_FALSE;
    }
    /* create an OpenGL rendering context */
    cx = glXCreateContext(dpy, vi, /* no display list sharing */ None, /* favor direct */ GL_TRUE);
    if (cx == NULL) XtAppError(app, "could not create rendering context");
    /* create an X colormap since probably not using default visual */
    cmap = XCreateColormap(dpy, RootWindow(dpy, vi->screen), vi->visual, AllocNone);

    XtVaSetValues(toplevel, XtNvisual, vi->visual, XtNdepth, vi->depth,
       XtNcolormap, cmap, NULL);
    XtAddEventHandler(toplevel, StructureNotifyMask, False, map_state_changed, NULL);

    form = XmCreateForm(toplevel, "form", NULL, 0);
    XtManageChild(form);

    frame = XmCreateFrame(form, "frame", NULL, 0);
    XtVaSetValues(frame, XmNbottomAttachment, XmATTACH_FORM,
        XmNtopAttachment, XmATTACH_FORM, XmNleftAttachment, XmATTACH_FORM,
        XmNrightAttachment, XmATTACH_FORM, NULL);
    XtManageChild(frame);

    glxarea = XtCreateManagedWidget("glxarea", xmDrawingAreaWidgetClass, frame, NULL, 0);
    XtAddCallback(glxarea, XmNexposeCallback, expose, NULL);
    XtAddCallback(glxarea, XmNresizeCallback, resize, NULL);
    XtAddCallback(glxarea, XmNinputCallback, input, NULL);

    XtRealizeWidget(toplevel);

    /* Once widget is realized (ie, associated with a created X window), we
     * can bind the OpenGL rendering context to the window.
     */
    glXMakeCurrent(dpy, XtWindow(glxarea), cx);

    /* setup OpenGL state */
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL); 
    glClearDepth(1.0);
    glClearColor(0.0, 0.0, 0.0, 0.0);
    glLoadIdentity();
    gluPerspective(40.0, 1.0, 10.0, 200.0);
    glTranslatef(0.0, 0.0, -50.0); glRotatef(-58.0, 0.0, 1.0, 0.0);

    XtAppMainLoop(app);
}
