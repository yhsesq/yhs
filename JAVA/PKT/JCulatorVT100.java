/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002 ymnk, JCraft,Inc.
 *  
 * Written by: 2002 ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package net.sf.pkt.ssh2;

import java.io.InputStream;
import java.io.IOException;

public class JCulatorVT100 extends JCulator{

  private int term_width=80;
  private int term_height=24;

  private int x=0;
  private int y=0;

  private int char_width;
  private int char_height;

  private Object fground=null;
  private Object bground=null;

  public JCulatorVT100(JCrm term, InputStream in){
    super(term, in);
  }

  public void setInputStream(InputStream in){
    this.in=in;
  }
  public void setTerm(JCrm term){
    this.term=term;
  }

  public void start(){

    term_width=term.getColumnCount();
    term_height=term.getRowCount();

    char_width=term.getCharWidth();
    char_height=term.getCharHeight();
    int rx=0;
    int ry=0;
    int w=0;
    int h=0;

    int region_y1=1;
    int region_y2=term_height;

    x=0;
    y=char_height;

    int[] intarg=new int[10];
    int intargi=0;

    int tab=8;

    byte b;

    try{
    while(true){

      b=getChar();

      ry=y;
      rx=x;

/*
        outputs from infocmp on RedHat8.0
#       Reconstructed via infocmp from file: /usr/share/terminfo/v/vt100
vt100|vt100-am|dec vt100 (w/advanced video), 
        am, msgr, xenl, xon, 
        cols#80, it#8, lines#24, vt#3, 
	acsc=``aaffggjjkkllmmnnooppqqrrssttuuvvwwxxyyzz{{||}}~~, 
	bel=^G, blink=\E[5m$<2>, bold=\E[1m$<2>, 
	clear=\E[H\E[J$<50>, cr=^M, csr=\E[%i%p1%d;%p2%dr,
	cub=\E[%p1%dD, cub1=^H, cud=\E[%p1%dB, cud1=^J,
        cuf=\E[%p1%dC, cuf1=\E[C$<2>,
        cup=\E[%i%p1%d;%p2%dH$<5>, cuu=\E[%p1%dA, 
        cuu1=\E[A$<2>, ed=\E[J$<50>, el=\E[K$<3>, el1=\E[1K$<3>, 
        enacs=\E(B\E)0, home=\E[H, ht=^I, hts=\EH, ind=^J, ka1=\EOq, 
        ka3=\EOs, kb2=\EOr, kbs=^H, kc1=\EOp, kc3=\EOn, kcub1=\EOD, 
        kcud1=\EOB, kcuf1=\EOC, kcuu1=\EOA, kent=\EOM, kf0=\EOy, 
        kf1=\EOP, kf10=\EOx, kf2=\EOQ, kf3=\EOR, kf4=\EOS, kf5=\EOt, 
        kf6=\EOu, kf7=\EOv, kf8=\EOl, kf9=\EOw, rc=\E8, 
        rev=\E[7m$<2>, ri=\EM$<5>, rmacs=^O, rmam=\E[?7l, 
        rmkx=\E[?1l\E>, rmso=\E[m$<2>, rmul=\E[m$<2>, 
        rs2=\E>\E[?3l\E[?4l\E[?5l\E[?7h\E[?8h, sc=\E7, 
        sgr=\E[0%?%p1%p6%|%t;1%;%?%p2%t;4%;%?%p1%p3%|%t;7%;%?%p4%t;5%;m%?%p9%t\016%e\017%;$<2>, 
        sgr0=\E[m\017$<2>, smacs=^N, smam=\E[?7h, smkx=\E[?1h\E=, 
        smso=\E[7m$<2>, smul=\E[4m$<2>, tbc=\E[3g, 
*/
/*
        am    terminal has automatic margnins
        msgr  safe to move while in standout mode
        xenl  newline ignored after 80 cols (concept)
        xon   terminal uses xon/xoff handshake
        cols  number of columns in a line
        it    tabs initially every # spaces
        lines number of lines on screen of page
        vt    virstual terminal number(CB/unix)
        acsc  graphics charset pairs, based on vt100
        bel   bell
        blink turn on blinking
        bold  turn on bold(extra bright) mode
        clear clear screen and home cursor(P*)
        cr    carriage return (P)(P*)
        csr   change region to line #1 to line #2(P)
        cub   move #1 characters to the left (P)
        cub1  move left one space
        cud   down #1 lines (P*)
        cud1  down one line
        cuf   move to #1 characters to the right.
        cuf1  non-destructive space (move right one space)
        cup   move to row #1 columns #2
        cuu   up #1 lines (P*)
        cuu1  up one line
        ed    clear to end of screen (P*)
        el    clear to end of line (P)
        el1   Clear to begining of line 
        enacs enable alterate char set
        home  home cursor (if no cup)
        ht    tab to next 8-space hardware tab stop
        hts   set a tab in every row, current columns
        ind   scroll text up
        ka1   upper left of keypad
        ka3   upper right of keypad
        kb2   center of keypad
        kbs   backspace key
        kc1   lower left of keypad
        kc3   lower right of keypad
        kcub1 left-arrow key
        kcud1 down-arrow key
        kcuf1 right-arrow key
        kcuu1 up-arrow key
        kent  enter/sekd key
        kf0   F0 function key
        kf1   F1 function key
        kf10  F10 function key
        kf2   F2 function key
        kf3   F3 function key
        kf4   F4 function key
        kf5   F5 function key
        kf6   F6 function key
        kf7   F7 function key
        kf8   F8 function key
        kf9   F9 function key
        rc    restore cursor to position of last save_cursor
        rev   turn on reverse video mode
        ri    scroll text down (P)
        rmacs end alternate character set 
        rmam  turn off automatic margins
        rmkx  leave 'keybroad_transmit' mode
        rmso  exit standout mode
        rmul  exit underline mode
        rs2   reset string
        sc    save current cursor position (P)
        sgr   define video attribute #1-#9(PG9)
        sgr0  turn off all attributes
        smacs start alternate character set (P)
        smam  turn on automatic margins 
        smkx  enter 'keyborad_transmit' mode
        smso  begin standout mode
        smul  begin underline mode
        tbc   clear all tab stops(P)
 */
      if(b==0){
	continue;
      }

      if(b==0x1b){
	b=getChar();

	if(b=='M'){   // sr \EM sr scroll text down (P)
	  term.draw_cursor();
	  term.scroll_area(0, (region_y1-1)*char_height,
			   term_width*char_width, 
			   (region_y2-region_y1)*char_height,
			   0, char_height);
	  term.clear_area(x, y-char_height, term_width*char_width, y);
	  term.redraw(0, 0,
		      term_width*char_width, 
		      term_height*char_height-char_height);
          //term.setCursor(x, y);
	  term.draw_cursor();

	  continue;
	}

	if(b=='D'){  // sf
	  term.draw_cursor();
	  term.scroll_area(0, (region_y1-1)*char_height,
			   term_width*char_width, 
			   (region_y2-region_y1+1)*char_height,
			   0, -char_height);
	  term.clear_area(0, region_y2*char_height-char_height, term_width*char_width, region_y2*char_height);
	  term.redraw(0, (region_y1-1)*char_height,
		      term_width*char_width, 
		      (region_y2-region_y1+1)*char_height);
	  term.draw_cursor();
	  continue;
	}

	if(b!='['){
//System.out.print("@1: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
	  pushChar(b);
	  continue;
	}

//System.out.print("@2: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");

/*
	b=getChar();
//System.out.print("@3: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
	if(b=='7'){
	  b=getChar();
	  if(b=='m'){                    // rev
//	    System.out.println("rev: "+bground+", "+fground);
	    term.setFGround(bground);
	    term.setBGround(fground);
	    continue;
	  }
	  else{
	    pushChar(b);
	    b=(byte)'7';
	  }
	}
	pushChar(b);
*/

	intargi=0;
	intarg[intargi]=0;
	int digit=0;
	while(true){
	  b=getChar();
//System.out.print("#"+new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
	  if(b==';'){
	    if(digit>0){
	    intargi++;
	    intarg[intargi]=0;
	    digit=0;
	    }
	    continue;
	  }
	  if('0'<=b && b<='9'){
	    intarg[intargi]=intarg[intargi]*10+(b-'0');
	    digit++;
	    continue;
	  }
	  pushChar(b);
	  break;
	}
	
	b=getChar();

//System.out.print("@4: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");

	if(b=='m'){
          if(intargi==0 && digit>0){
            if(intarg[0]==1){ // bold
              continue;
	    }
            if(intarg[0]==7){ // rev
	      term.setFGround(bground);
	      term.setBGround(fground);
	      continue;
	    }
          }
	  term.setFGround(fground);
	  term.setBGround(bground);
	  continue;
	}
//	if(b=='m'){ continue;}

	if(b=='r'){
	  region_y1=intarg[0];
	  region_y2=intarg[1];
//System.out.println("r: "+region_y1+", "+region_y2+", intargi="+intargi);
	  continue;
	}

	if(b=='H'){
	  if(intargi==0){
	    intarg[0]=intarg[1]=1;
	  }
//System.out.println("H: "+region_y1+", "+region_y2+", intargi="+intargi);
	  term.draw_cursor();
	  x=(intarg[1]-1)*char_width;
	  y=intarg[0]*char_height;
	  term.setCursor(x, y);
	  term.draw_cursor();
	  continue;
	}

	if(b=='C'){
	  term.draw_cursor();
	  if(intargi==0){
	    intarg[0]=1;
	  }
	  x+=(intarg[0])*char_width;
	  term.setCursor(x, y);
	  term.draw_cursor();
	  continue;
	}

	if(b=='K'){
	  term.draw_cursor();
	  term.clear_area(x, y-char_height, term_width*char_width, y);
	  term.redraw(x, y-char_height, 
		      (term_width-x/char_width)*char_width,
		      char_height);
	  //term.setCursor(x, y);
	  term.draw_cursor();
	  continue;
	}

	if(b=='J'){

//for(int i=0; i<intargi; i++){ System.out.print(intarg[i]+" ");}
//System.out.println(intarg[0]+"<- intargi="+intargi);

	  term.draw_cursor();
	  term.clear_area(x, y-char_height, 
			  term_width*char_width, term_height*char_height);
	  term.redraw(x, y-char_height, 
		      term_width*char_width-x, 
		      term_height*char_height-y+char_height);
	  //term.setCursor(x, y);
	  term.draw_cursor();
	  continue;
	}

	if(b=='A'){
	  term.draw_cursor();
	  x=0;
	  y-=char_height;
	  term.setCursor(x, y);
	  term.draw_cursor();
	  continue;
	}

	if(b=='?'){
	  b=getChar();
	  if(b=='1'){
	    b=getChar();
	    if(b=='l' || b=='l'){
	      b=getChar();
	      if(b==0x1b){
		b=getChar();
		if(b=='>' || b=='='){
		  continue;
		}
	      }
	    }
	    else if(b=='h'){
	      b=getChar();
	      if(b==0x1b){
		b=getChar();
		if(b=='='){          // smkx enter 'keyborad_transmit' mode
		  continue;
		}
	      }
	    }
	  }
	}

	if(b=='h'){   // kh \Eh home key
	  continue;
	}

//	System.out.println("unknown "+Integer.toHexString(b&0xff)+" "+new Character((char)b)+", "+intarg[0]+", "+intarg[1]+", "+intarg[2]+",intargi="+intargi);
	continue;
      }

      if(b==0x07){
	term.beep();
	continue;
      }

      if(b==0x09){    // ht(^I)
	term.draw_cursor();
	x=(((x/char_width)/tab+1)*tab*char_width);
	if(x>=term_width*char_width){
	  x=0;
	  y+=char_height;
	}
	term.setCursor(x, y);
	term.draw_cursor();
	continue;
      }

      if(b==0x0f){
	// end alternate character set (P)
	continue;
      }

      if(b==0x0d){
	term.draw_cursor();
	x=0;
	term.setCursor(x, y);
	term.draw_cursor();
	continue;
      }

      if(b==0x08){
	term.draw_cursor();
	x-=char_width;
	if(x<0){
	  y-=char_height;
	  x=term_width*char_width-char_width;
	}
	term.setCursor(x, y);
	term.draw_cursor();
	continue;
      }

      if(b!=0x0a){   // !'\n'

//	term.draw_cursor();
	if(x>=term_width*char_width){
//System.out.println("!! "+new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
	  x=0;
	  y+=char_height;


      if(y>region_y2*char_height){
	term.draw_cursor();
	y-=char_height;
	term.scroll_area(0, region_y1*char_height,
		    term_width*char_width, 
		    (region_y2-region_y1)*char_height,
		    0, -char_height);
	term.clear_area(0, y-char_height, term_width*char_width, y);
	term.redraw(0, 0, 
	       term_width*char_width, 
	       region_y2*char_height);
	term.setCursor(x, y);
	term.draw_cursor();
      }


	  rx=x;
	  ry=y;

	}

	term.draw_cursor();

//System.out.print(new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
	if((b&0x80)!=0){
	  term.clear_area(x, y-char_height, x+char_width*2, y);
	  byte[] foo=new byte[2];
	  foo[0]=b;
	  foo[1]=getChar();
	  term.drawString(new String(foo, 0, 2, "EUC-JP"), x, y);
	  x+=char_width;
	  x+=char_width;
	  w=char_width*2;
	  h=char_height;
	}
	else{
	  pushChar(b);
	  int foo=getASCII(term_width-(x/char_width));
if(foo!=0){
//System.out.println("foo="+foo+" "+x+", "+(y-char_height)+" "+(x+foo*char_width)+" "+y+" "+buf+" "+bufs+" "+b+" "+buf[bufs-foo]);
//System.out.println("foo="+foo+" "+new String(buf, bufs-foo, foo));

  term.clear_area(x, y-char_height, x+foo*char_width, y);
  term.drawBytes(buf, bufs-foo, foo, x, y);
/*
              graphics.setColor(Color.black);
    	      graphics.drawBytes(buf, bufs-foo, foo, x+1, y-descent+1);
              graphics.setColor(Color.blue);
    	      graphics.drawBytes(buf, bufs-foo, foo, x, y-descent);
*/
}
else{
foo=1; 
term.clear_area(x, y-char_height, x+foo*char_width, y);
byte[] bar=new byte[1];
bar[0]=getChar();
term.drawBytes(bar, 0, foo, x, y);
//System.out.print("["+Integer.toHexString(bar[0]&0xff)+"]");
}
x+=(char_width*foo);
w=char_width*foo;
h=char_height;
//w++;
//h++;
	}
	term.redraw(rx, ry-char_height,  w, h);
	term.setCursor(x, y);
	term.draw_cursor();
      }

      if(b==0x0a){ // '\n'
//System.out.println("x="+x+",y="+y);
	term.draw_cursor();
	y+=char_height;
	term.setCursor(x, y);
	term.draw_cursor();
      }

      if(y>region_y2*char_height){
	term.draw_cursor();
	y-=char_height;
	term.scroll_area(0, region_y1*char_height,
		    term_width*char_width, 
		    (region_y2-region_y1)*char_height,
		    0, -char_height);
	term.clear_area(0, y-char_height, term_width*char_width, y);
	term.redraw(0, 0, 
	       term_width*char_width, 
	       region_y2*char_height);
	term.setCursor(x, y);
	term.draw_cursor();
      }
    }
    }
    catch(Exception e){
    }
  }
  private static byte[] ENTER={(byte)0x0d};
  private static byte[] UP={(byte)0x1b, (byte)0x4f, (byte)0x41};
  private static byte[] DOWN={(byte)0x1b, (byte)0x4f, (byte)0x42};
  private static byte[] RIGHT={(byte)0x1b, (byte)/*0x5b*/0x4f, (byte)0x43};
  private static byte[] LEFT={(byte)0x1b, (byte)/*0x5b*/0x4f, (byte)0x44};
  private static byte[] F1={(byte)0x1b, (byte)0x4f, (byte)'P'};
  private static byte[] F2={(byte)0x1b, (byte)0x4f, (byte)'Q'};
  private static byte[] F3={(byte)0x1b, (byte)0x4f, (byte)'R'};
  private static byte[] F4={(byte)0x1b, (byte)0x4f, (byte)'S'};
  private static byte[] F5={(byte)0x1b, (byte)0x4f, (byte)'t'};
  private static byte[] F6={(byte)0x1b, (byte)0x4f, (byte)'u'};
  private static byte[] F7={(byte)0x1b, (byte)0x4f, (byte)'v'};
  private static byte[] F8={(byte)0x1b, (byte)0x4f, (byte)'I'};
  private static byte[] F9={(byte)0x1b, (byte)0x4f, (byte)'w'};
  private static byte[] F10={(byte)0x1b, (byte)0x4f, (byte)'x'};

  public byte[] getCodeENTER(){ return ENTER; }
  public byte[] getCodeUP(){ return UP; }
  public byte[] getCodeDOWN(){ return DOWN; }
  public byte[] getCodeRIGHT(){ return RIGHT; }
  public byte[] getCodeLEFT(){ return LEFT; }
  public byte[] getCodeF1(){ return F1; }
  public byte[] getCodeF2(){ return F2; }
  public byte[] getCodeF3(){ return F3; }
  public byte[] getCodeF4(){ return F4; }
  public byte[] getCodeF5(){ return F5; }
  public byte[] getCodeF6(){ return F6; }
  public byte[] getCodeF7(){ return F7; }
  public byte[] getCodeF8(){ return F8; }
  public byte[] getCodeF9(){ return F9; }
  public byte[] getCodeF10(){ return F10; }

  public void reset(){
    term_width=term.getColumnCount();
    term_height=term.getRowCount();
    char_width=term.getCharWidth();
    char_height=term.getCharHeight();

    fground=term.getFGround();
    bground=term.getBGround();
  }
}
