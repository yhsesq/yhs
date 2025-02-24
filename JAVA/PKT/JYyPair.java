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

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

public abstract class JYyPair{
  public static final int ERROR=0;
  public static final int DSA=1;
  public static final int RSA=2;
  public static final int UNKNOWN=3;

  static final int VENDOR_OPENSSH=0;
  static final int VENDOR_FSECURE=1;
  int vendor=VENDOR_OPENSSH;

  private static final byte[] cr="\n".getBytes();

  public static JYyPair genKeyPair(JYch jsch, int type) throws JYchException{
    return genKeyPair(jsch, type, 1024);
  }
  public static JYyPair genKeyPair(JYch jsch, int type, int key_size) throws JYchException{
    JYyPair kpair=null;
    if(type==DSA){ kpair=new JYyPairDSA(jsch); }
    else if(type==RSA){ kpair=new JYyPairRSA(jsch); }
    if(kpair!=null){
      kpair.generate(key_size);
    }
    return kpair;
  }

  abstract void generate(int key_size) throws JYchException;

  abstract byte[] getBegin();
  abstract byte[] getEnd();
  abstract int getKeySize();

  JYch jsch=null;
  private JYpher cipher;
  private JYSH hash;
  private JYndom random;

  private byte[] passphrase;

  public JYyPair(JYch jsch){
    this.jsch=jsch;
  }

  static byte[][] header={"Proc-Type: 4,ENCRYPTED".getBytes(),
			  "DEK-Info: DES-EDE3-CBC,".getBytes()};

  abstract byte[] getPrivateKey();

  public void writePrivateKey(java.io.OutputStream out){
    byte[] plain=getPrivateKey();
    byte[][] _iv=new byte[1][];
    byte[] encoded=encrypt(plain, _iv);
    byte[] iv=_iv[0];
    byte[] prv=JYil.toBase64(encoded, 0, encoded.length);

    try{
      out.write(getBegin()); out.write(cr);
      if(passphrase!=null){
	out.write(header[0]); out.write(cr);
	out.write(header[1]); 
	for(int i=0; i<iv.length; i++){
	  out.write(b2a((byte)((iv[i]>>>4)&0x0f)));
	  out.write(b2a((byte)(iv[i]&0x0f)));
	}
        out.write(cr);
	out.write(cr);
      }
      int i=0;
      while(i<prv.length){
	if(i+64<prv.length){
	  out.write(prv, i, 64);
	  out.write(cr);
	  i+=64;
	  continue;
	}
	out.write(prv, i, prv.length-i);
	out.write(cr);
	break;
      }
      out.write(getEnd()); out.write(cr);
      //out.close();
    }
    catch(Exception e){
    }
  }

  private static byte[] space=" ".getBytes();

  abstract byte[] getKeyTypeName();
  public abstract int getKeyType();

  public byte[] getPublicKeyBlob(){ return publickeyblob; }

  public void writePublicKey(java.io.OutputStream out, String comment){
    byte[] pubblob=getPublicKeyBlob();
    byte[] pub=JYil.toBase64(pubblob, 0, pubblob.length);
    try{
      out.write(getKeyTypeName()); out.write(space);
      out.write(pub, 0, pub.length); out.write(space);
      out.write(comment.getBytes());
      out.write(cr);
    }
    catch(Exception e){
    }
  }

  public void writePublicKey(String name, String comment) throws java.io.FileNotFoundException, java.io.IOException{
    FileOutputStream fos=new FileOutputStream(name);
    writePublicKey(fos, comment);
    fos.close();
  }

  public void writeSECSHPublicKey(java.io.OutputStream out, String comment){
    byte[] pubblob=getPublicKeyBlob();
    byte[] pub=JYil.toBase64(pubblob, 0, pubblob.length);
    try{
      out.write("---- BEGIN SSH2 PUBLIC KEY ----".getBytes()); out.write(cr);
      out.write(("Comment: \""+comment+"\"").getBytes()); out.write(cr);
      int index=0;
      while(index<pub.length){
	int len=70;
	if((pub.length-index)<len)len=pub.length-index;
	out.write(pub, index, len); out.write(cr);
	index+=len;
      }
      out.write("---- END SSH2 PUBLIC KEY ----".getBytes()); out.write(cr);
    }
    catch(Exception e){
    }
  }

  public void writeSECSHPublicKey(String name, String comment) throws java.io.FileNotFoundException, java.io.IOException{
    FileOutputStream fos=new FileOutputStream(name);
    writeSECSHPublicKey(fos, comment);
    fos.close();
  }


  public void writePrivateKey(String name) throws java.io.FileNotFoundException, java.io.IOException{
    FileOutputStream fos=new FileOutputStream(name);
    writePrivateKey(fos);
    fos.close();
  }

  public String getFingerPrint(){
    if(hash==null) hash=genHash();
    byte[] kblob=getPublicKeyBlob();
    if(kblob==null) return null;
    return getKeySize()+" "+JYil.getFingerPrint(hash, kblob);
  }

  private byte[] encrypt(byte[] plain, byte[][] _iv){
    if(passphrase==null) return plain;

    if(cipher==null) cipher=genCipher();
    byte[] iv=_iv[0]=new byte[cipher.getIVSize()];

    if(random==null) random=genRandom();
    random.fill(iv, 0, iv.length);

    byte[] key=genKey(passphrase, iv);
    byte[] encoded=plain;
    int bsize=cipher.getBlockSize();
    if(encoded.length%bsize!=0){
      byte[] foo=new byte[(encoded.length/bsize+1)*bsize];
      System.arraycopy(encoded, 0, foo, 0, encoded.length);
      encoded=foo;
    }

    try{
      cipher.init(JYpher.ENCRYPT_MODE, key, iv);
      cipher.update(encoded, 0, encoded.length, encoded, 0);
    }
    catch(Exception e){
      //System.out.println(e);
    }
    return encoded;
  }

  abstract boolean parse(byte[] data);

  private byte[] decrypt(byte[] data, byte[] passphrase, byte[] iv){
    /*
    if(iv==null){  // FSecure
      iv=new byte[8];
      for(int i=0; i<iv.length; i++)iv[i]=0;
    }
    */
    try{
      byte[] key=genKey(passphrase, iv);
      cipher.init(JYpher.DECRYPT_MODE, key, iv);
      byte[] plain=new byte[data.length];
      cipher.update(data, 0, data.length, plain, 0);
      return plain;
    }
    catch(Exception e){
      //System.out.println(e);
    }
    return null;
  }

  int writeSEQUENCE(byte[] buf, int index, int len){
    buf[index++]=0x30;
    index=writeLength(buf, index, len);
    return index;
  }
  int writeINTEGER(byte[] buf, int index, byte[] data){
    buf[index++]=0x02;
    index=writeLength(buf, index, data.length);
    System.arraycopy(data, 0, buf, index, data.length);
    index+=data.length;
    return index;
  }

  int countLength(int len){
    int i=1;
    if(len<=0x7f) return i;
    while(len>0){
      len>>>=8;
      i++;
    }
    return i;
  }

  int writeLength(byte[] data, int index, int len){
    int i=countLength(len)-1;
    if(i==0){
      data[index++]=(byte)len;
      return index;
    }
    data[index++]=(byte)(0x80|i);
    int j=index+i;
    while(i>0){
      data[index+i-1]=(byte)(len&0xff);
      len>>>=8;
      i--;
    }
    return j;
  }

  private JYndom genRandom(){
    if(random==null){
      try{
	Class c=Class.forName(jsch.getConfig("random"));
        random=(JYndom)(c.newInstance());
      }
      catch(Exception e){ System.err.println("connect: random "+e); }
    }
    return random;
  }

  private JYSH genHash(){
    try{
      Class c=Class.forName(jsch.getConfig("md5"));
      hash=(JYSH)(c.newInstance());
      hash.init();
    }
    catch(Exception e){
    }
    return hash;
  }
  private JYpher genCipher(){
    try{
      Class c;
      c=Class.forName(jsch.getConfig("3des-cbc"));
      cipher=(JYpher)(c.newInstance());
    }
    catch(Exception e){
    }
    return cipher;
  }

  /*
    hash is MD5
    h(0) <- hash(passphrase, iv);
    h(n) <- hash(h(n-1), passphrase, iv);
    key <- (h(0),...,h(n))[0,..,key.length];
  */
  synchronized byte[] genKey(byte[] passphrase, byte[] iv){
    if(cipher==null) cipher=genCipher();
    if(hash==null) hash=genHash();

    byte[] key=new byte[cipher.getBlockSize()];
    int hsize=hash.getBlockSize();
    byte[] hn=new byte[key.length/hsize*hsize+
		       (key.length%hsize==0?0:hsize)];
    try{
      byte[] tmp=null;
      if(vendor==VENDOR_OPENSSH){
	for(int index=0; index+hsize<=hn.length;){
	  if(tmp!=null){ hash.update(tmp, 0, tmp.length); }
	  hash.update(passphrase, 0, passphrase.length);
	  hash.update(iv, 0, iv.length);
	  tmp=hash.digest();
	  System.arraycopy(tmp, 0, hn, index, tmp.length);
	  index+=tmp.length;
	}
	System.arraycopy(hn, 0, key, 0, key.length); 
      }
      else if(vendor==VENDOR_FSECURE){
	for(int index=0; index+hsize<=hn.length;){
	  if(tmp!=null){ hash.update(tmp, 0, tmp.length); }
	  hash.update(passphrase, 0, passphrase.length);
	  tmp=hash.digest();
	  System.arraycopy(tmp, 0, hn, index, tmp.length);
	  index+=tmp.length;
	}
	System.arraycopy(hn, 0, key, 0, key.length); 
      }
    }
    catch(Exception e){
      System.out.println(e);
    }
    return key;
  } 

  public void setPassphrase(String passphrase){
    if(passphrase==null || passphrase.length()==0){
      setPassphrase((byte[])null);
    }
    else{
      setPassphrase(passphrase.getBytes());
    }
  }
  public void setPassphrase(byte[] passphrase){
    if(passphrase!=null && passphrase.length==0) 
      passphrase=null;
    this.passphrase=passphrase;
  }

  private boolean encrypted=false;
  private byte[] data=null;
  private byte[] iv=null;
  private byte[] publickeyblob=null;

  public boolean isEncrypted(){ return encrypted; }
  public boolean decrypt(String _passphrase){
    byte[] passphrase=_passphrase.getBytes();
    byte[] foo=decrypt(data, passphrase, iv);
    if(parse(foo)){
      encrypted=false;
    }
    return !encrypted;
  }

  public static JYyPair load(JYch jsch, String prvkey) throws JYchException{
    String pubkey=prvkey+".pub";
    if(!new File(pubkey).exists()){
      pubkey=null;
    }
    return load(jsch, prvkey, pubkey);
  }
  public static JYyPair load(JYch jsch, String prvkey, String pubkey) throws JYchException{

    byte[] iv=new byte[8];       // 8
    boolean encrypted=true;
    byte[] data=null;

    byte[] publickeyblob=null;

    int type=ERROR;
    int vendor=VENDOR_OPENSSH;

    try{
      File file=new File(prvkey);
      FileInputStream fis=new FileInputStream(prvkey);
      byte[] buf=new byte[(int)(file.length())];
      int len=fis.read(buf, 0, buf.length);
      fis.close();

      int i=0;

      while(i<len){
        if(buf[i]=='B'&& buf[i+1]=='E'&& buf[i+2]=='G'&& buf[i+3]=='I'){
          i+=6;	    
          if(buf[i]=='D'&& buf[i+1]=='S'&& buf[i+2]=='A'){ type=DSA; }
	  else if(buf[i]=='R'&& buf[i+1]=='S'&& buf[i+2]=='A'){ type=RSA; }
	  else if(buf[i]=='S'&& buf[i+1]=='S'&& buf[i+2]=='H'){ // FSecure
	    type=UNKNOWN;
	    vendor=VENDOR_FSECURE;
	  }
	  else{
            //System.out.println("invalid format: "+identity);
	    throw new JYchException("invaid privatekey: "+prvkey);
	  }
          i+=3;
	  continue;
	}
        if(buf[i]=='C'&& buf[i+1]=='B'&& buf[i+2]=='C'&& buf[i+3]==','){
          i+=4;
	  for(int ii=0; ii<iv.length; ii++){
            iv[ii]=(byte)(((a2b(buf[i++])<<4)&0xf0)+(a2b(buf[i++])&0xf));
  	  }
	  continue;
	}
	if(buf[i]==0x0d &&
	   i+1<buf.length && buf[i+1]==0x0a){
	  i++;
	  continue;
	}
	if(buf[i]==0x0a && i+1<buf.length){
	  if(buf[i+1]==0x0a){ i+=2; break; }
	  if(buf[i+1]==0x0d &&
	     i+2<buf.length && buf[i+2]==0x0a){
	     i+=3; break;
	  }
	  boolean inheader=false;
	  for(int j=i+1; j<buf.length; j++){
	    if(buf[j]==0x0a) break;
	    //if(buf[j]==0x0d) break;
	    if(buf[j]==':'){inheader=true; break;}
	  }
	  if(!inheader){
	    i++; 
	    encrypted=false;    // no passphrase
	    break;
	  }
	}
	i++;
      }

      if(type==ERROR){
	throw new JYchException("invaid privatekey: "+prvkey);
      }

      int start=i;
      while(i<len){
        if(buf[i]==0x0a){
	  boolean xd=(buf[i-1]==0x0d);
          System.arraycopy(buf, i+1, 
			   buf, 
			   i-(xd ? 1 : 0), 
			   len-i-1-(xd ? 1 : 0)
			   );
	  if(xd)len--;
          len--;
          continue;
        }
        if(buf[i]=='-'){  break; }
        i++;
      }
      data=JYil.fromBase64(buf, start, i-start);

      if(data.length>4 &&            // FSecure
	 data[0]==(byte)0x3f &&
	 data[1]==(byte)0x6f &&
	 data[2]==(byte)0xf9 &&
	 data[3]==(byte)0xeb){

	JYffer _buf=new JYffer(data);
	_buf.getInt();  // 0x3f6ff9be
	_buf.getInt();
	byte[]_type=_buf.getString();
	//System.out.println("type: "+new String(_type)); 
	byte[] _cipher=_buf.getString();
	String cipher=new String(_cipher);
	//System.out.println("cipher: "+cipher); 
	if(cipher.equals("3des-cbc")){
  	   _buf.getInt();
	   byte[] foo=new byte[data.length-_buf.getOffSet()];
	   _buf.getByte(foo);
	   data=foo;
	   encrypted=true;
	   throw new JYchException("unknown privatekey format: "+prvkey);
	}
	else if(cipher.equals("none")){
  	   _buf.getInt();
  	   _buf.getInt();

           encrypted=false;

	   byte[] foo=new byte[data.length-_buf.getOffSet()];
	   _buf.getByte(foo);
	   data=foo;
	}
      }

      if(pubkey!=null){
	try{
	  file=new File(pubkey);
	  fis=new FileInputStream(pubkey);
	  buf=new byte[(int)(file.length())];
	  len=fis.read(buf, 0, buf.length);
	  fis.close();

	  if(buf.length>4 &&             // FSecure's public key
	     buf[0]=='-' && buf[1]=='-' && buf[2]=='-' && buf[3]=='-'){

	    boolean valid=true;
	    i=0;
	    do{i++;}while(buf.length>i && buf[i]!=0x0a);
	    if(buf.length<=i) {valid=false;}

	    while(valid){
	      if(buf[i]==0x0a){
		boolean inheader=false;
		for(int j=i+1; j<buf.length; j++){
		  if(buf[j]==0x0a) break;
		  if(buf[j]==':'){inheader=true; break;}
		}
		if(!inheader){
		  i++; 
		  break;
		}
	      }
	      i++;
	    }
	    if(buf.length<=i){valid=false;}

	    start=i;
	    while(valid && i<len){
	      if(buf[i]==0x0a){
		System.arraycopy(buf, i+1, buf, i, len-i-1);
		len--;
		continue;
	      }
	      if(buf[i]=='-'){  break; }
	      i++;
	    }
	    if(valid){
	      publickeyblob=JYil.fromBase64(buf, start, i-start);
	      if(type==UNKNOWN){
		if(publickeyblob[8]=='d'){ type=DSA; }
		else if(publickeyblob[8]=='r'){ type=RSA; }
	      }
	    }
	  }
	  else{
	    if(buf[0]=='s'&& buf[1]=='s'&& buf[2]=='h' && buf[3]=='-'){
	      i=0;
	      while(i<len){ if(buf[i]==' ')break; i++;} i++;
	      if(i<len){
		start=i;
		while(i<len){ if(buf[i]==' ')break; i++;}
		publickeyblob=JYil.fromBase64(buf, start, i-start);
	      }
	    }
	  }
	}
	catch(Exception ee){
	}
      }
    }
    catch(Exception e){
      if(e instanceof JYchException) throw (JYchException)e;
      throw new JYchException(e.toString());
    }

    JYyPair kpair=null;
    if(type==DSA){ kpair=new JYyPairDSA(jsch); }
    else if(type==RSA){ kpair=new JYyPairRSA(jsch); }

    if(kpair!=null){
      kpair.encrypted=encrypted;
      kpair.publickeyblob=publickeyblob;
      kpair.vendor=vendor;

      if(encrypted){
	kpair.iv=iv;
	kpair.data=data;
      }
      else{
	if(kpair.parse(data)){
	  return kpair;
	}
	else{
	  throw new JYchException("invaid privatekey: "+prvkey);
	}
      }
    }

    return kpair;
  }

  static private byte a2b(byte c){
    if('0'<=c&&c<='9') return (byte)(c-'0');
    return (byte)(c-'a'+10);
  }
  static private byte b2a(byte c){
    if(0<=c&&c<=9) return (byte)(c+'0');
    return (byte)(c-10+'A');
  }

  public void dispose(){
    passphrase=null;
  }
}
