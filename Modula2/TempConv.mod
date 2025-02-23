MODULE TempConv;

(* Temperature Conversion Tutorial Excercise II *)
(* Converts Temperatures Farenheit to Celcius   *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn;
FROM RealInOut IMPORT ReadReal,WriteReal;
CONST mult=9.0/5.0;con=32.0;
VAR
centigrade,farenheit : REAL;
(* Main Program Begin... *)
BEGIN
	WriteLn;
	WriteLn;
	WriteString("Program for Conversion of Celcius to Farenheit");
	WriteLn;
	WriteLn;
	WriteString("Enter temperature in Celcius :");
	ReadReal(centigrade);
	WriteLn;	
	WriteLn;
	farenheit:=(centigrade * mult)+con;
	WriteString("Temperature in Farenheit :");
	WriteReal(farenheit,7);
	WriteString(" Degrees Farenheit");
	WriteLn;
	WriteLn
END TempConv.

 
