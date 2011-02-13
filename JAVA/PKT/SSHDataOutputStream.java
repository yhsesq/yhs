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
 * $Date: 2001/02/03 00:47:00 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.io.*;
import java.math.BigInteger;

public class SSHDataOutputStream extends DataOutputStream {

  SSHDataOutputStream(OutputStream out) {
    super(out);
  }

  public void writeBigInteger(BigInteger bi) throws IOException {
    short bytes = (short)((bi.bitLength() + 7) / 8);
    byte[] raw  = bi.toByteArray();
    writeShort(bi.bitLength());
    if(raw[0] == 0)
      write(raw, 1, bytes);
    else
      write(raw, 0, bytes);
  }

  public void writeString(String str) throws IOException {
    byte[] raw = str.getBytes();
    writeInt(raw.length);
    write(raw);
  }
}
