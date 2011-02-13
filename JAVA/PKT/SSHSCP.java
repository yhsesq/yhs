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
package mindbright.ssh;

import java.io.*;
import java.net.Socket;

import mindbright.terminal.Terminal;
import mindbright.security.Cipher;

public class SSHSCP extends SSHClientUserAdaptor implements SSHConsole {

    SSHClient       client;
    SSHInteractor   interactor = null;
    SSHClientUser   proxyUser  = null;
    SSHSCPIndicator indicator  = null;

    SSHInteractor   ourInteractAdapter = null;

    File    cwd;
    boolean recursive;
    boolean verbose;

    String sshHost;

    PipedInputStream  inTop;
    PipedOutputStream inBottom;

    public SSHSCP(String sshHost, int port, SSHAuthenticator authenticator,
		  File cwd, boolean recursive, boolean verbose) throws IOException {
	super(sshHost, port);

	// OUCH: Note must be set before constructing SSHClient since
	// its constructor calls getInteractor to fetch this
	//
	ourInteractAdapter = new SSHInteractorAdapter() {
	    public void open(SSHClient client) {
		SSHSCP.this.open(client);
	    }
	    public void disconnected(SSHClient client, boolean graceful) {
		SSHSCP.this.disconnected(client, graceful);
	    }
	    public void alert(String msg) {
		SSHSCP.this.alert(msg);
	    }
	};

	client    = new SSHClient(authenticator, this);
	inTop     = new PipedInputStream();
	inBottom  = new PipedOutputStream(inTop);
	this.cwd       = cwd;
	this.recursive = recursive;
	this.verbose   = verbose;
	this.sshHost   = sshHost;

	client.setConsole(this);
	client.activateTunnels = false;
    }

    public void setInteractor(SSHInteractor interactor) {
	this.interactor = interactor;
    }

    public void setClientUser(SSHClientUser proxyUser) {
	this.proxyUser = proxyUser;
    }

    public void setIndicator(SSHSCPIndicator indicator) {
	this.indicator = indicator;
    }

    public void abort() {
	interactor = null;
	indicator  = null;
	client.forcedDisconnect();
    }

    public void copyToRemote(String localFile, String remoteFile) throws IOException {
	File lf = new File(localFile);

	if(!lf.isAbsolute())
	    lf = new File(cwd, localFile);

	if(!lf.exists()) {
	    throw new IOException("File: " + localFile + " does not exist");
	}
	if(!lf.isFile() && !lf.isDirectory()) {
	    throw new IOException("File: " + localFile + " is not a regular file or directory");
	}
	if(lf.isDirectory() && !recursive) {
	    throw new IOException("File: " + localFile + " is a directory, use recursive mode");
	}
	if(remoteFile == null || remoteFile.equals(""))
	    remoteFile = ".";

	client.doSingleCommand("scp -B " + (lf.isDirectory() ? "-d " : "") + "-t " +
			       (recursive ? "-r " : "") + (verbose ? "-v " : "") + remoteFile, true, 0);
	readResponse("After starting remote scp");
	writeFileToRemote(lf);
	client.forcedDisconnect();
    }

    public void copyToRemote(String[] localFiles, String remoteFile) throws IOException {
	if(remoteFile == null || remoteFile.equals(""))
	    remoteFile = ".";
	if(localFiles.length == 1) {
	    copyToRemote(localFiles[0], remoteFile);
	} else {
	    client.doSingleCommand("scp -B " + "-d -t " + (recursive ? "-r " : "") +
				   (verbose ? "-v " : "") + remoteFile, true, 0);
	
	    readResponse("After starting remote scp");
	    for(int i = 0; i < localFiles.length; i++) {
		File lf = new File(localFiles[i]);
		if(!lf.isAbsolute())
		    lf = new File(cwd, localFiles[i]);
		if(!lf.isFile() && !lf.isDirectory()) {
		    alert("File: " + lf.getName() + " is not a regular file or directory");
		    continue;
		}
		writeFileToRemote(lf);
	    }
	    client.forcedDisconnect();
	}
    }

    public void copyToLocal(String localFile, String remoteFile) throws IOException {
		String[] localFileList = new String[ 1 ];
		localFileList[ 0 ] = localFile;
		copyToLocal( localFileList, remoteFile );
    }

    public void copyToLocal(String[] localFileList, String remoteFile) throws IOException {
	client.doSingleCommand("scp -B " + "-f " + (recursive ? "-r " : "") + (verbose ? "-v " : "") +
			       remoteFile, true, 0);

	for( int i = 0; i < localFileList.length; i++ ) {

	File lf = new File(".");
	if(!lf.isAbsolute())
	    lf = new File(cwd, ".");

	if(lf.exists() && !lf.isFile() && !lf.isDirectory()) {
	    throw new IOException("File: " + localFileList[ i ] + " is not a regular file or directory");
	}

	readFromRemote(lf);
	}
	client.forcedDisconnect();
    }

    boolean writeDirToRemote(File dir) throws IOException {
	if(!recursive) {
	    writeError("File " + dir.getName() + " is a directory, use recursive mode");
	    return false;
	}
	writeString("D0755 0 " + dir.getName() + "\n");
	if(indicator != null)
	    indicator.startDir(dir.getAbsolutePath());
	readResponse("After sending dirdata");
	String[] dirList = dir.list();
	for(int i = 0; i < dirList.length; i++) {
	    File f = new File(dir, dirList[i]);
	    writeFileToRemote(f);
	}
	writeString("E\n");
	if(indicator != null)
	    indicator.endDir();
	return true;
    }

    void writeFileToRemote(File file) throws IOException {
	if(file.isDirectory()) {
	    if(!writeDirToRemote(file))
		return;
	} else if(file.isFile()) {
	    writeString("C0644 " + file.length() + " " + file.getName() + "\n");
	    if(indicator != null)
		indicator.startFile(file.getName(), (int)file.length());
	    readResponse("After sending filedata");
	    FileInputStream fi = new FileInputStream(file);
	    writeFully(fi, (int)file.length());
	    writeByte(0);
	    if(indicator != null)
		indicator.endFile();
	} else {
	    throw new IOException("Not ordinary file: " + file.getName());
	}
	readResponse("After writing file");
    }

    void readFromRemote(File file) throws IOException {
	String   cmd;
	String[] cmdParts = new String[3];
	writeByte(0);
	while(true) {
	    do {
		try {
		    cmd = readString();
		} catch (EOFException e) {
		    return;
		}
	    } while(cmd == null);
	    char cmdChar = cmd.charAt(0);
	    switch(cmdChar) {
	    case 'E':
		writeByte(0);
		return;
	    case 'T':
		// !!!
		System.out.println("(T)ime not supported: " + cmd);
		break;
	    case 'C':
	    case 'D':
		String targetName = file.getAbsolutePath();
		parseCommand(cmd, cmdParts);
		if(file.isDirectory()) {
		    targetName += File.separator + cmdParts[2];
		}
		File targetFile = new File(targetName);
		if(cmdChar == 'D') {
		    if(targetFile.exists()) {
			if(!targetFile.isDirectory())
			    writeError("Invalid target " + targetFile.getName() +
				       ", must be a directory");
		    } else {
			if(!targetFile.mkdir())
			    writeError("Could not create directory: " + targetFile.getName());
		    }
		    if(indicator != null)
			indicator.startDir(targetFile.getAbsolutePath());
		    readFromRemote(targetFile);
		    if(indicator != null)
			indicator.endDir();
		    continue;
		}
		FileOutputStream fo = new FileOutputStream(targetFile);
		writeByte(0);
		int len = Integer.parseInt(cmdParts[1]);
		if(indicator != null)
		    indicator.startFile(targetFile.getName(), len);
		readFully(fo, len);
		readResponse("After reading file");
		if(indicator != null)
		    indicator.endFile();
		writeByte(0);
		break;
	    default:
		writeError("Unexpected cmd: " + cmd);
		throw new IOException("Unexpected cmd: " + cmd);
	    }
	}
    }

    void parseCommand(String cmd, String[] cmdParts) throws IOException {
	int l, r;
	l = cmd.indexOf(' ');
	r = cmd.indexOf(' ', l + 1);
	if(l == -1 || r == -1) {
	    writeError("Syntax error in cmd");
	    throw new IOException("Syntax error in cmd");
	}
	cmdParts[0] = cmd.substring(1, l);
	cmdParts[1] = cmd.substring(l + 1, r);
	cmdParts[2] = cmd.substring(r + 1);
    }

    void readResponse(String where) throws IOException {
	int r = readByte();
	if(r == 0) {
	    // All is well, no error
	    return;
	}
	String errMsg = readString();
	if(r == (byte)'\02')
	    throw new IOException(errMsg);
	alert(errMsg);
    }

    void writeError(String reason) throws IOException {
	writeByte(1);
	writeString(reason);
	alert(reason);
    }

    int readByte() throws IOException {
	return inTop.read();
    }

    String readString() throws IOException {
	byte[] buf = new byte[2048];
	int ch, i = 0;
	while(((ch = readByte()) != ((int)'\n')) && ch >= 0) {
	    buf[i++] = (byte)ch;
	}
	if(ch == -1) {
	    throw new EOFException();
	}
	if(buf[0] == (byte)'\n')
	    throw new IOException("Unexpected <NL>");
	if(buf[0] == (byte)'\02' || buf[0] == (byte)'\01') {
	    // !!!
	    String errMsg = new String(buf, 1, i - 1);
	    if(buf[0] == (byte)'\02')
		throw new IOException(errMsg);
	    alert(errMsg);
	    return null;
	}
	return new String(buf, 0, i);
    }

    void readFully(FileOutputStream file, int size) throws IOException {
	byte[] buf = new byte[2048];
	int cnt = 0, n;
	while(cnt < size) {
	    n = inTop.read(buf, 0, ((size - cnt) < 2048 ? (size - cnt) : 2048));
	    if(n == -1) {
		alert("Premature EOF");
		throw new IOException("Premature EOF");
	    }
	    cnt += n;
	    file.write(buf, 0, n);
	    if(indicator != null)
		indicator.progress(n);
	}
	file.close();
    }

    void writeByte(int b) throws IOException {
	byte[] buf = new byte[1];
	buf[0] = (byte)b;
	client.stdinWriteString(buf);
    }

    void writeString(String str) throws IOException {
	byte[] buf = str.getBytes();
	client.stdinWriteString(buf);
    }

    void writeFully(FileInputStream file, int size) throws IOException {
	byte[] buf = new byte[2048];
	int cnt = 0, n;
	while(cnt < size) {
	    n = file.read(buf, 0, ((size - cnt) < 2048 ? (size - cnt) : 2048));
	    if(n == -1)
		throw new IOException("Premature EOF");
	    cnt += n;
	    client.stdinWriteString(buf, 0, n);
	    if(indicator != null)
		indicator.progress(n);
	    Thread.yield();
	}
	file.close();
    }

    public void stdoutWriteString(byte[] str) {
	try {
	    inBottom.write(str);
	} catch(IOException e) {
	    try {
		inBottom.close();
	    } catch (IOException ee) {
		// !!!
	    }
	    alert("Error writing data to stdout-pipe");
	}
    }

    public void stderrWriteString(byte[] str) {
	if(verbose) alert("Remote warning/error: " + new String(str));
    }

    public Terminal getTerminal() {
	return null;
    }
    public void print(String str) {
    }
    public void println(String str) {
    }
    public void serverConnect(SSHChannelController controller, Cipher sndCipher) {
    }
    public void serverDisconnect(String reason) {
    }
    public boolean wantPTY() {
	return false;
    }
    public void open(SSHClient client) {
	if(indicator != null)
	    indicator.connected(sshHost);
    }
    public void disconnected(SSHClient client, boolean graceful) {
	try {
	    inBottom.close();
	} catch (IOException e) {
	    // !!!
	}
    }
    public void alert(String msg) {
	if(interactor != null) {
	    interactor.alert(msg);
	}
    }

    public Socket getProxyConnection() throws IOException {
	if(proxyUser != null) {
	    return proxyUser.getProxyConnection();
	}
	return null;
    }

    public SSHInteractor getInteractor() {
	return ourInteractAdapter;
    }

    /* !!! TESTING !!!
    public static void main(String[] argv) {
	try {
	    SSHSCP scp = new SSHSCP("hal", 22, new SSHPasswordAuthenticator("mats", "********"),
	                            new File("."), true, true);
	    String[] files = { "foo.h", "foo.c" };
	    scp.copyToRemote(files, "");
	    scp.copyToRemote(files, "foodir");
	    scp.copyToRemote("dummies/", "");
	    scp.copyToLocal("bar.c", "foo.c");
	    scp.copyToLocal("", "foodir");
	    System.exit(0);
	} catch (IOException e) {
	    System.out.println("Error here: " + e);
	    e.printStackTrace();
	}
    }
    */

}
