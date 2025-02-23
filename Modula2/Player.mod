IMPLEMENTATION MODULE Player;

FROM InOut IMPORT WriteString, WriteLn, WriteCard, WriteInt;

PROCEDURE PrintPlayer(VAR Players : PlayerRec);

BEGIN

	WriteLn;
	WriteString("Player 1 : Wealth : ");
	WriteInt(Players[Player1].wealth,5);
	WriteLn;
	WriteString("Player 2 : Wealth : ");
	WriteInt(Players[Player2].wealth,5);
	WriteLn;

END PrintPlayer;

END Player.
