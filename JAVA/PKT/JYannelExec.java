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

public class JYannelExec extends JYannelSession{
  boolean xforwading=false;
  String command="";
  /*
  JYannelExec(){
    super();
    type="session".getBytes();
    io=new IO();
  }
  */
  public void setXForwarding(boolean foo){
    xforwading=true;
  }
  public void start(){
    try{
      JYquest request;
      if(xforwading){
        request=new JYquestX11();
        request.request(session, this);
      }
      request=new JYquestExec(command);
      //((JYquestExec)request).setCommand(command);
      request.request(session, this);
    }
    catch(Exception e){
    }
    thread=new Thread(this);
    thread.start();
  }
  public void setCommand(String foo){ command=foo;}
  public void init(){
    io.setInputStream(session.in);
    io.setOutputStream(session.out);
  }
  public void finalize() throws java.lang.Throwable{
    if(thread!=null){
      ((Thread)thread).interrupt();
      thread=null;
    }
    super.finalize();
  }
  public void run(){
//System.out.println(this+":run >");
//    thread=Thread.currentThread();
    JYffer buf=new JYffer();
//    JYffer buf=new JYffer(lmpsize);
    JYcket packet=new JYcket(buf);
    int i=0;
    try{
      while(isConnected() &&
	    thread!=null && 
	    io!=null && 
	    io.in!=null){
        i=io.in.read(buf.buffer, 14, buf.buffer.length-14);
	if(i==0)continue;
	if(i==-1){
	  eof();
	  break;
	}
	if(close)break;
        packet.reset();
        buf.putByte((byte)JYssion.SSH_MSG_CHANNEL_DATA);
        buf.putInt(recipient);
        buf.putInt(i);
        buf.skip(i);
	session.write(packet, this, i);
      }
    }
    catch(Exception e){
      //System.out.println("# JYannelExec.run");
      //e.printStackTrace();
    }
    thread=null;
//System.out.println(this+":run <");
  }

  public void setErrStream(java.io.OutputStream out){
    setExtOutputStream(out);
  }
  public java.io.InputStream getErrStream() throws java.io.IOException {
    return getExtInputStream();
  }
}
