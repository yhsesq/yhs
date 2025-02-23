MODULE Star;

(* Astrological Signs .. Tutorial Excercise X   *)
(* Finds the Astrological Sign of a date        *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn, ReadCard, WriteCard, ReadInt, WriteInt;

TYPE
MonthType=(Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec);

VAR
month : MonthType;
day : INTEGER;
sign : INTEGER;
(* Main Program Begin... *)

PROCEDURE FindDate(day : INTEGER; month : MonthType; VAR sign : INTEGER);

BEGIN
	IF ((day>=20) AND (month=Jan)) OR ((day<=17) AND (month=Feb)) 
	THEN sign:=1;

	ELSIF ((day>=18) AND (month=Feb)) OR ((day<=19) AND (month=Mar)) 
	THEN sign:=2;

	ELSIF ((day>=20) AND (month=Mar)) OR ((day<=19) AND (month=Apr))
	THEN sign:=3;

	ELSIF ((day>=20) AND (month=Apr)) OR ((day<=20) AND (month=May))
	THEN sign:=4;

	ELSIF ((day>=21) AND (month=May)) OR ((day<=20) AND (month=Jun))
	THEN sign:=5;

	ELSIF ((day>=21) AND (month=Jun)) OR ((day<=22) AND (month=Jul))
	THEN sign:=6;

	ELSIF ((day>=23) AND (month=Jul)) OR ((day<=22) AND (month=Aug))
	THEN sign:=7;

	ELSIF ((day>=23) AND (month=Aug)) OR ((day<=22) AND (month=Sep))
	THEN sign:=8;

	ELSIF ((day>=23) AND (month=Sep)) OR ((day<=21) AND (month=Oct))
	THEN sign:=9;

	ELSIF ((day>=23) AND (month=Oct)) OR ((day<=21) AND (month=Nov))
	THEN sign:=10;

	ELSIF ((day>=22) AND (month=Nov)) OR ((day<=21) AND (month=Dec))
	THEN sign:=11;

	ELSIF ((day>=22) AND (month=Dec)) OR ((day<=19) AND (month=Jan))
	THEN sign:=12;

	ELSE  WriteString("Unknown");

	END; (*IF*)

WriteLn;WriteLn;WriteLn;

END FindDate;

PROCEDURE InputDate(VAR day : INTEGER;VAR month : MonthType);
VAR

	monthcard : CARDINAL;

BEGIN
	WriteLn;
	WriteLn;
	WriteString("Program to find Astrological Signs");
	WriteLn;
	WriteLn;
	WriteString("Enter Day of the month :  ");
	ReadInt(day);
	WriteLn;
	WriteLn;
	WriteString(" Enter the number corresponding to the month : ");
	WriteLn;
	WriteLn;
	WriteString("1. January			2. February ");
	WriteLn;
	WriteString("3. March			4. April ");
	WriteLn;
	WriteString("5. May				6. June");
	WriteLn;
	WriteString("7. July				8. August");
	WriteLn;
	WriteString("9. September			10. October");
	WriteLn;
	WriteString("11. November			12. December");
	WriteLn;
	WriteLn;
	WriteString(">>");
	ReadCard(monthcard);
	CASE	monthcard	OF
		  1: month := Jan
		| 2: month := Feb
		| 3: month := Mar
		| 4: month := Apr
		| 5: month := May
		| 6: month := Jun
		| 7: month := Jul
		| 8: month := Aug
		| 9: month := Sep
		| 10: month := Oct
		| 11: month := Nov
		| 12: month := Dec
	ELSE
		WriteString(" Not a valid choice..Assumed JAN ");
		month := Jan
	END; (*Case*)

	WriteLn;
	
END InputDate;


PROCEDURE PrintDate(sign : INTEGER);

BEGIN

	WriteLn;
	WriteLn; 
	WriteString(" Your Astrological Sign is : ");
	CASE sign OF
		1: WriteString("Aquarius");
	|	2: WriteString("Pisces");
	|	3: WriteString("Aries");
	|	4: WriteString("Taurus");
	|	5: WriteString("Gemini");
	|	6: WriteString("Cancer");
	|	7: WriteString("Leo");
	|	8: WriteString("Virgo");
	|	9: WriteString("Libra");
	|	10:WriteString("Scorpio");
	|	11:WriteString("Sagittarius");
	|	12:WriteString("Capricorn");
END;(*CASE*)
WriteLn;
END PrintDate;


BEGIN (*STAR*)


	InputDate(day, month);
	FindDate(day,month,sign);
	PrintDate(sign);

END Star.

 
