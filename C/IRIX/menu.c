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


/*-------------------------------------------------------------*/
/*                                                             */
/*     File: menu.c                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include "main.h"
#include "logmen.h"
#include <Xm/Xm.h>
#include <Xm/PushB.h>
#include <Xm/CascadeB.h>
#include <Xm/Separator.h>
#include <Xm/RowColumn.h>
#include <Xm/DrawnB.h>

/* Extern variables */
extern Widget top_level, menu_bar;
Pixmap cov = (Pixmap) NULL;
Widget flasher_button;
extern void create_open_dialog(Widget parent);
extern void activate_open_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_view_dialog(Widget parent);
extern void activate_view_dialog(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);

extern void create_save_as_interface(Widget parent);
extern void save_as_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void print_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_exit_dialog(Widget parent);
extern void activate_exit_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void histogram_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void histo_eq_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_threshold_dialog(Widget parent);
extern void activate_threshold_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void mean_filter_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void median_filter_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_smooth_t_dialog(Widget parent);
extern void activate_smooth_t_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void smooth_mask1_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void smooth_mask2_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void median_vf_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);


extern void create_region_dialog(Widget parent);
extern void activate_region_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void activate_image_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_sigma_dialog(Widget parent);
extern void create_split_dialog(Widget parent);
extern void activate_split_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_sobel_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_hough_dialog(Widget parent);
extern void activate_hough_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_prewitt_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_frei_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_marr_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_roberts_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void edge_vert_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);


extern void edge_unsharp_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void activate_total_gradient_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void mixed_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern int region_cut_callback(Widget w, XtPointer client_data, 
				XmAnyCallbackStruct *call_data);

extern int rectangles_callback(Widget w, XtPointer client_data,
				XmDrawingAreaCallbackStruct *call_data);

extern int ellipses_callback(Widget w, XtPointer client_data,
				XmDrawingAreaCallbackStruct *call_data);

extern int circles_callback(Widget w, XtPointer client_data,
				XmDrawingAreaCallbackStruct *call_data);

extern int tracking_callback(Widget w, XtPointer client_data,
				XmDrawingAreaCallbackStruct *call_data);

void create_options_menu (Widget parent);
extern void create_editing_tools_window (Widget parent);
extern void activate_editing_tools_window(Widget w, XtPointer client_data, 
                             XmAnyCallbackStruct *call_data);
extern void create_zoom_dialog(Widget parent);
extern void activate_zoom_dialog(Widget w, XtPointer client_data, 
				XmAnyCallbackStruct *call_data);

extern void activate_area_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void activate_perimeter_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void activate_lenght_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void activate_ctomass_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void activate_moments_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void multi_window_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void multi_merge_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void inverse_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void delete_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void dump_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void reduce_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void red_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void cascade_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void tile_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void operate_callback(Widget w, XtPointer client_data,
                                XmAnyCallbackStruct *call_data);
extern void activate_circular_callback(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);

extern void create_version_dialog(Widget parent);
extern void activate_version_dialog(Widget w, XtPointer client_data,
				XmAnyCallbackStruct *call_data);
extern void create_edge_hv_dialog(Widget parent);
extern void activate_edge_hv_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data);
extern void create_edge_dilate_dialog(Widget parent);
extern void activate_edge_dilate_dialog(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data);
extern void help_moi(Widget w, XtPointer client_data,
			       XmAnyCallbackStruct *call_data);



/* Variables for setting resources */
static Arg args[MAXARGS];
static Cardinal argcount;

/* Function prototype */
void create_main_menu(Widget parent);
void create_file_menu(Widget parent);
void create_preprocessing_menu(Widget parent);
void create_segmentation_menu(Widget parent);
void create_tools_menu(Widget parent);
void create_feature_menu(Widget parent);
void create_about_menu(Widget parent);
void create_window_menu(Widget parent);
void create_multi_menu(Widget parent);
void create_flasher(Widget parent);

/* Function definition */
void create_main_menu(Widget parent)
{
    /* Create the menu bar */
    argcount = 0;   
    menu_bar = XmCreateMenuBar(parent, "Menubar", args, argcount);
    XtManageChild(menu_bar);
    argcount=0;
    /* Attach submenus to it */
    create_file_menu(menu_bar);
    create_preprocessing_menu(menu_bar);
    create_segmentation_menu(menu_bar);
    create_tools_menu(menu_bar); 
    create_feature_menu(menu_bar);
    create_multi_menu(menu_bar);
    create_window_menu(menu_bar);
    create_about_menu(menu_bar);
    create_flasher(menu_bar);
}

void create_file_menu(Widget parent)
{
    Widget file_menu_pane, file_menu_button, open_butto, open_pane, 
           open_button, view_button, file_sep_1, save_as_button, 
           file_sep_2, print_button, file_sep_3, exit_button,print_pane,
           print_menu_button;

    /* Create the menu pane for the menu */
    file_menu_pane = XmCreatePulldownMenu(parent, "file menu pane", NULL, 0);
    print_pane = XmCreatePulldownMenu(parent, "Print menu pane", NULL, 0);
    /* Populate the menu pane */
    /* Open button */
    open_pane = XmCreatePulldownMenu(file_menu_pane, "open menu pane", NULL, 0);
    open_button = XmCreatePushButton(open_pane, "Open a study of 10 images", NULL, 0);
    XtManageChild(open_button); 
    view_button = XmCreatePushButton(open_pane, "Open a single RAW file", NULL, 0);
    XtManageChild(view_button);

      /* Create the submenus for open.     */
     argcount = 0;
     XtSetArg(args[argcount], XmNsubMenuId, open_pane); argcount++;
     open_butto = XmCreateCascadeButton(file_menu_pane, "Open", args, argcount);
     XtManageChild(open_butto);

    /* Create the open file dialog and add the activation function as
       the open button's activate callback */
    create_open_dialog(parent);
    XtAddCallback(open_button, XmNactivateCallback,
		(XtCallbackProc) activate_open_dialog, (XtPointer) NULL);

     create_view_dialog(parent);
     XtAddCallback(view_button, XmNactivateCallback,
                (XtCallbackProc) activate_view_dialog, (XtPointer) NULL);

    /* Separator 1 */
    file_sep_1 = XmCreateSeparator(file_menu_pane, "file sep 1", NULL, 0);
    XtManageChild(file_sep_1);

    /* Save As button */
    save_as_button = XmCreatePushButton(file_menu_pane, "Save As ...", NULL, 0);
    XtManageChild(save_as_button);

    /* Create the save_as dialog and add the activation function as
       the save_as button's activate callback */
    create_save_as_interface(parent);
    XtAddCallback(save_as_button, XmNactivateCallback,
		(XtCallbackProc) save_as_callback, (XtPointer) NULL);

    /* Separator 3 */
    file_sep_3 = XmCreateSeparator(file_menu_pane, "file sep 3", NULL, 0);
    XtManageChild(file_sep_3);

    /* Print button */
    print_button = XmCreatePushButton(print_pane, "Print f1 => f1.PS", NULL, 0);
    XtManageChild(print_button);
    XtAddCallback(print_button, XmNactivateCallback,
		(XtCallbackProc) print_callback, (XtPointer) NULL);

    /* Separator 2 */
    file_sep_2 = XmCreateSeparator(file_menu_pane, "file sep 2", NULL, 0);
    XtManageChild(file_sep_2);

    /* Exit button */
    exit_button = XmCreatePushButton(file_menu_pane, "Exit ...", NULL, 0);
    XtManageChild(exit_button);

    /* Create the exit dialog and add the activation function as the exit
       button's activate callback */
    create_exit_dialog(parent);
    XtAddCallback(exit_button, XmNactivateCallback,
		(XtCallbackProc) activate_exit_dialog, (XtPointer) NULL);

    /* Create the file cascade button on the menu bar and attach the *
     * file menu pane to it.                                         */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, file_menu_pane); argcount++;
    file_menu_button = XmCreateCascadeButton(parent, "File ", 
						args, argcount);
    XtManageChild(file_menu_button);
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, print_pane); argcount++;
    print_menu_button = XmCreateCascadeButton(parent, "Print ", 
						args, argcount);
    XtManageChild(print_menu_button);
}


void create_preprocessing_menu(Widget parent)
{
    Widget preprocessing_menu_pane, preprocessing_menu_button, histogram_button, preprocessing_sep_1,
	   histo_eq_button, preprocessing_sep_2, threshold_button, preprocessing_sep_3,
	   filter_button, filter_pane, mean_filter_button,
	   median_filter_button, smooth_t_button, smooth_mask1_button,
	   smooth_mask2_button, median_vf_button;

    /* Create the menu pane for the menu */
    preprocessing_menu_pane = XmCreatePulldownMenu(parent, "preprocessing menu pane", NULL, 0);

    /* Populate the menu pane */
    /* Histogram button */
    histogram_button = XmCreatePushButton(preprocessing_menu_pane, "Histogram", NULL, 0);
    XtManageChild(histogram_button);

    XtAddCallback(histogram_button, XmNactivateCallback,
		(XtCallbackProc) histogram_callback, (XtPointer) NULL);

    /* Separator 1 */
    preprocessing_sep_1 = XmCreateSeparator(preprocessing_menu_pane,
					"preprocessing sep 1", NULL, 0);
    XtManageChild(preprocessing_sep_1);

    /* Histogram equalization button */
    histo_eq_button = XmCreatePushButton(preprocessing_menu_pane,
					"Histogram Equalization", NULL, 0);
    XtManageChild(histo_eq_button);

    XtAddCallback(histo_eq_button, XmNactivateCallback,
		(XtCallbackProc) histo_eq_callback, (XtPointer) NULL);

    /* Separator 2 */
    preprocessing_sep_2 = XmCreateSeparator(preprocessing_menu_pane,
					"preprocessing sep 2", NULL, 0);
    XtManageChild(preprocessing_sep_2);

    /* Threshold button */
    threshold_button = XmCreatePushButton(preprocessing_menu_pane,
					"Threshold", NULL, 0);
    XtManageChild(threshold_button);

    /* Create the threshold dialog and add the activation function as the
       threshold button's activate callback */
    create_threshold_dialog(parent);
    XtAddCallback(threshold_button, XmNactivateCallback,
		(XtCallbackProc) activate_threshold_dialog, (XtPointer) NULL);

    /* Separator 3 */
    preprocessing_sep_3 = XmCreateSeparator(preprocessing_menu_pane,
					"preprocessing sep 3", NULL, 0);
    XtManageChild(preprocessing_sep_3);

    /* Filter button */
    filter_pane = XmCreatePulldownMenu(preprocessing_menu_pane,
					"filter pane", NULL, 0);

    mean_filter_button = XmCreatePushButton(filter_pane, "Mean Filter", NULL, 0);
    XtManageChild(mean_filter_button);
    XtAddCallback(mean_filter_button, XmNactivateCallback,
		(XtCallbackProc) mean_filter_callback, (XtPointer) NULL);

    median_filter_button = XmCreatePushButton(filter_pane,
					"Median Filter", NULL, 0);
    XtManageChild(median_filter_button);
    XtAddCallback(median_filter_button, XmNactivateCallback,
		(XtCallbackProc) median_filter_callback, (XtPointer) NULL);


    smooth_t_button = XmCreatePushButton(filter_pane, "Smooth T", NULL, 0);
    XtManageChild(smooth_t_button);
    create_smooth_t_dialog(parent);   /* Dialogo */
    XtAddCallback(smooth_t_button, XmNactivateCallback,
		(XtCallbackProc) activate_smooth_t_dialog, (XtPointer) NULL);

    smooth_mask1_button = XmCreatePushButton(filter_pane,
					"Smooth .. Mask1", NULL, 0);
    XtManageChild(smooth_mask1_button);
    XtAddCallback(smooth_mask1_button, XmNactivateCallback,
		(XtCallbackProc) smooth_mask1_callback, (XtPointer) NULL);

    smooth_mask2_button = XmCreatePushButton(filter_pane,
					"Smooth .. Mask2", NULL, 0);
    XtManageChild(smooth_mask2_button);
    XtAddCallback(smooth_mask2_button, XmNactivateCallback,
		(XtCallbackProc) smooth_mask2_callback, (XtPointer) NULL);

    median_vf_button = XmCreatePushButton(filter_pane,
					"Median VF", NULL, 0);
    XtManageChild(median_vf_button);
    XtAddCallback(median_vf_button, XmNactivateCallback,
		(XtCallbackProc) median_vf_callback, (XtPointer) NULL);

    /* Submenus del Filter */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, filter_pane); argcount++;
    filter_button = XmCreateCascadeButton(preprocessing_menu_pane,
					"Filter ", args, argcount );
    XtManageChild(filter_button);


   /* Create the preprocessing cascade button on the menu bar and attach the *
    * preprocessing menu pane to it.                                         */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, preprocessing_menu_pane); argcount++;
    preprocessing_menu_button = XmCreateCascadeButton(parent,
				"Pre-processing ", args, argcount);
/*    XtManageChild(preprocessing_menu_button); */
}


void create_segmentation_menu(Widget parent)
{
  Widget segmentation_menu_pane, segmentation_menu_button,
	 region_detection_button, region_detection_pane, region_growing_button,
	 grow_image_button, watershed_button, split_merge_button,
	 segmentation_sep_1, edge_detection_button, edge_detection_pane,
	 edge_sobel_button, hough_button, edge_prewitt_button, edge_frei_button,
	 edge_marr_button, edge_roberts_button, edge_vert_button,
	 edge_horiz_button, edge_hv_button, edge_unsharp_button,
	 segmentation_sep_2, mixed_methods_button, mixed_methods_pane, mixed_button,
	 region_growing_pane, grow_region_button;


   /* Create the menu pane for the menu */
  segmentation_menu_pane = XmCreatePulldownMenu(parent, "segmentation menu pane ", NULL, 0);

  /* Populate the menu pane */
  /* Region detection button */
  region_detection_pane = XmCreatePulldownMenu(segmentation_menu_pane, "region detection pane",
                              NULL, 0);


    /* Region growing button */
    region_growing_pane = XmCreatePulldownMenu(region_detection_pane,
				"region growing pane",NULL, 0);
	grow_region_button = XmCreatePushButton(region_growing_pane,
				"Grow Regions in 3D ... ", NULL, 0);
	create_region_dialog(parent);
	XtManageChild(grow_region_button);
	XtAddCallback(grow_region_button, XmNactivateCallback,
		(XtCallbackProc) activate_region_dialog, (XtPointer) NULL);

	grow_image_button = XmCreatePushButton(region_growing_pane,
				"Grow Images in 3D ... ", NULL, 0);
	XtManageChild(grow_image_button);
	XtAddCallback(grow_image_button, XmNactivateCallback,
		(XtCallbackProc) activate_image_dialog, (XtPointer) NULL);

    /* Submenus of Region Growing*/
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, region_growing_pane); argcount++;
    region_growing_button = XmCreateCascadeButton(region_detection_pane,
				"Region Growing", args, argcount );
    XtManageChild(region_growing_button);

    watershed_button = XmCreatePushButton(region_detection_pane, "Watershed ", NULL, 0);
    XtManageChild(watershed_button);
    create_sigma_dialog(parent);
    XtAddCallback(watershed_button, XmNactivateCallback,
		(XtCallbackProc) activate_total_gradient_dialog, (XtPointer) NULL);

    split_merge_button = XmCreatePushButton(region_detection_pane, "Split-Merge", NULL, 0);
    create_split_dialog(parent);
/*    XtManageChild(split_merge_button); */
    XtAddCallback(split_merge_button, XmNactivateCallback,
		(XtCallbackProc) activate_split_dialog, (XtPointer) NULL);

 
/* Submenus of Region */
  argcount = 0;
  XtSetArg(args[argcount], XmNsubMenuId, region_detection_pane); argcount++;
  region_detection_button = XmCreateCascadeButton(segmentation_menu_pane, "Region Detection",
       args, argcount );
  XtManageChild(region_detection_button); 


  /* Separator 1 */
  segmentation_sep_1 = XmCreateSeparator(segmentation_menu_pane, "segmentation sep 1", NULL, 0);
  XtManageChild(segmentation_sep_1);
  
  
  /* Edge detection button */
  edge_detection_pane = XmCreatePulldownMenu(segmentation_menu_pane, "edge detection pane", NULL, 0);

  edge_sobel_button = XmCreatePushButton(edge_detection_pane, "Edge Sobel", NULL, 0);
/*  XtManageChild(edge_sobel_button); */
  XtAddCallback(edge_sobel_button, XmNactivateCallback,
		(XtCallbackProc) edge_sobel_callback, (XtPointer) NULL);

  hough_button = XmCreatePushButton(edge_detection_pane, "Hough", NULL, 0);
/*  XtManageChild(hough_button); */
  create_hough_dialog(parent); /* Dialogo*/
  XtAddCallback(hough_button, XmNactivateCallback,
		(XtCallbackProc) activate_hough_dialog, (XtPointer) NULL);

  edge_prewitt_button = XmCreatePushButton(edge_detection_pane, "Edge Prewitt", NULL, 0);
/*  XtManageChild(edge_prewitt_button); */
  XtAddCallback(edge_prewitt_button, XmNactivateCallback,
		(XtCallbackProc) edge_prewitt_callback, (XtPointer) NULL);

  edge_frei_button = XmCreatePushButton(edge_detection_pane, "Edge Frei", NULL, 0);
/*  XtManageChild(edge_frei_button); */
  XtAddCallback(edge_frei_button, XmNactivateCallback,
		(XtCallbackProc) edge_frei_callback, (XtPointer) NULL);

  edge_marr_button = XmCreatePushButton(edge_detection_pane, "Edge Marr", NULL, 0);
/*  XtManageChild(edge_marr_button); */
  XtAddCallback(edge_marr_button, XmNactivateCallback,
		(XtCallbackProc) edge_marr_callback, (XtPointer) NULL);

  edge_roberts_button = XmCreatePushButton(edge_detection_pane, "Edge Roberts", NULL, 0);
/*  XtManageChild(edge_roberts_button); */
  XtAddCallback(edge_roberts_button, XmNactivateCallback,
		(XtCallbackProc) edge_roberts_callback, (XtPointer) NULL);

  edge_vert_button = XmCreatePushButton(edge_detection_pane, "Edge Vert.", NULL, 0);
/*  XtManageChild(edge_vert_button); */
  XtAddCallback(edge_vert_button, XmNactivateCallback,
		(XtCallbackProc) edge_vert_callback, (XtPointer) NULL);

  edge_horiz_button = XmCreatePushButton(edge_detection_pane, "Erode", NULL, 0);
  XtManageChild(edge_horiz_button);
 create_edge_dilate_dialog(parent);
  XtAddCallback(edge_horiz_button, XmNactivateCallback,
		(XtCallbackProc) activate_edge_dilate_dialog, (XtPointer) NULL);

  edge_hv_button = XmCreatePushButton(edge_detection_pane, "Dilate", NULL, 0);
  XtManageChild(edge_hv_button);
  create_edge_hv_dialog(parent);
  XtAddCallback(edge_hv_button, XmNactivateCallback,
		(XtCallbackProc) activate_edge_hv_dialog, (XtPointer) NULL);

  edge_unsharp_button = XmCreatePushButton(edge_detection_pane, "Edge Unsharp", NULL, 0);
/*  XtManageChild(edge_unsharp_button); */
  XtAddCallback(edge_unsharp_button, XmNactivateCallback,
		(XtCallbackProc) edge_unsharp_callback, (XtPointer) NULL);


  /* Submenus del Edge */
  argcount = 0;
  XtSetArg(args[argcount], XmNsubMenuId, edge_detection_pane); argcount++;
  edge_detection_button = XmCreateCascadeButton(segmentation_menu_pane, "Edge Detection",
	  args, argcount );
  XtManageChild(edge_detection_button);


  /* Separator 2 */
  segmentation_sep_2 = XmCreateSeparator(segmentation_menu_pane, "segmentation sep 2", NULL, 0);
  XtManageChild(segmentation_sep_2);


  /* Mixed button */
  mixed_methods_pane = XmCreatePulldownMenu(segmentation_menu_pane, "mixed methods pane", NULL, 0);

  mixed_button = XmCreatePushButton(mixed_methods_pane, "Mixed Methods", NULL, 0);
/*  XtManageChild(mixed_button); */
/* XtAddCallback(mixed_button, XmNactivateCallback,
		(XtCallbackProc) mixed_callback, (XtPointer) NULL);
*/
 /* Submenus del Mixed */
  argcount = 0;
  XtSetArg(args[argcount], XmNsubMenuId, mixed_methods_pane); argcount++;
  mixed_methods_button = XmCreateCascadeButton(segmentation_menu_pane, "Mixed Methods",
	  args, argcount );
/*  XtManageChild(mixed_methods_button); */


  /* Create the segmentation cascade button on the menu bar and */
  /* attach the about menu pane to it.                   */
  argcount = 0;
  XtSetArg(args[argcount], XmNsubMenuId, segmentation_menu_pane); argcount++;
  segmentation_menu_button = XmCreateCascadeButton(parent, "Segmentation ",
					      args, argcount);
  XtManageChild(segmentation_menu_button);
 
}



void create_tools_menu(Widget parent)
{
    Widget tools_menu_pane, tools_menu_button,
	   editing_tools_button,  region_cut_button, zoom_button,
	   tools_sep_1, tools_sep_2;

    /* Create the menu pane for the menu */
    tools_menu_pane = XmCreatePulldownMenu(parent, "tools menu pane", NULL, 0);

    /* Create the representation cascade button on the menu bar and */
 
    /* Editing tools button */
    editing_tools_button = XmCreatePushButton(tools_menu_pane,
				"Editing Tools ", NULL, 0);
    XtManageChild(editing_tools_button);

    /* Create the editing tools window and add the activation function as */
    /* the editing tools button's activate callback.			*/
       create_editing_tools_window(parent);
    XtAddCallback(editing_tools_button, XmNactivateCallback,
		(XtCallbackProc) activate_editing_tools_window, (XtPointer) NULL);
    
    /* Separator 1 */
    tools_sep_1 = XmCreateSeparator(tools_menu_pane, "tools sep 1", NULL, 0);
    XtManageChild(tools_sep_1);

    /* Region cut button*/
    region_cut_button = XmCreatePushButton(tools_menu_pane,
				"Region Cut", NULL, 0);
/*    XtManageChild(region_cut_button); */
    XtAddCallback(region_cut_button, XmNactivateCallback,
		(XtCallbackProc) region_cut_callback, (XtPointer) NULL);
  
    /* Separator 2 */
    tools_sep_2 = XmCreateSeparator(tools_menu_pane, "tools sep 2", NULL, 0);
    XtManageChild(tools_sep_2);

    /* Zoom button */
    zoom_button = XmCreatePushButton(tools_menu_pane, "Zoom ... ", NULL, 0);
    XtManageChild(zoom_button);
    create_zoom_dialog(parent);
    XtAddCallback(zoom_button, XmNactivateCallback,
		(XtCallbackProc) activate_zoom_dialog, (XtPointer) NULL);

    /* attach the tools menu pane to it. */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, tools_menu_pane); argcount++;
    tools_menu_button = XmCreateCascadeButton(parent, "Tools ", args, argcount);

    XtManageChild(tools_menu_button);
}



void create_feature_menu(Widget parent)
{
    Widget feature_menu_pane, feature_button, area_button, feature_sep_1, perimeter_button, 
	   feature_sep_2, ctomass_button, feature_sep_3, moments_button, feature_sep_4,
	   circular_button;

    /* Create the menu pane for the menu */
    feature_menu_pane = XmCreatePulldownMenu(parent, "feature menu pane", NULL, 0);

    /* Populate the menu pane */

    area_button = XmCreatePushButton(feature_menu_pane, "Area", NULL, 0);
    XtManageChild(area_button);
    XtAddCallback(area_button, XmNactivateCallback,
		(XtCallbackProc) activate_area_callback, (XtPointer) NULL);

    /* Separator 1 */
    feature_sep_1 = XmCreateSeparator(feature_menu_pane, "feature sep 1", NULL, 0);
    XtManageChild(feature_sep_1);

    perimeter_button = XmCreatePushButton(feature_menu_pane, "Perimeter", NULL, 0);
    XtManageChild(perimeter_button);
    XtAddCallback(perimeter_button, XmNactivateCallback,
		(XtCallbackProc) activate_perimeter_callback, (XtPointer) NULL);

   /* Separator 2*/
    feature_sep_2 = XmCreateSeparator(feature_menu_pane, "feature sep 2", NULL, 0);
    XtManageChild(feature_sep_2);

    ctomass_button = XmCreatePushButton(feature_menu_pane, "Ctomass", NULL, 0);
    XtManageChild(ctomass_button);
    XtAddCallback(ctomass_button, XmNactivateCallback,
		(XtCallbackProc) activate_ctomass_callback, (XtPointer) NULL);

   /* Separator3*/
    feature_sep_3 = XmCreateSeparator(feature_menu_pane, "feature sep 3", NULL, 0);
    XtManageChild(feature_sep_3);

    moments_button = XmCreatePushButton(feature_menu_pane, "Moments", NULL, 0);
    XtManageChild(moments_button);
    XtAddCallback(moments_button, XmNactivateCallback,
		NULL /*(XtCallbackProc) activate_moments_callback */, (XtPointer) NULL);

    /* Separator4*/
    feature_sep_4 = XmCreateSeparator(feature_menu_pane, "feature sep 4", NULL, 0);
    XtManageChild(feature_sep_4);

    circular_button = XmCreatePushButton(feature_menu_pane, "Circular", NULL, 0);
    XtManageChild(circular_button);
    XtAddCallback(circular_button, XmNactivateCallback,
	(XtCallbackProc) activate_circular_callback, (XtPointer) NULL);


    /* Create the featurea cascade button on the menu bar and */
    /* attach the about menu pane to it.                   */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, feature_menu_pane); argcount++;
    feature_button = XmCreateCascadeButton(parent, "Feature ", args, argcount);
/*    XtManageChild(feature_button); */
}

void create_multi_menu(Widget parent)
{
    Widget multi_menu_pane, inverse_button, multi_menu_button, twofile_button, merg_button;

    /* Create the menu pane for the menu */
    multi_menu_pane = XmCreatePulldownMenu(parent, "MultiImage ", NULL, 0);

    /* Populate the menu pane */
    /* Version button */
    twofile_button = XmCreatePushButton(multi_menu_pane, "Compare f1&f2=>f3", NULL, 0);
    XtManageChild(twofile_button);
    merg_button = XmCreatePushButton(multi_menu_pane, "Merge f1&f2=>f3", NULL, 0);
    XtManageChild(merg_button);

    /* Create the version dialog and add the activation function as */
    /* the version button's activate callback.                      */
    XtAddCallback(twofile_button, XmNactivateCallback,
		(XtCallbackProc) multi_window_callback, (XtPointer) NULL);
    XtAddCallback(merg_button, XmNactivateCallback,
		(XtCallbackProc) multi_merge_callback, (XtPointer) NULL);
    inverse_button = XmCreatePushButton(multi_menu_pane, "Inverse f1=>-f1", NULL, 0);
    XtManageChild(inverse_button);

    /* Create the version dialog and add the activation function as */
    /* the version button's activate callback.                      */
    XtAddCallback(inverse_button, XmNactivateCallback,
		(XtCallbackProc) inverse_callback, (XtPointer) NULL);

    /* Create the about cascade button on the menu bar and */
    /* attach the about menu pane to it.                   */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, multi_menu_pane); argcount++;
    multi_menu_button = XmCreateCascadeButton(parent, "MultiImage ", args, argcount);
    XtManageChild(multi_menu_button);
}


void create_window_menu(Widget parent)
{
    Widget dump_button, window_menu_pane, window_menu_button, operate_pane, operate_menu_button, cascade_button, tile_button, operate_button, delete_button, reduce_button;

    /* Create the menu pane for the menu */
    window_menu_pane = XmCreatePulldownMenu(parent, "Window ", NULL, 0);
    operate_pane = XmCreatePulldownMenu(parent, "Operate ", NULL, 0);

    /* Populate the menu pane */
    /* Version button */
    cascade_button = XmCreatePushButton(window_menu_pane, "Cascade", NULL, 0);
    XtManageChild(cascade_button);
    XtAddCallback(cascade_button, XmNactivateCallback,
		(XtCallbackProc) cascade_callback, (XtPointer) NULL); 
    tile_button = XmCreatePushButton(window_menu_pane, "Tile", NULL, 0);
    XtManageChild(tile_button);
    XtAddCallback(tile_button, XmNactivateCallback,
		(XtCallbackProc) tile_callback, (XtPointer) NULL); 
    operate_button = XmCreatePushButton(operate_pane, "Operate on", NULL, 0);
    XtManageChild(operate_button);
    XtAddCallback(operate_button, XmNactivateCallback,
		(XtCallbackProc) operate_callback, (XtPointer) NULL);
    delete_button = XmCreatePushButton(window_menu_pane, "Deallocate one file", NULL, 0);
    XtManageChild(delete_button);
    XtAddCallback(delete_button, XmNactivateCallback,
		(XtCallbackProc) delete_callback, (XtPointer) NULL);
    dump_button = XmCreatePushButton(window_menu_pane, "Deallocate all files", NULL, 0);
    XtManageChild(dump_button);
    XtAddCallback(dump_button, XmNactivateCallback,
		(XtCallbackProc) dump_callback, (XtPointer) NULL);
   reduce_button = XmCreatePushButton(window_menu_pane, "Reduce/Enlarge all files", NULL, 0);
    XtManageChild(reduce_button); 
    XtAddCallback(reduce_button, XmNactivateCallback,
		(XtCallbackProc) red_callback, (XtPointer) NULL); 

   argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, operate_pane); argcount++;
    operate_menu_button = XmCreateCascadeButton(parent, "Selector ", args, argcount);
    XtManageChild(operate_menu_button);
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, window_menu_pane); argcount++;
    window_menu_button = XmCreateCascadeButton(parent, "Window ", args, argcount);
    XtManageChild(window_menu_button);
}


void create_about_menu(Widget parent)
{
    Widget about_menu_pane, about_menu_button, version_button,help_button;

    /* Create the menu pane for the menu */
    about_menu_pane = XmCreatePulldownMenu(parent, "Help ", NULL, 0);
    help_button = XmCreatePushButton(about_menu_pane, "Help", NULL, 0);
    XtManageChild(help_button);

    /* Populate the menu pane */
    /* Version button */
    version_button = XmCreatePushButton(about_menu_pane, "Version", NULL, 0);
    XtManageChild(version_button);

    /* Create the version dialog and add the activation function as */
    /* the version button's activate callback.                      */
    create_version_dialog(parent);
    XtAddCallback(version_button, XmNactivateCallback,
		(XtCallbackProc) activate_version_dialog, (XtPointer) NULL);
    XtAddCallback(help_button, XmNactivateCallback,
		(XtCallbackProc) help_moi, (XtPointer) NULL);

    /* Create the about cascade button on the menu bar and */
    /* attach the about menu pane to it.                   */
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, about_menu_pane); argcount++;
    about_menu_button = XmCreateCascadeButton(parent, "Help ", args, argcount);
    XtManageChild(about_menu_button);
}

void create_flasher(Widget parent)
{
    Widget flasher_pane;
    Display *display;
    /* Create the menu pane for the menu */
    flasher_pane = XmCreatePulldownMenu(parent, "Flasher", NULL, 0);
      display=XtDisplay(parent); 
      cov = XCreatePixmapFromBitmapData(display,
                       DefaultRootWindow(display),
                       log_bits, log_width, log_height,
                       BlackPixel(display,DefaultScreen(display)),
                       WhitePixel(display,DefaultScreen(display)),
                       DefaultDepth(display,DefaultScreen(display)));
    argcount = 0;
    XtSetArg(args[argcount], XmNsubMenuId, flasher_pane); argcount++;
    XtSetArg(args[argcount], XmNmarginHeight, 0); argcount++;
    XtSetArg(args[argcount], XmNlabelType,XmPIXMAP); argcount++;
    XtSetArg(args[argcount], XmNlabelPixmap, cov); argcount++;
    flasher_button = XmCreateCascadeButton(parent,NULL, args, argcount);
    XtManageChild(flasher_button);
}
