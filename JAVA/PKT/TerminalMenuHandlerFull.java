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
package mindbright.terminal;

import java.awt.*; 
import java.awt.event.*; 

import mindbright.util.AWTConvenience;

public final class TerminalMenuHandlerFull extends TerminalMenuHandler {
  TerminalWin term;
  Object[]    optionsItems;
  Menu        optionsMenu;

  TerminalMenuListener listener;

  final static String[] optionsMenuTxt = { "VT Options",
					   "Reverse Video", "Auto Wraparound", "Reverse Wraparound",
					   "Insert mode", "Auto Linefeed",
					   "Scroll to Bottom On Key Press",
					   "Scroll to Bottom On Tty Output", "Local Page-ctrl Keys",
					   "Copy <CR><NL> Instead Of <CR>", "Visible Cursor",
					   "Use ASCII For Line Draw", "Local Echo",
					   "Scale Font On Resize", "Visual Bell",
					   "Map <CTRL>+<SPC> To ^@ (<NUL>)",
					   "Toggle 80/132 Columns",
					   "Enable 80/132 Switching",
					   "Copy On Select",
  };

  final static String[] settingsMenu = { "Terminal Settings",
					 "Emulation", "Resize gravity", "Font", "Savelines",
					 "Scrollbar", "Colors", "Backspace"
  };

  public void setTerminalWin(TerminalWin term) {
    this.term = term;
  }

  public void setTerminalMenuListener(TerminalMenuListener listener) {
    this.listener = listener;
  }

  public void update() {
    if(listener != null) {
      listener.update();
    }
  }

  public void setEnabledOpt(int opt, boolean val) {
    ((CheckboxMenuItem)optionsItems[opt]).setEnabled(val);
  }

  public void setStateOpt(int opt, boolean val) {
    ((CheckboxMenuItem)optionsItems[opt]).setState(val);
  }

  public Menu getOptionsMenu() {
    if(optionsMenu != null)
      return optionsMenu;

    optionsMenu = new Menu(optionsMenuTxt[0]);
    ItemListener il;
    il = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
	int i;
	for(i = 0; i < optionsItems.length; i++)
	  if(optionsItems[i] == e.getItemSelectable())
	    break;
	if(i >= optionsItems.length)
	  // !!! can't happen...
	  return;
	term.setProperty(TerminalDefProps.defaultPropDesc[i][TerminalDefProps.PROP_NAME],
			 String.valueOf(!term.termOptions[i]));
      }
    };

    optionsItems = new Object[optionsMenuTxt.length - 1];
    for(int i = 1; i < optionsMenuTxt.length; i++) {
      CheckboxMenuItem cb = new CheckboxMenuItem(optionsMenuTxt[i],
						 term.termOptions[i - 1]);
      optionsItems[i - 1] = cb;
      cb.addItemListener(il);
      optionsMenu.add(cb);
    }

    // !!! This is not supported for now...
    //
    ((CheckboxMenuItem)optionsItems[Terminal.OPT_SCALE_FONT]).setEnabled(false);
    //
    // !!!

    return optionsMenu;
  }

  Dialog settingsDialog;
  Choice choiceTE, choiceFN, choiceFG, choiceBG, choiceCC;
  Checkbox cbInitPos, cbUL, cbUR, cbLL, cbLR;
  CheckboxGroup cbgInitPos;
  TextField textFS, textFG, textBG, textCC, textRows, textCols, textInitPos;
  Label lblAlert;
  final static String[] te = TerminalXTerm.getTerminalTypes();
  final static String[] fn = Toolkit.getDefaultToolkit().getFontList();
  public final void termSettingsDialog() {
    if(settingsDialog == null) {
      settingsDialog = new Dialog(term.ownerFrame, settingsMenu[0], true);

      ItemListener       ilC;
      ItemListener       ilP;
      Label              lbl;
      GridBagLayout      grid  = new GridBagLayout();
      GridBagConstraints gridc = new GridBagConstraints();
      settingsDialog.setLayout(grid);

      gridc.insets = new Insets(4, 4, 4, 4);
      gridc.fill   = GridBagConstraints.NONE;
      gridc.anchor = GridBagConstraints.WEST;

      gridc.gridy = 0;
      gridc.gridwidth = 6;
      lbl = new Label("Terminal type:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);
      choiceTE = new Choice();
      grid.setConstraints(choiceTE, gridc);
      settingsDialog.add(choiceTE);

      gridc.gridy = 1;
      gridc.gridwidth = 4;
      lbl = new Label("Columns:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      gridc.gridwidth = 2;
      textCols = new TextField("", 3);
      grid.setConstraints(textCols, gridc);
      settingsDialog.add(textCols);

      lbl = new Label("Rows:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      textRows = new TextField("", 3);
      grid.setConstraints(textRows, gridc);
      settingsDialog.add(textRows);

      gridc.gridy = 2;
      gridc.gridwidth = 2;
      lbl = new Label("Font:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      gridc.gridwidth = 6;
      choiceFN = new Choice();
      grid.setConstraints(choiceFN, gridc);
      settingsDialog.add(choiceFN);

      gridc.gridwidth = 2;
      textFS = new TextField("", 3);
      grid.setConstraints(textFS, gridc);
      settingsDialog.add(textFS);

      gridc.gridy = 3;
      gridc.gridwidth = 10;
      lbl = new Label("Foreground color:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      gridc.gridy = 4;
      gridc.gridwidth = 6;
      choiceFG = new Choice();
      grid.setConstraints(choiceFG, gridc);
      settingsDialog.add(choiceFG);
      choiceFG.addItemListener(ilC = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  updateColors();
	}
      });

      textFG = new TextField("", 10);
      grid.setConstraints(textFG, gridc);
      settingsDialog.add(textFG);

      gridc.gridy = 5;
      lbl = new Label("Background color:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      gridc.gridy = 6;
      choiceBG = new Choice();
      grid.setConstraints(choiceBG, gridc);
      settingsDialog.add(choiceBG);
      choiceBG.addItemListener(ilC);

      textBG = new TextField("", 10);
      grid.setConstraints(textBG, gridc);
      settingsDialog.add(textBG);

      gridc.gridy = 7;
      lbl = new Label("Cursor color:");
      grid.setConstraints(lbl, gridc);
      settingsDialog.add(lbl);

      gridc.gridy = 8;
      choiceCC = new Choice();
      grid.setConstraints(choiceCC, gridc);
      settingsDialog.add(choiceCC);
      choiceCC.addItemListener(ilC);

      textCC = new TextField("", 10);
      grid.setConstraints(textCC, gridc);
      settingsDialog.add(textCC);

      Panel p = new Panel();
      GridBagLayout      grid2  = new GridBagLayout();
      GridBagConstraints gridc2 = new GridBagConstraints();
      p.setLayout(grid2);

      gridc2.gridy = 0;
      gridc2.gridwidth = 4;
      gridc2.anchor = GridBagConstraints.WEST;
      cbInitPos = new Checkbox("Window position:");
      grid2.setConstraints(cbInitPos, gridc2);
      p.add(cbInitPos);

      cbgInitPos = new CheckboxGroup();

      gridc2.gridwidth = 1;
      cbUL = new Checkbox("", true, cbgInitPos);
      grid2.setConstraints(cbUL, gridc2);
      p.add(cbUL);
      cbUR = new Checkbox("", false, cbgInitPos);
      grid2.setConstraints(cbUR, gridc2);
      p.add(cbUR);

      gridc2.gridy = 1;
      gridc2.gridwidth = 4;
      gridc2.anchor = GridBagConstraints.CENTER;
      textInitPos = new TextField("", 10);
      grid2.setConstraints(textInitPos, gridc2);
      p.add(textInitPos);

      gridc2.gridwidth = 1;
      gridc2.anchor = GridBagConstraints.WEST;
      cbLL = new Checkbox("", false, cbgInitPos);
      grid2.setConstraints(cbLL, gridc2);
      p.add(cbLL);
      cbLR = new Checkbox("", false, cbgInitPos);
      grid2.setConstraints(cbLR, gridc2);
      p.add(cbLR);

      cbInitPos.addItemListener(ilP = new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	  updateInitPos();
	}
      });
      cbUL.addItemListener(ilP);
      cbUR.addItemListener(ilP);
      cbLL.addItemListener(ilP);
      cbLR.addItemListener(ilP);

      gridc.gridy = 9;
      gridc.insets = new Insets(8, 4, 0, 0);
      gridc.anchor = GridBagConstraints.CENTER;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      grid.setConstraints(p, gridc);
      settingsDialog.add(p);

      lblAlert = new Label("", Label.CENTER);
      gridc.insets = new Insets(0, 0, 0, 0);
      gridc.gridy = 10;
      gridc.fill  = GridBagConstraints.HORIZONTAL;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(lblAlert, gridc);
      settingsDialog.add(lblAlert);

      Panel bp = new Panel(new FlowLayout());

      Button b;
      bp.add(b = new Button("OK"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  try {
	    term.setProperty("te", te[choiceTE.getSelectedIndex()]);
	    term.setProperty("fn", fn[choiceFN.getSelectedIndex()]);
	    term.setProperty("fs", textFS.getText());
	    term.setProperty("fg", getSelectedColor(choiceFG, textFG));
	    term.setProperty("bg", getSelectedColor(choiceBG, textBG));
	    term.setProperty("cc", getSelectedColor(choiceCC, textCC));

	    String geomPos = "";
	    if(cbInitPos.getState()) {
	      geomPos = textInitPos.getText();
	    }
	    term.setProperty("gm", textCols.getText() + "x" + textRows.getText() + geomPos);

	    settingsDialog.setVisible(false);
	  } catch (Exception ee) {
	    lblAlert.setText(ee.getMessage());
	  }
	}
      });
      bp.add(b = new Button("Cancel"));
      b.addActionListener(new AWTConvenience.CloseAction(settingsDialog));

      gridc.gridy = 11;
      grid.setConstraints(bp, gridc);
      settingsDialog.add(bp);

      fillChoices();

      settingsDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(settingsDialog);

      settingsDialog.setResizable(true);
      settingsDialog.pack();
    }

    choiceTE.select(term.getProperty("te"));
    choiceFN.select(term.getProperty("fn"));
    textFS.setText(term.getProperty("fs"));
    textCols.setText(String.valueOf(term.cols()));
    textRows.setText(String.valueOf(term.rows()));

    initColorSelect(choiceFG, textFG, term.getProperty("fg"));
    initColorSelect(choiceBG, textBG, term.getProperty("bg"));
    initColorSelect(choiceCC, textCC, term.getProperty("cc"));

    updateColors();

    String geomPos = term.savedGeomPos;
    if(geomPos.length() > 0) {
      cbInitPos.setState(true);
      updateInitPos();
      if(geomPos.equals("+0+0")) {
	cbUL.setState(true);
      } else if(geomPos.equals("-0+0")) {
	cbUR.setState(true);
      } else if(geomPos.equals("+0-0")) {
	cbLL.setState(true);
      } else if(geomPos.equals("-0-0")) {
	cbLR.setState(true);
      }
      textInitPos.setText(geomPos);
    } else {
      cbInitPos.setState(false);
      updateInitPos();
    }

    lblAlert.setText("");
    AWTConvenience.placeDialog(settingsDialog);

    choiceTE.requestFocus();
    settingsDialog.setVisible(true);
  }

  void initColorSelect(Choice c, TextField t, String colStr) {
    if(Character.isDigit(colStr.charAt(0))) {
      c.select("custom rgb");
      t.setText(colStr);
    } else {
      t.setText("");
      t.setEnabled(false);
      c.select(colStr);
    }
  }

  void checkColorSelect(Choice c, TextField t) {
    int cs = c.getSelectedIndex();
    
    if(cs == 0) {
      boolean en = t.isEnabled();
      if(!en) {
	t.setEditable(true);
	t.setEnabled(true);
	t.setBackground(SystemColor.text);
	t.requestFocus();
      }
    } else {
      t.setText("");
      t.setEditable(false);
      t.setEnabled(false);
      // on the Mac, Choices can't get keyboard focus
      // so we may need to move focus away from the TextField
      t.setBackground(term.termColors[cs - 1]);
    }
  }

  void updateColors() {
    checkColorSelect(choiceFG, textFG);
    checkColorSelect(choiceBG, textBG);
    checkColorSelect(choiceCC, textCC);
  }

  String getSelectedColor(Choice c, TextField t) {
    String colStr;
    if(c.getSelectedIndex() == 0)
      colStr = t.getText();
    else
      colStr = c.getSelectedItem();
    return colStr;
  }

  void updateInitPos() {
    if(cbInitPos.getState()) {
      textInitPos.setEnabled(true);
      cbUL.setEnabled(true);
      cbUR.setEnabled(true);
      cbLL.setEnabled(true);
      cbLR.setEnabled(true);

      if(cbUL.getState()) {
	textInitPos.setText("+0+0");
      } else if(cbUR.getState()) {
	textInitPos.setText("-0+0");
      } else if(cbLL.getState()) {
	textInitPos.setText("+0-0");
      } else if(cbLR.getState()) {
	textInitPos.setText("-0-0");
      }

    } else {
      textInitPos.setText("");
      textInitPos.setEnabled(false);
      cbUL.setEnabled(false);
      cbUR.setEnabled(false);
      cbLL.setEnabled(false);
      cbLR.setEnabled(false);
    }

  }

  void fillChoices() {
    int i;
    for(i = 0; i < te.length; i++) {
      choiceTE.add(te[i]);
    }
    for(i = 0; i < fn.length; i++) {
      choiceFN.add(fn[i]);
    }
    choiceBG.add("custom rgb");
    choiceFG.add("custom rgb");
    choiceCC.add("custom rgb");
    for(i = 0; i < term.termColorNames.length; i++) {
      choiceBG.add(term.termColorNames[i]);
      choiceFG.add(term.termColorNames[i]);
      choiceCC.add(term.termColorNames[i]);
    }
  }

  Dialog settingsDialog2;
  Choice choiceSB, choiceRG;
  Checkbox cbDEL, cbBS;
  TextField textSL, textSD;
  Label lblAlert2;
  final static String[] sb = { "left", "right", "none" };
  final static String[] rg = { "bottom", "top" };
  public final void termSettingsDialog2() {
    if(settingsDialog2 == null) {
      int i;
      settingsDialog2 = new Dialog(term.ownerFrame, "Terminal Miscellaneous Settings", true);

      Label              lbl;
      GridBagLayout      grid  = new GridBagLayout();
      GridBagConstraints gridc = new GridBagConstraints();
      settingsDialog2.setLayout(grid);

      gridc.insets = new Insets(4, 4, 0, 0);
      gridc.fill   = GridBagConstraints.NONE;
      gridc.anchor = GridBagConstraints.WEST;
      gridc.gridwidth = 4;

      gridc.gridy = 0;
      lbl = new Label("Savelines:");
      grid.setConstraints(lbl, gridc);
      settingsDialog2.add(lbl);
      textSL = new TextField("", 4);
      grid.setConstraints(textSL, gridc);
      settingsDialog2.add(textSL);

      gridc.gridy = 1;
      lbl = new Label("Scrollbar:");
      grid.setConstraints(lbl, gridc);
      settingsDialog2.add(lbl);
      choiceSB = new Choice();
      grid.setConstraints(choiceSB, gridc);
      settingsDialog2.add(choiceSB);
      for(i = 0; i < sb.length; i++) {
	choiceSB.add(sb[i]);
      }

      gridc.gridy = 2;
      lbl = new Label("Resize gravity:");
      grid.setConstraints(lbl, gridc);
      settingsDialog2.add(lbl);
      choiceRG = new Choice();
      grid.setConstraints(choiceRG, gridc);
      settingsDialog2.add(choiceRG);
      for(i = 0; i < rg.length; i++) {
	choiceRG.add(rg[i]);
      }

      gridc.gridy = 3;
      lbl = new Label("Select delim.:");
      grid.setConstraints(lbl, gridc);
      settingsDialog2.add(lbl);
      textSD = new TextField("", 4);
      grid.setConstraints(textSD, gridc);
      settingsDialog2.add(textSD);

      gridc.gridy = 4;
      gridc.gridwidth = 8;
      gridc.insets = new Insets(4, 16, 0, 0);
      cbBS = new Checkbox("Backspace sends Delete");
      grid.setConstraints(cbBS, gridc);
      settingsDialog2.add(cbBS);

      gridc.gridy = 5;
      cbDEL = new Checkbox("Delete sends Backspace");
      grid.setConstraints(cbDEL, gridc);
      settingsDialog2.add(cbDEL);

      lblAlert2 = new Label("", Label.CENTER);
      gridc.insets = new Insets(0, 0, 0, 0);
      gridc.gridy = 6;
      gridc.fill  = GridBagConstraints.HORIZONTAL;
      gridc.gridwidth = GridBagConstraints.REMAINDER;
      gridc.anchor = GridBagConstraints.CENTER;
      grid.setConstraints(lblAlert2, gridc);
      settingsDialog2.add(lblAlert2);

      Panel bp = new Panel(new FlowLayout());

      Button b;
      bp.add(b = new Button("OK"));
      b.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  try {
	    term.setProperty("sb", sb[choiceSB.getSelectedIndex()]);
	    term.setProperty("rg", rg[choiceRG.getSelectedIndex()]);
	    term.setProperty("sl", textSL.getText());
	    term.setProperty("sd", textSD.getText());
	    if(cbBS.getState())
	      term.setProperty("bs", "DEL");
	    else
	      term.setProperty("bs", "BS");
	    if(cbDEL.getState())
	      term.setProperty("de", "BS");
	    else
	      term.setProperty("de", "DEL");

	    settingsDialog2.setVisible(false);
	  } catch (Exception ee) {
	    lblAlert2.setText(ee.getMessage());
	  }
	}
      });
      bp.add(b = new Button("Cancel"));
      b.addActionListener(new AWTConvenience.CloseAction(settingsDialog2));

      gridc.gridy = 7;
      grid.setConstraints(bp, gridc);
      settingsDialog2.add(bp);

      settingsDialog2.addWindowListener(new AWTConvenience.CloseAdapter(b));

      AWTConvenience.setBackgroundOfChildren(settingsDialog2);

      settingsDialog2.setResizable(true);
      settingsDialog2.pack();
    }

    choiceSB.select(term.getProperty("sb"));
    choiceRG.select(term.getProperty("rg"));
    textSL.setText(term.getProperty("sl"));

    String sdSet = term.getProperty("sd");
    if((sdSet.charAt(0) == '"' && sdSet.charAt(sdSet.length() - 1) == '"')) {
      sdSet = sdSet.substring(1, sdSet.length() - 1);
    }
    textSD.setText(sdSet);

    if(term.getProperty("bs").equals("DEL")) {
      cbBS.setState(true);
    } else {
      cbBS.setState(false);
    }
    if(term.getProperty("de").equals("BS")) {
      cbDEL.setState(true);
    } else {
      cbDEL.setState(false);
    }

    lblAlert2.setText("");

    AWTConvenience.placeDialog(settingsDialog2);

    textSL.requestFocus();
    settingsDialog2.setVisible(true);
  }

  Dialog     findDialog = null;
  TextField  findText;
  Label      label;
  Checkbox   dirCheck, caseCheck;
  Button     findBut, cancBut;

  public final void findDialog() {
    if(findDialog == null) {
      findDialog = new Dialog(term.ownerFrame, "MindTerm - Find", false);
      GridBagLayout      grid  = new GridBagLayout();
      GridBagConstraints gridc = new GridBagConstraints();
      findDialog.setLayout(grid);

      gridc.fill   = GridBagConstraints.NONE;
      gridc.anchor = GridBagConstraints.WEST;
      gridc.gridwidth = 1;

      gridc.gridy = 0;
      label = new Label("Find:");
      grid.setConstraints(label, gridc);
      findDialog.add(label);

      gridc.fill = GridBagConstraints.HORIZONTAL;
      gridc.gridwidth = 5;

      findText = new TextField("", 26);
      grid.setConstraints(findText, gridc);
      findDialog.add(findText);

      gridc.gridwidth = 4;
      gridc.ipadx = 4;
      gridc.ipady = 4;
      gridc.insets = new Insets(6, 3, 3, 6);

      findBut = new Button("Find");
      grid.setConstraints(findBut, gridc);
      findDialog.add(findBut);

      gridc.insets = new Insets(0, 0, 0, 0);
      gridc.ipadx = 0;
      gridc.ipady = 0;
      gridc.gridwidth = 3;
      gridc.gridy = 1;
      gridc.fill   = GridBagConstraints.NONE;

      caseCheck = new Checkbox("Case sensitive");
      grid.setConstraints(caseCheck, gridc);
      findDialog.add(caseCheck);
      
      dirCheck = new Checkbox("Find backwards");
      grid.setConstraints(dirCheck, gridc);
      findDialog.add(dirCheck);

      gridc.gridwidth = 4;
      gridc.ipadx = 4;
      gridc.ipady = 4;
      gridc.insets = new Insets(3, 3, 6, 6);
      gridc.fill = GridBagConstraints.HORIZONTAL;
      cancBut = new Button("Cancel");
      grid.setConstraints(cancBut, gridc);
      findDialog.add(cancBut);

      cancBut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  findDialog.setVisible(false);
	  if(findLen > 0) {
	    term.clearSelection(curFindRow, curFindCol, curFindRow, curFindCol + findLen - 1);
	  }
	  curFindRow = 0;
	  curFindCol = 0;
	  findLen    = 0;
	}
      });

      findBut.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    String txt = findText.getText();
	    if(txt != null && txt.length() > 0) {
		doFind();
	    }
	}
      });

      findDialog.addWindowListener(new AWTConvenience.CloseAdapter(cancBut));

      AWTConvenience.setBackgroundOfChildren(findDialog);
      AWTConvenience.setKeyListenerOfChildren(findDialog,
					      new AWTConvenience.OKCancelAdapter(findBut, cancBut),
					      null);

      findDialog.setResizable(true);
      findDialog.pack();
    }

    AWTConvenience.placeDialog(findDialog);
    findText.requestFocus();
    findDialog.setVisible(true);
  }

  final static boolean doMatch(String findStr, char firstChar, char[] chars, int idx,
			       boolean caseSens, int len) {
      String cmpStr;
      if(caseSens) {
	if(chars[idx] != firstChar)
	  return false;
	cmpStr = new String(chars, idx, len);
	if(cmpStr.equals(findStr))
	  return true;
      } else {
	if(Character.toLowerCase(chars[idx]) != firstChar)
	  return false;
	cmpStr = new String(chars, idx, len);
	if(cmpStr.equalsIgnoreCase(findStr))
	  return true;
      }
      return false;
  }

  int curFindRow = 0;
  int curFindCol = 0;
  int findLen    = 0;

  void doFind() {
    String  findStr = findText.getText();
    String  cmpStr;
    int     len = findStr.length();
    boolean caseSens = caseCheck.getState();
    boolean revFind  = dirCheck.getState();
    int     lastRow  = term.saveVisTop + term.curRow;
    int     startCol;
    boolean found    = false;
    int     i, j = 0;
    char    firstChar = (caseSens ? findStr.charAt(0) : Character.toLowerCase(findStr.charAt(0)));

    if(findLen > 0) {
      term.clearSelection(curFindRow, curFindCol, curFindRow, curFindCol + findLen - 1);
    }
    
    if(revFind) {
      if(findLen > 0) {
	startCol = curFindCol - 1;
      } else {
	curFindRow = lastRow;
	startCol   = term.cols - len;
      }
    foundItRev:
      for(i = curFindRow; i >= 0; i--) {
	for(j = startCol; j >= 0; j--) {
	  if(term.screen[i][j] == 0)
	    continue;
	  if(doMatch(findStr, firstChar, term.screen[i], j, caseSens, len))
	    break foundItRev;
	}
	startCol = term.cols - len;
      }
      if(i >= 0)
	found = true;
    } else {
      startCol = curFindCol + findLen;
    foundIt:
      for(i = curFindRow; i < lastRow; i++) {
	for(j = startCol; j < term.cols - len; j++) {
	  if(term.screen[i][j] == 0)
	    continue;
	  if(doMatch(findStr, firstChar, term.screen[i], j, caseSens, len))
	    break foundIt;
	}
	startCol = 0;
      }
      if(i < lastRow)
	found = true;
    }
    if(found) {
      findLen = len;
      if(term.saveVisTop < i)
	term.visTop = term.saveVisTop;
      else if(term.visTop > i || (i - term.visTop > term.rows))
	term.visTop = i;
      term.updateScrollbarValues();
      term.makeAllDirty(false);
      term.makeSelection(i, j, i, j + len - 1);
      curFindRow = i;
      curFindCol = j;
      findLen    = len;
    } else {
      term.doBell();
      curFindRow = 0;
      curFindCol = 0;
      findLen    = 0;
    }
  }

}
