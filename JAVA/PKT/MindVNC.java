//
//  Copyright (C) 1997, 1998 Olivetti & Oracle Research Laboratory
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//

//
// MindVNC.java - the VNC viewer class.  This class mainly just sets up the
// user interface, leaving it to the vncCanvas to do the actual rendering of
// a VNC desktop.
//
// (C) 2004 Yohann Sulaiman. Fixed for better interface.
package mindbright.application;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import mindbright.ssh.*;
import mindbright.vnc.*;

public class MindVNC extends java.applet.Applet
		       implements java.lang.Runnable
{
  boolean inAnApplet = true;
  boolean separateFrame;
  private static boolean terminate=false;
  static Container cont;
  private static MindVNC v;
  //
  // main() is called when run as a java program from the command line.  It
  // simply creates a frame and runs the applet inside it.
  //

  public static void main(String[] argv){try{
    v = new MindVNC();
    v.mainArgs = argv;
    v.inAnApplet = false;

    v.f = new Frame("PKT File Utilities - MindVNC v0.1");
    v.f.add("Center", v);

    v.init();
    v.start();
  }catch(Exception e){System.err.println(e.toString());}}

  String[] mainArgs;

  String sshHost;
  int    sshPort;
  String vncHost;
  int    vncPort;
  String sshUser;
  String sshPasswd;

  ScrollPane sp;
  vncCanvas vc;
  public rfbProto rfb;
  GridBagLayout gridbag;
  Panel buttonPanel;
  Button disconnectButton;
  Button optionsButton;
  Button clipboardButton;
  Button ctrlAltDelButton;
  authenticationPanel authenticator=null;
  static Frame f=null;
  static Thread rfbThread=null;
  public static optionsFrame options=null;
  public static clipboardFrame clipboard=null;

  //
  // init()
  //

  public void init() {

    readParameters();

    if(inAnApplet && separateFrame)
	cont = f = new Frame("MindVNC v0.1");
    else
	cont = this;

    if(f != null) {
	f.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e)  { doExit(); 
						/*System.exit()*/ }
	});
    }


    options = new optionsFrame(this);
    clipboard = new clipboardFrame(this);
    authenticator = new authenticationPanel();

//    rfbThread = new Thread(this);
	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}
    rfbThread = new Thread(SSH.getThreadGroup(), this); // JH_Mod
    rfbThread.start();
  }

  public void update(Graphics g) {
  }

  //
  // run() - executed by the rfbThread to deal with the RFB socket.
  //

  public void run() {

    gridbag = new GridBagLayout();
    cont.setLayout(gridbag);

    buttonPanel = new Panel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    disconnectButton = new Button("Disconnect");
    buttonPanel.add(disconnectButton);
    optionsButton = new Button("Options");
    buttonPanel.add(optionsButton);
    clipboardButton = new Button("Clipboard");
    buttonPanel.add(clipboardButton);
    ctrlAltDelButton = new Button("Send Ctrl-Alt-Del");
    buttonPanel.add(ctrlAltDelButton);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gridbag.setConstraints(buttonPanel,gbc);
    cont.add(buttonPanel);

    disconnectButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    buttonPressed(0);
	}
    });
    optionsButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    buttonPressed(1);
	}
    });
    clipboardButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    buttonPressed(2);
	}
    });
    ctrlAltDelButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    buttonPressed(3);
	}
    });


    while(true) {

      disconnectButton.disable();
      clipboardButton.disable();
      ctrlAltDelButton.disable();

      try {
	connectAndAuthenticate();

	rfb.doProtocolInitialisation(options.encodings, options.nEncodings);

	vc = new vncCanvas(this);

	sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill    = GridBagConstraints.BOTH;
	gridbag.setConstraints(sp,gbc);
	cont.add(sp);

	if(f != null) {
	  int sbw = sp.getVScrollbarWidth();
	  int sbh = sp.getHScrollbarHeight();
	  Dimension max  = Toolkit.getDefaultToolkit().getScreenSize();
	  Insets    fIns = f.getInsets();
	  max.width -= (fIns.left + fIns.right + sbw);
	  max.height -= (fIns.top + fIns.bottom + sbh);
	  int w = max.width > rfb.framebufferWidth + 4 ? rfb.framebufferWidth + 4 : max.width;
	  int h = max.height > rfb.framebufferHeight + 4 ? rfb.framebufferHeight + 4 : max.height;
	  sp.setSize(new Dimension(w, h));
	} else {
	  sp.setSize(getSize());
	}
	sp.add(vc);

	if(f != null) {
	  f.setTitle(rfb.desktopName);
	  f.pack();
	} else {
	  validate();
	}

	disconnectButton.enable();
	clipboardButton.enable();
	ctrlAltDelButton.enable();

	vc.processNormalProtocol();
	cont.remove(sp);

      } catch (Exception e) {
	  e.printStackTrace();
	  fatalError(e.toString());
      }
    }
  }


  //
  // Connect to the RFB server and authenticate the user.
  //

  void connectAndAuthenticate() throws IOException {

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.ipadx = 100;
    gbc.ipady = 50;
    gridbag.setConstraints(authenticator,gbc);
    cont.add(authenticator);

    validate();
    if(f != null) {
      f.pack();
      f.show();
    }

    boolean authenticationDone = false;

    while (!authenticationDone) {

      synchronized(authenticator) {
	try {
	  authenticator.wait();
	} catch (InterruptedException e) {
	}
      }

      sshUser   = authenticator.sshUser.getText();
      sshPasswd = authenticator.sshPassword.getText();
      vncHost   = authenticator.vncHost.getText();
      vncPort   = 5900;

      int ix;
      if((ix = vncHost.indexOf(':')) != -1) {
	  try {
	      vncPort += Integer.parseInt(vncHost.substring(ix + 1));
	  } catch (NumberFormatException e) {
	      authenticator.retry("VNC host-string format error");
	      continue;
	  }
	  vncHost = vncHost.substring(0, ix);
      }

      try {
	  rfb = new rfbProto(sshHost, sshPort, sshUser, sshPasswd, vncHost, vncPort, this);
      } catch (java.net.ConnectException e) {
	  System.out.println("Connect...");
	  authenticator.retry("VNC host, connection refused");
	  continue;
      } catch (java.io.IOException e) {
	  System.out.println("IO...");
	  authenticator.retry("SSH error: " + e.getMessage());
	  continue;
      }

      if(rfb.connectAndAuthenticate(authenticator))
	break;
    }

    cont.remove(authenticator);
  }

  int getInt(byte[] b, int o) {
      int i = ( ( b[o + 0] & 0xff ) << 24 ) |
	      ( ( b[o + 1] & 0xff ) << 16 ) |
	      ( ( b[o + 2] & 0xff ) <<  8 ) |
	      ( b[o + 3] & 0xff );
      return i;
  }

  void putInt(byte[] b, int o, int i) {
      b[o + 0] = (byte) ( i >>> 24 );
      b[o + 1] = (byte) ( i >>> 16 );
      b[o + 2] = (byte) ( i >>>  8 );
      b[o + 3] = (byte)   i;
  }

  //
  // setCutText() - send the given cut text to the RFB server.
  //

  public void setCutText(String text) {
    try {
      if ((rfb != null) && rfb.inNormalProtocol) {
	rfb.writeClientCutText(text);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  //
  // Respond to button presses
  //

  public void buttonPressed(int buttonNum) {
      switch(buttonNum) {
      case 0:
	  rfb.close();
	  rfb = null;
	  break;
      case 1:
	  if (options.isVisible()) {
	      options.hide();
	  } else {
	      options.show();
	  }
	  break;
      case 2:
	  if (clipboard.isVisible()) {
	      clipboard.hide();
	  } else {
	      clipboard.show();
	  }
	  break;
      case 3:
	  rfb.sendCtrlAltDel();
      }
  }


  //
  // Detect when the focus goes in and out of the applet.  See
  // vncCanvas.handleEvent() for details of why this is necessary.
  //

  public boolean gotFocus = false;

  public boolean gotFocus(Event evt, Object what) {
    gotFocus = true;
    return true;
  }
  public boolean lostFocus(Event evt, Object what) {
    gotFocus = false;
    return true;
  }


  //
  // encryptBytes() - encrypt some bytes in memory using a password.  Note that
  // the mapping from password to key must be the same as that used on the rfb
  // server side.
  //
  // Note also that IDEA encrypts data in 8-byte blocks, so here we will ignore
  // any data beyond the last 8-byte boundary leaving it to the calling
  // function to pad the data appropriately.
  //

  void encryptBytes(byte[] bytes, String passwd) {
    byte[] key = new byte[8];
    passwd.getBytes(0, passwd.length(), key, 0);

    for (int i = passwd.length(); i < 8; i++) {
      key[i] = (byte)0;
    }

    DesCipher des = new DesCipher(key);

    des.encrypt(bytes,0,bytes,0);
    des.encrypt(bytes,8,bytes,8);
  }


  //
  // readParameters() - read parameters from the html source or from the
  // command line.  On the command line, the arguments are just a sequence of
  // param_name/param_value pairs where the names and values correspond to
  // those expected in the html applet tag source.
  //

  public void readParameters() {
    sshHost = readParameter("sshhost", !inAnApplet);
    if (sshHost == null) {
      sshHost = getCodeBase().getHost();
      if (sshHost.equals("")) {
	fatalError("Not able to determine sshhost");
      }
    }

    String s = readParameter("sshport", false);
    if(s != null)
	sshPort = Integer.parseInt(s);
    else
	sshPort = SSH.DEFAULTPORT;

    try {
      separateFrame = (new Boolean(getParameter("sepframe"))).booleanValue();
    } catch (Exception e) {
      separateFrame = true;
    }

  }

  public String readParameter(String name, boolean required) {
    if (inAnApplet) {
      String s = getParameter(name);
      if ((s == null) && required) {
	fatalError(name + " parameter not specified");
      }
      return s;
    }

    for (int i = 0; i < mainArgs.length; i += 2) {
      if (mainArgs[i].equalsIgnoreCase(name)) {
	try {
	  return mainArgs[i+1];
	} catch (Exception e) {
	  if (required) {
	    fatalError(name + " parameter not specified");
	  }
	  return null;
	}
      }
    }
    if (required) {
      fatalError(name + " parameter not specified");
    }
    return null;
  }

  //
  // fatalError() - print out a fatal error message.
  //

public static void doExit(){
try{
clipboard.dispose();clipboard=null;
}catch(Exception e){}try{
options.dispose();options=null;
}catch(Exception e){}try{
f.dispose();
}catch(Exception e){}try{
rfbThread.stop();rfbThread=null;
}catch(Exception e){}try{
terminate=true;
}catch(Exception e){}try{
cont=null;/*System.exit(1);*/
}catch(Exception e){}try{
v=null;
}catch(Exception e){}
}

  public void fatalError(String s) {
    System.out.println(s);

    if (inAnApplet) {
      cont.removeAll();
      Label l = new Label(s);
      setLayout(new FlowLayout(FlowLayout.LEFT, 30, 30));
      cont.add(l);
      validate();
      Thread.currentThread().stop();
    } else {
      f.dispose();rfbThread.stop();rfbThread=null;terminate=true;cont=null;/*System.exit(1);*/
    }
  }
}
