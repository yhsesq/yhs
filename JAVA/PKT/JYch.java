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

import java.io.InputStream;
public class JYch{
  static java.util.Hashtable config=new java.util.Hashtable();
  static{
//  config.put("kex", "diffie-hellman-group-exchange-sha1");
    config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group-exchange-sha1");
    config.put("server_host_key", "ssh-rsa,ssh-dss");
    //config.put("server_host_key", "ssh-dss,ssh-rsa");

    config.put("cipher.s2c", "3des-cbc,blowfish-cbc");
    config.put("cipher.c2s", "3des-cbc,blowfish-cbc");

    config.put("mac.s2c", "hmac-md5,hmac-sha1,hmac-sha1-96,hmac-md5-96");
    config.put("mac.c2s", "hmac-md5,hmac-sha1,hmac-sha1-96,hmac-md5-96");
    config.put("compression.s2c", "none");
    config.put("compression.c2s", "none");
    config.put("lang.s2c", "");
    config.put("lang.c2s", "");

    config.put("diffie-hellman-group-exchange-sha1", 
                                "net.sf.pkt.ssh2.JYGEX");
    config.put("diffie-hellman-group1-sha1", 
	                        "net.sf.pkt.ssh2.JYG1");

    config.put("dh",            "net.sf.pkt.ssh2.JX");
    config.put("3des-cbc",      "net.sf.pkt.ssh2.JXipleDESCBC");
    config.put("blowfish-cbc",  "net.sf.pkt.ssh2.JXowfishCBC");
    config.put("hmac-sha1",     "net.sf.pkt.ssh2.JXACSHA1");
    config.put("hmac-sha1-96",  "net.sf.pkt.ssh2.JXACSHA196");
    config.put("hmac-md5",      "net.sf.pkt.ssh2.JXACMD5");
    config.put("hmac-md5-96",   "net.sf.pkt.ssh2.JXACMD596");
    config.put("sha-1",         "net.sf.pkt.ssh2.JXA1");
    config.put("md5",           "net.sf.pkt.ssh2.X5");
    config.put("signature.dss", "net.sf.pkt.ssh2.JXgnatureDSA");
    config.put("signature.rsa", "net.sf.pkt.ssh2.JXgnatureRSA");
    config.put("keypairgen.dsa",   "net.sf.pkt.ssh2.JXyPairGenDSA");
    config.put("keypairgen.rsa",   "net.sf.pkt.ssh2.JXyPairGenRSA");
    config.put("random",        "net.sf.pkt.ssh2.JXndom");

    config.put("aes128-cbc",    "net.sf.pkt.ssh2.JXS128CBC");
//  config.put("cipher.s2c", "aes128-cbc,3des-cbc,blowfish-cbc");
//  config.put("cipher.c2s", "aes128-cbc,3des-cbc,blowfish-cbc");

    config.put("zlib",          "net.sf.pkt.ssh2.JXmpression");

    config.put("StrictHostKeyChecking",  "ask");
  }
  java.util.Vector pool=new java.util.Vector();
  java.util.Vector identities=new java.util.Vector();
  //private KnownHosts known_hosts=null;
  private JYstKeyRepository known_hosts=null;

  public JYch(){
    //known_hosts=new KnownHosts(this);
  }

  public JYssion getSession(String username, String host) throws JYchException { return getSession(username, host, 22); }
  public JYssion getSession(String username, String host, int port) throws JYchException {
    JYssion s=new JYssion(this); 
    s.setUserName(username);
    s.setHost(host);
    s.setPort(port);
    pool.addElement(s);
    return s;
  }
  public void setHostKeyRepository(JYstKeyRepository foo){
    known_hosts=foo;
  }
  public void setKnownHosts(String foo) throws JYchException{
    if(known_hosts==null) known_hosts=new JYownHosts(this);
    if(known_hosts instanceof JYownHosts){
      synchronized(known_hosts){
	((JYownHosts)known_hosts).setKnownHosts(foo); 
      }
    }
  }
  public void setKnownHosts(InputStream foo) throws JYchException{ 
    if(known_hosts==null) known_hosts=new JYownHosts(this);
    if(known_hosts instanceof JYownHosts){
      synchronized(known_hosts){
	((JYownHosts)known_hosts).setKnownHosts(foo); 
      }
    }
  }
  /*
  HostKeyRepository getKnownHosts(){ 
    if(known_hosts==null) known_hosts=new KnownHosts(this);
    return known_hosts; 
  }
  */
  public JYstKeyRepository getHostKeyRepository(){ 
    if(known_hosts==null) known_hosts=new JYownHosts(this);
    return known_hosts; 
  }
  /*
  public HostKey[] getHostKey(){
    if(known_hosts==null) return null;
    return known_hosts.getHostKey(); 
  }
  public void removeHostKey(String foo, String type){
    removeHostKey(foo, type, null);
  }
  public void removeHostKey(String foo, String type, byte[] key){
    if(known_hosts==null) return;
    known_hosts.remove(foo, type, key); 
  }
  */
  public void addIdentity(String foo) throws JYchException{
    addIdentity(foo, (String)null);
  }
  public void addIdentity(String foo, String bar) throws JYchException{
    JYentity identity=new JYentityFile(foo, this);
    if(bar!=null) identity.setPassphrase(bar);
    identities.addElement(identity);
  }
  String getConfig(String foo){ return (String)(config.get(foo)); }

  private java.util.Vector proxies;
  void setProxy(String hosts, JYoxy proxy){
    java.lang.String[] patterns=JYil.split(hosts, ",");
    if(proxies==null){proxies=new java.util.Vector();}
    synchronized(proxies){
      for(int i=0; i<patterns.length; i++){
	if(proxy==null){
	  proxies.insertElementAt(null, 0);
	  proxies.insertElementAt(patterns[i].getBytes(), 0);
	}
	else{
	  proxies.addElement(patterns[i].getBytes());
	  proxies.addElement(proxy);
	}
      }
    }
  }
  JYoxy getProxy(String host){
    if(proxies==null)return null;
    byte[] _host=host.getBytes();
    synchronized(proxies){
      for(int i=0; i<proxies.size(); i+=2){
	if(JYil.glob(((byte[])proxies.elementAt(i)), _host)){
	  return (JYoxy)(proxies.elementAt(i+1));
	}
      }
    }
    return null;
  }
  void removeProxy(){
    proxies=null;
  }

  public static void setConfig(java.util.Hashtable foo){
    synchronized(config){
      for(java.util.Enumeration e=foo.keys() ; e.hasMoreElements() ;) {
	String key=(String)(e.nextElement());
	config.put(key, (String)(foo.get(key)));
      }
    }
  }
}
