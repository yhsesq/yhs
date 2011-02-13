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

public final class TerminalDumb extends TerminalInterpreter {

  public String terminalType() {
    return "DUMB";
  }

  public int interpretChar(char c) {
    switch(c) {
    case 7: // BELL
      term.doBell();
      break;
    case 8: // BS/CTRLH
      term.doBS();
      break;
    case '\t':
      term.doTab();
      break;
    case '\r':
      term.doLF();
      break;
    case '\n':
      term.doCR();
      break;
    default:
      return (int)c;
    }
    return IGNORE;
  }

}
