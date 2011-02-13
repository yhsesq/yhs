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

import java.io.IOException;
import java.net.Socket;

public class SSHFtpListenChannel extends SSHListenChannel {

  public SSHFtpListenChannel(String localHost, int localPort, String remoteHost, int remotePort,
			     SSHChannelController controller)
    throws IOException {
    super(localHost, localPort, remoteHost, remotePort, controller);
  }

  public SSHTunnel newTunnel(Socket ioSocket, int channelId, int remoteChannelId,
			     SSHChannelController controller) throws IOException {
    return new SSHFtpTunnel(ioSocket, channelId, remoteChannelId, controller);
  }

}
