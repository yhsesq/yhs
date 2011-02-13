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
import java.util.Vector;

import mindbright.security.*;

public final class SSHChannelController extends SSH implements SSHChannelListener {

  protected SSHTxChannel        txChan;
  protected SSHRxChannel        rxChan;
  protected SSHConnectChannel   cnChan;
  protected SSHPduQueue         txQueue;
  protected SSHPduQueue         cnQueue;

  protected int      totalTunnels;
  protected int      nextEmptyChan;
  protected Object[] tunnels;

  protected Vector listenChannels;

  protected SSH        sshHook;
  protected SSHConsole console;

  protected Cipher sndCipher;
  protected Cipher rcvCipher;

  public SSHChannelController(SSH sshHook, InputStream in, OutputStream out,
			      Cipher sndCipher, Cipher rcvCipher,
			      SSHConsole console, boolean haveCnxWatch) {
    this.sndCipher = sndCipher;
    this.rcvCipher = rcvCipher;

    this.sshHook = sshHook;
    this.console = console;

    this.tunnels         = new Object[16];
    this.nextEmptyChan   = 0;
    this.totalTunnels    = 0;
    this.listenChannels  = new Vector();
    
    txChan = new SSHTxChannel(out, MAIN_CHAN_NUM);
    rxChan = new SSHRxChannel(in, MAIN_CHAN_NUM);

    rxChan.setSSHChannelListener(this);
    txChan.setSSHChannelListener(this);
    rxChan.setSSHPduFactory(new SSHPduInputStream(MSG_ANY, rcvCipher));
    txQueue = txChan.getQueue();

    if(haveCnxWatch) {
      cnChan = new SSHConnectChannel(this);
      cnChan.setSSHChannelListener(this);
      cnQueue = cnChan.getQueue();
    } else {
      cnQueue = new SSHPduQueue();
    }

  }

  public void start() {
    txChan.start();
    rxChan.start();
    if(cnChan != null)
      cnChan.start();
  }

  public void waitForExit() throws InterruptedException {
    waitForExit(0); // Wait forever...
  }
  public void waitForExit(long msWait) throws InterruptedException {
    if(rxChan != null)
      rxChan.join(msWait);
    Thread.currentThread().sleep(100);
    killAll();
  }

  public void killAll() {
    killAllTunnels();
    killListenChannels();
    if(rxChan != null && rxChan.isAlive())
      rxChan.stop();
    if(txChan != null && txChan.isAlive())
      txChan.stop();
    if(cnChan != null && cnChan.isAlive())
      cnChan.stop();

    rxChan = null;
    txChan = null;
    cnChan = null;
    System.runFinalization();
  }

  public synchronized int newChannelId() {
    int newChan = nextEmptyChan;
    if(nextEmptyChan < tunnels.length) {
      int i;
      for(i = nextEmptyChan + 1; i < tunnels.length; i++)
	if(tunnels[i] == null)
	  break;
      nextEmptyChan = i;
    } else {
      Object[] tmp = new Object[tunnels.length + 16];
      System.arraycopy(tunnels, 0, tmp, 0, tunnels.length);
      tunnels = tmp;
      nextEmptyChan++;
    }

    return newChan;
  }

  public synchronized String[] listTunnels() {
    int i, cnt = 0;
    String[] list1 = new String[tunnels.length];

    for(i = 0; i < tunnels.length; i++) {
      if(tunnels[i] == null)
	continue;
      list1[cnt++] = ((SSHTunnel)tunnels[i]).getDescription();
    }

    String[] list2 = new String[cnt];
    System.arraycopy(list1, 0, list2, 0, cnt);

    return list2;
  }

  public synchronized void closeTunnelFromList(int listIdx) {
    int i;
    for(i = 0; i < tunnels.length; i++) {
      if(tunnels[i] == null)
	continue;
      listIdx--;
      if(listIdx < 0)
	break;
    }
    if(i < tunnels.length) {
      ((SSHTunnel)tunnels[i]).terminateNow();
    }
  }

  public synchronized void killAllTunnels() {
    for(int i = 0; i < tunnels.length; i++) {
      if(tunnels[i] == null)
	continue;
      ((SSHTunnel)tunnels[i]).openFailure(); // !!! Forced close
      tunnels[i] = null;
    }
    tunnels = new Object[16];
  }

  public synchronized void addTunnel(SSHTunnel tunnel) throws IOException {
    totalTunnels++;
    tunnels[tunnel.channelId] = tunnel;
  }

  public synchronized SSHTunnel delTunnel(int channelId) {
    SSHTunnel tunnelToDelete = (SSHTunnel) tunnels[channelId];
    tunnels[channelId] = null;
    nextEmptyChan = (channelId < nextEmptyChan ? channelId : nextEmptyChan);
    totalTunnels--;
    return tunnelToDelete;
  }

  public boolean haveHostInFwdOpen() {
    return sshHook.isProtocolFlagSet(PROTOFLAG_HOST_IN_FWD_OPEN);
  }

  public SSHListenChannel newListenChannel(String localHost, int localPort,
					   String remoteHost, int remotePort,
					   String plugin) throws IOException {
    SSHListenChannel newListenChan = null;
    newListenChan = SSHProtocolPlugin.getPlugin(plugin).localListener(localHost, localPort,
								      remoteHost, remotePort,
								      this);
    newListenChan.setSSHChannelListener(this);
    newListenChan.start();
    synchronized(listenChannels) {
      listenChannels.addElement(newListenChan);
    }
    return newListenChan;
  }

  public void killListenChannel(String localHost, int listenPort) {
    SSHListenChannel listenChan;
    synchronized(listenChannels) {
      for(int i = 0; i < listenChannels.size(); i++) {
	listenChan = (SSHListenChannel) listenChannels.elementAt(i);
	if(listenChan.getListenPort() == listenPort && listenChan.getListenHost().equals(localHost)) {
	  listenChannels.removeElementAt(i);
	  listenChan.forceClose();
	  break;
	}
      }
    }
  }

  public void killListenChannels() {
    SSHListenChannel listenChan;
    synchronized(listenChannels) {
      while(listenChannels.size() > 0) {
	listenChan = (SSHListenChannel) listenChannels.elementAt(0);
	listenChan.forceClose();
	listenChannels.removeElementAt(0);
      }
    }
  }

  public SSHPdu prepare(SSHPdu pdu) {
    return pdu;
  }

  public void transmit(SSHPdu pdu) {
    txQueue.putLast(pdu);
  }

  public void receive(SSHPdu pdu) {
    SSHPduInputStream inPdu = (SSHPduInputStream) pdu;
    SSHTunnel         tunnel;
    int               channelNum;
    try {
      switch(inPdu.type) {
      case SMSG_STDOUT_DATA:
	if(console != null)
	  console.stdoutWriteString(inPdu.readStringAsBytes());
	break;
      case SMSG_STDERR_DATA:
	if(console != null)
	  console.stderrWriteString(inPdu.readStringAsBytes());
	break;
      case SMSG_EXITSTATUS:
	SSHPduOutputStream exitPdu = new SSHPduOutputStream(CMSG_EXIT_CONFIRMATION, sndCipher);
	int status = inPdu.readInt();
	if(console != null) {
	  if(status != 0)
	    console.serverDisconnect(sshAsClient().getServerAddr().getHostName() + " disconnected: " + status);
	  else
	    console.serverDisconnect("Connection to " + sshAsClient().getServerAddr().getHostName() + " closed.");
	}
	transmit(exitPdu);
	sshAsClient().disconnect(true);
	break;
      case SMSG_X11_OPEN:
	// Fallthrough
      case MSG_PORT_OPEN:
	cnQueue.putLast(inPdu);
	break;
      case MSG_CHANNEL_DATA:
	channelNum = inPdu.readInt();
	tunnel     = (SSHTunnel)tunnels[channelNum];
	if(tunnel != null)
	  tunnel.transmit(pdu);
	else
	  throw new Exception("Data on nonexistent channel: " + channelNum);
	break;
      case MSG_CHANNEL_OPEN_CONFIRMATION:
	channelNum  = inPdu.readInt();
	tunnel = (SSHTunnel)tunnels[channelNum];
	if(tunnel != null) {
	  if(!tunnel.setRemoteChannelId(inPdu.readInt()))
	    throw new Exception("Open confirmation on allready opened channel!");
	  tunnel.start();
	} else
	  throw new Exception("Open confirm on nonexistent: " + channelNum);
	break;
      case MSG_CHANNEL_OPEN_FAILURE:
	SSHTunnel failTunnel;
	channelNum = inPdu.readInt();
	if((failTunnel = delTunnel(channelNum)) != null) {
	  alert("Channel open failure on " + failTunnel.remoteDesc);
	  failTunnel.openFailure();
	} else
	  throw new Exception("Open failure on nonexistent channel: " + channelNum);
	break;
      case MSG_CHANNEL_INPUT_EOF:
	channelNum = inPdu.readInt();
	tunnel     = (SSHTunnel)tunnels[channelNum];
	if(tunnel != null) {
	  tunnel.receiveInputEOF();
	} else
	  throw new Exception("Input eof on nonexistent channel: " + channelNum);
	break;
      case MSG_CHANNEL_OUTPUT_CLOSED:
	channelNum  = inPdu.readInt();
	;
	if(channelNum < tunnels.length && ((tunnel = (SSHTunnel)tunnels[channelNum]) != null)) {
	  tunnel.receiveOutputClosed();
	} else
	  throw new Exception("Output closed on nonexistent channel: " + channelNum);
	break;
      case MSG_DISCONNECT:
	disconnect("Peer disconnected: " + inPdu.readString());
	break;
      case CMSG_WINDOW_SIZE:
	break;
      case CMSG_STDIN_DATA:
	break;
      case CMSG_EOF:
	System.out.println("!!! EOF received...");
	break;
      case CMSG_EXIT_CONFIRMATION:
	break;
      default:
	throw new Exception("Unknown packet type (" + inPdu.type + "), disconnecting...");
      }
    } catch(Exception e) {
      // !!! Are there known BUGS in here?? Nah... :-)
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      System.out.println("\nplease send a mail to mats@mindbright.se with:");
      System.out.println("(I found a bug in MindTerm!), error: " + e.getMessage());
      System.out.println(sw.toString());
      sendDisconnect("please send a mail to mats@mindbright.se with:" + "\n\r" +
		     "(I found a bug in MindTerm!), error: " + e.getMessage() + "\n\r" +
		     kludgeLF2CRLFMap(sw.toString()));
      // !!!
    }
  }

  static String kludgeLF2CRLFMap(String orig) {
    int o = 0, n;
    String result = "";
    while((n = orig.indexOf('\n', o)) != -1) {
      result += orig.substring(o, n) + "\n\r";
      o = n + 1;
    }
    result += orig.substring(o);
    return result;
  }

  public void close(SSHChannel chan) {
    // !!!
    if(chan instanceof SSHConnectChannel)
      SSH.logExtra("Controller connect-channel closed");
    else if(chan instanceof SSHTxChannel)
      SSH.logExtra("Controller TX-channel closed");
    else if(chan instanceof SSHRxChannel)
      SSH.logExtra("Controller RX-channel closed");
    else if(chan instanceof SSHListenChannel)
      SSH.logExtra("Listen channel for port " + ((SSHListenChannel)chan).getListenPort() +
		   " closed");
    else
      alert("Bug in SSHChannelController.close 'chan' is: " + chan);
  }

  public void disconnect(String reason) {
    if(sshHook.isAnSSHClient)
      sshAsClient().disconnect(false);
    if(txChan != null)
	txChan.setClosePending();
    if(console != null)
      console.serverDisconnect("\r\nDisconnecting, " + reason);
    else
      SSH.log("\r\nDisconnecting, " + reason);

    if(!sshHook.isAnSSHClient && rxChan != null) {
      rxChan.forceClose();
    }
  }

  public void sendDisconnect(String reason) {
    try {
      SSHPduOutputStream pdu = new SSHPduOutputStream(MSG_DISCONNECT, sndCipher);
      pdu.writeString(reason);
      if(txQueue != null)
	  txQueue.putFirst(pdu);
      Thread.currentThread().sleep(300);
      disconnect(reason);
    } catch (Exception e) {
      alert("Error in sendDisconnect: " + e.toString());
    }
  }

  public void alert(String msg) {
    if(sshHook.isAnSSHClient) {
	SSHInteractor interactor = sshAsClient().user.getInteractor();
	if(interactor != null)
	    interactor.alert(msg);
    } else {
	SSH.log(msg);
    }
  }

  protected SSHClient sshAsClient() {
    return (SSHClient)sshHook;
  }

  public SSHPduQueue getCnQueue() {
    return cnQueue;
  }

  public void addHostMapTemporary(String fromHost, String toHost, int toPort) {
    cnChan.addHostMapTemporary(fromHost, toHost, toPort);
  }

  public void addHostMapPermanent(String fromHost, String toHost, int toPort) {
    cnChan.addHostMapPermanent(fromHost, toHost, toPort);
  }

  public void delHostMap(String fromHost) {
    cnChan.delHostMap(fromHost);
  }

  public Vector getHostMap(String fromHost) {
    return cnChan.getHostMap(fromHost);
  }

}
