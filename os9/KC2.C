#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <conio.h>

int main(int argc, char *argv[])
{
FILE *txt;
int 	a1=0,a2=0,a3=0,a4=0,a5=0,a6=0,
		aa1,aa2,aa3,aa4,aa5,aa6,
		at1=0,at2=0,at3=0,at4=0,at5=0,at6=0;
int 	duration,line;
int 	status,pid,wcp;
int	time[800],RGV_no[800],state[800],R_pid[800],R_wcp[800];
int 	size=0,count=0,mov_time=0,pallet_no=0,carry_time=0;
int 	temp[8][8]={0},wc_mov_time[8][8]={0};
int   Rt[64];
int	dep,arl,loc,i;
float	tempRt[8][8];
char	*wc_ptr,*temp_ptr;
char  wc_string[]={"STOWC1WC2WC3WC4WC5WC6DIS"};
char 	string[80],
		filename[70];
char 	*strp, *wcstrp,*wcstr,
		*timep,*hourp,*minp,*secp;
char 	*wc,*ptr;
int   choice=0;

clrscr();
if (argc>1)
strcpy(filename,argv[1]);
else
{
printf ("\nEnter the file name : ");
scanf("%70s",filename);
}

if((txt = fopen(filename,"r")) == NULL)
{
perror("Error opening file because ");
return 1;
}


//============= TO CALCULATE THE DATA DURATION =============================
//============= AND WORK CENTRE STATISTICS =================================

duration=0;line=0;

while(!feof(txt))
	{
	strp=fgets(string,80,txt);
	if(strp!=NULL)
		{
		string[strlen(string)-1]='\0';
		wcstr=strstr(string,"||!|?||P|");
		wcstrp=string;
		timep=wcstrp+9;
		hourp=strtok(timep,":");
		minp=strtok(NULL,":");
		secp=strtok(NULL,"|");
		time[0]=(atoi(hourp))*3600+(atoi(minp))*60+atoi(secp);

		if(wcstr!=NULL)
			{
			ptr=wcstr+8;
			status=atoi(strtok(ptr,"|"));
			pid=atoi(strtok(NULL,"|"));
			ptr=string;
			wc=ptr+3;
			wcp=atoi(strtok(wc,"|"));

			if(status&16)
			switch(wcp)
				{
				case 1:	aa1=time[0];
							a1=a1+1;
							break;
				case 2:	aa2=time[0];
							a2=a2+1;
							break;
				case 3:	aa3=time[0];
							a3=a3+1;
							break;
				case 4:	aa4=time[0];
							a4=a4+1;
							break;
				case 5:	aa5=time[0];
							a5=a5+1;
							break;
				case 6:	aa6=time[0];
							a6=a6+1;
							break;
				}

				if((status&32)&&(pid!=0))
				switch(wcp)
				{
				case 1:	at1+=time[0]-aa1;
							break;
				case 2:	at2+=time[0]-aa2;
							break;
				case 3:	at3+=time[0]-aa3;
							break;
				case 4:	at4+=time[0]-aa4;
							break;
				case 5:  at5+=time[0]-aa5;
							break;
				case 6:	at6+=time[0]-aa6;
							break;
				}
			}

line++;

if(line>1)
	{
		if(time[0]>time[1])
		{
		duration+=time[0]-time[1];
		time[1]=time[0];
		}
	}
else
time[1]=time[0];
	}	}
rewind(txt);


//=================== END OF WC STATISTICS==================================
//==========================================================================
//=================== RGV STATISTICS =======================================

line=0;

while(!feof(txt))
{
strp=fgets(string,80,txt);

	if(strp != NULL)
		{
		string[strlen(string)-1]='\0';
		wcstr=strstr(string,"||!|?||R|");

		if(wcstr != NULL)
			{
			timep=wcstr-8;
			hourp=strtok(timep,":");
			minp=strtok(NULL,":");
			secp=strtok(NULL,"|");
			time[line]=(atoi(hourp))*3600+(atoi(minp))*60+atoi(secp);
			ptr=wcstr+8;
			RGV_no[line]=atoi(strtok(ptr,"|"));
			state[line]=atoi(strtok(NULL,"|"));
			wc_ptr=strtok(NULL,"|");
			temp_ptr=strstr(wc_string,wc_ptr);
			R_wcp[line]=(temp_ptr-wc_string)/3;
			R_pid[line]=atoi(strtok(NULL,"|"));
			line++;
			}

		if((strstr(string,"||!|F||)-14)==1)
		printf("1");





			}}

//RGV CALCULATION

size=line-1;

for(count=0;count<=size;count++)

{
	if(state[count] == 3)                            /*moving time*/
		mov_time += time[count + 1] - time[count];

	if((state[count] == 3) && (R_pid[count] != 0))   /*carry time*/
		{
		carry_time += time[count + 1] - time[count];
		pallet_no++;
		}


	for(dep=0;dep<=7;dep++)
		for(arl=0;arl<=7;arl++)
			if((dep != arl) && (state[count + 1] != NULL))
				{
				if((state[count+1] == 3) && (R_wcp[count + 1] == arl)
												 && (R_wcp[count] == dep))/*sto to wc1 time*/
				{
				wc_mov_time[dep][arl] += time[count + 2] - time[count + 1];
				temp[dep][arl]++;
				}
				}

}
loc=0;
for(dep=0;dep<=7;dep++)
for(arl=0;arl<=7;arl++)
{if((arl != dep) && (temp[dep][arl] != 0))
	{tempRt[dep][arl] = (wc_mov_time[dep][arl])/(temp[dep][arl]);
	Rt[loc] = tempRt[dep][arl];}
else Rt[loc] = 0;
loc++;}
rewind(txt);

//=================== END OF RGV STATISTICS ================================
//==========================================================================
//=================== PALLET STATISTICS=====================================
if(




//=================== TO SELECT OUTPUT =====================================
while(choice != 5)
{
	fflush(stdin);
	printf("\n\nYour choice of output:");
	printf("\n----------------------");
	printf("\n1.Duration of data and Work centre statistics");
	printf("\n2.RGV statistics");
	printf("\n3.Pallet statistics");
	printf("\n5.Terminate the programme");
	printf("\nPlease select output ->");
	fflush(stdin);
	scanf("%d",&choice);

//=============== PRINT DURATION OUTPUT ====================================
if(choice == 1)
{
clrscr();
printf("\nDuration of data is %is\n",duration);
printf("\nWork Centre statistics");
printf("\n\tWC1 : %i pallets processed for %.0f%% of the time\n",
			a1,(float)at1/duration*100);
printf("\tWC2 : %i pallets processed for %.0f%% of the time\n",
			a2,(float)at2/duration*100);
printf("\tWC3 : %i pallets processed for %.0f%% of the time\n",
			a3,(float)at3/duration*100);
printf("\tWC4 : %i pallets processed for %.0f%% of the time\n",
			a4,(float)at4/duration*100);
printf("\tWC5 : %i pallets processed for %.0f%% of the time\n",
			a5,(float)at5/duration*100);
printf("\tWC6 : %i pallets processed for %.0f%% of the time\n",
			a6,(float)at6/duration*100);
}

//=============== PRINT RGV STATISTIC ======================================
if(choice == 2)
{
clrscr();
printf("\nRGV Statistics\n");
printf("\n\tProportion of time moving is %.0f%%",(float)mov_time/duration*100);
printf("\n\tCarried %u pallets for %.0f%% of time.\n",
		pallet_no,(float)carry_time/duration*100);
printf("\n\tAverage Route Times in seconds for RGV no. 1");
printf("\n\t\t\tD E S T I N A T I O N");
printf("\n D\t\tSTO\tWC1\tWC2\tWC3\tWC4\tWC5\tWC6\tDIS");
printf("\n E\tSTO");
for(i=0;i<=7;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n P\tWC1");
for(i=8;i<=15;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n A\tWC2");
for(i=16;i<=23;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n R\tWC3");
for(i=24;i<=31;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n T\tWC4");
for(i=32;i<=39;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n U\tWC5");
for(i=40;i<=47;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n R\tWC6");
for(i=48;i<=55;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
printf("\n E\tDIS");
for(i=56;i<=63;i++) if(Rt[i] != 0) printf("\t%u",Rt[i]);
						else printf("\t---");
}
//=============== PRINT PALLET STATICTIC ===================================
if(choice == 3)
{
clrscr();
printf("Pallet Statistics");
}
//=============== TERMINATION OF PROGRAM ===================================
}
if(choice == 5)
printf("\nUser has terminated the programme.");
return 0;
}
//=============== END OF PROGRAM ===========================================
