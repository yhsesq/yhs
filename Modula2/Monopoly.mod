MODULE Monopoly;

(* Monopoly Game  ------- Course Work           *)
(* Reads the data file Monopoly.dat             *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn, ReadCard, WriteCard, ReadInt, WriteInt, ReadString, Read, OpenInput, CloseInput, Done;
IMPORT Die;				(* Library for Dice control routines   *)
IMPORT Square;				(* Library with structure of squares   *)
IMPORT Board;				(* Library with Board control routines *)
IMPORT Player;				(* Handles Player info & sets up Player*)
IMPORT Game;				(* Game calculation routines & setups  *)

VAR

Dice : Die.DieType;
Squares : Board.BoardRec;
Players : Player.PlayerRec;

PROCEDURE PlayMonopoly(VAR Dice : Die.DieType; VAR Squares : Board.BoardRec; VAR Players : Player.PlayerRec);(* Procedure for playing *)

VAR

pno : INTEGER;			(* Temp variables for User I/O *)
cnt : ARRAY [0..100] OF CHAR;   (* & Player controls           *)

BEGIN

	REPEAT					(* PlayGame Loop *)
	WriteLn;
	WriteString(" Press r <ENTER> to roll or q <ENTER> to roll & quit : ");
	WriteLn;

	cnt[0]:=' ';
	WHILE ((cnt[0]<>'q') AND (cnt[0]<>'r')) DO   (* User Input *)

		cnt[0]:=' ';
		ReadString(cnt);

	END; (*WHILE*)

	WriteString(" Player 1 ...."); 		(* Run game for Player 1 *)

	Die.InitialiseDie(Dice);
	Die.ThrowDie(Dice);
	Die.ShowDie(Dice);
	pno:=1;					(* Temporary variable *)
	Game.PlayGame(Dice,Squares,Players,pno);

	WriteString(" Player 2 ....");		(* Run game for Player 2 *)
	Die.InitialiseDie(Dice);
	Die.ThrowDie(Dice);
	Die.ShowDie(Dice);
	pno:=2;					(* Temporary variable *)
	Game.PlayGame(Dice,Squares,Players,pno);

	WriteString(" Current Status of game : "); (* Game Status *)
	WriteLn;
	Board.Print(Squares);			   (* Call routine for game *)
	Player.PrintPlayer(Players); 		   (* and to print players *)

	UNTIL (cnt[0] = 'q');			   (* REPEAT Until.... *)

END PlayMonopoly;
 
(* Main Program Begin... *)
BEGIN

        WriteLn;
        WriteString(" Monopoly Board Game - MODULA/2 CourseWork");
        WriteLn;
        Players[Player.Player1].wealth:=1000; (* Starting Wealth *)
        Players[Player.Player2].wealth:=1000; (* for both players*)
        Board.Read(Squares);                  (* Read Squares *)
        PlayMonopoly(Dice,Squares,Players);      (* Play the game *)

END Monopoly.                                 (* EndGame *)

