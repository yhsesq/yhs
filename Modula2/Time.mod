MODULE Time;

(* Time/MODULA2  -------- Tutorial Excercise VI *)
(* Adds two different times together            *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn, ReadCard, WriteCard;


VAR
 hours1,minutes1,seconds1,hours2,minutes2,seconds2,hours3,minutes3,seconds3 : CARDINAL;


PROCEDURE ReadTime(VAR h1,m1,s1,h2,m2,s2 : CARDINAL);

BEGIN

	WriteLn;
	WriteLn;
	WriteString("Program to add two times together...");
	WriteLn;
	WriteLn;
	WriteString("Enter the hours of the first time :");
	ReadCard(h1);
	WriteLn;
	WriteString("Enter the minutes of the first time :");
	ReadCard(m1);
	WriteLn;
	WriteString("Enter the seconds of the first time :");
	ReadCard(s1);
	WriteLn;
	WriteString("Enter the hours of the second time :");
	ReadCard(h2);
	WriteLn;
	WriteString("Enter the minutes of the second time :");
	ReadCard(m2);
	WriteLn;
	WriteString("Enter the seconds of the second time :");
	ReadCard(s2);
	WriteLn;
	WriteLn;

END ReadTime;

PROCEDURE CalculateTime(h1,m1,s1,h2,m2,s2 : CARDINAL;VAR h3,m3,s3 : CARDINAL);

BEGIN

	s3:=s1+s2;

		IF (s3 > 59) THEN

				s3:=s3-60; m1:=m1+1;
			END;
	m3:=m1+m2;

		IF (m3 >59) THEN
			
				m3:=m3-60; h1:=h1+1;
			END;
	h3:=h1+h2;

END CalculateTime;

PROCEDURE WriteTime(h3,m3,s3 : CARDINAL);

BEGIN

	WriteLn;
	WriteLn;
	WriteString("The added time is = ");
	WriteCard(h3,3);
	WriteString(" : ");
	WriteCard(m3,2);
	WriteString(" : ");
	WriteCard(s3,2);
	WriteLn;
	WriteLn;

END WriteTime;

(* Main Program Begin... *)
BEGIN

	ReadTime(hours1,minutes1,seconds1,hours2,minutes2,seconds2);

	(* Small Error Check to see if the inputs are read correctly *)
		WriteLn;WriteString(" The First Time is - ");
		WriteCard(hours1,2);
		WriteString(" : ");
		WriteCard(minutes1,2);
		WriteString(" : ");
		WriteCard(seconds1,2);
		WriteLn;WriteString(" The Second Time is - ");
		WriteCard(hours2,2);
		WriteString(" : ");
		WriteCard(minutes2,2);
		WriteString(" : ");
		WriteCard(seconds2,2); WriteLn;
	(* Error check code ends *)

	CalculateTime(hours1,minutes1,seconds1,hours2,minutes2,seconds2,hours3,minutes3,seconds3);
	WriteTime(hours3,minutes3,seconds3);

END Time.

 
