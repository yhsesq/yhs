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

class JYerAuthPassword extends JYerAuth{
  JYerInfo userinfo;
  JYerAuthPassword(JYerInfo userinfo){
   this.userinfo=userinfo;
  }

  public boolean start(JYssion session) throws Exception{
//    super.start(session);
//System.out.println("JYerAuthPassword: start");
    JYcket packet=session.packet;
    JYffer buf=session.buf;
    final String username=session.username;
    String password=session.password;
    String dest=username+"@"+session.host;
    if(session.port!=22){
      dest+=(":"+session.port);
    }

    while(true){
      if(password==null){
	if(userinfo==null){
	  //throw new JYchException("USERAUTH fail");
	  return false;
	}
	if(!userinfo.promptPassword("Password for "+dest)){
	  throw new JYchAuthCancelException("password");
	  //break;
	}
	password=userinfo.getPassword();
	if(password==null){
	  throw new JYchAuthCancelException("password");
	  //break;
	}
      }

      byte[] _username=null;
      try{ _username=username.getBytes("UTF-8"); }
      catch(java.io.UnsupportedEncodingException e){
	_username=username.getBytes();
      }

      byte[] _password=null;
      try{ _password=password.getBytes("UTF-8"); }
      catch(java.io.UnsupportedEncodingException e){
	_password=password.getBytes();
      }

      // send
      // byte      SSH_MSG_USERAUTH_REQUEST(50)
      // string    user name
      // string    service name ("ssh-connection")
      // string    "password"
      // boolen    FALSE
      // string    plaintext password (ISO-10646 UTF-8)
      packet.reset();
      buf.putByte((byte)JYssion.SSH_MSG_USERAUTH_REQUEST);
      buf.putString(_username);
      buf.putString("ssh-connection".getBytes());
      buf.putString("password".getBytes());
      buf.putByte((byte)0);
      buf.putString(_password);
      session.write(packet);

      loop:
      while(true){
	// receive
	// byte      SSH_MSG_USERAUTH_SUCCESS(52)
	// string    service name
	buf=session.read(buf);
	//System.out.println("read: 52 ? "+    buf.buffer[5]);
	if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_SUCCESS){
	  return true;
	}
	if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_BANNER){
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
	  continue loop;
	}
	if(buf.buffer[5]==JYssion.SSH_MSG_USERAUTH_FAILURE){
	  buf.getInt(); buf.getByte(); buf.getByte(); 
	  byte[] foo=buf.getString();
	  int partial_success=buf.getByte();
	  //System.out.println(new String(foo)+
	  //		 " partial_success:"+(partial_success!=0));
	  if(partial_success!=0){
	    throw new JYchPartialAuthException(new String(foo));
	  }
	  break;
	}
	else{
//        System.out.println("USERAUTH fail ("+buf.buffer[5]+")");
//	  throw new JYchException("USERAUTH fail ("+buf.buffer[5]+")");
	  return false;
	}
      }
      password=null;
    }
    //throw new JYchException("USERAUTH fail");
    //return false;
  }
}
