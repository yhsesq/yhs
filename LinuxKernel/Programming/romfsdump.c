#include <stdlib.h>
#include <stdio.h>
#include <math.h>

main()
{
int i;
long j;
char s;
FILE *file1;
file1=(FILE *)fopen("pda.rom","r");
// printf("File opened ok..\n");
for (i=0;i<1025;i++)
{
s=fgetc(file1);
printf("array[%i]=%i;\n",i,s);
// printf("%i char is %c and numeric is %i. \n",i,s,s);
}
fclose(file1);
}
