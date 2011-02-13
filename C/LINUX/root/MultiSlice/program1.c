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


/* This programm give two output files, imagnum? and scalnum? with an image normal and rescaled
 respectivaly from the input file glo1. The file glo1 contain 10 CT images. num is the number of
 image desired by the user. 
*/



#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <ctype.h>
#include <sys/file.h>
#include <stdlib.h>

int read_slice();
int rescale();
void maximo();
void minimo();


FILE *out_file, *in_file, *scaled;            /* define pointer to output and input file.*/
short  max, min;               
char num[3];
int seek_ptr;
long int num_bytes;

int main()

{
	char ch;

        read_slice();
	maximo();
	minimo();

	printf("\n Do you desire a small file  (dim 256*256) ? y/n ");

	scanf("%c",  &ch); 
	if (ch == 'y') { rescale_reduce() ; }
		     else { rescale(); }

	fclose(in_file);
	fclose(out_file);


	return 0;
}

int read_slice ()
{

	long int  cont_byte;  
	int  cont_imag, int_num;                 /* num_image desided*/                                                               							 	          	   											        
	static char  imag[]= "imag";
	short value;

	printf("\nEnter number of image desired ? ");
        fgets(num,4,stdin);
	
	/* open to read and assign phisic_name*/

	if ( (in_file = fopen("glo1", "rb"))== NULL)
                { printf( "error in input file\n");
                  exit(1);
                }
  
	if ((out_file = fopen(strcat(imag, num), "wb")) == NULL)                                            						                   
		{
      		    printf ("\nUnable to open file %s: errno = %d", "file_im1", 
				errno);
		    perror(",message");
		    return 1;
		}
                
        /*  copy 524288 bytes */
	
        int_num= atoi(num);
	num_bytes = (((int_num-1)*512*512 )+1)*sizeof(short) ;	
	if ( (seek_ptr = fseek (in_file, num_bytes,0))== -1)
		{ printf( "error in lseek");
		  exit(1);
		}
	else { printf("\nlseek = %d\n", seek_ptr); }  
     	for (cont_byte=1; cont_byte<= (512 * 512); cont_byte++)
              { 
		fread(&value,sizeof(short),1,in_file);
		fwrite(&value, sizeof(short),1,out_file);
	
	      }
}

														
void maximo()
{
	short max_aux, temp, aux;
	long int i;

	max_aux=-32768;
 	aux = -32768;
	fseek (in_file, num_bytes,0);
	
	for(i=0; i<(512*512); i++)
	   { 
	     fread(&temp,sizeof(short),1, in_file);
	        

 	     if (temp >= max_aux)
		    max_aux=temp;
	   }
	max= max_aux;

printf("\n maxfinal= %d\n", max_aux) ;
 	
} 


void minimo()
{
	short min_aux, temp, aux;
	long int i;

	min_aux=32767;
        aux= 32767;   
 	fseek (in_file, num_bytes,0);
	
	for(i=0; i<(512*512); i++)
	   { 
	      fread(&temp,sizeof(short),1,in_file);
	    	      if (temp <= min_aux)
		    min_aux=temp;
	   }
	min=min_aux;


printf("\n minfinal= %d\n", min_aux) ;
 

}    	 
   	 

int rescale()
 
{
	static char  scal[]= "scal";
	float escala;
	static int ptos=256;
	short  valor;
	unsigned char rescaled;
	long int  cont_byte;  

	fseek (in_file, num_bytes,0);
	if ((scaled = fopen(strcat(scal, num), "wb")) == NULL)                                            						                   
		{
      		    printf ("\nUnable to open file %s: errno = %d", "file_im", 
				errno);
		    perror(",message");
		    return 1;
		}
	
	fseek (in_file, num_bytes,0);
 		
	escala = (ptos /(max - min)); 

	for (cont_byte=1; cont_byte<= (512 * 512); cont_byte++)
              { 
		fread(&valor,sizeof(short),1,in_file);

	
		rescaled =(char) ((float) (valor-min)/ (max-min) * ptos); 
	

                fwrite(&rescaled, sizeof(char),1,scaled);

	      }
	
	 fclose(scaled);
}


rescale_reduce()
 
{
	static char  scal[]= "scalr";
	static int ptos=256;
	short  valor;
	unsigned char rescaled;
	long int  cont_byte, row_cont;  

	fseek (in_file, num_bytes,0);
	if ((scaled = fopen(strcat(scal, num), "wb")) == NULL)                                            						                   
		{
      		    printf ("\nUnable to open file %s: errno = %d", "file_im", 
				errno);
		    perror(",message");
		    return 1;
		}
	
	fseek (in_file, num_bytes,0);
 		
	for (cont_byte=1; cont_byte<= (512 * 512); cont_byte++)
              { 
		row_cont = cont_byte / 512 ;
		fread(&valor,sizeof(short),1,in_file);
		if (((cont_byte % 2) == 1) && ((row_cont % 2) == 1))
		     {
	
			rescaled =(char) ((float) (valor-min)/ (max-min) * ptos);
                	fwrite(&rescaled, sizeof(char),1,scaled);

		      }
	
	      }
	 fclose(scaled);
}
