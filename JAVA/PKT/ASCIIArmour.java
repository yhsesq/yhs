/******************************************************************************
 *
 * Copyright (c) 2000 by Mindbright Technology AB, Stockholm, Sweden.
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
package mindbright.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

public final class ASCIIArmour {
    public final static int DEFAULT_LINE_LENGTH = 70;

    String    EOL;
    String    headerLine;
    Hashtable headerFields;
    String    tailLine;

    boolean   blankHeaderSep;
    int       lineLen;
    boolean   haveChecksum;


    boolean   unknownHeaderLines;
    String    headerLinePrePostFix;

    public ASCIIArmour(String headerLine, String tailLine,
		       boolean blankHeaderSep, int lineLen) {
	this.EOL                = "\r\n";
	this.headerLine         = headerLine;
	this.tailLine           = tailLine;
	this.blankHeaderSep     = blankHeaderSep;
	this.lineLen            = lineLen;
	this.unknownHeaderLines = false;
	this.headerFields       = new Hashtable();
    }

    public ASCIIArmour(String headerLine, String tailLine) {
	this(headerLine, tailLine, false, DEFAULT_LINE_LENGTH);
    }

    public ASCIIArmour(String headerLinePrePostFix) {
	this(headerLinePrePostFix, headerLinePrePostFix);
	this.unknownHeaderLines   = true;
    }

    public void setCanonicalLineEnd(boolean value) {
	if(value) {
	    EOL = "\r\n";
	} else {
	    EOL = "\n";
	}
    }

    public Hashtable getHeaderFields() {
	return headerFields;
    }

    public String getHeaderField(String headerName) {
	return (String)headerFields.get(headerName);
    }

    public void setHeaderField(String headerName, String value) {
	headerFields.put(headerName, value);
    }

    public byte[] encode(byte[] data) {
	return encode(data, 0, data.length);
    }

    public byte[] encode(byte[] data, int offset, int length) {
	if(unknownHeaderLines) {
	    return null;
	}
	int n = ((length / 3) * 4);
	StringBuffer buf = new StringBuffer(headerLine.length() +
					    tailLine.length() +
					    n + (n / lineLen) + 512);
	buf.append(headerLine);
	buf.append(EOL);

	buf.append(printHeaders());

	if(blankHeaderSep)
	    buf.append(EOL);
	byte[] base64 = Base64.encode(data, offset, length);
	for(int i = 0; i < base64.length; i += lineLen) {
	    int j = lineLen;
	    if(i + j > base64.length)
		j = base64.length - i;
	    String line = new String(base64, i, j);
	    buf.append(line);
	    buf.append(EOL);
	}
	if(haveChecksum) {
	    // !!! TODO:
	}
	buf.append(tailLine);
	buf.append(EOL);

	return buf.toString().getBytes();
    }

    public void encode(OutputStream out, byte[] data, int off, int len)
	throws IOException
    {
	byte[] outData = encode(data, off, len);
	out.write(outData);
    }

    public void encode(OutputStream out, byte[] data) throws IOException {
	encode(out, data, 0, data.length);
    }

    public byte[] decode(byte[] data) {
	return decode(data, 0, data.length);
    }

    public byte[] decode(byte[] data, int offset, int length) {
	String armourChunk = new String(data, offset, length);
	StringTokenizer st = new StringTokenizer(armourChunk, "\n");
	boolean foundHeader = false;
	boolean foundData   = false;
	boolean foundTail   = false;
	String line = "";
	while(!foundHeader && st.hasMoreTokens()) {
	    line = st.nextToken();
	    if(line.startsWith(headerLine)) {
		foundHeader = true;
		// !!! TODO: if(unknownHeaderLines) {
	    }
	}
	headerFields = new Hashtable();
	String lastName = null;
	while(!foundData && st.hasMoreTokens()) {
	    line = st.nextToken();
	    if(lastName != null) {
		String val = (String)headerFields.get(lastName);
		headerFields.put(lastName, val + StringUtil.trimRight(line));
		lastName = null;
		continue;
	    }
	    int i = line.indexOf(':');
	    if(i < 0) {
		foundData = true;
	    } else {
		String name  = line.substring(0, i).trim();
		String value = line.substring(i + 1).trim();
		if(value.charAt(0) == '"' &&
		   value.charAt(value.length() - 1) == '\\') {
		    lastName = name;
		    value = value.substring(0, value.length() - 1);
		}
		headerFields.put(name, value);
	    }
	}
	if(blankHeaderSep) {
	    // !!!
	}
	StringBuffer base64Data = new StringBuffer();
	while(!foundTail) {
	    if(line.startsWith(tailLine)) {
		foundTail = true;
		// !!! TODO: if(unknownHeaderLines) {
	    } else {
		base64Data.append(line);
		if(st.hasMoreTokens())
		    line = st.nextToken();
		else
		    return null;
	    }
	}

	data = Base64.decode(base64Data.toString().getBytes());

	return data;
    }

    public byte[] decode(InputStream in) throws IOException {
        StringBuffer lineBuf    = new StringBuffer();
        StringBuffer dataBuf    = new StringBuffer();
	int          found      = 0;
	int c;
	while(found < 2) {
	    c = in.read();
	    if(c == -1)
		throw new IOException("Premature EOF, corrupt ascii-armour");
	    if(c == '\r')
	       continue;
            if(c != '\n') {
                lineBuf.append((char)c); 
            } else {
		String line = new String(lineBuf);
		if(found == 0) {
		    if(line.startsWith(headerLine)) {
			dataBuf.append(line);
			dataBuf.append(EOL);
			found++;
		    }
		} else {
		    dataBuf.append(line);
		    dataBuf.append(EOL);
		    if(line.startsWith(tailLine)) {
			found++;
		    }
		}
		lineBuf.setLength(0);
	    }
	}
	return decode(dataBuf.toString().getBytes());
    }

    public String printHeaders() {
	Enumeration headerNames = headerFields.keys();
	StringBuffer buf = new StringBuffer();
	while(headerNames.hasMoreElements()) {
	    String fieldName = (String)headerNames.nextElement();
	    buf.append(fieldName);
	    buf.append(": ");
	    String val = (String)headerFields.get(fieldName);
	    if(val.charAt(0) == '"' &&
	       fieldName.length() + 2 + val.length() > lineLen) {
		int n = lineLen - (fieldName.length() + 2);
		buf.append(val.substring(0, n));
		buf.append("\\");
		buf.append(EOL);
		val = val.substring(n);
	    }
	    buf.append(val);
	    buf.append(EOL);
	}
	return buf.toString();
    }

    public static void main(String[] argv) {
	byte[] data = "Hej svejs i lingonskogen!!!".getBytes();
	ASCIIArmour armour =
	    new ASCIIArmour("---- BEGIN GARBAGE ----",
			    "---- END GARBAGE ----");
	armour.setHeaderField("Subject", "mats");
	armour.setHeaderField("Comment", "\"this is a comment\"");

	byte[] encoded = armour.encode(data);

	System.out.println("Encoded block:");
	System.out.println(new String(encoded));

	System.out.println("Decoded: " + new String(armour.decode(encoded)));
	System.out.println("Headers:");
	System.out.println(armour.printHeaders());

	encoded = ("---- BEGIN SSH2 PUBLIC KEY ----\r\n" +
	    "Subject: root\r\n" +
	    "Comment: \"host key for hal, accepted by root Mon Sep 20 1999 10:10:02 +0100\"\r\n" +
	    "AAAAB3NzaC1kc3MAAACBAKpCbpj86G+05T53tn6Y+tJ1N87Kx2RbQTDC48LWHYNRZ3c4He\r\n" +
	    "0tmQNFbyg14m/dYrdBI0GxPWQH0RYuyL5YLhBrcscmdz7Ca8buEgehcQULlAJ1P0gZ3hvW\r\n" +
	    "qru55vgU8O0kZVNGSsA+cmXRpq689W6RU0u9qaW03FNdeH7tTq/1AAAAFQDCLg54vUWNe0\r\n" +
	    "n5kMFnEH/DiV5dgQAAAIEAmlOAXHQ/3nrFDnLiTIfCkCvAj/P2rMQUViYXXi9cQ+Qd8Ie5\r\n" +
	    "TmyFJ6t9iJQZ6x3HlScGfQOJcD4h4ydxuXr+rRd6yi48kSB5/g3EscL+6+LMYdMGSGA2ni\r\n" +
	    "l1Vpjm49xZHxHlvTQ+KExk6Pcyb9D5zTW9uoOTBA08SPpYAlbZ4+MAAACAKEeiebGmZg5x\r\n" +
	    "sbxQt6HUPU3Cov9KeXw98qmn4Rr2ENWSTriwl8uxoD8wCuURHaJ61YX5spAj4QkVESqc7Y\r\n" +
	    "NBcZgpST0sUWCF0rNPZm8D6K0hgaUmtfrUJ6EzwxqfKH3YduMHFz5RSv492TSZvKKv+Ucb\r\n" +
	    "X4hEjfmP6SKc+Q4wGaQ=\r\n" +
	    "---- END SSH2 PUBLIC KEY ----\r\n").getBytes();
	armour =
	    new ASCIIArmour("---- BEGIN SSH2 PUBLIC KEY ----",
			    "---- END SSH2 PUBLIC KEY ----");

	byte[] decoded = null;
	try {
	    armour =
		new ASCIIArmour("---- BEGIN SSH2 ENCRYPTED PRIVATE KEY ----",
				"---- END SSH2 ENCRYPTED PRIVATE KEY ----");
	    decoded = armour.decode(new java.io.FileInputStream("/home/matsa/tstkey.prv"));
	} catch (Exception e) {
	    System.out.println("Error: " + e);
	}

	System.out.println("Decoded: ");
	mindbright.util.HexDump.hexDump(decoded, 0, decoded.length);
	System.out.println("Headers:");
	System.out.println(armour.printHeaders());

	try {
	    java.io.FileOutputStream f =
		new java.io.FileOutputStream("/home/matsa/tstkey2.prv");
	    armour.setCanonicalLineEnd(false);
	    armour.encode(f, decoded);
	    f.close();
	} catch (Exception e) {
	    System.out.println("Error: " + e);
	}
    }

}

