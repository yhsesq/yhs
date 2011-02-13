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

import java.io.IOException;
import java.util.Properties;
import java.util.NoSuchElementException;

public interface Terminal {

  public final static int ATTR_BOLD         = 0x0001;
  public final static int ATTR_LOWINTENSITY = 0x0002;
  public final static int ATTR_UNDERLINE    = 0x0004;
  public final static int ATTR_BLINKING     = 0x0008;
  public final static int ATTR_INVERSE      = 0x0010;
  public final static int ATTR_INVISIBLE    = 0x0020;
  public final static int ATTR_FGCOLOR      = 0x0040;
  public final static int ATTR_BGCOLOR      = 0x0080;

  final static int OPT_REV_VIDEO    = 0;
  final static int OPT_AUTO_WRAP    = 1;
  final static int OPT_REV_WRAP     = 2;
  final static int OPT_INSERTMODE   = 3;
  final static int OPT_AUTO_LF      = 4;
  final static int OPT_SCROLL_SK    = 5;
  final static int OPT_SCROLL_SI    = 6;
  final static int OPT_LOCAL_PGKEYS = 7;
  final static int OPT_COPY_CRNL    = 8;
  final static int OPT_VIS_CURSOR   = 9;
  final static int OPT_ASCII_LDC    = 10;
  final static int OPT_LOCAL_ECHO   = 11;
  final static int OPT_SCALE_FONT   = 12;
  final static int OPT_VIS_BELL     = 13;
  final static int OPT_MAP_CTRLSP   = 14;
  final static int OPT_DECCOLM      = 15;
  final static int OPT_DEC132COLS   = 16;
  final static int OPT_COPY_SEL     = 17;
  final static int OPT_LAST_OPT     = 18;

  public String terminalType();
  public int    rows();
  public int    cols();
  public int    vpixels();
  public int    hpixels();

  public void write(char c);
  public void write(char[] c, int off, int len);
  public void write(String str);
  public void writeLineDrawChar(char c);

  public void addTerminalListener(TerminalListener listener);

  public void sendBytes(byte[] b);

  public void doBell();
  public void doBS();
  public void doTab();
  public void doTabs(int n);
  public void doBackTabs(int n);
  public void setTab(int col);
  public void clearTab(int col);
  public void resetTabs();
  public void clearAllTabs();
  public void doCR();
  public void doLF();

  public void resetInterpreter();
  public void resetWindow();
  public void setWindow(int top, int bottom);
  public void setWindow(int top, int right, int bottom, int left);
  public int  getWindowTop();
  public int  getWindowBottom();
  public int  getWindowLeft();
  public int  getWindowRight();

  public int getCursorV();
  public int getCursorH();

  public void cursorSetPos(int v, int h, boolean relative);
  public void cursorUp(int n);
  public void cursorDown(int n);
  public void cursorForward(int n);
  public void cursorBackward(int n);
  public void cursorIndex(int n);
  public void cursorIndexRev(int n);

  public void cursorSave();
  public void cursorRestore();

  public void scrollUp(int n);
  public void scrollDown(int n);

  public void clearBelow();
  public void clearAbove();
  public void clearScreen();
  public void clearRight();
  public void clearLeft();
  public void clearLine();

  public void eraseChars(int n);
  public void insertChars(int n);
  public void insertLines(int n);
  public void deleteChars(int n);
  public void deleteLines(int n);

  public void    setOption(int opt, boolean val);
  public boolean getOption(int opt);

  public void    setAttribute(int attr, boolean val);
  public boolean getAttribute(int attr);
  public void    setForegroundColor(int c);
  public void    setBackgroundColor(int c);
  public void    clearAllAttributes();

  public void setProperties(Properties newProps, boolean merge) throws IllegalArgumentException,
    NoSuchElementException;
  public Properties getProperties();
  public boolean getPropsChanged();
  public void  setPropsChanged(boolean value);



}
