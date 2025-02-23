IMPLEMENTATION MODULE Stack;
(* operates a stack; a Last-In-First-Out data structure*)
FROM InOut IMPORT Write, WriteLn;
FROM Storage IMPORT ALLOCATE, DEALLOCATE;
TYPE      
	StackType = POINTER TO StackRecord;
	StackRecord  =	RECORD
          			stackChar : CHAR;
          			next : StackType
     			END; (*StackRecord*)

PROCEDURE CreateStack (VAR top :StackType);
BEGIN
	top := NIL
END CreateStack;

PROCEDURE IsEmpty (top : StackType): BOOLEAN;
BEGIN
	IF top=NIL THEN
		RETURN TRUE
	ELSE
		RETURN FALSE
	END
END IsEmpty;

PROCEDURE Push (VAR top :StackType; inChar : CHAR);
(* add an item to the top of the stack *)
VAR temp: StackType;
BEGIN
	ALLOCATE(temp, SIZE(StackRecord));
	WITH temp^ DO
		stackChar:=inChar;
		next:=top
	END; (*WITH*)
	top:= temp
END Push;

PROCEDURE Pop (VAR top : StackType; VAR outChar : CHAR);
(* remove an Item from the top of the stack*)
VAR temp : StackType;
BEGIN
	IF NOT IsEmpty(top) THEN
		temp := top;
		WITH temp^ DO
			outChar:= stackChar;
			top := next
		END; (*WITH*)
		DEALLOCATE(temp,SIZE(StackRecord))
	END (*IF*)
END Pop;

PROCEDURE DisposeStack(VAR top : StackType);
(*remove all items from the stack*)
VAR outChar : CHAR;
BEGIN
	WHILE (NOT IsEmpty(top)) DO
		Pop(top,outChar)
	END (*WHILE*)
END DisposeStack;

PROCEDURE DisplayStack(top : StackType);
VAR
	outChar : CHAR;
BEGIN
	IF NOT IsEmpty(top) THEN
		Pop(top,outChar);
		Write(outChar);
		WriteLn;
		DisplayStack(top^.next)
	END (*IF*)
END DisplayStack;

END Stack.
