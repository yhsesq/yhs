/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: ymnk@jcraft.com $
 * $Date: 2000/12/18 17:13:50 $
 * $Name: rel0-0-0 $
 *****************************************************************************/
package mindbright.ssh;

import mindbright.ssh.*;
import net.sf.pkt.ssh2.*;

final class SSHCompression{
  static private ZStream deflate_stream=null;
  static private ZStream inflate_stream=null;

  static void init(int level){
    deflate_stream=new ZStream();
    inflate_stream=new ZStream();
    deflate_stream.deflateInit(level);
    inflate_stream.inflateInit();
  }

  static void uninit(){
    if(SSH.DEBUGMORE){
    if(deflate_stream!=null)
    System.out.println("compress outgoing: raw data "+deflate_stream.total_in+
		       ", compressed "+deflate_stream.total_out+
		       ", factor "+(deflate_stream.total_in == 0 ?
				    0.0 :
				    ((double)deflate_stream.total_out) /
				    ((double)deflate_stream.total_in)));
    if(inflate_stream!=null)
    System.out.println("compress incoming: raw data "+inflate_stream.total_out+
		       ", compressed "+inflate_stream.total_in+
		       ", factor "+(inflate_stream.total_out == 0 ?
				    0.0 :
				    ((double)inflate_stream.total_in) /
				    ((double)inflate_stream.total_out)));
    }
    if(deflate_stream!=null){
      deflate_stream.deflateEnd();
      deflate_stream.free();
      deflate_stream=null;
    }
    if(inflate_stream!=null){
      inflate_stream.inflateEnd();
      inflate_stream.free();
      inflate_stream=null;
    }
  }

  static private final int BUF_SIZE=4096;
  static private byte[] d_buf=new byte[BUF_SIZE];
  static private byte[] i_buf=new byte[BUF_SIZE];

  static int compress(byte[] buf, int len){

    deflate_stream.next_in=buf;
    deflate_stream.next_in_index=8;
    deflate_stream.avail_in=len-8;

    int status;
    int outputlen=8;

    do{
      deflate_stream.next_out=d_buf;
      deflate_stream.next_out_index=0;
      deflate_stream.avail_out=BUF_SIZE;
      status=deflate_stream.deflate(JZlib.Z_PARTIAL_FLUSH);
      switch(status){
      case JZlib.Z_OK:
	System.arraycopy(d_buf, 0, 
			 buf, outputlen,
			 BUF_SIZE-deflate_stream.avail_out);
	outputlen=(BUF_SIZE-deflate_stream.avail_out);
	break;
      default:
	System.err.println("SSHCompression.compress: deflate returnd "+status);
      }
    }
    while(deflate_stream.avail_out==0);
    return outputlen;
  }

  static private byte[] out_buf = new byte[BUF_SIZE];
  static void uncompress(SSHPduInputStream input){
    int pad=(8-(input.length%8));
    int out_end=0;

    inflate_stream.next_in=input.bytes;
    inflate_stream.next_in_index=pad;
    inflate_stream.avail_in=input.length - 4; // chop checksum field

    while(true){
      inflate_stream.next_out=i_buf;
      inflate_stream.next_out_index=0;
      inflate_stream.avail_out=BUF_SIZE;

      int status=inflate_stream.inflate(JZlib.Z_PARTIAL_FLUSH);
      switch(status){
      case JZlib.Z_OK:
        if(out_buf.length<out_end+BUF_SIZE-inflate_stream.avail_out){
	  byte[] foo=new byte[out_end+BUF_SIZE-inflate_stream.avail_out];
          System.arraycopy(out_buf, 0, foo, 0, out_end);
          out_buf=foo;
	}
	System.arraycopy(i_buf, 0, 
			 out_buf, out_end,
			 BUF_SIZE-inflate_stream.avail_out);
	out_end+=(BUF_SIZE-inflate_stream.avail_out);
	break;
      case JZlib.Z_BUF_ERROR:
	if(out_end>input.bytes.length){
	  byte[] foo=new byte[out_end];
          System.arraycopy(out_buf, 0, foo, 0, out_end);
          input.bytes=foo;
	}
	else{
          System.arraycopy(out_buf, 0, input.bytes, 0, out_end);
	}
	input.length=out_end;
	try{ input.reset(); }
	catch(Exception e){}
	return;
      default:
	System.err.println("SSHCompression.uncompress: inflate returnd "+status);
	return; // humm..
      }
    }
  }
}

