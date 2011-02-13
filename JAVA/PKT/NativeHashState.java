/******************************************************************************
 *
 * Copyright (c) 1998-2000 by Mindbright Technology AB, Stockholm, Sweden.
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
package mindbright.security;

public class NativeHashState extends MessageDigest {

    java.security.MessageDigest md;
    String                      myAlg;
  
    public NativeHashState() {
    }

    protected void init(String algorithm) throws Exception {
	myAlg = algorithm;
	if(algorithm.equals("SHA1"))
	    algorithm = "SHA";
	md = java.security.MessageDigest.getInstance(algorithm);
    }

    public String getName() {
	return myAlg;
    }

    public void reset() {
	md.reset();
    }

    public void update(byte[] buffer, int offset, int length) {
	md.update(buffer,offset,length);
    }

    public byte[] digest() {
	return md.digest();
    }

    public int blockSize() {
	return 64;
    }

    public int hashSize() {
	if(myAlg.equals("SHA")) {
	    return 20;
	} else {
	    return 16;
	}
    }
}
