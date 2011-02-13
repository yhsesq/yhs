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
import java.util.Hashtable;
import java.util.Vector;

public class SSHConnectChannel extends SSHTxChannel {
  SSHChannelController controller;

  Hashtable hostMap;

  public SSHConnectChannel(SSHChannelController controller) {
    super(null, SSH.CONNECT_CHAN_NUM);
    this.controller     = controller; 
    this.hostMap        = new Hashtable();
  }

  public synchronized void addHostMapPermanent(String fromHost, String toHost, int toPort) {
    Vector hostPortPair = new Vector();
    hostPortPair.addElement(toHost);
    hostPortPair.addElement(new Integer(toPort));
    hostPortPair.addElement(new Boolean(true));
    hostMap.put(fromHost, hostPortPair);
  }
  public synchronized void addHostMapTemporary(String fromHost, String toHost, int toPort) {
    Vector hostPortPair = new Vector();
    hostPortPair.addElement(toHost);
    hostPortPair.addElement(new Integer(toPort));
    hostPortPair.addElement(new Boolean(false));
    hostMap.put(fromHost, hostPortPair);
  }

  public synchronized void delHostMap(String fromHost) {
    hostMap.remove(fromHost);
  }

  public synchronized Vector getHostMap(String fromHost) {
    Vector hostPortPair = (Vector)hostMap.get(fromHost);
    if(hostPortPair != null && !(((Boolean)hostPortPair.elementAt(2)).booleanValue())) {
      delHostMap(fromHost);
    }
    return hostPortPair;
  }

  int displayNumber(String display) {
    int hostEnd;
    int dispEnd;
    int displayNum;
    if(display == null || display.equals("") ||
       (hostEnd = display.indexOf(':')) == -1)
      return 0;

    if((dispEnd = display.indexOf('.', hostEnd)) == -1)
      dispEnd = display.length();

    try {
      return Integer.parseInt(display.substring(hostEnd + 1, dispEnd));
    } catch (Exception e) {
      // !!!
      displayNum = 0;
    }
    return displayNum;
  }

  String displayHost(String display) {
    int hostEnd;
    if(display == null || display.equals("") ||
       display.charAt(0) == ':' || display.indexOf("unix:") == 0 ||
       (hostEnd = display.indexOf(':')) == -1)
      return "localhost";
    return display.substring(0, hostEnd);
  }

  public void serviceLoop() throws Exception {
    SSHPduInputStream inPdu;
    int               remoteChannel;
    int               port;
    String            host;
    String            origin;
    Socket            fwdSocket;

    for(;;) {
      inPdu         = (SSHPduInputStream) queue.getFirst();
      remoteChannel = inPdu.readInt();

      if(inPdu.type == SSH.SMSG_X11_OPEN) {
	if(!controller.sshAsClient().user.wantX11Forward()) {
	    controller.alert("Something is fishy with the server, unsolicited X11 forward!");
	    throw new Exception("Something is fishy with the server, unsolicited X11 forward!");
	}
	String display = controller.sshAsClient().user.getDisplay();
	host = displayHost(display);
	port = 6000 + displayNumber(display);
      } else {
	host = inPdu.readString();
	port = inPdu.readInt();
      }

      if(controller.haveHostInFwdOpen())
	origin = inPdu.readString();
      else
	origin = "unknown (origin-option not used)";

      // See if there is a translation entry for this host
      //
      Vector hostPortPair = getHostMap(host);
      if(hostPortPair != null) {
	host = (String)hostPortPair.elementAt(0);
	port = ((Integer)hostPortPair.elementAt(1)).intValue();
      }

      SSHPduOutputStream respPdu;

      try {
	fwdSocket        = new Socket(host, port);
	int newChan      = controller.newChannelId();
	SSHTunnel tunnel = new SSHTunnel(fwdSocket, newChan, remoteChannel, controller);
	controller.addTunnel(tunnel);
	tunnel.setRemoteDesc(origin);

	respPdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_OPEN_CONFIRMATION, controller.sndCipher);
	respPdu.writeInt(remoteChannel);
	respPdu.writeInt(newChan);

	SSH.log("Port open (" + origin + ") : " + host + ": " + port +
		" (#" + remoteChannel + ")" + " new: " + newChan);

	controller.transmit(respPdu);

	// We must wait until after we have put the response in the
	// controllers tx-queue with starting the tunnel
	// (to avoid data reaching the server before the response)
	//
	tunnel.start();

      } catch (IOException e) {
 	respPdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_OPEN_FAILURE, controller.sndCipher);
 	respPdu.writeInt(remoteChannel);

	controller.alert("Failed port open (" + origin + ") : " + host + ": " + port +
		" (#" + remoteChannel + ")");

	controller.transmit(respPdu);
      }

    }
  }

}
