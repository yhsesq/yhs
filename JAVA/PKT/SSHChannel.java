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

public abstract class SSHChannel implements Runnable { // Thread {

  protected int                channelId;
  protected SSHChannelListener listener;
  private Thread thread;
  
  public SSHChannel(int channelId) {
//    super();
	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}
    thread = new Thread( SSH.getThreadGroup(), this, SSH.createThreadName() ); // JH_Mod
    this.channelId = channelId;
    this.listener  = null;
  }

  public void setSSHChannelListener(SSHChannelListener listener) {
    this.listener = listener;
  }

  public int getId() {
    return channelId;
  }

  public abstract void serviceLoop() throws Exception;

  public void close() {
  }

  public void run() {

    if(SSH.NETSCAPE_SECURITY_MODEL) {
      try {
	netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
      } catch (netscape.security.ForbiddenTargetException e) {
	// !!! A pity, we could have done so much fun... :-)
      }
    }

    try {
      serviceLoop();
    } catch (Exception e) {

      if(SSH.DEBUGMORE) {
	System.out.println("--- channel exit (exception is not an error):");
	e.printStackTrace();
	System.out.println("---");
      }

      close();
      if(listener != null)
	listener.close(this);

    } catch (ThreadDeath death) {
      SSH.logExtra("Channel killed " + channelId);
      throw death;
    }
  }

  public void start() {
   thread.start();   
  }
  
  public void stop() {
   thread.stop();   
  }
  
  public void join(long l) throws InterruptedException {
   thread.join( l );
  }
  
  public boolean isAlive() {
      return thread.isAlive();
  }
}
