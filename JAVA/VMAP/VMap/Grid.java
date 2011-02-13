package vmap;

public class Grid 
{ 
public Grid() { } 

// Command and control.
// Decision making.
// Control logic.
// Data stores.

int start=0, end=0, hour, metaix=0, metaiy=0, metaiix=0, metaiiy=0;

public void clearall()
{

start=0; end=0; metaix=0; metaiy=0; metaiix=0; metaiiy=0; }


public void startpt(int data)
    {
	start=data;
	}
public void endpt(int data)
    {
	end=data;
	}

public void setstart(int x, int y)
{
metaix=x;
metaiy=y;
}

public void setend(int x, int y)
{
metaiix=x;
metaiiy=y;
}

public int retx(int type)
{
if (type == 1) {return metaiix;}
else {return metaix;}
}
public int rety(int type)
{
if (type == 1) {return metaiiy;}
else {return metaiy;}
}

public int currentdata()
    {
// 1 = endpt
// 0 = startpt
if (start==0)
{return start;}
else {return 1;}
	}

public int gonogo()
{
if (start == 0 || end == 0)
{return 1;}
else
{return 0;}
}

public void setlight(int light)
{
hour=light;
}

}
