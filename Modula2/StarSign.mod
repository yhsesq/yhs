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

(* Main Program Begin... *)

PROCEDURE FindDate(day : INTEGER; month : MonthType);

BEGIN
	IF ((day>=20) AND (month=Jan)) OR ((day<=17) AND (month=Feb)) 
	THEN WriteString("Aquarius");

	ELSIF ((day>=18) AND (month=Feb)) OR ((day<=19) AND (month=Mar)) 
	THEN WriteString("Pisces");

	ELSIF ((day>=20) AND (month=Mar)) OR ((day<=19) AND (month=Apr))
	THEN WriteString("Aries");

	ELSIF ((day>=20) AND (month=Apr)) OR ((day<=20) AND (month=May))
	THEN WriteString("Taurus");

	ELSIF ((day>=21) AND (month=May)) OR ((day<=20) AND (month=Jun))
	THEN WriteString("Gemini");

	ELSIF ((day>=21) AND (month=Jun)) OR ((day<=22) AND (month=Jul))
	THEN WriteString("Cancer");

	ELSIF ((day>=23) AND (month=Jul)) OR ((day<=22) AND (month=Aug))
	THEN WriteString("Leo");

	ELSIF ((day>=23) AND (month=Aug)) OR ((day<=22) AND (month=Sep))
	THEN WriteString("Virgo");

	ELSIF ((day>=23) AND (month=Sep)) OR ((day<=21) AND (month=Oct))
	THEN WriteString("Libra");

	ELSIF ((day>=23) AND (month=Oct)) OR ((day<=21) AND (month=Nov))
	THEN WriteString("Scorpio");

	ELSIF ((day>=22) AND (month=Nov)) OR ((day<=21) AND (month=Dec))
	THEN WriteString("Sagittarius");

	ELSIF ((day>=22) AND (month=Dec)) OR ((day<=19) AND (month=Jan))
	THEN WriteString("Capricorn");

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


PROCEDURE PrintDate();

BEGIN

	WriteLn;
	WriteLn; 
	WriteString(" Your Astrological Sign is : ");

END PrintDate;


BEGIN (*STAR*)


	InputDate(day, month);
	PrintDate();
	FindDate(day,month);

END Star.

 
