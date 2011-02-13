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

public class SSHDataInputStream extends DataInputStream {

  SSHDataInputStream(InputStream in) {
    super(in);
  }

  public BigInteger readBigInteger() throws IOException {
    short  bits = readShort();
    byte[] raw  = new byte[(bits + 7) / 8 + 1];

    raw[0] = 0;
    read(raw, 1, raw.length - 1);

    return new BigInteger(raw);
  }

  public String readString() throws IOException {
    int    len = readInt();
    byte[] raw = new byte[len];
    read(raw, 0, raw.length);
    return new String(raw);
  }

  public byte[] readStringAsBytes() throws IOException {
    int    len = readInt();
    byte[] raw = new byte[len];
    read(raw, 0, raw.length);
    return raw;
  }

}
