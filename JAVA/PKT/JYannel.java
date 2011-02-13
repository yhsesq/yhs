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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


public abstract class JYannel implements Runnable{
  static int index=0; 
  private static java.util.Vector pool=new java.util.Vector();
  static JYannel getChannel(String type){
    if(type.equals("session")){
      return new JYannelSession();
    }
    if(type.equals("shell")){
      return new JYannelShell();
    }
    if(type.equals("exec")){
      return new JYannelExec();
    }
    if(type.equals("x11")){
      return new JYannelX11();
    }
    if(type.equals("direct-tcpip")){
      return new JYannelDirectTCPIP();
    }
    if(type.equals("forwarded-tcpip")){
      return new JYannelForwardedTCPIP();
    }
    if(type.equals("sftp")){
      return new JYannelSftp();
    }
    return null;
  }
  static JYannel getChannel(int id, JYssion session){
    synchronized(pool){
      for(int i=0; i<pool.size(); i++){
	JYannel c=(JYannel)(pool.elementAt(i));
	if(c.id==id && c.session==session) return c;
      }
    }
    return null;
  }
  static void del(JYannel c){
    synchronized(pool){
      pool.removeElement(c);
    }
  }

  int id;
  int recipient=-1;
  byte[] type="foo".getBytes();
  int lwsize_max=0x100000;
  int lwsize=lwsize_max;  // local initial window size
  int lmpsize=0x4000;     // local maximum packet size

  int rwsize=0;         // remote initial window size
  int rmpsize=0;        // remote maximum packet size

  JYIO io=null;    
  Thread thread=null;

  boolean eof_local=false;
  boolean eof_remote=false;

  boolean close=false;

  int exitstatus=-1;

  int reply=0; 

  JYssion session;

  JYannel(){
    synchronized(pool){
      id=index++;
      pool.addElement(this);
    }
  }
  void setRecipient(int foo){
    this.recipient=foo;
  }
  int getRecipient(){
    return recipient;
  }

  void init(){
  }

  public void connect() throws JYchException{
    if(!isConnected()){
      throw new JYchException("session is down");
    }
    try{
      JYffer buf=new JYffer(100);
      JYcket packet=new JYcket(buf);
      // send
      // byte   SSH_MSG_CHANNEL_OPEN(90)
      // string channel type         //
      // uint32 sender channel       // 0
      // uint32 initial window size  // 0x100000(65536)
      // uint32 maxmum packet size   // 0x4000(16384)
      packet.reset();
      buf.putByte((byte)90);
      buf.putString(this.type);
      buf.putInt(this.id);
      buf.putInt(this.lwsize);
      buf.putInt(this.lmpsize);
      session.write(packet);

      int retry=1000;
      while(this.getRecipient()==-1 &&
	    session.isConnected() &&
	    retry>0){
	try{Thread.sleep(50);}catch(Exception ee){}
	retry--;
      }
      if(!session.isConnected()){
	throw new JYchException("session is down");
      }
      if(retry==0){
        throw new JYchException("channel is not opened.");
      }
      start();
    }
    catch(Exception e){
      if(e instanceof JYchException) throw (JYchException)e;
    }
  }

  public void setXForwarding(boolean foo){
  }

  public void start() throws JYchException{}

  public boolean isEOF() {return eof_remote;}

  void getData(JYffer buf){
    setRecipient(buf.getInt());
    setRemoteWindowSize(buf.getInt());
    setRemotePacketSize(buf.getInt());
  }

  public void setInputStream(InputStream in){
    io.setInputStream(in);
  }
  public void setOutputStream(OutputStream out){
    io.setOutputStream(out);
  }
  public void setExtOutputStream(OutputStream out){
    io.setExtOutputStream(out);
  }
  public InputStream getInputStream() throws IOException {
    PipedInputStream in=new PipedInputStream();
    io.setOutputStream(new PassiveOutputStream(in));
    return in;
  }
  public InputStream getExtInputStream() throws IOException {
    PipedInputStream in=new PipedInputStream();
    io.setExtOutputStream(new PassiveOutputStream(in));
    return in;
  }
  public OutputStream getOutputStream() throws IOException {
    PipedOutputStream out=new PipedOutputStream();
    io.setInputStream(new PassiveInputStream(out));
    return out;
  }

  void setLocalWindowSizeMax(int foo){ this.lwsize_max=foo; }
  void setLocalWindowSize(int foo){ this.lwsize=foo; }
  void setLocalPacketSize(int foo){ this.lmpsize=foo; }
  void setRemoteWindowSize(int foo){ this.rwsize=foo; }
  void addRemoteWindowSize(int foo){ this.rwsize+=foo; }
  void setRemotePacketSize(int foo){ this.rmpsize=foo; }

  public void run(){
  }

  void write(byte[] foo) throws IOException {
    write(foo, 0, foo.length);
  }
  void write(byte[] foo, int s, int l) throws IOException {
    //if(eof_remote)return;
    if(io.out!=null)
      io.put(foo, s, l);
  }
  void write_ext(byte[] foo, int s, int l) throws IOException {
    //if(eof_remote)return;
    if(io.out_ext!=null)
      io.put_ext(foo, s, l);
  }

  void eof(){
//System.out.println("EOF!!!! "+this);
//Thread.dumpStack();
    if(eof_local)return;
    eof_local=true;
    //close=eof;
    try{
      JYffer buf=new JYffer(100);
      JYcket packet=new JYcket(buf);
      packet.reset();
      buf.putByte((byte)JYssion.SSH_MSG_CHANNEL_EOF);
      buf.putInt(getRecipient());
      session.write(packet);
    }
    catch(Exception e){
      //System.out.println("JYannel.eof");
      //e.printStackTrace();
    }
    if(!isConnected()){
      disconnect();
    }
  }

  void close(){
    //System.out.println("close!!!!");
    if(close)return;
    close=true;
    try{
      JYffer buf=new JYffer(100);
      JYcket packet=new JYcket(buf);
      packet.reset();
      buf.putByte((byte)JYssion.SSH_MSG_CHANNEL_CLOSE);
      buf.putInt(getRecipient());
      session.write(packet);
    }
    catch(Exception e){
      //e.printStackTrace();
    }
  }
  static void eof(JYssion session){
    JYannel[] channels=null;
    int count=0;
    synchronized(pool){
      channels=new JYannel[pool.size()];
      for(int i=0; i<pool.size(); i++){
	try{
	  JYannel c=((JYannel)(pool.elementAt(i)));
	  if(c.session==session){
	    channels[count++]=c;
	  }
	}
	catch(Exception e){
	}
      } 
    }
    for(int i=0; i<count; i++){
      channels[i].eof();
    }
  }

  public void finalize() throws Throwable{
    disconnect();
    super.finalize();
    session=null;
  }

  public void disconnect(){
//System.out.println(this+":disconnect "+((JYannelExec)this).command+" "+io.in);
//System.out.println(this+":disconnect "+io+" "+io.in);
    close();
//System.out.println("$1");
    thread=null;
    try{
      if(io!=null){
	try{
	  //System.out.println(" io.in="+io.in);
	  if(io.in!=null && 
	     (io.in instanceof PassiveInputStream)
	     )
	    io.in.close();
	}
	catch(Exception ee){}
	try{
	  //System.out.println(" io.out="+io.out);
	  if(io.out!=null && 
	     (io.out instanceof PassiveOutputStream)
	     )
	    io.out.close();
	}
	catch(Exception ee){}
      }
    }
    catch(Exception e){
      //e.printStackTrace();
    }
//System.out.println("$2");
    io=null;
    JYannel.del(this);
  }

  public boolean isConnected(){
    if(this.session!=null){
      return session.isConnected();
    }
    return false;
  }

  public void sendSignal(String foo) throws Exception {
    JYquestSignal request=new JYquestSignal();
    request.setSignal(foo);
    request.request(session, this);
  }

//  public String toString(){
//      return "JYannel: type="+new String(type)+",id="+id+",recipient="+recipient+",window_size="+window_size+",packet_size="+packet_size;
//  }

/*
  class OutputThread extends Thread{
    JYannel c;
    OutputThread(JYannel c){ this.c=c;}
    public void run(){c.output_thread();}
  }
*/

  class PassiveInputStream extends PipedInputStream{
    PipedOutputStream out;
    PassiveInputStream(PipedOutputStream out) throws IOException{
      super(out);
      this.out=out;
    }
    public void close() throws IOException{
      if(out!=null){
        this.out.close();
      }
      out=null;
    }
  }
  class PassiveOutputStream extends PipedOutputStream{
    PassiveOutputStream(PipedInputStream in) throws IOException{
      super(in);
    }
  }

  void setExitStatus(int foo){ exitstatus=foo; }
  public int getExitStatus(){ return exitstatus; }

  void setSession(JYssion session){
    this.session=session;
  }
}
