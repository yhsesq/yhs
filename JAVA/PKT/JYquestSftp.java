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

public class JYquestSftp implements JYquest{
  public void request(JYssion session, JYannel channel) throws Exception{
    JYffer buf=new JYffer();
    JYcket packet=new JYcket(buf);

    boolean reply=waitForReply();
    if(reply){
      channel.reply=-1;
    }

    packet.reset();
    buf.putByte((byte)JYssion.SSH_MSG_CHANNEL_REQUEST);
    buf.putInt(channel.getRecipient());
    buf.putString("subsystem".getBytes());
    buf.putByte((byte)(waitForReply() ? 1 : 0));
    buf.putString("sftp".getBytes());
    session.write(packet);

    if(reply){
      while(channel.reply==-1){
	try{Thread.sleep(10);}
	catch(Exception ee){
	}
      }
      if(channel.reply==0){
	throw new JYchException("failed to send sftp request");
      }
    }
  }
  public boolean waitForReply(){ return true; }
}
