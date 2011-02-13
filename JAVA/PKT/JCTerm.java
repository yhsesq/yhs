/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002-2004 ymnk, JCraft,Inc.
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

import net.sf.pkt.ssh2.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.text.*;
import mindbright.ssh.SSHMiscDialogs;

public class JCTerm  extends JPanel implements KeyListener, ActionListener, Runnable, JCrm{
  static String COPYRIGHT=
"JCTerm 0.0.6ys\nCopyright (C) 2005 Yohann Sulaiman, 2002-2004 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"+
"Official Homepage: http://pkt.sf.net, JCTerm homepage : http://www.jcraft.com/jcterm/\n"+
"This software is licensed under GNU GPL.";
  private static final int SHELL=0;
  private static final int SFTP=1;
  private static final int EXEC=2;
private static JCTerm ssh2object;
private static JFrame frame;
  private int mode=SHELL;

  private OutputStream out;
  private InputStream in;

  private BufferedImage img;
  private BufferedImage background;
  private Graphics2D cursor_graphics;
  private Graphics2D graphics;
  private java.awt.Color bground=Color.white;
  private java.awt.Color fground=Color.black;
  private java.awt.Component term_area=null;
  private java.awt.Font font;

  private int term_width=80;
  private int term_height=24;

  private int x=0;
  private int y=0;
  private int descent=0;

  private int char_width;
  private int char_height;

  private String xhost="127.0.0.1";    
  private int xport=0;    
  private boolean xforwarding=false;
  private String user=System.getProperty("user.name");
  private String host="127.0.0.1";

  private String proxy_http_host=null;
  private int proxy_http_port=0;

  private String proxy_socks5_host=null;
  private int proxy_socks5_port=0;

  private JYssion session=null;
  private JYoxy proxy=null;

  private boolean antialiasing=true;
  //private int line_space=0;
  private int line_space=-2;
  private int compression=0;

  public JCTerm(){jctermgo();}
 public void jctermgo(){
    font=java.awt.Font.decode("Monospaced-14");
    img=new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    graphics=(Graphics2D)(img.getGraphics());
    graphics.setFont(font);
    {
      FontMetrics fo = graphics.getFontMetrics();
      descent=fo.getDescent();
      /*
System.out.println(fo.getDescent());
System.out.println(fo.getAscent());
System.out.println(fo.getLeading());
System.out.println(fo.getHeight());
System.out.println(fo.getMaxAscent());
System.out.println(fo.getMaxDescent());
System.out.println(fo.getMaxDecent());
System.out.println(fo.getMaxAdvance());
      */
      char_width=(int)(fo.charWidth((char)'@'));
      char_height=(int)(fo.getHeight())+(line_space*2);   
      descent+=line_space;
    }
    img.flush();
    graphics.dispose();

    background=new BufferedImage(char_width, char_height, BufferedImage.TYPE_INT_RGB);
    {
      Graphics2D foog=(Graphics2D)(background.getGraphics());
      foog.setColor(bground);
      foog.fillRect(0, 0, char_width, char_height);
      foog.dispose();
    }

    img=new BufferedImage(getTermWidth(), getTermHeight(),
			  BufferedImage.TYPE_INT_RGB);
    graphics=(Graphics2D)(img.getGraphics());
    graphics.setFont(font);

    clear();

    cursor_graphics=(Graphics2D)(img.getGraphics());
    cursor_graphics.setColor(fground);
    cursor_graphics.setXORMode(bground);

    setAntiAliasing(antialiasing);

    term_area=this;

    JPanel panel=this;
    panel.setPreferredSize(new Dimension(getTermWidth(), getTermHeight()));

    panel.setSize(getTermWidth(), getTermHeight());
    panel.setFocusable(true);
    panel.enableInputMethods(true);

    panel.setFocusTraversalKeysEnabled(false);
//  panel.setOpaque(true);
  }

  public void setFrame(java.awt.Component term_area){
    this.term_area=term_area;
  }

  private Thread thread=null;
  public void kick(){
    this.thread=new Thread(this);
    this.thread.start();
  }

  private JCulator emulator=null;
  public void run(){
    JYch jsch=new JYch();
    while(thread!=null){
      try{
	int port=22;
	try{
	  String _host=JOptionPane.showInputDialog(this, 
						   "Enter username@hostname",
						   user+"@"+host);
	  if(_host==null){
	    break;
	  }
	  String _user=_host.substring(0, _host.indexOf('@'));
	  _host=_host.substring(_host.indexOf('@')+1);
	  if(_host==null || _host.length()==0){
	    continue;
	  }
          if(_host.indexOf(':')!=-1){
            try{
              port=Integer.parseInt(_host.substring(_host.indexOf(':')+1));
            }
            catch(Exception eee){}
            _host=_host.substring(0, _host.indexOf(':'));
          }
	  user=_user;
	  host=_host;
	}
	catch(Exception ee){
	  alertDialog("Err:"+ee.toString());continue;
	}

	try{
  	  session=jsch.getSession(user, host, port);
	  session.setProxy(proxy);

	  JYerInfo ui=new MyUserInfo();
	  session.setUserInfo(ui);

	  java.util.Properties config=new java.util.Properties();
	  if(compression==0){
	    config.put("compression.s2c", "none");
	    config.put("compression.c2s", "none");
	  }
	  else{
	    config.put("compression.s2c", "zlib,none");
	    config.put("compression.c2s", "zlib,none");
	  }
	  session.setConfig(config);

	  session.setTimeout(35000);
	  session.connect();
	  session.setTimeout(15000);
	}
	catch(Exception e){
          alertDialog("Err:"+e.toString());
          break;
	}

	JYannel channel=null;

	if(mode==SHELL){
          channel=session.openChannel("shell");
	  if(xforwarding){
 	    session.setX11Host(xhost);
	    session.setX11Port(xport+6000);
 	    channel.setXForwarding(true);
	  }

	  out=channel.getOutputStream();
	  in=channel.getInputStream();

	  channel.connect();
	}
	else if(mode==SFTP){

	  out=new PipedOutputStream();
	  in=new PipedInputStream();

	  channel=session.openChannel("sftp");

//	  out=channel.getOutputStream();
//	  in=channel.getInputStream();

	  channel.connect();

	  (new JCtp((JYannelSftp)channel, 
		    (InputStream)(new PipedInputStream((PipedOutputStream)out)),
		    new PipedOutputStream((PipedInputStream)in))).kick();
	}

        requestFocus();

	emulator=new JCulatorVT100(this, in);
	emulator.reset();
	emulator.start();
      }
      catch(Exception e){
	alertDialog("Err:"+e.toString());
	//e.printStackTrace();
      }
      break;
    }

    thread=null;

    if(session!=null){
      session.disconnect();
      session=null;
    }

    clear();

    redraw(0, 0, getTermWidth(), getTermHeight());
  }

  public void paintComponent(Graphics g){
    super.paintComponent(g);
    if(img!=null){
      g.drawImage(img, 0, 0, term_area);
    }
  }

//  public void update(Graphics g){
//  }
  public void paint(Graphics g){
    super.paint(g);
  }

  public void processKeyEvent(KeyEvent e){
//System.out.println(e);
    int id=e.getID();
    if(id == KeyEvent.KEY_PRESSED) { keyPressed(e); }
    else if(id == KeyEvent.KEY_RELEASED) { /*keyReleased(e);*/ }
    else if(id == KeyEvent.KEY_TYPED) { keyTyped(e);/*keyTyped(e);*/ }
    e.consume(); // ??
  }

  byte[] obuffer=new byte[3];
  public void keyPressed(KeyEvent e){
    int keycode=e.getKeyCode();
    byte[] code=null;
    switch(keycode){
    case KeyEvent.VK_CONTROL:
    case KeyEvent.VK_SHIFT:
    case KeyEvent.VK_ALT:
    case KeyEvent.VK_CAPS_LOCK:
      return;
    case KeyEvent.VK_ENTER:
      code=emulator.getCodeENTER();
      break;
    case KeyEvent.VK_UP:
      code=emulator.getCodeUP();
      break;
    case KeyEvent.VK_DOWN:
      code=emulator.getCodeDOWN();
      break;
    case KeyEvent.VK_RIGHT:
      code=emulator.getCodeRIGHT();
      break;
    case KeyEvent.VK_LEFT:
      code=emulator.getCodeLEFT();
      break;
    case KeyEvent.VK_F1:
      code=emulator.getCodeF1();
      break;
    case KeyEvent.VK_F2:
      code=emulator.getCodeF2();
      break;
    case KeyEvent.VK_F3:
      code=emulator.getCodeF3();
      break;
    case KeyEvent.VK_F4:
      code=emulator.getCodeF4();
      break;
    case KeyEvent.VK_F5:
      code=emulator.getCodeF5();
      break;
    case KeyEvent.VK_F6:
      code=emulator.getCodeF6();
      break;
    case KeyEvent.VK_F7:
      code=emulator.getCodeF7();
      break;
    case KeyEvent.VK_F8:
      code=emulator.getCodeF8();
      break;
    case KeyEvent.VK_F9:
      code=emulator.getCodeF9();
      break;
    case KeyEvent.VK_F10:
      code=emulator.getCodeF10();
      break;
    }
    if(code!=null){
      try{
        out.write(code, 0, code.length);
        out.flush();
      }
      catch(Exception ee){
      }
      return;
    }

    char keychar=e.getKeyChar();
    if((keychar&0xff00)==0){
      obuffer[0]=(byte)(e.getKeyChar());
      try{
        out.write(obuffer, 0, 1);
        out.flush();
      }
      catch(Exception ee){
      }
    }
  }

  public void keyTyped(KeyEvent e){
    char keychar=e.getKeyChar();
    if((keychar&0xff00)!=0){
      char[] foo=new char[1];
      foo[0]=keychar;
      try{
        byte[] goo=new String(foo).getBytes("EUC-JP");
        out.write(goo, 0, goo.length);
        out.flush();
      }
      catch(Exception eee){ }
    }
  }

  public int getTermWidth(){ return char_width*term_width; }
  public int getTermHeight(){ return char_height*term_height; }
  public int getCharWidth(){ return char_width; }
  public int getCharHeight(){ return char_height; }
  public int getColumnCount(){ return term_width; }
  public int getRowCount(){ return term_height; }

  public void clear(){
    graphics.setColor(bground);
    graphics.fillRect(0, 0, char_width*term_width, char_height*term_height);
    graphics.setColor(fground);
  }
  public void setCursor(int x, int y){
    this.x=x;
    this.y=y;
  }
  public void draw_cursor(){
    cursor_graphics.fillRect(x, y-char_height, char_width, char_height);
//    term_area.repaint(x, y-char_height, char_width, char_height);

    Graphics g=getGraphics();
    g.setClip(x, y-char_height, char_width, char_height);
    g.drawImage(img, 0, 0, term_area);
  }    
  public void redraw(int x, int y, int width, int height){
//    term_area.repaint(x, y, width, height);
    Graphics g=getGraphics();
    g.setClip(x, y, width, height);
    g.drawImage(img, 0, 0, term_area);
  }
  public void clear_area(int x1, int y1, int x2, int y2){
//    for(int i=y1; i<y2; i+=char_height){
//      for(int j=x1; j<x2; j+=char_width){
//        graphics.drawImage(background, j, i, term_area); 
//      }
//    }
    graphics.setColor(bground);
    graphics.fillRect(x1, y1, x2-x1, y2-y1);
    graphics.setColor(fground);
  }    
  public void scroll_area(int x, int y, int w, int h, int dx, int dy){
//    graphics.copyArea(x, y, w, h, dx, dy);

    getGraphics().copyArea(x, y, w, h, dx, dy);
    graphics.copyArea(x, y, w, h, dx, dy);
  }
  public void drawBytes(byte[] buf, int s, int len, int x, int y){
//    clear_area(x, y, x+len*char_width, y+char_height);
//    graphics.setColor(fground);
    graphics.drawBytes(buf, s, len, x, y-descent);
  }
  public void drawString(String str, int x, int y){
//    clear_area(x, y, x+str.length()*char_width, y+char_height);
//    graphics.setColor(fground);
    graphics.drawString(str, x, y-descent);
  }
  public void beep(){
    Toolkit.getDefaultToolkit().beep();
  }
  public class MyUserInfo implements JYerInfo{
    public boolean promptYesNo(String str){
      Object[] options={ "yes", "no" };
      int foo=JOptionPane.showOptionDialog(null,
					   str,
					   "Warning",
					   JOptionPane.DEFAULT_OPTION,
					   JOptionPane.WARNING_MESSAGE,
					   null, options, options[0]);
      return foo==0;
    }

    String passwd=null;
    String passphrase=null;
    JTextField pword=new JPasswordField(20); 

    public String getPassword(){  return passwd; }
    public String getPassphrase(){ return passphrase; }

    public boolean promptPassword(String message){
      Object[] ob={pword};
      int result=
	JOptionPane.showConfirmDialog(null, ob, message,
				      JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
        passwd=pword.getText();
        return true;
      }
      else{ return false; }
    }
    public boolean promptPassphrase(String message){
      return true; 
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
  }

  /** Ignores key released events. */
  public void keyReleased(KeyEvent event){}
//  public void keyPressed(KeyEvent event){}

  public void setProxyHttp(String host, int port){
    proxy_http_host=host;
    proxy_http_port=port;
    if(proxy_http_host!=null && proxy_http_port!=0){
      proxy=new JYoxyHTTP(proxy_http_host, proxy_http_port);
    }
    else{
      proxy=null;
    }
  }
  public String getProxyHttpHost(){return proxy_http_host;}
  public int  getProxyHttpPort(){return proxy_http_port;}

  public void setProxySOCKS5(String host, int port){
    proxy_socks5_host=host;
    proxy_socks5_port=port;
    if(proxy_socks5_host!=null && proxy_socks5_port!=0){
      proxy=new JYoxySOCKS5(proxy_socks5_host, proxy_socks5_port);
    }
    else{
      proxy=null;
    }
  }
  public String getProxySOCKS5Host(){return proxy_socks5_host;}
  public int  getProxySOCKS5Port(){return proxy_socks5_port;}
  public void setXHost(String xhost){this.xhost=xhost;}
  public void setXPort(int xport){this.xport=xport;}
  public void setXForwarding(boolean foo){this.xforwarding=foo;}
  public void setLineSpace(int foo){this.line_space=foo;}
  public void setAntiAliasing(boolean foo){
    if(graphics==null) return;
    antialiasing=foo;
    java.lang.Object mode=foo?
	RenderingHints.VALUE_TEXT_ANTIALIAS_ON:
	RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
    RenderingHints hints=
	new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, mode);
    graphics.setRenderingHints(hints);
  }
  public void setCompression(int compression){
    if(compression<0 || 9<compression) return;
    this.compression=compression;
  }
  public int getCompression(){return compression;}
  public void setUserHost(String userhost){
    try{
      String _user=userhost.substring(0, userhost.indexOf('@'));
      String _host=userhost.substring(userhost.indexOf('@')+1);
      this.user=_user;
      this.host=_host;
    }
    catch(Exception e){
    }
  }
  public void openSession(){
    kick();
  }
  public void setPortForwardingL(int port1, String host, int port2){
    try{session.setPortForwardingL(port1, host, port2);}
    catch(JYchException e){
    }
  }
  public void setPortForwardingR(int port1, String host, int port2){
    try{ session.setPortForwardingR(port1, host, port2); }
    catch(JYchException e){
    }
  }

  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    if (action.equals("Open SHELL Session...") ||
	action.equals("Open SFTP Session...")
	){
      if(thread==null){
        if(action.equals("Open SHELL Session...")){ mode=SHELL; }
        else if(action.equals("Open SFTP Session...")){ mode=SFTP; }
        openSession();
      }
    }
    else if (action.equals("HTTP...")){
      String foo=getProxyHttpHost();
      int bar=getProxyHttpPort();
      String proxy=
        JOptionPane.showInputDialog(this,
				    "HTTP proxy server (hostname:port)",
				    ((foo!=null&&bar!=0)? foo+":"+bar :
				     ""));
      if(proxy==null)return;
      if(proxy.length()==0){
        setProxyHttp(null, 0);
        return;
      }

      try{
        foo=proxy.substring(0, proxy.indexOf(':'));
	bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
	if(foo!=null){
          setProxyHttp(foo, bar);
	}
      }
      catch(Exception ee){
      }
    }
    else if (action.equals("SOCKS5...")){
      String foo=getProxySOCKS5Host();
      int bar=getProxySOCKS5Port();
      String proxy=
        JOptionPane.showInputDialog(this,
				    "SOCKS5 server (hostname:1080)",
				    ((foo!=null&&bar!=0)? foo+":"+bar : ""));
      if(proxy==null)return;
      if(proxy.length()==0){
        setProxySOCKS5(null, 0);
	return;
      }

      try{
        foo=proxy.substring(0, proxy.indexOf(':'));
  	bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
        if(foo!=null){
          setProxySOCKS5(foo, bar);
	}
      }
      catch(Exception ee){
      }
    }
    else if(action.equals("X11 Forwarding...")){
      String display=JOptionPane.showInputDialog(this,
						 "XDisplay name (hostname:0)", 
						 (xhost==null)? "": (xhost+":"+xport));
      try{
        if(display!=null){
          xhost=display.substring(0, display.indexOf(':'));
	  xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
	  xforwarding=true;
	}
      }
      catch(Exception ee){
        xforwarding=false;
	xhost=null;
      }
    }
    else if((action.equals("AntiAliasing"))){
      setAntiAliasing(!antialiasing);
    }
    else if(action.equals("Compression...")){
      String foo=JOptionPane.showInputDialog(this,
					     "Compression level(0-9)\n0 means no compression.\n1 means fast.\n9 means slow, but best.", 
					     new Integer(compression).toString());
      try{
        if(foo!=null){
	  compression=Integer.parseInt(foo);
	}
      }
      catch(Exception ee){
      }
    }
    else if(action.equals("About...")){
      JOptionPane.showMessageDialog(this, COPYRIGHT);
      return;
    }
    else if((action.equals("Local Port...")) || 
	    (action.equals("Remote Port..."))){
      if(session==null){
        JOptionPane.showMessageDialog(this,
				      "Establish the connection before this setting.");
	return;
      }

      try{
        String title="";
	if(action.equals("Local Port...")){
          title+="Local port forwarding";
	}
	else{
          title+="remote port forwarding";
	}
        title+="(port:host:hostport)";

	String foo=JOptionPane.showInputDialog(this,
					       title,
					       "");
	if(foo==null)return;
	int port1=Integer.parseInt(foo.substring(0, foo.indexOf(':')));
	foo=foo.substring(foo.indexOf(':')+1);
	String host=foo.substring(0, foo.indexOf(':'));
	int port2=Integer.parseInt(foo.substring(foo.indexOf(':')+1));

	if(action.equals("Local Port...")){
          setPortForwardingL(port1, host, port2);
	}
	else{
          setPortForwardingR(port1, host, port2);
	}
      }
      catch(Exception ee){
      }
    }
    else if (action.equals("Quit")){
      quit();
    }
  }

  public JMenuBar getJMenuBar(){
    JMenuBar mb=new JMenuBar();
    JMenu m;
    JMenuItem mi;

    m=new JMenu("File");
    mi=new JMenuItem("Open SHELL Session...");
    mi.addActionListener(this);
    mi.setActionCommand("Open SHELL Session...");
    m.add(mi);
    mi=new JMenuItem("Open SFTP Session...");
    mi.addActionListener(this);
    mi.setActionCommand("Open SFTP Session...");
    m.add(mi);
    mi=new JMenuItem("Quit");
    mi.addActionListener(this);
    mi.setActionCommand("Quit");
    m.add(mi);
    mb.add(m);

    m=new JMenu("Proxy");
    mi=new JMenuItem("HTTP...");
    mi.addActionListener(this);
    mi.setActionCommand("HTTP...");
    m.add(mi);
    mi=new JMenuItem("SOCKS5...");
    mi.addActionListener(this);
    mi.setActionCommand("SOCKS5...");
    m.add(mi);
    mb.add(m);

    m=new JMenu("PortForwarding");
    mi=new JMenuItem("Local Port...");
    mi.addActionListener(this);
    mi.setActionCommand("Local Port...");
    m.add(mi);
    mi=new JMenuItem("Remote Port...");
    mi.addActionListener(this);
    mi.setActionCommand("Remote Port...");
    m.add(mi);
    mi=new JMenuItem("X11 Forwarding...");
    mi.addActionListener(this);
    mi.setActionCommand("X11 Forwarding...");
    m.add(mi);
    mb.add(m);

    m=new JMenu("Settings");
    mi=new JMenuItem("AntiAliasing");
    mi.addActionListener(this);
    mi.setActionCommand("AntiAliasing");
    m.add(mi);
    mi=new JMenuItem("Compression...");
    mi.addActionListener(this);
    mi.setActionCommand("Compression...");
    m.add(mi);
    mb.add(m);

    m=new JMenu("Help");
    mi=new JMenuItem("About...");
    mi.addActionListener(this);
    mi.setActionCommand("About...");
    m.add(mi);
    mb.add(m);

    return mb;
  }

  public static void main(String[] arg){}
  public void jclaunch(){

    ssh2object=this;
    frame=new JFrame("JCTerm - SSH2");

    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){quit();}
    });

    JMenuBar mb=ssh2object.getJMenuBar();
    frame.setJMenuBar(mb);

    frame.setSize(ssh2object.getTermWidth(), ssh2object.getTermHeight());
    frame.getContentPane().add("Center", ssh2object);

    frame.pack();
    ssh2object.setVisible(true);
    frame.setVisible(true);

    frame.setResizable(true);
    {
      int foo=ssh2object.getTermWidth();
      int bar=ssh2object.getTermHeight();
      foo+=(frame.getWidth()-frame.getContentPane().getWidth());
      bar+=(frame.getHeight()-frame.getContentPane().getHeight());
      frame.setSize(foo, bar);
    }
    frame.setResizable(false);

    ssh2object.setFrame(frame.getContentPane());
try{
java.security.KeyPairGenerator.getInstance("DH");
javax.crypto.KeyAgreement.getInstance("DH");
}catch(Exception xex){ alertDialog(" WARNING: Attempt to initialize JCE failed. \n Please ensure that you have JCE installed or are running Java 1.4 or later. \n JCE error : "+xex.toString()); }
//  ssh2object.kick();
  }
  public void doExit(){quit();}
  public void quit(){
    thread=null;
    if(session!=null){
      session.disconnect();
      session=null;
    } if (frame!=null){frame.dispose();frame=null;}ssh2object.removeAll();ssh2object=null; 
  }

  public void setFGround(Object f){
    if(f instanceof String){
      fground=java.awt.Color.getColor((String)f);
    }
    if(f instanceof java.awt.Color){
      fground=(java.awt.Color)f;
    }
    graphics.setColor(fground);
  } 
  public void setBGround(Object b){
    if(b instanceof String){
      bground=java.awt.Color.getColor((String)b);
    }
    if(b instanceof java.awt.Color){
      bground=(java.awt.Color)b;
    }
    Graphics2D foog=(Graphics2D)(background.getGraphics());
    foog.setColor(bground);
    foog.fillRect(0, 0, char_width, char_height);
    foog.dispose();
  } 
 public final void alertDialog(String message) {
     SSHMiscDialogs.alert("JCTerm SSHv2 - Alert", message, frame);
 }

  public Object getFGround(){ return fground; }
  public Object getBGround(){ return bground; }
}
