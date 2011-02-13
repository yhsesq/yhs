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


/*--------------------------------------------------------------*/
/*								*/
/*     File: region_ops.c					*/
/*								*/
/*     CT Image Application Program				*/
/*								*/
/*     OSF/Motif version.					*/
/*								*/
/*     V1.0 November 1996					*/
/*								*/
/*--------------------------------------------------------------*/

#include "main.h"
#include <X11/Xlib.h>

#define UP    0
#define DOWN  1
#define LEFT  2
#define RIGHT 3


/* Function protopytes */
void create_region_list(REGIONPTR *list);
void free_region_list(REGIONPTR *list);
NODEPTR insert_region (REGIONPTR *region_list, 
			XPoint *points, int num_points, int label);
Region get_region (REGIONPTR *region_list, int x, int y);
Region get_first_region (REGIONPTR *region_list);
void delete_region (REGIONPTR *region_list, int type);
NODEPTR make_list_point(XPoint *points, int num_points);
void display_regions(REGIONPTR region_list);
extern void show_region(NODEPTR point_list);
void draw_region_boundaries (Region reg);

extern XImage *theXImage_2;
extern GC theGC;
extern Widget draw_2;
extern int array1[512][512];
extern int array2[512][512];


/* Function definitions */

/*----------------------------------------------------------------------*/
/*  c r e a t e _ r e g i o n _ l i s t					*/
/*									*/
/*  Initialise the list of regions					*/
/*----------------------------------------------------------------------*/

void create_region_list(REGIONPTR *list)
{
    *list = NULL;
}



/*----------------------------------------------------------------------*/
/*  f r e e _ r e g i o n _ l i s t					*/
/*									*/
/*  Delete the list of regions						*/
/*----------------------------------------------------------------------*/

void free_region_list(REGIONPTR *list)
{
    while (*list!=NULL)
	delete_region (list,0);
}



/*----------------------------------------------------------------------*/
/*  i n s e r t _ r e g i o n						*/
/*									*/
/*  Insert a new region in the list or regions				*/
/*----------------------------------------------------------------------*/

NODEPTR insert_region (REGIONPTR *region_list,
			 XPoint *points, int num_points, int label)
{ 
    Region region;
    REGIONPTR newr;

    /* Generate a region from the array of points */
    region = XPolygonRegion (points, num_points, WindingRule);

    /* Insert the new defined region in the list */
    newr = (REGIONPTR) malloc(sizeof(REGION));
    newr->region.ptr        = region;
    newr->region.points     = (NODEPTR)make_list_point(points, num_points);
    newr->region.num_points = num_points;
    newr->region.label      = label;
    newr->next              = NULL;
    newr->previous          = NULL;

    if (*region_list == NULL)
	{*region_list = newr;}
    else
	{newr->next = *region_list;
	(*region_list)->previous = newr;
	 *region_list = newr;};

    return (newr->region.points);
}



/*----------------------------------------------------------------------*/
/*  g e t _ r e g i o n							*/
/*									*/
/*  Get the union of the regions which includes a given point		*/
/*----------------------------------------------------------------------*/

Region get_region (REGIONPTR *region_list, int x, int y)
{
    REGIONPTR	top;
    TPREGION	first;
    Region	aux, reg;

    top = *region_list;
    reg = XCreateRegion();

    /* Generate the union of regions to grow */
    while (top != NULL) {
	aux = top->region.ptr;
	if ( XPointInRegion(aux, x, y) ) {
	    XUnionRegion(reg, aux, reg);
	};
	top = top->next;
    };
    return(reg);
}



/*----------------------------------------------------------------------*/
/*  g e t _ f i r s t _ r e g i o n					*/
/*									*/
/*  Get the first region of list_region					*/
/*----------------------------------------------------------------------*/

Region get_first_region (REGIONPTR *region_list)
{
    REGIONPTR	top;
    TPREGION	first;
    Region	aux, reg;

    top = *region_list;
    reg = XCreateRegion();
    if (top != NULL) { reg = top->region.ptr; }
    return(reg);
}



/*----------------------------------------------------------------------*/
/*  d e l e t e _ r e g i o n						*/
/*									*/
/*  Delete the first region of region_list				*/
/*----------------------------------------------------------------------*/

void delete_region (REGIONPTR *region_list, int type)
{ 
    REGIONPTR top;
    NODEPTR aux, aux1;

    if (*region_list != NULL) {
    	top = *region_list;
    	*region_list = (*region_list)->next;
	XDestroyRegion (top->region.ptr);
	aux = top->region.points;

	/* Remove the point list which define the region */
	while (aux != NULL ) {
	    aux1 = aux->next;
	    free(aux);
	    aux = aux1;
	}
	free(top);
    }
}



/*----------------------------------------------------------------------*/
/*  m e r g e _ r e g i o n s						*/
/*									*/
/*  Compute the union of two regions					*/
/*----------------------------------------------------------------------*/

void merge_regions (REGIONPTR *region_list, REGIONPTR p, REGIONPTR q)
{ 
    NODEPTR aux, aux1;

    if (q->previous!=NULL) (q->previous)->next = q->next;
    if (q==*region_list) *region_list = (*region_list)->next;
    if (q->next!=NULL)     (q->next)->previous = q->previous;

    XUnionRegion(p->region.ptr, q->region.ptr, p->region.ptr);
    XDestroyRegion(q->region.ptr);

    aux = q->region.points;
    while (aux != NULL ) {
	aux1 = aux->next;
	free(aux);
	aux = aux1;
    }
    free (q);

}



/*----------------------------------------------------------------------*/
/*  m a k e _ l i s t _ p o i n t					*/
/*									*/
/*  Transforn an array of points into a list of points			*/
/*----------------------------------------------------------------------*/

NODEPTR make_list_point(XPoint *points, int num_points)
{
    NODEPTR newone, list;
    int i;

    list = NULL;
    for (i=num_points-1; i>=0; i--) {
	newone = (NODEPTR) malloc(sizeof(NODE)); 
	newone->infolist.coord_x = points[i].x;
	newone->infolist.coord_y = points[i].y;
	newone->infolist.grey_level = 0; /* this value is not important */
	newone->next = list;
	list = newone;
    };
    return (list);
}



/*----------------------------------------------------------------------*/
/*  d i s p l a y _ r e g i o n s					*/
/*									*/
/*  Display all the region defined in the list				*/
/*----------------------------------------------------------------------*/

void display_regions(REGIONPTR region_list)
{
    REGIONPTR aux;

    aux = region_list;
    while (aux != NULL) {
	show_region(aux->region.points);
	aux = aux->next;
    }
}



/*----------------------------------------------------------------------*/
/*  d r a w _ r e g i o n _ b o u n d a r i e s				*/
/*									*/
/*  Draw the boundaries of a given region				*/
/*----------------------------------------------------------------------*/

void draw_region_boundaries (Region reg)
{
    XRectangle rectangle;
    short x,y, off_x, off_y, i, j, current, former;
    unsigned int width, height;

	XClipBox (reg, &rectangle);
	off_x=rectangle.x;
	off_y=rectangle.y;
	width=rectangle.width;
	height=rectangle.height;

	for (x=off_x; x<off_x+width; x++) {
	    current=former=0;
	    for (y=off_y; y<off_y+height; y++) {
		current = (XPointInRegion(reg,x,y)==True);
		if ( (former==False)&&(current==True) ) {
		    array2[x][y]=255;
		    /* XDrawPoint (XtDisplay(draw_2), XtWindow(draw_2), theGC, x, y); */
		}
		former=current;
	    }
	}
	for (y=off_y; y<off_y+height; y++) {
	    current=former=0;
	    for (x=off_x; x<off_x+width; x++) {
		current = (XPointInRegion(reg,x,y)==True);
		if ( (former==False)&&(current==True) ) {
		    array2[x][y]=255;
		    /* XDrawPoint (XtDisplay(draw_2), XtWindow(draw_2), theGC, x, y); */
		}
		former=current;
	    }
	}


}
