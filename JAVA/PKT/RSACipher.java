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
/*
 * !!! Author's comment: The contents of this file is heavily based
 * upon Tatu Ylonen's c-code in the ssh1.2.26 package, which in turn
 * is a standard implementation of the RSA algorithm, the code is
 * rather trivial (though the math behind it is not :-). I don't know
 * whom are responsible for the original optimization using the
 * Chinese remainder theorem which I guess is the only non-trivial
 * part of this implementation. Please note that RSA can't be used
 * without proper licensing in the United States.
 *
 * Below is some references to useful information about RSA:
 *
 * Bruce Schneier: Applied Cryptography 2nd ed., John Wiley & Sons, 1996
 * Arto Salomaa: Public-Key Cryptography 2nd ed., Springer-Verlag, 1996
 * Man Young Rhee: Cryptography and Secure Data Comm.,  McGraw-Hill, 1994
 * R. Rivest, A. Shamir, and L. M. Adleman: Cryptographic Communications
 *    System and Method.  US Patent 4,405,829, 1983.
 * Hans Riesel: Prime Numbers and Computer Methods for Factorization.  
 *    Birkhauser, 1994.  
 */
package mindbright.security;

import java.math.BigInteger;
import java.io.IOException;

public class RSACipher {

  public KeyPair keys;

  public RSACipher(KeyPair keys) {
    this.keys = keys;
  }

  public BigInteger doPublic(BigInteger input) {
    RSAPublicKey pubKey = (RSAPublicKey)keys.getPublic();
    BigInteger   result;

    result = input.modPow(pubKey.getE(), pubKey.getN());

    return result;
  }

  public BigInteger doPrivate(BigInteger input) {
    BigInteger    dp;
    BigInteger    dq;
    BigInteger    p2;
    BigInteger    q2;
    BigInteger    k;
    BigInteger    result;
    BigInteger    one = BigInteger.valueOf(1L);
    RSAPrivateKey privKey = (RSAPrivateKey)keys.getPrivate();

    dp = privKey.getP().subtract(one);
    dp = privKey.getD().mod(dp);

    dq = privKey.getQ().subtract(one);
    dq = privKey.getD().mod(dq);

    p2 = input.mod(privKey.getP());
    p2 = p2.modPow(dp, privKey.getP());

    q2 = input.mod(privKey.getQ());
    q2 = q2.modPow(dq, privKey.getQ());

    if(p2.compareTo(q2) == 0)
      return p2;

    k = q2.subtract(p2).mod(privKey.getQ());
    k = k.multiply(privKey.getU());
    k = k.mod(privKey.getQ());

    result = k.multiply(privKey.getP());
    result = result.add(p2);

    return result;
  }

  public static BigInteger stripPad(BigInteger input) throws IOException {
    byte[] strip = input.toByteArray();
    byte[] val;
    int    i;

    if(strip[0] != 0x02)
      throw new IOException("Invalid strip-data");
    for(i = 0; i < strip.length; i++)
      if(strip[i] == 0)
	break;
    if(i == strip.length)
      throw new IOException("Invalid strip-data");
    val = new byte[strip.length - i];
    System.arraycopy(strip, i, val, 0, val.length);
    return new BigInteger(val);
  }

  public static BigInteger doPad(BigInteger input, int padLen, SecureRandom rand) throws IOException {
    BigInteger result;
    BigInteger rndInt;
    int inByteLen  = (input.bitLength() + 7) / 8;
    int padByteLen = (padLen + 7) / 8;

    if(inByteLen > padByteLen - 3)
      throw new IOException("rsaPad: Input too long to pad");

    // !!! byte[] ranBytes = new byte[(padByteLen - inByteLen - 3) + 1];
    byte[] ranBytes = new byte[(padByteLen - inByteLen - 3) + 1];
    rand.nextBytes(ranBytes);
    ranBytes[0] = 0;
    for(int i = 1; i < (padByteLen - inByteLen - 3 + 1); i++)
      if(ranBytes[i] == 0)
	ranBytes[i] = 0x17;
	
    rndInt = new BigInteger(ranBytes);
    rndInt = rndInt.shiftLeft((inByteLen + 1) * 8);
    result = new BigInteger("2");
    result = result.shiftLeft((padByteLen - 2) * 8);
    result = result.or(rndInt);
    result = result.or(input);

    return result;
  }


  /* !!! DEBUG !!!
  public static void main(String[] argv) {
    KeyPair    kp;
    RSACipher  cipher;
    BigInteger p;
    BigInteger q;
    BigInteger t;
    BigInteger p_1;
    BigInteger q_1;
    BigInteger phi;
    BigInteger G;
    BigInteger F;
    BigInteger e;
    BigInteger d;
    BigInteger u;
    BigInteger n;
    BigInteger one = new BigInteger("1");

    System.out.println("Generating primes...");

    //    p = new BigInteger(128, 64, new SecureRandom());
    p = new BigInteger("3336670033");
    //    q = new BigInteger(128, 64, new SecureRandom());
    q = new BigInteger("9876543211");

    if(p.compareTo(q) == 0) {
      System.out.println("Same prime, impossible!!!");
      System.exit(0);
    } else if(q.compareTo(p) < 0) {
      t = q;
      q = p;
      p = t;
    }

    t = p.gcd(q);
    if(t.compareTo(one) != 0) {
      System.out.println("Same prime, impossible!!!");
      System.exit(0);
    }

    p_1 = p.subtract(one);
    q_1 = q.subtract(one);
    phi = p_1.multiply(q_1);
    G   = p_1.gcd(q_1);
    F   = phi.divide(G);

    e   = one.shiftLeft(5);
    e   = e.subtract(one);
    do {
      e = e.add(one.add(one));
      t = e.gcd(phi);
    } while(t.compareTo(one) != 0);

    //    d = e.modInverse(F);
    d = e.modInverse(phi);
    n = p.multiply(q);
    u = p.modInverse(q);

    kp = new KeyPair(new RSAPublicKey(e, n),
		     new RSAPrivateKey(e, n, d, u, p, q));
    cipher = new RSACipher(kp);

    System.out.println("..p = " + p.toString());
    System.out.println("..q = " + q.toString());
    System.out.println("..phi = " + phi.toString());
    System.out.println("..e = " + e.toString());
    System.out.println("..n = " + n.toString());
    System.out.println("..d = " + d.toString());
    System.out.println("..u = " + u.toString());


    //    one = new BigInteger(256, new SecureRandom());
    one = new BigInteger("1901211401001920");

    System.out.println("..val = " + one.toString());
    one = cipher.doPublic(one);
    System.out.println("..val = " + one.toString());
    one = cipher.doPrivate(one);
    System.out.println("..val = " + one.toString());

  }
  */

}
