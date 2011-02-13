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
package mindbright.util;

import java.math.BigInteger;

import mindbright.security.SecureRandom;

public final class Math {

    public static BigInteger findRandomGenerator(BigInteger order,
						   BigInteger modulo,
						   SecureRandom random) {
	BigInteger one = BigInteger.valueOf(1);
	BigInteger aux = modulo.subtract(BigInteger.valueOf(1));
	BigInteger t   = aux.mod(order);
	BigInteger generator;

	if(t.longValue() != 0) {
	    return null;
	}

	t = aux.divide(order);

	while(true) {
	    generator = new BigInteger(modulo.bitLength(), random);
	    generator = generator.mod(modulo);
	    generator = generator.modPow(t, modulo);
	    if(generator.compareTo(one) != 0)
		break;
	}

	aux = generator.modPow(order, modulo);

	if(aux.compareTo(one) != 0) {
	    return null;
	}

	return generator;
    }

    public static BigInteger[] findRandomStrongPrime(int primeBits,
						     int orderBits,
						     SecureRandom random) {
	BigInteger one = BigInteger.valueOf(1);
	BigInteger u, aux, aux2, t;
	long[] table_q, table_u, prime_table;
	PrimeSieve sieve = new PrimeSieve(16000);
	int table_count  = sieve.availablePrimes() - 1;
	int i, j;
	boolean flag;
	BigInteger prime = null, order = null;

	order = new BigInteger(orderBits, 20, random);

	prime_table = new long[table_count];
	table_q     = new long[table_count];
	table_u     = new long[table_count];

	i = 0;
	for(int pN = 2; pN != 0; pN = sieve.getNextPrime(pN), i++) {
	    prime_table[i] = (long)pN;
	}

	for(i = 0; i < table_count; i++) {
	    table_q[i] =
		(order.mod(BigInteger.valueOf(prime_table[i])).longValue() *
		 (long)2) % prime_table[i];
	}

	while(true) {
	    u = new BigInteger(primeBits, random);
	    u.setBit(primeBits - 1);
	    aux = order.shiftLeft(1);
	    aux2 = u.mod(aux);
	    u = u.subtract(aux2);
	    u = u.add(one);

	    if(u.bitLength() <= (primeBits - 1))
		continue;

	    for(j = 0; j < table_count; j++) {
		table_u[j] =
		    u.mod(BigInteger.valueOf(prime_table[j])).longValue();
	    }

	    aux2 = order.shiftLeft(1);

	    for(i = 0; i < (1 << 24); i++) {
		long cur_p;
		long value;

		flag = true;
		for(j = 1; j < table_count; j++) {
		    cur_p = prime_table[j];
		    value = table_u[j];
		    if(value >= cur_p)
			value -= cur_p;
		    if(value == 0)
			flag = false;
		    table_u[j] = value + table_q[j];
		}
		if(!flag)
		    continue;

		aux   = aux2.multiply(BigInteger.valueOf(i));
		prime = u.add(aux);

		if(prime.bitLength() > primeBits)
		    continue;

		if(prime.isProbablePrime(20))
		    break;
	    }

	    if(i < (1 << 24))
		break;
	}

	return new BigInteger[] { prime, order };
    }

}

