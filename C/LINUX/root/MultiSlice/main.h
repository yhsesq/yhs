/*-------------------------------------------------------------*/
/*                                                             */
/*     File: main.h                                            */
/*                                                             */
/*     C-T Image Application Program                           */
/*                                                             */
/*     OSF/Motif version.                                      */
/*                                                             */
/*-------------------------------------------------------------*/

#include <Xm/Xm.h>
#include <math.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <X11/Xlib.h>
#include <X11/Xatom.h>

#define MAXARGS           50

#define MAXPOINTS       1500 /* max num. of points to define a region */
#define PI     3.14159265359 

#define IMAGE_WIDTH      512
#define IMAGE_HEIGHT     512

#define CSCALE           255
#define COLORMAP_SIZE    256

#define NONE	        0

#define OPEN_FILE	10
#define VIEW_FILE       13
#define SAVE_AS         11
#define EXIT            12

#define HISTOGRAM       20
#define HISTO_EQ        21
#define THRESHOLD       22
#define MEAN_FILTER	230
#define MEDIAN_FILTER   231
#define SMOOTH_T        232
#define SMOOTH_MASK1    233
#define SMOOTH_MASK2    234
#define MEDIAN_VF       235

#define REGION_GROWING  300
#define WATERSHED	301
#define EDGE_DETECTION  310
#define EDGE_SOBEL      311
#define EDGE_LAPLAC     312
#define EDGE_KIRSCH     313
#define EDGE_UNSHARP    314
#define EDGE_FREI       12
#define EDGE_PREWITT    13
#define LINE_FREI       14
#define EDGE_MARR       15
#define EDGE_ROBERTS    16
#define EDGE_VERT       17
#define EDGE_HORIZ      18
#define EDGE_HV         19
#define AREA            20
#define PERIMETER       21
#define CTOMASS         22
#define MOMENTS         23	
#define CIRCULAR        24
#define	GAUSSIAN	321	
#define	DGAUSSIAN	322
#define	DGAUXGAUY	323
#define	DGAUYGAUX	324
#define	TOTALGRADIENT	325
#define	GRADIENTXY	326



#define HOUGH           31
#define VERSION         33

#define TRACKING           34

#define RECTANGLES	   35
#define ELLIPSES           36
#define CIRCLES            37

#define REGION_CUT      320

#define ZOOM		400


#define SPLIT_MERGE	100

#define SELECT  "Select menu option."
#define LOAD    "Load file in editor."
#define VIEW    "Load file in viewer."
#define CLICK   "Click on INPUT WINDOW to select the seed."

#define DRAW    "Draw a polygon"
#define TRACK   "Mouse tracking"
#define CUT     "Define tracking on the OUTPUT WINDOW."

#define MAX_BUFFER_SIZE  300


/* Declare the structure of a list*/
struct tpinfo {
       int coord_x, coord_y;
       unsigned long grey_level;
};

typedef struct tpinfo TPINFO;


struct node {
       TPINFO 	   infolist;
       struct node *next;
};
typedef struct node NODE;
typedef NODE *NODEPTR;


struct tpregion {
	Region  ptr;
	NODEPTR points;
	int     num_points;
	int     label;
};
typedef struct tpregion TPREGION;


struct region_node {
	TPREGION region;
	struct region_node *next;
	struct region_node *previous;
};
typedef struct region_node REGION;
typedef REGION *REGIONPTR;


struct pointerlist {
	REGIONPTR region_pointer;
	struct pointerlist *next;
};
typedef struct pointerlist POINTER_LIST;
typedef POINTER_LIST *REGION_LIST_PTR;



/***************************************************/
/* Structures emplied for split and merge algorithm*/
/***************************************************/

typedef struct tree TREE;
typedef TREE *TREEPTR;

typedef struct merge MERGE;
typedef MERGE *MERGEPTR;

typedef struct leaf LEAF;
typedef LEAF *LEAFPTR;


/*Structure for the hierarchical tree*/
struct tree {
    Region      region;
    short       included, level;
    float	mean, sqr_mean;
    struct tree *subreg0, *subreg1, *subreg2, *subreg3;
    MERGEPTR    pmerge;
};


/*List of regions that finally belong to the same merged region*/
struct leaf {
    TREEPTR      leaf_ptr;
    struct leaf  *next;
};


/*List of pointers to the lists of merged regions*/
struct merge {
    LEAFPTR	  leaf_list, last_node;
    double	  mean,
		  sqr_mean;
    unsigned long num_points;
    struct merge  *next,
		  *moved;
};


/*Structure used to room the neighbourhood regions of a given region*/
struct path {
    char path[10];
    struct path *next;
};
typedef struct path PATH;
typedef PATH *PATHPTR;
