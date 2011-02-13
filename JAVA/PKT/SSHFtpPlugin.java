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

import java.security.SecureRandom;
import java.io.IOException;

public class SSHFtpPlugin extends SSHProtocolPlugin {

  public void initiate(SSHClient client) {
    client.delRemotePortForward("ftp");
    if(client.havePORTFtp) {
      SecureRandom rnd = new SecureRandom();
      int rndval;
      while((rndval = (rnd.nextInt() & 0xfff0)) < 8192)
	;
      client.firstFTPPort = rndval;
      for(int i = 0; i < SSHFtpTunnel.MAX_REMOTE_LISTEN; i++) {
	client.addRemotePortForward(client.firstFTPPort + i,
				    SSHFtpTunnel.TUNNEL_NAME + i,
				    client.firstFTPPort + i, "ftp");
      }
    }
  }

  public SSHListenChannel localListener(String localHost, int localPort,
					String remoteHost, int remotePort,
					SSHChannelController controller) throws IOException {
    return new SSHFtpListenChannel(localHost, localPort, remoteHost, remotePort, controller);
  }

}
