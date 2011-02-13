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
package mindbright.ssh;

import java.net.*;
import java.io.*;
import java.math.BigInteger;

import mindbright.security.*;
import mindbright.terminal.*;

/**
 * @author  Mats Andersson
 * @version 0.96, 12/04/98
 * @see     SSHClient
 */
public class SSHServer extends SSH implements Runnable {

  static KeyPair serverKey;
  static KeyPair hostKey;

  protected InetAddress localAddr;

  static String authKeysDir   = "";
  static String hostKeyFile   = "identity";
  static int    serverKeyBits = SSH.SERVER_KEY_LENGTH;

  protected String cliVersionStr;
  protected int    cliVersionMajor;
  protected int    cliVersionMinor;

  protected Thread myThread;

  protected Socket               sshSocket;
  protected BufferedInputStream  sshIn;
  protected BufferedOutputStream sshOut;

  protected SSHChannelController controller;

  static public void setHostKeyFile(String fileName) {
    hostKeyFile = fileName;
  }

  static public void setAuthKeysDir(String dirName) {
    authKeysDir = dirName;
  }

  static public void setServerKeyBits(int bits) {
    serverKeyBits = bits;
  }

  public InetAddress getLocalAddr() {
    return localAddr;
  }

  public void setLocalAddr(String addr) throws UnknownHostException {
    localAddr = InetAddress.getByName(addr);
  }

  public SSHServer(Socket sshSocket,
		   int protocolFlags, int supportedCiphers, int supportedAuthTypes,
		   KeyPair srvServerKey, KeyPair srvHostKey) throws IOException {
    this.isAnSSHClient = false;
    this.sshSocket     = sshSocket;
    this.sshIn         = new BufferedInputStream(sshSocket.getInputStream(), 8192);
    this.sshOut        = new BufferedOutputStream(sshSocket.getOutputStream());

    this.protocolFlags      = protocolFlags;
    this.supportedCiphers   = supportedCiphers;
    this.supportedAuthTypes = supportedAuthTypes;
    this.srvServerKey       = srvServerKey;
    this.srvHostKey         = srvHostKey;
  }

  protected void start() {
//    myThread = new Thread(this);
	    if(SSH.NETSCAPE_SECURITY_MODEL) {
		try {
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
		} catch (netscape.security.ForbiddenTargetException e) {
		    e.printStackTrace();
		    // !!!
		}
	    }

      myThread = new Thread(SSH.getThreadGroup(), this); // JH_Mod
    myThread.start();
  }

  public void run() {
    try {
      System.out.println("connection from " + sshSocket.getInetAddress().getHostAddress() + " port " +
			 sshSocket.getPort());

      negotiateVersion();
      sendServerData();
      receiveSessionKey();

      // !!!
      // At this stage the communication is encrypted
      // !!!

      authenticateUser();
      controller = new SSHChannelController(this, sshIn, sshOut, sndCipher, rcvCipher,
					    null, true);
      receiveOptions();

      controller.start();

      try {
	controller.waitForExit();
      } catch(InterruptedException e) {
	// !!!
	log("Error when shutting down SSHClient: " + e.getMessage());
	controller.killAll();
      }

    } catch (IOException e) {
      // !!!
      log("error in MindTunnel: " + e);
    }
  }

  static RSAPrivateKey getPrivate(SSHRSAKeyFile keyFile) {
    RSAPrivateKey privKey = keyFile.getPrivate("");
    while(privKey == null) {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("");
      System.out.print("key-file '" + keyFile.getComment() + "' password: ");
      String passwd;
      try {
	passwd = br.readLine();
      } catch(IOException e) {
	passwd = "";
      }
      privKey = keyFile.getPrivate(passwd);
    }
    return privKey;
  }

  public static void sshd(int port) throws IOException {
    boolean      keepRunning = true;
    ServerSocket listenSock  = null;

    SSHRSAKeyFile keyFile;
    keyFile    = new SSHRSAKeyFile(hostKeyFile);
    hostKey    = new KeyPair(keyFile.getPublic(), getPrivate(keyFile));
    listenSock = new ServerSocket(port);

    int keyLenDiff = Math.abs(serverKeyBits -
			      ((RSAPublicKey)hostKey.getPublic()).bitLength());
    if(keyLenDiff < 24) {
      throw new IOException("Invalid server keys, difference in sizes must be at least 24 bits");
    }

    System.out.print("generating server-key of length " + serverKeyBits + "...");
    serverKey  = SSH.generateRSAKeyPair(serverKeyBits, secureRandom());
    System.out.println("done");

    System.out.println("starting new MindTunnel on port " + port + "...");

    while(keepRunning) {
      Socket sshSocket = listenSock.accept();
      SSHServer srv = new SSHServer(sshSocket, PROTOFLAG_HOST_IN_FWD_OPEN, 0xff, 0x0f,
				    serverKey, hostKey);

      // !!! Change later, must take arguments for localHost
      //
      srv.localAddr = InetAddress.getLocalHost();
      srv.start();
    }

  }

  void negotiateVersion() throws IOException {
    byte[] buf;
    int    len;
    String verStr;

    verStr = getVersionId(false);
    verStr += "\n";
    buf    = verStr.getBytes();

    sshOut.write(buf);
    sshOut.flush();

    buf = new byte[256];
    len = sshIn.read(buf);

    cliVersionStr = new String(buf, 0, len);

    try {
	int l = cliVersionStr.indexOf('-');
	int r = cliVersionStr.indexOf('.');
	cliVersionMajor = Integer.parseInt(cliVersionStr.substring(l + 1, r));
	l = r;
	r = cliVersionStr.indexOf('-', l);
	if(r == -1) {
	    cliVersionMinor = Integer.parseInt(cliVersionStr.substring(l + 1));
	} else {
	    cliVersionMinor = Integer.parseInt(cliVersionStr.substring(l + 1, r));
	}
    } catch (Throwable t) {
      throw new IOException("Client version string invalid: " + cliVersionStr);
    }

    if(cliVersionMajor > 1) {
      throw new IOException("MindTunnel do not support SSHv2 yet, can only serve SSHv1 client");
    } else if(cliVersionMajor < 1 || cliVersionMinor < 5) {
      throw new IOException("Client's protocol version (" + cliVersionMajor + "-" +
			    cliVersionMinor + ") is too old, please upgrade");
    }

    // Strip white-space
    cliVersionStr = cliVersionStr.trim();
  }

  void sendServerData() throws IOException {
    SSHPduOutputStream pdu = new SSHPduOutputStream(SMSG_PUBLIC_KEY, null);

    SecureRandom rand = secureRandom();

    srvCookie = new byte[8];
    rand.nextBytes(srvCookie);
    generateSessionId();

    pdu.write(srvCookie);

    RSAPublicKey publ = (RSAPublicKey)srvServerKey.getPublic();
    int           n   = publ.bitLength();
    pdu.writeInt(n);
    pdu.writeBigInteger(publ.getE());
    pdu.writeBigInteger(publ.getN());

    publ = (RSAPublicKey)srvHostKey.getPublic();
    n    = publ.bitLength();
    pdu.writeInt(n);
    pdu.writeBigInteger(publ.getE());
    pdu.writeBigInteger(publ.getN());

    pdu.writeInt(protocolFlags);
    pdu.writeInt(supportedCiphers);
    pdu.writeInt(supportedAuthTypes);

    pdu.writeTo(sshOut);
  }

  void receiveSessionKey() throws IOException {
    SSHPduInputStream inpdu = new SSHPduInputStream(CMSG_SESSION_KEY, null);
    inpdu.readFrom(sshIn);

    cipherType = (int)inpdu.readByte();
    if(!isCipherSupported(cipherType))
      //sendDisconnect();
      ;
    log("cipher: " + getCipherName(cipherType));

    byte[] srvCookieCopy = new byte[srvCookie.length];
    inpdu.read(srvCookieCopy);

    BigInteger encKey = inpdu.readBigInteger();
    int cliProtoFlags = inpdu.readInt();

    RSACipher rsa1;
    RSACipher rsa2;
    if(((RSAPrivateKey)serverKey.getPrivate()).bitLength() >
       ((RSAPrivateKey)hostKey.getPrivate()).bitLength()) {
      rsa1 = new RSACipher(serverKey);
      rsa2 = new RSACipher(hostKey);
    } else {
      rsa2 = new RSACipher(serverKey);
      rsa1 = new RSACipher(hostKey);
    }
    encKey     = rsa1.doPrivate(encKey);
    encKey     = rsa1.stripPad(encKey);
    encKey     = rsa2.doPrivate(encKey);
    encKey     = rsa2.stripPad(encKey);
    sessionKey = encKey.toByteArray();

    // Must strip if too long, i.e. m.s.b. is 0 because of how BigInteger's toByteArray work...
    //
    if(sessionKey.length > (SESSION_KEY_LENGTH / 8)) {
      byte[] keyCopy = new byte[SESSION_KEY_LENGTH / 8];
      System.arraycopy(sessionKey, 1, keyCopy, 0, SESSION_KEY_LENGTH / 8);
      sessionKey = keyCopy;
    }

    for(int i = 0; i < sessionId.length; i++)
      sessionKey[i] ^= sessionId[i];

    initServerCipher();

    // !!! Check that all is ok...

    sendResult(SMSG_SUCCESS);
  }

  void authenticateUser() throws IOException {
    boolean           finished = false;
    SSHPduInputStream inpdu = new SSHPduInputStream(CMSG_USER, rcvCipher);
    String            user;

    inpdu.readFrom(sshIn);
    user = inpdu.readString();

    log("authenticating: " + user);
    sendResult(SMSG_FAILURE);

    while(!finished) {
      inpdu = new SSHPduInputStream(MSG_ANY, rcvCipher);
      inpdu.readFrom(sshIn);

      switch(inpdu.type) {
      case CMSG_AUTH_RSA:
      	if(doRSAAuth(user, inpdu.readBigInteger())) {
	  log("rsa-authentication for " + user + " succeeded");
	  sendResult(SMSG_SUCCESS);
	  finished = true;
	} else {
	  log("rsa-authentication for " + user + " failed");
	  sendResult(SMSG_FAILURE);
	}
	break;
      case CMSG_AUTH_PASSWORD:
	log("trying passwd-auth for: " + user);
	sendResult(SMSG_FAILURE);
	break;
      default:
	sendResult(SMSG_FAILURE);
      }
    }

  }

  boolean doRSAAuth(String userName, BigInteger pubKeyN) throws IOException {
    SSHRSAPublicKeyFile keyFile;
    SSHPduOutputStream  outpdu;
    SSHPduInputStream   inpdu;
    boolean             authenticated = false;

    keyFile = SSHRSAPublicKeyFile.loadFromFile(authKeysDir + userName, false);

    RSAPublicKey pubKey = keyFile.getPublic(pubKeyN, userName);
    if(pubKey == null)
      return false;

    RSACipher    rsa       = new RSACipher(new KeyPair(pubKey, null));
    byte[]       challenge = new byte[32];
    byte[]       tmp;
    BigInteger   enc;

    secureRandom().nextBytes(challenge);
    tmp = new byte[challenge.length + 1];
    System.arraycopy(challenge, 0, tmp, 1, challenge.length);
    enc = new BigInteger(tmp);

    enc = rsa.doPad(enc, pubKey.bitLength(), secureRandom());
    enc = rsa.doPublic(enc);
    outpdu = new SSHPduOutputStream(SMSG_AUTH_RSA_CHALLENGE, sndCipher);
    outpdu.writeBigInteger(enc);
    outpdu.writeTo(sshOut);

    inpdu = new SSHPduInputStream(CMSG_AUTH_RSA_RESPONSE, rcvCipher);
    inpdu.readFrom(sshIn);
    tmp = new byte[16];
    inpdu.read(tmp, 0, 16);

    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.update(challenge, 0, 32);
      md5.update(sessionId);
      challenge = md5.digest();
    } catch (Exception e) {
      // !!!
      System.out.println("!!! MD5 Not supported...");
      throw new IOException(e.getMessage());
    }

    int i;
    for(i = 0; i < challenge.length; i++)
      if(tmp[i] != challenge[i])
	break;
    if(i == challenge.length)
      authenticated = true;

    return authenticated;
  }

  void receiveOptions() throws IOException {
    SSHPduInputStream pdu;
    boolean           finished = false;

    while(!finished) {
      pdu = new SSHPduInputStream(MSG_ANY, rcvCipher);
      pdu.readFrom(sshIn);
      switch(pdu.type) {
      case CMSG_REQUEST_COMPRESSION:
	log("compression requested");
	break;
      case CMSG_MAX_PACKET_SIZE:
	log("mtu requested");
	break;
      case CMSG_X11_REQUEST_FORWARDING:
	log("x11-tunnel requested");
	sendResult(SMSG_FAILURE);
	break;
      case CMSG_REQUEST_PTY:
	log("pty requested");
	sendResult(SMSG_FAILURE);
	break;
      case CMSG_PORT_FORWARD_REQUEST:
	int    localPort  = pdu.readInt();
	String remoteHost = pdu.readString();
	int    remotePort = pdu.readInt();
	log("port-fwd requested: " + localPort + ":" + remoteHost + ":" + remotePort);
	controller.newListenChannel(localAddr.getHostAddress(), localPort, remoteHost, remotePort, "general");
	sendResult(SMSG_SUCCESS);
	break;
      case CMSG_EXEC_CMD:
	String command = pdu.readString();
	log("cmd: " + command);
	finished = true;
	break;
      case CMSG_EXEC_SHELL:
	log("exec-shell");
	finished = true;
	break;
      default:
	log("receiveOptions got unknown msg");
	break;
      }
    }
  }

  void sendResult(int type) throws IOException {
    SSHPduOutputStream pdu;
    pdu = new SSHPduOutputStream(type, sndCipher);
    pdu.writeTo(sshOut);
  }

}
