/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: josh $
 * $Date: 2001/02/03 00:47:01 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import java.awt.*;
import java.awt.event.*;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import mindbright.application.MindTunnelCli;

import mindbright.util.AWTConvenience;

public class SSHTunnelingClient extends SSHClient implements Runnable, SSHInteractor {

    public static class Logo extends Component {
	Image logo;

	public Logo(Image logo) {
	    this.logo = logo;
	}

	public Dimension getMinimumSize() {
	    return getPreferredSize();
	}

	public Dimension getPreferredSize() {
	    int width  = -1;
	    int height = -1;
	    boolean ready = false;

	    while (!ready) {
		width  = logo.getWidth(null);
		height = logo.getHeight(null);
		if(width != -1 && height != -1) {
		    ready = true;
		}
		Thread.yield();
	    }
	    Dimension dim = new Dimension(width, height);

	    return dim;
	}

	public void paint(Graphics g) {
	    if(logo == null)
		return;
	    Dimension d = getSize();
	    g.drawImage(logo, 0, 0, d.width, d.height, this);
	}
    }

    public static final boolean expires  = false;
    public static final boolean licensed = true;

    public static final long validFrom = 965157940452L; // 000801/21:25
    public static final long validTime = (33L * 24L * 60L * 60L * 1000L);

    public boolean verbose          = false;
    public boolean listMode         = false;
    public boolean haveTunnelDialog = false;
    public boolean haveSCPDialog    = false;
    public boolean haveProxyDialog  = false;
    public boolean haveServer       = false;

    Thread clientThread = null;

    MindTunnelCli tunnelClient;

    Frame parent;

    int logoPlacement;

    SSHPropertyHandler propsHandler;

    public SSHTunnelingClient(SSHPropertyHandler propsHandler, MindTunnelCli tunnelClient) {
	super(propsHandler, propsHandler);

	this.propsHandler = propsHandler;
	this.interactor   = this; // !!! OUCH

	this.tunnelClient = tunnelClient;

	propsHandler.setInteractor(this);
	propsHandler.setClient(this);
    }

    boolean hasExpired() {
	boolean expired = false;
	long now = System.currentTimeMillis();

	if(licensed)
	    return false;

	if(expires) {
	    int daysRemaining = (int)((validTime - (now - validFrom)) / (1000L * 60L * 60L * 24L));
	    if(daysRemaining <= 0) {
		alertDialog("This is a demo version of MindTunnel, it has expired!");
		expired = true;
	    } else {
		alertDialog("This is a demo version of MindTunnel, it will expire in " + daysRemaining + " days");
	    }
	} else {
	    int daysOld = (int)((now - validFrom) / (1000L * 60L * 60L * 24L));
	    alertDialog("This is a demo version of MindTunnel, it is " + daysOld + " days old.");
	}
	return expired;
    }

    public SSHPropertyHandler getPropertyHandler() {
	return propsHandler;
    }

    public void run() {
	clientThread = Thread.currentThread();

	String srv = propsHandler.getProperty("server");
	if(srv != null && srv.trim().length() != 0) {
	    haveServer = true;
	}

	showMainWindow();

	if(hasExpired()) {
	    parent.dispose();
	    return;
	}

	if(NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
	    } catch (netscape.security.ForbiddenTargetException e) {
		alertDialog("Full network access denied, normal applet security applies");
	    }
	}

	while(true) {
	    parent.setTitle(SSH.VER_MINDTUNL + " (offline)");
	    updateButtons();

	    if(listMode) {
		choiceHosts.requestFocus();
	    } else if(haveServer) {
		textUser.requestFocus();
	    } else {
		textSrv.requestFocus();
	    }

	    try {
		// This starts a connection to the sshd and all the related stuff...
		//
		bootSSH(true);

		parent.setTitle(propsHandler.getProperty("usrname") +
				"@" + propsHandler.getProperty("server") +
				" (online)");

		// Tell MindTunnelCli that we are now online
		//
		tunnelClient.online();

		// Join main receiver channel thread and wait for session to end
		//
		controller.waitForExit();

	    } catch(SSHClient.AuthFailException e) {
		alertDialog(e.getMessage());
		propsHandler.clearPasswords();

	    } catch(UnknownHostException e) {
		String host = e.getMessage();
		String msg;
		if(propsHandler.getProperty("proxytype").equals("none")) {
		    msg = "Unknown host: " + host;
		} else {
		    msg = "Unknown proxy host: " + host;
		}
		alertDialog(msg);
		propsHandler.clearServerSetting();

	    } catch(FileNotFoundException e) {
		alertDialog("File not found: " + e.getMessage());

	    } catch(Exception e) {
		String msg = e.getMessage();
		if(msg == null || msg.trim().length() == 0)
		    msg = e.toString();
		msg = "Error connecting to " + propsHandler.getProperty("server") + ", reason: " + msg;
		if(SSH.DEBUGMORE) {
		    System.out.println("If an error occured, please send the below stacktrace to mats@mindbright.se");
		    e.printStackTrace();
		}
		alertDialog(msg);

	    } catch(ThreadDeath death) {
		if(controller != null)
		    controller.killAll();
		controller = null;
		throw death;
	    }

	    propsHandler.passivateProperties();
	    if(usedOTP) {
		propsHandler.clearPasswords();
	    }
	}
    }

    public void updateButtons() {
	boolean isConn = isConnected();
	connBut.setEnabled(!isConn);
	discBut.setEnabled(isConn);
	if(listMode) {
	    choiceHosts.setEnabled(!isConn);
	} else {
	    if(!haveServer)
		textSrv.setEnabled(!isConn);
	    textUser.setEnabled(!isConn);
	    textPasswd.setEnabled(!isConn);
	}

	if(tunnelBut != null)
	    tunnelBut.setEnabled(isConn);
	if(scpBut != null)
	    scpBut.setEnabled(isConn);
	if(proxyBut != null)
	    proxyBut.setEnabled(!isConn);
    }

    //
    // SSHInteractor interface
    //
    public void propsStateChanged(SSHPropertyHandler props) {
    }

    public void startNewSession(SSHClient client) {
	try {
	    synchronized(this) {
		this.wait();
	    }
	} catch (InterruptedException e) {
	}
	if(listMode) {
	    try {
		String host = choiceHosts.getSelectedItem();
		String pwd = "";
		do {
		    try {
			propsHandler.setPropertyPassword(pwd);
			propsHandler.loadAliasFile(host, false);
			break;
		    } catch(SSHClient.AuthFailException ee) {
		    }
		} while((pwd = passwordDialog("Please give file password for " +
					      host, "MindTunnel - File Password")) != null);
	      } catch (Throwable t) {
		  alertDialog("Error loading settings: " + t.getMessage());
	      }
	} else {
	    if(!haveServer)
		propsHandler.setProperty("server", textSrv.getText());
	    propsHandler.setProperty("usrname", textUser.getText());
	    String password = textPasswd.getText();
	    propsHandler.setProperty("password", password);
	    propsHandler.setProperty("rsapassword", password);
	    propsHandler.setProperty("tispassword", password);
	    propsHandler.setPropertyPassword(password);
	    textPasswd.setText("");
	}

	propsHandler.setProperty("forcpty", "false");
    }

    public void sessionStarted(SSHClient client) {
    }

    public boolean quietPrompts() {
	return true;
    }

    public String promptLine(String prompt, String defaultVal) throws IOException {
	return null;
    }

    public String promptPassword(String prompt) throws IOException {
	String pwd = passwordDialog(prompt, "MindTunnel - Password");
	if(pwd == null) {
	    throw new IOException("Login canceled by user");
	}
	return pwd;
    }

    public boolean isVerbose() {
	return verbose;
    }

    public boolean askConfirmation(String message, boolean defAnswer) {
	return confirmDialog(message, defAnswer);
    }

    public void connected(SSHClient client) {
    }

    public void open(SSHClient client) {
	updateButtons();
    }

    public void disconnected(SSHClient client, boolean graceful) {
	updateButtons();
    }

    public void report(String msg) {
	if(verbose) {
	    System.out.println(msg);
	    System.out.println("");
	}
    }

    public void alert(String msg) {
	alertDialog(msg);
    }

    public void windowClosing(WindowEvent e) {
	if(!isConnected()) {
	    parent.dispose();
	    clientThread.stop();
	} else {
	    alertDialog("Please disconnect before exiting!");
	}
    }

    Choice choiceHosts;
    TextField textSrv, textUser, textPasswd;
    Button connBut, discBut, tunnelBut, scpBut, proxyBut;
    public final void showMainWindow() {
	parent = new Frame();
	parent.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e)  { SSHTunnelingClient.this.windowClosing(e); }
	});

	if(listMode && propsHandler.availableAliases() == null) {
	    listMode = false;
	    alertDialog("Must have home directory with aliases to use list mode");
	}

	GridBagLayout       grid  = new GridBagLayout();
	GridBagConstraints  gridc = new GridBagConstraints();

	ActionListener      al;
	Label               lbl;
	Panel               p = new Panel();

	p.setLayout(grid);

	gridc.insets    = new Insets(4, 4, 4, 4);
	gridc.fill      = GridBagConstraints.HORIZONTAL;
	gridc.weightx   = 1.0;
	gridc.gridy     = 0;
	gridc.gridwidth = 1;
	gridc.anchor    = GridBagConstraints.WEST;

	String[] l = propsHandler.availableAliases();
	if(listMode && l != null) {
	    lbl = new Label("SSH Server:");
	    grid.setConstraints(lbl, gridc);
	    p.add(lbl);

	    choiceHosts = new Choice();
	    for(int i = 0; i < l.length; i++) {
		choiceHosts.add(l[i]);
	    }
	    choiceHosts.select(0);
	    grid.setConstraints(choiceHosts, gridc);
	    p.add(choiceHosts);
	    gridc.gridy += 1;
	} else {

	    if(!haveServer) {
		gridc.weightx   = 0;
		lbl = new Label("Server:");
		grid.setConstraints(lbl, gridc);
		p.add(lbl);
		gridc.gridwidth = GridBagConstraints.REMAINDER;
		gridc.weightx   = 1.0;
		textSrv = new TextField("", 16);
		grid.setConstraints(textSrv, gridc);
		p.add(textSrv);
	    }

	    gridc.gridy += 1;
	    gridc.gridwidth = 1;
	    gridc.weightx   = 0;
	    lbl = new Label("Username:");
	    grid.setConstraints(lbl, gridc);
	    p.add(lbl);
	    gridc.gridwidth = GridBagConstraints.REMAINDER;
	    gridc.weightx   = 1.0;
	    textUser = new TextField("", 16);
	    grid.setConstraints(textUser, gridc);
	    p.add(textUser);

	    gridc.gridy += 1;
	    gridc.gridwidth = 1;
	    gridc.weightx   = 0;
	    lbl = new Label("Password:");
	    grid.setConstraints(lbl, gridc);
	    p.add(lbl);
	    gridc.gridwidth = GridBagConstraints.REMAINDER;
	    gridc.weightx   = 1.0;
	    textPasswd = new TextField("", 16);
	    textPasswd.setEchoChar('*');
	    grid.setConstraints(textPasswd, gridc);
	    p.add(textPasswd);

	    if(!haveServer)
		textSrv.setText(propsHandler.getProperty("server"));
	    textUser.setText(propsHandler.getProperty("usrname"));
	}

	al = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Connect")) {
		    if(!listMode) {
			String srv = "dummy";
			if(!haveServer) {
			    srv = textSrv.getText();
			}
			String usr = textUser.getText();
			if(srv == null || srv.trim().length() == 0 ||
			   usr == null || usr.trim().length() == 0) {
			       return;
			   }
		    }
		    synchronized(SSHTunnelingClient.this) {
			SSHTunnelingClient.this.notify();
		    }
		    connBut.setEnabled(false);
		    discBut.requestFocus();
		} else if(e.getActionCommand().equals("Disconnect")) {
		    forcedDisconnect();
		    discBut.setEnabled(false);
		} else if(e.getActionCommand().equals("Tunnels Setup")) {
		    SSHTunnelDialog.show("MindTunnel - Basic Tunnels Setup",
					 SSHTunnelingClient.this, propsHandler, parent);
		} else if(e.getActionCommand().equals("File Transfer")) {
		    SSHSCPDialog.show("MindTunnel - File Transfer", parent,
				      propsHandler, SSHTunnelingClient.this);
		} else if(e.getActionCommand().equals("Configure Proxy")) {
		    SSHProxyDialog.show("MindTunnel - Proxy Settings", parent,
					propsHandler);
		}
	    }
	};

	Panel bp;

	if(haveTunnelDialog || haveSCPDialog || haveProxyDialog) {
	    bp = new Panel(new FlowLayout());
	    if(haveTunnelDialog) {
		tunnelBut = new Button("Tunnels Setup");
		tunnelBut.addActionListener(al);
		bp.add(tunnelBut);
	    }
	    if(haveSCPDialog) {
		scpBut = new Button("File Transfer");
		scpBut.addActionListener(al);
		bp.add(scpBut);
	    }
	    if(haveProxyDialog) {
		proxyBut = new Button("Configure Proxy");
		proxyBut.addActionListener(al);
		bp.add(proxyBut);
	    }
	    gridc.gridy += 1;
	    gridc.gridwidth = GridBagConstraints.REMAINDER;
	    gridc.anchor    = GridBagConstraints.CENTER;
	    grid.setConstraints(bp, gridc);
	    p.add(bp);
	}

	bp = new Panel(new FlowLayout());
	bp.add(connBut = new Button("Connect"));

	connBut.addActionListener(al);

	bp.add(discBut = new Button("Disconnect"));
	discBut.addActionListener(al);

	gridc.gridy += 1;
	gridc.gridwidth = GridBagConstraints.REMAINDER;
	gridc.anchor    = GridBagConstraints.CENTER;
	grid.setConstraints(bp, gridc);
	p.add(bp);

	grid  = new GridBagLayout();
	gridc = new GridBagConstraints();

	parent.setLayout(grid);

	gridc.insets    = new Insets(4, 4, 4, 4);
	gridc.gridy     = 0;
	gridc.gridwidth = GridBagConstraints.REMAINDER;
	gridc.fill      = GridBagConstraints.HORIZONTAL;
	gridc.weightx   = 1.0;
	gridc.anchor    = GridBagConstraints.CENTER;

	Image logoImg = getLogo();
	if(logoImg != null) {
	    Logo logo = new Logo(logoImg);
	    switch(logoPlacement) {
	    case GridBagConstraints.NORTH:
		gridc.fill = GridBagConstraints.NONE;
		grid.setConstraints(logo, gridc);
		parent.add(logo);
		gridc.fill  = GridBagConstraints.HORIZONTAL;
		gridc.gridy = 1;
		grid.setConstraints(p, gridc);
		parent.add(p);
		break;

	    case GridBagConstraints.WEST:
		gridc.fill = GridBagConstraints.NONE;
		gridc.gridwidth = 1;
		grid.setConstraints(logo, gridc);
		parent.add(logo);
		gridc.fill      = GridBagConstraints.HORIZONTAL;
		gridc.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(p, gridc);
		parent.add(p);
		break;

	    case GridBagConstraints.EAST:
		gridc.gridwidth = 1;
		grid.setConstraints(p, gridc);
		parent.add(p);
		gridc.fill      = GridBagConstraints.NONE;
		gridc.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(logo, gridc);
		parent.add(logo);
		break;

	    case GridBagConstraints.SOUTH:
		grid.setConstraints(p, gridc);
		parent.add(p);
		gridc.gridy = 1;
		gridc.fill  = GridBagConstraints.NONE;
		grid.setConstraints(logo, gridc);
		parent.add(logo);
		break;
	    }
	} else {
	    grid.setConstraints(p, gridc);
	    parent.add(p);
	}

	AWTConvenience.setKeyListenerOfChildren(parent,
						new AWTConvenience.OKCancelAdapter(connBut, discBut),
						null);

	AWTConvenience.setBackgroundOfChildren(parent);

	parent.pack();
	parent.show();
    }

    Image getLogo() {
	InputStream is;
	ByteArrayOutputStream baos;
	Image logo = null;

	if((is = this.getClass().getResourceAsStream("/images/logo_top.gif")) != null) {
	    logoPlacement = GridBagConstraints.NORTH;
	} else if((is = this.getClass().getResourceAsStream("/images/logo_left.gif")) != null) {
	    logoPlacement = GridBagConstraints.WEST;
	} else if((is = this.getClass().getResourceAsStream("/images/logo_right.gif")) != null) {
	    logoPlacement = GridBagConstraints.EAST;
	} else if((is = this.getClass().getResourceAsStream("/images/logo_bottom.gif")) != null) {
	    logoPlacement = GridBagConstraints.SOUTH;
	} else {
	    return null;
	}

	baos = new ByteArrayOutputStream();
	try {
	    int c;
	    while((c = is.read()) >= 0)
		baos.write(c);
	    logo = Toolkit.getDefaultToolkit().createImage(baos.toByteArray());
	} catch(IOException e) {
	    // !!!
	}

	return logo;
    }

    public final boolean confirmDialog(String message, boolean defAnswer) {
	return SSHMiscDialogs.confirm("MindTunnel - Confirmation", message, true,
				      parent);
    }

    public final void alertDialog(String message) {
	SSHMiscDialogs.alert("MindTunnel - Alert", message, parent);
    }

    public final String passwordDialog(String message, String title) {
	return SSHMiscDialogs.password(title, message, parent);
    }

}
