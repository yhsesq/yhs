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

public class SSHRSAKeyFile {

  //
  //
  int                cipherType;
  RSAPublicKey       pubKey;
  String             fileComment;

  byte[]             encrypted;

  final static String privFileId = "SSH PRIVATE KEY FILE FORMAT 1.1\n";

  static public void createKeyFile(KeyPair kp, String passwd, String name, String comment)
  throws IOException {
    RSAPrivateKey privKey = (RSAPrivateKey)kp.getPrivate();

    ByteArrayOutputStream baos  = new ByteArrayOutputStream(8192);
    SSHDataOutputStream dataOut = new SSHDataOutputStream(baos);

    byte[] c = new byte[2];
    SSH.secureRandom().nextBytes(c);
    dataOut.writeByte((int)c[0]);
    dataOut.writeByte((int)c[1]);
    dataOut.writeByte((int)c[0]);
    dataOut.writeByte((int)c[1]);
    dataOut.writeBigInteger(privKey.getD());
    dataOut.writeBigInteger(privKey.getU());
    dataOut.writeBigInteger(privKey.getP());
    dataOut.writeBigInteger(privKey.getQ());

    byte[] encrypted = baos.toByteArray();
    c = new byte[(8 - (encrypted.length % 8)) + encrypted.length];
    System.arraycopy(encrypted, 0, c, 0, encrypted.length);
    encrypted = c;

    int cipherType = SSH.CIPHER_DEFAULT;

    Cipher cipher = Cipher.getInstance(SSH.cipherClasses[cipherType][0]);
    cipher.setKey(passwd);
    encrypted = cipher.encrypt(encrypted);

    FileOutputStream fileOut = new FileOutputStream(name);
    dataOut = new SSHDataOutputStream(fileOut);

    dataOut.writeBytes(privFileId);
    dataOut.writeByte(0);

    dataOut.writeByte(cipherType);
    dataOut.writeInt(0);
    dataOut.writeInt(0);
    dataOut.writeBigInteger(((RSAPublicKey)kp.getPublic()).getN());
    dataOut.writeBigInteger(((RSAPublicKey)kp.getPublic()).getE());
    dataOut.writeString(comment);

    dataOut.write(encrypted, 0, encrypted.length);
    dataOut.close();
  }

  public SSHRSAKeyFile(String name) throws IOException {
    FileInputStream    fileIn = new FileInputStream(name);
    SSHDataInputStream dataIn = new SSHDataInputStream(fileIn);

    byte[] id = new byte[privFileId.length()];
    dataIn.readFully(id);
    String idStr = new String(id);
    dataIn.readByte(); // Skip end-of-string (?!)

    if(!idStr.equals(privFileId))
      throw new IOException("RSA key file corrupt");

    cipherType = dataIn.readByte();
    if(SSH.cipherClasses[cipherType][0] == null)
      throw new IOException("Ciphertype " + cipherType + " in key-file not supported");

    dataIn.readInt(); // Skip a reserved int

    dataIn.readInt(); // Skip bits... (!?)

    BigInteger n = dataIn.readBigInteger();
    BigInteger e = dataIn.readBigInteger();
    pubKey       = new RSAPublicKey(e, n);

    fileComment  = dataIn.readString();

    byte[] rest = new byte[8192];
    int    len  = dataIn.read(rest);
    dataIn.close();

    encrypted = new byte[len];
    System.arraycopy(rest, 0, encrypted, 0, len);
  }

  public String getComment() {
    return fileComment;
  }

  public RSAPublicKey getPublic() {
    return pubKey;
  }

  public RSAPrivateKey getPrivate(String passwd) {
    RSAPrivateKey privKey = null;

    Cipher cipher = Cipher.getInstance(SSH.cipherClasses[cipherType][0]);
    cipher.setKey(passwd);
    byte[] decrypted = cipher.decrypt(encrypted);
    SSHDataInputStream dataIn = new SSHDataInputStream(new ByteArrayInputStream(decrypted));

    try {
      byte c1  = dataIn.readByte();
      byte c2  = dataIn.readByte();
      byte c11 = dataIn.readByte();
      byte c22 = dataIn.readByte();

      if(c1 != c11 || c2 != c22)
	return null;

      BigInteger d = dataIn.readBigInteger();
      BigInteger u = dataIn.readBigInteger();
      BigInteger p = dataIn.readBigInteger();
      BigInteger q = dataIn.readBigInteger();
      dataIn.close();

      privKey = new RSAPrivateKey(pubKey.getE(), pubKey.getN(),
				  d, u, p, q);
    } catch (IOException e) {
      privKey = null;
    }

    return privKey;
  }

  /* !!! DEBUG
  public static void main(String[] argv) {
    SSHRSAKeyFile file = null;

    try {
      file = new SSHRSAKeyFile("/home/mats/.ssh/identity");
      file.getPrivate("********");
    } catch (Exception e) {
      System.out.println("Error: " + e.toString());
    }
    System.out.println("Comment: " + file.fileComment);
  }
  */

}


