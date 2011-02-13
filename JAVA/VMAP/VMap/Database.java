package vmap;

import java.io.*; 
import java.util.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.applet.*;
import java.text.*; 
import java.lang.*; 
import java.net.*;

public class Database extends Applet
{ 
public Database() { } 

// Data stores.
// Matrix.DB
// Shortest path.
String errormsg=new String();
URL url=null;
String zero="0";
String etc=new String();
int temp;int tempx=0;int tempy=0;int lastx=0,lasty=0;
int a=0;int b=0;int c=0;int d=0;
int mainx=0;int mainy=0,curant=0;
int lock;int maxrun=0;
// Database plugin.
int maxrecords;
int stat[]=new int[100];
int flagn[]=new int[100];
int flags[]=new int[100];
int flage[]=new int[100];
int flagw[]=new int[100];
int flagl[]=new int[100];
int dist[]=new int[100];
int lnkstart[]=new int[100];
int lnkend[]=new int[100];
int matx[]=new int[100];
int maty[]=new int[100];
int ntext[][]=new int[100][61]; //forDigit for conversion. 
int stext[][]=new int[100][61];
int etext[][]=new int[100][61];
int wtext[][]=new int[100][61];
int ltext[][]=new int[100][61];
int ant[]=new int[100];
int antttl[]=new int[100];
// end db plugin
//Ant races plugin
int grid[][]=new int[100][100];
int gridant[][]=new int[100][100];
int gridantttl[][]=new int[100][100];
int shortpath[][]=new int[100][100];
// end ant races plugin

public int loaddb(URL urlx) { if (lock != 1){ try{
// I suppose i shouldnt hardcode it but use getCodeBase() instead... :) -Ys- 8.4.99
// done! -Ys- 1.5.99
URL url = new URL(urlx,"matrix");
BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream() ) );
temp=in.read()-48;
temp=temp*10+in.read()-48;
temp=temp*10+in.read()-48;
if(temp > 100){temp=100;}
// Start loading the database
maxrecords=temp;
for (a=0;a<maxrecords;a++){
ant[a]=0;
stat[a]=in.read()-48;
flagn[a]=in.read()-48;
flags[a]=in.read()-48;
flage[a]=in.read()-48;
flagw[a]=in.read()-48;
flagl[a]=in.read()-48;
dist[a]=in.read()-47; // distance always >= 1.
antttl[a]=dist[a];
temp=in.read()-48;
temp=temp*10+in.read()-48;
temp=temp*10+in.read()-48;
lnkstart[a]=temp; temp=0;
temp=in.read()-48;
temp=temp*10+in.read()-48;
temp=temp*10+in.read()-48;
lnkend[a]=temp;
matx[a]=in.read()-48;
maty[a]=in.read()-48;
for (b=0;b<60;b++){
ntext[a][b]=in.read();}
for (b=0;b<60;b++){
stext[a][b]=in.read();}
for (b=0;b<60;b++){
etext[a][b]=in.read();}
for (b=0;b<60;b++){
wtext[a][b]=in.read();}
for (b=0;b<60;b++){
ltext[a][b]=in.read();}
} // End loading the database
} catch(MalformedURLException e){
System.out.println("URLException:"+e);
} catch(IOException e){
System.out.println("IOException:"+e);}
lock=1;
return maxrecords;}
else
{return maxrecords;}
}


public String error()
{return errormsg;}

public int getmainx()
{return mainx;}

public int getmainy()
{return mainy;}

public void LockCoords(int street)
{
mainx=0;
mainy=0;
for (a=0;a<100;a++)
{if ((lnkstart[a] <= street) && (lnkend[a] >= street)) 
{ mainx=matx[a];mainy=maty[a];  }}
}

public int AntRaces(int startx, int starty,int endx,int endy)
{
// Clear everything.
for (a=0;a<100;a++)
{for (b=0;b<100;b++){
grid[a][b]=0; // grid [a] [b] = 0 means it is blocked.
gridant[a][b]=0;
gridantttl[a][b]=99; // max value for min shortest path fill.
shortpath[a][b]=0;
}} gridantttl[startx+1][starty+1]=0;
// Load in matrix.
for (a=0;a<maxrecords;a++)
{if (stat[a] != 0) {grid[matx[a]+1][maty[a]+1]=dist[a];}}
// Lock in co-ordinates.
gridant[startx+1][starty+1]=1;
// Ready...Ant Races init()
while (gridant[endx+1][endy+1] != 1)
{

for (a=0;a<100;a++){
for (b=0;b<100;b++){

if(gridant[a][b] != 99 && gridant[a][b] != 0){ 

if (gridant[a][b] == 1){ // Ready to move
// Block of 8 squares around it. Use only NSEW.
//		b-1		b		b+1
//
//  a-1		(a+1,b-1)	(a+1,b)		(a+1,b+1)	
//
//   a		(a,b-1)		(a,b)		(a,b+1)
//
//  a+1		(a-1,b-1)	(a-1,b)		(a-1,b+1)
// 
// Now has bound protection for edge limits, removed manual db block limits - -Ys- 27.4.99
// Removed NS,NE,SW,SE links due to bounded matrix node blocks. - -Ys- 14.4.99 
// Performance testing yield approx 40ms full grid P5.200 class. - -Ys- 12.4.99
// Translational problem for reversed, opposing directions removed. -Ys- 8.4.99
// if(grid[a-1][b-1] != 0 && gridant[a-1][b-1] == 0)
//	{  gridant[a-1][b-1]=grid[a][b]; gridantttl[a-1][b-1]=grid[a][b]+gridantttl[a][b];  }

if(grid[a-1][b] != 0 && gridant[a-1][b] == 0)
	{  gridant[a-1][b]=grid[a][b]; gridantttl[a-1][b]=grid[a][b]+gridantttl[a][b];  }

if(grid[a][b-1] != 0 && gridant[a][b-1] == 0)
	{  gridant[a][b-1]=grid[a][b]; gridantttl[a][b-1]=grid[a][b]+gridantttl[a][b];  }

// if(grid[a+1][b+1] != 0 && gridant[a+1][b+1] == 0)
//	{  gridant[a+1][b+1]=grid[a][b]; gridantttl[a+1][b+1]=grid[a][b]+gridantttl[a][b];  }

if(grid[a+1][b] != 0 && gridant[a+1][b] == 0)
	{  gridant[a+1][b]=grid[a][b]; gridantttl[a+1][b]=grid[a][b]+gridantttl[a][b];  }

if(grid[a][b+1] != 0 && gridant[a][b+1] == 0)
	{  gridant[a][b+1]=grid[a][b]; gridantttl[a][b+1]=grid[a][b]+gridantttl[a][b];  }

// if(grid[a-1][b+1] != 0 && gridant[a-1][b+1] == 0)
//	{  gridant[a-1][b+1]=grid[a][b]; gridantttl[a-1][b+1]=grid[a][b]+gridantttl[a][b];  }
// if(grid[a+1][b-1] != 0 && gridant[a+1][b-1] == 0)
//	{  gridant[a+1][b-1]=grid[a][b]; gridantttl[a+1][b-1]=grid[a][b]+gridantttl[a][b];  }
// End block.
gridant[a][b]=99; // dies.
} else if (gridant[a][b] > 1) {
gridant[a][b]=gridant[a][b]-1; // walking the time varying path. chewing the cud. et al.
}

}
}}
}
// Trace back the shortest path..
a=endx+1;b=endy+1; // 1 - North, 2 - South, 3 - East, 4 - West, Always put the opposite due to swap later.
c=0;d=0;tempx=endx+1;tempy=endy+1;temp=99;
while(gridantttl[a][b] != 0)
{
shortpath[c][0]=a-1;
shortpath[c][1]=b-1;
shortpath[c][2]=d;
c=c+1;temp=99;
// if (gridantttl[a+1][b+1] < temp) {tempx=a+1;tempy=b+1; temp=gridantttl[a+1][b+1];}
if (gridantttl[a+1][b] < temp) {tempx=a+1;tempy=b;temp=gridantttl[a+1][b];d=1;}
// if (gridantttl[a+1][b-1] < temp) {tempx=a+1;tempy=b-1;temp=gridantttl[a+1][b-1];}
if (gridantttl[a][b+1] < temp) {tempx=a;tempy=b+1;temp=gridantttl[a][b+1];d=4;}
// if (gridantttl[a-1][b-1] < temp) {tempx=a-1;tempy=b-1;temp=gridantttl[a-1][b-1];}
if (gridantttl[a-1][b] < temp) {tempx=a-1;tempy=b;temp=gridantttl[a-1][b];d=2;}
if (gridantttl[a][b-1] < temp) {tempx=a;tempy=b-1;temp=gridantttl[a][b-1];d=3;}
// if (gridantttl[a-1][b+1] < temp) {tempx=a-1;tempy=b+1;temp=gridantttl[a-1][b+1];}
a=tempx;b=tempy;
}

shortpath[c][0]=startx;
shortpath[c][1]=starty;

for (a=0;a<((c+1)/2);a++)
{tempx=shortpath[a][0];
tempy=shortpath[a][1];
d=shortpath[a][2];
shortpath[a][0]=shortpath[c-a][0];
shortpath[a][1]=shortpath[c-a][1];
shortpath[a][2]=shortpath[c-a][2];
shortpath[c-a][0]=tempx;
shortpath[c-a][2]=d;
shortpath[c-a][1]=tempy;}
// Simple work around for unknown direction. 
if (shortpath[0][0] > shortpath[1][0]){shortpath[0][2]=1;}
if (shortpath[0][1] > shortpath[1][1]){shortpath[0][2]=4;}
if (shortpath[0][0] < shortpath[1][0]){shortpath[0][2]=2;}
if (shortpath[0][1] < shortpath[1][1]){shortpath[0][2]=3;}
maxrun=0;
while(shortpath[maxrun][2] != 0)
{maxrun=maxrun+1;}
shortpath[maxrun][2]=shortpath[maxrun-1][2];
maxrun=maxrun+1;curant=0;
return maxrun;
}

public String Sequencer(int current)
{
// 1 - North, 2 - South, 3 - East, 4 - West

etc="NULLETC"; errormsg="NULLERRORMSG";

// temp=((shortpath[curant][0]*10)+shortpath[curant][1]);
// the above does not handles 0 cases. deleted. -Ys- 28.4.99

if (shortpath[current][0]==0){etc=zero;}
else {etc=""+shortpath[current][0];}
if (current < maxrun){lastx=shortpath[current][0];}
if (shortpath[current][1]==0){etc=etc+zero;}
else {etc=etc+shortpath[current][1];}
if (current < maxrun){lasty=shortpath[current][1];}
if (shortpath[current][2]==1)
{errormsg=etc+"n"+".jpg";}
else if (shortpath[current][2]==2)
{errormsg=etc+"s"+".jpg";}
else if (shortpath[current][2]==3)
{errormsg=etc+"e"+".jpg";}
else if (shortpath[current][2]==4)
{errormsg=etc+"w"+".jpg";}
else {errormsg=etc+"l"+".jpg";}
return errormsg;
}

public String SequencerL()
{
// 1 - North, 2 - South, 3 - East, 4 - West

etc="NULLETC"; errormsg="NULLERRORMSG";

if (lastx==0){etc=zero;}
else {etc=""+lastx;}

if (lasty==0){etc=etc+zero;}
else {etc=etc+lasty;}

errormsg=etc+"l"+".jpg";
return errormsg;
}

public String SequencerC(int current)
{
// 1 - North, 2 - South, 3 - East, 4 - West
// North to North, S to S etc = straight ahead.
// North to East = e, S to W, E to S, W to N
etc="NULLETC"; errormsg="NULLERRORMSG";

current=current-1;

if (shortpath[current][2]==shortpath[current+1][2])
{errormsg="n"+".gif";}
else if ((shortpath[current][2]==1 && shortpath[current+1][2]==3)||(shortpath[current][2]==2 &&
shortpath[current+1][2]==4)||(shortpath[current][2]==3 && shortpath[current+1][2]==2)||(shortpath[current][2]==4 &&
shortpath[current+1][2]==1)) {errormsg="e"+".gif";}
else if (shortpath[current+1][2] != 0){errormsg="w"+".gif";}
else {errormsg="compass"+".gif";}
return errormsg;
}

public String SequencerX(int current)
{
// 1 - North, 2 - South, 3 - East, 4 - West
// North to North, S to S etc = straight ahead.
// North to East = e, S to W, E to S, W to N
etc="NULLETC"; errormsg="NULLERRORMSG";

current=current-1;

if (shortpath[current][2]==shortpath[current+1][2])
{errormsg="Keep going straight ahead to the next intersection..";}
else if ((shortpath[current][2]==1 && shortpath[current+1][2]==3)||(shortpath[current][2]==2 &&
shortpath[current+1][2]==4)||(shortpath[current][2]==3 && shortpath[current+1][2]==2)||(shortpath[current][2]==4 &&
shortpath[current+1][2]==1)) {errormsg="Turn right at the intersection of the road..";}
else if (shortpath[current+1][2] != 0){errormsg="Turn left at the intersection of the road..";}
else {errormsg="Destination reached. Now loading map..";}
return errormsg;
}

public void DumpAntRaces(){
for (a=0;a<10;a++)
{ for (b=0;b<10;b++){
System.out.print(" "+gridantttl[a][b]);
}System.out.println("");}
System.out.println("----------------------");
for (a=0;a<maxrecords;a++)
{System.out.println(shortpath[a][0]+","+shortpath[a][1]+">"+shortpath[a][2]);}
System.out.println("MAXRUN is "+maxrun);
}

public void DumpData()
{
// Debugging purposes ONLY
for (a=0;a<maxrecords;a++){
System.out.println("-------------------------------");
System.out.println("Record number = "+a);
if (stat[a]==0){System.out.println("Blocked");}else{System.out.println("Linked");}
if(flagn[a] != 0){System.out.println("North picture active");}
if(flags[a] != 0){System.out.println("South picture active");}
if(flage[a] != 0){System.out.println("East picture active");}
if(flagw[a] != 0){System.out.println("West picture active");}
if(flagl[a] != 0){System.out.println("Landmark picture active");}
System.out.println("Distance flag = "+dist[a]);
System.out.println("Block covers : "+lnkstart[a]+" - "+lnkend[a]);
System.out.println("Matrix coordinates are : X="+matx[a]+", Y="+maty[a]);
System.out.println("Ant visited status is :"+ant[a]+" TTL Field is "+antttl[a]);
}}

}

