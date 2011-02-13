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
package mindbright.application;

import java.io.*;

import java.awt.*;
import java.awt.event.*;

import mindbright.ssh.*;
import mindbright.security.*;
import mindbright.terminal.*;

public class MindTunnel {

  public static void main(String[] argv) {
    MindTunnel controller = new MindTunnel();
    controller.startup(argv);
  }

  public void startup(String[] argv) {
    try {
      String  hostKeyFile    = null;
      String  authKeysDir    = null;
      boolean doGenerateId   = false;
      int     port           = SSH.DEFAULTPORT;
      int     bits           = 0;
      int     i;

      for(i = 0; i < argv.length; i++) {
	String arg = argv[i];
	if(arg.charAt(0) != '-')
	  break;
	switch(arg.charAt(1)) {
	case 'a':
	  authKeysDir = argv[++i] + File.separatorChar;
	  break;
	case 'b':
	  bits = Integer.parseInt(argv[++i]);
	  break;
	case 'g':
	  doGenerateId = true;
	  break;
	case 'v':
	  System.out.println("verbose mode selected...");
	  SSH.DEBUG = true;
	  break;
	case 'V':
	  System.out.println(SSH.VER_MINDTUNL);
	  System.out.println("SSH protocol version " + SSH.SSH_VER_MAJOR + "." + SSH.SSH_VER_MINOR);
	  System.exit(0);
	  break;
	case 'p':
	  port = Integer.parseInt(argv[++i]);
	  break;
	default:
	  throw new Exception();
	}
      }

      if(bits == 0)
	  bits = (doGenerateId ? SSH.HOST_KEY_LENGTH : SSH.SERVER_KEY_LENGTH);

      if(doGenerateId) {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String fileName, comment, passwd;
	System.out.print("filename: ");
	fileName = br.readLine();
	System.out.print("password (return for none): ");
	passwd = br.readLine();
	System.out.print("comment: ");
	comment = br.readLine();

	System.out.print("Generating " + (passwd.length() == 0 ? "(unencrypted) " : "") +
			 "identity of length " + bits + "...");
	KeyPair kp = SSH.generateRSAKeyPair(bits, SSH.secureRandom());
	System.out.println("done");
    
	SSH.generateKeyFiles(kp, fileName, passwd, comment);
	System.exit(0);
      }

      hostKeyFile = argv[i];

      if(authKeysDir == null) {
	i = hostKeyFile.lastIndexOf(File.separatorChar);
	if(i != -1)
	  authKeysDir = hostKeyFile.substring(0, i + 1);
	else
	  authKeysDir = "";
      }

      SSHServer.setAuthKeysDir(authKeysDir);
      SSHServer.setHostKeyFile(hostKeyFile);
      SSHServer.setServerKeyBits(bits);

      SSHServer.sshd(port);

    } catch(FileNotFoundException e) {
      System.out.println("File not found: " + e.getMessage());
    } catch(IOException e) {
      System.out.println("Error starting MindTunnel sshd: " + e.getMessage());
    } catch(Exception e) {
      System.out.println("usage: MindTunnel [options] <host_key_file>");
      System.out.println("Options:");
      System.out.println("  -a <dir>    Directory where the users authorized_keys files are located");
      System.out.println("              (default same dir as host-key).");
      System.out.println("  -b <bits>   Specifies the number of bits in the server key (default 768).");
      System.out.println("              OR length of key to generate with option -g.");
      System.out.println("  -g          Generate authentication key pair files (default 1024 bits).");
      System.out.println("  -v          Verbose; display verbose debugging messages.");
      System.out.println("  -V          Display version number only.");
      System.out.println("  -p <port>   Port to listen for connections on (default is 22).");
    }

    System.exit(0);
  }


}
