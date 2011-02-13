/******************************************************************************
 *
 * Copyright (c) 2000 by Mindbright Technology AB, Stockholm, Sweden.
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

public class HexDump {

    /* hexadecimal digits. */
    private static final char[] HEX_DIGITS = {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

   /**
    * Returns a string of 8 hexadecimal digits (most significant
    * digit first) corresponding to the integer <i>n</i>, which is
    * treated as unsigned.
    */
   public static String intToString (int n) {
      char[] buf = new char[8];
      for(int i = 7; i >= 0; i--) {
	  buf[i] = HEX_DIGITS[n & 0x0F];
	  n >>>= 4;
      }
      return new String(buf);
   }

    /**
     * Returns a string of hexadecimal digits from a byte array. Each
     * byte is converted to 2 hex symbols.
     */
    private static String toString (byte[] ba) {
	return toString(ba, 0, ba.length);
    }

    private static String toString (byte[] ba, int offset, int length) {
	char[] buf = new char[length * 2];
	for(int i = offset, j = 0, k; i < offset+length; ) {
	    k = ba[i++];
	    buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
	    buf[j++] = HEX_DIGITS[ k      & 0x0F];
	}
	return new String(buf);
    }

    public static String formatHex(int i, int sz) {
	String str = Integer.toHexString(i);
	while(str.length() < sz) {
	    str = "0" + str;
	}
	return str;
    }

    public static void hexDump(byte[] buf, int off, int len) {
	int i, j, jmax;
	int c;

	for(i = 0; i < len; i += 0x10) {
	    String line = formatHex(i + off, 8) + ": ";

	    jmax = len - i;
	    jmax = jmax > 16 ? 16 : jmax;

	    for(j = 0; j < jmax; j++) {
		c = ((int)buf[i+j] + 0x100) % 0x100;
		line += formatHex(c, 2);
		if ((j % 2) == 1)
		    line += " ";
	    }

	    for(; j < 16; j++) {
		line += "  ";
		if ((j % 2) == 1)
		    line += " ";
	    }

	    line += " ";

	    for(j = 0; j < jmax; j++) {
		c = ((int)buf[i+j] + 0x100) % 0x100;
		c = c < 32 || c >= 127 ? '.' : c;
		line += (char)c;
	    }

	    System.out.println(line);
	}
    }

}
