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

public class SSHPasswordAuthenticator implements SSHAuthenticator {
  protected String username;
  protected String password;
  protected int    cipher;

  public SSHPasswordAuthenticator(String username, String password, String cipher) {
    this.username = username;
    this.password = password;
    this.cipher   = SSH.getCipherType(cipher);
  }

  public SSHPasswordAuthenticator(String username, String password) {
    this.username = username;
    this.password = password;
    this.cipher   = SSH.CIPHER_DEFAULT;
  }

  public String getUsername(SSHClientUser origin) {
    return username;
  }

  public String getPassword(SSHClientUser origin) {
    return password;
  }

  public String getChallengeResponse(SSHClientUser origin, String challenge) {
    return password;
  }

  public int[] getAuthTypes(SSHClientUser origin) {
    int[] types = new int[1];
    types[0] = SSH.AUTH_PASSWORD;
    return types;
  }

  public int getCipher(SSHClientUser origin) {
    return cipher;
  }

  public String getIdentityPassword(SSHClientUser origin) {
    return "";
  }

  public SSHRSAKeyFile getIdentityFile(SSHClientUser origin) {
    return null;
  }

  public boolean verifyKnownHosts(RSAPublicKey hostPub) {
    return true;
  }

}

