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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

public class SSHProtocolPlugin {

  static Hashtable plugins = new Hashtable();

  static {
    SSHProtocolPlugin.addPlugin("general", new SSHProtocolPlugin());
    try {
      SSHProtocolPlugin.addPlugin("ftp", new SSHFtpPlugin());
    } catch (Throwable e) {
      System.out.println("FTP plugin not found, disabled");
    }
  }

  public static SSHProtocolPlugin getPlugin(String name) {
    SSHProtocolPlugin plugin = (SSHProtocolPlugin)plugins.get(name);
       return (SSHProtocolPlugin)plugins.get(name);
  }

  public static void addPlugin(String name, SSHProtocolPlugin plugin) {
    plugins.put(name, plugin);
  }

  public static void initiateAll(SSHClient client) {
    SSHProtocolPlugin plugin;
    Enumeration e = plugins.elements();
    while(e.hasMoreElements()) {
      plugin = (SSHProtocolPlugin)e.nextElement();
      plugin.initiate(client);
    }
  }

  public void initiate(SSHClient client) {
  }

  public SSHListenChannel localListener(String localHost, int localPort,
					String remoteHost, int remotePort,
					SSHChannelController controller) throws IOException {
    return new SSHListenChannel(localHost, localPort, remoteHost, remotePort, controller);
  }

  public void remoteListener(int remotePort, String localHost, int localPort,
			     SSHChannelController controller) {
  }

}
