MODULE SquareTest;

(* Square Tester -------- Tutorial Excercise IV *)
(* Tests a function Square which squares a no.  *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn;
FROM RealInOut IMPORT ReadReal,WriteReal;

VAR
SqIn,SqAout : REAL;

PROCEDURE Square(SqP : REAL):REAL;

BEGIN

	RETURN(SqP*SqP);

END Square;

PROCEDURE ReadStr(VAR SqZ : REAL);

BEGIN

	WriteLn;
	WriteLn;
	WriteString("Program to test Square Functions");
	WriteLn;
	WriteLn;
	WriteString("Enter the No. to be squared : ");
	ReadReal(SqZ);
	WriteLn;
	WriteLn;

END ReadStr;

PROCEDURE WriteStr(SqA : REAL);

BEGIN

	WriteString("The Square of the no. is : ");
	WriteReal(SqA,10);
	WriteLn;
	WriteLn;

END WriteStr;
(* Main Program Begin... *)
BEGIN

	ReadStr(SqIn);
	SqAout:=Square(SqIn);
	WriteStr(SqAout);

END SquareTest.

 
