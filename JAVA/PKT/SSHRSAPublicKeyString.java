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

import java.math.BigInteger;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.io.*;

import mindbright.security.*;

public class SSHRSAPublicKeyString extends RSAPublicKey {

  String user;
  String opts;

  public SSHRSAPublicKeyString(String opts, String user, BigInteger e, BigInteger n) {
    super(e, n);
    this.opts = opts;
    this.user = user;
  }

  public static SSHRSAPublicKeyString createKey(String opts, String pubKey) throws NoSuchElementException {
    StringTokenizer tok  = new StringTokenizer(pubKey);
    String          user = null;
    String bits;
    String e;
    String n;

    bits = tok.nextToken();
    e    = tok.nextToken();
    n    = tok.nextToken();
    if(tok.hasMoreElements())
      user = tok.nextToken();

    return new SSHRSAPublicKeyString(opts, user, new BigInteger(e), new BigInteger(n));
  }

  public String getOpts() {
    return opts;
  }

  public String getUser() {
    return user;
  }

  public String toString() {
    return ((opts != null ? (opts + " ") : "") +
	    bitLength() + " " + getE() + " " + getN() + " " +
	    (user != null ? user : ""));
  }

  public void toFile(String fileName) throws IOException {
    FileOutputStream    fileOut = new FileOutputStream(fileName);
    SSHDataOutputStream dataOut = new SSHDataOutputStream(fileOut);
    dataOut.writeBytes(toString());
    dataOut.close();
  }

}
