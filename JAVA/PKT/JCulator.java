/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002 ymnk, JCraft,Inc.
 *  
 * Written by: 2002 ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package net.sf.pkt.ssh2;

import java.io.InputStream;
import java.io.IOException;

public abstract class JCulator{
  JCrm term=null;
  InputStream in=null;

  public JCulator(JCrm term, InputStream in){
    this.term=term;
    this.in=in;
  }

  public abstract void start();
  public abstract byte[] getCodeENTER();
  public abstract byte[] getCodeUP();
  public abstract byte[] getCodeDOWN();
  public abstract byte[] getCodeRIGHT();
  public abstract byte[] getCodeLEFT();
  public abstract byte[] getCodeF1();
  public abstract byte[] getCodeF2();
  public abstract byte[] getCodeF3();
  public abstract byte[] getCodeF4();
  public abstract byte[] getCodeF5();
  public abstract byte[] getCodeF6();
  public abstract byte[] getCodeF7();
  public abstract byte[] getCodeF8();
  public abstract byte[] getCodeF9();
  public abstract byte[] getCodeF10();

  public abstract void reset();

  byte[] buf=new byte[1024];
  int bufs=0;
  int buflen=0;
  byte getChar() throws java.io.IOException {
    if(buflen==0){
      fillBuf();
    }
    buflen--;

//System.out.println("getChar: "+new Character((char)buf[bufs])+"["+Integer.toHexString(buf[bufs]&0xff)+"]");

    return buf[bufs++];
  }
  void fillBuf() throws java.io.IOException {
    buflen=bufs=0;
    buflen=in.read(buf, bufs, buf.length-bufs);
    /*
System.out.println("fillBuf: ");
for(int i=0; i<buflen; i++){
byte b=buf[i];
System.out.print(new Character((char)b)+"["+Integer.toHexString(b&0xff)+"], ");
}
System.out.println("");
    */
    if(buflen<=0){
      buflen=0;
      throw new IOException("fillBuf");
    }
  }
  void pushChar(byte foo) throws java.io.IOException {
    buflen++;
    buf[--bufs]=foo;
  }
  int getASCII(int len) throws java.io.IOException {
//System.out.println("bufs="+bufs+", buflen="+buflen+", len="+len);
    if(buflen==0){
      fillBuf();
    }
    if(len>buflen)len=buflen;
    int foo=len;
    byte tmp;
    while(len>0){
      tmp=buf[bufs++];
      if(0x20<=tmp && tmp<=0x7f){
        buflen--;
        len--;
	continue;
      }
      bufs--;
      break;
    }
//System.out.println(" return "+(foo-len));
    return foo-len;
  }
}
