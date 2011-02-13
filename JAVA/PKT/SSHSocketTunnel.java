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

public final class SSHSocketTunnel extends SSHTunnel {

  public final static class SSHSocketIS extends InputStream {
    protected SSHSocketTunnel tunnel;
    protected SSHSocketIS(SSHSocketTunnel tunnel) {
      this.tunnel = tunnel;
    }
    public int read() throws IOException {
      byte[] b = new byte[1];
      if(read(b, 0, 1) == -1)
	return -1;
      return (int) b[0];
    }
    public int read(byte b[], int off, int len) throws IOException {
      return tunnel.read(b, off, len);
    }
    public void close() throws IOException {
      tunnel.closein(true);
    }
  }

  public final static class SSHSocketOS extends OutputStream {
    protected SSHSocketTunnel tunnel;
    protected SSHSocketOS(SSHSocketTunnel tunnel) {
      this.tunnel = tunnel;
    }
    public void write(int b) throws IOException {
      byte[] ba = new byte[1];
      ba[0] = (byte) b;
      tunnel.write(ba, 0, 1);
    }
    public void write(byte b[], int off, int len) throws IOException {
      tunnel.write(b, off, len);
    }
    public void close() throws IOException {
      tunnel.closeout();
    }
  }

  Object  lock;
  boolean inputClosePending;
  boolean inputExplicitClosed;
  boolean outputClosed;
  boolean terminated;
  boolean openFail;

  protected SSHPdu      rest;
  protected SSHSocketIS in;
  protected SSHSocketOS out;
  protected InetAddress localAddress;

  protected SSHSocketImpl impl;

  public SSHSocketTunnel(SSHChannelController controller, SSHSocketImpl impl) throws IOException {
    super(null, controller.newChannelId(), SSH.UNKNOWN_CHAN_NUM, controller);

    this.lock    = new Object();

    this.inputClosePending   = false;
    this.inputExplicitClosed = false;
    this.outputClosed        = false;
    this.terminated          = false;
    this.openFail            = false;

    this.txQueue = new SSHPduQueue();
    this.in      = new SSHSocketIS(this);
    this.out     = new SSHSocketOS(this);
    this.impl    = impl;
  }

  public void start() {
    synchronized(lock) {
      lock.notify();
    }
  }

  public void openFailure() {
    openFail = true;
    start();
  }

  public int read(byte b[], int off, int len) throws IOException {
    SSHPdu pdu = null;
    int    actLen;

    synchronized(this) {
	if(inputExplicitClosed)
	    throw new SocketException("Socket closed");
    }

    // We reuse the connect-lock since it is only used before we
    // start, after that it becomes the read-synchronization lock
    //
    synchronized(lock) {
	if(rest != null) {
	    pdu  = rest;
	    rest = null;
	} else if(inputClosePending && txQueue.isEmpty()) {
	    pdu = null;
	} else {
	    pdu = txQueue.getFirst();
	}

	if(pdu == null)
	    return -1;

	int rawLen = pdu.rawSize();
	if(len < rawLen) {
	    rest   = pdu;
	    actLen = len;
	} else {
	    actLen = rawLen;
	}

	System.arraycopy(pdu.rawData(), pdu.rawOffset(), b, off, actLen);

	if(rest != null) {
	    rest.rawAdjustSize(rawLen - len);
	}
    }

    return actLen;
  }

  public void write(byte b[], int off, int len) throws IOException {
    SSHPduOutputStream pdu;

    synchronized(this) {
	if(outputClosed)
	    throw new IOException("Resource temporarily unavailable");
    }

    pdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_DATA, controller.sndCipher);
    pdu = (SSHPduOutputStream)prepare(pdu);
    pdu.writeInt(len);
    pdu.write(b, off, len);
    controller.transmit(pdu);
  }

  public void connect(String host, int port) throws IOException {
    SSHPduOutputStream respPdu;

    setRemoteDesc(host + ":" + port);

    respPdu = new SSHPduOutputStream(SSH.MSG_PORT_OPEN, controller.sndCipher);
    controller.addTunnel(this);
    respPdu.writeInt(channelId);
    respPdu.writeString(host);
    respPdu.writeInt(port);
    respPdu.writeString(localAddress.getHostAddress());
    controller.transmit(respPdu);

    // Wait for start() to be called (i.e. the channel to be confirmed open)
    //
    synchronized(lock) {
      try {
	lock.wait();
      } catch(InterruptedException e) {
	// !!!
      }
    }

    if(openFail)
      throw new ConnectException("Connection Refused");
  }

  public void close() throws IOException {
    closein(true);
    closeout();
  }

  public synchronized void closeout() {
    outputClosed = true;
    sendInputEOF();
  }

  public void closein(boolean explicit) {
    txQueue.release();
    synchronized(this) {
      inputClosePending   = true;
      inputExplicitClosed = explicit;
      sendOutputClosed();
    }
  }

  public int available() throws IOException {
    if(rest != null)
      return rest.rawSize();
    else
      return 0;
  }

  protected void setLocalAddress(InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  public synchronized void checkTermination() {
    if(sentInputEOF && sentOutputClosed &&
       receivedInputEOF && receivedOutputClosed) {
      terminated = true;
      controller.delTunnel(channelId);
      impl.factory.closePseudoUser(controller.sshAsClient(), impl);
    }
  }

  public synchronized boolean terminated() {
    return terminated;
  }

  public void receiveOutputClosed() {
    super.receiveOutputClosed();
    closeout();
  }

  public void receiveInputEOF() {
    // !!! NOTE: we do not call super's method...
    receivedInputEOF = true;
    closein(false);
    closeout();
    checkTermination();
  }

}
