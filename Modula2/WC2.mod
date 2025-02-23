MODULE WC2; 
(*
**From: Making Sense of Modula-2, Tatham and Glendinning
**Purpose: wc program module counts the number of 
**characters, words and lines in a text file. 
**Uses module:  InOut
** Further modifications. Zurk Technology Inc. .Jan96.
*) 
FROM InOut IMPORT OpenInput,CloseInput,Read,WriteLn,WriteString,WriteCard,EOL,Done;
VAR 
	charCount,lineCount,wordCount : CARDINAL;
	prevch : BOOLEAN;

PROCEDURE CountText( VAR charCount,wordCount,
			lineCount:CARDINAL);
(*Pre: None
**Post: No. of characters is returned in CharCount 
**		Number of words is returned in WordCount
**		Number of lines is returned in LineCount*)
VAR 
	ch:  CHAR; 
	wasInLine,wasInWord,nowInLine,nowInWord:BOOLEAN;


	PROCEDURE IsAlpha(Ch:CHAR):BOOLEAN;
	(*Pre: ch can be any character from character set
	**Post:  TRUE if ch is an alphabetic character,
	**otherwise FALSE *)
	BEGIN

		IF ((CAP(ch)>="A") AND (CAP(ch)<="Z")) THEN
			prevch:=FALSE;
			RETURN TRUE
		ELSE
			IF ((ch)="-") AND (NOT prevch) THEN 
				prevch:=TRUE;
				RETURN TRUE 
			ELSE
				RETURN FALSE
			END
		END

	END IsAlpha;


BEGIN  (*CountText*)
	prevch:=FALSE;
	charCount:=0;
	lineCount:=0;
	wordCount:=0;
	wasInWord:=FALSE;
	wasInLine:=FALSE;
	nowInLine:=FALSE;
	(*Open the file*)
	WriteString("Type file name, default extension is 'txt'");
	WriteLn;
	OpenInput("txt"); 
  	IF NOT Done THEN  (*the OpenInput call failed*) 
    	WriteString ("Could not open file - - sorry."); 
   		WriteLn
	ELSE
		Read (ch); 
  		WHILE Done DO
		(*until we run out of characters to read*) 
     		nowInWord := IsAlpha(ch); 
			nowInLine := (ch<>EOL);
 			(*Count words*)
			IF NOT wasInWord AND nowInWord THEN
				wordCount:=wordCount+1
			END; (*IF*)

			(*Count lines*)
			IF NOT wasInLine AND nowInLine THEN
				lineCount:=lineCount+1
			END; (*IF*)
				
			(*Count characters*)
			IF nowInLine THEN
				charCount:=charCount+1
			END; (*IF*)
			wasInWord:=nowInWord;
			wasInLine:=nowInLine;
     		Read(ch) (*Read the next character*) 
   		 END; (*WHILE*)
		CloseInput 
	END (*IF*)
  END CountText; 

PROCEDURE DisplayCount(charCount,wordCount,lineCount:CARDINAL);
(*Pre: 	Number of characters is in charCount 
**		Number of words is in wordCount
**		Number of lines is in lineCount
**Post: None*)
BEGIN
	WriteLn;
	WriteString("Number of Characters :  ");
	WriteCard(charCount,1); 
	WriteLn;
	WriteString("Number of Words :  ");
	WriteCard(wordCount,1);
	WriteLn;
	WriteString("Number of Lines :  ");
	WriteCard(lineCount,1);
	WriteLn
END DisplayCount;


BEGIN (*Main wc program*)
 	CountText(charCount,wordCount,lineCount);
 	DisplayCount(charCount, wordCount,lineCount)
END WC2. 
