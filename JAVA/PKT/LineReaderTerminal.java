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

public final class LineReaderTerminal implements TerminalListener {

    TerminalWin     terminal;
    StringBuffer    readLineStr;
    boolean         echoStar;
    boolean         isReadingLine;

    ExternalMessageException extMsg;

    static public class ExternalMessageException extends Exception {
	public ExternalMessageException(String msg) {
	    super(msg);
	}
    }

    public LineReaderTerminal(TerminalWin terminal) {
	this.terminal = terminal;
	terminal.addTerminalListener(this);
    }

    public void print(String str) {
	if(terminal != null) {
	    terminal.write(str);
	} else {
	    System.out.print(str);
	}
    }

    public void println(String str) {
	if(terminal != null) {
	    terminal.write(str + "\n\r");
	} else {
	    System.out.println(str);
	}
    }

    public void breakPromptLine(String msg) {
	if(isReadingLine) {
	    synchronized(this) {
		extMsg = new ExternalMessageException(msg);
		this.notify();
	    }
	}
    }

    public String readLine(String defaultVal) {
	synchronized(this) {
	    if(defaultVal != null) {
		readLineStr = new StringBuffer(defaultVal);
		terminal.write(defaultVal);
	    } else {
		readLineStr   = new StringBuffer();
	    }
	    isReadingLine = true;
	    try {
		this.wait();
	    } catch (InterruptedException e) {
		/* don't care */
	    }
	    isReadingLine = false;
	}
	return readLineStr.toString();
    }

    public String promptLine(String prompt, String defaultVal, boolean echoStar)
	throws ExternalMessageException
    {
	String line = null;
	if(terminal != null) {
	    terminal.setAttribute(Terminal.ATTR_BOLD, true);
	    terminal.write(prompt);
	    terminal.setAttribute(Terminal.ATTR_BOLD, false);
	    this.echoStar = echoStar;
	    line = readLine(defaultVal);
	    this.echoStar = false;
	} /*
	    else {
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    System.out.print(prompt);
	    line = br.readLine();
	    }
	  */
	if(extMsg != null) {
	    ExternalMessageException msg = extMsg;
	    extMsg = null;
	    throw msg;
	}
	return line;
    }

    // TerminalListener interface
    //
    public synchronized void typedChar(char c) {
	if(isReadingLine) {
	    if(c == (char)127 || c == (char)0x08) {
		if(readLineStr.length() > 0) {
		    boolean ctrlChar = false;
		    if(readLineStr.charAt(readLineStr.length() - 1) < ' ') {
			ctrlChar = true;
		    }
		    readLineStr.setLength(readLineStr.length() - 1);
		    terminal.write((char)8);
		    if(ctrlChar) terminal.write((char)8);
		    terminal.write(' ');
		    if(ctrlChar) terminal.write(' ');
		    terminal.write((char)8);
		    if(ctrlChar) terminal.write((char)8);
		} else
		    terminal.doBell();
	    } else if(c == '\r') {
		this.notify();
		terminal.write("\n\r");
	    } else {
		readLineStr.append(c);
		if(echoStar)
		    terminal.write('*');
		else
		    terminal.write(c);
	    }
	}
    }

    public void sendBytes(byte[] b) {
	for(int i = 0; i < b.length; i++)
	    typedChar((char)b[i]);
    }

    public void signalWindowChanged(int rows, int cols, int vpixels, int hpixels) {
    }
    public void setSelection(String selection) {
    }
    public String getSelection() {
	return null;
    }
    public void selectionAvailable(boolean val) {
    }

    public static void main(String[] argv) {
	java.awt.Frame frame = new java.awt.Frame();
	TerminalWin terminal = new TerminalWin(frame, new TerminalXTerm());
	LineReaderTerminal linereader = new LineReaderTerminal(terminal);

	frame.setLayout(new java.awt.BorderLayout());
	frame.add(terminal.getPanelWithScrollbar(),
		  java.awt.BorderLayout.CENTER);

	frame.pack();
	frame.show();

	linereader.println("Now entering lines...");
	String line;
	try {
	    while(true) {
		line = linereader.promptLine("prompt> ", "", false);
		System.out.println("line: " + line);
	    }
	} catch (Exception e) {
	    System.out.println("Error: " + e);
	}
    }

}
