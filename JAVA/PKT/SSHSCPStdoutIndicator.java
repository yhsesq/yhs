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

public class SSHSCPStdoutIndicator implements SSHSCPIndicator {
    public void connected(String server) {
	System.out.println("Connected to " + server + "...");
    }
    public void startFile(String file, int size) {
	System.out.print("Transfering " + file + " (" + size + " bytes)...");
    }
    public void startDir(String file) {
	System.out.println("Entering directory " + file);
    }
    public void endFile() {
	System.out.println("done");
    }
    public void endDir() {
    }
    public void progress(int size) {
    }
}
