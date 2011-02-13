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

import java.net.*;
import java.io.*;

import mindbright.security.RSAPublicKey;

public class SSHRSAAuthenticator implements SSHAuthenticator {
  protected String fileName;
  protected String username;
  protected String password;
  protected int    cipher;

  public SSHRSAAuthenticator(String username, String password, String fileName, String cipher) {
    this.username = username;
    this.password = password;
    this.cipher   = SSH.getCipherType(cipher);
    this.fileName = fileName;
  }

  public SSHRSAAuthenticator(String username, String password, String fileName) {
    this.username = username;
    this.password = password;
    this.cipher   = SSH.CIPHER_DEFAULT;
    this.fileName = fileName;
  }

  public String getUsername(SSHClientUser origin) {
    return username;
  }

  public String getPassword(SSHClientUser origin) {
    return "";
  }

  public String getChallengeResponse(SSHClientUser origin, String challenge) {
    return "";
  }

  public int[] getAuthTypes(SSHClientUser origin) {
    int[] types = new int[1];
    types[0] = SSH.AUTH_RSA;
    return types;
  }

  public int getCipher(SSHClientUser origin) {
    return cipher;
  }

  public String getIdentityPassword(SSHClientUser origin) {
    return password;
  }

  public SSHRSAKeyFile getIdentityFile(SSHClientUser origin) throws IOException {
    return new SSHRSAKeyFile(fileName);
  }

  public boolean verifyKnownHosts(RSAPublicKey hostPub) {
    return true;
  }

}

