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

import java.io.*;
import java.net.*;
import java.lang.*;

public class JYssion implements Runnable{
  static private final String version="SSH2-0.1.20x";

  // http://ietf.org/internet-drafts/draft-ietf-secsh-assignednumbers-01.txt
  static final int SSH_MSG_DISCONNECT=                      1;
  static final int SSH_MSG_IGNORE=                          2;
  static final int SSH_MSG_UNIMPLEMENTED=                   3;
  static final int SSH_MSG_DEBUG=                           4;
  static final int SSH_MSG_SERVICE_REQUEST=                 5;
  static final int SSH_MSG_SERVICE_ACCEPT=                  6;
  static final int SSH_MSG_KEXINIT=                        20;
  static final int SSH_MSG_NEWKEYS=                        21;
  static final int SSH_MSG_KEXDH_INIT=                     30;
  static final int SSH_MSG_KEXDH_REPLY=                    31;
  static final int SSH_MSG_USERAUTH_REQUEST=               50;
  static final int SSH_MSG_USERAUTH_FAILURE=               51;
  static final int SSH_MSG_USERAUTH_SUCCESS=               52;
  static final int SSH_MSG_USERAUTH_BANNER=                53;
  static final int SSH_MSG_USERAUTH_INFO_REQUEST=          60;
  static final int SSH_MSG_USERAUTH_INFO_RESPONSE=         61;
  static final int SSH_MSG_USERAUTH_PK_OK=                 60;
  static final int SSH_MSG_GLOBAL_REQUEST=                 80;
  static final int SSH_MSG_REQUEST_SUCCESS=                81;
  static final int SSH_MSG_REQUEST_FAILURE=                82;
  static final int SSH_MSG_CHANNEL_OPEN=                   90;
  static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION=      91;
  static final int SSH_MSG_CHANNEL_OPEN_FAILURE=           92;
  static final int SSH_MSG_CHANNEL_WINDOW_ADJUST=          93;
  static final int SSH_MSG_CHANNEL_DATA=                   94;
  static final int SSH_MSG_CHANNEL_EXTENDED_DATA=          95;
  static final int SSH_MSG_CHANNEL_EOF=                    96;
  static final int SSH_MSG_CHANNEL_CLOSE=                  97;
  static final int SSH_MSG_CHANNEL_REQUEST=                98;
  static final int SSH_MSG_CHANNEL_SUCCESS=                99;
  static final int SSH_MSG_CHANNEL_FAILURE=               100;

  private byte[] V_S;                                 // server version
  private byte[] V_C=("SSH-2.0-"+version).getBytes(); // client version

  private byte[] I_C; // the payload of the client's SSH_MSG_KEXINIT
  private byte[] I_S; // the payload of the server's SSH_MSG_KEXINIT
  private byte[] K_S; // the host key

  private byte[] session_id;

  private byte[] IVc2s;
  private byte[] IVs2c;
  private byte[] Ec2s;
  private byte[] Es2c;
  private byte[] MACc2s;
  private byte[] MACs2c;

  private int seqi=0;
  private int seqo=0;

  private JYpher s2ccipher;
  private JYpher c2scipher;
  private JYC s2cmac;
  private JYC c2smac;
  private byte[] mac_buf;

  private JYmpression deflater;
  private JYmpression inflater;

  private JYIO io;
  private Socket socket;
  private int timeout=0;

  private boolean isConnected=false;

  private Thread connectThread=null;

  InputStream in=null;
  OutputStream out=null;

  static JYndom random;

  JYffer buf;
  JYcket packet;

  JYcketFactory socket_factory=null;

  private java.util.Hashtable config=null;

  private JYoxy proxy=null;
  private JYerInfo userinfo;

  String host="127.0.0.1";
  int port=22;

  String username=null;
  String password=null;

  JYch jsch;

  JYssion(JYch jsch) throws JYchException{
    super();
    this.jsch=jsch;
    buf=new JYffer();
    packet=new JYcket(buf);
    io=new JYIO();
  }

  public void connect() throws JYchException{
    connect(timeout);
  }

  public void connect(int connectTimeout) throws JYchException{
    if(random==null){
      try{
	Class c=Class.forName(getConfig("random"));
        random=(JYndom)(c.newInstance());
      }
      catch(Exception e){ System.err.println("connect: random "+e); }
    }
    JYcket.setRandom(random);

    try	{
      int i, j;
      int pad=0;

      if(proxy==null){
	proxy=jsch.getProxy(host);
	if(proxy!=null){
	  synchronized(proxy){
	    proxy.close();
	  }
	}
      }

      if(proxy==null){
        InputStream in;
        OutputStream out;
	if(socket_factory==null){
	  if(connectTimeout==0){
	    socket=new Socket(host, port);
	  }
	  else{
	    final Socket[] sockp=new Socket[1];
	    final Thread thread=Thread.currentThread();
	    final Exception[] ee=new Exception[1];
	    final boolean[] done=new boolean[1];
	    done[0]=false;
	    String message="";
	    Thread tmp=new Thread(new Runnable(){
		public void run(){
		  try{
		    sockp[0]=new Socket(host, port);
		    if(done[0]){ 
		      if(sockp[0]!=null){
			sockp[0].close();
			sockp[0]=null;
		      }
		    }
		    else thread.interrupt();
		  }
		  catch(Exception e){
		    ee[0]=e;
		    thread.interrupt();
		    if(sockp[0]!=null){
		      try{
			sockp[0].close();
			sockp[0]=null;
		      }catch(Exception eee){}
		    }
		  }
		}
	      });
	    tmp.start();
	    try{ 
	      Thread.sleep(connectTimeout); 
	      message="timeout: ";
	    }
	    catch(java.lang.InterruptedException eee){
	      tmp.interrupt();
	      tmp=null;
	      System.gc();
	    }
	    done[0]=true;
	    if(sockp[0]!=null){
	      socket=sockp[0];
	    }
	    else{
	      message+="socket is not established";
	      if(ee[0]!=null){
		message=ee[0].toString();
	      }
	      throw new JYchException(message);
	    }
	  }
	  in=socket.getInputStream();
	  out=socket.getOutputStream();
	}
	else{
          socket=socket_factory.createSocket(host, port);
	  in=socket_factory.getInputStream(socket);
	  out=socket_factory.getOutputStream(socket);
	}
	if(timeout>0){
	  socket.setSoTimeout(timeout);
	}
        socket.setTcpNoDelay(true);
        io.setInputStream(in);
        io.setOutputStream(out);
      }
      else{
	synchronized(proxy){
	  proxy.connect(this, host, port);
	  io.setInputStream(proxy.getInputStream());
	  io.setOutputStream(proxy.getOutputStream());
	}
      }

      isConnected=true;

      i=0;
      while(i<buf.buffer.length){
        j=io.getByte();
        buf.buffer[i]=(byte)j; i++; 
        if(j==10)break;
      }

      if(buf.buffer[i-1]==10){    // 0x0a
	i--;
	if(buf.buffer[i-1]==13){  // 0x0d
	  i--;
	}
      }

      if(i==buf.buffer.length ||
	 i<7 ||                                      // SSH-1.99 or SSH-2.0
	 (buf.buffer[4]=='1' && buf.buffer[6]!='9')  // SSH-1.5
	 ){
	throw new JYchException("invalid server's version string");
      }

      V_S=new byte[i]; System.arraycopy(buf.buffer, 0, V_S, 0, i);
      //System.out.println("V_S: ("+i+") ["+new String(V_S)+"]");

      //io.put(V_C, 0, V_C.length); io.put("\n".getBytes(), 0, 1);
      {
	// Some Cisco devices will miss to read '\n' if it is sent separately.
	byte[] foo=new byte[V_C.length+1];
	System.arraycopy(V_C, 0, foo, 0, V_C.length);
	foo[foo.length-1]=(byte)'\n';
	io.put(foo, 0, foo.length);
      }

      buf=read(buf);
      //System.out.println("read: 20 ? "+buf.buffer[5]);
      if(buf.buffer[5]!=SSH_MSG_KEXINIT){
	throw new JYchException("invalid protocol: "+buf.buffer[5]);
      }
      JYyExchange kex=receive_kexinit(buf);

      boolean result;
      while(true){
	buf=read(buf);
	if(kex.getState()==buf.buffer[5]){
	  result=kex.next(buf);
	  if(!result){
	    //System.out.println("verify: "+result);
	    throw new JYchException("verify: "+result);
	  }
	}
	else{
	  throw new JYchException("invalid protocol(kex): "+buf.buffer[5]);
	}
	if(kex.getState()==JYyExchange.STATE_END){
	  break;
	}
      }

      checkHost(host, kex);

send_newkeys();

      // receive SSH_MSG_NEWKEYS(21)
      buf=read(buf);
      //System.out.println("read: 21 ? "+buf.buffer[5]);
      if(buf.buffer[5]==SSH_MSG_NEWKEYS){
	receive_newkeys(buf, kex);
      }
      else{
	throw new JYchException("invalid protocol(newkyes): "+buf.buffer[5]);
      }

      boolean auth=false;
      boolean auth_cancel=false;

      JYerAuthNone usn=new JYerAuthNone(userinfo);
      auth=usn.start(this);

      String methods=usn.getMethods();
      // methods: publickey,password,keyboard-interactive
      if(methods==null){
	methods="publickey,password,keyboard-interactive";
      }

      loop:
      while(true){

//System.out.println("methods: "+methods);

	while(!auth && 
	      methods!=null && methods.length()>0){

//System.out.println("  methods: "+methods);

	  JYerAuth us=null;
	  if(methods.startsWith("publickey")){
//System.out.println("   jsch.identities.size()="+jsch.identities.size());
	    synchronized(jsch.identities){
	      if(jsch.identities.size()>0){
		us=new JYerAuthPublicKey(userinfo);
	      }
	    }
	  }
	  else if(methods.startsWith("keyboard-interactive")){
	    if(userinfo instanceof JYKeyboardInteractive){
	      us=new JYerAuthKeyboardInteractive(userinfo);
	    }
	  }
	  else if(methods.startsWith("password")){
	    us=new JYerAuthPassword(userinfo);
	  }
	  if(us!=null){
	    try{ 
	      auth=us.start(this); 
	      auth_cancel=false;
	    }
	    catch(JYchAuthCancelException ee){
	      //System.out.println(ee);
	      auth_cancel=true;
	    }
	    catch(JYchPartialAuthException ee){
	      methods=ee.getMethods();
	      //System.out.println("PartialAuth: "+methods);
	      auth_cancel=false;
	      continue loop;
	    }
	    catch(RuntimeException ee){
	      throw ee;
	    }
	    catch(Exception ee){
	      //System.out.println("ee: "+ee); // SSH_MSG_DISCONNECT: 2 Too many authentication failures
	    }
	  }
	  if(!auth){
	    int comma=methods.indexOf(",");
	    if(comma==-1) break;
	    methods=methods.substring(comma+1);
	  }
	}
        break;
      }

      if(auth){
	connectThread=new Thread(this);
	connectThread.start();
	return;
      }
      if(auth_cancel)
	throw new JYchException("Auth cancel");
      throw new JYchException("Auth fail");
    }
    catch(Exception e) {
      if(isConnected){
	try{
	  packet.reset();
	  buf.putByte((byte)SSH_MSG_DISCONNECT);
	  buf.putInt(3);
	  buf.putString(e.toString().getBytes());
	  buf.putString("en".getBytes());
	  write(packet);
	  disconnect();
	}
	catch(Exception ee){
	}
      }
      isConnected=false;
      //e.printStackTrace();
      if(e instanceof RuntimeException) throw (RuntimeException)e;
      if(e instanceof JYchException) throw (JYchException)e;
      throw new JYchException("Session.connect: "+e);
    }
  }

  private JYyExchange receive_kexinit(JYffer buf) throws Exception {
    int j=buf.getInt();
    if(j!=buf.getLength()){    // packet was compressed and
      buf.getByte();           // j is the size of deflated packet.
      I_S=new byte[buf.index-5];
    }
    else{
      I_S=new byte[j-1-buf.getByte()];
    }
    System.arraycopy(buf.buffer, buf.s, I_S, 0, I_S.length);
/*
try{
byte[] tmp=new byte[I_S.length];
System.arraycopy(I_S, 0, tmp, 0, I_S.length);
JYffer tmpb=new JYffer(tmp);
System.out.println("I_S: len="+I_S.length);
tmpb.setOffSet(17);
System.out.println("kex: "+new String(tmpb.getString()));
System.out.println("server_host_key: "+new String(tmpb.getString()));
System.out.println("cipher.c2s: "+new String(tmpb.getString()));
System.out.println("cipher.s2c: "+new String(tmpb.getString()));
System.out.println("mac.c2s: "+new String(tmpb.getString()));
System.out.println("mac.s2c: "+new String(tmpb.getString()));
System.out.println("compression.c2s: "+new String(tmpb.getString()));
System.out.println("compression.s2c: "+new String(tmpb.getString()));
System.out.println("lang.c2s: "+new String(tmpb.getString()));
System.out.println("lang.s2c: "+new String(tmpb.getString()));
System.out.println("?: "+(tmpb.getByte()&0xff));
System.out.println("??: "+tmpb.getInt());
}
catch(Exception e){
System.out.println(e);
}
*/

    send_kexinit();
    String[] guess=JYyExchange.guess(I_S, I_C);
    if(guess==null){
      throw new JYchException("Algorithm negotiation fail");
    }

    JYyExchange kex=null;
    try{
      Class c=Class.forName(getConfig(guess[JYyExchange.PROPOSAL_KEX_ALGS]));
      kex=(JYyExchange)(c.newInstance());
    }
    catch(Exception e){ System.err.println("kex: "+e); }
    kex.guess=guess;
    kex.init(this, V_S, V_C, I_S, I_C);
    return kex;
  }

  private boolean in_kex=false;
  public void rekey() throws Exception {
    send_kexinit();
  }
  private void send_kexinit() throws Exception {
    if(in_kex) return;
    in_kex=true;

    // byte      SSH_MSG_KEXINIT(20)
    // byte[16]  cookie (random bytes)
    // string    kex_algorithms
    // string    server_host_key_algorithms
    // string    encryption_algorithms_client_to_server
    // string    encryption_algorithms_server_to_client
    // string    mac_algorithms_client_to_server
    // string    mac_algorithms_server_to_client
    // string    compression_algorithms_client_to_server
    // string    compression_algorithms_server_to_client
    // string    languages_client_to_server
    // string    languages_server_to_client
    packet.reset();
    buf.putByte((byte) SSH_MSG_KEXINIT);
    synchronized(random){
      random.fill(buf.buffer, buf.index, 16); buf.skip(16);
    }
    buf.putString(getConfig("kex").getBytes());
    buf.putString(getConfig("server_host_key").getBytes());
    buf.putString(getConfig("cipher.c2s").getBytes());
    buf.putString(getConfig("cipher.s2c").getBytes());
    buf.putString(getConfig("mac.c2s").getBytes());
    buf.putString(getConfig("mac.s2c").getBytes());
    buf.putString(getConfig("compression.c2s").getBytes());
    buf.putString(getConfig("compression.s2c").getBytes());
    buf.putString(getConfig("lang.c2s").getBytes());
    buf.putString(getConfig("lang.s2c").getBytes());
    buf.putByte((byte)0);
    buf.putInt(0);

    buf.setOffSet(5);
    I_C=new byte[buf.getLength()];
    buf.getByte(I_C);

    write(packet);
  }

  private void send_newkeys() throws Exception {
    // send SSH_MSG_NEWKEYS(21)
    packet.reset();
    buf.putByte((byte)SSH_MSG_NEWKEYS);
    write(packet);
  }

  private void checkHost(String host, JYyExchange kex) throws JYchException {
    String shkc=getConfig("StrictHostKeyChecking");

    //System.out.println("shkc: "+shkc);

    byte[] K_S=kex.getHostKey();
    String key_type=kex.getKeyType();
    String key_fprint=kex.getFingerPrint();

    hostkey=new JYstKey(host, K_S);

    JYstKeyRepository hkr=jsch.getHostKeyRepository();
    int i=0;
    synchronized(hkr){
      i=hkr.check(host, K_S);
    }
    if((shkc.equals("ask") || shkc.equals("yes")) &&
       i==JYstKeyRepository.CHANGED){
      String file=null;
      synchronized(hkr){
	file=hkr.getKnownHostsRepositoryID();
      }
      if(file==null){file="known_hosts";}
      String message=
"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"+
"@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @\n"+
"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"+
"IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!      \n"+
"Someone could be eavesdropping on you right now (man-in-the-middle attack)!\n"+
"It is also possible that the "+key_type+" host key has just been changed.\n"+
"The fingerprint for the "+key_type+" key sent by the remote host is\n"+
	  key_fprint+".\n"+
"Please contact your system administrator.\n"+
"Add correct host key in "+file+" to get rid of this message.";
      if(userinfo!=null){
	userinfo.showMessage(message);
      }
      throw new JYchException("HostKey has been changed");
    }

    boolean insert=false;
 
    if((shkc.equals("ask") || shkc.equals("yes")) &&
       i!=JYstKeyRepository.OK){
      if(shkc.equals("yes")){
	throw new JYchException("reject HostKey");
      }
      //System.out.println("finger-print: "+key_fprint);
      if(userinfo!=null){
	boolean foo=userinfo.promptYesNo(
"The authenticity of host '"+host+"' can't be established.\n"+
key_type+" key fingerprint is "+key_fprint+".\n"+
"Are you sure you want to continue connecting?"
					 );
	if(!foo){
	  throw new JYchException("reject HostKey");
	}
	insert=true;
      }
      else{
	if(i==JYstKeyRepository.NOT_INCLUDED) 
	  throw new JYchException("UnknownHostKey");
	else throw new JYchException("HostKey has been changed.");
      }
    }

    if(shkc.equals("no") && 
       JYstKeyRepository.NOT_INCLUDED==i){
      insert=true;
    }

    if(insert){
      synchronized(hkr){
	hkr.add(host, K_S, userinfo);
      }
    }

  }

//public void start(){ (new Thread(this)).start();  }

  public JYannel openChannel(String type) throws JYchException{
    if(!isConnected){
      throw new JYchException("session is down");
    }
    try{
      JYannel channel=JYannel.getChannel(type);
      addChannel(channel);
      channel.init();
      return channel;
    }
    catch(Exception e){
      //e.printStackTrace();
    }
    return null;
  }

  // encode will bin invoked in write with synchronization.
  public void encode(JYcket packet) throws Exception{
//System.out.println("encode: "+packet.buffer.buffer[5]);
//if(packet.buffer.buffer[5]==96){
//Thread.dumpStack();
//}
    if(deflater!=null){
      packet.buffer.index=deflater.compress(packet.buffer.buffer, 
					    5, packet.buffer.index);
    }
    packet.padding();
    byte[] mac=null;
    if(c2scipher!=null){
      int pad=packet.buffer.buffer[4];
      synchronized(random){
	random.fill(packet.buffer.buffer, packet.buffer.index-pad, pad);
      }
    }
    if(c2smac!=null){
      c2smac.update(seqo);
      c2smac.update(packet.buffer.buffer, 0, packet.buffer.index);
      mac=c2smac.doFinal();
    }
    if(c2scipher!=null){
      byte[] buf=packet.buffer.buffer;
      c2scipher.update(buf, 0, packet.buffer.index, buf, 0);
    }
    if(mac!=null){
      packet.buffer.putByte(mac);
    }
  }

  int[] uncompress_len=new int[1];

  public JYffer read(JYffer buf) throws Exception{
    int j=0;
    while(true){
      buf.reset();
      io.getByte(buf.buffer, buf.index, 8); buf.index+=8;
      if(s2ccipher!=null){
        s2ccipher.update(buf.buffer, 0, 8, buf.buffer, 0);
      }
      j=((buf.buffer[0]<<24)&0xff000000)|
        ((buf.buffer[1]<<16)&0x00ff0000)|
        ((buf.buffer[2]<< 8)&0x0000ff00)|
        ((buf.buffer[3]    )&0x000000ff);
      io.getByte(buf.buffer, buf.index, j-4); buf.index+=(j-4);
      if(s2ccipher!=null){
        s2ccipher.update(buf.buffer, 8, j-4, buf.buffer, 8);
      }
      if(s2cmac!=null){
	s2cmac.update(seqi);
	s2cmac.update(buf.buffer, 0, buf.index);
	byte[] result=s2cmac.doFinal();
	io.getByte(mac_buf, 0, mac_buf.length);
        if(!java.util.Arrays.equals(result, mac_buf)){
          System.out.println("mac error");
	  throw new IOException("MAC Error");
	}
      }
      seqi++;

      if(inflater!=null){
        //inflater.uncompress(buf);
	int pad=buf.buffer[4];
	uncompress_len[0]=buf.index-5-pad;
	byte[] foo=inflater.uncompress(buf.buffer, 5, uncompress_len);
	if(foo!=null){
	  buf.buffer=foo;
	  buf.index=5+uncompress_len[0];
	}
	else{
	  System.err.println("fail in inflater");
	  break;
	}
      }

      int type=buf.buffer[5]&0xff;
//System.out.println("read: "+type);
      if(type==SSH_MSG_DISCONNECT){
        buf.rewind();
        buf.getInt();buf.getShort();
	int reason_code=buf.getInt();
	byte[] description=buf.getString();
	byte[] language_tag=buf.getString();
/*
	System.err.println("SSH_MSG_DISCONNECT:"+
                           " "+reason_code+
			   " "+new String(description)+
			   " "+new String(language_tag));
*/
	throw new JYchException("SSH_MSG_DISCONNECT:"+
				" "+reason_code+
				" "+new String(description)+
				" "+new String(language_tag));
	//break;
      }
      else if(type==SSH_MSG_IGNORE){
      }
      else if(type==SSH_MSG_DEBUG){
        buf.rewind();
        buf.getInt();buf.getShort();
/*
	byte always_display=(byte)buf.getByte();
	byte[] message=buf.getString();
	byte[] language_tag=buf.getString();
	System.err.println("SSH_MSG_DEBUG:"+
			   " "+new String(message)+
			   " "+new String(language_tag));
*/
      }
      else if(type==SSH_MSG_CHANNEL_WINDOW_ADJUST){
          buf.rewind();
          buf.getInt();buf.getShort();
	  JYannel c=JYannel.getChannel(buf.getInt(), this);
	  if(c==null){
	  }
	  else{
	    c.addRemoteWindowSize(buf.getInt()); 
	  }
      }
      else{
        break;
      }
    }
    buf.rewind();
    return buf;
  }

  byte[] getSessionId(){
    return session_id;
  }

  private void receive_newkeys(JYffer buf, JYyExchange kex) throws Exception {
//    send_newkeys();
    in_kex=false;
    updateKeys(kex);
  }
  private void updateKeys(JYyExchange kex) throws Exception{
    byte[] K=kex.getK();
    byte[] H=kex.getH();
    JYSH hash=kex.getHash();

    String[] guess=kex.guess;

    if(session_id==null){
      session_id=new byte[H.length];
      System.arraycopy(H, 0, session_id, 0, H.length);
    }

    /*
      Initial IV client to server:     JYSH (K || H || "A" || session_id)
      Initial IV server to client:     JYSH (K || H || "B" || session_id)
      Encryption key client to server: JYSH (K || H || "C" || session_id)
      Encryption key server to client: JYSH (K || H || "D" || session_id)
      Integrity key client to server:  JYSH (K || H || "E" || session_id)
      Integrity key server to client:  JYSH (K || H || "F" || session_id)
    */

    buf.reset();
    buf.putMPInt(K);
    buf.putByte(H);
    buf.putByte((byte)0x41);
    buf.putByte(session_id);
    hash.update(buf.buffer, 0, buf.index);
    IVc2s=hash.digest();

    int j=buf.index-session_id.length-1;

    buf.buffer[j]++;
    hash.update(buf.buffer, 0, buf.index);
    IVs2c=hash.digest();

    buf.buffer[j]++;
    hash.update(buf.buffer, 0, buf.index);
    Ec2s=hash.digest();

    buf.buffer[j]++;
    hash.update(buf.buffer, 0, buf.index);
    Es2c=hash.digest();

    buf.buffer[j]++;
    hash.update(buf.buffer, 0, buf.index);
    MACc2s=hash.digest();

    buf.buffer[j]++;
    hash.update(buf.buffer, 0, buf.index);
    MACs2c=hash.digest();

    try{
      Class c;
  
      c=Class.forName(getConfig(guess[JYyExchange.PROPOSAL_ENC_ALGS_STOC]));
      s2ccipher=(JYpher)(c.newInstance());
      while(s2ccipher.getBlockSize()>Es2c.length){
        buf.reset();
        buf.putMPInt(K);
        buf.putByte(H);
        buf.putByte(Es2c);
        hash.update(buf.buffer, 0, buf.index);
        byte[] foo=hash.digest();
        byte[] bar=new byte[Es2c.length+foo.length];
	System.arraycopy(Es2c, 0, bar, 0, Es2c.length);
	System.arraycopy(foo, 0, bar, Es2c.length, foo.length);
	Es2c=bar;
      }
      s2ccipher.init(JYpher.DECRYPT_MODE, Es2c, IVs2c);
      c=Class.forName(getConfig(guess[JYyExchange.PROPOSAL_MAC_ALGS_STOC]));
      s2cmac=(JYC)(c.newInstance());
      s2cmac.init(MACs2c);
      mac_buf=new byte[s2cmac.getBlockSize()];

      c=Class.forName(getConfig(guess[JYyExchange.PROPOSAL_ENC_ALGS_CTOS]));
      c2scipher=(JYpher)(c.newInstance());
      while(c2scipher.getBlockSize()>Ec2s.length){
        buf.reset();
        buf.putMPInt(K);
        buf.putByte(H);
        buf.putByte(Ec2s);
        hash.update(buf.buffer, 0, buf.index);
        byte[] foo=hash.digest();
        byte[] bar=new byte[Ec2s.length+foo.length];
	System.arraycopy(Ec2s, 0, bar, 0, Ec2s.length);
	System.arraycopy(foo, 0, bar, Ec2s.length, foo.length);
	Ec2s=bar;
      }
      c2scipher.init(JYpher.ENCRYPT_MODE, Ec2s, IVc2s);

      c=Class.forName(getConfig(guess[JYyExchange.PROPOSAL_MAC_ALGS_CTOS]));
      c2smac=(JYC)(c.newInstance());
      c2smac.init(MACc2s);

      if(!guess[JYyExchange.PROPOSAL_COMP_ALGS_CTOS].equals("none")){
	String foo=getConfig(guess[JYyExchange.PROPOSAL_COMP_ALGS_CTOS]);
	if(foo!=null){
	  try{
	    c=Class.forName(foo);
	    deflater=(JYmpression)(c.newInstance());
	    deflater.init(JYmpression.DEFLATER, 6);
	  }
	  catch(Exception ee){
	    System.err.println(foo+" isn't accessible.");
	  }
	}
      }
      else{
	if(deflater!=null){
	  deflater=null;
	}
      }
      if(!guess[JYyExchange.PROPOSAL_COMP_ALGS_STOC].equals("none")){
	String foo=getConfig(guess[JYyExchange.PROPOSAL_COMP_ALGS_STOC]);
	if(foo!=null){
	  try{
	    c=Class.forName(foo);
	    inflater=(JYmpression)(c.newInstance());
	    inflater.init(JYmpression.INFLATER, 0);
	  }
	  catch(Exception ee){
	    System.err.println(foo+" isn't accessible.");
	  }
	}
      }
      else{
	if(inflater!=null){
	  inflater=null;
	}
      }
    }
    catch(Exception e){ System.err.println("updatekeys: "+e); }
  }

  public /*synchronized*/ void write(JYcket packet, JYannel c, int length) throws Exception{
    while(true){
      if(c.rwsize>=length){
        c.rwsize-=length;
        break;
      }
      if(c.close || !isConnected()){
	throw new IOException("channel is broken");
      }
      if(c.rwsize>0){
	int len=c.rwsize;
	int s=packet.shift(len, (c2smac!=null ? c2smac.getBlockSize() : 0));
	byte command=packet.buffer.buffer[5];
	int recipient=c.getRecipient();
	length-=len;
	c.rwsize=0;
	write(packet);
	packet.unshift(command, recipient, s, length);
      }
      try{Thread.sleep(10);}
      catch(java.lang.InterruptedException e){};
    }
    write(packet);
  }
  public synchronized void write(JYcket packet) throws Exception{
     encode(packet);
     if(io!=null){
       io.put(packet);
       seqo++;
     }
  }

  Runnable thread;
  public void run(){
    thread=this;

    byte[] foo;
    JYffer buf=new JYffer();
    JYcket packet=new JYcket(buf);
    int i=0;
    JYannel channel;
    int[] start=new int[1];
    int[] length=new int[1];
    JYyExchange kex=null;

    try{
      while(isConnected &&
	    thread!=null){
        buf=read(buf);
	int msgType=buf.buffer[5]&0xff;
//      if(msgType!=94)
//System.out.println("read: 94 ? "+msgType);

	if(kex!=null && kex.getState()==msgType){
	  boolean result=kex.next(buf);
	  if(!result){
	    throw new JYchException("verify: "+result);
	  }
	  continue;
	}

        switch(msgType){
	case SSH_MSG_KEXINIT:
System.out.println("KEXINIT");
	  kex=receive_kexinit(buf);
	  break;

	case SSH_MSG_NEWKEYS:
System.out.println("NEWKEYS");
send_newkeys();
	  receive_newkeys(buf, kex);
	  kex=null;
	  break;

	case SSH_MSG_CHANNEL_DATA:
          buf.getInt(); 
          buf.getByte(); 
          buf.getByte(); 
          i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  foo=buf.getString(start, length);
	  if(channel==null){
	    break;
	  }
try{
	  channel.write(foo, start[0], length[0]);
}
catch(Exception e){
//System.out.println(e);
  try{channel.disconnect();}catch(Exception ee){}
break;
}
	  int len=length[0];
	  channel.setLocalWindowSize(channel.lwsize-len);
 	  if(channel.lwsize<channel.lwsize_max/2){
            packet.reset();
	    buf.putByte((byte)SSH_MSG_CHANNEL_WINDOW_ADJUST);
	    buf.putInt(channel.getRecipient());
	    buf.putInt(channel.lwsize_max-channel.lwsize);
	    write(packet);
	    channel.setLocalWindowSize(channel.lwsize_max);
	  }
	  break;

        case SSH_MSG_CHANNEL_EXTENDED_DATA:
          buf.getInt();
	  buf.getShort();
	  i=buf.getInt();
	  channel=JYannel.getChannel(i, this);
	  buf.getInt();                   // data_type_code == 1
	  foo=buf.getString(start, length);
	  //System.out.println("stderr: "+new String(foo,start[0],length[0]));
	  if(channel==null){
	    break;
	  }
	  //channel.write(foo, start[0], length[0]);
	  channel.write_ext(foo, start[0], length[0]);

	  len=length[0];
	  channel.setLocalWindowSize(channel.lwsize-len);
 	  if(channel.lwsize<channel.lwsize_max/2){
            packet.reset();
	    buf.putByte((byte)SSH_MSG_CHANNEL_WINDOW_ADJUST);
	    buf.putInt(channel.getRecipient());
	    buf.putInt(channel.lwsize_max-channel.lwsize);
	    write(packet);
	    channel.setLocalWindowSize(channel.lwsize_max);
	  }
	  break;

	case SSH_MSG_CHANNEL_WINDOW_ADJUST:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel==null){
	    break;
	  }
	  channel.addRemoteWindowSize(buf.getInt()); 
	  break;

	case SSH_MSG_CHANNEL_EOF:
          buf.getInt(); 
          buf.getShort(); 
          i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel!=null){
	    channel.eof_remote=true;
	    channel.eof();
	  }
	  /*
	  packet.reset();
	  buf.putByte((byte)SSH_MSG_CHANNEL_EOF);
	  buf.putInt(channel.getRecipient());
	  write(packet);
	  */
	  break;
	case SSH_MSG_CHANNEL_CLOSE:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel!=null){
//	      channel.close();
	    channel.disconnect();
	  }
	  /*
          if(JYannel.pool.size()==0){
	    thread=null;
	  }
	  */
	  break;
	case SSH_MSG_CHANNEL_OPEN_CONFIRMATION:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel==null){
	    //break;
	  }
	  channel.setRecipient(buf.getInt());
	  channel.setRemoteWindowSize(buf.getInt());
	  channel.setRemotePacketSize(buf.getInt());
	  break;
	case SSH_MSG_CHANNEL_OPEN_FAILURE:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel==null){
	    //break;
	  }
	  int reason_code=buf.getInt(); 
	  //foo=buf.getString();  // additional textual information
	  //foo=buf.getString();  // language tag 
	  channel.exitstatus=reason_code;
	  channel.close=true;
	  channel.eof_remote=true;
	  channel.setRecipient(0);
	  break;
	case SSH_MSG_CHANNEL_REQUEST:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  foo=buf.getString(); 
          boolean reply=(buf.getByte()!=0);
	  channel=JYannel.getChannel(i, this);
	  if(channel!=null){
	    byte reply_type=(byte)SSH_MSG_CHANNEL_FAILURE;
	    if((new String(foo)).equals("exit-status")){
	      i=buf.getInt();             // exit-status
	      channel.setExitStatus(i);
//	    System.out.println("exit-stauts: "+i);
//          channel.close();
	      reply_type=(byte)SSH_MSG_CHANNEL_SUCCESS;
	    }
	    if(reply){
	      packet.reset();
	      buf.putByte(reply_type);
	      buf.putInt(channel.getRecipient());
	      write(packet);
	    }
	  }
	  else{
	  }
	  break;
	case SSH_MSG_CHANNEL_OPEN:
          buf.getInt(); 
	  buf.getShort(); 
	  foo=buf.getString(); 
	  String ctyp=new String(foo);
	  //System.out.println("type="+ctyp);
          if(!"forwarded-tcpip".equals(ctyp) &&
	     !"x11".equals(ctyp)){
            System.out.println("Session.run: CHANNEL OPEN "+ctyp); 
	    throw new IOException("Session.run: CHANNEL OPEN "+ctyp);
	  }
	  else{
	    channel=JYannel.getChannel(ctyp);
	    addChannel(channel);
	    channel.getData(buf);
	    channel.init();

	    packet.reset();
	    buf.putByte((byte)SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
	    buf.putInt(channel.getRecipient());
 	    buf.putInt(channel.id);
	    buf.putInt(channel.lwsize);
	    buf.putInt(channel.lmpsize);
	    write(packet);
	    (new Thread(channel)).start();
	    break;
	  }
	case SSH_MSG_CHANNEL_SUCCESS:
          buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel==null){
	    break;
	  }
	  channel.reply=1;
	  break;
	case SSH_MSG_CHANNEL_FAILURE:
	  buf.getInt(); 
	  buf.getShort(); 
	  i=buf.getInt(); 
	  channel=JYannel.getChannel(i, this);
	  if(channel==null){
	    break;
	  }
	  channel.reply=0;
	  break;
	case SSH_MSG_GLOBAL_REQUEST:
	  buf.getInt(); 
	  buf.getShort(); 
	  foo=buf.getString();       // request name
	  reply=(buf.getByte()!=0);
	  if(reply){
	    packet.reset();
	    buf.putByte((byte)SSH_MSG_REQUEST_FAILURE);
	    write(packet);
	  }
	  break;
	case SSH_MSG_REQUEST_SUCCESS:
	case SSH_MSG_REQUEST_FAILURE:
	  break;
	default:
          System.out.println("Session.run: unsupported type "+msgType); 
	  throw new IOException("Unknown SSH message type "+msgType);
	}
      }
    }
    catch(Exception e){
      //System.out.println("# Session.run");
      //e.printStackTrace();
    }
    try{
      disconnect();
    }
    catch(NullPointerException e){
      //System.out.println("@1");
      //e.printStackTrace();
    }
    catch(Exception e){
      //System.out.println("@2");
      //e.printStackTrace();
    }
    isConnected=false;
  }

  public void finalize() throws Throwable{
    disconnect();
    jsch=null;
  }

  public void disconnect(){
    if(!isConnected) return;
    isConnected=false;
    //System.out.println(this+": disconnect");
    //Thread.dumpStack();
    /*
    for(int i=0; i<JYannel.pool.size(); i++){
      try{
        JYannel c=((JYannel)(JYannel.pool.elementAt(i)));
	if(c.session==this) c.eof();
      }
      catch(Exception e){
      }
    } 
    */

    JYannel.eof(this);

    JYrtWatcher.delPort(this);
    JYannelForwardedTCPIP.delPort(this);

    connectThread.interrupt();
    connectThread.yield();
    connectThread=null;

    thread=null;
    try{
      if(io!=null){
	if(io.in!=null) io.in.close();
	if(io.out!=null) io.out.close();
      }
      if(proxy==null){
        if(socket!=null)
	  socket.close();
      }
      else{
	synchronized(proxy){
	  proxy.close();	  
	}
	proxy=null;
      }
    }
    catch(Exception e){
//      e.printStackTrace();
    }
    io=null;
    socket=null;
    synchronized(jsch.pool){
      jsch.pool.removeElement(this);
    }
    System.gc();
  }

  public void setPortForwardingL(int lport, String host, int rport) throws JYchException{
    setPortForwardingL("127.0.0.1", lport, host,rport);
  }
  public void setPortForwardingL(String boundaddress, int lport, String host, int rport) throws JYchException{
    JYrtWatcher pw=JYrtWatcher.addPort(this, boundaddress, lport, host, rport);
    (new Thread(pw)).start();
  }
  public void delPortForwardingL(int lport) throws JYchException{
    JYrtWatcher.delPort(this, lport);
  }
  public String[] getPortForwardingL() throws JYchException{
    return JYrtWatcher.getPortForwarding(this);
  }

  public void setPortForwardingR(int rport, String host, int lport) throws JYchException{
    JYannelForwardedTCPIP.addPort(this, rport, host, lport);

    JYffer buf=new JYffer(100); // ??
    JYcket packet=new JYcket(buf);

    try{
      // byte SSH_MSG_GLOBAL_REQUEST 80
      // string "tcpip-forward"
      // boolean want_reply
      // string  address_to_bind
      // uint32  port number to bind
      packet.reset();
      buf.putByte((byte) SSH_MSG_GLOBAL_REQUEST);
      buf.putString("tcpip-forward".getBytes());
      buf.putByte((byte)0);
      buf.putString("0.0.0.0".getBytes());
      buf.putInt(rport);
      write(packet);
    }
    catch(Exception e){
      throw new JYchException(e.toString());
    }
  }
  public void delPortForwardingR(int rport) throws JYchException{
    JYannelForwardedTCPIP.delPort(this, rport);
  }

  void addChannel(JYannel channel){
    channel.session=this;
  }
  public String getConfig(String name){
    Object foo=null;
    if(config!=null){
      foo=config.get(name);
      if(foo instanceof String) return (String)foo;
    }
    foo=jsch.getConfig(name);
    if(foo instanceof String) return (String)foo;
    return null;
  }
//  public JYannel getChannel(){ return channel; }
  public void setProxy(JYoxy proxy){ this.proxy=proxy; }
  public void setHost(String host){ this.host=host; }
  public void setPort(int port){ this.port=port; }
  void setUserName(String foo){ this.username=foo; }
  public void setPassword(String foo){ this.password=foo; }
  public void setUserInfo(JYerInfo userinfo){ this.userinfo=userinfo; }
  public void setInputStream(InputStream in){ this.in=in; }
  public void setOutputStream(OutputStream out){ this.out=out; }
  public void setX11Host(String host){ JYannelX11.setHost(host); }
  public void setX11Port(int port){ JYannelX11.setPort(port); }
  public void setX11Cookie(String cookie){ JYannelX11.setCookie(cookie); }

  public void setConfig(java.util.Properties foo){
setConfig((java.util.Hashtable)foo);
  }
 
  public void setConfig(java.util.Hashtable foo){
    if(config==null) config=new java.util.Hashtable();
    for(java.util.Enumeration e=foo.keys() ; e.hasMoreElements() ;) {
      String key=(String)(e.nextElement());
      config.put(key, (String)(foo.get(key)));
    }
  }
  public void setSocketFactory(JYcketFactory foo){ socket_factory=foo;}
  public boolean isConnected(){ return isConnected; }
  public int getTimeout(){ return timeout; }
  public void setTimeout(int foo) throws JYchException {
    if(socket==null){
      if(foo<0){
        throw new JYchException("invalid timeout value");
      }
      this.timeout=foo;
      return;
    }
    try{
      socket.setSoTimeout(foo);
      timeout=foo;
    }
    catch(Exception e){
      throw new JYchException(e.toString());
    }
  }
  public String getServerVersion(){
    return new String(V_S);
  }
  public String getClientVersion(){
    return new String(V_C);
  }
  public void setClientVersion(String cv){
    V_C=cv.getBytes();
  }

  public void sendIgnore() throws Exception{
    JYffer buf=new JYffer();
    JYcket packet=new JYcket(buf);
    packet.reset();
    buf.putByte((byte)SSH_MSG_IGNORE);
    write(packet);
  }
  
  private JYstKey hostkey=null;
  public JYstKey getHostKey(){ return hostkey; }
}
