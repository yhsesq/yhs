MODULE RotArray;

(* Array Tester  -------- Tutorial Excercise IX *)
(* Tests a function that rotates an Array 180d  *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn, Read, Write, WriteCard;

CONST
	ASIZE=5;LOOPX=ASIZE/2;
TYPE
	ArrayX = ARRAY [1..ASIZE],[1..ASIZE] OF CHAR;
VAR
	arrayx : ArrayX;


PROCEDURE Reada(VAR arrayx : ArrayX);

VAR
	row,col : CARDINAL;

BEGIN

	WriteString("Enter your array of 25 Characters below...");
	WriteLn;
        WriteString("Press ENTER when done :");

	FOR row:=1 TO ASIZE DO

		FOR col:=1 TO ASIZE DO

			Read(arrayx[row,col]);

		END;
	END;

END Reada;

PROCEDURE Display(arrayx : ArrayX);

VAR
	row,col : CARDINAL;

BEGIN
	WriteLn;WriteString("The Array of characters is -");WriteLn;
	WriteLn;

	FOR row:=1 TO ASIZE DO

		FOR col:=1 TO ASIZE DO

			WriteString("  "); 
			Write(arrayx[row,col]);

		END;

	WriteLn;

	END;

	WriteLn;

END Display;

PROCEDURE RotateArray(VAR arrayx : ArrayX);

VAR
	row,col,srow,scol : CARDINAL;
	tempx,tempy : CHAR;
	
BEGIN

	FOR row:=1 TO LOOPX DO

		FOR col:=1 TO ASIZE DO

			tempx:=arrayx[row,col];
			srow:=ASIZE+1-row;scol:=ASIZE+1-col;
			tempy:=arrayx[srow,col];
			arrayx[row,col]:=tempy;
			arrayx[srow,col]:=tempx;
		END;
	END;

	FOR row:=1 TO ASIZE DO

		FOR col:=1 TO LOOPX DO

			tempx:=arrayx[row,col];
			srow:=ASIZE+1-row;scol:=ASIZE+1-col;
			tempy:=arrayx[row,scol];
			arrayx[row,col]:=tempy;
			arrayx[row,scol]:=tempx;

		END;

	END;

WriteLn;
WriteString("Rotation Completed....");
WriteLn;

END RotateArray;

(* Main Program Begin... *)
BEGIN

	Reada(arrayx);
	Display(arrayx);
	RotateArray(arrayx);	
	Display(arrayx);
	
END RotArray.

(*
Discussion & Limitations -
1] This program was designed under the specifications given in
   the book and tutorial notes.
2] The book has listed a program specification for a square 5*5
   array. Hence this program implements a 180 degree turn for 
   a Square array ONLY. However, similar to the book, the array
   size can be varied by varying the ASIZE variable.
3] The program wastes approx. 8 bits of memory due to the addition
   of two temp CHAR variables instead of only one. However, due to 
   the requirement for clarity and simplicity in the program,
   the two temp variables were used.
*)
 
