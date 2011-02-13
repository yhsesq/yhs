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
package mindbright.ssh;

import java.net.*;
import java.io.*;

public class SSHListenChannel extends SSHChannel {
  static boolean allowRemoteConnect = false;

  static final int LISTEN_QUEUE_SIZE = 16;

  SSHChannelController controller;

  ServerSocket listenSocket;
  String       remoteHost;
  int          remotePort;
  InetAddress  localHost1;
  InetAddress  localHost2;

  boolean temporaryListener;

  public SSHListenChannel(String localHost, int localPort,
			  String remoteHost, int remotePort,
			  SSHChannelController controller)
    throws IOException {
    super(SSH.LISTEN_CHAN_NUM);
    this.controller         = controller;
    try {
	this.listenSocket = new ServerSocket(localPort, LISTEN_QUEUE_SIZE,
					     InetAddress.getByName(localHost));
    } catch (IOException e) {
	throw new IOException("Error setting up local forward on port " +
			      localPort + ", " + e.getMessage());
    }
    this.remoteHost         = remoteHost;
    this.remotePort         = remotePort;

    this.localHost1 = InetAddress.getLocalHost();
    this.localHost2 = InetAddress.getByName("127.0.0.1");
  }

  public int getListenPort() {
    return listenSocket.getLocalPort();
  }

  public String getListenHost() {
    return listenSocket.getInetAddress().getHostAddress();
  }

  public static synchronized void setAllowRemoteConnect(boolean val) {
    allowRemoteConnect = val;
  }

  static synchronized boolean getAllowRemoteConnect() {
    return allowRemoteConnect;
  }

  public SSHTunnel newTunnel(Socket ioSocket, int channelId, int remoteChannelId,
			     SSHChannelController controller) throws IOException {
    return new SSHTunnel(ioSocket, channelId, remoteChannelId, controller);
  }

  public void setTemporaryListener(boolean val) {
    temporaryListener = val;
  }

  public void serviceLoop() throws IOException {

    SSH.log("Starting listen-chan: " + listenSocket.getLocalPort());

    try {
	for(;;) {
	    Socket  fwdSocket = listenSocket.accept();

	    if(!getAllowRemoteConnect() &&
	       !(fwdSocket.getInetAddress().equals(localHost1) ||
		 fwdSocket.getInetAddress().equals(localHost2))) {
		controller.alert("Remote connect to local tunnel rejected: " + fwdSocket.getInetAddress());
		fwdSocket.close();
		continue;
	    }

	    SSHPduOutputStream respPdu = new SSHPduOutputStream(SSH.MSG_PORT_OPEN, controller.sndCipher);

	    int newChan      = controller.newChannelId();
	    SSHTunnel tunnel = newTunnel(fwdSocket,
					 newChan, SSH.UNKNOWN_CHAN_NUM,
					 controller);
	    controller.addTunnel(tunnel);
	    tunnel.setRemoteDesc(remoteHost + ":" + remotePort);

	    respPdu.writeInt(newChan);
	    respPdu.writeString(remoteHost);
	    respPdu.writeInt(remotePort);

	    SSH.log("got connect for: " + remoteHost + " : " + remotePort + ", " + newChan);

	    //
	    // !!! TODO: check this !!! if(controller.haveHostInFwdOpen())
	    //
	    respPdu.writeString(fwdSocket.getInetAddress().getHostAddress());

	    controller.transmit(respPdu);

	    if(temporaryListener)
		break;
	}
    } finally {
	listenSocket.close();
    }
  }

  public void forceClose() {
      if(isAlive()) {
	  stop();
      }
      try {
	  listenSocket.close();
      } catch (IOException e) {
      }
  }

}
