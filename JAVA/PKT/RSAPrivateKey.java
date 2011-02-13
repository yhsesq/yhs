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
package mindbright.security;

import java.math.BigInteger;

public class RSAPrivateKey extends RSAKey implements PrivateKey {

  private BigInteger d;
  private BigInteger u;
  private BigInteger p;
  private BigInteger q;

  public RSAPrivateKey(BigInteger e, BigInteger n,
		       BigInteger d, BigInteger u,
		       BigInteger p, BigInteger q) {
    super(e, n);
    this.d = d;
    this.u = u;
    this.p = p;
    this.q = q;
  }

  public BigInteger getD() {
    return d;
  }

  public BigInteger getU() {
    return u;
  }

  public BigInteger getP() {
    return p;
  }

  public BigInteger getQ() {
    return q;
  }

}
