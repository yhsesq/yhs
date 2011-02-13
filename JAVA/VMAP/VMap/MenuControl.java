package vmap;
import java.*;
import java.awt.*;
import java.applet.*;

public class MenuControl extends Applet
{ 
public static String header="menu";
public static String extension=".gif";
public MenuControl() { } 

public static int checkmouse(int x, int y, int menu)
{

int refresh=99;
refresh=99;

		if (menu == 0)
		{
if (x > 14 && x < 43 && y > 10 && y < 35)  
	{ refresh=1; }
		}

		if (menu == 1)
		{
if (y > 16 && y < 39) {

if (x > 45 && x < 97)
	{ refresh=2; }
if (x > 197 && x < 310)
	{ refresh=3;}
if (x > 411 && x < 513)
	{ refresh=4; }
if (x > 615 && x < 678)
	{ refresh=5; }  }
if (x > 744 && x < 772 && y > 13 && y < 34)
	{ refresh = 0; }
		}

		if (menu == 2) {
if (y > 16 && y < 39) {
if (x > 45 && x < 97)
	{ refresh=2; }
if (x > 197 && x < 310)
	{ refresh=3;}
if (x > 411 && x < 513)
	{ refresh=4; }
if (x > 615 && x < 678)
	{ refresh=5; }  }
if (x > 744 && x < 772 && y > 13 && y < 34)
	{ refresh = 0; }

if (x > 60 && x < 200)
{
if (y > 47 && y < 75)
{
refresh=refresh+1;
// Throw LoadMapSelectionHere
}
if (y > 84 && y < 112)
{
refresh=refresh+2;
// Throw QuitSelectionHere
}}} 
		if (menu == 3) {
if (y > 16 && y < 39) {
if (x > 45 && x < 97)
	{ refresh=2; }
if (x > 197 && x < 310)
	{ refresh=3;}
if (x > 411 && x < 513)
	{ refresh=4; }
if (x > 615 && x < 678)
	{ refresh=5; }  }
if (x > 744 && x < 772 && y > 13 && y < 34)
	{ refresh = 0; }

if (x > 200 && x < 435) {
if (y > 53 && y < 75)
	{ refresh = refresh+3; 
// Throw PhotonicsCentreSelectionHere
	}
if (y > 82 && y < 100)
	{ refresh = refresh+4;
// Throw MorseAuditoriumHere
	}
if (y > 110 && y < 126)
	{ refresh = refresh+5;
// Throw StartPointHere  
	}
if (y > 130 && y < 152)
	{ refresh = refresh+6;
// Throw EndPoint Here 
	}
if (y > 158 && y < 181) 
	{ refresh = refresh+7;
// Throw GPS Coordinates Here
	}
}}

		if (menu == 4) {

if (y > 16 && y < 39) {
if (x > 45 && x < 97)
	{ refresh=2; }
if (x > 197 && x < 310)
	{ refresh=3;}
if (x > 411 && x < 513)
	{ refresh=4; }
if (x > 615 && x < 678)
	{ refresh=5; }  }
if (x > 744 && x < 772 && y > 13 && y < 34)
	{ refresh = 0; }

if (x > 415 && x < 559 && y > 44 && y < 81)
{
refresh=0;
}
				}

		if (menu == 5) {
if (y > 16 && y < 39) {
if (x > 45 && x < 97)
	{ refresh=2; }
if (x > 197 && x < 310)
	{ refresh=3;}
if (x > 411 && x < 513)
	{ refresh=4; }
if (x > 615 && x < 678)
	{ refresh=5; }  }
if (x > 744 && x < 772 && y > 13 && y < 34)
	{ refresh = 0; }

if (x > 617 && x < 735 && y > 47 && y < 82)
{ refresh=refresh+8;
// Throw dialog box here.
}
				}
return refresh;
}

public static String image(int menu)
    {
	String filename;
	filename = header+menu+extension;
	return filename;
	}
}
