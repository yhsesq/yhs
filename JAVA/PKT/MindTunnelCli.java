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
 * $Date: 2001/02/03 00:47:00 $
 * $Name:  $
 *****************************************************************************/
package mindbright.application;

import java.applet.Applet;
import java.applet.AppletContext;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Properties;

import mindbright.ssh.SSH;
import mindbright.ssh.SSHClient;
import mindbright.ssh.SSHTunnelingClient;
import mindbright.ssh.SSHPropertyHandler;

public class MindTunnelCli extends Applet implements Runnable {

    static Properties paramSSHProps = new Properties();

    String[] cmdLineArgs;

    String  sshHomeDir  = null;
    String  propsFile   = null;

    String  commandLine = null;
    String  redirURL    = null;
    String  redirTarget = null;

    boolean listMode         = false;
    boolean haveTunnelDialog = false;
    boolean haveSCPDialog    = false;
    boolean haveProxyDialog  = false;

    boolean weAreAnApplet = false;

    public MindTunnelCli() {
	super();
    }

    public static void main(String[] argv) {
	MindTunnelCli controller = new MindTunnelCli();
	controller.cmdLineArgs   = argv;
    
	try {
	    controller.getApplicationParams();
	} catch (Exception e) {
	    System.out.println("Error: " + e.getMessage());
	    System.exit(1);
	}

	try {
	    controller.run();
	} catch (Exception e) {
	    System.out.println("Error, please mail below stack-trace to mats@mindbright.se");
	    e.printStackTrace();
	}
    }

    public void init() {
	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	getAppletParams();
//	(new Thread(this)).start();
	(new Thread(SSH.getThreadGroup(), this)).start(); // JH_Mod
    }

    public void run() {
	try {
	    SSHPropertyHandler propsHandler = new SSHPropertyHandler(paramSSHProps);

	    if(propsFile != null) {
		// !!! REMOVE (todo: fix password!)
		try {
		    propsHandler = SSHPropertyHandler.fromFile(propsFile, "");
		} catch (SSHClient.AuthFailException e) {
		    throw new Exception("Sorry, can only use passwordless settings files for now");
		}
		propsHandler.mergeProperties(paramSSHProps);
	    }

	    SSHTunnelingClient client = new SSHTunnelingClient(propsHandler, this);

	    client.getPropertyHandler().setSSHHomeDir(sshHomeDir);
	    client.verbose = SSH.DEBUG;
	    client.listMode         = listMode;
	    client.haveTunnelDialog = haveTunnelDialog;
	    client.haveSCPDialog    = haveSCPDialog;
	    client.haveProxyDialog  = haveProxyDialog;

	    try {
//		Thread clientThread = new Thread(client);
	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}
		Thread clientThread = new Thread(SSH.getThreadGroup(), client); // JH_Mod
		clientThread.start();
		clientThread.join();
		if(!weAreAnApplet) {
		    System.exit(0);
		}

	    } catch(InterruptedException e) {
		// !!!
	    }

	} catch (Exception e) {
	    System.out.println("Error: " + e.getMessage());
	    e.printStackTrace();
	}
    }

    public void online() {
	if(weAreAnApplet) {
	    if(redirURL != null) {
		try {
		    getAppletContext().showDocument(new URL(redirURL), redirTarget);
		} catch (MalformedURLException e) {
		    getAppletContext().showStatus("MindTunnel Client, malformed URL: " + redirURL);
		}
	    }
	} else {
	    if(commandLine != null) {
		try {
		    Runtime.getRuntime().exec(commandLine);
		} catch (Exception e) {
		    System.out.println("Error running external program: " + commandLine);
		    System.out.println("Error is: " + e);
		}
	    }
	}
    }

    public void getApplicationParams() throws Exception {
	String    name;
	String    value;
	int       numOfOpts;
	int       i;

	// First we check the MindTerm options (i.e. not the ssh/terminal-properties)
	//
	try {
	    for(i = 0; i < cmdLineArgs.length; i++) {
		String arg = cmdLineArgs[i];
		if(!arg.startsWith("--"))
		    break;
		switch(arg.charAt(2)) {
		case 'h':
		    sshHomeDir = cmdLineArgs[++i];
		    break;
		case 'l':
		    listMode = true;
		    break;
		case 'f':
		    propsFile = cmdLineArgs[++i];
		    break;
		case 's':
		    haveSCPDialog = true;
		    break;
		case 't':
		    haveTunnelDialog = true;
		    break;
		case 'v':
		    System.out.println("verbose mode selected...");
		    SSH.DEBUG = true;
		    break;
		case 'y':
		    haveProxyDialog = true;
		    break;
		case 'V':
		    System.out.println(SSH.VER_MINDTERM);
		    System.out.println("SSH protocol version " + SSH.SSH_VER_MAJOR + "." + SSH.SSH_VER_MINOR);
		    System.exit(0);
		    break;
		case 'D':
		    SSH.DEBUG     = true;
		    SSH.DEBUGMORE = true;
		    break;
		case '?':
		    printHelp();
		    System.exit(0);
		default:
		    throw new Exception("unknown parameter '" + arg + "'");
		}
	    }
	} catch (Exception e) {
	    printHelp();
	    throw e;
	}

	numOfOpts = i;
	for(i = numOfOpts; i < cmdLineArgs.length; i += 2) {
	    name = cmdLineArgs[i];
	    if((name.charAt(0) != '-') || ((i + 1) == cmdLineArgs.length))
		break;
	    name  = name.substring(1);
	    value = cmdLineArgs[i + 1];
	    if(SSHPropertyHandler.isProperty(name))
		paramSSHProps.put(name, value);
	    else
		System.out.println("Unknown property '" + name + "'");
	}

	if(i < cmdLineArgs.length) {
	    commandLine = "";
	    for(; i < cmdLineArgs.length; i++) {
		commandLine += cmdLineArgs[i] + " ";
	    }
	    commandLine = commandLine.trim();
	}

    }

    public void getAppletParams() {
	String    name;
	String    value;
	String    param;
	int       i;

	weAreAnApplet = true;

	try {
	    SSH.DEBUG = (new Boolean(getParameter("verbose"))).booleanValue();
	} catch (Exception e) {
	    SSH.DEBUG = false;
	}

	try {
	    haveTunnelDialog = (new Boolean(getParameter("edittunnels"))).booleanValue();
	} catch (Exception e) {
	    haveTunnelDialog = false;
	}

	try {
	    haveSCPDialog = (new Boolean(getParameter("filetransfer"))).booleanValue();
	} catch (Exception e) {
	    haveSCPDialog = false;
	}

	try {
	    haveProxyDialog = (new Boolean(getParameter("configproxy"))).booleanValue();
	} catch (Exception e) {
	    haveProxyDialog = false;
	}

	try {
	    listMode = (new Boolean(getParameter("listmode"))).booleanValue();
	} catch (Exception e) {
	    listMode = false;
	}

	try {
	    SSH.DEBUGMORE = (new Boolean(getParameter("debug"))).booleanValue();
	    SSH.DEBUG = SSH.DEBUGMORE;
	} catch (Exception e) {
	}

	sshHomeDir  = getParameter("sshhome");
	propsFile   = getParameter("propsfile");

	redirURL    = getParameter("redirurl");
	redirTarget = getParameter("redirtarget");

	for(i = 0; i < SSHPropertyHandler.defaultPropDesc.length; i++) {
	    name  = SSHPropertyHandler.defaultPropDesc[i][SSHPropertyHandler.PROP_NAME];
	    value = getParameter(name);
	    if(value != null)
		paramSSHProps.put(name, value);
	}
	i = 0;
	while((value = getParameter("local" + i)) != null) {
	    paramSSHProps.put("local" + i, value);
	    i++;
	}
	i = 0;
	while((value = getParameter("remote" + i)) != null) {
	    paramSSHProps.put("remote" + i, value);
	    i++;
	}
    }

    void printHelp() {
	System.out.println("usage: MindTunnelCli [options] [properties] [command]");
	System.out.println("Options:");
	System.out.println("  --f <file> Use settings from the given file.");
	System.out.println("  --h dir    Name of the MindTerm home-dir (default: ~/mindterm/).");
	System.out.println("  --l        Enables list mode (i.e. lists available settings to choose from).");
	System.out.println("  --s        Enables file transfer dialog. (i.e. scp functionality).");
	System.out.println("  --t        Enables tunnel dialog (i.e. ability to edit tunnels).");
	System.out.println("  --v        Verbose; display verbose messages.");
	System.out.println("  --y        Enables proxy configure dialog (i.e. proxy settings).");
	System.out.println("  --D        Debug; display extra debug info.");
	System.out.println("  --V        Version; display version number only.");
	System.out.println("  --?        Help; display this help.");
    }

}
