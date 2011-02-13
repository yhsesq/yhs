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
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

public class SSHSocketFactory extends SSHClientUserAdaptor {

  public final static int NOLISTENPORT = -1;

  protected SSHAuthenticator authenticator;
  protected Hashtable        clientCache;

  public SSHSocketFactory(String host, int port, SSHAuthenticator authenticator) {
    super(host, port);
    this.authenticator = authenticator;
    this.clientCache   = new Hashtable();
  }

  public SSHSocketFactory(String host, SSHAuthenticator authenticator) {
    this(host, SSH.DEFAULTPORT, authenticator);
  }

  protected synchronized SSHClient getClient() {
    Enumeration e = clientCache.keys();
    if(!e.hasMoreElements())
      return null;
    return (SSHClient)e.nextElement();
  }

  protected void finalize() throws IOException {
    Enumeration e = clientCache.keys();
    while(e.hasMoreElements()) {
      SSHClient client = (SSHClient)e.nextElement();
      client.waitForExit(1000);
    }
  }

  protected synchronized void addClient(SSHClient client) {
    clientCache.put(client, new Vector());
  }

  protected synchronized void registerPseudoUser(SSHClient client, SSHSocketImpl impl) {
    Vector users = (Vector)clientCache.get(client);
    users.addElement(impl);
  }

  protected synchronized void closePseudoAll(SSHClient client) {
    Vector users = (Vector)clientCache.get(client);
    if(users == null)
      return;
    SSHSocketImpl u;
    while(users.size() > 0) {
      u = (SSHSocketImpl)users.elementAt(0);
      try {
	u.close();
      } catch (IOException e) {
	// !!!
      }
      users.removeElementAt(0);
    }
    clientCache.remove(client);
  }

  protected synchronized void closePseudoUser(SSHClient client, SSHSocketImpl u) {
    Vector users = (Vector)clientCache.get(client);
    for(int i = 0; i < users.size(); i++) {
      if(users.elementAt(i) == u) {
	users.removeElementAt(i);
	client.delRef();
	break;
      }
    }
    if(users.size() == 0)
      clientCache.remove(client);
  }

  protected SSHClient createSSHClient(int listenPort) throws IOException {
    SSHClient client;
    if(listenPort != NOLISTENPORT || ((client = getClient()) == null)) {
      client = new SSHClient(authenticator, this);
      // !!! This indicates that we are going to use this client to implement a SSHServerSocket
      //
      if(listenPort != NOLISTENPORT)
	client.addRemotePortForward(listenPort, InetAddress.getLocalHost().getHostAddress(),
				    listenPort, "general");
      client.bootSSH(false);
      client.startExitMonitor();
      addClient(client);
    }
    return client;
  }

  protected SSHSocketImpl createSocketImpl(SSHClient client, boolean acceptor) throws IOException {
    SSHSocketImpl impl   = new SSHSocketImpl();

    impl.setFactory(this);
    try {
      impl.create(client, acceptor);
    } catch (IOException e) {
      impl.close();
      throw e;
    }

    registerPseudoUser(client, impl);
    return impl;
  }

  public SSHSocket createSocket(String address, int port) throws IOException {
    SSHSocketImpl impl   = createSocketImpl(createSSHClient(NOLISTENPORT), false);
    SSHSocket     sock   = new SSHSocket(impl);
    try {
      impl.connect(address, port);
    } catch (IOException e) {
      impl.close();
      throw e;
    }
    return sock;
  }

  public SSHServerSocket createServerSocket(int port, int backlog) throws IOException {
    SSHSocketImpl   impl = createSocketImpl(createSSHClient(port), true);
    SSHServerSocket sock = new SSHServerSocket(impl);
    if (port < 0 || port > 0xFFFF)
      throw new IllegalArgumentException("Port value out of range: " + port);

    impl.setFactory(this);
    try {
      impl.bind(InetAddress.getLocalHost(), port);
      impl.listen(backlog);
    } catch (IOException e) {
      impl.close();
      throw e;
    }
    sock.setSocketFactory(this);
    return sock;
  }

  public SSHServerSocket createServerSocket(int port) throws IOException {
    return createServerSocket(port, SSHServerSocket.DEFAULT_BACKLOG);
  }

  public void setServer(String host, int port) {
    sshHost = host;
    sshPort = port;
  }

  public void setServer(String host) {
    setServer(host, SSH.DEFAULTPORT);
  }

  public void setAuthenticator(SSHAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  // SSHClientUser interface "extensions"
  //
  public void open(SSHClient client) {
    // !!! Might want to handle something here, I'm too tired to think of it now (also :-)...
  }

  public void connected(SSHClient client) {
    // !!! Might want to handle something here, I'm too tired to think of it now...
  }

  public void disconnected(SSHClient client, boolean graceful) {
    closePseudoAll(client);
  }

  public void report(String msg) {
    SSH.log("Embedded SSHClient report: " + msg);
  }

  public void alert(String msg) {
    SSH.log("Embedded SSHClient alert: " + msg);
  }

}
