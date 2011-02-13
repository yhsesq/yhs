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
package mindbright.ssh;

import java.io.*;
import java.awt.*;

import mindbright.terminal.*;
import mindbright.security.*;

public final class SSHCommandShellImpl implements SSHCommandShell {

  static Toolkit toolkit = Toolkit.getDefaultToolkit();

  SSHStdIO stdio;

  int      escapeIdx;
  String   escapeString = "~$";

  public void setStdIO(SSHStdIO stdio) {
    this.stdio = stdio;
  }

  // One hell of a kludge all this... (a bit better now at least...)
  //
  public String getNextArg(String args) {
    int i = args.indexOf(' ');
    if(i > -1)
      args = args.substring(0, i);
    return args;
  }
  public String[] makeArgv(String cmdLine) {
    String[] argv = new String[32];
    String[] argvRet;
    int n = 0, i;
    while(cmdLine != null) {
      argv[n++] = getNextArg(cmdLine);
      i = cmdLine.indexOf(' ');
      if(i > -1) {
	cmdLine = cmdLine.substring(i);
	cmdLine = cmdLine.trim();
      } else
	cmdLine = null;
    }
    argvRet = new String[n];
    System.arraycopy(argv, 0, argvRet, 0, n);
    return argvRet;
  }
  public void doHelp() {
    stdio.println("The following commands are available:");
    stdio.println("");
    stdio.println("go                                    Start SSH-session with current settings.");
    stdio.println("quit                                  Quit program (or disconnect if connected).");
    stdio.println("add <l|r> [/<plug>/]<port>:<host>:<port>  (see below).");
    stdio.println("del l <local-host>:<listen-port>|*    Delete local forward (* = all).");
    stdio.println("del r <listen-port>|*                 Delete remote forward (* = all).");
    stdio.println("list [ssh | term]                     Lists ssh- and/or terminal-settings.");
    stdio.println("set [<parameter> <value>]             Set value of a ssh-parameter.");
    stdio.println("tset [<parameter> <value>]            Set value of a terminal-parameter.");
    stdio.println("key [<bits>]                          Generate RSA key-pair (of length <bits>).");
    stdio.println("help                                  Display this list, but you knew that :-).");
    stdio.println("");
    stdio.println("(do 'set' without arguments to list parameter-usage)");
    stdio.println("");
    stdio.println("Examples of adding a remote/local tunnel:");
    stdio.println("> add r 4711:www.foo.com:80           Adds a remote tunnel at port 4711 back to");
    stdio.println("                                      www.foo.com port 80 without a plugin,");
    stdio.println("                                      i.e. default tunneling behaviour.");
    stdio.println("> add l /ftp/4711:ftp.bar.com:21      Adds a local tunnel going to ftp.bar.com");
    stdio.println("                                      port 21 using the ftp protocol-plugin to");
    stdio.println("                                      handle protocol specific needs.");
    stdio.println("");
    stdio.println("NOTE: The first character of the command can be used instead of the full word.");
    stdio.println("");
  }
  public void doHelpSet() {
    int i;
    stdio.println("SSH-parameters:");
    stdio.println("");
    for(i = 0; i < stdio.client.propsHandler.defaultPropDesc.length; i++) {
      stdio.println(stdio.client.propsHandler.defaultPropDesc[i][stdio.client.propsHandler.PROP_NAME] + "\t: " +
	      stdio.client.propsHandler.defaultPropDesc[i][stdio.client.propsHandler.PROP_DESC]);
    }
    stdio.println("(to see possible parameter-values use 'list')");
  }
  public void doHelpTSet() {
    int i;
    stdio.println("Terminal-parameters:");
    stdio.println("");
    for(i = 0; i < TerminalDefProps.defaultPropDesc.length; i++) {
      stdio.println(TerminalDefProps.defaultPropDesc[i][TerminalDefProps.PROP_NAME] + "\t: " +
	      TerminalDefProps.defaultPropDesc[i][TerminalDefProps.PROP_DESC]);
    }
    stdio.println("(to see possible parameter-values use 'list')");
  }
  public void doAdd(String[] argv) {
    if(argv.length < 3 || (!argv[1].equals("l") && !argv[1].equals("r")))
      doHelp();
    else {
      try {
	if(argv[1].equals("l"))
	  stdio.client.propsHandler.setProperty("local" + stdio.client.localForwards.size(), argv[2]);
	else
	  stdio.client.propsHandler.setProperty("remote" + stdio.client.remoteForwards.size(), argv[2]);
      } catch (Exception e) {
	doHelp();
      }
    }
  }
  public void doDel(String[] argv) {
    if(argv.length < 3 || (!argv[1].equals("l") && !argv[1].equals("r")))
      doHelp();
    else {
      try {
	int port;
	if(argv[2].equals("*"))
	  port = -1;
	if(argv[1].equals("l")) {
	  int d = argv[2].indexOf(':');
	  String host;
	  host = argv[2].substring(0, d);
	  port = Integer.parseInt(argv[2].substring(d + 1));
	  stdio.client.delLocalPortForward(host, port);
	} else {
	  port = Integer.parseInt(argv[2]);
	  stdio.client.delRemotePortForward(port);
	}
      } catch (Exception e) {
	doHelp();
      }
    }
  }
  public void doListSSH() {
    int i;
    stdio.println("");
    if(stdio.term != null)
      stdio.term.setAttribute(Terminal.ATTR_BOLD, true);
    stdio.println("SSH settings:");
    if(stdio.term != null)
      stdio.term.setAttribute(Terminal.ATTR_BOLD, false);

    for(i = 0; i < stdio.client.propsHandler.defaultPropDesc.length; i++) {
      String propName = stdio.client.propsHandler.defaultPropDesc[i][stdio.client.propsHandler.PROP_NAME];
      String propVal  = stdio.client.propsHandler.getProperty(propName);
      stdio.println(propName + "\t: " + (propVal.equals("") ? "<not set>" : propVal) + " " +
	      stdio.client.propsHandler.defaultPropDesc[i][stdio.client.propsHandler.PROP_ALLOWED]);
    }

    stdio.println("");
    stdio.println("local tunnels:");
    for(i = 0; i < stdio.client.localForwards.size(); i++) {
      SSHClient.LocalForward fwd = (SSHClient.LocalForward) stdio.client.localForwards.elementAt(i);
      stdio.println("\tlocal:  " + fwd.localPort + "\tremote: " + fwd.remoteHost + "/" +
	      fwd.remotePort + " (plugin: " + fwd.plugin + ")");
    }
    if(i == 0)
      stdio.println("\t<none>");
    stdio.println("remote tunnels:");
    for(i = 0; i < stdio.client.remoteForwards.size(); i++) {
      SSHClient.RemoteForward fwd = (SSHClient.RemoteForward) stdio.client.remoteForwards.elementAt(i);
      stdio.println("\tremote: " + fwd.remotePort + "\tlocal:  " + fwd.localHost + "/" +
	      fwd.localPort + " (plugin: " + fwd.plugin + ")");
    }
    if(i == 0)
      stdio.println("\t<none>");
    stdio.println("");

    if(stdio.client.isOpened()) {
      if(stdio.term != null)
	stdio.term.setAttribute(Terminal.ATTR_BOLD, true);
      stdio.println("Currently active tunnels:");
      if(stdio.term != null)
	stdio.term.setAttribute(Terminal.ATTR_BOLD, false);
      String[] list = stdio.controller.listTunnels();
      if(list.length == 0)
	stdio.print("\t<none>");
      else {
	for(i = 0; i < list.length; i++)
	  stdio.println("\t" + list[i]);
      }
      stdio.println("");
    }
  }

  public void doListTerm() {
    int i;
    stdio.println("");

    if(stdio.term != null) {
      TerminalWin termwin = (TerminalWin)stdio.term;
      stdio.term.setAttribute(Terminal.ATTR_BOLD, true);
      stdio.println("Terminal settings:");
      stdio.term.setAttribute(Terminal.ATTR_BOLD, false);

      for(i = 0; i < TerminalDefProps.defaultPropDesc.length; i++) {
	String propName = TerminalDefProps.defaultPropDesc[i][TerminalDefProps.PROP_NAME];
	stdio.println(propName + "\t: " + termwin.getProperty(propName) + " " +
		TerminalDefProps.defaultPropDesc[i][TerminalDefProps.PROP_ALLOWED]);
      }

      stdio.println("");
    }
  }

  public void doSet(String[] argv) {
    if(argv.length < 3) {
      doHelpSet();
    } else {
      try {
	String prm = argv[1];
	String arg = argv[2];
	stdio.client.propsHandler.setProperty(prm, arg);
      } catch (Exception e) {
	stdio.println(e.getMessage());
	stdio.println("(use 'set' without parameters to get help on available parameters)");
      }
    }
  }
  public void doTSet(String[] argv) {
    if(argv.length < 3) {
      doHelpTSet();
    } else {
      String prm = argv[1];
      String arg = argv[2];
      TerminalWin termwin = null;
      if(stdio.term instanceof TerminalWin)
	termwin = (TerminalWin)stdio.term;
      if(termwin != null) {
	try {
	  termwin.setProperty(prm, arg);
	} catch (Exception e) {
	  stdio.println(e.getMessage());
	  stdio.println("(use 'tset' without parameters to get help on available parameters)");
	}
      } else {
	stdio.println("Can't set terminal-parameters in dumb-console mode.");
      }
    }
  }
  public void doGenKey(String[] argv) {
    String fileName, passwd, comment;
    KeyPair kp;
    int     bits = 1024;

    stdio.println("");

    if(argv.length > 1) {
      try {
	bits = Integer.parseInt(argv[1]);
      } catch(Exception e) {
	stdio.println("(invalid <bits>, using default 1024)");
      }
    }

    try {
      stdio.println("The key-pair will be stored in a file with the name you enter.");
      stdio.println("Files are stored in '" + stdio.client.propsHandler.getSSHHomeDir() + "' if no path is given.");
      stdio.println("(note: the public key will also be stored in a file with ext. '.pub')");
      fileName = stdio.promptLine("Filename to save identity in: " , "", false);
      if(!fileName.startsWith(File.separator))
	fileName = stdio.client.propsHandler.getSSHHomeDir() + fileName;
      do {
      passwd   = stdio.promptLine("Password to protect private key: " , "", true);
      } while(!passwd.equals(stdio.promptLine("Password again: " , "", true)));
      comment  = stdio.promptLine("Comment to store in key-files: " , "", false);
      stdio.print("Generating identity of length " + bits + "...");
      try {
	Thread.currentThread().sleep(100); // !!! let the text show... (?)
      } catch (InterruptedException e) {
	// !!!
      }
      kp = SSH.generateRSAKeyPair(bits, SSH.secureRandom());
      stdio.println("done");
      String pks = SSH.generateKeyFiles(kp, fileName, passwd, comment);
      stdio.setSelection(pks);
      stdio.selectionAvailable(true);
    } catch (Exception ee) {
      stdio.println("An error occured while generating key...");
    }
      stdio.println("");
  }

  public boolean doCommandShell() {
    boolean retVal = true;
    stdio.println("");
    stdio.println("...entering local command-shell (type 'h' for help).");
    stdio.println("");
    try {
      boolean keepRunning = true;
      String[] argv;
      while(keepRunning) {
	String cmdLine = stdio.promptLine("mindterm> ", null, false);
	String cmd;
	cmdLine = cmdLine.toLowerCase();
	cmdLine = cmdLine.trim();
	if(cmdLine.equals(""))
	  continue;
	argv = makeArgv(cmdLine);
	cmd  = argv[0];
	if(cmd.equals("l") || cmd.equals("list")) {
	  if(argv.length > 1) {
	    if(argv[1].equals("ssh") || argv[1].equals("term")) {
	      if(argv[1].equals("ssh"))
		doListSSH();
	      else
		doListTerm();
	      stdio.println("(permitted values are in parentheses on the right)");
	      stdio.println("");
	    } else
	      stdio.println("usage: 'list [ssh | term]'");
	  } else {
	    doListSSH();
	    doListTerm();
	    stdio.println("(permitted values are in parentheses on the right)");
	    stdio.println("");
	  }
	} else if(cmd.equals("a") || cmd.equals("add")) {
	doAdd(argv);
	} else if(cmd.equals("d") || cmd.equals("del")) {
	  doDel(argv);
	} else if(cmd.equals("s") || cmd.equals("set")) {
	  doSet(argv);
	} else if(cmd.equals("t") || cmd.equals("ts") || cmd.equals("tset")) {
	  doTSet(argv);
	} else if(cmd.equals("help") || cmd.equals("?") || cmd.equals("h")) {
	  doHelp();
	} else if(cmd.equals("go") || cmd.equals("g")) {
	  retVal      = true;
	  keepRunning = false;
	} else if(cmd.equals("key")) {
	  doGenKey(argv);
	} else if(cmd.equals("q") || cmd.equals("quit")) {
	  retVal      = false;
	  keepRunning = false;
	} else {
	  doHelp();
	}
      }
    } catch (SSHStdIO.CtrlDPressedException e) {
      retVal = true;
    } catch (Exception e) {
      retVal = false;
    }
    return retVal;
  }

  public void launchCommandShell() {
//    (new Thread(new Runnable() {
	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}
    (new Thread(SSH.getThreadGroup(), new Runnable() { // JH_Mod
      public void run() { 
        stdio.isConnected = false;
	if(!doCommandShell()) {
	  stdio.controller.sendDisconnect("exit");
	} else {
	  stdio.isConnected = true;
	  try {
	    stdio.typedChar((char)0x0c);
	  } catch (IOException e) {
	    // !!!
	  }
	}
      }
    })).start();
  }

  public boolean escapeSequenceTyped(char c) {
    if(c == escapeString.charAt(escapeIdx))
      escapeIdx++;
    else
      escapeIdx = 0;
    if(escapeIdx == escapeString.length()) {
      escapeIdx = 0;
      return true;
    }
    return false;
  }

  public String escapeString() {
      return escapeString;
  }

}
