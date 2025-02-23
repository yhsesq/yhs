IMPLEMENTATION MODULE Lights;

FROM InOut IMPORT WriteString, WriteLn;
FROM Storage IMPORT ALLOCATE, DEALLOCATE;

PROCEDURE Create(VAR tl:TrafficLight);
BEGIN
	ALLOCATE(tl,SIZE(LightType));
END Create;

PROCEDURE Dispose(VAR tl:TrafficLight);
BEGIN
	DEALLOCATE(tl,SIZE(LightType));
	tl := NIL
END Dispose;

PROCEDURE Stop(VAR tl:TrafficLight);
BEGIN
	IF tl <> NIL THEN
		tl^.red:=on;
		tl^.amber:=off;
		tl^.green:=off
	END (*IF*)
END Stop;

PROCEDURE PreStop(VAR tl:TrafficLight);
BEGIN
	IF tl <> NIL THEN
		tl^.red:=off;
		tl^.amber:=on;
		tl^.green:=off
	END (*IF*)
END PreStop;

PROCEDURE PreGo(VAR tl:TrafficLight);
BEGIN
	IF tl <> NIL THEN
		tl^.red:=on;
		tl^.amber:=on;
		tl^.green:=off
	END (*IF*)
END PreGo;

PROCEDURE Go(VAR tl:TrafficLight);
BEGIN
	IF tl <> NIL THEN
		tl^.red:=off;
		tl^.amber:=off;
		tl^.green:=on
	END (*IF*)
END Go;

PROCEDURE Display(tl:TrafficLight);
BEGIN
	IF tl <> NIL THEN
		IF tl^.red=on THEN
			WriteString("Red is on");
		ELSE
			WriteString("Red is off");
		END; (*IF*)
		WriteLn;
		IF tl^.green=on THEN
			WriteString("Green is on");
		ELSE
			WriteString("Green is off");
		END; (*IF*)
		WriteLn;
		IF tl^.amber=on THEN
			WriteString("Amber is on");
		ELSE
			WriteString("Amber is off");
		END; (*IF*)
		WriteLn;
		WriteLn
	END (*IF*) 
 END Display;

END Lights.
