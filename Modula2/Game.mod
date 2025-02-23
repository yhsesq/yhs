IMPLEMENTATION MODULE Game;

IMPORT InOut;
IMPORT Board;
IMPORT Player;
IMPORT Die;

PROCEDURE PlayGame(VAR Dice : Die.DieType; VAR Squares : Board.BoardRec; VAR Players : Player.PlayerRec; VAR pno : INTEGER);

VAR sqr : INTEGER;

BEGIN

	CASE Dice OF
   		        	Die.one : sqr:=1; 
       		 	|       Die.two : sqr:=2;
        		|       Die.three : sqr:=3;
        		|       Die.four : sqr:=4;
      			|       Die.five : sqr:=5;
      			|       Die.six : sqr:=6;
	ELSE;
	END; (*CASE*)

	CASE pno OF
			1: 	Squares.position[Player.Player1] := Squares.position[Player.Player1] + sqr;
				IF Squares.position[Player.Player1] <= 24 THEN
				(* Do nothing... *)
			   ELSE
				Squares.position[Player.Player1] := (Squares.position[Player.Player1] - 24);
				Players[Player.Player1].wealth := Players[Player.Player1].wealth + Squares.squares[1].amount;

			   END; (*IF*)

				CASE Squares.squares[Squares.position[Player.Player1]].sqrstat OF 
					  'P' : IF Squares.squares[Squares.position[Player.Player1]].owned=FALSE THEN
						Players[Player.Player1].wealth := Players[Player.Player1].wealth - Squares.squares[Squares.position[Player.Player1]].price;
						Squares.squares[Squares.position[Player.Player1]].owned := TRUE;
						Squares.squares[Squares.position[Player.Player1]].owner := Player.Player1;
						ELSE
						Players[Player.Player1].wealth := Players[Player.Player1].wealth - Squares.squares[Squares.position[Player.Player1]].rent;
						END; (*IF*)
					| 'T' : Players[Player.Player1].wealth := Players[Player.Player1].wealth - Squares.squares[Squares.position[Player.Player1]].amount;
				ELSE; END; (*CASE*) 
		|	2: Squares.position[Player.Player2] := Squares.position[Player.Player2] + sqr;
                                IF Squares.position[Player.Player2] <= 24 THEN
                                (* Do nothing... *)
                           ELSE
                                Squares.position[Player.Player2] := (Squares.position[Player.Player2] - 23);
                                Players[Player.Player2].wealth := Players[Player.Player2].wealth + Squares.squares[1].amount;

                           END; (*IF*)

                                CASE Squares.squares[Squares.position[Player.Player2]].sqrstat OF
                                          'P' : IF Squares.squares[Squares.position[Player.Player2]].owned=FALSE THEN
                                                Players[Player.Player2].wealth := Players[Player.Player2].wealth - Squares.squares[Squares.position[Player.Player2]].price;
                                                Squares.squares[Squares.position[Player.Player2]].owned := TRUE;
                                                Squares.squares[Squares.position[Player.Player2]].owner := Player.Player2;
                                                ELSE
                                                Players[Player.Player2].wealth := Players[Player.Player2].wealth - Squares.squares[Squares.position[Player.Player2]].rent;
                                                END; (*IF*)
                                        | 'T' : Players[Player.Player2].wealth := Players[Player.Player2].wealth - Squares.squares[Squares.position[Player.Player2]].amount;
                                ELSE; END; (*CASE*)
	ELSE;
	END; (*CASE*)	

END PlayGame;

END Game.
