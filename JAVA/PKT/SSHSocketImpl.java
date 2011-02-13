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
// (C) 2004 Yohann Sulaiman Modified - updated socketimpl.
package mindbright.ssh;

import java.net.*;
import java.io.*;

public class SSHSocketImpl extends SocketImpl {

  public final static int SO_TIMEOUT   = 0x1006; // !!!

  protected SSHSocketFactory factory;

  SSHClient       client;
  SSHSocketTunnel tunnel;
  SSHPduQueue     cnQueue;

  boolean isClosed;

  protected SSHSocketImpl() {
    // !!! Used to indicate to create if we have done bind() or not...
    // (i.e. if we are an implementation of a SSHServerSocket or a SSHSocket)
    localport = SSHSocketFactory.NOLISTENPORT;
  }

  protected void setFactory(SSHSocketFactory factory) {
    this.factory = factory;
  }

  protected void create(boolean stream) {
    // !!! Not used, but abstract in SocketImpl
  }

  protected void create(SSHClient client, boolean acceptor) throws IOException {
    InetAddress localHost = InetAddress.getLocalHost();
    
    this.client = client;
    client.addRef();

    if(acceptor) {
      cnQueue = client.controller.getCnQueue();
    } else {
      tunnel = new SSHSocketTunnel(client.controller, this);
      tunnel.setLocalAddress(localHost);
    }
  }

  protected void connect(String host, int port) throws IOException {
    try {
      address = InetAddress.getByName(host);
    } catch (Exception e) {
      address = InetAddress.getLocalHost();
    }

    tunnel.connect(host, port);
  }

  protected void connect(InetAddress address, int port) throws IOException {
    connect(address.getHostAddress(), port);
  }

  protected void bind(InetAddress host, int port) throws IOException {
    localport = port;
    // !!! This is done elsewhere (also, the local address is not important...)
    // !!! tunnel.setLocalAddress(host);
  }

  protected void listen(int backlog) throws IOException {
    cnQueue.setMaxDepth(backlog);
  }

  protected void accept(SocketImpl s) throws IOException {
    SSHPduOutputStream respPdu;
    SSHPduInputStream  inPdu;
    SSHSocketImpl      aImpl = (SSHSocketImpl)s;
    int                remoteChannel;
    inPdu = (SSHPduInputStream) cnQueue.getFirst();
    if(inPdu == null)
      throw new IOException("Socket closed");
    remoteChannel = inPdu.readInt();
    aImpl.tunnel.setRemoteChannelId(remoteChannel);
    respPdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_OPEN_CONFIRMATION, client.controller.sndCipher);
    respPdu.writeInt(remoteChannel);
    respPdu.writeInt(aImpl.tunnel.channelId);
    client.controller.transmit(respPdu);
    client.controller.addTunnel(aImpl.tunnel);

    /* !!!
    } catch (Exception e) {
      respPdu = new SSHPduOutputStream(SSH.MSG_CHANNEL_OPEN_FAILURE, client.controller.sndCipher);
      respPdu.writeInt(remoteChannel);
      client.controller.transmit(respPdu);
      throw new IOException(e.getMessage());
    }
    */
  }

  protected InputStream getInputStream() throws IOException {
    return tunnel.in;
  }

  protected OutputStream getOutputStream() throws IOException {
    return tunnel.out;
  }

  protected int available() throws IOException {
    return tunnel.available();
  }

  protected void close() throws IOException {
    if(localport == SSHSocketFactory.NOLISTENPORT) {
      if(tunnel != null && !tunnel.terminated())
	tunnel.close();
    } else {
      factory.closePseudoUser(client, this);
    }
  }

  protected void finalize() throws IOException {
    if(!isClosed)
      close();
  }

  public void setOption(int optID, Object value) throws SocketException {
    throw new SocketException("Not implemented");
  }

  public Object getOption(int optID) throws SocketException {
    throw new SocketException("Not implemented");
  }

  protected InetAddress getInetAddress() {
    return address;
  }

  protected int getPort() {
    return port;
  }

protected void sendUrgentData(int x){}
protected boolean supportsUrgentData(){return false;}
protected void connect(SocketAddress address,int timeout){}
  protected int getLocalPort() {
    return localport;
  }

}
