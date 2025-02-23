MODULE Traffic;

(* Traffic Lights  ------- Tutorial ExcerciseXII*)
(* Uses Traffic lights to control traffic       *)
(* Compiler : Gardens Point / Modula 2 - gpm    *)
 

FROM InOut IMPORT WriteString, WriteLn;
IMPORT Lights;

VAR
light : Lights.TrafficLight;


(* Main Program Begin... *)
BEGIN
	WriteLn;
	WriteString("Traffic light program...");
	WriteLn;WriteLn;
	WriteString(" GO ...");
	WriteLn;WriteLn;
	Lights.Create(light);
	Lights.Go(light);
	Lights.Display(light);
	Lights.Dispose(light);
        WriteLn;WriteLn;
        WriteString(" PREPARE TO STOP ...");
        WriteLn;WriteLn;
        Lights.Create(light);
        Lights.PreStop(light);
        Lights.Display(light);
        Lights.Dispose(light);
        WriteLn;WriteLn;
        WriteString(" STOP ...");
        WriteLn;WriteLn;
        Lights.Create(light);
        Lights.Stop(light);
        Lights.Display(light);
        Lights.Dispose(light);
        WriteLn;WriteLn;
        WriteString(" PREPARE TO GO ...");
        WriteLn;WriteLn;
        Lights.Create(light);
        Lights.PreGo(light);
        Lights.Display(light);
        Lights.Dispose(light);
        WriteLn;WriteLn;
        WriteString(" GO ...");
        WriteLn;WriteLn;
        Lights.Create(light);
        Lights.Go(light);
        Lights.Display(light);
        Lights.Dispose(light);

END Traffic.

 
