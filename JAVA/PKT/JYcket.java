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

public class JYcket{

  private static JYndom random=null;
  static void setRandom(JYndom foo){ random=foo;}

  JYffer buffer;
  byte[] tmp=new byte[4]; 
  public JYcket(JYffer buffer){
    this.buffer=buffer;
  }
  public void reset(){
    buffer.index=5;
  }
  void padding(){
    int len=buffer.index;
    int pad=(-len)&7;
    if(pad<8){
      pad+=8;
    }
    len=len+pad-4;
    tmp[0]=(byte)(len>>>24);
    tmp[1]=(byte)(len>>>16);
    tmp[2]=(byte)(len>>>8);
    tmp[3]=(byte)(len);
    System.arraycopy(tmp, 0, buffer.buffer, 0, 4);
    buffer.buffer[4]=(byte)pad;
    synchronized(random){
      random.fill(buffer.buffer, buffer.index, pad);
    }
    buffer.skip(pad);
    //buffer.putPad(pad);
/*
for(int i=0; i<buffer.index; i++){
  System.out.print(Integer.toHexString(buffer.buffer[i]&0xff)+":");
}
System.out.println("");
*/
  }

  int shift(int len, int mac){
    int s=len+5+9;
    int pad=(-s)&7;
    if(pad<8)pad+=8;
    s+=pad;
    s+=mac;

    System.arraycopy(buffer.buffer, 
		     len+5+9, 
		     buffer.buffer, s, buffer.index-5-9-len);
    buffer.index=10;
    buffer.putInt(len);
    buffer.index=len+5+9;
    return s;
  }
  void unshift(byte command, int recipient, int s, int len){
    System.arraycopy(buffer.buffer, 
		     s, 
		     buffer.buffer, 5+9, len);
    buffer.buffer[5]=command;
    buffer.index=6;
    buffer.putInt(recipient);
    buffer.putInt(len);
    buffer.index=len+5+9;
  }

}
