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

public final class StringUtil {
    public static String trimLeft(String str) {
	char[] val = str.toCharArray();
	int st = 0;
	while ((st < val.length) && (val[st] <= ' ')) {
	    st++;
	}
	return str.substring(st);
    }

    public static String trimRight(String str) {
	char[] val = str.toCharArray();
	int    end = val.length;
	while ((end > 0) && (val[end - 1] <= ' ')) {
	    end--;
	}
	return str.substring(0, end);
    }
}
