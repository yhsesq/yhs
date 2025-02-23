MODULE MadLibtium; 

(* Mad Libtium Exercise III                     *)
(* Plays a game called Mad Libtumi              *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn, ReadString;

TYPE
StrType = ARRAY [0..100] OF CHAR;

VAR name,noun,animal,verb : StrType; 

PROCEDURE GetUserWords(VAR name,noun,animal,verb:StrType);
BEGIN
WriteString("Enter your name here :");
ReadString(name);
WriteLn;
WriteString("Enter your noun here :");
ReadString(noun);
WriteLn;
WriteString("Enter your animal here :");
ReadString(animal);
WriteLn;
WriteString("Enter your verb here :");
ReadString(verb);
WriteLn;
END GetUserWords;

PROCEDURE DisplayText(name,noun,animal,verb : StrType);

BEGIN
WriteLn;
WriteString("Gosh, Doctor ");
WriteString(name);
WriteString(", I don't know what happened to my ");
WriteString(noun);
WriteString(". My pet ");
WriteString(animal);
WriteString(" must have ");
WriteString(verb);
WriteString(" it.");
WriteLn;
END DisplayText;

(* Main Program Begin... *)
BEGIN(*Main Program*)
	GetUserWords(name,noun,animal,verb);
	DisplayText(name,noun,animal,verb)
END MadLibtium.

 
