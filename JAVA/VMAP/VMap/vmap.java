package vmap;
/* VMap/J v.1.2.5.c
 * Video Mapping System for Java 1.1.7+
 * This file is released under the GNU GPL (www.gnu.org) v2.0 or later
 */ 

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.text.*;
import java.lang.*;
import java.net.*;
import vmap.Grid;
import vmap.MenuControl;
import vmap.SliderControl;
import vmap.SFX;
import vmap.RTC;
import vmap.Database;

public class vmap extends SFX implements MouseListener, KeyListener, Runnable {
    Thread timer = null; int about=0; int DEBUG=0; // DEBUG=1 will implement debugging mode, 0 switches off.
    int CPU_INFO=0; // Give a current CPU_INFO setting.
    RTC rtc = new RTC(0);
    MenuControl menuhandler;
    SliderControl slidehandler;
    Grid gridg=new Grid();
    Database ants=new Database();
    URL url=null;
    Image myImage, myImagec, myImageb, myImagebt,myImagesl;
	// Preloading and caching images results in a significantly faster response.
    Image myImagem0, myImagem1, myImagem2, myImagem3, myImagem4, myImagem5;
	// This technique is not very elegant but does work more effectively than the alternatives.
    String starttext;
    String endtext;
    int runonce=0, blocking = 0, code=5, flag=0, seconds = 0, exception=0, lighting=1, maxrun=0;
    Date dummy = new Date();
    GregorianCalendar cal = new GregorianCalendar();
    SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    String lastdate = df.format(dummy);
    Font F = new Font("MonoSpaced", Font.BOLD, 14);
    Font Fx = new Font("Helvetica", Font.BOLD, 18); 
    Date dat = null;
    Color cblack = new Color(0, 0, 0); //set color to Black
    Color fgcol = cblack;
    Color fgcol2 = cblack;
    String computertext = "VMap/J v1.0 now loading .... Please Wait .... ";
    String usertext = ""; String errormsg="";String blankstring=new String();
    String commandtext = "  ";int flip=0;
    int state=0, mousex=0, mousey=0, comflash=0, updatemenu=0, menu=0, slider=0, updateslider=0; 
    int maprecords=0;int mapcalc=0;int backupmaxrun=0;

public void init() {
        int x,y;
        try {
            setBackground(new Color(Integer.parseInt(getParameter("bgcolor"),16)));
        } catch (Exception E) { }
        try {
            fgcol = new Color(Integer.parseInt(getParameter("fgcolor1"),0));
        } catch (Exception E) { }
        try {
            fgcol2 = new Color(Integer.parseInt(getParameter("fgcolor2"),0));
        } catch (Exception E) { }
        myImage=getImage(getCodeBase(),"vmap.jpg");
        myImagec=getImage(getCodeBase(),"commandbar.gif");
        myImageb=getImage(getCodeBase(),"compass.gif");
        myImagebt=getImage(getCodeBase(),"compasa.gif");
	myImagesl=getImage(getCodeBase(),"slider.gif");
	// Not very elegant as noted earlier. 
        myImagem0=getImage(getCodeBase(),"menu0.gif");
	myImagem1=getImage(getCodeBase(),"menu1.gif");
        myImagem2=getImage(getCodeBase(),"menu2.gif");
	myImagem3=getImage(getCodeBase(),"menu3.gif");
        myImagem4=getImage(getCodeBase(),"menu4.gif");
	myImagem5=getImage(getCodeBase(),"menu5.gif");
        // Initial codebase. Prevents unauthourised d/ls.

        // Screen specs :
        // Main : 900 X 642
        // Compass : 182 X 177
        // Command bar 900 X 51
        resize(900,693);              // Set window size
        this.addMouseListener(this);
        this.addKeyListener(this);
    }

    // implements mouse + keyboard calls.

public void mouseReleased(MouseEvent e) {
        mousex=e.getX();
        mousey=e.getY();  }
public void keyTyped(KeyEvent e) {
        usertext = usertext + e.getKeyChar(); }
public void mouseClicked(MouseEvent e)  { }
public void mousePressed(MouseEvent e) { }
public void mouseEntered(MouseEvent e)  { }
public void mouseExited(MouseEvent e)   { }
public void keyPressed(KeyEvent e)      { }
public void keyReleased(KeyEvent e)     { }

    // Sets the state based on a time varying feature. Also uses exceptions/events.

public void state()
    {
	int temp=99;
	int junk=0;
	if (state != 0)
	{
	if (updatemenu > 0)
	{  // Forced update.
	// Returns string of image..deleted. menuhandler.image(menu);
	updatemenu=0; }

	temp=menuhandler.checkmouse(mousex, mousey, menu);
	if (temp != 99)
	{ updatemenu=1;
	if (temp < 99)
	{  menu=temp; }
	if (temp > 99)
	{  menu=1;	
	exception=temp-99; }
	  temp=99; }
	}

        if (state == 0) {
            if (seconds > 10 && flag == 0 && state == 0)
            {
                flag = 1;
                computertext="Click the OK button to continue..";
                commandtext="OK";
                state = state + 1;
                mousex=0;
                mousey=0;
                comflash=1;
		seconds=rtc.retval();
		CPU_INFO=seconds;
	    if (lighting > 0) {lighting=lighting-1; myImage=Lightning(myImage,10); 
		seconds=rtc.retval();CPU_INFO=seconds-CPU_INFO;}
                usertext="";computertext="Click the OK button to continue....[CPU:"+CPU_INFO+"]";
		System.out.print("\07"); System.out.flush();
		if (CPU_INFO<2){computertext=computertext+" [Normal, SFX On] ";}
		else if (CPU_INFO > 1 && CPU_INFO < 9) { computertext=computertext+" [Slow, SFX On] "; }
		else { computertext=computertext+"[CPU too slow, please exit]"; }
            }
            if (seconds < 10 && flag == 0 && state == 0)
            {
                computertext="VMap/J v1.0 (C) Yohann Sulaiman. Now loading .... "+10*seconds+"%";
                commandtext=10*seconds+"%";
		if (seconds==9){computertext="Running dynamic lighting performance benchmarks...";}
                comflash=0;
            }
        }

        if (state == 1) {
            if (mousex < 900 && mousex > 728 && mousey < 645 && mousey > 475)
            { computertext = "Use the menu bar displayed to enter start/end coordinates :";
	        myImage=getImage(getCodeBase(),"map.jpg");
		updatemenu=1; about=0;
		menu=1;
                mousex=0;
                mousey=0;
                usertext="";
                state=state + 1;
                commandtext=" ";
                comflash=0;
		url=this.getCodeBase();
		try{maprecords=ants.loaddb(url);} catch(Exception E){} // Ant Races init.
		if (DEBUG==1){System.out.println("WARNING: SYSTEM IN DEBUG MODE.");ants.DumpData();}
		System.out.println("VMap/J run time engine..init()::"+url);
            }
        }

        if (state == 2)
        {
if (exception != 0) {
// Menu bar code handlers...
state=exception+2;
exception=0; }       	
if (mousex < 900 && mousex > 728 && mousey < 645 && mousey > 475 && mapcalc==1)
{state=20;}}

	if (state == 3)
	{
	state=1; mousex=800; mousey=600;about=0;
	}
	if (state == 4)
	{
	getGraphics().fillRect(0,0,900,693);
	repaint();
	timer=null;
	Runtime.getRuntime().exit(0);
	}
	if (state == 5)
	{
	// Set current start or end address to photonics centre.
	state=2;about=0;mousex=0;mousey=0;
	if (gridg.currentdata()==0)
	{computertext="Start Point is at Photonics Centre."; 
	gridg.startpt(19);gridg.setstart(2,2);}
	else {computertext="End Point is at Photonics Centre.";
	gridg.endpt(19);gridg.setend(2,2);commandtext="GO";comflash=1;mapcalc=1;}
	}
	if (state == 6)
	{
	// Set current start or end address to morse auditorium.
	state=2; about=0;mousex=0;mousey=0;
	if (gridg.currentdata()==0)
	{computertext="Start Point is at Morse Auditorium."; 
	gridg.startpt(605);gridg.setstart(0,7);}
	else {computertext="End Point is at Morse Auditorium.";
	gridg.endpt(605);gridg.setend(0,7);commandtext="GO"; comflash=1;mapcalc=1;}
	}
	if (state == 7)
	{
	// Set Start Pt's Street Address.
	// clickable bar with no sliders.
	state=11;about=0;
        myImagec=getImage(getCodeBase(),"commandslide.gif");
	computertext=" ";
	}
	if (state == 8)
	{
	// Set End Pt's Street Address.
	// clickable bar with no sliders.
	state=12;about=0;
        myImagec=getImage(getCodeBase(),"commandslide.gif");
	computertext=" ";
	}
	if (state == 9)
	{
	// GPS Keyboard Input long = , lat =
	// <> substitute go/execute for this 
	if (gridg.gonogo()==1)
	{ about=0; computertext="Please enter start/end co-ordinates to execute."; }
	myImage=getImage(getCodeBase(),"schematics.jpg");
	state=2;
	}
	if (state == 10) 
	{
	// Display the about box.
	myImage=getImage(getCodeBase(),"about.jpg");
	state=2;
	getGraphics().drawImage(myImage,0,0,this);
	repaint();
	computertext="VMap/J v1.2.5c [SC712 Project] (C) 1999 Yohann_S";
	System.out.println("Dynamic Video Mapping System (VMap/J v1.2.5c) (C) 1999 Yohann_S");
	System.out.println("Code Base : "+url);
        about=1;
	menu=0;
	}
	if (state == 11) {
	// Handles start co-ordinates.
	junk=slidehandler.slidemouse(mousex,mousey);
	if (junk != 0)
	{slider=junk;
	updateslider=1;}
	if (junk==200)
	{ junk = 999;}
	if (junk==300)
	{ junk = 999; }
        if (junk != 999 && junk != 0)
	{gridg.startpt(junk);ants.LockCoords(junk);gridg.setstart(ants.getmainx(),ants.getmainy());}
	if (junk==999)
	{ state=1; mousex=800; mousey=600; slider=0;about=0;
        myImagec=getImage(getCodeBase(),"commandbar.gif");}
	// Handle exceptions
	if (exception+2 == 4)
	{state=4;about=0;}
	}
	if (state == 12) {
	// Handles end co-ordinates.
	junk=slidehandler.slidemouse(mousex,mousey);
	if (junk != 0)
	{slider=junk;
	updateslider=1;}
	if (junk==200)
	{ junk = 999;}
	if (junk==300)
	{ junk = 999; }
        if (junk != 999 && junk != 0)
	{gridg.endpt(junk);ants.LockCoords(junk);gridg.setend(ants.getmainx(),ants.getmainy());}
	if (junk==999)
	{ mousex=0;mousey=0;state=2; about=0;commandtext="GO"; comflash=1; slider=0; mapcalc=1;
        myImagec=getImage(getCodeBase(),"commandbar.gif");}
	// Handle exceptions
	if (exception+2 == 4)
	{state=4;}
	}
	if (state==20)
	{ // Run the ant races algorithm
	mousex=800;mousey=600;
	computertext="Evolving shortest path .. AntRaces::init()..";
	maxrun=ants.AntRaces(gridg.retx(0),gridg.rety(0),gridg.retx(1),gridg.rety(1));
	if (DEBUG==1){ants.DumpAntRaces();} backupmaxrun=maxrun;
	comflash=0;state=21;menu=0;about=0;seconds=rtc.rst(100);junk=0;flip=0;
	computertext="AntRaces::end().. Loading image sequence.."; commandtext=" ";
	}
	if (state==21)
	{
	blankstring=ants.Sequencer(flip);flip=flip+1;
	if (seconds<110 && junk==0 && maxrun > 0)
	{myImage=getImage(getCodeBase(),blankstring);myImageb=getImage(getCodeBase(),ants.SequencerC(flip));
	computertext=ants.SequencerX(flip);
	getGraphics().drawImage(myImage,0,0,this);getGraphics().drawImage(myImageb, 718, 465, this);
	repaint();junk=1;seconds=rtc.rst(100);state=22;
	if (DEBUG==1){System.out.println(blankstring+"flipper: "+flip);}
	}
	if (seconds > 110 && maxrun <= 0)
	{state=22;}
	}
	if (state==22)
	{
	if (seconds>110 && maxrun >0)
	{seconds=rtc.rst(100);maxrun=maxrun-1;junk=0;state=21;}
	commandtext=(110-seconds)+"s";
	if (maxrun<=0 && seconds >110)
	{myImageb=getImage(getCodeBase(),"compass.gif");state=23;junk=1;}
	}
	if (state==23)
	{
	if (junk == 1){
	junk=0; blankstring=ants.SequencerL();
	if (DEBUG==1){System.out.println(blankstring+" backup: "+backupmaxrun);}
	myImage=getImage(getCodeBase(),blankstring);
	getGraphics().drawImage(myImage,0,0,this);repaint();seconds=rtc.rst(100);}
	if (seconds>110)
	{state=24;}
	}
	if (state==24)
	{
	state=1;mousex=800;mousey=600;gridg.clearall();
	}
}

    // Plotpoints allows calculation to only cover 45 degrees of the circle,
    // and then mirror

public void plotpoints(int x0, int y0, int x, int y, Graphics g) {
        // g.drawLine(x0-x,y0+y,x0-x,y0+y);
        // Allows us to plot points at 1 second intervals with a runnable timer.
    }

    // Circle is just Bresenham's algorithm for a scan converted circle.

public void circle(int x0, int y0, int r, Graphics g) {
        int x,y;
        float d;

        x=0;
        y=r;
        d=5/4-r;

        plotpoints(x0,y0,x,y,g);

        while (y>x){
            if (d<0) {
                d=d+2*x+3;
                x++;
            }
            else {
                d=d+2*(x-y)+5;
                x++;
                y--;
            }
            plotpoints(x0,y0,x,y,g);
        }
    }

    // Paint is the main part of the program
    // Paint allows drawing of layered GUI imagery.

public void paint(Graphics g) {
        int s, m, h, xcenter, ycenter;
        String today;

        dat = new Date();
        cal.setTime(dat);
        //  cal.computeFields(); Not needed anymore
        s = (int)cal.get(Calendar.SECOND);
        m = (int)cal.get(Calendar.MINUTE);
        h = (int)cal.get(Calendar.HOUR_OF_DAY);
        today = df.format(dat);
        xcenter=0;
        ycenter=0;
	if (System.getProperty("os.name").equalsIgnoreCase("IRIX")) {
        g.setFont(F);} else {g.setFont(Fx);}
        g.setColor(fgcol);
        g.setColor(fgcol2);
        //        g.setColor(getBackground());
        lastdate = today;
        // bi - sync algorithm for n microsecond draws with intervals.
        // Uses double buffering for smooth animations. Upto 10 fps.
	// Any Images outside the if's are clipped.
        // triple layered with animation support routine for all 3 layers.
        // layer 1 image   0-3
        // layer 2 navbar  0-3
        // layer 3 command console 0-3
        if (runonce==0)
        {
	    gridg.setlight(h);
            g.drawImage(myImage, 0, 0, this); 
            g.drawImage(myImageb, 718, 465, this);
            g.drawImage(myImagec, 0, 641, this);
            g.drawString(computertext,25,677);
            g.drawString(commandtext,800,558);

	if (about==1)
		{
	// Print String s = System.getProperty("os.name");
	// java. version, vendor, vendor.url, class.version
	// os. name, arch, version
		String abouts="Machine Information : ";
		g.drawString(abouts,188,184);
		abouts=" OS Name : "+System.getProperty("os.name");
		g.drawString(abouts,194,204);
		abouts=" Architecture : "+System.getProperty("os.arch");
		g.drawString(abouts,194,224);
		abouts=" OS Version : "+System.getProperty("os.version");
		g.drawString(abouts,194,244);
		abouts=" JVM Version : "+System.getProperty("java.version");
		g.drawString(abouts,194,264);
		abouts=" Java Vendor : ";
		g.drawString(abouts,194,284);
		abouts=System.getProperty("java.vendor");
		g.drawString(abouts,204,304);
		abouts=" URL : "+System.getProperty("java.vendor.url");
		g.drawString(abouts,194,324);
		abouts=" Class version : "+System.getProperty("java.class.version");
		g.drawString(abouts,194,344);
		abouts=" Map DB : "+maprecords+" records.";
		g.drawString(abouts,194,364);
		g.drawString("This program is licensed under the terms",184,404); 
		g.drawString("of the GNU GPL v2.0 or later.(www.gnu.org).",184,424);
		g.drawString(lastdate,194,444);
		}
        }
        if (runonce==5)
        {
	    seconds=rtc.retval();
            state();
            if (comflash > 0)
            {   g.drawImage(myImagebt, 718, 465, this);
                g.drawString(commandtext,800,558); 
	    }
	}	

	// Crude, but significantly improves performance due to preloaded image stream.

	if (menu==0)
	    {g.drawImage(myImagem0, 0, 0, this);}
	if (menu==1)
	    {g.drawImage(myImagem1, 0, 0, this);}
	if (menu==2)
	    {g.drawImage(myImagem2, 0, 0, this);}
	if (menu==3)
	    {g.drawImage(myImagem3, 0, 0, this);}
	if (menu==4)
	    {g.drawImage(myImagem4, 0, 0, this);}
	if (menu==5)
	    {g.drawImage(myImagem5, 0, 0, this);}

if (slider != 0) {g.drawString(""+slider,20,677);
if (slider > 600)
{ if (updateslider==1)
{updateslider=0;
myImagesl=Stretch(slidehandler.slid(slider)*51,10,getImage(getCodeBase(),"slider.gif"));}
g.drawImage(myImagesl,124,647,this);}
else {
if (updateslider==1)
{updateslider=0;
myImagesl=Stretch(slidehandler.slid(slider)*46,10,getImage(getCodeBase(),"slider.gif"));}
g.drawImage(myImagesl,124,675,this);}}

        runonce++;
if (DEBUG==1){ g.drawString(mousex+","+mousey+" St:"+state+" Men:"+menu+" CPU:"+CPU_INFO+"s:"+seconds,200,400);
g.drawString("Start:"+gridg.retx(0)+","+gridg.rety(0)+" End:"+gridg.retx(1)+","+gridg.rety(1),200,500);
g.drawString(" WARNING : ** DEBUG MODE ** !! WARNING : ** DEBUG MODE ** !! WARNING : ** DEBUG MODE **",0,22);
g.drawString(" WARNING : ** DEBUG MODE ** !! WARNING : ** DEBUG MODE ** !! WARNING : ** DEBUG MODE **",0,356);}
        if (runonce>9){runonce=0;}
        dat=null;
	timer.yield();
    }

public void start() {
        if(timer == null)
        {
            timer = new Thread(this);
            timer.start();
	    rtc.start();
//	    timer.setPriority(100);
//	    rtc.setPriority(1);
        }
    }

public void stop() {
        timer = null;
    }

public void run() {

        while (timer != null) {
            // 1000 = 1 second.
            repaint();
	    Thread.yield();
            try {Thread.sleep(100); } catch (InterruptedException e){}
        }
        timer = null;
   }

}
