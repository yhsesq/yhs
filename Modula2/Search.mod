MODULE Search;
(*
**Binary Search on a reverse ordered file of 100 names
**Tutorial Excercise XII
**Author:Zurk Technology Inc. 	Compiler: GPM/2
**Uses: InOut, StringStuff
*)

FROM InOut IMPORT OpenInput, CloseInput, WriteString, WriteLn, ReadString, ReadCard, WriteCard;
FROM StringStuff IMPORT StringCompare, Result;
CONST
	LIMIT = 100;
TYPE
	WordType = ARRAY [0..50] OF CHAR;
	ListType = ARRAY [1..LIMIT] OF WordType;

VAR
	list : ListType;
	name : WordType;
	position : CARDINAL;
	foundWord : BOOLEAN;

PROCEDURE GetList(VAR list : ListType);
VAR
	index : CARDINAL;
BEGIN
	WriteString("Type input file name, default extension is 'dat' ");
	WriteLn;
	OpenInput("dat");
	FOR index:= 1 TO LIMIT DO
		ReadString(list[(LIMIT+1)-index]);
	END;
	WriteLn;
	WriteString("Read complete...");
	WriteLn;
	WriteLn;
	WriteString("Enter a name to find :");
	WriteLn;
	CloseInput();
END GetList;

PROCEDURE GetName(VAR name : ARRAY OF CHAR);
BEGIN
	ReadString(name);
	WriteLn;
END GetName;

PROCEDURE Binary(list:ListType;
			name : ARRAY OF CHAR;
			startPos, endPos : CARDINAL;
			VAR foundWord:BOOLEAN;
			VAR position:CARDINAL);
(*Binary name search
**Uses: StringCompare from StringStuff*)
CONST
	NULL=0C;
VAR
	midPos:CARDINAL;
BEGIN
	IF startPos>endPos THEN
		foundWord:=FALSE;
		position:=0
	ELSE
		midPos:=(startPos + endPos) DIV 2;
		IF StringCompare(name,list[midPos])=same THEN
			foundWord:=TRUE;
			position:=(LIMIT+1)-midPos
		ELSIF StringCompare(name,list[midPos])=less THEN
	 		Binary(list,name,startPos, midPos-1,foundWord,position)
		ELSE
			Binary(list,name,midPos+1, endPos,foundWord,position)
		END (*IF*)
	END (*IF*)
END Binary;


BEGIN
	GetList(list);
	GetName(name);
	Binary(list,name,1,LIMIT,foundWord,position);
	IF foundWord THEN
		WriteString("Name found at position ");
		WriteCard(position,1);
		WriteLn;
	ELSE
		WriteString("Name not found");
		WriteLn;
	END
END Search.
