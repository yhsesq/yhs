IMPLEMENTATION MODULE StringStuff;

	PROCEDURE StringLen(s:ARRAY OF CHAR):CARDINAL;
	CONST
		NULL=0C;
	VAR
		index:CARDINAL;
	BEGIN
		index:=0;
		WHILE (index<=HIGH(s)) AND (s[index]<>NULL) DO
			INC(index)
		END;  (*WHILE*)
		RETURN index
	END StringLen;

PROCEDURE StringCopy(	orig:ARRAY OF CHAR; 
			VAR copy:ARRAY OF CHAR;
			VAR success:BOOLEAN);
CONST
	NULL=0C;
VAR
	index:CARDINAL;
BEGIN
	index:=0;
	IF StringLen(copy)<StringLen(orig) THEN 
		success:= FALSE
	ELSE
		WHILE (index<StringLen(orig)) DO
			copy[index]:=orig[index];
			INC(index);
		END;  (*WHILE*)
		copy[index]:=NULL;
		success:=TRUE
	END (*IF*)
END StringCopy;


PROCEDURE StringCompare(s1,s2:ARRAY OF CHAR):Result;
CONST
	NULL=0C;
VAR
	ch1,ch2:CHAR;
	index:CARDINAL;
BEGIN
	index:=0;
	LOOP
		IF index>HIGH(s1) THEN
			ch1:=NULL
		ELSE
			ch1:=s1[index]
		END;
		IF index>HIGH(s2) THEN
			ch2:=NULL			
		ELSE
			ch2:=s2[index]
		END;
		IF ch1<ch2 THEN
			RETURN less
		ELSIF ch1>ch2 THEN
			RETURN greater
		ELSIF (ch1=ch2) AND (ch1=NULL) THEN
			RETURN same
		END;
		INC(index)
	END (*LOOP*)	
END StringCompare;

END StringStuff.
