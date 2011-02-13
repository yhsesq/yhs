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

import java.io.*;

public class SSHRxChannel extends SSHChannel {

  protected InputStream  in;
  protected SSHPdu       pduFactory;

  public SSHRxChannel(InputStream in, int channelId) {
    super(channelId);
    this.in = in;
  }

  public void setSSHPduFactory(SSHPdu pduFactory) {
    this.pduFactory = pduFactory;
  }

  public void serviceLoop() throws Exception {
    SSH.logExtra("Starting rx-chan: " + channelId);
    for(;;) {
      SSHPdu pdu;
      pdu = pduFactory.createPdu();
      pdu = listener.prepare(pdu);
      //      pdu = pdu.preProcess(pdu);
      pdu.readFrom(in);
      //      pdu = pdu.postProcess();
      listener.receive(pdu);
    }
  }

  public void forceClose() {
    try {
      in.close();
    } catch (IOException e) {
      // !!!
    }
  }

}
