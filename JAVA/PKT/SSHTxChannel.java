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

public class SSHTxChannel extends SSHChannel {

  protected OutputStream out;
  protected SSHPduQueue  queue;

  boolean closePending;

  public SSHTxChannel(OutputStream out, int channelId) {
    super(channelId);
    this.out          = out;
    this.closePending = false;
    queue = new SSHPduQueue();
  }

  public SSHPduQueue getQueue() {
    return queue;
  }

  public void setClosePending() {
    closePending = true;
    queue.release();
  }

  public synchronized boolean isClosePending() {
    return closePending;
  }

  public void serviceLoop() throws Exception {
    SSH.logExtra("Starting tx-chan: " + channelId);
    for(;;) {
      SSHPdu pdu;
      // !!! the thread is (hopefully) suspended when we set closePending
      // so we don't have to access a lock each loop
      if(closePending && queue.isEmpty()) {
	  throw new Exception("CLOSE");
      }
      pdu = queue.getFirst();
      //      pdu = pdu.preProcess();
      pdu.writeTo(out);
      //      pdu = pdu.postProcess();
    }
  }

}
