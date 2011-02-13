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

public interface SSHPdu {
  public void   writeTo(OutputStream out) throws IOException;
  public void   readFrom(InputStream in) throws IOException;
  public SSHPdu createPdu() throws IOException;

  public byte[] rawData();
  public void   rawSetData(byte[] raw);
  public int    rawOffset();
  public int    rawSize();
  public void   rawAdjustSize(int size);

  //  public SSHPdu preProcess();
  //  public SSHPdu postProcess();
}
