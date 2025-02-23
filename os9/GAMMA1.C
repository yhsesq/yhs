
/* Program to calculate the angles Beta and Gamma for Coventry */
/* 25/11/97 */
/* Gurdeep Singh Billan */


#include <conio.h>
#include <stdio.h>
#include <math.h>

main(void) {

	int day;                                    /* Declarations of variables */
	float Bcon,eqntime,SolarTime,Stan_Time,Time,S,W,Beta,Gamma;
	const S_Meridian=0, Lgt_Lct=1.3, phi=52.25;

	printf("Enter day of the year : ");         /* Prompt user to enter the */
	scanf ("%d",&day);                          /* day at which calculations */
															  /* should begin */
	do {
	Stan_Time=8.0; S=0.0; W=0.0; Bcon=0.0; eqntime=0.0; SolarTime=0.0;
		do {                                     /* initialising variables */

		printf("\nDay of year = %u",day);
		if (Stan_Time>12.5) {Time=Stan_Time-12.0;}
		else {Time=Stan_Time;}                   /* Setting the "am" and "pm" */
		printf("    Time = %.2f",Time);          /* modes */
		if (Stan_Time<12.0) {printf("am");}
		else {printf("pm");}

		Bcon = (day-1)*0.98630137;
															  /* Calculating the values */
															  /* of the variables */
		eqntime = 229.2*(0.000075+0.001868*cos(Bcon*M_PI/180)
		-0.032077*sin(Bcon*M_PI/180)-0.014615*cos(2*(Bcon*M_PI/180))
		-0.04089*sin(2*(Bcon*M_PI/180)));


		SolarTime = (Stan_Time*3600 +
				(4*(S_Meridian-Lgt_Lct) + eqntime)*60)/3600;


		S = 23.45*sin(((360.0/365.0)*(284.0+day))*M_PI/180);


		W = 15*(SolarTime-12);


		Beta = acos(sin(phi*M_PI/180)*sin(S*M_PI/180) +
		cos(phi*M_PI/180)*cos(S*M_PI/180)*cos(W*M_PI/180))*180/M_PI;
		printf("    Beta = %f",Beta);            /* Using functions to */
															  /* caclulate the required */
															  /* angles */
		Gamma = acos((sin(phi*M_PI/180)*cos(S*M_PI/180)*cos(W*M_PI/180) -
		cos(phi*M_PI/180)*sin(S*M_PI/180))/sin(Beta*M_PI/180))*180/M_PI;
		printf("    Gamma = %f",Gamma);

		Stan_Time = Stan_Time + 1.0;

	  /*	printf("\n"); */
		getch();

		} while (Stan_Time<16.5);
	day++;
	} while (day<34); //just for a quick demonstration value given as 4
			 //for real thing replace 4 with 366

	return 0;
}