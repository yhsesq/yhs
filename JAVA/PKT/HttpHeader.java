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
package mindbright.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

import mindbright.util.Base64;

public class HttpHeader {
    String    startLine;
    Hashtable headerFields;
    boolean   isResponse;

    private final String processLine(String line, String lastHeaderName) throws IOException {
	String name, value;
	char c = line.charAt(0);
	if(c == ' ' || c == '\t') {
	    name  = lastHeaderName;
	    value = getHeaderField(lastHeaderName) + " " + line.trim();
	} else {
	    int n = line.indexOf(':');
	    if(n == -1)
		throw new IOException("HttpHeader, corrupt header-field: '" + line + "'");
	    name  = line.substring(0, n).toLowerCase();
	    value = line.substring(n + 1).trim();
	}

	setHeaderField(name, value);
	return name;
    }

    public HttpHeader(String fullHeader) throws IOException {
	this();
	String  lastHeaderName = null;
	boolean readStart      = false;
	int l = 0, r = 0, n = 0;
	while(true) {
	    r = fullHeader.indexOf("\r\n", l);
	    if(l == r || r == -1)
		break;
	    String line = fullHeader.substring(l, r);
	    if(readStart) {
		lastHeaderName = processLine(line, lastHeaderName);
	    } else {
		startLine = line;
		readStart = true;
	    }
	    l = r + 2;
	}
    }

    public HttpHeader(InputStream input) throws IOException {
	this();
        StringBuffer lineBuf        = new StringBuffer();
	boolean      readStart      = false;
	String       lastHeaderName = null;
	int c;
        while(true) {
	    c = input.read();
	    if(c == -1)
		throw new IOException("HttpHeader, corrupt header, input stream closed");
	    if(c == '\n')
	       continue;
            if(c != '\r') {
                lineBuf.append((char)c); 
            } else {
                if(lineBuf.length() != 0) {
		    String line = new String(lineBuf);
                    if(readStart) {
			lastHeaderName = processLine(line, lastHeaderName);
                    } else {
			startLine = line;
			readStart = true;
		    }
		    lineBuf.setLength(0);
                } else {
                    break;
		}
            }
	}
	/* Strip the last \n */
	c = input.read();
    }

    public HttpHeader() {
	headerFields = new Hashtable();
    }

    public String getStartLine() {
	return startLine;
    }

    public void setStartLine(String startLine) {
	this.startLine = startLine;
    }

    public Hashtable getHeaderFields() {
	return headerFields;
    }

    public String getHeaderField(String headerName) {
	return (String)headerFields.get(headerName.toLowerCase());
    }

    public void setHeaderField(String headerName, String value) {
	headerFields.put(headerName.toLowerCase(), value);
    }

    public void writeTo(OutputStream output) throws IOException {
	String fullHeader = toString();
	output.write(fullHeader.getBytes());
	output.flush();
    }

    public int getStatus() {
	int pos;
	int status = -1;
	if((pos = startLine.indexOf(" ")) > 0) {
	    try {
		status = new Integer(startLine.substring(pos + 1, pos + 4)).intValue();
	    } catch (NumberFormatException e) {
		status = -1;
	    }
	}
	return status;
    }

    public void setBasicProxyAuth(String username, String password) {
	String authStr = username + ":" + password;
	byte[] authB64enc = Base64.encode(authStr.getBytes());
	setHeaderField("Proxy-Authorization", "Basic " +
		       (new String(authB64enc)));
    }

    public String getProxyAuthMethod() {
	String challenge = getHeaderField("Proxy-Authenticate");
	String method    = null;
	if(challenge != null) {
	    int n = challenge.indexOf(' ');
	    method = challenge.substring(0, n);
	}
	return method;
    }

    public String getProxyAuthRealm() {
	String challenge = getHeaderField("Proxy-Authenticate");
	String realm = null;
	if(challenge != null) {
	    int l, r = challenge.indexOf('=');
	    while(r >= 0) {
		l = challenge.lastIndexOf(' ', r);
		realm = challenge.substring(l + 1, r);
		if(realm.equalsIgnoreCase("realm")) {
		    l = r + 2;
		    r = challenge.indexOf('"', l);
		    realm = challenge.substring(l, r);
		    break;
		}
		r = challenge.indexOf('=', r + 1);
	    }
	}
	return realm;
    }

    public String toString() {
	String fullHeader = startLine + "\r\n";
	Enumeration headerNames = headerFields.keys();
	while(headerNames.hasMoreElements()) {
	    String fieldName = (String)headerNames.nextElement();
	    fullHeader += fieldName + ": " + headerFields.get(fieldName) + "\r\n";
	}
	fullHeader += "\r\n";
	return fullHeader;
    }

    /* !!! REMOVE
    public static void main(String[] argv) {

	try {
	HttpHeader requestHeader = new HttpHeader();

	requestHeader.setStartLine("CONNECT " + "www.foobar.com" + ":" + 4711 + " HTTP/1.0");
	requestHeader.setHeaderField("User-Agent", "Proxologist/0.1");
	requestHeader.setHeaderField("Pragma", "No-Cache");
	requestHeader.setHeaderField("Proxy-Connection", "Keep-Alive");

	String authStr    = "foobar" + ":" + "zippo";
	byte[] authB64enc = Base64.encode(authStr.getBytes());
	requestHeader.setHeaderField("Proxy-Authorization", "Basic " + (new String(authB64enc)));

	System.out.println("HTTP header:");

	String req = requestHeader.toString();
	System.out.print(req);

	requestHeader = new HttpHeader(req);

	System.out.println("HTTP startline: " + requestHeader.getStartLine());
	System.out.println("HTTP proxy-auth: " + requestHeader.getHeaderField("proxy-authorization"));
	} catch (Exception e) {
	    System.out.println("Error: " + e);
	}
    }
    */

}
