IMPLEMENTATION MODULE Board;

IMPORT Square;
IMPORT Player;
FROM InOut IMPORT WriteString, WriteLn, OpenInput, CloseInput, ReadString, ReadInt, WriteInt, WriteCard;

PROCEDURE Read(VAR Squares : BoardRec);

VAR
temparray : ARRAY [1..100] OF CHAR;
counter : CARDINAL;

BEGIN
OpenInput("dat");
FOR counter := 1 TO 24 DO
ReadString(temparray);
Squares.squares[counter].sqrstat:=temparray[1];
Squares.squares[counter].sqrtype:=temparray[1];
	CASE temparray[1] OF
			'P' : 	ReadString(Squares.squares[counter].name);
				ReadInt(Squares.squares[counter].price);
				ReadInt(Squares.squares[counter].rent);
				Squares.squares[counter].owned:=FALSE;
		|	'W' : 	ReadString(Squares.squares[counter].name);
				ReadInt(Squares.squares[counter].amount);
		|	'T' : 	ReadString(Squares.squares[counter].name);
				ReadInt(Squares.squares[counter].amount);
		|	'X' : 	ReadString(Squares.squares[counter].name);
		END (*CASE*)
END; (*FOR*)
CloseInput();
Squares.position[Player.Player1] := 1;
Squares.position[Player.Player2] := 1;
WriteLn;
END Read;

PROCEDURE Print(VAR Squares : BoardRec);

VAR
counter : CARDINAL;

BEGIN

FOR counter := 1 TO 24 DO

	WriteCard(counter,5); WriteString("	"); WriteString(Squares.squares[counter].name);WriteString("	");
	
	CASE Squares.squares[counter].sqrtype OF
			'P' : WriteString("	Price = ");WriteInt(Squares.squares[counter].price,5);
			      WriteString("	Rent = ");WriteInt(Squares.squares[counter].rent,5);
			      	CASE Squares.squares[counter].owned OF
					TRUE : CASE Squares.squares[counter].owner OF
							Player.Player1: WriteString ("		Owned by Player_1"); WriteLn;
						ELSE WriteString("		Owned by Player_2");WriteLn;END; (*CASE*)
					ELSE WriteString("		Not owned ");WriteLn;
					END (*CASE*)
			| 'T','W' : WriteString("	Tax Square / Rebate Square = ");WriteInt(Squares.squares[counter].amount,5);WriteLn;
			ELSE  WriteString("	Museum / Public Place / Free drop ");WriteLn;
			END (*CASE*)

END;(*FOR*)

WriteLn;
WriteString("Player 1 position : ");
WriteCard(Squares.position[Player.Player1],5);
WriteLn;
WriteString("Player 2 position : ");
WriteCard(Squares.position[Player.Player2],5);
WriteLn;
	
END Print;

END Board.
