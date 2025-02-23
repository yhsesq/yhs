IMPLEMENTATION MODULE MonopolyIO;

FROM Random IMPORT Random;
FROM InOut IMPORT WriteString, WriteLn;

PROCEDURE InitialiseDie(VAR Die : DieType);

BEGIN

	Die:=six

END InitialiseDie;

PROCEDURE ThrowDie(VAR Die : DieType);

VAR
	cardRand: CARDINAL;

BEGIN
	cardRand:=TRUNC(Random());
	cardRand:=TRUNC((Random()*5.0)+1.5);
	CASE cardRand OF
		1: Die := one;
	|	2: Die := two;
	|	3: Die := three;
	|	4: Die := four;
	|	5: Die := five;
	|	6: Die := six;
	ELSE 	Die := zero; (*Error trap..captured in ShowDie*)
	END;
END ThrowDie;

PROCEDURE ShowDie(VAR Die : DieType);

BEGIN

	WriteLn;

	CASE Die OF
		one : WriteString(" Die is showing ONE ");WriteLn; 
	|	two : WriteString(" Die is showing TWO ");WriteLn;
	|	three : WriteString(" Die is showing THREE ");WriteLn;
	|	four : WriteString(" Die is showing FOUR ");WriteLn;
	|	five : WriteString(" Die is showing FIVE ");WriteLn;
	|	six : WriteString(" Die is showing SIX ");WriteLn;
	ELSE	WriteString(" ERROR: INVALID SETTING FOR DIE : DIETYPE ");WriteLn;
	END;

	WriteLn;

END ShowDie; 




END MonopolyIO.
