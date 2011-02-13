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



/*
 * paperplane can be compiled to use a "single visual" for the entire window
 * hierarchy and render OpenGL into a standard Motif drawing area widget:
 *
 *  cc -o sv_paperplane paperplane.c -DnoGLwidget -lGL -lXm -lXt -lX11 -lm
 *
 * Or paperplane can be compiled to use the default visual for most of
 * the window hierarchy but render OpenGL into a special "OpenGL widget":
 *
 *  cc -o glw_paperplane paperplane.c -lGLw -lGL -lXm -lXt -lX11 -lm
 */
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <math.h>
#include <Xm/MainW.h>
#include <Xm/RowColumn.h>
#include <Xm/PushB.h>
#include <Xm/ToggleB.h>
#include <Xm/CascadeB.h>
#include <Xm/Frame.h>
#ifdef noGLwidget
#include <Xm/DrawingA.h>        /* Motif drawing area widget */
#else
/** NOTE: in IRIX 5.2, the OpenGL widget headers are mistakenly in   **/
/** <GL/GLwDrawA.h> and <GL/GlwMDraw.h> respectively.  Below are the **/
/** _official_ standard locations.                                   **/
#ifdef noMotifGLwidget
#include <X11/GLw/GLwDrawA.h>   /* pure Xt OpenGL drawing area widget */
#else
#include <X11/GLw/GLwMDrawA.h>  /* Motif OpenGL drawing area widget */
#endif
#endif
#include <X11/keysym.h>
#include <GL/gl.h>
#include <GL/glu.h>
#include <GL/glx.h>

static int dblBuf[] = {
    GLX_DOUBLEBUFFER, GLX_RGBA, GLX_DEPTH_SIZE, 16,
    GLX_RED_SIZE, 1, GLX_GREEN_SIZE, 1, GLX_BLUE_SIZE, 1,
    None
};
static int *snglBuf = &dblBuf[1];
static String   fallbackResources[] = {
#ifdef IRIX_5_2_or_higher
    "*sgiMode: true",           /* try to enable IRIX 5.2+ look & feel */
    "*useSchemes: all",         /* and SGI schemes */
#endif
    "*title: OpenGL paper plane demo",
    "*glxarea*width: 300", "*glxarea*height: 300", NULL
};
Display     *dpy;
GLboolean    doubleBuffer = GL_TRUE, moving = GL_FALSE, made_current = GL_FALSE;
XtAppContext app;
XtWorkProcId workId = 0;
Widget       toplevel, mainw, menubar, menupane, btn, cascade, frame, glxarea;
GLXContext   cx;
XVisualInfo *vi;
#ifdef noGLwidget
Colormap     cmap;
#endif
Arg          menuPaneArgs[1], args[1];

#define MAX_PLANES 15

struct {
    float           speed;      /* zero speed means not flying */
    GLfloat         red, green, blue;
    float           theta;
    float           x, y, z, angle;
} planes[MAX_PLANES];

#define v3f glVertex3f /* v3f was the short IRIS GL name for glVertex3f */

void draw(Widget w)
{
    GLfloat         red, green, blue;
    int             i;

    glClear(GL_DEPTH_BUFFER_BIT);
    /* paint black to blue smooth shaded polygon for background */
    glDisable(GL_DEPTH_TEST);
    glShadeModel(GL_SMOOTH);
    glBegin(GL_POLYGON);
        glColor3f(0.0, 0.0, 0.0);
        v3f(-20, 20, -19); v3f(20, 20, -19);
        glColor3f(0.0, 0.0, 1.0);
        v3f(20, -20, -19); v3f(-20, -20, -19);
    glEnd();
    /* paint planes */
    glEnable(GL_DEPTH_TEST);
    glShadeModel(GL_FLAT);
    for (i = 0; i < MAX_PLANES; i++)
        if (planes[i].speed != 0.0) {
            glPushMatrix();
                glTranslatef(planes[i].x, planes[i].y, planes[i].z);
                glRotatef(290.0, 1.0, 0.0, 0.0);
                glRotatef(planes[i].angle, 0.0, 0.0, 1.0);
                glScalef(1.0 / 3.0, 1.0 / 4.0, 1.0 / 4.0);
                glTranslatef(0.0, -4.0, -1.5);
                glBegin(GL_TRIANGLE_STRIP);
                    /* left wing */
                    v3f(-7.0, 0.0, 2.0); v3f(-1.0, 0.0, 3.0);
                    glColor3f(red = planes[i].red, green = planes[i].green,
                              blue = planes[i].blue);
                    v3f(-1.0, 7.0, 3.0);
                    /* left side */
                    glColor3f(0.6 * red, 0.6 * green, 0.6 * blue);
                    v3f(0.0, 0.0, 0.0); v3f(0.0, 8.0, 0.0);
                    /* right side */
                    v3f(1.0, 0.0, 3.0); v3f(1.0, 7.0, 3.0);
                    /* final tip of right wing */
                    glColor3f(red, green, blue);
                    v3f(7.0, 0.0, 2.0);
                glEnd();
            glPopMatrix();
        }
    if (doubleBuffer) glXSwapBuffers(dpy, XtWindow(w));
    if(!glXIsDirect(dpy, cx))
        glFinish(); /* avoid indirect rendering latency from queuing */
#ifdef DEBUG
    { /* for help debugging, report any OpenGL errors that occur per frame */
        GLenum error;
        while((error = glGetError()) != GL_NO_ERROR)
            fprintf(stderr, "GL error: %s\n", gluErrorString(error));
    }
#endif
}

void tick_per_plane(int i)
{
    float theta = planes[i].theta += planes[i].speed;
    planes[i].z = -9 + 4 * cos(theta);
    planes[i].x = 4 * sin(2 * theta);
    planes[i].y = sin(theta / 3.4) * 3;
    planes[i].angle = ((atan(2.0) + M_PI_2) * sin(theta) - M_PI_2) * 180 / M_PI;
    if (planes[i].speed < 0.0) planes[i].angle += 180;
}

void add_plane(void)
{
    int i;

    for (i = 0; i < MAX_PLANES; i++)
        if (planes[i].speed == 0) {

#define SET_COLOR(r,g,b) \
        planes[i].red=r; planes[i].green=g; planes[i].blue=b; break;

            switch (random() % 6) {
            case 0: SET_COLOR(1.0, 0.0, 0.0); /* red */
            case 1: SET_COLOR(1.0, 1.0, 1.0); /* white */
            case 2: SET_COLOR(0.0, 1.0, 0.0); /* green */
            case 3: SET_COLOR(1.0, 0.0, 1.0); /* magenta */
            case 4: SET_COLOR(1.0, 1.0, 0.0); /* yellow */
            case 5: SET_COLOR(0.0, 1.0, 1.0); /* cyan */
            }
            planes[i].speed = (random() % 20) * 0.001 + 0.02;
            if (random() & 0x1) planes[i].speed *= -1;
            planes[i].theta = ((float) (random() % 257)) * 0.1111;
            tick_per_plane(i);
            if (!moving) draw(glxarea);
            return;
        }
    XBell(dpy, 100); /* can't add any more planes */
}

void remove_plane(void)
{
    int             i;

    for (i = MAX_PLANES - 1; i >= 0; i--)
        if (planes[i].speed != 0) {
            planes[i].speed = 0;
            if (!moving) draw(glxarea);
            return;
        }
    XBell(dpy, 100); /* no more planes to remove */
}

void resize(Widget w, XtPointer data, XtPointer callData)
{
    Dimension       width, height;

    if(made_current) {
        XtVaGetValues(w, XmNwidth, &width, XmNheight, &height, NULL);
        glViewport(0, 0, (GLint) width, (GLint) height);
    }
}

void tick(void)
{
    int i;

    for (i = 0; i < MAX_PLANES; i++)
        if (planes[i].speed != 0.0) tick_per_plane(i);
}

Boolean animate(XtPointer data)
{
    tick();
    draw(glxarea);
    return False;               /* leave work proc active */
}

void toggle(void)
{
    moving = !moving; /* toggle */
    if (moving)
        workId = XtAppAddWorkProc(app, animate, NULL);
    else
        XtRemoveWorkProc(workId);
}

void quit(Widget w, XtPointer data, XtPointer callData)
{
    exit(0);
}

void input(Widget w, XtPointer data, XtPointer callData)
{
    XmDrawingAreaCallbackStruct *cd = (XmDrawingAreaCallbackStruct *) callData;
    char            buf[1];
    KeySym          keysym;
    int             rc;

    if(cd->event->type == KeyPress)
        if(XLookupString((XKeyEvent *) cd->event, buf, 1, &keysym, NULL) == 1)
            switch (keysym) {
            case XK_space:
                if (!moving) { /* advance one frame if not in motion */
                    tick();
                    draw(w);
                }
                break;
            case XK_Escape:
                exit(0);
            }
}

void map_state_changed(Widget w, XtPointer data, XEvent * event, Boolean * cont)
{
    switch (event->type) {
    case MapNotify:
        if (moving && workId != 0) workId = XtAppAddWorkProc(app, animate, NULL);
        break;
    case UnmapNotify:
        if (moving) XtRemoveWorkProc(workId);
        break;
    }
}

main(int argc, char *argv[])
{
    toplevel = XtAppInitialize(&app, "Paperplane", NULL, 0, &argc, argv,
                               fallbackResources, NULL, 0);
    dpy = XtDisplay(toplevel);
    /* find an OpenGL-capable RGB visual with depth buffer */
    vi = glXChooseVisual(dpy, DefaultScreen(dpy), dblBuf);
    if (vi == NULL) {
        vi = glXChooseVisual(dpy, DefaultScreen(dpy), snglBuf);
        if (vi == NULL)
            XtAppError(app, "no RGB visual with depth buffer");
        doubleBuffer = GL_FALSE;
    }
    /* create an OpenGL rendering context */
    cx = glXCreateContext(dpy, vi, /* no display list sharing */ None,
        /* favor direct */ GL_TRUE);
    if (cx == NULL)
        XtAppError(app, "could not create rendering context");
    /* create an X colormap since probably not using default visual */
#ifdef noGLwidget
    cmap = XCreateColormap(dpy, RootWindow(dpy, vi->screen),
        vi->visual, AllocNone);
    /*
     * Establish the visual, depth, and colormap of the toplevel
     * widget _before_ the widget is realized.
     */
    XtVaSetValues(toplevel, XtNvisual, vi->visual, XtNdepth, vi->depth,
                  XtNcolormap, cmap, NULL);
#endif
    XtAddEventHandler(toplevel, StructureNotifyMask, False,
                      map_state_changed, NULL);
    mainw = XmCreateMainWindow(toplevel, "mainw", NULL, 0);
    XtManageChild(mainw);
    /* create menu bar */
    menubar = XmCreateMenuBar(mainw, "menubar", NULL, 0);
    XtManageChild(menubar);
#ifdef noGLwidget
    /* Hack around Xt's unfortunate default visual inheritance. */
    XtSetArg(menuPaneArgs[0], XmNvisual, vi->visual);
    menupane = XmCreatePulldownMenu(menubar, "menupane", menuPaneArgs, 1);
#else
    menupane = XmCreatePulldownMenu(menubar, "menupane", NULL, 0);
#endif
    btn = XmCreatePushButton(menupane, "Quit", NULL, 0);
    XtAddCallback(btn, XmNactivateCallback, quit, NULL);
    XtManageChild(btn);
    XtSetArg(args[0], XmNsubMenuId, menupane);
    cascade = XmCreateCascadeButton(menubar, "File", args, 1);
    XtManageChild(cascade);
#ifdef noGLwidget
    menupane = XmCreatePulldownMenu(menubar, "menupane", menuPaneArgs, 1);
#else
    menupane = XmCreatePulldownMenu(menubar, "menupane", NULL, 0);
#endif
    btn = XmCreateToggleButton(menupane, "Motion", NULL, 0);
    XtAddCallback(btn, XmNvalueChangedCallback, (XtCallbackProc)toggle, NULL);
    XtManageChild(btn);
    btn = XmCreatePushButton(menupane, "Add plane", NULL, 0);
    XtAddCallback(btn, XmNactivateCallback, (XtCallbackProc)add_plane, NULL);
    XtManageChild(btn);
    btn = XmCreatePushButton(menupane, "Remove plane", NULL, 0);
    XtAddCallback(btn, XmNactivateCallback, (XtCallbackProc)remove_plane, NULL);
    XtManageChild(btn);
    XtSetArg(args[0], XmNsubMenuId, menupane);
    cascade = XmCreateCascadeButton(menubar, "Planes", args, 1);
    XtManageChild(cascade);
    /* create framed drawing area for OpenGL rendering */
    frame = XmCreateFrame(mainw, "frame", NULL, 0);
    XtManageChild(frame);
#ifdef noGLwidget
    glxarea = XtVaCreateManagedWidget("glxarea", xmDrawingAreaWidgetClass,
                                      frame, NULL);
#else
#ifdef noMotifGLwidget
    /* notice glwDrawingAreaWidgetClass lacks an 'M' */
    glxarea = XtVaCreateManagedWidget("glxarea", glwDrawingAreaWidgetClass,
#else
    glxarea = XtVaCreateManagedWidget("glxarea", glwMDrawingAreaWidgetClass,
#endif
                                      frame, GLwNvisualInfo, vi, NULL);
#endif
    XtAddCallback(glxarea, XmNexposeCallback, (XtCallbackProc)draw, NULL);
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
    glClearDepth(1.0);
    glClearColor(0.0, 0.0, 0.0, 0.0);
    glMatrixMode(GL_PROJECTION);
    glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 20);
    glMatrixMode(GL_MODELVIEW);
    /* add three initial random planes */
    srandom(getpid());
    add_plane(); add_plane(); add_plane(); add_plane(); add_plane();
    /* start event processing */
    toggle();
    XtAppMainLoop(app);
}
