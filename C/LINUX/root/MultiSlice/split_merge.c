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


/*--------------------------------------------------------------*/
/*                                                              */
/*     File: split_merge.c                                      */
/*                                                              */
/*     CT Image Application Program                             */
/*                                                              */
/*     OSF/Motif version.                                       */
/*                                                              */
/*     V1.0 March 1997						*/
/*                                                              */
/*--------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>
#include <Xm/SelectioB.h>

#define SPLIT_MESSAGE "Enter maximum intensity variation"
#define MERGE_MESSAGE "Enter range for merging process"

#define UP_LEFT    0
#define UP_RIGHT   1
#define DOWN_LEFT  2
#define DOWN_RIGHT 3


/* Extern variables */
extern char 	*action;
extern int 	selection;
extern XImage 	*theXImage_1;
extern XImage 	*theXImage_2;
extern void 	refresh_action(void);
extern Widget 	draw_1, draw_2, main_window;
extern Pixmap   thePixmap_1;
extern Pixmap   thePixmap_2;
extern Cursor   theCursor;
extern GC       image_gc_2, theGC;
extern unsigned long bg;
extern int 	file_not_loaded;

extern void get_neighbours(PATHPTR *list, TREEPTR root, char c[]);
extern void initialize_label(char c[], int start);
extern void write_label(char *c);
extern void copy_label (char *dest, char *source);
extern void draw_region_boundaries (Region reg);


/*Internal variables*/
static TREEPTR	 root=NULL;
static MERGEPTR merged_region_list=NULL;
short 	 yes=1, no=0,
		 visited=-1;
static char	 n0[10],n1[10],n2[10],n3[10];
static unsigned long num_reg,
		     num_reg_after,
		     num_split;


/* Variables for setting resources */
static Arg	args[MAXARGS];
static Cardinal argcount;
static Widget split_dialog = (Widget) NULL;
static Widget merge_dialog = (Widget) NULL;


/* Function prototypes */
void create_split_dialog(Widget parent);
void activate_split_dialog(Widget w, XtPointer client_data,
			    XmAnyCallbackStruct *call_data);
void start_split(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data);
TREEPTR split(short deviation, short x, short y, short level);

static void create_merge_dialog(Widget parent);
static void activate_merge_dialog(Widget w, XtPointer client_data,
			    XmAnyCallbackStruct *call_data);
static void start_merge(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data);
static MERGEPTR merge(TREEPTR root, short condition);
static void analize_tree(TREEPTR node, short condition, short reg, int level);

static void create_merged_region_list(MERGEPTR *list);
static void add_new_merged_region(MERGEPTR *list);
static void insert_leaf_list(MERGEPTR *list, TREEPTR leaf);
static void merge_lists(MERGEPTR *list1,MERGEPTR *list2);

static void display_contour(MERGEPTR merged_region_list);
static void display_tree(TREEPTR node);

TREEPTR get_leaf(char path[]);
static Region get_merged_region(LEAFPTR actual_region);

static void free_tree(TREEPTR node);
static void free_merge(MERGEPTR *merged_region_list);

static int similar(char p[], char q[], short condition);

void insert_path(PATHPTR *first, PATHPTR *end, char n[]);
static void delete_first(PATHPTR *list, char value[]);

void view_path_list(PATHPTR *path);

static float get_deviation(float mean, float sqr_mean);



/* Function definition */

/*----------------------------------------------------------------------*/
/*									*/
/*  c r e a t e _ s p l i t _ d i a l o g				*/
/*									*/
/*----------------------------------------------------------------------*/

void create_split_dialog(Widget parent)
{
    XmString message;
    Widget temp_widget = parent;

    /* Ensure the parent of the dialog is a shell widget */
    while ( !XtIsShell(temp_widget) ) {
	temp_widget = XtParent(temp_widget);
    }
    message = XmStringLtoRCreate(SPLIT_MESSAGE, XmSTRING_DEFAULT_CHARSET);
    argcount = 0;
    XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
    split_dialog = XmCreatePromptDialog(temp_widget, "split dialog",
				args, argcount);
    /* Remove the help button from the dialog */
    temp_widget = XmSelectionBoxGetChild(split_dialog, XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(temp_widget);
    /* Add the actions to the buttons */
    XtAddCallback(split_dialog, XmNokCallback,
		(XtCallbackProc) start_split, (XtPointer) NULL);
    XtAddCallback(split_dialog, XmNokCallback,
		(XtCallbackProc) activate_merge_dialog, (XtPointer) NULL);
    XmStringFree(message);
    create_merge_dialog(main_window);
}



/*----------------------------------------------------------------------*/
/*  c r e a t e _ m e r g e _ d i a l o g				*/
/*									*/
/*  Define what the dialog looks like					*/
/*----------------------------------------------------------------------*/

void create_merge_dialog(Widget parent)
{
    XmString message;
    Widget temp_widget = parent;

    /* Ensure the parent of the dialog is a shell widget */
    while ( !XtIsShell(temp_widget) ) {
	temp_widget = XtParent(temp_widget);
    }
    message = XmStringLtoRCreate(MERGE_MESSAGE, XmSTRING_DEFAULT_CHARSET);
    argcount = 0;
    XtSetArg(args[argcount], XmNselectionLabelString, message); argcount++;
    merge_dialog = XmCreatePromptDialog(temp_widget, "merge dialog",
				args, argcount);
    /* Remove the help button from the dialog */
    temp_widget = XmSelectionBoxGetChild(merge_dialog, XmDIALOG_HELP_BUTTON);
    XtUnmanageChild(temp_widget);
    /* Add the actions to the buttons */
    XtAddCallback(merge_dialog, XmNokCallback,
		(XtCallbackProc) start_merge, (XtPointer) NULL);
    XtAddCallback(merge_dialog, XmNokCallback,
		(XtCallbackProc) activate_merge_dialog, (XtPointer) NULL);
    XmStringFree(message);
}



/*----------------------------------------------------------------------*/
/*  a c t i v a t e _ s p l i t _ d i a l o g				*/
/*									*/
/*  Show the split dialog in the screen					*/
/*----------------------------------------------------------------------*/

void activate_split_dialog(Widget w, XtPointer client_data,
			    XmAnyCallbackStruct *call_data)
{
    if (file_not_loaded) return;
    selection = SPLIT_MERGE;
    action = CLICK;
    refresh_action();
    XtManageChild(split_dialog);
}


/*----------------------------------------------------------------------*/
/*  a c t i v a t e _ m e r g e _ d i a l o g				*/
/*									*/
/*  Show the merge dialog in the screen					*/
/*----------------------------------------------------------------------*/

void activate_merge_dialog(Widget w, XtPointer client_data,
			    XmAnyCallbackStruct *call_data)
{
    selection = SPLIT_MERGE;
    action = CLICK;
    refresh_action();
    XtManageChild(merge_dialog);
}



/*----------------------------------------------------------------------*/
/*  s t a r t  _  s p l i t                                             */
/*                                                                      */
/*  Event handler to start_split procedure                              */
/*----------------------------------------------------------------------*/

void start_split(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data)
{
    short i,j,condition;
    char *cond;

    /* Get threshold value from user's selection */
    XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &cond);

    condition = atoi(cond);
    if (!(condition>=0 && condition<=20512) ) {
	XBell(XtDisplay(w),100);
	return;
    }

    /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,bg);

    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

    /* Associate the watch cursor with the main window */
    XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

    /* Flush the request buffer and wait for all events */
    /* and errors to be processed by the server.        */
    XSync(XtDisplay(draw_1), False);

    /* Free the memory used for giving room to the structure */
    /* that represents the merged regions if any */
    if (merged_region_list!=NULL) free_merge(&merged_region_list);

    /* Free the memory used for giving room to the tree structure if any */
    if (root!=NULL) free_tree(root); root=NULL;

    num_reg=0;
    num_split=0;

    /* Generate the quadtree structure */
    root = split(condition,0,0,0);

    /* Show usefull information on the screen */
    fprintf (stderr,"\n\n\n************************ SPLIT ***********************\n");
    fprintf (stderr,"    Standard deviation:            %i\n", condition);
    fprintf (stderr,"    Number of split operations:    %i\n", num_split);
    fprintf (stderr,"    Number of regions after split: %i\n", num_reg);
    display_tree(root);

    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_1),0,0,0,0,True);

    /* Disassociate the watch cursor from the main window */
    XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

    /* Copy image into pixmap */
    XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2, 
		0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);
}



/*----------------------------------------------------------------------*/
/*  s t a r t  _  m e r g e						*/
/*                                                                      */
/*  Event handler to start_split procedure                              */
/*----------------------------------------------------------------------*/

void start_merge(Widget w, XtPointer client_data,
			XmSelectionBoxCallbackStruct *call_data)
{
    short i,j,condition;
    char *cond;

    /* Get threshold value from user's selection */
    XmStringGetLtoR(call_data->value, XmSTRING_DEFAULT_CHARSET, &cond);

    condition = atoi(cond);
    if (!(condition>=0 && condition<=512) ) {
	XBell(XtDisplay(w),100);
	return;
    }

    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);

    /* Associate the watch cursor with the main window */
    XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor);

    /* Flush the request buffer and wait for all events */
    /* and errors to be processed by the server.        */
    XSync(XtDisplay(draw_1), False);

    /* Free the memory used for giving room to the structure */
    /* that represents the merged regions if any */
    if (merged_region_list!=NULL) free_merge(&merged_region_list);

    num_reg_after=0;

    /* Compute merge procedure */
    merged_region_list = merge(root,condition);

    fprintf (stderr,"\n    ------------- MERGE ------------\n");
    fprintf (stderr,"        Standard deviation  %i\n", condition);
    fprintf (stderr,"        Number of regions   %i\n", num_reg_after);

    /* Fill in the image with background color */
    for(i=0; i<IMAGE_WIDTH; i++)
	for(j=0; j<IMAGE_HEIGHT; j++)
	    XPutPixel(theXImage_2,i,j,XGetPixel(theXImage_1,i,j));

    display_contour(merged_region_list);

    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_1),0,0,0,0,True);

    /* Disassociate the watch cursor from the main window */
    XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window));

    /* Copy image into pixmap */
    XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2, 
		0, 0, 0, 0, theXImage_2->width, theXImage_2->height);
    /* Clear the drawing window so the image is displayed again */
    XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True);
}



/*----------------------------------------------------------------------*/
/*  s p l i t								*/
/*									*/
/*  Split the picture into homogeneous regions generating the		*/
/*  quadtree structure							*/
/*----------------------------------------------------------------------*/

TREEPTR split(short deviation, short x, short y, short level)
{
    short  i, j, height, width, intensity;
    long   size;
    double mean, sqr_mean;
    float  dev, dev1, variance;

    XPoint region_points[4];
    TREEPTR new_node;

    /* Allocate memory for the new defined region */
    new_node = (TREEPTR)malloc(sizeof(TREE));
    new_node->region  = NULL;
    new_node->subreg0 = NULL;
    new_node->subreg1 = NULL;
    new_node->subreg2 = NULL;
    new_node->subreg3 = NULL;
    new_node->pmerge  = NULL;
    new_node->included = no;
    new_node->mean=0;
    new_node->sqr_mean=0;
    new_node->level=level;

    /* Get the size of the region according to the level */
    height = IMAGE_HEIGHT/pow(2,level);
    width  = IMAGE_WIDTH /pow(2,level);
    size = height*width;

    /* Initialize some variables */
    mean=0; sqr_mean=0;

    /*Compute the mean and the mean of the squared pixels intensity*/
    for (i=x; i<x+width; i++)
	for (j=y; j<y+height; j++) {
	    intensity=XGetPixel(theXImage_1,i,j);
	    XPutPixel(theXImage_2,i,j,intensity);
	    mean=mean+intensity;
	    sqr_mean=sqr_mean+pow(intensity,2);
	};
    mean = mean/size;
    sqr_mean = sqr_mean/size;

    /*Store the mean and sqr_mean values*/
    new_node->mean = mean;
    new_node->sqr_mean = sqr_mean;

    /*Compute standard deviation of the region*/
    dev = get_deviation(mean,sqr_mean);

    /*Recursive splitting process*/
    if (dev>deviation) {
	/*The condition is not satisfied so split region*/
        num_split++;
	new_node->subreg0 = split(deviation,x,y,level+1);
	new_node->subreg1 = split(deviation,x+(width/2),y,level+1);
	new_node->subreg2 = split(deviation,x,y+(height/2),level+1);
	new_node->subreg3 = split(deviation,x+(width/2),y+(height/2),level+1);
    } else {
	/*Condition satisfied. Insert region in the quadtree structure*/
	num_reg++;
	region_points[0].x=x; region_points[0].y=y;
	region_points[1].x=x; region_points[1].y=y+height;
	region_points[2].x=x+width; region_points[2].y=y+height;
	region_points[3].x=x+width; region_points[3].y=y;
	new_node->region = XPolygonRegion (region_points, 4, WindingRule);
    }
    return (new_node);
}



/*----------------------------------------------------------------------*/
/*  m e r g e								*/
/*									*/
/*  Analyse the quadtree structure in order to merge the adjacent	*/
/*  homogeneous regions							*/
/*----------------------------------------------------------------------*/

MERGEPTR merge(TREEPTR root, short condition)
{
    char path[10];

    initialize_label(path,0);
    create_merged_region_list(&merged_region_list);
    analize_tree(root,condition,-1,0);
    yes++;
    no++;
    return(merged_region_list);
}



/*----------------------------------------------------------------------*/
/*  g e t _ l o c a t i o n						*/
/*									*/
/*  Obtain a pointer to a node in the tree knowing its label		*/
/*----------------------------------------------------------------------*/

MERGEPTR* get_location(TREEPTR node)
{
    MERGEPTR p;

    p=node->pmerge;
    if (p!=NULL) while (p->moved!=NULL) p=p->moved;
    return (&p);
}



/*----------------------------------------------------------------------*/
/*  a n a l i z e _ t r e e						*/
/*									*/
/*  This is the mean function to merge homogeneous functions		*/
/*----------------------------------------------------------------------*/

void analize_tree(TREEPTR root, short condition, short reg, int level)
{
    char    neigh[10],path[10];
    PATHPTR path_list=NULL,
	    neigh_list=NULL,
	    breath_h=NULL,
	    breath_q=NULL;
    short   length, merged, end;
    TREEPTR node,neigh_node;
    MERGEPTR node1, node2;


    initialize_label(path,0);
    node=root;

    end=False;
    while (!end) {
	node=get_leaf(path);

	/*Compute merge operations*/
	if (node->region!=NULL) {    /*A leaf has been found*/
	    /*Merge its neighbours if condition is satisfied*/
	    if (node->included!=yes) {
		/*Add a new element to the list of merged regions*/
		add_new_merged_region(&merged_region_list);
		/*Insert the region in the list of merged regions*/
		insert_leaf_list(&merged_region_list, node);
		node->included=yes;
		/*Actualize the pointer to the merged region which includes it*/
		node->pmerge = merged_region_list;
	    }
	    /* Obtain the list of neighbours of the node referenced by path */
	    get_neighbours(&neigh_list,root,path);
	    while (neigh_list!=NULL) {
		initialize_label(neigh,0);
		/* Get and delete the first element in the list of neighbours */
		delete_first(&neigh_list,neigh);
		neigh_node=get_leaf(neigh);
		/* Get the pointers to the candidate regions */
		node1=*get_location(node);
		node2=*get_location(neigh_node);
		if ( node1!=node2 ) {
		    /* The neighbour region doesn't belong to a merged region yet*/
		    if (neigh_node->included!=yes) {
			/* Check whether both regions are homogeneous or not */ 
			if (similar(path,neigh,condition)) {
			    /* Insert the neighbour in the same list than path */
			    /* All the regions that are homogeneous belong to */
			    /* the same list */ 
			    insert_leaf_list(get_location(node),neigh_node);
			    neigh_node->included=yes;
			    /* Set pmerge field to point to the list */
			    /* where it has been inserted */
			    neigh_node->pmerge=*get_location(node);
			}
		    } else {  /* Each region belongs to a different merged region */
			/* Check whether both regions are homogeneous or not */ 
			if (similar(path,neigh,condition)) {
			    /* Check whether both merged regions are homogeneous */
			    if (abs( node1->mean - node2->mean ) < condition)
				/* Merge both lists of merged regions */
				merge_lists(&node1,&node2);
			}
		    }
		}
	    }
	} else {
	    length = strlen(path);
	    /* Insert new nodes to be analised using the breath first method */
	    path[length]='0'; insert_path(&breath_h,&breath_q,path);
	    path[length]='1'; insert_path(&breath_h,&breath_q,path);
	    path[length]='2'; insert_path(&breath_h,&breath_q,path);
	    path[length]='3'; insert_path(&breath_h,&breath_q,path);
	}
	/* Take a node from the list to analize it if there are nodes left */
	if (breath_h==NULL) end=True;
	    else delete_first(&breath_h,path);
    }
}



/*----------------------------------------------------------------------*/
/*  c r e a t e _ m  e r g e d _ r e g i o n _ l i s t			*/
/*									*/
/*  Create an empty structure to lodge the result of the merge process	*/
/*----------------------------------------------------------------------*/

void create_merged_region_list(MERGEPTR *list)
{
    *list=NULL;
}



/*-----------------------------------------------------------------------*/
/*  a d d _ n e w _ m e r g e d _ r e g i o n				 */
/*									 */
/*  Add a new node to point to a list of regions that are merged together*/
/*-----------------------------------------------------------------------*/

void add_new_merged_region(MERGEPTR *list)
{
    MERGEPTR newone;

    /* Create a new node to point to a list of merged regions */
    newone = (MERGEPTR)malloc(sizeof(MERGE));
    newone->leaf_list = NULL;
    newone->last_node = NULL;
    newone->mean = 0;
    newone->sqr_mean = 0;
    newone->num_points = 0;
    newone->next  = NULL;
    newone->moved = NULL;

    /* Insert that node in the head */
    if (*list==NULL) *list=newone;
    else {
	newone->next=*list;
	*list=newone;
    }
    num_reg_after++;
}



/*----------------------------------------------------------------------*/
/*  i n s e r t _ l e a f _ l i s t					*/
/*									*/
/*  Insert a node in the list of homogeneous regions			*/
/*----------------------------------------------------------------------*/

void insert_leaf_list(MERGEPTR *list, TREEPTR leaf)
{
    short mean;
    unsigned long num_points, sum;    
    LEAFPTR newone;

    /* Create a new node in the list of merged regions */
    newone = (LEAFPTR)malloc(sizeof(LEAF));
    newone->leaf_ptr = leaf;
    newone->next = NULL;

    num_points = pow(2,(9-leaf->level))*pow(2,(9-leaf->level));
    sum = (*list)->num_points+num_points;

    /*Update the mean value with the information from the new added region*/
    (*list)->mean = ( ((*list)->num_points*(*list)->mean)
			+(num_points*(leaf->mean)) ) / sum;

    /*Update the squares mean value with the information from the new added region*/
    (*list)->sqr_mean = ( ((*list)->num_points*(*list)->sqr_mean)
			+(num_points*(leaf->sqr_mean)) ) / sum;

    /*Update the number of points included in all the regions of the list*/
    (*list)->num_points = sum;

    /*Insert the new region in the list*/
    if ((*list)->leaf_list==NULL) 
	(*list)->leaf_list=(*list)->last_node=newone;
    else {
	newone->next=(*list)->leaf_list;
	(*list)->leaf_list=newone;
    }
}



/*----------------------------------------------------------------------*/
/*  m e r g e _ l i s t s						*/
/*									*/
/*  Merge two different list of homogeneous regions because they are	*/
/*  also adjacent and homogeneous					*/
/*----------------------------------------------------------------------*/

void merge_lists(MERGEPTR *list1,MERGEPTR *list2)
{
    MERGEPTR p,q;
    unsigned long num_points;

    p=*list1;
    q=*list2;

    num_points = p->num_points+q->num_points;

    if (p!=q) {
	while (p->moved!=NULL) p=p->moved;
	while (q->moved!=NULL) q=q->moved;
	if (p!=q) {
	    (p->last_node)->next = q->leaf_list;
	    p->last_node = q->last_node;
	    p->mean = (p->mean*p->num_points + q->mean*q->num_points)
			 / num_points;
	    p->sqr_mean = (p->sqr_mean*p->num_points + q->sqr_mean*q->num_points)
			 / num_points;
	    p->num_points = num_points;

	    q->leaf_list = NULL;
	    q->last_node = NULL;
	    q->mean = 0;
	    q->sqr_mean = 0;
	    q->num_points = 0;
	    q->moved = p;

	    num_reg_after--;
	}
    }
}



/*----------------------------------------------------------------------*/
/*  g e t _ m e r g e d _ r e g i o n					*/
/*									*/
/*  Compute the union of the homogeneous regions inserted in a list	*/
/*----------------------------------------------------------------------*/

Region get_merged_region(LEAFPTR actual_region)
{
    Region merged;

    merged = XCreateRegion();
    /*Compute the union of all the regions that has been merged*/
    while (actual_region!=NULL) {
	XUnionRegion (merged,(actual_region->leaf_ptr)->region,merged);
        actual_region=actual_region->next;
    }
    return(merged);
}



/*----------------------------------------------------------------------*/
/*  d i s p l a y _ c o n t o u r					*/
/*									*/
/*  Display the boundaries of all the merged regions			*/
/*----------------------------------------------------------------------*/

void display_contour(MERGEPTR merged_region_list)
{
    MERGEPTR actual_set;
    LEAFPTR   actual_region;
    Region merged_region;

    actual_set=merged_region_list;
    while (actual_set!=NULL) {
	actual_region=actual_set->leaf_list;
	merged_region = get_merged_region(actual_region);
	draw_region_boundaries(merged_region);
	XDestroyRegion(merged_region);
	actual_set=actual_set->next;
    }
}



/*----------------------------------------------------------------------*/
/*  f r e e _ t r e e							*/
/*									*/
/*  Free the memory allocated to lodge the quadtree structure		*/
/*----------------------------------------------------------------------*/

void free_tree(TREEPTR node)
{
    TREEPTR n1, n2, n3, n4;

    if (node->region==NULL) {
	free_tree(node->subreg0);
	free_tree(node->subreg1);
	free_tree(node->subreg2);
	free_tree(node->subreg3);
	free(node);    
    } else {
	XDestroyRegion(node->region);
	free(node);
    };
}



/*----------------------------------------------------------------------*/
/*  d i s p l a y _ t r e e						*/
/*									*/
/*  Display the leaves of the quadtree structure			*/
/*----------------------------------------------------------------------*/

void display_tree(TREEPTR node)
{
    if (node->region!=NULL) draw_region_boundaries(node->region);
    else {
	display_tree(node->subreg0);
	display_tree(node->subreg1);
	display_tree(node->subreg2);
	display_tree(node->subreg3);
    }
}



/*----------------------------------------------------------------------*/
/*  f r e e _ m e r g e							*/
/*									*/
/*  Free the allocated memory that lodges the merged regions		*/
/*----------------------------------------------------------------------*/

void free_merge(MERGEPTR *merged_region_list)
{
    MERGEPTR  actual_set, aux_set;
    LEAFPTR   actual_region, aux_reg;
    Region    merged_region;

    actual_set=*merged_region_list;
    while (actual_set!=NULL) {
	actual_region=actual_set->leaf_list;
	while (actual_region!=NULL) {
	    aux_reg=actual_region;
	    /* The location pointed by pmerge is */
	    /* deleted in the instruction free(aux_set) */
	    actual_region->leaf_ptr->pmerge=NULL;
	    actual_region=actual_region->next;
	    free(aux_reg);
	}
	aux_set=actual_set;
	actual_set=actual_set->next;
	free(aux_set);
    }
    *merged_region_list=actual_set; /*actual_set==NULL*/
}



/*----------------------------------------------------------------------*/
/*  similar								*/
/*									*/
/*  Check whether two regions are homogeneous or not			*/
/*----------------------------------------------------------------------*/

int similar(char p[], char q[], short condition)
{
    TREEPTR node1, node2;
    double p_mean, q_mean,
	   p_deviation, q_deviation,
	   difference;

    node1=get_leaf(p);
    node2=get_leaf(q);
    p_mean = node1->mean;
    q_mean = node2->mean;

    p_deviation = get_deviation(node1->mean,node1->sqr_mean);
    q_deviation = get_deviation(node2->mean,node2->sqr_mean);

    /* This is to avoid very little regions not to get merged because */
    /* a null standar daviation */
    if (p_deviation==0) p_deviation=0.5;
    if (q_deviation==0) q_deviation=0.5;

    difference = p_mean-q_mean;

    return ( (abs(difference)<=condition*p_deviation) ||
	     (abs(difference)<=condition*q_deviation) );
}



/*----------------------------------------------------------------------*/
/*  d e l e t e _ f i r s t						*/
/*									*/
/*  Delete and return the first element of a list of nodes		*/
/*----------------------------------------------------------------------*/

void delete_first(PATHPTR *list, char value[])
{
    PATHPTR top;

    if (*list != NULL) {
    	top = *list;
    	*list = (*list)->next;
	copy_label(value,top->path);

	free(top);
    }
}



/*----------------------------------------------------------------------*/
/*  i n s e r t _ p a t h						*/
/*									*/
/*  Insert a node in a list of nodes					*/
/*----------------------------------------------------------------------*/

void insert_path(PATHPTR *first, PATHPTR *end, char n[])
{
    PATHPTR newone, last;

    if (strlen(n)>0) {
	newone = (PATHPTR)malloc(sizeof(PATH));
	initialize_label(newone->path,0);
	copy_label(newone->path,n);
	newone->next = NULL;
    
	if (*first==NULL) {
	    *first=*end=newone;
	} else {
	    (*end)->next=newone;
	    *end=newone;
	}
    }
}



/*----------------------------------------------------------------------*/
/*  g e t _ l e a f							*/
/*									*/
/*  Get the pointer of the node specified by it label			*/
/*----------------------------------------------------------------------*/

TREEPTR get_leaf(char path[])
{
    TREEPTR actual;
    short i, length;

    length = strlen(path);
    actual = root;

    for (i=0; i<length; i++) {
	switch (path[i]) {
	    case '0': actual=actual->subreg0; break;
	    case '1': actual=actual->subreg1; break;
	    case '2': actual=actual->subreg2; break;
	    case '3': actual=actual->subreg3; break;
	}
    }
    return(actual);
}



/*----------------------------------------------------------------------*/
/*  v i e w _ p a t h _ l i s t						*/
/*									*/
/*  Prints the labels of the nodes inserted in a list of nodes		*/
/*----------------------------------------------------------------------*/

void view_path_list(PATHPTR *path)
{
    PATHPTR p;

    p=*path;
    while (p!=NULL) {
	write_label(p->path);
	p=p->next;
    }
    write_label("\n");
}



/*----------------------------------------------------------------------*/
/*  g e t _ d e v i a t i o n						*/
/*									*/
/*  Compute and return the standard deviation of a node			*/
/*----------------------------------------------------------------------*/

float get_deviation(float mean, float sqr_mean)
{
    /* Compute and return the standard deviation */
    return( powf(sqr_mean-pow(mean,2),0.5) );
}
