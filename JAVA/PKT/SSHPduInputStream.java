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
 * $Author: josh $
 * $Date: 2001/02/03 00:47:01 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.io.*;
import java.math.BigInteger;

import mindbright.security.*;
import mindbright.util.*;

public final class SSHPduInputStream extends SSHDataInputStream implements SSHPdu {

  static final class PduByteArrayInputStream extends ByteArrayInputStream {
    PduByteArrayInputStream(byte[] data) {
      super(data);
    }
    public int getPos() {
      return pos;
    }
    public void setPos(int pos) {
      this.pos = pos;
    }
    public byte[] getBuf() {
      return buf;
    }
    public void setBuf(byte[] buf) {
      this.buf = buf;
    }
  }

  public int type;
  public int length;

  byte[] bytes;
  Cipher cipher;

  SSHPduInputStream(int type, Cipher cipher) {
    super(null);
    this.type   = type; // This is the expected type (checked in readFrom())
    this.cipher = cipher;
  }

  boolean validChecksum() throws IOException {
    int padLen = ((length + 8) & ~7);
    int stored, calculated;

    skip(padLen - 4);
    stored = readInt();
    reset();

    calculated = (int)CRC32.getValue(bytes, 0, padLen - 4);

    if(calculated != stored)
      return false;

    return true;
  }

  public SSHPdu createPdu() {
    return new SSHPduInputStream(this.type, this.cipher);
  }

  public void readFrom(InputStream in) throws IOException {
    SSHDataInputStream dIn    = new SSHDataInputStream(in);
    int                len    = dIn.readInt();
    int                padLen = ((len + 8) & ~7);
    int                type;
    byte[]             data;

    if(padLen > 256000)
      throw new IOException("Corrupt incoming packet, too large");

    data = new byte[padLen];

    dIn.readFully(data);
    if(cipher != null)
      cipher.decrypt(data, 0, data, 0, padLen);

    this.in     = new PduByteArrayInputStream(data);
    this.bytes  = data;
    this.length = len;

    if(!this.validChecksum())
      throw new IOException("Invalid checksum in packet");

    this.skip(8 - (len % 8));
if(SSH.compressionLevel!=0){
      //System.out.print("readFrom: length="+length);
      byte[] foo=bytes;
      SSHCompression.uncompress(this);
      if(foo!=bytes){
        this.in = new PduByteArrayInputStream(bytes);
      }
      //System.out.println(" -> length="+length);
    }

    type = (int)this.readByte();

    if(type == SSH.MSG_DEBUG) {
      SSH.logDebug("MSG_DEBUG: " + this.readString());
      this.readFrom(in);
    } else if(type == SSH.MSG_IGNORE) {
      SSH.logIgnore(this);
      this.readFrom(in);
    } else {
      if((this.type != SSH.MSG_ANY) && (this.type != type)) {
	if(type == SSH.MSG_DISCONNECT)
	  throw new IOException("Server disconnected: " + this.readString());
	else
	  throw new IOException("Invalid type: " + type + " (expected: " +
				this.type + ")");
      }
      this.type = type;
    }

  }

  public void writeTo(OutputStream sshOut) throws IOException {
    int off, n;

    if(type != SSH.MSG_CHANNEL_DATA &&
       type != SSH.SMSG_STDOUT_DATA &&
       type != SSH.SMSG_STDERR_DATA)
      throw new IOException("Trying to write raw data from non-data PDU");

    // Here we assume that the content left is readable through readString
    // which is the case if this is SSH-data
    //
    int len = readInt();

    PduByteArrayInputStream is = (PduByteArrayInputStream)in;

    sshOut.write(bytes, is.getPos(), len);

    sshOut.flush();
  }

  public byte[] rawData() {
    return bytes;
  }
  public void rawSetData(byte[] raw) {
    PduByteArrayInputStream is = (PduByteArrayInputStream)in;
    bytes = new byte[raw.length + 4];
    is.setPos(0);
    int len = raw.length;
    int off = 0;
    bytes[off++] = (byte)((len >>> 24) & 0xff);
    bytes[off++] = (byte)((len >>> 16) & 0xff);
    bytes[off++] = (byte)((len >>> 8)  & 0xff);
    bytes[off++] = (byte)(len & 0xff);
    System.arraycopy(raw, 0, bytes, off, raw.length);
    is.setBuf(bytes);
  }
  public int rawOffset() {
    PduByteArrayInputStream is = (PduByteArrayInputStream)in;
    return is.getPos() + 4; // The first four bytes is the length of the data
  }
  public int rawSize() {
    PduByteArrayInputStream is = (PduByteArrayInputStream)in;
    int off = is.getPos();
    int ch1 = ((bytes[off++] + 256) & 0xff);
    int ch2 = ((bytes[off++] + 256) & 0xff);
    int ch3 = ((bytes[off++] + 256) & 0xff);
    int ch4 = ((bytes[off]   + 256) & 0xff);
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  }
  // !!! Only allowed to shrink for now !!!
  public void rawAdjustSize(int size) {
    PduByteArrayInputStream is = (PduByteArrayInputStream)in;
    int oldSz = rawSize();
    if(size >= oldSz)
      return;
    int pos = is.getPos() + (oldSz - size);
    is.setPos(pos);
    bytes[pos++] = (byte)((size >>> 24) & 0xff);
    bytes[pos++] = (byte)((size >>> 16) & 0xff);
    bytes[pos++] = (byte)((size >>> 8)  & 0xff);
    bytes[pos++] = (byte) (size & 0xff);
  }

}
