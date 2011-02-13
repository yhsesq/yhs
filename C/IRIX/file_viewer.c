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



    yhs_files_open++; 
    ac=0;
    argcount=0;
    manager[yhs_files_open]=XmCreateFormDialog(main_window,file_name,args,argcount);
if(yhs_files_open == 1) {strcpy(yhs_file1,file_name);manage1=XtParent(manager[yhs_files_open]);strcpy(yhs_filename1,yhs_file1); curfile=1; }
if(yhs_files_open == 2) {strcpy(yhs_file2,file_name);manage2=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 3) {strcpy(yhs_file3,file_name);manage3=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 4) {strcpy(yhs_file4,file_name);manage4=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 5) {strcpy(yhs_file5,file_name);manage5=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 6) {strcpy(yhs_file6,file_name);manage6=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 7) {strcpy(yhs_file7,file_name);manage7=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 8) {strcpy(yhs_file8,file_name);manage8=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 9) {strcpy(yhs_file9,file_name);manage9=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 10) { strcpy(yhs_file10,file_name);manage10=XtParent(manager[yhs_files_open]);}
if(yhs_files_open == 11) { strcpy(yhs_file11,file_name);manage11=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 12) { strcpy(yhs_file12,file_name);manage12=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 13) { strcpy(yhs_file13,file_name);manage13=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 14) { strcpy(yhs_file14,file_name);manage14=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 15) { strcpy(yhs_file15,file_name);manage15=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 16) { strcpy(yhs_file16,file_name);manage16=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 17) { strcpy(yhs_file17,file_name);manage17=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 18) { strcpy(yhs_file18,file_name);manage18=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 19) { strcpy(yhs_file19,file_name);manage19=XtParent(manager[yhs_files_open]); }
if(yhs_files_open == 20) { strcpy(yhs_file20,file_name);manage20=XtParent(manager[yhs_files_open]); }
    XtManageChild(manager[yhs_files_open]);
    argcount = 0;
    XtSetArg(args[argcount], XmNwidth ,  IMAGE_WIDTH); argcount++;
    XtSetArg(args[argcount], XmNheight, IMAGE_HEIGHT); argcount++;
    view_image = XmCreateDrawingArea(manager[yhs_files_open], "View", args, argcount);
    XtManageChild(view_image);
    argcount=0;
    XtSetArg(args[argcount], XmNtopOffset ,  10 ); argcount++;
    XtSetArg(args[argcount], XmNrightOffset ,  10 ); argcount++;
    XtSetValues(manager[yhs_files_open],args,(Cardinal) argcount);
    display = XtDisplay(manager[yhs_files_open]);
    screen  = XtScreen(manager[yhs_files_open]);
    window = XtWindow(view_image);
   XMoveWindow(XtDisplay(XtParent(manager[yhs_files_open])),XtWindow(XtParent(manager[yhs_files_open])),
   150+(20*yhs_files_open),150+(20*yhs_files_open)); 
    if (engage_false_colour != 1)
    {  XSetWindowColormap(display,window,ysmap);     }
	list_w = (Widget *) malloc(2*sizeof(Widget));
	list_w[0] = view_image;
	XtSetWMColormapWindows(top_level, list_w, (Cardinal) 1);
    new_visual = DefaultVisual(display, DefaultScreen(display));
    arrayGC[yhs_files_open] = XCreateGC(display, window, 8, &xgcv);
    xgcv.foreground = fg ^ bg;
    xgcv.background = bg;
    xgcv.function = GXxor;
    ysxorGC[yhs_files_open] = XtGetGC(view_image, GCForeground | GCBackground | GCFunction, &xgcv);
    xgcv.foreground = fg;
    xgcv.background = bg;
    ystheGC[yhs_files_open] = XtGetGC(view_image, GCForeground | GCBackground, &xgcv);
    view[yhs_files_open]=view_image;
    if(yhs_files_open == 1)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &one);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&one);
}
    if(yhs_files_open == 2)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &two);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&two);
}
    if(yhs_files_open == 3)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &three);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&three);
}
    if(yhs_files_open == 4)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &four);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&four);
}
    if(yhs_files_open == 5)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &five);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&five);
}
    if(yhs_files_open == 6)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &six);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&six);
}
    if(yhs_files_open == 7)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &seven);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&seven);
}
    if(yhs_files_open == 8)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &eight);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&eight);
}
    if(yhs_files_open == 9)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &nine);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&nine);
}
    if(yhs_files_open == 10)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &ten);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&ten);
}
    if(yhs_files_open == 11)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &eleven);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&eleven);
}
    if(yhs_files_open == 12)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &twelve);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&twelve);
}
    if(yhs_files_open == 13)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &thirteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&thirteen);
}
    if(yhs_files_open == 14)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &fourteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&fourteen);
}
    if(yhs_files_open == 15)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &fifteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&fifteen);
}
    if(yhs_files_open == 16)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &sixteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&sixteen);
}
    if(yhs_files_open == 17)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &seventeen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&seventeen);
}
    if(yhs_files_open == 18)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &eighteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&eighteen);
}
    if(yhs_files_open == 19)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &nineteen);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&nineteen);
}
    if(yhs_files_open == 20)
{    XtAddCallback(view_image, XmNexposeCallback, (XtCallbackProc) handle_expose_y, &twenty);
XtAddCallback(XtVaCreateManagedWidget(".",xmPushButtonWidgetClass,manager[yhs_files_open],XmNlabelString,XmStringCreateLtoR("",XmSTRING_DEFAULT_CHARSET),NULL),XmNactivateCallback,(XtCallbackProc)squish,&twenty);
}
   image_bytes = 512 * 512;
   if((pfile = fopen(file_name,"r")) == NULL)
    {   fprintf(stderr, "Cannot open: %s.\n", file_name);   exit(1);  }
    if((image_y = (unsigned char *) calloc(image_bytes, sizeof(unsigned char) )) == NULL)
    {   fprintf(stderr, "Error allocating room for image...\n");  exit(1);    }
    i=0;
    for (a=0;a<512;a++){ for (b=0;b<512;b++) {
    if (resize != 1) { image_y[i] = getc(pfile); }
    if (resize == 1) { image_y[i] = array2[a][b]; array2[a][b]=0; }
    i++;}}
    fclose(pfile);
    theXImage_y[yhs_files_open] = XCreateImage(XtDisplay(view_image), theVisual, /* vis_depth */ 8, 
                    ZPixmap, 0, (char *)image_y, 512,
                    512, 8, 0);
    theXImage_y[yhs_files_open]->byte_order = MSBFirst;
    thePixmap_y[yhs_files_open] = XCreatePixmap(XtDisplay(view_image), XtWindow(view_image),
                       theXImage_y[yhs_files_open]->width, theXImage_y[yhs_files_open]->height, /* vis_depth */ 8);
    XPutImage(XtDisplay(view_image), thePixmap_y[yhs_files_open], arrayGC[yhs_files_open], theXImage_y[yhs_files_open], 
              0, 0, 0, 0, theXImage_y[yhs_files_open]->width, theXImage_y[yhs_files_open]->height);
   XClearArea(XtDisplay(manager[yhs_files_open]),XtWindow(view_image),0,0,0,0,True);
   XCopyArea(XtDisplay(view_image), thePixmap_y[yhs_files_open], XtWindow(view_image), arrayGC[yhs_files_open],
                 0, 0, 512, 512, 0, 0);
