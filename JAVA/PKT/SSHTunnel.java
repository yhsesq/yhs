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

public class SSHTunnel implements SSHChannelListener {

  int channelId;
  int remoteChannelId;

  boolean   sentInputEOF;
  boolean   sentOutputClosed;
  boolean   receivedInputEOF;
  boolean   receivedOutputClosed;

  protected SSHChannelController controller;

  protected Socket       ioSocket;
  protected SSHTxChannel txChan;
  protected SSHRxChannel rxChan;

  protected SSHPduQueue  txQueue;

  protected String remoteDesc;

  public SSHTunnel(Socket ioSocket, int channelId, int remoteChannelId,
		   SSHChannelController controller)
    throws IOException {

    this.ioSocket             = ioSocket;
    this.channelId            = channelId;
    this.remoteChannelId      = remoteChannelId;
    this.controller           = controller;
    this.sentInputEOF         = false;
    this.sentOutputClosed     = false;
    this.receivedInputEOF     = false;
    this.receivedOutputClosed = false;

    if(ioSocket != null) {
      try {
	rxChan = new SSHRxChannel(new BufferedInputStream(ioSocket.getInputStream(), 8192), channelId);
	txChan = new SSHTxChannel(new BufferedOutputStream(ioSocket.getOutputStream()), channelId);
      } catch (Exception e) {
	throw new IOException("Could not create tunnel: " + e.toString());
      }
      txQueue = txChan.getQueue();
      rxChan.setSSHPduFactory(new SSHPduOutputStream(SSH.MSG_CHANNEL_DATA, controller.sndCipher));
      txChan.setSSHChannelListener(this);
      rxChan.setSSHChannelListener(this);
    }
  }

  public int getLocalPort() {
    if(ioSocket != null)
      return ioSocket.getLocalPort();
    return 0;
  }

  public String getLocalHost() {
    if(ioSocket != null)
      return ioSocket.getLocalAddress().getHostAddress();
    return "N/A";
  }

  public boolean isOpen() {
    if(this.remoteChannelId == SSH.UNKNOWN_CHAN_NUM)
      return false;
    return true;
  }

  public boolean setRemoteChannelId(int remoteChannelId) {
    if(isOpen())
      return false;
    this.remoteChannelId = remoteChannelId;
    return true;
  }

  public void start() {
    txChan.start();
    rxChan.start();
  }

  public void openFailure() {
    if(ioSocket != null) {
      try {
	ioSocket.close();
      } catch (IOException e) {
	// !!!
      }
    }
  }

  public SSHPdu prepare(SSHPdu pdu) throws IOException {
    ((SSHPduOutputStream)pdu).writeInt(remoteChannelId);
    return pdu;
  }

  public void receive(SSHPdu pdu) {
    controller.transmit(pdu);
  }

  public void transmit(SSHPdu pdu) {
    txQueue.putLast(pdu);
  }

  public void close(SSHChannel chan) {
    if(chan == null || chan instanceof SSHTxChannel) {
      sendOutputClosed();
      try {
	ioSocket.close();
      } catch (IOException e) {
	controller.alert("Error closing socket for: " + channelId + " : " + e.toString());
      }
    } else {
      sendInputEOF();
    }
    checkTermination();
  }

  public synchronized void terminateNow() {
    close(null);
  }

  public synchronized void checkTermination() {
    if(sentInputEOF && sentOutputClosed &&
       receivedInputEOF && receivedOutputClosed) {
      controller.delTunnel(channelId);
      if(txChan != null && txChan.isAlive())
	txChan.stop();
      if(rxChan != null && rxChan.isAlive())
	rxChan.stop();
    }
  }

  public void sendOutputClosed() {
    if(sentOutputClosed)
      return;
    try {
      SSHPduOutputStream pdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_OUTPUT_CLOSED,
						      controller.sndCipher);
      pdu.writeInt(remoteChannelId);
      controller.transmit(pdu);
      sentOutputClosed = true;
    } catch (Exception e) {
      controller.alert("Error sending output-closed: " + e.toString());
    }
  }

  public void sendInputEOF() {
    if(sentInputEOF)
      return;
    try {
      SSHPduOutputStream pdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_INPUT_EOF,
						      controller.sndCipher);
      pdu.writeInt(remoteChannelId);
      controller.transmit(pdu);
      sentInputEOF = true;
    } catch (Exception e) {
      controller.alert("Error sending input-EOF: " + e.toString());
    }
  }

  public void receiveOutputClosed() {
    if(rxChan != null)
      rxChan.stop();
    receivedOutputClosed = true;
    sendInputEOF();
    checkTermination();
  }

  public void receiveInputEOF() {
    if(txChan != null)
      txChan.setClosePending();
    receivedInputEOF = true;
    checkTermination();
  }

  public void setRemoteDesc(String desc) {
    remoteDesc = desc;
  }

  public String getDescription() {
    if(ioSocket != null)
      return ioSocket.getInetAddress().getHostAddress() + ":" + ioSocket.getPort() + " <--> " +
	getLocalHost() + ":" + ioSocket.getLocalPort() + " <-ssh-> " + remoteDesc;
    else
      return "< N/A >";
  }

}
