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

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class SSHClientUserAdaptor implements SSHClientUser {

  protected String sshHost;
  protected int    sshPort;

  public SSHClientUserAdaptor(String server, int port) {
    this.sshHost = server;
    this.sshPort = port;
  }

  public SSHClientUserAdaptor(String server) {
    this(server, SSH.DEFAULTPORT);
  }

  public String getSrvHost() {
    return sshHost;
  }

  public int getSrvPort() {
    return sshPort;
  }

  public Socket getProxyConnection() throws IOException {
    return null;
  }

  public String getDisplay() {
    return "";
  }

  public int getMaxPacketSz() {
    return 0;
  }

  public int getAliveInterval() {
    return 0;
  }
public int getCompressionLevel() {
    return 0;
  }
  public boolean wantX11Forward() {
    return false;
  }

  public boolean wantPrivileged() {
    return false;
  }

  public boolean wantPTY() {
    return false;
  }

  public SSHInteractor getInteractor() {
      return null;
  }

}
