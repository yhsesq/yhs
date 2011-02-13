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

public class SSHServerSocket {
  //
  // !!! Can't extend ServerSocket since it's not intended to be used
  // in this way... (i.e. it's only for a "vm-global" switch of
  // socket-implementation)
  //
  // If we extend ServerSocket we waste a local port for each remote
  // port we listen to, this is perhaps not too bad, BUT since applets
  // can't listen to local sockets we are doomed if we want to use it
  // in one... There is not much gain in extension here, I can't think
  // of a real case where it would really matter.
  //
  public static final int DEFAULT_BACKLOG = 25;

  protected SSHSocketFactory factory;
  protected SSHSocketImpl    impl;

  protected SSHServerSocket(SSHSocketImpl impl) {
    this.impl = impl;
  }

  protected void finalize() throws IOException {
    impl.close();
  }

  protected void setSocketFactory(SSHSocketFactory factory) {
    this.factory = factory;
  }

  public InetAddress getInetAddress() {
    return impl.getInetAddress();
  }

  protected int getLocalPort() {
    return impl.getLocalPort();
  }

  public SSHSocket accept() throws IOException {
    SSHSocketImpl aImpl = factory.createSocketImpl(impl.client, false);
    SSHSocket     aSock;

    impl.accept(aImpl);
    aSock = new SSHSocket(aImpl);

    return aSock;
  }

  public void close() throws IOException {
    impl.close();
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
    impl.setOption(SSHSocketImpl.SO_TIMEOUT, new Integer(timeout));
  }

  public synchronized int getSoTimeout() throws IOException {
    Object o = impl.getOption(SSHSocketImpl.SO_TIMEOUT);
    /* extra type safety */
    if (o instanceof Integer) {
      return ((Integer) o).intValue();
    } else {
      return 0;
    }
  }

  public String toString() {
    return "ServerSocket[addr=" + impl.getInetAddress() +
      ",port=" + impl.getPort() +
      ",localport=" + impl.getLocalPort()  + "]";
  }

}
