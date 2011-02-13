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
 * $Date: 2001/02/07 22:31:13 $
 * $Name:  $
 *****************************************************************************/
// (C) 2004 Yohann Sulaiman. Removed license crap.
package mindbright.ssh;

import java.util.Properties;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import mindbright.terminal.*;
import mindbright.security.*;

import mindbright.net.*;

public final class SSHInteractiveClient extends SSHClient
  implements Runnable, SSHInteractor {

  public static final boolean expires  = false;
  public static final boolean licensed = false;

  public static final String licenseMessage = "This copy of MindTerm is licensed to ";
  public static final String licensee       = "nobody";

//  public static final long validFrom = 965157940452L; // 000801/21:25
  public static final long validFrom = 981400361829L; // 010204/11:13
  public static final long validTime = (33L * 24L * 60L * 60L * 1000L);

  public static boolean wantHelpInfo       = true;
  public static String  customStartMessage = null;

  Thread dumbConsoleThread;

  SSHMenuHandler     menus;
  SSHStdIO           sshStdIO;
  SSHPropertyHandler propsHandler;

  public boolean quiet;
  boolean        initQuiet;

  static public class DumbConsoleThread implements Runnable {
    SSHChannelController controller;
    SSHStdIO             console;

    public DumbConsoleThread(SSHChannelController controller, SSHStdIO console) {
      this.controller = controller;
      this.console    = console;
    }

    public void run() {
      SSHPduOutputStream stdinPdu;
      String line;
      try {
	while(true) {
	  line = console.promptLine("", "", false);
	  stdinPdu = new SSHPduOutputStream(SSH.CMSG_STDIN_DATA, console.sndCipher);
	  stdinPdu.writeString(line + "\n");
	  controller.transmit(stdinPdu);
	  Thread.currentThread().sleep(400);
	}
      } catch (SSHStdIO.CtrlDPressedException e) {
	controller.sendDisconnect("exit");
      } catch (Exception e) {
	controller.alert("Error in console-thread: " + e.toString());
      }
    }
  }

	public SSHStdIO getIO() {
		return sshStdIO;
	}

  public static String copyright() {
    return "Copyright (c) 1998-2000 by Mindbright Technology AB, Stockholm, Sweden\r\n" +
	       "SCP modifications copyright (c) 2001 by ISNetworks, Seattle, WA\r\n"+
	       "SSH v1 Compression copyright (c) 2002 by ymnk, Beijing, China\r\n"+
		"PKT Integration copyright (c) 2004-2005 by Yohann Sulaiman, Toronto, Canada";
  }

  public SSHInteractiveClient(boolean quiet, boolean cmdsh, SSHPropertyHandler propsHandler) {
    super(propsHandler, propsHandler);

    this.propsHandler = propsHandler;
    this.interactor   = this; // !!! OUCH

    propsHandler.setInteractor(this);
    propsHandler.setClient(this);

    this.quiet     = quiet;
    this.initQuiet = quiet;

    setConsole(new SSHStdIO());
    sshStdIO = (SSHStdIO)console;
    sshStdIO.setClient(this);
    sshStdIO.enableCommandShell(cmdsh);
  }

  public SSHInteractiveClient(SSHInteractiveClient clone) {
    this(true, clone.sshStdIO.hasCommandShell(), new SSHPropertyHandler(clone.propsHandler));

    this.activateTunnels = false;

    this.wantHelpInfo       = clone.wantHelpInfo;
    this.customStartMessage = clone.customStartMessage;
  }

  public void setMenus(SSHMenuHandler menus) {
    this.menus = menus;
  }

  public SSHPropertyHandler getPropertyHandler() {
      return propsHandler;
  }

  public void updateMenus() {
    if(menus != null)
      menus.update();
  }

  public void printCopyright() {
    console.println(copyright());

    if(licensed) {
	console.println(licenseMessage + licensee);
    }

    if(customStartMessage != null) {
	console.println(customStartMessage);
    }
  }

  void printHelpInfo() {
    if(!wantHelpInfo)
      return;

    if(propsHandler.getSSHHomeDir() != null)
      console.println("MindTerm home: " + propsHandler.getSSHHomeDir());

    if(sshStdIO.hasCommandShell()) {
      console.println("\tpress <ctrl> + 'D' to enter local command shell");
      if(isDumb())
	console.println("\t(...you might have to press ENTER also...)");
    }
    if(menus != null && menus.havePopupMenu) {
      console.println("\tpress <ctrl> + <mouse-" + menus.getPopupButton() + "> for main-menu");
    }
    console.println("");
  }

  boolean hasExpired() {
    boolean expired = false;
    long now = System.currentTimeMillis();

    if(licensed)
      return false;

      if(expires) {
        int daysRemaining = (int)((validTime - (now - validFrom)) / (1000L * 60L * 60L * 24L));
        if(daysRemaining <= 0) {
//  	console.println("This is a demo version of MindTerm, it has expired!");
//	console.println("Please go to http://www.mindbright.se/mindterm/ to get a copy");
	expired = true;
        } else {
//	console.println("");
//	console.println("This is a demo version of MindTerm, it will expire in " + daysRemaining + " days");
//	console.println("");
        }
    } else {
      int daysOld = (int)((now - validFrom) / (1000L * 60L * 60L * 24L));
//      console.println("");
//      console.println("This is a demo version of MindTerm, it is " + daysOld + " days old.");
//      console.println("Please go to http://www.mindbright.se/mindterm/");
//      console.println("\tto check for new versions of the MindBright release now and then");
//      console.println("Or go to http://www.isnetworks.com/ssh/");
//      console.println("\tto check for new versions of the ISNetworks modifications");
//      console.println("");
    }
    return false;
  }

  void initRandomSeed() {
    console.print("Initializing random number generator, please wait...");
    SSH.initSeedGenerator();
    console.println("done");
  }

  public void doSingleCommand(String commandLine, boolean background, long msTimeout)
    throws IOException {
    boolean haveDumbConsole = (propsHandler.wantPTY() && isDumb());

    initRandomSeed();
    console.println("");

    printHelpInfo();

    this.commandLine = commandLine;

    if(NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
	console.println("Full network access granted, can do tunneling and connect to any host");
      } catch (netscape.security.ForbiddenTargetException e) {
	console.println("Full network access denied, normal applet security applies");
      }
      console.println("");
    }

    bootSSH(false);

    if(haveDumbConsole) {
      startDumbConsole();
    }

    if(background)
      startExitMonitor(msTimeout);
    else
      waitForExit(msTimeout);

    if(haveDumbConsole) {
      stopDumbConsole();
    }
  }

  public void run() {
    boolean doCommandShell;
    boolean gotExtMsg;

    initRandomSeed();

    if(NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
	console.println("Full network access granted, can do tunneling and connect to any host");
      } catch (netscape.security.ForbiddenTargetException e) {
	console.println("Full network access denied, normal applet security applies");
      }
      console.println("");
    }

    if(hasExpired()) {
    }

    boolean keepRunning = true;
    while(keepRunning) {
      doCommandShell = false;
      gotExtMsg      = false;
      try {
	console.println("");
	printHelpInfo();

	// This starts a connection to the sshd and all the related stuff...
	//
	bootSSH(true);

	if(isDumb())
	  startDumbConsole();

	// Join main receiver channel thread and wait for session to end
	//
	controller.waitForExit();

	if(isDumb())
	  stopDumbConsole();

	if(sshStdIO.isConnected()) {
	  // Server died on us without sending disconnect
	  sshStdIO.serverDisconnect("\n\r\n\rServer died or connection lost");
	}

	// !!! Wait for last session to close down entirely (i.e. so
	// disconnected gets a chance to be called...)
	//
	Thread.currentThread().sleep(1000);

	try {
	    propsHandler.checkSave();
	} catch (IOException e) {
	    alert("Error saving settings!");
	}

      } catch(SSHClient.AuthFailException e) {
	console.println("");
	console.println(e.getMessage());
	propsHandler.clearPasswords();

      } catch(WebProxyException e) {
	console.println("");
	console.println(e.getMessage());
	propsHandler.clearPasswords();

      } catch(SSHStdIO.CtrlDPressedException e) {
	doCommandShell = true;

      } catch(SSHStdIO.SSHExternalMessage e) {
	gotExtMsg = true;
	console.println("");
	console.println(e.getMessage());

      } catch(UnknownHostException e) {
	  String host = e.getMessage();
	  if(propsHandler.getProperty("proxytype").equals("none")) {
	      console.println("Unknown host: " + host);
	  } else {
	      console.println("Unknown proxy host: " + host);
	  }
	  propsHandler.clearServerSetting();

      } catch(FileNotFoundException e) {
	console.println("File not found: " + e.getMessage());

      } catch(Exception e) {
	String msg = e.getMessage();
	if(msg == null || msg.trim().length() == 0)
	  msg = e.toString();
	console.println("");
	console.println("Error connecting to " + propsHandler.getProperty("server") + ", reason:");
	console.println("-> " + msg);
	if(SSH.DEBUGMORE) {
	  System.out.println("If an error occured, please send the below stacktrace to mats@mindbright.se");
	  e.printStackTrace();
	}

      } catch(ThreadDeath death) {
	if(controller != null)
	  controller.killAll();
	controller = null;
	throw death;
      }

      propsHandler.passivateProperties();
      activateTunnels = true;
      propsHandler.currentPropsFile = null;

      if(!propsHandler.savePasswords || usedOTP) {
	  propsHandler.clearPasswords();
      }

      if(!gotExtMsg) {
	  if(!propsHandler.autoLoadProps) {
	      propsHandler.clearPasswords();
	      initQuiet = false;
	  }
	  quiet = false;
      }

      controller = null;

      TerminalWin t = getTerminalWin();
      if(t != null)
	t.setTitle(null);

      if(doCommandShell && sshStdIO.hasCommandShell()) {
	keepRunning = sshStdIO.commandShell.doCommandShell();
      }
    }
  }

  public boolean isDumb() {
    return (console.getTerminal() == null);
  }

  public TerminalWin getTerminalWin() {
    Terminal term = console.getTerminal();
    if(term != null && term instanceof TerminalWin)
      return (TerminalWin)term;
    return null;
  }

  public void startDumbConsole() {
    Runnable dumbConsole = new DumbConsoleThread(controller, sshStdIO);
//    dumbConsoleThread = new Thread(dumbConsole);
	    if(SSH.NETSCAPE_SECURITY_MODEL) {
		try {
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
		} catch (netscape.security.ForbiddenTargetException e) {
		    e.printStackTrace();
		    // !!!
		}
	    }
    dumbConsoleThread = new Thread(SSH.getThreadGroup(), dumbConsole); // JH_Mod
    dumbConsoleThread.start();
  }
  public void stopDumbConsole() {
    dumbConsoleThread.stop();
  }

  public void updateTitle() {
    sshStdIO.updateTitle();
  }

  //
  // SSHInteractor interface
  //
  public void propsStateChanged(SSHPropertyHandler props) {
      updateMenus();
  }

  public void startNewSession(SSHClient client) {
      // !!! REMOVE
      // Here we can have a login-dialog with proxy-info also (or configurable more than one method)
      // !!!
  }

  public void sessionStarted(SSHClient client) {
      quiet = initQuiet;
  }

  public boolean quietPrompts() {
      return (commandLine != null || quiet);
  }

  public boolean isVerbose() {
      return wantHelpInfo;
  }

  public String promptLine(String prompt, String defaultVal) throws IOException {
    return sshStdIO.promptLine(prompt, defaultVal, false);
  }

  public String promptPassword(String prompt) throws IOException {
      return sshStdIO.promptLine(prompt, "", true);
  }

  public boolean askConfirmation(String message, boolean defAnswer) {
    boolean confirm = false;
    try {
      confirm = askConfirmation(message, true, defAnswer);
    } catch (IOException e) {
	// !!!
    }
    return confirm;
  }

  public boolean askConfirmation(String message, boolean preferDialog, boolean defAnswer) throws IOException {
    boolean confirm = false;
    if(menus != null && preferDialog) {
      confirm = menus.confirmDialog(message, defAnswer);
    } else {
      String answer = promptLine(message + (defAnswer ? " ([yes]/no) " : "(yes/[no]) "), "");
      if(answer.equalsIgnoreCase("yes") || answer.equals("y")) {
	confirm = true;
      } else if(answer.equals("")) {
	confirm = defAnswer;
      }
    }
    return confirm;
  }

  public void connected(SSHClient client) {
      updateMenus();
      if(wantHelpInfo) {
	  console.println("Connected to server running " + srvVersionStr);
	  if(sshStdIO.hasCommandShell())
	      console.println("(command shell escape-sequence is '" + sshStdIO.commandShell.escapeString() + "')");
	  console.println("");
      }
  }

  public void open(SSHClient client) {
      updateMenus();
      updateTitle();
  }

  public void disconnected(SSHClient client, boolean graceful) {
      sshStdIO.breakPromptLine("Login aborted by user");
      updateMenus();
      updateTitle();
  }

  public void report(String msg) {
      console.println(msg);
      console.println("");
  }

  public void alert(String msg) {
      if(menus != null) {
	  if(msg.length() < 35)
	      menus.alertDialog(msg);
	  else
	      menus.textDialog("MindTerm - Alert", msg, 4, 38, true);
      } else {
	  report(msg);
      }
  }

}
