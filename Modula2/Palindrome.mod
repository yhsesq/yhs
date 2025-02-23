MODULE Palindrome;

(* Palindrome  ------- Tutorial Excercise   XIII*)
(* Uses Stack and StringStuff to run Palindrome *)
(* Author : Yohann H. Sulaiman                  *)
(* Class: 101CS Programming Methodology         *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)

FROM InOut IMPORT WriteString,WriteInt, WriteLn, Read, EOL, Write, ReadString;
IMPORT Stack;
IMPORT StringStuff;

CONST
	NULL=0C;

VAR
stackTOP : Stack.StackType;
string1 : ARRAY [1..255] OF CHAR; (* normal string *)
string2 : ARRAY [1..255] OF CHAR; (* reversed string *)
counter : INTEGER; (* up counter *)
dcounter : INTEGER; (* down counter *)


PROCEDURE LoadStack (VAR stackTOP : Stack.StackType);
VAR
inchar : CHAR;

BEGIN
	counter := 1;
	Read(inchar);
	string1[counter] := inchar;
	WHILE (inchar <> EOL) DO
		Stack.Push(stackTOP,inchar);
		Read (inchar);
		INC(counter);
		string1[counter] := inchar;
	END;  (*WHILE*)
	string1[counter]:=string2[counter];
END LoadStack;

PROCEDURE EmptyStack(VAR stackTOP : Stack.StackType);

VAR 
outchar : CHAR;

BEGIN
	dcounter := 1;
	WHILE (NOT Stack.IsEmpty(stackTOP)) DO		
		Stack.Pop(stackTOP,outchar);
		string2[dcounter]:=outchar;
		INC(dcounter);
		END; (*WHILE*)
END EmptyStack;

PROCEDURE WriteResult(strings1,strings2 : ARRAY OF CHAR);
BEGIN
	WriteLn;
	IF (StringStuff.StringCompare(strings1,strings2)=StringStuff.same) THEN WriteString(" String is a Palindrome ...");
	ELSE WriteString(" String is NOT a Palindrome ...");
	END(*IF*)
END WriteResult;

(* Main Program Begin... *)
BEGIN

	WriteLn;
	WriteString("Palindrome program : ");
	WriteLn;WriteLn;
	WriteString(" Enter Palindrome : ");
	Stack.CreateStack(stackTOP);
	LoadStack(stackTOP);
	EmptyStack(stackTOP);
	WriteResult(string1,string2);

END Palindrome.

 
