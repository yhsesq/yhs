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

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class JX implements net.sf.pkt.ssh2.JYDH{
  BigInteger p;
  BigInteger g;
  BigInteger e;  // my public key
  byte[] e_array;
  BigInteger f;  // your public key
  BigInteger K;  // shared secret key
  byte[] K_array;

  private KeyPairGenerator myKpairGen;
  private KeyAgreement myKeyAgree;
  public void init() throws Exception{
    myKpairGen=KeyPairGenerator.getInstance("DH");
    myKeyAgree=KeyAgreement.getInstance("DH");
  }
  public byte[] getE() throws Exception{
    if(e==null){
      DHParameterSpec dhSkipParamSpec=new DHParameterSpec(p, g);
      myKpairGen.initialize(dhSkipParamSpec);
      KeyPair myKpair=myKpairGen.generateKeyPair();
      myKeyAgree.init(myKpair.getPrivate());
//    BigInteger x=((javax.crypto.interfaces.DHPrivateKey)(myKpair.getPrivate())).getX();
      byte[] myPubKeyEnc=myKpair.getPublic().getEncoded();
      e=((javax.crypto.interfaces.DHPublicKey)(myKpair.getPublic())).getY();
      e_array=e.toByteArray();
    }
    return e_array;
  }
  public byte[] getK() throws Exception{
    if(K==null){
      KeyFactory myKeyFac=KeyFactory.getInstance("DH");
      DHPublicKeySpec keySpec=new DHPublicKeySpec(f, p, g);
      PublicKey yourPubKey=myKeyFac.generatePublic(keySpec);
      myKeyAgree.doPhase(yourPubKey, true);
      byte[] mySharedSecret=myKeyAgree.generateSecret();
      K=new BigInteger(mySharedSecret);
      K_array=K.toByteArray();

//System.out.println("K.signum(): "+K.signum()+
//		   " "+Integer.toHexString(mySharedSecret[0]&0xff)+
//		   " "+Integer.toHexString(K_array[0]&0xff));

      K_array=mySharedSecret;
    }
    return K_array;
  }
  public void setP(byte[] p){ setP(new BigInteger(p)); }
  public void setG(byte[] g){ setG(new BigInteger(g)); }
  public void setF(byte[] f){ setF(new BigInteger(f)); }
  void setP(BigInteger p){this.p=p;}
  void setG(BigInteger g){this.g=g;}
  void setF(BigInteger f){this.f=f;}
}
