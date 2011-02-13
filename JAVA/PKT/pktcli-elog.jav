package net.sf.pkt;
//
//       _/_/_/_/  _/  _/ _/_/_/_/_/_/
//      _/    _/  _/ _/       _/
//     _/    _/  _/_/        _/
//    _/_/_/_/  _/ _/       _/
//   _/        _/   _/     _/
//  _/        _/     _/   _/
//
//  This file is part of PKT (an XML Universal Packet Archiver
//  tool). See http://PKT.sourceforge.net for details of PKT.
//
//  Copyright (C) 2000-2004 Yohann Sulaiman (yhs@users.sf.net)
//
//  PKT is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  PKT is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
import java.util.Date;
import java.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.lang.*;
import java.text.*;
import java.math.*;
import java.security.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import net.sf.pkt.PKTENGINE;

public class PKTCLI  {
static String version="0.8";
static boolean DEBUG=false; 
private static SecureRandom gentag = new SecureRandom();
// protection
private Object o=new Object();
public final Object clone() throws java.lang.CloneNotSupportedException{ if (true) { throw new java.lang.CloneNotSupportedException();} return o; }
private final void writeObject(ObjectOutputStream out) throws java.io.IOException { throw new java.io.IOException("Object cannot be serialized."); }
private final void readObject(ObjectInputStream in) throws java.io.IOException { throw new java.io.IOException("Class cannot be deserialized."); }
// protection
public static void main (String[] args) {gentag.nextInt();System.gc();
try{ // (new FileOutputStream("pkt.log",false)).close();
log(" ");
log("                _/_/_/_/  _/  _/ _/_/_/_/_/_/");
log("               _/    _/  _/ _/       _/");
log("              _/    _/  _/_/        _/");
log("             _/_/_/_/  _/ _/       _/");
log("            _/        _/   _/     _/");
log(" http://   _/        _/     _/   _/  .sf.net");
log("                       v."+version);
log(" Loading plugins...");
log("  LDPC parity (m=XX) plugin ....(OK)"); // move to 
log("  RIPEMD-160bit hash plugin ....(OK)"); // bridge and
log("  XML reader/writer plugin  ....(OK)"); // actually do
log("  UU Encoder/Decoder plugin ....(OK)"); // tests.
log("  BZIP2 compression plugin  ....(OK)");
log("  AES-256bit enc/dec plugin ....(OK)");
log("  Secure RNG function tests ....(OK)");
log("  XUL GUI Shell Interface   ....(OK)");
log("  ASCII CLI Shell Interface ....(OK)");
log(" ");
log("PKT "+version+" starting at "+new Date());
new PKTCLI().doit(args);
log("PKT "+version+" ending at "+new Date());System.gc();}
catch(Exception e){log("ERROR! "+e.toString());}}
private void doit(String[] args) throws Exception{(new PKTENGINE(this)).bridge(args);}
public static void log(String s){System.err.println(s);if (false){try{System.out.println("PKT>"+s);
PrintStream pixie=new PrintStream(new FileOutputStream("PKT.log",true));pixie.println("PKT>"+s);
pixie.close();pixie=null;}catch(Exception e){System.err.println(e.toString());}}}
private static void logx(String s){if (true){try{System.out.println("PKT>"+s);
PrintStream pixie=new PrintStream(new FileOutputStream("PKT.log",true));pixie.println("PKT>"+s);
pixie.close();pixie=null;}catch(Exception e){System.err.println(e.toString());}}}
private static void logb(String s){if (true){System.err.println("PKT>"+s);}}

} // End of file.
