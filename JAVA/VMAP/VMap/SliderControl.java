package vmap;
import java.*;
import java.awt.*;
import java.applet.*;

public class SliderControl extends Applet
{ 
public SliderControl() { } 

public static int slidemouse(int x, int y)
{

// Returns a slider offset based on the position of the mouse.

int refresh=999;
refresh=999;

if (x > 123 && y > 642 && x < 635  && y < 662)
	{ refresh=(int) (603+((x-123)*(0.250))); }
if (x > 123 && y >671 && x < 635 && y < 687)
	{ refresh=(int) (1+((x-123)*(0.218))); }
if (x > 756 && x < 878)
{
if (y > 647 && y < 666)
{
refresh=200;
}
if (y >671 && y < 689)
{
refresh=300;
}
}

if (refresh==999)
{refresh=0;}
return refresh;
}


public static int slid(int scaling)
{

// Returns a scale factor.

if (scaling > 600)
{
scaling = (int) (scaling-602);
scaling= (int) (scaling/12.8);
}
else
{
scaling = (int) (scaling/10);
}

return scaling;
}

}
