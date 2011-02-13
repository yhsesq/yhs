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

import java.net.*;
import java.io.*;

import mindbright.security.RSAPublicKey;

public interface SSHAuthenticator {
  public String        getUsername(SSHClientUser origin) throws IOException;
  public String        getPassword(SSHClientUser origin) throws IOException;
  public String        getChallengeResponse(SSHClientUser origin, String challenge) throws IOException;
  public int[]         getAuthTypes(SSHClientUser origin);
  public int           getCipher(SSHClientUser origin);
  public SSHRSAKeyFile getIdentityFile(SSHClientUser origin) throws IOException;
  public String        getIdentityPassword(SSHClientUser origin) throws IOException;
  public boolean       verifyKnownHosts(RSAPublicKey hostPub) throws IOException;
}
