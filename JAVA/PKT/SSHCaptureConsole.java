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

import java.io.*;

import mindbright.terminal.Terminal;
import mindbright.security.Cipher;

public final class SSHCaptureConsole implements SSHConsole {

  SSHConsole   realConsole;
  OutputStream captureOut;
  SSHClient    client;

  public SSHCaptureConsole(SSHClient client, OutputStream captureOut) {
    this.realConsole = client.getConsole();
    this.captureOut  = captureOut;
    this.client      = client;
    client.setConsole(this);
  }

  public Terminal getTerminal() {
    return realConsole.getTerminal();
  }

  public void stdoutWriteString(byte[] str) {
    capture(new String(str));
    realConsole.stdoutWriteString(str);
  }

  public void stderrWriteString(byte[] str) {
    capture(new String(str));
    realConsole.stderrWriteString(str);
  }

  public void print(String str) {
    capture(str);
    realConsole.print(str);
  }

  public void println(String str) {
    capture(str);
    realConsole.println(str);
  }

  void capture(String str) {
    try {
      captureOut.write(str.getBytes());
      captureOut.flush();
    } catch (IOException e) {
    }
  }

  public void endCapture() {
    client.setConsole(realConsole);
    try {
      captureOut.close();
    } catch (IOException e) {
    }
  }

  public void serverConnect(SSHChannelController controller, Cipher sndCipher) {
    realConsole.serverConnect(controller, sndCipher);
  }

  public void serverDisconnect(String reason) {
    realConsole.serverDisconnect(reason);
  }

}
