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
import java.util.Vector;

class JYerAuthPublicKey extends JYerAuth{
  JYerInfo userinfo;
  JYerAuthPublicKey(JYerInfo userinfo){
   this.userinfo=userinfo;
  }

  public boolean start(JYssion session) throws Exception{
    //super.start(session);

    //Vector identities=JYch.identities;
    Vector identities=session.jsch.identities;

    JYcket packet=session.packet;
    JYffer buf=session.buf;

    String passphrase=null;
    final String username=session.username;

    byte[] _username=null;
    try{ _username=username.getBytes("UTF-8"); }
    catch(java.io.UnsupportedEncodingException e){
      _username=username.getBytes();
    }

    for(int i=0; i<identities.size(); i++){
      JYentity identity=(JYentity)(identities.elementAt(i));
      byte[] pubkeyblob=identity.getPublicKeyBlob();

//System.out.println("JYerAuthPublicKey: "+identity+" "+pubkeyblob);

      if(pubkeyblob!=null){
	// send
	// byte      SSH_MSG_USERAUTH_REQUEST(50)
	// string    user name
	// string    service name ("ssh-connection")
	// string    "publickey"
	// boolen    FALSE
	// string    plaintext password (ISO-10646 UTF-8)
	packet.reset();
	buf.putByte((byte)JYssion.SSH_MSG_USERAUTH_REQUEST);
	buf.putString(_username);
	buf.putString("ssh-connection".getBytes());
	buf.putString("publickey".getBytes());
	buf.putByte((byte)0);
	buf.putString(identity.getAlgName().getBytes());
	buf.putString(pubkeyblob);
	session.write(packet);

	loop1:
	while(true){
	  // receive
	  // byte      SSH_MSG_USERAUTH_PK_OK(52)
	  // string    service name
	  buf=session.read(buf);
	  //System.out.println("read: 60 ? "+    buf.buffer[5]);
	  if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_PK_OK){
	    break;
	  }
	  else if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_FAILURE){
//	System.out.println("USERAUTH publickey "+session.getIdentity()+
//			   " is not acceptable.");
	    break;
	  }
	  else if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_BANNER){
	    buf.getInt(); buf.getByte(); buf.getByte();
	    byte[] _message=buf.getString();
	    byte[] lang=buf.getString();
	    String message=null;
	    try{ message=new String(_message, "UTF-8"); }
	    catch(java.io.UnsupportedEncodingException e){
	      message=new String(_message);
	    }
	    if(userinfo!=null){
	      userinfo.showMessage(message);
	    }
	    continue loop1;
	  }
	  else{
	    //System.out.println("USERAUTH fail ("+buf.buffer[5]+")");
	    //throw new JYchException("USERAUTH fail ("+buf.buffer[5]+")");
	    break;
	  }
	}
	if(buf.buffer[5]!=JYssion.SSH_MSG_USERAUTH_PK_OK){
	  continue;
	}
      }

//System.out.println("JYerAuthPublicKey: identity.isEncrypted()="+identity.isEncrypted());

      int count=5;
      while(true){
	if((identity.isEncrypted() && passphrase==null)){
	  if(userinfo==null) throw new JYchException("USERAUTH fail");
	  if(identity.isEncrypted() &&
	     !userinfo.promptPassphrase("Passphrase for "+identity.getName())){
	    throw new JYchAuthCancelException("publickey");
	    //throw new JYchException("USERAUTH cancel");
	    //break;
	  }
	  passphrase=userinfo.getPassphrase();
	}

	if(!identity.isEncrypted() || passphrase!=null){
//System.out.println("JYerAuthPublicKey: @1 "+passphrase);
	  if(identity.setPassphrase(passphrase))
	    break;
	}
	passphrase=null;
	count--;
	if(count==0)break;
      }

//System.out.println("JYerAuthPublicKey: identity.isEncrypted()="+identity.isEncrypted());

      if(identity.isEncrypted()) continue;
      if(pubkeyblob==null) pubkeyblob=identity.getPublicKeyBlob();

//System.out.println("JYerAuthPublicKey: pubkeyblob="+pubkeyblob);

      if(pubkeyblob==null) continue;

      // send
      // byte      SSH_MSG_USERAUTH_REQUEST(50)
      // string    user name
      // string    service name ("ssh-connection")
      // string    "publickey"
      // boolen    TRUE
      // string    plaintext password (ISO-10646 UTF-8)
      packet.reset();
      buf.putByte((byte)JYssion.SSH_MSG_USERAUTH_REQUEST);
      buf.putString(_username);
      buf.putString("ssh-connection".getBytes());
      buf.putString("publickey".getBytes());
      buf.putByte((byte)1);
      buf.putString(identity.getAlgName().getBytes());
      buf.putString(pubkeyblob);

//      byte[] tmp=new byte[buf.index-5];
//      System.arraycopy(buf.buffer, 5, tmp, 0, tmp.length);
//      buf.putString(signature);

      byte[] sid=session.getSessionId();
      int sidlen=sid.length;
      byte[] tmp=new byte[4+sidlen+buf.index-5];
      tmp[0]=(byte)(sidlen>>>24);
      tmp[1]=(byte)(sidlen>>>16);
      tmp[2]=(byte)(sidlen>>>8);
      tmp[3]=(byte)(sidlen);
      System.arraycopy(sid, 0, tmp, 4, sidlen);
      System.arraycopy(buf.buffer, 5, tmp, 4+sidlen, buf.index-5);

      byte[] signature=identity.getSignature(session, tmp);
      if(signature==null){  // for example, too long key length.
	break;
      }
      buf.putString(signature);

      session.write(packet);

      loop2:
      while(true){
	// receive
	// byte      SSH_MSG_USERAUTH_SUCCESS(52)
	// string    service name
	buf=session.read(buf);
	//System.out.println("read: 52 ? "+    buf.buffer[5]);
	if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_SUCCESS){
	  return true;
	}
	else if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_BANNER){
	  buf.getInt(); buf.getByte(); buf.getByte();
	  byte[] _message=buf.getString();
	  byte[] lang=buf.getString();
	  String message=null;
	  try{ message=new String(_message, "UTF-8"); }
	  catch(java.io.UnsupportedEncodingException e){
	    message=new String(_message);
	  }
	  if(userinfo!=null){
	    userinfo.showMessage(message);
	  }
	  continue loop2;
	}
	else if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_FAILURE){
	  buf.getInt(); buf.getByte(); buf.getByte(); 
	  byte[] foo=buf.getString();
	  int partial_success=buf.getByte();
	  //System.out.println(new String(foo)+
	  //                   " partial_success:"+(partial_success!=0));
	  if(partial_success!=0){
	    throw new JYchPartialAuthException(new String(foo));
	  }
	  break;
	}
	//System.out.println("USERAUTH fail ("+buf.buffer[5]+")");
	//throw new JYchException("USERAUTH fail ("+buf.buffer[5]+")");
	break;
      }
    }
    return false;
  }
}
