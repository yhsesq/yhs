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


/*----------------------------------------------------------------------*/
/*                                                             	 	*/
/*     File: neighbour.c						*/
/*									*/
/*     CT Image Application Program					*/
/*									*/
/*     OSF/Motif version.						*/
/*									*/
/*     V1.0 March 1997							*/
/*									*/
/*----------------------------------------------------------------------*/

#include "main.h"

#define UP    0
#define DOWN  1
#define LEFT  2
#define RIGHT 3


struct neighbour {
    char reg_number;
    short carry;
};
typedef struct neighbour NEIGH;


NEIGH neigh[4][4] = {
    {'2',1,'2',0,'1',1,'1',0},
    {'3',1,'3',0,'0',0,'0',1},
    {'0',0,'0',1,'3',1,'3',0},
    {'1',0,'1',1,'2',0,'2',1}
};


/* Function prototypes */
void get_neighbours(PATHPTR *list, TREEPTR root, char c[]);
static void analize(PATHPTR *list, TREEPTR root, char path[], short where);
static void include_sub_neigh(PATHPTR *list,char path[],
			TREEPTR node, short level,short where);

static void get_4_neighbours(char c[],char v1[],char v2[],char v3[],char v4[]);
static void analize_label(char *c, short i, short pos);

void initialize_label(char c[], int start);
void copy_label (char *dest, char *source);
void write_label(char *c);
void insert(PATHPTR *list, char n[]);

extern TREEPTR get_leaf(char path[]);
extern void insert_path(PATHPTR *first, PATHPTR *end, char n[]);
extern short yes;
PATHPTR end=NULL;



/* Function definitions */

/*----------------------------------------------------------------------*/
/* g e t _ n e i g h b o u r s						*/
/*									*/
/* Return the pointer to a list of the neighbours of a given region	*/
/*----------------------------------------------------------------------*/

void get_neighbours(PATHPTR *list, TREEPTR root, char c[])
{
    char neigh1[10],neigh2[10],neigh3[10],neigh4[10];

    get_4_neighbours(c,neigh1,neigh2,neigh3,neigh4);
    analize(list,root,neigh1,UP);
    analize(list,root,neigh2,DOWN);
    analize(list,root,neigh3,LEFT);
    analize(list,root,neigh4,RIGHT);
}



/*----------------------------------------------------------------------*/
/* g e t _ 4 _ n e i g h b o u r s					*/
/*									*/
/* Compute the 4-adjacent neighbours that belong to the same level that	*/
/* the given region							*/
/*----------------------------------------------------------------------*/

void get_4_neighbours(char c[],char v1[],char v2[],char v3[],char v4[])
{
    short length;

    initialize_label(v1,0); initialize_label(v2,0);
    initialize_label(v3,0); initialize_label(v4,0);
    copy_label(v1,c); copy_label(v2,c); copy_label(v3,c); copy_label(v4,c);

    length = strlen(c);
    analize_label(v1,length-1,UP);
    analize_label(v2,length-1,DOWN);
    analize_label(v3,length-1,LEFT);
    analize_label(v4,length-1,RIGHT);
}



/*----------------------------------------------------------------------*/
/* a n a l i z e _ l a b e l						*/
/*									*/
/* Modifies the label according to the technique presented in the	*/
/* report to find the 4-adjacent neighbours				*/
/*----------------------------------------------------------------------*/

void analize_label(char *subc, short i, short pos)
{
    short n;
    char last[2];

    last[1]='\0';
    if (i>=0) {
	last[0] = subc[i];
	n=atoi(last);
	subc[i]=neigh[n][pos].reg_number;
	if (neigh[n][pos].carry) analize_label(subc,i-1,pos);
    } else initialize_label(subc,0);
}



/*----------------------------------------------------------------------*/
/* a n a l i z e							*/
/*									*/
/* Analise the path to reach the neighbour in the same level to find	*/
/* out if the neighbours in that branch are placed in a higher or lower */
/* level in the hierarchy						*/
/*----------------------------------------------------------------------*/

void analize(PATHPTR *list, TREEPTR root, char path[], short where)
{
    short i, length;
    TREEPTR node;

    i=0;
    node=root;
    length = strlen(path);
    if (length>0) {
	while ( (node->region==NULL) && (i<length) ) {
	    switch (path[i]) {
		case '0': node=node->subreg0; break;
		case '1': node=node->subreg1; break;
		case '2': node=node->subreg2; break;
		case '3': node=node->subreg3; break;
	    }
	    i++;
	}
	if (i==length) {
	    if (node->region!=NULL) insert(list,path);
	    else include_sub_neigh(list,path,node,i,where);
	}
	if (i<length) {
	    initialize_label(path,i);
	    insert(list,path);
	}
    }
}



/*----------------------------------------------------------------------*/
/* i n c l u d e _ s u b _ n e i g h					*/
/*									*/
/* Add to the list those neighbours that are placed in a lower level	*/
/* in the hierarchy							*/
/*----------------------------------------------------------------------*/

void include_sub_neigh(PATHPTR *list,char path[],
				TREEPTR node,short level,short where)
{
    TREEPTR node1, node2;
    char neigh1[10], neigh2[10];

    initialize_label(neigh1,0); initialize_label(neigh2,0);
    copy_label(neigh1,path); copy_label(neigh2,path);

    switch (where) {
	case UP :
	    node1=node->subreg2;
	    neigh1[level]='2';
	    if (node1->region==NULL)
			include_sub_neigh(list,neigh1,node1,level+1,where);
	    	else { insert(list,neigh1); initialize_label(neigh1,level+1); }
	    node2=node->subreg3;
	    neigh2[level]='3';
	    if (node2->region==NULL)
			include_sub_neigh(list,neigh2,node2,level+1,where);
		else { insert(list,neigh2); initialize_label(neigh2,level+1); }
	    break;

	case DOWN :
	    node1=node->subreg0;
	    neigh1[level]='0';
	    if (node1->region==NULL)
			include_sub_neigh(list,neigh1,node1,level+1,where);
		else { insert(list,neigh1); initialize_label(neigh1,level+1); }
	    node2=node->subreg1;
	    neigh2[level]='1';
	    if (node2->region==NULL)
			include_sub_neigh(list,neigh2,node2,level+1,where);
		else { insert(list,neigh2); initialize_label(neigh2,level+1); }
	    break;

	case LEFT :
	    node1=node->subreg1;
	    neigh1[level]='1';
	    if (node1->region==NULL)
			include_sub_neigh(list,neigh1,node1,level+1,where);
		else { insert(list,neigh1); initialize_label(neigh1,level+1); }
	    node2=node->subreg3;
	    neigh2[level]='3';
	    if (node2->region==NULL)
			include_sub_neigh(list,neigh2,node2,level+1,where);
		else { insert(list,neigh2); initialize_label(neigh2,level+1); }
	    break;

	case RIGHT :
	    node1=node->subreg0;
	    neigh1[level]='0';
	    if (node1->region==NULL)
			include_sub_neigh(list,neigh1,node1,level+1,where);
		else { insert(list,neigh1); initialize_label(neigh1,level+1); }
	    node2=node->subreg2;
	    neigh2[level]='2';
	    if (node2->region==NULL)
			include_sub_neigh(list,neigh2,node2,level+1,where);
		else { insert(list,neigh2); initialize_label(neigh2,level+1); }
	    break;
    }
}



/*----------------------------------------------------------------------*/
/* c o p y _ l a b e l							*/
/*									*/
/* Copy the source label in the destination label			*/
/*----------------------------------------------------------------------*/

void copy_label(char *dest, char *source)
{
    short len,i,n;
    char num;

    for (i=0; i<10; i++) dest[i]=source[i];
}



/*----------------------------------------------------------------------*/
/* initialize_label							*/
/*									*/
/* Set to null the characters since the position specified till the	*/
/* end of the label								*/
/*----------------------------------------------------------------------*/

void initialize_label(char c[], int start)
{
    short i;

    for (i=start; i<=10; i++) c[i]='\0';
}



/*----------------------------------------------------------------------*/
/* w r i t e _ l a b e l						*/
/*									*/
/* Print the label in the screen					*/
/*----------------------------------------------------------------------*/

void write_label(char *c)
{
    short i, length;

    length=strlen(c);
    for (i=0; i<length; i++)
	fprintf(stderr,"%c",c[i]);
    fprintf(stderr," ");
}



/*----------------------------------------------------------------------*/
/* i n s e r t								*/
/*									*/
/* Insert the label in a list						*/
/*----------------------------------------------------------------------*/

void insert(PATHPTR *list, char n[])
{
    PATHPTR newone, last;


    if (get_leaf(n)->included==yes) 
	/*Add at the end of the list*/
	insert_path(list,&end,n);
    else {

      if (strlen(n)>0) {
	newone = (PATHPTR)malloc(sizeof(PATH));
	initialize_label(newone->path,0);
	copy_label(newone->path,n);
	newone->next = NULL;
    
	if (*list==NULL) 
		*list=end=newone;
	else {
	    newone->next = *list;
	    *list=newone;
	}
      }
    }
}

