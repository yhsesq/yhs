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
/*								*/
/*     File: watershed.c					*/
/*								*/
/*     CT Image Application Program				*/
/*								*/
/*     OSF/Motif version.					*/
/*								*/
/*     V1.0 March 1996						*/
/*								*/
/*--------------------------------------------------------------*/

#include "main.h"

#include <Xm/Xm.h>
#include <Xm/SelectioB.h>

#define MAX_PIXELS (IMAGE_WIDTH*IMAGE_HEIGHT)
#define WSHED 0
#define MASK  1
#define INIT  2


typedef struct {		/* structure for FIFO stack */
          TPINFO pix_info[MAX_PIXELS];
          unsigned long head, tail;
} FIFO;


/* Extern variables */

extern char 	*action;
extern int 	selection;
extern XImage 	*theXImage_1;
extern XImage 	*theXImage_2;
extern unsigned long bg, fg;
extern Pixmap	thePixmap_1;
extern Pixmap	thePixmap_2;
extern Cursor	theCursor;
extern GC	image_gc_2, theGC;
extern Widget 	draw_1,
		draw_2,
		main_window;
extern int 	array1[512][512];
extern int 	array2[512][512];
extern int	yhs_filename[21];
extern char 	*yhs_filename1;
extern char     *tempfileold;
extern char     *tempfilenew;
extern char	*addcharac;
extern int      file_loader;
extern int      run_once;
extern char     *file_yhs;
extern int 	yhs_files_open;

/* Variables for setting resources */

static NODEPTR 	p, list_pixels[COLORMAP_SIZE];
static unsigned long intensity;


/* Function prototypes */
void watershed_callback(Widget w, XtPointer client_data,
		XmAnyCallbackStruct *call_data);
static void watershed_contour();
static void create_p_list(NODEPTR *list);
static void insert_p_list(NODEPTR *list, TPINFO *info);
static void view_p_list(NODEPTR list);
static void free_p_list(NODEPTR *list);
static void fifo_init(FIFO *pixel_q);
static void fifo_add(TPINFO p, FIFO *pixel_q);
static TPINFO fifo_first(FIFO *pixel_q);
static int fifo_empty(FIFO *pixel_q);
static int image_pixel(int x, int y);
static int labeled_neighbour(int x, int y,
		unsigned long o[IMAGE_WIDTH][IMAGE_HEIGHT]);
extern void refresh_action(void);
extern void prepare_handlers (int sel);
extern void activate_total_gradient_dialog(Widget w, XtPointer client_data,
		XmAnyCallbackStruct *call_data);



/* Function definition */

/*----------------------------------------------------------------------*/
/*  s t a r t  _  w a t e r s h e d					*/
/*									*/
/*  Event handler to start watershed procedure				*/
/*----------------------------------------------------------------------*/
void watershed_callback(Widget w, XtPointer client_data,
		XmAnyCallbackStruct *call_data)
{
   int i,j;
   FILE *p_file;
   int a,b;
if (yhs_filename[0] >= 1 && yhs_files_open > 0 && yhs_files_open < 20)
{
      /* Fill in the image with background color */
  /*    for(i=0; i<IMAGE_WIDTH; i++)
          for(j=0; j<IMAGE_HEIGHT; j++)
               XPutPixel(theXImage_2,i,j,bg); */

      /* Clear the drawing window so the image is displayed again */
/*      XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True); */

      /* Associate the watch cursor with the main window */
/*      XDefineCursor(XtDisplay(draw_1), XtWindow(main_window), theCursor); */

      /* Flush the request buffer and wait for all events */
      /* and errors to be processed by the server.        */
/*      XSync(XtDisplay(draw_1), False); */
 
 for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { array1[a][b]=array2[a][b];}}
      watershed_contour();

      /* Clear the drawing window so the image is displayed again */
/*      XClearArea(XtDisplay(draw_1),XtWindow(draw_1),0,0,0,0,True); */

      /* Disassociate the watch cursor from the main window */
/*      XUndefineCursor(XtDisplay(draw_1), XtWindow(main_window)); */
      /* Copy image into pixmap */
/*      XPutImage(XtDisplay(draw_2), thePixmap_2, image_gc_2, theXImage_2, 
                      0, 0, 0, 0, theXImage_2->width, theXImage_2->height); */
      /* Clear the drawing window so the image is displayed again */
/*      XClearArea(XtDisplay(draw_1),XtWindow(draw_2),0,0,0,0,True); */

       strcpy(tempfileold,tempfilenew); /* copy the new filename to the old file i.e. save the old file */
       strcat(tempfilenew,addcharac); /* new free temp file */
  if((p_file = fopen(tempfileold,"w")) == NULL)
    {
        fprintf(stderr, "Cannot open: %s - Temporary file for writing.\n",tempfileold);
        exit(1);
    }
    for (a=0;a<512;a++){
    for (b=0;b<512;b++)
    { putc(array2[a][b],p_file); }}
    fclose(p_file);
    run_once=0;
    file_loader=1;
    file_yhs=tempfileold;
}
}



/*-------------------------------------------------------------*/
/*  c r e a t e _ p _ l i s t                                  */
/*                                                             */
/*  Initialise the list                                        */
/*-------------------------------------------------------------*/
void create_p_list(NODEPTR *list)
{
   *list = NULL;
}


/*-------------------------------------------------------------*/
/*  i n s e r t _ p _ l i s t                                  */
/*                                                             */
/*  Insert a new node in the list                              */
/*-------------------------------------------------------------*/
void insert_p_list(NODEPTR *list, TPINFO *info)
{
    NODEPTR newone;

    newone = (NODEPTR) malloc(sizeof(NODE)); 
    newone->infolist.coord_x = info->coord_x;
    newone->infolist.coord_y = info->coord_y;
    newone->infolist.grey_level = info->grey_level;
    newone->next = NULL;

    if (*list == NULL) {
	*list = newone;
    } else {
	newone->next = *list;
	*list = newone;
    }
}



/*-------------------------------------------------------------*/
/*  v i e w _ p _ l i s t                                      */
/*                                                             */
/*  Copy the pixels  to the pixmap                             */
/*-------------------------------------------------------------*/
void view_p_list(NODEPTR list)
{
    NODEPTR p;

    p = list;
    while (p != NULL) {
	XPutPixel(theXImage_2,p->infolist.coord_x, 
		p->infolist.coord_y,p->infolist.grey_level);
	p = p->next;
    }
}



/*-------------------------------------------------------------*/
/*  f r e e _ p _ l i s t                                      */
/*                                                             */
/*  Release the memory allocated by the list                   */
/*-------------------------------------------------------------*/
void free_p_list(NODEPTR *list)
{
    NODEPTR p;

    while (*list != NULL) {
	p = *list;
	*list = (*list)->next;
	free(p);
    }
}



/*-------------------------------------------------------------*/
/*  f i f o _ i n i t                                          */
/*                                                             */
/*  writes value 0 to fifo array, head = tail = 0              */
/*-------------------------------------------------------------*/
void fifo_init(FIFO *pixel_q)
{
    unsigned long i;

    for (i=0; i<MAX_PIXELS; i++) {
	pixel_q->pix_info[i].grey_level = 0;
	pixel_q->pix_info[i].coord_x = 0;
	pixel_q->pix_info[i].coord_y =0;
    }
    pixel_q->head = 0;
    pixel_q->tail = 0;
}



/*-------------------------------------------------------------*/
/*  f i f o _ a d d                                            */
/*                                                             */
/*  adds a pixel to the fifo stack                             */
/*-------------------------------------------------------------*/
void fifo_add(TPINFO p, FIFO *pixel_q)
{
    unsigned long t;

    t = pixel_q->tail;  
    pixel_q->pix_info[t].grey_level = p.grey_level;
    pixel_q->pix_info[t].coord_x = p.coord_x;
    pixel_q->pix_info[t].coord_y = p.coord_y;
    t = t+1;
    pixel_q->tail = t % MAX_PIXELS;
}



/*-------------------------------------------------------------*/
/*  f i f o _ f i r s t                                        */
/*                                                             */
/*  removes a pixel from the fifo stack                        */
/*-------------------------------------------------------------*/
TPINFO fifo_first(FIFO *pixel_q)
{
    unsigned long h;
    TPINFO p;

    h = pixel_q->head;
    p = pixel_q->pix_info[h];
    h = h + 1;
    pixel_q->head = h % MAX_PIXELS;
    return p;
}



/*-------------------------------------------------------------*/
/*  f i f o _ e m p t y                                        */
/*                                                             */
/*  returns true if fifo is empty                              */
/*-------------------------------------------------------------*/
int fifo_empty(FIFO *pixel_q)
{
    unsigned long h,t;

    h = pixel_q->head;
    t = pixel_q->tail;
    return (h==t);
}
 


/*-------------------------------------------------------------*/
/*  i m a g e _ p i x e l                                      */
/*                                                             */
/*  returns true if pixel lies within the image window         */
/*-------------------------------------------------------------*/
int image_pixel(int x, int y)
{
    if ( x>=0 && x<IMAGE_WIDTH && y>=0 && y<IMAGE_HEIGHT )
	return(TRUE);
    else 
	return(FALSE);
}  



/*-------------------------------------------------------------*/
/*  l a b e l e d _ n e i g h b o u r                          */
/*                                                             */
/*  returns true if pixel has 8-neighbour which is part of     */
/*  labeled region or watershed                                */
/*-------------------------------------------------------------*/
int labeled_neighbour(int x, int y,
		unsigned long o[IMAGE_WIDTH][IMAGE_HEIGHT])
{
    int i,j,r;

    r = FALSE;
    for (i=-1; i<=1; i++)
	for (j=-1; j<=1; j++) {
	    if ((image_pixel(x+i,y+j)==TRUE) && ((i==0&&j==0)==FALSE)) {
		if ((o[x+i][y+j]>2) || (o[x+i][y+j]==WSHED)) r=TRUE;
	}
    }
    return(r);
}



/*-------------------------------------------------------------*/
/*  s n a k e _ c o n t o u r                                  */
/*                                                             */
/*  Throws a closed contour around a pixel                     */
/*-------------------------------------------------------------*/
void watershed_contour()
{
    int s,t,i,j,n,x,y;
    TPINFO p_info, p_dash_info, p_dash_dash_info;
    TPINFO m_info;			/* fictitious marker pixel */
    TPINFO t_info; 
    FIFO pixel_q;
    unsigned long v, output_pixel, current_label = 2; /* values 0,1,2 reserved */
    int current_distance;
    unsigned long d[IMAGE_WIDTH][IMAGE_HEIGHT], o[IMAGE_WIDTH][IMAGE_HEIGHT];
    int forever;
   
    /* initialise fictitious marker with unique values */
    m_info.grey_level = 328;                     /* non existant pixel value */
    m_info.coord_x = IMAGE_WIDTH+10;             /* non existant x value */
    m_info.coord_y = IMAGE_HEIGHT+10;            /* non existant y value */

    /* initialise fifo  */
    fifo_init(&pixel_q);

    /* initialise the array of pointers */
    for (n=0; n<COLORMAP_SIZE; n++)
	create_p_list(&list_pixels[n]);

    /* initialise the output and work images and make the histogram */ 
    for (i=0; i<IMAGE_WIDTH; i++)
	for (j=0; j<IMAGE_HEIGHT; j++) {
	    array2[i][j]=fg; 
	    o[i][j] = INIT;                      /* output image */
	    d[i][j] = 0;                         /* work image */
	    p_info.grey_level = array1[i][j];
	    p_info.coord_x = i;
	    p_info.coord_y = j;
	    insert_p_list(&list_pixels[p_info.grey_level], &p_info);
	}


    /* compute the watershed by incremental thresholding */

    for (n=0; n<COLORMAP_SIZE; n++) {
	p = list_pixels[n];
	while (p != NULL) {
	    x = p->infolist.coord_x;
	    y = p->infolist.coord_y;
	    v = p->infolist.grey_level;
	    o[x][y] = MASK; 
	    /* is p adjacent to labeled region or watershed? */
	    if (labeled_neighbour(x,y,o)) {
	    d[x][y] = 1;
	    fifo_add(p->infolist, &pixel_q);
	}  
	p = p ->next;
    }


    /*  propagate pixel distances  */
  
    current_distance = 1;
    fifo_add(m_info, &pixel_q);
    forever = TRUE;
    while (forever == TRUE) {
	p_info = fifo_first(&pixel_q);
	if (p_info.grey_level == m_info.grey_level) {
	    if (fifo_empty(&pixel_q)) break;
	    else {
		fifo_add(m_info, &pixel_q);
		current_distance++;
		p_info = fifo_first(&pixel_q);
	    }
	}
	x = p_info.coord_x;
	y = p_info.coord_y;
	for (i= -1; i<=1; i++)
	    for (j= -1; j<=1; j++) {
		if ((image_pixel(x+i,y+j)==TRUE)&&((i==0&&j==0)==FALSE)) {
		    output_pixel = o[x+i][y+j];
		    if ((d[x+i][y+j]<current_distance) && 
			(output_pixel>2 || output_pixel==WSHED)) {
			/* neighbour belongs to already labeled */
			/* region or to the watersheds */
			if (output_pixel > 2) {
			    /* neighbour belongs to already labeled region */
			    if (o[x][y]==MASK || o[x][y]==WSHED) {
				o[x][y] = output_pixel;
				/* recheck neighbours to locate possible watershed */
				for (s= -1; s<=1; s++)
				  for (t= -1; t<=1; t++) 
				    if ((image_pixel(x+s,y+t)==TRUE) && 
					((s==0&&t==0)==FALSE) ) 
					if (o[x+s][y+t]>2)
					  if (o[x][y]!=o[x+s][y+t])
					    o[x][y] = WSHED;
			    } 
			    else
			    if (o[x][y] != output_pixel) {
				/* two adjacent pixels belong to different regions */
				o[x][y] = WSHED;
			    }
			}
			else
			/* neighbour belongs to the watersheds */
			if (o[x][y] == MASK) o[x][y] = WSHED;
		    }
		    else
		    if (output_pixel == MASK && d[x+i][y+j] == 0) {
			d[x+i][y+j] = current_distance + 1;
			p_dash_info.grey_level = array1[x+i][y+j];
			p_dash_info.coord_x = x+i;
			p_dash_info.coord_y = y+j;
			fifo_add(p_dash_info, &pixel_q);
		    }
		}    /* if ((image_pixel(x+i,y+j)==TRUE ...*/
	    }    /* for (j=-1; j<-1 ... */
    }    /* while */                
               
    /*  checks if new minima have been discovered */
    p = list_pixels[n];
    while (p != NULL) {
      x = p->infolist.coord_x;
      y = p->infolist.coord_y;
      v = p->infolist.grey_level;
      d[x][y] = 0;
      output_pixel = o[x][y];
      if (output_pixel == MASK) {
        current_label = current_label+1;
        fifo_add(p->infolist, &pixel_q);
        o[x][y] = current_label;
        t_info.coord_x = x;
        t_info.coord_y = y;
        t_info.grey_level = current_label;
        while (fifo_empty(&pixel_q) == FALSE) {
          p_dash_info = fifo_first(&pixel_q);
          x = p_dash_info.coord_x;
          y = p_dash_info.coord_y;
          v = p_dash_info.grey_level;
          for (i= -1; i<=1; i++)
	    for (j= -1; j<=1; j++) {
              if ((image_pixel(x+i,y+j) == TRUE) && 
                  ((i==0 && j==0) == FALSE)) {
                output_pixel = o[x+i][y+j];
                if (output_pixel == MASK) {
                  p_dash_dash_info.grey_level = array1[x+i][y+j];
                  p_dash_dash_info.coord_x = x+i;
                  p_dash_dash_info.coord_y = y+j;
                  fifo_add(p_dash_dash_info, &pixel_q);
                  o[x+i][y+j] = current_label;
	        }
	      }
	    }    /* for */
        }    /* while */
      }    /* if */
      p = p->next;
    }    /* while */
  }



    /* display the image - and the watersheds */

    for (i=0; i<IMAGE_WIDTH; i++)
	for (j=0; j<IMAGE_HEIGHT; j++) {
	    output_pixel = o[i][j];
		if (output_pixel==0) {
		    array2[i][j]=fg;
		} else {
		    intensity = array1[i][j];
		    array2[i][j]=intensity;
		}
	} 


    /* do garbage collection */
    for (n=0; n<COLORMAP_SIZE; n++)
	free_p_list(&list_pixels[n]);     

}
