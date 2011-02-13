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

import java.util.Properties;
import java.awt.Toolkit;

// !!! KLUDGE

public abstract class TerminalDefProps {

    // !!! OUCH (we don't want to drag in TerminalXTerm...
    //
    public final static String[] terminalTypes = {
	"xterm", "linux", "scoansi",  "att6386", "sun", "aixterm",
	"vt220", "vt100", "ansi",  "vt52",

	"xterm-color", "linux-lat", "", "at386", "", "", "vt320", "vt102"
    };
    public static String listAvailableTerminalTypes() {
	int i;
	String list = " ";
	for(i = 0; i < terminalTypes.length; i++)
	    list += terminalTypes[i] + " ";
	return list;
    }

    static public final int PROP_NAME    = 0;
    static public final int PROP_VALUE   = 1;
    static public final int PROP_DESC    = 2;
    static public final int PROP_ALLOWED = 3;
    static public Properties defaultProperties = new Properties();
    static public final String[][] defaultPropDesc = {
	// Options
	{ "rv", "false", "reverse video", "(true/false)"},
	{ "aw", "true",  "autowrap of line if output reaches edge of window", "(true/false)" },
	{ "rw", "false", "reverse autowrap when going off left edge of window", "(true/false)" },
	{ "im", "false", "insert mode", "(true/false)" },
	{ "al", "false", "do auto-linefeed", "(true/false)" },
	{ "sk", "true",  "reposition scroll-area to bottom on keyboard input", "(true/false)" },
	{ "si", "true",  "reposition scroll-area to bottom on output to screen", "(true/false)" },
	{ "lp", "false", "use PgUp, PgDn, Home, End keys locally or escape them to shell", "(true/false)" },
	{ "sc", "false", "put <CR><NL> instead of <CR> at end of lines when selecting", "(true/false)" },
	{ "vi", "true",  "visible cursor", "(true/false)" },
	{ "ad", "false", "ASCII Line-draw-characters", "(true/false)" },
	{ "le", "false", "do local echo", "(true/false)" },
	{ "sf", "false", "scale font when resizing window", "(true/false)" },
	{ "vb", "false", "visual bell", "(true/false)" },
	{ "ct", "true",  "map <ctrl>+<space> to <NUL>", "(true/false)" },
	{ "dc", "false", "toggle 80/132 columns", "(true/false)" },
	{ "da", "true", "enable 80/132 switching", "(true/false)" },
	{ "cs", "true", "copy on mouse-selection", "(true/false)" },
	// Settings
	{ "fn", defaultFont(), "name of font to use in terminal", ("(" + fontList() + ")") },
	{ "fs", "12",      "size of font to use in terminal", "(system dep.)" },
	{ "gm", "80x24", "geometry of terminal", "('<cols>x<rows>')" },
	{ "te", terminalTypes[0],
	  "name of terminal to emulate", ("(" + listAvailableTerminalTypes() + ")") },
	{ "sl", "512",     "number of lines to save in \"scrollback\" buffer", "(0 - 8k)" },
	{ "sb", "right",   "scrollbar position", "(none/left/right)" },
	{ "bg", "white",   "background color", "(<name> or '<r>,<g>,<b>')" },
	{ "fg", "black",   "foreground color", "(<name> or '<r>,<g>,<b>')" },
	{ "cc", "i_blue",    "cursor color", "(<name> or '<r>,<g>,<b>')" },
	{ "rg", "bottom",   "resize gravity, fixpoint of screen when resizing", "(top/bottom)" },
	{ "bs", "DEL",   "character to send on BACKSPACE", "('BS' or 'DEL')" },
	{ "de", "DEL",   "character to send on DELETE", "('BS' or 'DEL')" },
	{ "sd", "\" \"", "delimeter characters for click-selection", "<string>" },
    };
    static {
	for(int i = 0; i < defaultPropDesc.length; i++)
	    defaultProperties.put(defaultPropDesc[i][PROP_NAME], defaultPropDesc[i][PROP_VALUE]);
    }

    public static String[] systemFonts;
    public static String fontList() {
	if(systemFonts == null)
	    systemFonts = Toolkit.getDefaultToolkit().getFontList();
	String list = "";
	for(int i = 0; i < systemFonts.length; i++) {
	    list += systemFonts[i];
	    if(i < systemFonts.length - 1)
		list += ", ";
	}
	return list;
    }

    public static String defaultFont() {
	if(fontExists("monospaced"))
	    return "Monospaced";
	if(fontExists("courier"))
	    return "Courier";
	if(fontExists("dialoginput"))
	    return "DialogInput";
	return systemFonts[0];
    }

    public static boolean fontExists(String font) {
	int i;
	if(systemFonts == null)
	    systemFonts = Toolkit.getDefaultToolkit().getFontList();
	for(i = 0; i < systemFonts.length; i++) {
	    if(systemFonts[i].equalsIgnoreCase(font))
		break;
	}
	if(i == systemFonts.length)
	    return false;
	return true;
    }

    public static boolean isProperty(String key) {
	return defaultProperties.containsKey(key);
    }
}

