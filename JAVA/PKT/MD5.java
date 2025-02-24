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

public final class MD5 extends MessageDigest implements Cloneable {
    private int[]  x;
    private long   count;
    private int    rest;
    int[]  hash;
    byte[] buffer;

    static byte padding[] = {
        (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private static int rotateLeft (int x, int n) {
	return (x << n) | (x >>> (32 - n));
    }

    private static int FF(int a, int b, int c, int d, int x, int s, int ac) {
	a = a + (((c ^ d) & b) ^ d) + x + ac;
	return rotateLeft(a, s) + b;
    }

    private static int GG(int a, int b, int c, int d, int x, int s, int ac) {
	a = a + (((b ^ c) & d) ^ c) + x + ac;
	return rotateLeft(a, s) + b;
    }

    private static int HH(int a, int b, int c, int d, int x, int s, int ac) {
	a = a + (b ^ c ^ d) + x + ac;
	return rotateLeft(a, s) + b;
    }

    private static int II(int a, int b, int c, int d, int x, int s, int ac) {
	a = a + (c ^ (b | ~d)) + x + ac;
	return rotateLeft(a, s) + b;
    }

    void transform(byte data[], int offset) {
	int a = hash[0];
	int b = hash[1];
	int c = hash[2];
	int d = hash[3];

	int i;
	for (i = 0; i < 16; i++) {
	    x[i] =
		((int)  (data[offset++] & 0xff))        |
		(((int) (data[offset++] & 0xff)) << 8)  |
		(((int) (data[offset++] & 0xff)) << 16) |
		(((int) (data[offset++] & 0xff)) << 24);
	}

	// Round 1 
	a = FF (a, b, c, d, x[ 0],   7, 0xd76aa478); // 1
	d = FF (d, a, b, c, x[ 1],  12, 0xe8c7b756); // 2
	c = FF (c, d, a, b, x[ 2],  17, 0x242070db); // 3
	b = FF (b, c, d, a, x[ 3],  22, 0xc1bdceee); // 4
	a = FF (a, b, c, d, x[ 4],   7, 0xf57c0faf); // 5
	d = FF (d, a, b, c, x[ 5],  12, 0x4787c62a); // 6
	c = FF (c, d, a, b, x[ 6],  17, 0xa8304613); // 7
	b = FF (b, c, d, a, x[ 7],  22, 0xfd469501); // 8
	a = FF (a, b, c, d, x[ 8],   7, 0x698098d8); // 9
	d = FF (d, a, b, c, x[ 9],  12, 0x8b44f7af); // 10
	c = FF (c, d, a, b, x[10],  17, 0xffff5bb1); // 11
	b = FF (b, c, d, a, x[11],  22, 0x895cd7be); // 12
	a = FF (a, b, c, d, x[12],   7, 0x6b901122); // 13
	d = FF (d, a, b, c, x[13],  12, 0xfd987193); // 14
	c = FF (c, d, a, b, x[14],  17, 0xa679438e); // 15
	b = FF (b, c, d, a, x[15],  22, 0x49b40821); // 16

	// Round 2 
	a = GG (a, b, c, d, x[ 1],   5, 0xf61e2562); // 17
	d = GG (d, a, b, c, x[ 6],   9, 0xc040b340); // 18
	c = GG (c, d, a, b, x[11],  14, 0x265e5a51); // 19
	b = GG (b, c, d, a, x[ 0],  20, 0xe9b6c7aa); // 20
	a = GG (a, b, c, d, x[ 5],   5, 0xd62f105d); // 21
	d = GG (d, a, b, c, x[10],   9,  0x2441453); // 22
	c = GG (c, d, a, b, x[15],  14, 0xd8a1e681); // 23
	b = GG (b, c, d, a, x[ 4],  20, 0xe7d3fbc8); // 24
	a = GG (a, b, c, d, x[ 9],   5, 0x21e1cde6); // 25
	d = GG (d, a, b, c, x[14],   9, 0xc33707d6); // 26
	c = GG (c, d, a, b, x[ 3],  14, 0xf4d50d87); // 27
	b = GG (b, c, d, a, x[ 8],  20, 0x455a14ed); // 28
	a = GG (a, b, c, d, x[13],   5, 0xa9e3e905); // 29
	d = GG (d, a, b, c, x[ 2],   9, 0xfcefa3f8); // 30
	c = GG (c, d, a, b, x[ 7],  14, 0x676f02d9); // 31
	b = GG (b, c, d, a, x[12],  20, 0x8d2a4c8a); // 32

            // Round 3 
	a = HH (a, b, c, d, x[ 5],   4, 0xfffa3942); // 33
	d = HH (d, a, b, c, x[ 8],  11, 0x8771f681); // 34
	c = HH (c, d, a, b, x[11],  16, 0x6d9d6122); // 35
	b = HH (b, c, d, a, x[14],  23, 0xfde5380c); // 36
	a = HH (a, b, c, d, x[ 1],   4, 0xa4beea44); // 37
	d = HH (d, a, b, c, x[ 4],  11, 0x4bdecfa9); // 38
	c = HH (c, d, a, b, x[ 7],  16, 0xf6bb4b60); // 39
	b = HH (b, c, d, a, x[10],  23, 0xbebfbc70); // 40
	a = HH (a, b, c, d, x[13],   4, 0x289b7ec6); // 41
	d = HH (d, a, b, c, x[ 0],  11, 0xeaa127fa); // 42
	c = HH (c, d, a, b, x[ 3],  16, 0xd4ef3085); // 43
	b = HH (b, c, d, a, x[ 6],  23,  0x4881d05); // 44
	a = HH (a, b, c, d, x[ 9],   4, 0xd9d4d039); // 45
	d = HH (d, a, b, c, x[12],  11, 0xe6db99e5); // 46
	c = HH (c, d, a, b, x[15],  16, 0x1fa27cf8); // 47
	b = HH (b, c, d, a, x[ 2],  23, 0xc4ac5665); // 48

            // Round 4 
	a = II (a, b, c, d, x[ 0],   6, 0xf4292244); // 49
	d = II (d, a, b, c, x[ 7],  10, 0x432aff97); // 50
	c = II (c, d, a, b, x[14],  15, 0xab9423a7); // 51
	b = II (b, c, d, a, x[ 5],  21, 0xfc93a039); // 52
	a = II (a, b, c, d, x[12],   6, 0x655b59c3); // 53
	d = II (d, a, b, c, x[ 3],  10, 0x8f0ccc92); // 54
	c = II (c, d, a, b, x[10],  15, 0xffeff47d); // 55
	b = II (b, c, d, a, x[ 1],  21, 0x85845dd1); // 56
	a = II (a, b, c, d, x[ 8],   6, 0x6fa87e4f); // 57
	d = II (d, a, b, c, x[15],  10, 0xfe2ce6e0); // 58
	c = II (c, d, a, b, x[ 6],  15, 0xa3014314); // 59
	b = II (b, c, d, a, x[13],  21, 0x4e0811a1); // 60
	a = II (a, b, c, d, x[ 4],   6, 0xf7537e82); // 61
	d = II (d, a, b, c, x[11],  10, 0xbd3af235); // 62
	c = II (c, d, a, b, x[ 2],  15, 0x2ad7d2bb); // 63
	b = II (b, c, d, a, x[ 9],  21, 0xeb86d391); // 64

	hash[0] += a;
	hash[1] += b;
	hash[2] += c;
	hash[3] += d;
    }

    public MD5() {
	buffer = new byte[64];
	hash   = new int[4];
	x      = new int[16];
	reset();
    }

    private MD5(MD5 c) {
	buffer = new byte[64];
	hash   = new int[4];
	x      = new int[16];
	System.arraycopy(c.hash, 0, hash, 0, 4);
	System.arraycopy(c.buffer, 0, buffer, 0, 64);
	count = c.count;
	rest  = c.rest;
    }

    public Object clone() {
	return new MD5(this);
    }

    public String getName() {
        return "MD5";
    }

    public void reset() {
	hash[0] = 0x67452301;
	hash[1] = 0xefcdab89;
	hash[2] = 0x98badcfe;
	hash[3] = 0x10325476;
	count   = 0;
        rest    = 0;
    }

    public void update(byte[] data, int offset, int length) {
        int left = 64 - rest;

        count += length;

	if(rest > 0 && length >= left) {
	    System.arraycopy(data, offset, buffer, rest, left);
	    transform(buffer, 0);
	    offset += left;
	    length -= left;
	    rest   =  0;
	}

	while(length > 63) {
	    transform(data, offset);
	    offset += 64;
	    length -= 64;
	}

	if(length > 0) {
	    System.arraycopy(data, offset, buffer, rest, length);
	    rest += length;
	}
    }

    public byte[] digest() {
	byte[] buf = new byte[16];
	digestInto(buf, 0);
        return buf;
    }

    public int digestInto(byte[] dest, int destOff) {
	int padlen = (rest < 56) ? (56 - rest) : (120 - rest);

	count *= 8;
	byte[] countBytes = {
	    (byte)(count),
	    (byte)(count >>>  8),
	    (byte)(count >>> 16),
	    (byte)(count >>> 24),
	    (byte)(count >>> 32),
	    (byte)(count >>> 40),
	    (byte)(count >>> 58),
	    (byte)(count >>> 56)
	};

	update(padding, 0, padlen);
	update(countBytes, 0, 8);

        int i;
        for (i = 0; i < 4; i++) {
            dest[destOff++] = (byte) (hash[i] & 0xff);
            dest[destOff++] = (byte)((hash[i] >>> 8) & 0xff);
            dest[destOff++] = (byte)((hash[i] >>> 16) & 0xff);
            dest[destOff++] = (byte)((hash[i] >>> 24) & 0xff);
        }

	reset();
	return 16;
    }

    public int blockSize() {
        return 64;
    }

    public int hashSize() {
        return 16;
    }
}
