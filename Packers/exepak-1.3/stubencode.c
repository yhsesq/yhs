
/* 
 * Encodes the stub in a file called stubencode.h that will be 
 * included in the main exepak binary. 
 */

#include <stdio.h>

void main()
{
	FILE *bfile,*ifile;
	int stubsize;
	unsigned char readbuf[256];
	int n,i,c = 0;
	
	if (!(bfile = fopen("exepak_stub","r"))) {
		printf("Can't open exepak_stub\n");
		exit(0);
	}
	if (!(ifile = fopen("stubcode.h","w"))) {
		printf("Can't create stubcode.h\n");
		exit(0);
	}
	
	fseek(bfile,0,SEEK_END);
	stubsize = ftell(bfile);
	rewind(bfile);
	printf("creating stubcode.h, exepak_stub is %d bytes long... ",stubsize);

    fprintf(ifile,"\n#define EXEPAK_STUBSIZE %d",stubsize);
	fprintf(ifile,"\n\nstatic unsigned char exepak_stub[EXEPAK_STUBSIZE] = {\n\t");
	while((n = fread(readbuf,1,sizeof(readbuf),bfile))) {
		for(i=0;i<n;i++) {
			fprintf(ifile,"0x%02x,",readbuf[i]);
			if (++c == 13) {
				fprintf(ifile,"\n\t");
				c = 0;
			}
		}
	}
	fprintf(ifile,"\n};\n");
	fclose(ifile);
	fclose(bfile);
    printf("ready\n");
	exit(0);
}
