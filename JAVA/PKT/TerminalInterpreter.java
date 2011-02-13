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

public abstract class TerminalInterpreter {

  protected Terminal term;

  public final static int IGNORE = -1;

  abstract public String terminalType();
  abstract public int interpretChar(char c);

  public void vtReset() {
  }

  public void keyHandler(int virtualKey, int gMode) {
  }

  public void mouseHandler(int x, int y, boolean press, int modifiers) {
  }

  public final void setTerminal(Terminal term) {
    this.term = term;
  }

}
