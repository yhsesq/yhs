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

import java.awt.*;
import java.awt.event.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.InetAddress;

import mindbright.application.MindTerm;
import mindbright.security.KeyPair;
import mindbright.security.SecureRandom;
import mindbright.terminal.TerminalWin;
import mindbright.terminal.TerminalMenuListener;
import mindbright.terminal.TerminalMenuHandlerFull;
import mindbright.gui.XProgressBar;
import mindbright.util.AWTConvenience;

public final class SSHMenuHandlerFull extends SSHMenuHandler implements ActionListener,
									ItemListener,
									TerminalMenuListener {
  String aboutText = 
"\n" +
"Copyright (c) 1998-2000 by Mindbright Technology AB, Stockholm, Sweden \n"+
"SCP modifications copyright (c) 2001 by ISNetworks, Seattle, WA \n"+
"SSH v1 Compression copyright (c) 2002 by ymnk, Beijing, China \n"+
"PKT Integration, Security fixes, Cryptographically Secure RNG and code cleanup copyright (c) 2004-2005 by Yohann Sulaiman, Toronto, Canada \n\n"+
"This program is free software; you can redistribute it and/or modify " +
"it under the terms of the GNU General Public License as published by " +
"the Free Software Foundation; either version 2 of the License, or " +
"(at your option) any later version.\n" +
"\n" +
"This program is distributed in the hope that it will be useful, " +
"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
"GNU General Public License for more details.\n" +
"\n" +
"\tOriginal Author:\tMats Andersson (mats@mindbright.se)\n" +
"\tWeb:\thttp://www.mindbright.se/mindterm/\n" +
"\tInfo:\tmindterm@mindbright.se\n\n" +
"\tCVS " + SSH.CVS_NAME + "\n" +
"\tCVS " + SSH.CVS_DATE + "\n" +
"Running on:\n" +
"\tJava vendor:\t" + MindTerm.javaVendor  + "\n" +
"\tJava version:\t" + MindTerm.javaVersion  + "\n" +
"\tOS name:\t" + MindTerm.osName  + "\n" +
"\tOS architecture:\t" + MindTerm.osArch  + "\n" +
"\tOS version:\t" + MindTerm.osVersion  + "\n";

  protected class TunnelEditor extends Panel {
    List      list;
    TextField text;

    public TunnelEditor(String head, ActionListener alAdd, ActionListener alDel) {
      super(new BorderLayout(5, 5));

      Panel  pi;
      Button b;

      add(new Label(head), BorderLayout.NORTH);
      add(list = new List(5, false), BorderLayout.CENTER);
      pi = new Panel(new FlowLayout());
      pi.add(text = new TextField("", 26));
      pi.add(b = new Button("Add"));
      b.addActionListener(alAdd);
      pi.add(b = new Button("Delete"));
      b.addActionListener(alDel);
      add(pi, BorderLayout.SOUTH);
      list.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  text.setText(list.getSelectedItem());
	  text.requestFocus();
	}
      });
    }

    public int getItemCount() {
      return list.getItemCount();
    }

    public String getItem(int i) {
      return list.getItem(i);
    }

    public void addToList(String item) {
      list.add(item);
    }

    public int getSelectedIndex() {
      return list.getSelectedIndex();
    }

    public void selectText() {
      text.selectAll();
    }

    public String getText() {
      return text.getText();
    }

    public void removeAll() {
      list.removeAll();
    }
  }

  SSHInteractiveClient client;
  Frame                parent;
  TerminalWin          term;
  MindTerm             mindterm;

  boolean havePopupMenu = false;

  final static int MENU_FILE     = 0;
  final static int MENU_EDIT     = 1;
  final static int MENU_SETTINGS = 2;
  final static int MENU_TUNNELS  = 3;
  final static int MENU_HELP     = 4;

  final static int M_FILE_NEW     = 1;
  final static int M_FILE_CLONE   = 2;
  final static int M_FILE_CONN    = 3;
  final static int M_FILE_DISC    = 4;
  final static int M_FILE_LOAD    = 6;
  final static int M_FILE_SAVE    = 7;
  final static int M_FILE_SAVEAS  = 8;
  final static int M_FILE_CREATID = 10;
  final static int M_FILE_SCP     = 12;
  final static int M_FILE_CAPTURE = 13;
  final static int M_FILE_SEND    = 14;
  final static int M_FILE_CLOSE   = 16;
  final static int M_FILE_EXIT    = 17;

  final static int M_EDIT_COPY    = 1;
  final static int M_EDIT_PASTE   = 2;
  final static int M_EDIT_CPPASTE = 3;
  final static int M_EDIT_SELALL  = 4;
  final static int M_EDIT_FIND    = 5;
  final static int M_EDIT_CLS     = 7;
  final static int M_EDIT_CLEARSB = 8;
  final static int M_EDIT_VTRESET = 9;

  final static int M_SET_SSH      = 1;
  final static int M_SET_TERM     = 2;
  final static int M_SET_MISC     = 3;
  final static int M_SET_PROXY    = 4;
  final static int M_SET_CMDSH    = 6;
  final static int M_SET_AUTOSAVE = 8;
  final static int M_SET_AUTOLOAD = 9;
  final static int M_SET_SAVEPWD  = 10;

  final static int M_TUNL_SIMPLE   = 1;
  final static int M_TUNL_ADVANCED = 2;
  final static int M_TUNL_CURRENT  = 4;

  final static int M_HELP_TOPICS  = 1;
  final static int M_HELP_ABOUT   = 2;

  final static String[][] menuTexts = {
      { "File", 
	"New Terminal", "Clone Terminal", "Connect...", "Disconnect", null,
	"Load Settings...", "Save Settings", "Save Settings As...", null,
	"Create RSA Identity...", null,
	"SCP File Transfer...", "_Capture To File...", "Send ASCII File...", null, "Close", "Exit"
      },

      { "Edit",
	"Copy Ctrl+Ins", "Paste Shift+Ins", "Copy & Paste", "Select All", "Find...", null,
	"Clear Screen", "Clear Scrollback", "VT Reset"
      },

      { "Settings",
	"SSH Connection...", "Terminal...", "Terminal Misc...", "Proxy...", null,
	"Local Command-Shell", null,
	"_Auto Save Settings", "_Auto Load Settings", "_Save Passwords"
      },

      { "Tunnels",
	"Basic...", "Advanced...", null, "Current Connections..."
      },

      { "Help",
	"Help Topics...", "About MindTerm"
      },
  };

  final static int NO_SHORTCUT = -1;
  final static int[][] menuShortCuts = {
    { NO_SHORTCUT, KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_C, NO_SHORTCUT,
      NO_SHORTCUT, NO_SHORTCUT, KeyEvent.VK_S, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, KeyEvent.VK_E, KeyEvent.VK_X },

    { NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, KeyEvent.VK_A, KeyEvent.VK_F,
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT,
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT },

    { NO_SHORTCUT, KeyEvent.VK_H, KeyEvent.VK_T, KeyEvent.VK_M, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT },

    { NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT },

    { NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, 
      NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT, NO_SHORTCUT },

  };

  Object[][] menuItems;

  public void init(MindTerm mindterm, SSHInteractiveClient client, Frame parent, TerminalWin term) {
    this.mindterm = mindterm;
    this.client   = client;
    this.parent   = parent;
    this.term     = term;
  }

  int popButtonNum = 3;
  public void setPopupButton(int popButtonNum) {
    term.setPopupButton(popButtonNum);
    this.popButtonNum = popButtonNum;
  }

  public int getPopupButton() {
    return popButtonNum;
  }

  Menu getMenu(int idx) {
    Menu m = new Menu(menuTexts[idx][0]);
    int len = menuTexts[idx].length;
    MenuItem mi;
    String   t;

    if(menuItems == null)
      menuItems = new Object[menuTexts.length][];
    if(menuItems[idx] == null)
      menuItems[idx] = new Object[menuTexts[idx].length];

    for(int i = 1; i < len; i++) {
      t = menuTexts[idx][i];
      if(t == null) {
	m.addSeparator();
	continue;
      }
      if(t.charAt(0) == '_') {
	t = t.substring(1);
	mi = new CheckboxMenuItem(t);
	((CheckboxMenuItem)mi).addItemListener(this);
      } else {
	mi = new MenuItem(t);
	mi.addActionListener(this);
      }

      if(menuShortCuts[idx][i] != NO_SHORTCUT) {
	mi.setShortcut(new MenuShortcut(menuShortCuts[idx][i], true));
      }

      menuItems[idx][i] = mi;
      m.add(mi);
    }
    return m;
  }

  int[] mapAction(String action) {
    int[] id = new int[2];
    int i = 0, j = 0;

    for(i = 0; i < menuTexts.length; i++) {
      for(j = 1; j < menuTexts[i].length; j++) {
	String mt = menuTexts[i][j];
	if(mt != null && action.equals(mt)) {
	    id[0] = i;
	    id[1] = j;
	    i = menuTexts.length;
	    break;
	}
      }
    }
    return id;
  }

  public void actionPerformed(ActionEvent e) {
    int[] id = mapAction(((MenuItem)(e.getSource())).getLabel());
    handleMenuAction(id);
  }

  public void itemStateChanged(ItemEvent e) {
    int[] id = mapAction("_" + (String)e.getItem());
    handleMenuAction(id);
  }

  public void handleMenuAction(int[] id) {
    switch(id[0]) {
    case MENU_FILE:
      switch(id[1]) {
      case M_FILE_NEW:
	// mindterm.newWindow();
	break;
      case M_FILE_CLONE:
	// mindterm.cloneWindow();
	break;
      case M_FILE_CONN:
	connectDialog();
	break;
      case M_FILE_DISC:
	  if(mindterm.confirmClose()) {
	      client.forcedDisconnect();
	      client.quiet = client.initQuiet;
	  }
	break;
      case M_FILE_LOAD:
	loadFileDialog();
	break;
      case M_FILE_SAVE:
	try {
	    if(client.propsHandler.savePasswords &&
	       client.propsHandler.emptyPropertyPassword()) {
		String pwd = setPasswordDialog("Please set password for alias " +
					       client.propsHandler.currentAlias,
					       "MindTerm - Set File Password");
		if(pwd == null)
		    return;
		client.propsHandler.setPropertyPassword(pwd);
	    }
	    client.propsHandler.saveCurrentFile();
	} catch (Throwable t) {
	  alertDialog("Error saving settings: " + t.getMessage());
	}
	break;
      case M_FILE_SAVEAS:
	saveAsFileDialog();
	break;
      case M_FILE_CREATID:
	keyGenerationDialog();
	break;
      case M_FILE_SCP:
	  client.quiet = true;
	  SSHSCPDialog.show("MindTerm - File Transfer", parent,
			    client.propsHandler, client);
	break;
      case M_FILE_CAPTURE:
	if(((CheckboxMenuItem)menuItems[MENU_FILE][M_FILE_CAPTURE]).getState()) {
	  if(!captureToFileDialog()) {
	    ((CheckboxMenuItem)menuItems[MENU_FILE][M_FILE_CAPTURE]).setState(false);
	  }
	} else {
	  endCapture();
	}
	break;
      case M_FILE_SEND:
	sendFileDialog();
	break;
      case M_FILE_CLOSE:
	mindterm.close();
	break;
      case M_FILE_EXIT:
	mindterm.exit();
	break;
      }
      break;

    case MENU_EDIT:
      switch(id[1]) {
      case M_EDIT_COPY:
	term.doCopy();
	break;
      case M_EDIT_PASTE:
	term.doPaste();
	break;
      case M_EDIT_CPPASTE:
	term.doCopy();
	term.doPaste();
	break;
      case M_EDIT_SELALL:
	term.selectAll();
	break;
      case M_EDIT_FIND:
	((TerminalMenuHandlerFull)term.getMenus()).findDialog();
	break;
      case M_EDIT_CLS:
	term.clearScreen();
	term.cursorSetPos(0, 0, false);
	break;
      case M_EDIT_CLEARSB:
	term.clearSaveLines();
	break;
      case M_EDIT_VTRESET:
	term.resetInterpreter();
	break;
      }
      break;

    case MENU_SETTINGS:
      switch(id[1]) {
      case M_SET_SSH:
	sshSettingsDialog();
	break;
      case M_SET_TERM:
	((TerminalMenuHandlerFull)term.getMenus()).termSettingsDialog();
	break;
      case M_SET_MISC:
	((TerminalMenuHandlerFull)term.getMenus()).termSettingsDialog2();
	break;
      case M_SET_PROXY:
	SSHProxyDialog.show("MindTerm - Proxy Settings", parent,
			    client.propsHandler);
	break;
      case M_SET_CMDSH:
	client.console.println("");
	client.console.println("** hit a key to enter local command-shell **");
	client.sshStdIO.wantCommandShell();
	break;
      case M_SET_AUTOSAVE:
	client.propsHandler.setAutoSaveProps(
	    ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_AUTOSAVE]).getState());
	update();
	break;
      case M_SET_AUTOLOAD:
	  client.propsHandler.setAutoLoadProps(
	    ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_AUTOLOAD]).getState());
	update();
	break;
      case M_SET_SAVEPWD:
	  client.propsHandler.setSavePasswords(
	    ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_SAVEPWD]).getState());
	  if(client.propsHandler.savePasswords && 
	     client.propsHandler.emptyPropertyPassword() &&
	     client.propsHandler.getAlias() != null) {
	      String pwd = setPasswordDialog("Please set password for alias " +
					     client.propsHandler.currentAlias,
					     "MindTerm - Set File Password");
	      if(pwd == null) {
		  client.propsHandler.setSavePasswords(false);
		  update();
		  return;
	      }
	      client.propsHandler.setPropertyPassword(pwd);
	  }
	break;
      }
      break;

    case MENU_TUNNELS:
      switch(id[1]) {
      case M_TUNL_SIMPLE:
	  SSHTunnelDialog.show("MindTerm - Basic Tunnels Setup",
			       client, client.propsHandler, parent);
	break;
      case M_TUNL_ADVANCED:
	advancedTunnelsDialog();
	break;
      case M_TUNL_CURRENT:
	currentTunnelsDialog();
	break;
      }
      break;

    case MENU_HELP:
      switch(id[1]) {
      case M_HELP_TOPICS:
	alertDialog("No help available yet, be patient :-)");
	break;
      case M_HELP_ABOUT:
	textDialog("About " + SSH.VER_MINDTERM, aboutText, 15, 60, true);
	break;
      }
      break;
    }
  }

  public void update() {
      boolean isOpen      = client.isOpened();
      boolean isConnected = client.isConnected();

      ((MenuItem)menuItems[MENU_SETTINGS][M_SET_CMDSH]).setEnabled(isOpen && client.sshStdIO.hasCommandShell());
      ((MenuItem)menuItems[MENU_FILE][M_FILE_SCP]).setEnabled(isOpen);
      ((MenuItem)menuItems[MENU_FILE][M_FILE_SEND]).setEnabled(isOpen);
      ((MenuItem)menuItems[MENU_FILE][M_FILE_SAVEAS]).setEnabled(isOpen &&
				       (client.propsHandler.getSSHHomeDir() != null));

      ((MenuItem)menuItems[MENU_TUNNELS][M_TUNL_CURRENT]).setEnabled(isOpen);
      ((MenuItem)menuItems[MENU_TUNNELS][M_TUNL_SIMPLE]).setEnabled(isOpen);
      ((MenuItem)menuItems[MENU_TUNNELS][M_TUNL_ADVANCED]).setEnabled(isOpen);

      ((MenuItem)menuItems[MENU_FILE][M_FILE_CONN]).setEnabled(!isConnected);
      ((MenuItem)menuItems[MENU_FILE][M_FILE_DISC]).setEnabled(isConnected);
      ((MenuItem)menuItems[MENU_FILE][M_FILE_LOAD]).setEnabled(!isConnected);

      ((MenuItem)menuItems[MENU_FILE][M_FILE_SAVE]).setEnabled(client.propsHandler.wantSave());

      ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_AUTOSAVE]).setState(client.propsHandler.autoSaveProps);
      ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_AUTOLOAD]).setState(client.propsHandler.autoLoadProps);
      ((CheckboxMenuItem)menuItems[MENU_SETTINGS][M_SET_SAVEPWD]).setState(client.propsHandler.savePasswords);

      boolean selAvail = client.sshStdIO.selectionAvailable;
      ((MenuItem)menuItems[MENU_EDIT][M_EDIT_COPY]).setEnabled(selAvail);
      ((MenuItem)menuItems[MENU_EDIT][M_EDIT_CPPASTE]).setEnabled(selAvail);
  }

  public void prepareMenuBar(MenuBar mb) {
    mb.add(getMenu(0));
    mb.add(getMenu(1));
    mb.add(getMenu(2));
    mb.add(((TerminalMenuHandlerFull)term.getMenus()).getOptionsMenu());
    mb.add(getMenu(3));
    mb.setHelpMenu(getMenu(4));

    update();
  }

  public void preparePopupMenu(PopupMenu popupmenu) {
    havePopupMenu = true;
    popupmenu.add(getMenu(0));
    popupmenu.add(getMenu(1));
    popupmenu.add(getMenu(2));
    popupmenu.add(((TerminalMenuHandlerFull)term.getMenus()).getOptionsMenu());
    popupmenu.add(getMenu(3));
    popupmenu.addSeparator();
    popupmenu.add(getMenu(4));
    update();
  }

  Dialog     settingsDialog = null;
  Choice     choiceCipher, choiceAuthTyp;
  Choice     choiceCompressionLevel;
  Checkbox   cbX11, cbPrvPrt, cbRemFwd, cbIdHost, cbPortFtp, cbLocHst, cbMTU, cbAlive, cbForcPty;
  TextField  textPort, textUser, textId, textDisp, textMtu, textAlive, textSrv,
      textRealAddr, textAuthList, textLocHost;
  Label      lblAlert;
  String[]   cipher, authtyp, bool;
  FileDialog idFileFD;
  Button     idFileBut, advButton;
  boolean    newServer, advanced = false;
  Panel      ap;
  public final void sshSettingsDialog() {
    int   i;

    if(settingsDialog == null) {
      cipher  = SSH.getCiphers();
      authtyp = SSH.getAuthTypeList();

      settingsDialog = new Dialog(parent, true);

      Label              lbl;
      GridBagLayout      grid  = new GridBagLayout();
      GridBagConstraints gridc = new GridBagConstraints();
      settingsDialog.setLayout(grid);
      Button             okBut, cancBut;
      ItemListener       il;

      gridc.fill   = GridBagConstraints.HORIZONTAL;
      gridc.anchor = GridBagConstraints.WEST;
      gridc.gridwidth = 1;
      gridc.insets = new Insets(4, 4, 0, 4);

      gridc.gridy = 0;
      lbl = new Label("Server:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      gridc.gridwidth = 3;
      textSrv = new TextField("", 16);
      grid.setConstraints(textSrv, gridc);
      settingsDialog.add(textSrv);
      gridc.gridwidth = 1;
      lbl = new Label("Port:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      textPort = new TextField("", 4);
      grid.setConstraints(textPort, gridc);
      settingsDialog.add(textPort);

      gridc.gridy = 1;
      gridc.gridwidth = 2;
      lbl = new Label("Username:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      textUser = new TextField("", 10);
      grid.setConstraints(textUser, gridc);
      settingsDialog.add(textUser);
    
      lbl = new Label("Cipher:");
      gridc.gridwidth = 1;
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      choiceCipher = new Choice();
      for(i = 0; i < cipher.length; i++) {
	choiceCipher.add(cipher[i]);
      }
      grid.setConstraints(choiceCipher, gridc);
      settingsDialog.add(choiceCipher);

      gridc.gridy = 4;
      gridc.gridwidth = 2;
      lbl = new Label("Authentication:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      choiceAuthTyp = new Choice();
      for(i = 0; i < authtyp.length; i++) {
	choiceAuthTyp.add(authtyp[i]);
      }
      gridc.gridwidth = 1;
      grid.setConstraints(choiceAuthTyp, gridc);
      settingsDialog.add(choiceAuthTyp);
      choiceAuthTyp.addItemListener(il = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  updateChoices();
	}
      });
      gridc.gridwidth = 3;
      textAuthList = new TextField("", 10);
      grid.setConstraints(textAuthList, gridc);
      settingsDialog.add(textAuthList);

      gridc.gridy = 5;
      gridc.gridwidth = 2;
      lbl = new Label("Identity:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      gridc.gridwidth = 3;
      textId = new TextField("", 16);
      grid.setConstraints(textId, gridc);
      settingsDialog.add(textId);
      idFileBut = new Button("...");
      idFileBut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if(idFileFD == null) {
	    idFileFD = new FileDialog(parent, "MindTerm - Select file with identity (private)", FileDialog.LOAD);
if(client.propsHandler.getSSHHomeDir() != null){
idFileFD.setDirectory(client.propsHandler.getSSHHomeDir());}else{
idFileFD.setDirectory("/");}
	  }
	  idFileFD.setVisible(true);
	  if(idFileFD.getFile() != null && idFileFD.getFile().length() > 0)
	    textId.setText(idFileFD.getDirectory() + idFileFD.getFile());
	}
      });
      gridc.fill      = GridBagConstraints.NONE;
      gridc.gridwidth = 1;
      grid.setConstraints(idFileBut, gridc);
      settingsDialog.add(idFileBut);

      gridc.gridy = 6;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.fill      = GridBagConstraints.NONE;
      gridc.anchor = GridBagConstraints.EAST;
      gridc.insets = new Insets(4, 0, 4, 8);

      Panel bp = new Panel(new FlowLayout());
      Button prxBut = new Button("Configure Proxy");
      prxBut.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	      SSHProxyDialog.show("MindTerm - Proxy Settings", parent,
				  client.propsHandler);
	  }
      });
      bp.add(prxBut);

      advButton = new Button("More options...");
      advButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if(advanced) {
	    advButton.setLabel("More options...");
	    settingsDialog.remove(ap);
	  } else {
	    advButton.setLabel("Less options...");
	    settingsDialog.add(ap);
	  }
	  settingsDialog.pack();
	  advanced = !advanced;
	}
      });
      bp.add(advButton);

      grid.setConstraints(bp, gridc);
      settingsDialog.add(bp);



      ap = new Panel();
      GridBagLayout      grid2  = new GridBagLayout();
      GridBagConstraints gridc2 = new GridBagConstraints();
      gridc2.fill = GridBagConstraints.NONE;
      gridc2.gridwidth = 2;
      gridc2.anchor = GridBagConstraints.WEST;
      gridc2.insets = new Insets(4, 4, 0, 4);
      ap.setLayout(grid2);

      gridc2.gridy = 0;
      gridc2.gridwidth = 1;
      cbX11 = new Checkbox("X11 forward");
      grid2.setConstraints(cbX11, gridc2);
      ap.add(cbX11);
      cbX11.addItemListener(il);
      lbl = new Label("Local X11-display:");
      gridc2.gridwidth = 3;
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      textDisp = new TextField("", 12);
      gridc2.gridwidth = 1;
      grid2.setConstraints(textDisp, gridc2);
      ap.add(textDisp);

      gridc2.gridy = 1;
      cbMTU = new Checkbox("Set MTU");
      gridc2.gridwidth = 1;
      grid2.setConstraints(cbMTU, gridc2);
      ap.add(cbMTU);
      lbl = new Label("Max. packet size:");
      gridc2.gridwidth = 3;
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      textMtu = new TextField("", 12);
      gridc2.gridwidth = 1;
      grid2.setConstraints(textMtu, gridc2);
      ap.add(textMtu);
      cbMTU.addItemListener(il);

      gridc2.gridy = 2;
      cbAlive = new Checkbox("Send keep-alive");
      gridc2.gridwidth = 1;
      grid2.setConstraints(cbAlive, gridc2);
      ap.add(cbAlive);
      lbl = new Label("Interval (seconds):");
      gridc2.gridwidth = 3;
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      textAlive = new TextField("", 12);
      gridc2.gridwidth = 1;
      grid2.setConstraints(textAlive, gridc2);
      ap.add(textAlive);
      cbAlive.addItemListener(il);

      gridc2.gridy = 3;
      cbPortFtp = new Checkbox("Enable ftp PORT");
      gridc2.gridwidth = 1;
      grid2.setConstraints(cbPortFtp, gridc2);
      ap.add(cbPortFtp);
      lbl = new Label("Real sshd address:");
      gridc2.gridwidth = 3;
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      textRealAddr = new TextField("", 12);
      gridc2.gridwidth = 1;
      grid2.setConstraints(textRealAddr, gridc2);
      ap.add(textRealAddr);
      cbPortFtp.addItemListener(il);

      gridc2.gridy = 4;
      cbLocHst = new Checkbox("Set localhost");
      gridc2.gridwidth = 1;
      grid2.setConstraints(cbLocHst, gridc2);
      ap.add(cbLocHst);
      lbl = new Label("Localhost address:");
      gridc2.gridwidth = 3;
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      textLocHost = new TextField("", 12);
      gridc2.gridwidth = 1;
      grid2.setConstraints(textLocHost, gridc2);
      ap.add(textLocHost);
      cbLocHst.addItemListener(il);

      gridc2.gridy = 5;
      gridc2.gridwidth = 2;
      gridc2.insets = new Insets(16, 4, 0, 4);

      cbIdHost = new Checkbox("Verify server key");
      grid2.setConstraints(cbIdHost, gridc2);
      ap.add(cbIdHost);

      cbForcPty = new Checkbox("Allocate PTY");
      grid2.setConstraints(cbForcPty, gridc2);
      ap.add(cbForcPty);

      gridc2.gridy = 6;
      cbPrvPrt = new Checkbox("Priv. source port");
      grid2.setConstraints(cbPrvPrt, gridc2);
      ap.add(cbPrvPrt);

      gridc2.gridwidth = 3;
      cbRemFwd = new Checkbox("Allow remote connects");
      grid2.setConstraints(cbRemFwd, gridc2);
      ap.add(cbRemFwd);
gridc2.gridy = 7;
      lbl = new Label("CompressionLevel:");
      grid2.setConstraints(lbl, gridc2);
      ap.add(lbl);
      choiceCompressionLevel = new Choice();
      for(i = 0; i < 10; i++) {
	choiceCompressionLevel.add(new Integer(i).toString());
      }
      grid2.setConstraints(choiceCompressionLevel, gridc2);
      ap.add(choiceCompressionLevel);

      gridc.gridy = 7;
      gridc.insets = new Insets(0, 0, 0, 0);
      gridc.anchor = GridBagConstraints.CENTER;
      gridc.fill = GridBagConstraints.NONE;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      grid.setConstraints(ap, gridc);

      gridc.insets = new Insets(0, 0, 0, 0);
      lblAlert = new Label("", Label.CENTER);
      gridc.gridy = 12;
      gridc.fill   = GridBagConstraints.HORIZONTAL;
      grid.setConstraints(lblAlert, gridc);
      settingsDialog.add(lblAlert);

      bp = new Panel(new FlowLayout());

      bp.add(okBut = new Button("OK"));
      okBut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  try {
	    String host = null;
	    if(newServer) {
	      host = textSrv.getText();
	      if(host.length() == 0) {
		lblAlert.setText("Please specify a server to connect to");
		return;
	      }
	      client.quiet = true;
	      client.propsHandler.setProperty("server", host);
	      client.propsHandler.setAlias(host);
	      String prxPasswd = client.propsHandler.getProperty("prxpassword");
	      client.propsHandler.clearPasswords();
	      if(prxPasswd != null)
		  client.propsHandler.setProperty("prxpassword", prxPasswd);
	      client.propsHandler.clearAllForwards();
	    }

	    String authType = choiceAuthTyp.getSelectedItem();
	    if(authType.equals("custom...")) {
	      client.propsHandler.setProperty("authtyp", textAuthList.getText());
	    } else {
	      client.propsHandler.setProperty("authtyp", authType);
	    }
String level = choiceCompressionLevel.getSelectedItem();
	    client.propsHandler.setProperty("compression", level);
	    client.propsHandler.setProperty("port", textPort.getText());
	    client.propsHandler.setProperty("usrname", textUser.getText());
	    client.propsHandler.setProperty("cipher", cipher[choiceCipher.getSelectedIndex()]);
	    client.propsHandler.setProperty("idfile", textId.getText());
	    client.propsHandler.setProperty("display", textDisp.getText());
	    client.propsHandler.setProperty("mtu", textMtu.getText());
	    client.propsHandler.setProperty("x11fwd", String.valueOf(cbX11.getState()));
	    client.propsHandler.setProperty("prvport", String.valueOf(cbPrvPrt.getState()));
	    client.propsHandler.setProperty("remfwd", String.valueOf(cbRemFwd.getState()));
	    client.propsHandler.setProperty("idhost", String.valueOf(cbIdHost.getState()));
	    client.propsHandler.setProperty("forcpty", String.valueOf(cbForcPty.getState()));
	    client.propsHandler.setProperty("portftp", String.valueOf(cbPortFtp.getState()));
	    client.propsHandler.setProperty("localhst", String.valueOf(textLocHost.getText()));
	    if(cbPortFtp.getState())
	      client.propsHandler.setProperty("realsrv", textRealAddr.getText());
	    else
	      client.propsHandler.setProperty("realsrv", "");
	    client.propsHandler.setProperty("alive", textAlive.getText());

	    if(client.propsHandler.savePasswords) {
		String pwd = setPasswordDialog("Please set password for alias " + host,
					       "MindTerm - Set File Password");
		if(pwd == null)
		    return;
		client.propsHandler.setPropertyPassword(pwd);
	    }

	    if(newServer)
	      client.sshStdIO.breakPromptLine("Connecting to: " + host);

	    settingsDialog.setVisible(false);
	  } catch (Exception ee) {
	    lblAlert.setText(ee.getMessage());
	  }
	}
      });
      bp.add(cancBut = new Button("Cancel"));
      cancBut.addActionListener(new AWTConvenience.CloseAction(settingsDialog));

      gridc.gridy = 13;
      grid.setConstraints(bp, gridc);
      settingsDialog.add(bp);

      settingsDialog.addWindowListener(new AWTConvenience.CloseAdapter(cancBut));

      AWTConvenience.setBackgroundOfChildren(settingsDialog);
      AWTConvenience.setKeyListenerOfChildren(settingsDialog,
					      new AWTConvenience.OKCancelAdapter(okBut, cancBut),
					      null);

      settingsDialog.setResizable(true);
      settingsDialog.pack();
    }

    String srv = client.propsHandler.getProperty("server");
    if((srv != null && srv.length() == 0) || !client.isConnected) {
      textSrv.setEnabled(true);
      settingsDialog.setTitle("MindTerm - New Server");
      newServer = true;
      client.propsHandler.clearServerSetting();
    } else {
      textSrv.setEnabled(false);
      newServer = false;
      settingsDialog.setTitle("MindTerm - SSH Settings: " + client.propsHandler.currentAlias);
    }
    textSrv.setText(srv);

    textPort.setText(client.propsHandler.getProperty("port"));

    textUser.setText(client.propsHandler.getProperty("usrname"));

    choiceCipher.select(client.propsHandler.getProperty("cipher"));

    String at = client.propsHandler.getProperty("authtyp");
    if(at.indexOf(',') == -1) {
      choiceAuthTyp.select(at);
    } else {
      choiceAuthTyp.select("custom...");
      textAuthList.setText(at);
    }

at = client.propsHandler.getProperty("compression");
    choiceCompressionLevel.select(at);

    textId.setText(client.propsHandler.getProperty("idfile"));

    textDisp.setText(client.propsHandler.getProperty("display"));
    textMtu.setText(client.propsHandler.getProperty("mtu"));
    textAlive.setText(client.propsHandler.getProperty("alive"));

    InetAddress realAddr = client.getServerRealAddr();
    if(realAddr != null)
      textRealAddr.setText(realAddr.getHostAddress());

    cbX11.setState(Boolean.valueOf(client.propsHandler.getProperty("x11fwd")).booleanValue());
    cbMTU.setState(!client.propsHandler.getProperty("mtu").equals("0"));
    cbAlive.setState(!client.propsHandler.getProperty("alive").equals("0"));

    cbLocHst.setState(!client.propsHandler.getProperty("localhst").equals("0.0.0.0"));
    textLocHost.setEnabled(false);

    cbPrvPrt.setState(Boolean.valueOf(client.propsHandler.getProperty("prvport")).booleanValue());
    cbRemFwd.setState(Boolean.valueOf(client.propsHandler.getProperty("remfwd")).booleanValue());
    cbIdHost.setState(Boolean.valueOf(client.propsHandler.getProperty("idhost")).booleanValue());
    cbPortFtp.setState(Boolean.valueOf(client.propsHandler.getProperty("portftp")).booleanValue());
    cbForcPty.setState(Boolean.valueOf(client.propsHandler.getProperty("forcpty")).booleanValue());

    updateChoices();

    lblAlert.setText("");

    AWTConvenience.placeDialog(settingsDialog);
    if(textSrv.isEnabled())
      textSrv.requestFocus();
    else
      textUser.requestFocus();
    settingsDialog.setVisible(true);
  }

  void updateChoices() {
    String at = choiceAuthTyp.getSelectedItem();
    if(at.equals("rsa") || at.equals("rhostsrsa") || at.equals("custom...")) {
      textId.setEnabled(true);
      idFileBut.setEnabled(true);
    } else {
      textId.setEnabled(false);
      idFileBut.setEnabled(false);
    }
    if(at.equals("custom...")) {
      textAuthList.setEnabled(true);
    } else {
      textAuthList.setEnabled(false);
      textAuthList.setText("");
    }

    textDisp.setEnabled(cbX11.getState());
    textMtu.setEnabled(cbMTU.getState());
    if(!cbMTU.getState())
      textMtu.setText("0");

    if(!textAlive.isEnabled() && cbAlive.getState()) {
      textAlive.setText("10");
    } else if(!textAlive.isEnabled()) {
      textAlive.setText("0");
    }
    textAlive.setEnabled(cbAlive.getState());

    textRealAddr.setEnabled(cbPortFtp.getState());

    if(cbLocHst.getState()) {
      if(!textLocHost.isEnabled())
	textLocHost.setText(client.propsHandler.getProperty("localhst"));
    } else {
      textLocHost.setText("0.0.0.0");
    }
    textLocHost.setEnabled(cbLocHst.getState());
  }

  Dialog currentTunnelsDialog = null;
  List   currList;
  public final void currentTunnelsDialog() {
    if(currentTunnelsDialog == null) {
      currentTunnelsDialog = new Dialog(parent, "MindTerm - Currently Open Tunnels", false);

      GridBagLayout       grid  = new GridBagLayout();
      GridBagConstraints  gridc = new GridBagConstraints();
      Label               label;
      Button              b;
      ActionListener      al;

      currentTunnelsDialog.setLayout(grid);

      gridc.fill      = GridBagConstraints.NONE;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.gridy     = 0;
      gridc.insets    = new Insets(10, 10, 0, 10);
      gridc.gridwidth = 2;

      label = new Label("Currently open tunnels:");
      grid.setConstraints(label, gridc);
      currentTunnelsDialog.add(label);

      gridc.fill      = GridBagConstraints.BOTH;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.insets    = new Insets(10, 10, 10, 10);
      gridc.gridy     = 1;
      gridc.gridwidth = 10;

      currList = new List(8);
      grid.setConstraints(currList, gridc);
      currentTunnelsDialog.add(currList);

      Panel bp = new Panel(new FlowLayout());
      bp.add(b = new Button("Close Tunnel"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  int i = currList.getSelectedIndex();
	  if(i == -1) {
	    term.doBell();
	    return;
	  }
	  // !!! Ouch !!!
	  client.controller.closeTunnelFromList(i);
	  Thread.yield();
	  refreshCurrList();
	}
      });
      bp.add(b = new Button("Refresh"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  refreshCurrList();
	}
      });
      bp.add(b = new Button("Close Dialog"));
      b.addActionListener(new AWTConvenience.CloseAction(currentTunnelsDialog));

      gridc.gridy     = 2;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(bp, gridc);
      currentTunnelsDialog.add(bp);

      currentTunnelsDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(currentTunnelsDialog);

      currentTunnelsDialog.setResizable(true);
      currentTunnelsDialog.pack();
    }
    refreshCurrList();

    AWTConvenience.placeDialog(currentTunnelsDialog);
    currList.requestFocus();
    currentTunnelsDialog.setVisible(true);
  }

  void refreshCurrList() {
    currList.removeAll();
    String[] l = client.controller.listTunnels();
    for(int i = 0; i < l.length; i++) {
      currList.add(l[i]);
    }
    if(l.length > 0)
      currList.select(0);
  }

  TunnelEditor localEdit    = null;
  TunnelEditor remoteEdit   = null;
  Dialog       tunnelDialog = null;
  public final void advancedTunnelsDialog() {
    if(tunnelDialog == null) {
      tunnelDialog = new Dialog(parent, "MindTerm - Advanced Tunnels Setup", true);

      GridBagLayout       grid  = new GridBagLayout();
      GridBagConstraints  gridc = new GridBagConstraints();
      tunnelDialog.setLayout(grid);

      gridc.fill      = GridBagConstraints.BOTH;
      gridc.weightx   = 1.0;
      gridc.weighty   = 1.0;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.insets    = new Insets(4, 8, 4, 8);

      gridc.gridy     = 0;

      localEdit  = new TunnelEditor("Local: ([/plug/][<loc-host>]:<loc-port>:<rem-host>:<rem-port>)",
				    new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    try {
						client.propsHandler.setProperty("local" + client.localForwards.size(), localEdit.getText());
						updateAdvancedTunnelLists();
					    } catch (Exception e1) {
						localEdit.selectText();
					    }
					}
				    },
				    new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    int i = localEdit.getSelectedIndex();
					    if(i != -1) {
						client.propsHandler.removeLocalTunnelAt(i, true);
						updateAdvancedTunnelLists();
					    }
					}
				    });

      grid.setConstraints(localEdit, gridc);
      tunnelDialog.add(localEdit);

      gridc.gridy     = 1;

      remoteEdit = new TunnelEditor("Remote: ([/plug/]<rem-port>:<loc-host>:<loc-port>)",
				    new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    try {
						client.propsHandler.setProperty("remote" + client.remoteForwards.size(), remoteEdit.getText());
						updateAdvancedTunnelLists();
					    } catch (Exception e2) {
						remoteEdit.selectText();
					    }
					}
				    },
				    new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					    int i = remoteEdit.getSelectedIndex();
					    if(remoteEdit.getItem(i).indexOf(SSHFtpTunnel.TUNNEL_NAME) != -1) {
					      return;
					    }
					    if(i != -1) {
						client.propsHandler.removeRemoteTunnelAt(i);
						updateAdvancedTunnelLists();
					    }
					}
				    });

      grid.setConstraints(remoteEdit, gridc);
      tunnelDialog.add(remoteEdit);

      Button b;
      b = new Button("Close Dialog");
      b.addActionListener(new AWTConvenience.CloseAction(tunnelDialog));
      gridc.gridy     = 2;
      gridc.anchor = GridBagConstraints.CENTER;
      gridc.fill = GridBagConstraints.NONE;
      gridc.weighty   = 0;
      grid.setConstraints(b, gridc);
      tunnelDialog.add(b);

      tunnelDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(tunnelDialog);

      tunnelDialog.setResizable(true);
      tunnelDialog.pack();
    }

    updateAdvancedTunnelLists();
    AWTConvenience.placeDialog(tunnelDialog);
    tunnelDialog.setVisible(true);
  }

  void updateAdvancedTunnelLists() {
    String plugStr;
    int    i;
    localEdit.removeAll();
    remoteEdit.removeAll();
    for(i = 0; i < client.localForwards.size(); i++) {
      SSHClient.LocalForward fwd = (SSHClient.LocalForward) client.localForwards.elementAt(i);
      plugStr = (fwd.plugin.equals("general") ? "" : "/" + fwd.plugin + "/");
      localEdit.addToList(plugStr + fwd.localHost + ":" + fwd.localPort + ":" +
			  fwd.remoteHost + ":" + fwd.remotePort);
    }

    for(i = 0; i < client.remoteForwards.size(); i++) {
      SSHClient.RemoteForward fwd = (SSHClient.RemoteForward) client.remoteForwards.elementAt(i);
      plugStr = (fwd.plugin.equals("general") ? "" : "/" + fwd.plugin + "/");
      remoteEdit.addToList(plugStr + fwd.remotePort + ":" + fwd.localHost + ":" + fwd.localPort);
    }
  }

  Dialog  connectDialog = null;
  List    hostList;
  boolean wantToRunSettingsDialog = false;
  public final void connectDialog() {
    if(connectDialog == null) {
      connectDialog = new Dialog(parent, "MindTerm - Connect", true);

      GridBagLayout       grid  = new GridBagLayout();
      GridBagConstraints  gridc = new GridBagConstraints();
      Label               label;
      Button              b;
      ActionListener      al;

      connectDialog.setLayout(grid);

      gridc.fill      = GridBagConstraints.NONE;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.gridwidth = 2;
      gridc.gridy     = 0;
      gridc.insets    = new Insets(8, 8, 0, 8);

      label = new Label("Available hosts/aliases:");
      grid.setConstraints(label, gridc);
      connectDialog.add(label);

      gridc.gridy     = 1;
      label = new Label("(dir: " + client.propsHandler.getSSHHomeDir() + ")");
      grid.setConstraints(label, gridc);
      connectDialog.add(label);

      gridc.fill      = GridBagConstraints.BOTH;
      gridc.weightx   = 1.0;
      gridc.weighty   = 1.0;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.insets    = new Insets(8, 8, 8, 8);
      gridc.gridy     = 2;

      hostList = new List(8);
      grid.setConstraints(hostList, gridc);
      connectDialog.add(hostList);

      hostList.addActionListener(al = new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	      String host = hostList.getSelectedItem();
	      try {
		  String pwd = "";
		  do {
		      try {
			  client.propsHandler.setPropertyPassword(pwd);
			  client.propsHandler.loadAliasFile(host, false);
			  client.quiet = true;
			  client.sshStdIO.breakPromptLine("Connecting to: " + host);
			  connectDialog.setVisible(false);
			  break;
		      } catch(SSHClient.AuthFailException ee) {
		      }
		  } while((pwd = passwordDialog("Please give file password for " +
						host, "MindTerm - File Password")) != null);
	      } catch (Throwable t) {
		  alertDialog("Error loading settings: " + t.getMessage());
	      }
	  }
      });

      Panel bp = new Panel(new FlowLayout());
      bp.add(b = new Button("Connect"));
      b.addActionListener(al);
      bp.add(b = new Button("New Server"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  connectDialog.setVisible(false);
	  try {
	    client.propsHandler.checkSave();
	  } catch (Throwable t) {
	    alertDialog("Error saving settings: " + t.getMessage());
	  }
	  client.propsHandler.clearServerSetting();
	  wantToRunSettingsDialog = true;
	  connectDialog.setVisible(false);
	}
      });

      bp.add(b = new Button("Cancel"));
      b.addActionListener(new AWTConvenience.CloseAction(connectDialog));

      gridc.gridy     = 4;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.weightx   = 1.0;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(bp, gridc);
      connectDialog.add(bp);

      connectDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(connectDialog);

      connectDialog.setResizable(true);
      connectDialog.pack();
    }
    hostList.removeAll();

    String[] l = client.propsHandler.availableAliases();

    if(l != null) {
      for(int i = 0; i < l.length; i++) {
	hostList.add(l[i]);
      }
    }
    hostList.select(0);
    connectDialog.pack();

    AWTConvenience.placeDialog(connectDialog);
    hostList.requestFocus();
    connectDialog.setVisible(true);

    if(wantToRunSettingsDialog) {
      wantToRunSettingsDialog = false;
      sshSettingsDialog();
    }

  }

  FileDialog loadFileDialog = null;
  public final void loadFileDialog() {
    if(loadFileDialog == null) {
      loadFileDialog = new FileDialog(parent, "MindTerm - Select file to load settings from", FileDialog.LOAD);
    }
    loadFileDialog.setDirectory(client.propsHandler.getSSHHomeDir());
    loadFileDialog.setVisible(true);

    String fileName = loadFileDialog.getFile();
    String dirName  = loadFileDialog.getDirectory();
    if(fileName != null && fileName.length() > 0) {
      try {
	  String pwd = "";
	  do {
	      try {
		  client.propsHandler.setPropertyPassword(pwd);
		  client.propsHandler.loadAbsoluteFile(dirName + fileName, false);
		  client.quiet = true;
		  client.sshStdIO.breakPromptLine("Loaded new settings: " + fileName);
		  break;
	      } catch(SSHClient.AuthFailException ee) {
	      }
	  } while((pwd = passwordDialog("Please give password for " +
					fileName, "MindTerm - File Password")) != null);
      } catch (Throwable t) {
	alertDialog("Error loading settings: " + t.getMessage());
      }
    }
  }

  FileDialog saveAsFileDialog = null;
  public final void saveAsFileDialog() {
    if(saveAsFileDialog == null) {
      saveAsFileDialog = new FileDialog(parent, "MindTerm - Select file to save settings to", FileDialog.SAVE);
    }
    saveAsFileDialog.setDirectory(client.propsHandler.getSSHHomeDir());
    String fname = client.propsHandler.currentAlias;
    if(fname == null)
	fname = client.propsHandler.getProperty("server");
    saveAsFileDialog.setFile(fname + client.propsHandler.PROPS_FILE_EXT);
    saveAsFileDialog.setVisible(true);

    String fileName = saveAsFileDialog.getFile();
    String dirName  = saveAsFileDialog.getDirectory();

    if(fileName != null && fileName.length() > 0) {
      try {
	  if(client.propsHandler.savePasswords) {
	      String pwd = setPasswordDialog("Please set password for " + fileName,
					     "MindTerm - Set File Password");
	      if(pwd == null)
		  return;
	      client.propsHandler.setPropertyPassword(pwd);
	  }
	  client.propsHandler.saveAsCurrentFile(dirName + fileName);
      } catch (Throwable t) {
	alertDialog("Error saving settings: " + t.getMessage());
      }
    }
  }

  FileDialog sendFileDialog = null;
  public final void sendFileDialog() {
    if(sendFileDialog == null) {
      sendFileDialog = new FileDialog(parent, "MindTerm - Select ASCII-file to send", FileDialog.LOAD);
    }
    sendFileDialog.setVisible(true);

    if(SSH.NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
      } catch (netscape.security.ForbiddenTargetException e) {
	// !!!
      }
    }

    String fileName = sendFileDialog.getFile();
    String dirName  = sendFileDialog.getDirectory();
    if(fileName != null && fileName.length() > 0) {
      try {
	FileInputStream fileIn = new FileInputStream(dirName + fileName);
	byte[] bytes = new byte[fileIn.available()];
	fileIn.read(bytes);
	String fileStr = new String(bytes);
	client.stdinWriteString(fileStr);
      } catch (Throwable t) {
	alertDialog("Error sending file: " + t.getMessage());
      }
    }

  }

  SSHCaptureConsole captureConsole;
  FileDialog captureToFileDialog = null;
  public final boolean captureToFileDialog() {
    if(captureToFileDialog == null) {
      captureToFileDialog = new FileDialog(parent, "MindTerm - Select file to capture to", FileDialog.SAVE);
    }
    captureToFileDialog.setVisible(true);

    if(SSH.NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
      } catch (netscape.security.ForbiddenTargetException e) {
	// !!!
      }
    }

    String fileName = captureToFileDialog.getFile();
    String dirName  = captureToFileDialog.getDirectory();
    if(fileName != null && fileName.length() > 0) {
      try {
	FileOutputStream fileOut = new FileOutputStream(dirName + fileName, true);
	captureConsole = new SSHCaptureConsole(client, fileOut);
	return true;
      } catch (Throwable t) {
	alertDialog("Error in capture: " + t.getMessage());
      }
    }
    return false;
  }

  public void endCapture() {
    if(captureConsole != null) {
      captureConsole.endCapture();
      captureConsole = null;
    }
  }

  final static String keyGenerationHelp =
"This will create a RSA identity which can be used with the RSA " +
"authentication method. Your identity will consist of two " +
"parts: public and private keys. Your private key will be saved " +
"in the location which you specify; the corresponding public key " +
"is saved in a file with an identical name but with an extension of " +
"'.pub' added to it.\n" +
"\n" +
"Your private key is protected by encryption, if you enter a " +
"password. If you leave the password field blank, the key will " +
"not be encrypted. This should only be used in protected " +
"environments where unattended logins are desired. The contents " +
"of the 'comment' field are stored with your key, and displayed " +
"each time you are prompted for the key's password.\n" +
"\n" +
"The key is generated using a random number generator, which " +
"is seeded by mouse movement in the field containing this text. " +
"Please move the mouse around in here until the progress bar below " +
"registers 100%.\n" +
"\n" +
"To use the key, you must transfer the '.pub' public key " +
"file to an SSH server and add the contents of it to the " +
"file 'authorized_keys' in your ssh directory (e.g. ~/.ssh) " +
"on the server. For convenience, your public key is also " +
"copied to the clipboard.";

  Dialog keyGenerationDialog;
  FileDialog keyGenFD;
  TextField bitsText;
  TextField fileText;
  TextField pwdText;
  TextField pwdText2;
  TextField commText;
  TextArea  descText;
  Checkbox  useCheck;
  XProgressBar progBar;
  Label     msgLbl;
  Button    okBut;
  int    randCnt   = 0;
  int    dummy     = 0;
  byte[] randBytes = new byte[512];
  public void keyGenerationDialog() {

    if(SSH.NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
      } catch (netscape.security.ForbiddenTargetException e) {
	// !!!
      }
    }

    if(keyGenerationDialog == null) {
      keyGenerationDialog = new Dialog(parent, "MindTerm - RSA Key Generation", true);

      MouseMotionListener ml;
      GridBagLayout       grid  = new GridBagLayout();
      GridBagConstraints  gridc = new GridBagConstraints();
      Label               label;
      Button              b;

      keyGenerationDialog.setLayout(grid);

      gridc.fill      = GridBagConstraints.BOTH;
      gridc.anchor    = GridBagConstraints.WEST;
      gridc.weightx   = 1.0;
      gridc.weighty   = 1.0;
      gridc.gridwidth = 12;
      gridc.gridy     = 0;

      gridc.insets    = new Insets(8, 8, 8, 8);

      int rows = 18;
      Dimension sDim = Toolkit.getDefaultToolkit().getScreenSize();
      if(sDim.height < 600)
	  rows = 8;

      descText = new TextArea(keyGenerationHelp, rows, 48, TextArea.SCROLLBARS_VERTICAL_ONLY);
      descText.addMouseMotionListener(ml = new MouseMotionAdapter() {
	  public void mouseMoved(MouseEvent e) {
	    dummy++;
	    if(randCnt < 256) {
	      if((dummy % 2) == 0) {
		randBytes[randCnt++] = (byte)(((randCnt % 2) == 0) ? e.getX() : e.getY());
		progBar.setValue(randCnt);
	      }
	    } else {
	      okBut.setEnabled(true);
	    }
	  }
      });
      grid.setConstraints(descText, gridc);
      keyGenerationDialog.add(descText);
      descText.setEditable(false);

      gridc.gridy     = 1;
      gridc.fill      = GridBagConstraints.NONE;
      gridc.anchor    = GridBagConstraints.CENTER;

      progBar = new XProgressBar(256, 150, 20);
      grid.setConstraints(progBar, gridc);
      keyGenerationDialog.add(progBar);

      gridc.anchor    = GridBagConstraints.WEST;
      gridc.insets    = new Insets(4, 4, 0, 0);
      gridc.gridwidth = 4;

      gridc.gridy     = 2;
      gridc.weightx   = 0;

      label = new Label("Keylength (bits):");
      grid.setConstraints(label, gridc);
      keyGenerationDialog.add(label);
      bitsText = new TextField("", 5);
      gridc.weightx   = 1.0;
      grid.setConstraints(bitsText, gridc);
      keyGenerationDialog.add(bitsText);

      gridc.gridy     = 3;
      gridc.fill      = GridBagConstraints.HORIZONTAL;
      gridc.weightx   = 0;
      label = new Label("Identity file:");
      grid.setConstraints(label, gridc);
      keyGenerationDialog.add(label);
      fileText = new TextField("", 18);
      gridc.weightx   = 1.0;
      grid.setConstraints(fileText, gridc);
      keyGenerationDialog.add(fileText);
      b = new Button("...");
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if(keyGenFD == null) {
	    keyGenFD = new FileDialog(parent, "MindTerm - Select file to save identity to", FileDialog.SAVE);
	    keyGenFD.setDirectory(client.propsHandler.getSSHHomeDir());
	  }
	  keyGenFD.setVisible(true);
	  if(keyGenFD.getFile() != null && keyGenFD.getFile().length() > 0)
	    fileText.setText(keyGenFD.getDirectory() + keyGenFD.getFile());
	}
      });
      gridc.fill = GridBagConstraints.NONE;
      grid.setConstraints(b, gridc);
      keyGenerationDialog.add(b);

      gridc.gridy     = 4;
      gridc.fill      = GridBagConstraints.HORIZONTAL;
      gridc.weightx   = 0;
      label = new Label("Password:");
      grid.setConstraints(label, gridc);
      keyGenerationDialog.add(label);
      pwdText = new TextField("", 18);
      pwdText.setEchoChar('*');
      gridc.weightx   = 1.0;
      grid.setConstraints(pwdText, gridc);
      keyGenerationDialog.add(pwdText);

      gridc.gridy     = 5;
      gridc.weightx   = 0;
      label = new Label("Password again:");
      grid.setConstraints(label, gridc);
      keyGenerationDialog.add(label);
      pwdText2 = new TextField("", 18);
      pwdText2.setEchoChar('*');
      gridc.weightx   = 1.0;
      grid.setConstraints(pwdText2, gridc);
      keyGenerationDialog.add(pwdText2);

      gridc.gridy     = 6;
      gridc.weightx   = 0;
      label = new Label("Comment:");
      grid.setConstraints(label, gridc);
      keyGenerationDialog.add(label);
      commText = new TextField("", 18);
      gridc.weightx   = 1.0;
      grid.setConstraints(commText, gridc);
      keyGenerationDialog.add(commText);

      gridc.gridy     = 7; 
      gridc.fill      = GridBagConstraints.NONE;
      gridc.gridwidth = 2;
      gridc.insets    = new Insets(8, 4, 0, 0);
      useCheck = new Checkbox("Use key in current session");
      grid.setConstraints(useCheck, gridc);
      keyGenerationDialog.add(useCheck);

      gridc.gridy     = 8;
      gridc.fill      = GridBagConstraints.HORIZONTAL;
      gridc.insets    = new Insets(0, 0, 0, 0);
      gridc.gridwidth = 4;
      msgLbl = new Label("", Label.CENTER);
      gridc.fill  = GridBagConstraints.HORIZONTAL;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(msgLbl, gridc);
      keyGenerationDialog.add(msgLbl);

      gridc.gridy     = 9;
      Panel bp = new Panel(new FlowLayout());
      bp.add(okBut = new Button("Generate"));
      okBut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  int    bits;
	  String passwd   = pwdText.getText();
	  String fileName = fileText.getText();
	  String comment  = commText.getText();

	  try {
	    bits = Integer.valueOf(bitsText.getText()).intValue();
	    if(bits < 512 || bits > 2048) {
	      msgLbl.setText("Keylength must be in interval 512-2048");
	      return;
	    }
	  } catch (Exception ee) {
	    msgLbl.setText("Keylength must be an integer");
	    return;
	  }
	  if(!passwd.equals(pwdText2.getText())) {
	    msgLbl.setText("Please give same password twice");
	    return;
	  }
	  try {
	    if(fileName.length() == 0)
	      throw new Exception();
	    if(fileName.indexOf(File.separator) == -1)
	      fileName = client.propsHandler.getSSHHomeDir() + fileName;
	    FileOutputStream f = new FileOutputStream(fileName);
	    f.close();
	  } catch (Exception eee) {
	    msgLbl.setText("File can't be written to, please check");
	    return;
	  }

	  // To make the label show up in win32/jview?!
	  //
	  keyGenerationDialog.remove(msgLbl);
	  msgLbl.setText("Please wait while generating key...");
	  keyGenerationDialog.add(msgLbl);
	  okBut.setEnabled(false);
	  try {
	    String pks = SSH.generateKeyFiles(SSH.generateRSAKeyPair(bits, new SecureRandom(randBytes)), fileName, passwd, comment);
	    client.sshStdIO.setSelection(pks);
	    client.sshStdIO.selectionAvailable(true);
	    msgLbl.setText("");
	    alertDialog("Key is now generated and saved to file and clipboard");
	    if(useCheck.getState())
	      client.propsHandler.setProperty("idfile", fileName);
	  } catch (IOException ee) {
	    alertDialog("Error saving identity: " + ee.getMessage());
	    msgLbl.setText("An error occured while saving identity");
	  }

	  pwdText.setText("");
	  pwdText2.setText("");
	  randCnt = 0;
	  dummy   = 0;
	  progBar.setValue(0);
	  setDefaultFileName();
	}
      });
      bp.add(b = new Button("Close"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  randCnt = 0;
	  dummy   = 0;
	  keyGenerationDialog.setVisible(false);
	  pwdText.setText("");
	  pwdText2.setText("");
	}
      });

      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(bp, gridc);
      keyGenerationDialog.add(bp);

      keyGenerationDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(keyGenerationDialog);

      keyGenerationDialog.setResizable(true);
      keyGenerationDialog.pack();
    }
    useCheck.setState(true);
    msgLbl.setText("");
    okBut.setEnabled(false);
    bitsText.setText("1024");
    progBar.setValue(0);
    setDefaultFileName();

    AWTConvenience.placeDialog(keyGenerationDialog);
    bitsText.requestFocus();
    keyGenerationDialog.setVisible(true);
  }

  void setDefaultFileName() {
    try {
      String fn = client.propsHandler.getSSHHomeDir() + SSHPropertyHandler.DEF_IDFILE;
      File   f  = new File(fn);
      int    fi = 0;
      while(f.exists()) {
	fn = client.propsHandler.getSSHHomeDir() + SSHPropertyHandler.DEF_IDFILE + fi;
	f  = new File(fn);
	fi++;
      }
      fi--;
      fileText.setText(SSHPropertyHandler.DEF_IDFILE + (fi >= 0 ? String.valueOf(fi) : ""));
    } catch (Throwable t) {
      // !!!
      // Don't care...
    }
  }

  public final void alertDialog(String message) {
      SSHMiscDialogs.alert("MindTerm - Alert", message, parent);
  }

  public final String passwordDialog(String message, String title) {
      return SSHMiscDialogs.password(title, message, parent);
  }

  public final String setPasswordDialog(String message, String title) {
      return SSHMiscDialogs.setPassword(title, message, parent);
  }

  public final boolean confirmDialog(String message, boolean defAnswer) {
      return SSHMiscDialogs.confirm("MindTerm - Confirmation", message,
				    defAnswer, parent);
  }

  public final void textDialog(String title, String text, int rows, int cols, boolean scrollbar) {
      SSHMiscDialogs.notice(title, text, rows, cols, scrollbar, parent);
  }

}
