/* -*-mode:java; c-basic-offset:2; -*- */
/*
Copyright (c) 2002,2003,2004 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package net.sf.pkt.ssh2;

import java.net.*;
import java.io.*;

class JYrtWatcher implements Runnable{
  private static java.util.Vector pool=new java.util.Vector();

  JYssion session;
  int lport;
  int rport;
  String host;
  String boundaddress;
  ServerSocket ss;
  Runnable thread;

  static String[] getPortForwarding(JYssion session){
    java.util.Vector foo=new java.util.Vector();
    synchronized(pool){
      for(int i=0; i<pool.size(); i++){
	JYrtWatcher p=(JYrtWatcher)(pool.elementAt(i));
	if(p.session==session){
	  foo.addElement(p.lport+":"+p.host+":"+p.rport);
	}
      }
    }
    String[] bar=new String[foo.size()];
    for(int i=0; i<foo.size(); i++){
      bar[i]=(String)(foo.elementAt(i));
    }
    return bar;
  }
  static JYrtWatcher getPort(JYssion session, int lport){
    synchronized(pool){
      for(int i=0; i<pool.size(); i++){
	JYrtWatcher p=(JYrtWatcher)(pool.elementAt(i));
	if(p.session==session && p.lport==lport) return p;
      }
      return null;
    }
  }
  static JYrtWatcher addPort(JYssion session, String address, int lport, String host, int rport) throws JYchException{
    if(getPort(session, lport)!=null){
      throw new JYchException("PortForwardingL: local port "+lport+" is already registered.");
    }
    JYrtWatcher pw=new JYrtWatcher(session, address, lport, host, rport);
    pool.addElement(pw);
    return pw;
  }
  static void delPort(JYssion session, int lport) throws JYchException{
    JYrtWatcher pw=getPort(session, lport);
    if(pw==null){
      throw new JYchException("PortForwardingL: local port "+lport+" is not registered.");
    }
    pw.delete();
    pool.removeElement(pw);
  }
  static void delPort(JYssion session){
    JYrtWatcher[] foo=new JYrtWatcher[pool.size()];
    int count=0;
    synchronized(pool){
      for(int i=0; i<pool.size(); i++){
	JYrtWatcher p=(JYrtWatcher)(pool.elementAt(i));
	if(p.session==session) {
	  p.delete();
	  foo[count++]=p;
	}
      }
      for(int i=0; i<count; i++){
	JYrtWatcher p=foo[i];
	pool.removeElement(p);
      }
    }
  }
  JYrtWatcher(JYssion session, 
	      String boundaddress, int lport, 
	      String host, int rport) throws JYchException{
    this.session=session;
    this.boundaddress=boundaddress;
    this.lport=lport;
    this.host=host;
    this.rport=rport;
    try{
//    ss=new ServerSocket(port);
      ss=new ServerSocket(lport, 0, 
			  InetAddress.getByName(this.boundaddress));
    }
    catch(Exception e){ 
      System.out.println(e);
      throw new JYchException("PortForwardingL: local port "+lport+" cannot be bound.");
    }
  }

  public void run(){
    JYffer buf=new JYffer(300); // ??
    JYcket packet=new JYcket(buf);
    thread=this;
    try{
      while(thread!=null){
        Socket socket=ss.accept();
	socket.setTcpNoDelay(true);
        InputStream in=socket.getInputStream();
        OutputStream out=socket.getOutputStream();
        JYannelDirectTCPIP channel=new JYannelDirectTCPIP();
        channel.init();
        channel.setInputStream(in);
        channel.setOutputStream(out);
	session.addChannel(channel);
	((JYannelDirectTCPIP)channel).setHost(host);
	((JYannelDirectTCPIP)channel).setPort(rport);
	((JYannelDirectTCPIP)channel).setOrgIPAddress(socket.getInetAddress().getHostAddress());
	((JYannelDirectTCPIP)channel).setOrgPort(socket.getPort());
        channel.connect();
	if(channel.exitstatus!=-1){
	}
      }
    }
    catch(Exception e){
      //System.out.println("! "+e);
    }
  }

  void delete(){
    thread=null;
    try{ 
      if(ss!=null)ss.close();
      ss=null;
    }
    catch(Exception e){
    }
  }
}
