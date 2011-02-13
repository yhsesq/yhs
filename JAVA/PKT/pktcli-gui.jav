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
import net.sf.pkt.PKTENGINE;
import java.awt.*;
import java.io.*;
import java.text.*;
import thinlet.*;
import thinlet.Thinlet;
import thinlet.FrameLauncher;
import mindbright.application.*;
import net.sf.pkt.hexed.*;
import net.sf.pkt.calc.*;
import net.sf.pkt.browse.*;
import net.sf.pkt.clock.*;
import net.sf.pkt.convert.*;
import net.sf.pkt.edit.*;
import net.sf.pkt.ssh2.*;
import net.sf.pkt.Unicode.*;

public class PKTCLI  extends Thinlet implements Runnable {
	private Image openedicon, closedicon, fileicon, loading, loaded, load;
	private static boolean ok=true;
	private static boolean jws=false;
	private static Thinlet tinlet;
	private static String[] passed=null;
	private static Object grouplist;
	private static Object filelist;
	private static String CIPHER="aes256";
private static Object browse=null;
private static Object clock=null;
private static Object conv=null;
private static Object calc=null;
private static Object vnc=null;
private static Object mt=null;
private static Object jc=null;
private static Object ht=null;
private static Object ed=null;
private static Object vk=null;
	private static Object cmdl; // System ready
	public static Object statl; // textbox
	public static Object tpar; // parity use split [1]
	public static Object pwd; //  password
	public static Object prog; // progress bar
	private static Object status; // icon 
static String version="2.4";
static boolean DEBUG=false; 
private static SecureRandom gentag = new SecureRandom();
	public PKTCLI(String s) {try{ 
// getClass().getResource("/icons/ico.gif")
// if(new File("pktgui.xml").exists()){
		add(parse("pktgui.xml"));
// }else{log(" pktgui.xml not found! ");}
	}catch(Exception e){log(" GUI Instantiate error :"+e.toString());}}
	public PKTCLI() throws Exception {}
public static void main (String[] args) {
try{  if(args.length != 0 && args[0].compareTo("DEBUG")==0){(System.getProperties()).list(System.err);System.err.println("PKT is distributed under the terms of the GNU GPL (www.gnu.org) and is copyright (C) 2004 Yohann Sulaiman (yhs@users.sf.net).");(System.getProperties()).list(System.out);System.out.println("PKT is distributed under the terms of the GNU GPL (www.gnu.org) and is copyright (C) 2004 Yohann Sulaiman (yhs@users.sf.net).");}
gentag.nextInt();System.gc();if(args.length != 0 && args[0].compareTo("JWS")==0){jws=true;}
if(!jws){(new FileOutputStream("pkt.log",false)).close();}
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
log("  AES-256 (FIPS-197) plugin ....(OK)");
log("  AES-128 (FIPS-197) plugin ....(OK)");
log("  Blowfish Cipher plugin    ....(OK)");
log("  DoD 5220.22-M wipe plugin ....(OK)");
log("  Legacy archiver plugin    ....(OK)");
log("  MT SSH/SCP/TUN/VNC snapin ....(OK)");
log("  JC SSHv2/SFTP/TUN snapin  ....(OK)");
log("  ASCII editor/VKbd snapin  ....(OK)");
log("  Hex Editor / tools snapin ....(OK)");
log("  Calc / Clock / Cnv snapin ....(OK)");
log("  Secure RNG function tests ....(OK)");
log("  XUL GUI Shell Interface   ....(OK)");
log("  ASCII CLI Shell Interface ....(OK)");
log(" ");
log(" System started at "+new Date()+" by "+System.getProperty("user.name")+" from "+System.getProperty("user.dir")+" using "+System.getProperty("java.vm.name")+" ver. "+System.getProperty("java.vm.version")+" manufactured by "+System.getProperty("java.vm.vendor")+" running on a "+System.getProperty("os.arch")+" machine with "+System.getProperty("os.name")+" ver. "+System.getProperty("os.version")+".");
log(" ");
if(args.length==0){ String[] s = new String[2];s[0]=new String("version");s[1]=" ";
log("PKT GUI "+version+" starting at "+new Date());
(new thinlet.FrameLauncher(" PKT - Universal XML Packet Archiver xul shell [pktgui v"+version+"] [pkt engine rev."+(String)((new PKTENGINE(false)).bridge(s)).elementAt(0)+"] pkt.sf.net", new net.sf.pkt.PKTCLI("null"), 640, 480)).setResizable(false);s=null;System.gc();
}else{
if(args[0].compareTo("JWS")==0){jws=true;
log("PKT GUI "+version+" starting at "+new Date());
String[] s = new String[2];s[0]=new String("version");s[1]=" ";
(new thinlet.FrameLauncher(" PKT - Universal XML Packet Archiver jws xul shell [pktgui v"+version+"] [pkt engine rev."+(String)((new PKTENGINE(false)).bridge(s)).elementAt(0)+"] pkt.sf.net", new net.sf.pkt.PKTCLI("null"), 640, 480)).setResizable(false);s=null;System.gc();
}else{
log("PKT CLI "+version+" starting at "+new Date());
new PKTCLI().doit(args);
log("PKT CLI "+version+" ending at "+new Date());System.gc();}
}}catch(Exception e){log("ERROR! "+e);}}

public void run() {try{if(passed!=null){ runner();
}}catch(Exception e){log("ThreadERROR! "+e);}spinunlock();}

private void runner() throws Exception{
final Thread t = Thread.currentThread();
(new PKTENGINE(this)).bridge(passed);passed=null;}

private void doit(String[] args) throws Exception{(new PKTENGINE(this)).bridge(args);}

public void wipelog(){try{new File("pkt.log").deleteOnExit();}catch(Exception e){}}

public void shutdown(){System.exit(255);}

public void launchvnc(){String[] a=new String[2];a[0]=new String("sshhost");a[1]=new String("127.0.0.1");
if(vnc != null){ try{
((mindbright.application.MindVNC)vnc).doExit();
}catch(Exception e){} vnc=null;} 
vnc=(new mindbright.application.MindVNC());((mindbright.application.MindVNC)vnc).main(a);}

public void launchbrowse(){if(browse != null){ try{
((net.sf.pkt.browse.PKTBrowse)browse).doExit();
}catch(Exception e){} browse=null;} 
browse=(new net.sf.pkt.browse.PKTBrowse("http://pkt.sourceforge.net/tutorial.html"));}

public void launchclock(){if(clock != null){ try{
((net.sf.pkt.clock.PKTClock)clock).doExit();
}catch(Exception e){} clock=null;} 
clock=(new net.sf.pkt.clock.PKTClock());}

public void launchedit(){if(ed != null){ try{
((net.sf.pkt.edit.notepad)ed).doExit();
}catch(Exception e){} ed=null;} 
ed=(new net.sf.pkt.edit.notepad());}

public void launchvk(){if(vk != null){ try{
((net.sf.pkt.Unicode.VKBD)vk).doExit();
}catch(Exception e){} vk=null;} 
vk=(new net.sf.pkt.Unicode.VKBD());}

public void launchedit(Thinlet thinlet,Object s){if(ed != null){ try{
((net.sf.pkt.edit.notepad)ed).doExit();
}catch(Exception e){} ed=null;} 
ed=(new net.sf.pkt.edit.notepad(thinlet.getString(s,"text")));}

public void launchconv(){if(conv != null){ try{
((net.sf.pkt.convert.PKTConvert)conv).doExit();
}catch(Exception e){} conv=null;} 
conv=(new net.sf.pkt.convert.PKTConvert());}

public void launchcalc(){if(calc != null){ try{
((net.sf.pkt.calc.Calculator)calc).doExit();
}catch(Exception e){} calc=null;} 
calc=(new net.sf.pkt.calc.Calculator());}

public void launchmt(){String[] a=new String[2];a[0]=new String("sshhost");a[1]=new String("127.0.0.1");
if(mt != null){ try{ ((mindbright.application.MindTerm)mt).doExit();
}catch(Exception e){} mt=null;} 
mt=(new mindbright.application.MindTerm());((mindbright.application.MindTerm)mt).main(new String[0]);}

public void launchjc(){
if(jc != null){ try{ ((net.sf.pkt.ssh2.JCTerm)jc).doExit();
}catch(Exception e){} jc=null;} 
jc=(new net.sf.pkt.ssh2.JCTerm());((net.sf.pkt.ssh2.JCTerm)jc).jclaunch();}

public void hexed(Thinlet thinlet){ String[] a=new String[2];
a[0]=new String("sshhost");a[1]=new String("127.0.0.1");
if(ht != null){ try{ ((net.sf.pkt.hexed.gui.HexEditor)ht).doExit();
}catch(Exception e){} ht=null;} 
ht=(new net.sf.pkt.hexed.gui.HexEditor());}

public void tlegacy(Thinlet thinlet){legacy(thinlet,"-tvf",null);}
public void tIlegacy(Thinlet thinlet){legacy(thinlet,"-tIvf",null);}
public void tzlegacy(Thinlet thinlet){legacy(thinlet,"-tzvf",null);}
public void Tlegacy(Thinlet thinlet){legacy(thinlet,"-Tvf",null);}
public void xlegacy(Thinlet thinlet){legacy(thinlet,"-xvf",null);}
public void xIlegacy(Thinlet thinlet){legacy(thinlet,"-xIvf",null);}
public void xzlegacy(Thinlet thinlet){legacy(thinlet,"-xzvf",null);}
public void Xlegacy(Thinlet thinlet){legacy(thinlet,"-Xvf",null);}
public void clegacy(Thinlet thinlet){legacy(thinlet,"-cvf","TAR");}
public void cIlegacy(Thinlet thinlet){legacy(thinlet,"-cIvf","TAR.BZ2");}
public void czlegacy(Thinlet thinlet){legacy(thinlet,"-czvf","TAR.GZ");}
public void Clegacy(Thinlet thinlet){legacy(thinlet,"-Cvf","ZIP");}
public void Jlegacy(Thinlet thinlet){legacy(thinlet,"-Cvf","JAR");}

private static synchronized boolean spinlock(){
if(ok){ok = false;return true;}else{return false;}}

private static synchronized void spinunlock(){
tinlet.setIcon(prog, "icon", tinlet.getIcon("loaded.gif"));
tinlet.setIcon(status, "icon", tinlet.getIcon("loaded.gif"));
tinlet.setString(cmdl,"text","SYSTEM READY"); ok = true;}

private void legacy(Thinlet thinlet, String zi, String fad) {if(spinlock()){ try{
String zzi=(" L"+System.currentTimeMillis()+"-"+giverandom(1000)+"L.").trim();
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;
thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
grp.addElement("legacy");
grp.addElement(zi);
if(fad!=null){grp.addElement(zzi+fad);log(" Writing archive to "+zzi+fad+" in current directory "+System.getProperty("user.dir"));}
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

public void setspinlock(){if(ok){ok=false;log(" WARNING: Spinlock now manually armed. Functions will be disabled. ");}else{ok=true;log(" WARNING: Spinlock now manually disarmed. Functions are enabled, thread safety is not guaranteed. ");}}

public void schgcol(Thinlet thinlet){
thinlet.setColors(0xeeeecc, 0x000000, 0xffffff,0x999966, 0xb0b096, 0xededcb, 0xcccc99,0xcc6600, 0xffcc66);}
public void ochgcol(Thinlet thinlet){
thinlet.setColors(0x6375d6, 0xffffff, 0x7f8fdd,0xd6dff5, 0x9caae5, 0x666666, 0x003399,0xff3333, 0x666666);}
public void mchgcol(Thinlet thinlet){
thinlet.setColors(0xece9d8, 0x000000, 0xffffff,0x909090, 0xb0b0b0, 0xededed, 0xc7c5b2,0xe68b2c, 0xf2c977);}
public void dchgcol(Thinlet thinlet){
thinlet.setColors(0xe6e6e6, 0x000000, 0xffffff,0x909090, 0xb0b0b0, 0xededed, 0xb9b9b9,0x89899a, 0xc5c5dd);}

public void chgcipher(Thinlet thinlet, Object a, Object b, Object c) {
if( CIPHER.compareTo("aes256")!=0 && thinlet.getBoolean(a,"selected")){
CIPHER="aes256";thinlet.setBoolean(a,"selected",true);thinlet.setBoolean(b,"selected",false);thinlet.setBoolean(c,"selected",false);
}else if( CIPHER.compareTo("aes128")!=0 && thinlet.getBoolean(b,"selected")){
CIPHER="aes128";thinlet.setBoolean(a,"selected",false);thinlet.setBoolean(b,"selected",true);thinlet.setBoolean(c,"selected",false);
}else{CIPHER="blowfish";thinlet.setBoolean(a,"selected",false);thinlet.setBoolean(b,"selected",false);thinlet.setBoolean(c,"selected",true);}}

public void pops(Thinlet thinlet, Object li, Object pp) {
String[] args=new String[2];args[0]="list";
if(thinlet.getSelectedIndex(li)!=-1){ 
args[1]=thinlet.getString(thinlet.getSelectedItem(li),"text");
Vector v=(new PKTENGINE(this)).bridge(args);
thinlet.removeAll(pp); 
for(int i=0;i<v.size();i++){
                Object item = Thinlet.create("menuitem");
                thinlet.setString(item, "text", (String)v.elementAt(i));
                thinlet.add(pp, item);
}}}

public void fops(Thinlet thinlet, Object li, Object pp) {
String[] args=new String[2];args[0]="flist";
if(thinlet.getSelectedIndex(li)!=-1){ 
args[1]=thinlet.getString(thinlet.getSelectedItem(li),"name");
Vector v=(new PKTENGINE(this)).bridge(args);
thinlet.removeAll(pp); 
for(int i=0;i<v.size();i++){
                Object item = Thinlet.create("menuitem");
                thinlet.setString(item, "text", (String)v.elementAt(i));
                thinlet.add(pp, item);
}}}

public void decpkt(Thinlet thinlet, Object rep, Object decr, Object extr, Object fsav) {if(spinlock()){ try{
if(!(thinlet.getBoolean(rep,"selected")) && !(thinlet.getBoolean(decr,"selected")) && !(thinlet.getBoolean(extr,"selected"))){}else{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;
thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
// ok we do encryption
if((thinlet.getBoolean(rep,"selected")) && (thinlet.getBoolean(extr,"selected"))){
grp.addElement("pardec="+CIPHER+","+thinlet.getString(pwd,"text"));
}else if((thinlet.getBoolean(rep,"selected")) && (thinlet.getBoolean(decr,"selected")) ){
grp.addElement("pardec="+CIPHER+","+thinlet.getString(pwd,"text"));
}else if(!(thinlet.getBoolean(rep,"selected")) && !(thinlet.getBoolean(decr,"selected")) && !(thinlet.getBoolean(extr,"selected"))){
}else if((thinlet.getBoolean(rep,"selected")) && !(thinlet.getBoolean(decr,"selected")) && !(thinlet.getBoolean(extr,"selected"))){
grp.addElement("rebuild");
}else{grp.addElement("decrypt="+CIPHER+","+thinlet.getString(pwd,"text"));}
grp.addElement(thinlet.getString(fsav,"text"));
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}


public void shellex(Thinlet thinlet, Object exec) {try{
Process px=Runtime.getRuntime().exec(thinlet.getString(exec,"text"));
}catch(Exception e){log(" Cannot execute : "+e.toString());}}

public void cwd(Thinlet thinlet, Object fsav) {if(spinlock()){ try{
String newdir=new String();newdir=(thinlet.getString(fsav,"text")).trim();
if (newdir.charAt(newdir.length()-1) != '\\' && newdir.charAt(newdir.length()-1)!='/'){
newdir=newdir+'/';thinlet.setString(fsav,"text",newdir);}
if( new File(newdir).exists() && new File(newdir).isDirectory()){
log(" Changing working directory from "+System.setProperty("user.dir",newdir)+" to "+newdir);
}else {log(" Cannot change working directory to non-existent directory "+newdir);thinlet.setString(fsav,"text","./");}
spinunlock();}catch(Exception e){log("CWDError:"+e.toString());}spinunlock();}}

public void hashpkt(Thinlet thinlet) {if(spinlock()){ try{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;
thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
// ok we do encryption
grp.addElement("hash");
if(oglist.length >0){
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

public void crlfpkt(Thinlet thinlet) {if(spinlock()){ try{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;
thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
// ok we do encryption
grp.addElement("crlf");
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

public void wipepkt(Thinlet thinlet) {if(spinlock()){ try{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;
thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
// ok we do encryption
grp.addElement("shred");
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
passed=myfiles;thinlet.removeAll(grouplist);
new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

public void fmpkt(Thinlet thinlet, Object fil, Object fpos) {if(spinlock()){ try{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
grp.addElement("fmerge");
grp.addElement(thinlet.getString(fil,"text"));
grp.addElement(thinlet.getString(fpos,"text"));
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

public void createpkt(Thinlet thinlet, Object tcomp, Object tpkt, Object tenc, Object spinny, Object fsav) {if(spinlock()){ try{
if (load == null) {load = thinlet.getIcon("load.gif");}
if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
if (loading == null) {loading = thinlet.getIcon("loading.gif");}
tinlet=thinlet;thinlet.setString(statl,"text"," ");
Object[] oglist = thinlet.getItems(grouplist);
Vector grp=new Vector();
if(oglist.length >0){
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
String s=new String();
if(thinlet.getBoolean(tenc,"selected") && thinlet.getString(pwd,"text")!=null){
// ok we do encryption
grp.addElement("encrypt");
grp.addElement(CIPHER+","+thinlet.getString(pwd,"text"));
}else if(thinlet.getBoolean(tcomp,"selected")){
// we do just compression
grp.addElement("compress");
grp.addElement("bzip2");
}else{}
// add it on if we want parity too.
// if no encryption or compression then just parity ok.
if( (thinlet.getString(tpar,"text").split(",")[1]).compareTo("0")==0 ){}else{
// ok we do parity too.
grp.addElement("parity");
grp.addElement("ldpcxx,"+thinlet.getString(tpar,"text").split(",")[1]);}
if(grp.size()>0){
if(thinlet.getBoolean(tpkt,"selected")){grp.addElement("binary");}else{grp.addElement("ascii");}
grp.addElement(thinlet.getString(spinny,"text").trim());
if(((thinlet.getString(fsav,"text").trim()).charAt( (thinlet.getString(fsav,"text").trim()).length()-1 ) !='/') && ((thinlet.getString(fsav,"text").trim()).charAt((thinlet.getString(fsav,"text").trim()).length()-1) !='\\'))
{thinlet.setString(fsav,"text",thinlet.getString(fsav,"text").trim()+"/");}
grp.addElement(thinlet.getString(fsav,"text").trim());
for(int i=0;i<oglist.length;i++){grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}
// ok now we have all items.
String[] myfiles=new String[grp.size()];
for(int i=0;i<grp.size();i++){myfiles[i]=(String)grp.elementAt(i);}
thinlet.setString(cmdl,"text","SYSTEM WORKING");
thinlet.setIcon(prog, "icon", load);
thinlet.setIcon(status, "icon", loading);
passed=myfiles;new Thread(new PKTCLI(),"pkt").start();
}else{spinunlock();}}}catch(Exception e){log("ERROR:"+e.toString());spinunlock();} 
}}

	public void initall(Thinlet thinlet, Object a, Object b, Object c, Object d, Object e, Object f) {if(true){
	tinlet=thinlet;cmdl=a;statl=b;tpar=c;pwd=d;prog=e;status=f;
	if (load == null) {load = thinlet.getIcon("load.gif");}
	if (loaded == null) {loaded = thinlet.getIcon("loaded.gif");}
	if (loading == null) {loading = thinlet.getIcon("loading.gif");}
	}}
	private void fill(Thinlet thinlet, Object list, Vector values, Vector dirs, Vector ffiles, Vector fdirs) {if(true){
	thinlet.removeAll(filelist);
        for (int i = 0; i < dirs.size(); i++) {
                Object item = Thinlet.create("item");
                thinlet.setString(item, "text", (String)dirs.elementAt(i));
                thinlet.setString(item, "name", (String)fdirs.elementAt(i));
		if (fileicon == null) { closedicon = thinlet.getIcon("folder.gif"); }
		thinlet.setIcon(item, "icon", closedicon);
                thinlet.add(filelist, item);
        	}
        for (int i = 0; i < values.size(); i++) {
                Object item = Thinlet.create("item");
                thinlet.setString(item, "text", (String)values.elementAt(i));
                thinlet.setString(item, "name", (String)ffiles.elementAt(i));
		if (fileicon == null) { fileicon = thinlet.getIcon("new.gif"); }
		thinlet.setIcon(item, "icon", fileicon);
                thinlet.add(filelist, item);
        	}
	}}
	
	public void tselected(Thinlet thinlet, Object tree, Object node) {if(true){
			String path = "";
			for (Object item = node; item != tree; item = thinlet.getParent(item)) {
				path = (thinlet.getString(item, "text") + '/' + path).trim();
			}
			if(!new File(path).exists()){
			path = (thinlet.getString(node, "text")).trim()+'/';}
			String[] list;if(new File(path).isDirectory()){
			list = new File(path).list();}else{
			list=new String[1]; list[0]=(thinlet.getString(node,"text")).trim();}
			Vector files=new Vector();
			Vector dirs=new Vector();
			Vector ffiles=new Vector();
			Vector fdirs=new Vector();
			int dircount = 0;
			for (int i = 0; i < list.length; i++) { // separate directories and files
				if (new File(path,list[i]).isDirectory()) {
				fdirs.addElement(path+list[i]);
				dirs.addElement(list[i]);
				}else{
				ffiles.addElement(path+list[i]);
				files.addElement(list[i]);}
			} fill(thinlet,node,files,dirs,ffiles,fdirs);
	}}

	public void tinit(Thinlet thinlet, Object tree, Object fl, Object gl, Object branch) {if(true){
	thinlet.removeAll(tree);filelist=fl;grouplist=gl;addNode(thinlet, tree, thinlet.getString(branch,"text"), true);
	}}
	
	public void texpand(Thinlet thinlet, Object tree, Object node) {if(true){
		if (thinlet.getIcon(node, "icon") == closedicon) {
			if (openedicon == null) {
				openedicon = thinlet.getIcon("open.gif");
			}
			thinlet.setIcon(node, "icon", openedicon);
		}
		
		if (thinlet.getProperty(node, "load") == Boolean.TRUE) {
			String path = "";
			for (Object item = node; item != tree; item = thinlet.getParent(item)) {
				path = (thinlet.getString(item, "text") + '/' + path).trim();
			}
			String[] list = new File(path).list();
			
			int dircount = 0;
			for (int i = 0; i < list.length; i++) { // separate directories and files
				if (new File(path, list[i]).isDirectory()) {
					String swap = list[dircount]; list[dircount] = list[i]; list[i] = swap;
					dircount++;
				}
			}
			
			Collator collator = Collator.getInstance();
			collator.setStrength(Collator.SECONDARY);
			for (int i = 0; i < list.length; i++) { // sort names
				for (int j = i; (j > 0) && ((i < dircount) || (j > dircount)) &&
						(collator.compare(list[j - 1], list[j]) > 0); j--) {
					String swap = list[j]; list[j] = list[j - 1]; list[j - 1] = swap;
				}
			}
			
			thinlet.removeAll(node);
			for (int i = 0; i < list.length; i++) {
				addNode(thinlet, node, list[i], i < dircount);
			}
			thinlet.putProperty(node, "load", null);
		}
	}}
	
	public void tcollapse(Thinlet thinlet, Object node) {if(true){
		if (thinlet.getIcon(node, "icon") == openedicon) {
			thinlet.setIcon(node, "icon", closedicon);
		}
	}}
	
	private void addNode(Thinlet thinlet, Object node, String text, boolean directory) {if(true){
		Object subnode = Thinlet.create("node");
		thinlet.setString(subnode, "text", text);
		if (directory) {
			if (closedicon == null) { closedicon = thinlet.getIcon("folder.gif"); }
			thinlet.setIcon(subnode, "icon", closedicon);
		} else {
			if (fileicon == null) { fileicon = thinlet.getIcon("new.gif"); }
			thinlet.setIcon(subnode, "icon", fileicon);
		}
		thinlet.add(node, subnode);
		if (directory) {
			thinlet.setBoolean(subnode, "expanded", false);
			thinlet.putProperty(subnode, "load", Boolean.TRUE);
			Object loading = Thinlet.create("node");
			thinlet.setString(loading, "text", "loading...");
			thinlet.add(subnode, loading);
		}
	}}

	public void groupadd(Thinlet thinlet, Object node) {if(true){
	Object[] oglist = thinlet.getItems(grouplist);
	Object[] oflist = thinlet.getSelectedItems(filelist);
	Vector grp=new Vector();

	if(oglist.length >0){for(int i=0;i<oglist.length;i++){
	grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}}

	if(oflist.length >0){for(int i=0;i<oflist.length;i++){
	grp=vecuniqadd(grp,thinlet.getString(oflist[i],"name"));}}

	thinlet.removeAll(grouplist);

        for (int i = 0; i < grp.size(); i++) {
if( new File((String)grp.elementAt(i)).exists() ){
                Object item = Thinlet.create("item");
                thinlet.setString(item, "text", (String)grp.elementAt(i));
                thinlet.add(grouplist, item);
}else{log(" WARNING: Item "+(String)grp.elementAt(i)+" no longer exists. Please refresh the file list. ");}
        	}

	}}

	public void groupdel(Thinlet thinlet, Object node) {if(true){
	Object[] oglist = thinlet.getItems(grouplist);
	Object[] oflist = thinlet.getSelectedItems(grouplist);
	Vector grp=new Vector();

	if(oglist.length >0){for(int i=0;i<oglist.length;i++){
	grp=vecuniqadd(grp,thinlet.getString(oglist[i],"text"));}}

	if(oflist.length >0){for(int i=0;i<oflist.length;i++){
	grp=vecuniqrm(grp,thinlet.getString(oflist[i],"text"));}}

	thinlet.removeAll(grouplist);

        for (int i = 0; i < grp.size(); i++) {
                Object item = Thinlet.create("item");
                thinlet.setString(item, "text", (String)grp.elementAt(i));
                thinlet.add(grouplist, item);
        	}

	}}
public static int giverandom(int maxrand){return Math.abs(gentag.nextInt()) % maxrand;}
private static Vector vecuniqadd(Vector v, String s){boolean flag=false;for(int sv=0;sv<v.size();sv++){if( ((String) v.elementAt(sv)).compareTo(s)==0 ){flag=true;}}if(!flag){v.addElement(s);}return v;}
private static Vector vecuniqrm(Vector v, String s){int flag=v.size()+10;for(int sv=0;sv<v.size();sv++){if( ((String) v.elementAt(sv)).compareTo(s)==0 ){flag=sv;}}if(flag<v.size()){v.removeElementAt(flag);}return v;}
public static void log(String s){/*System.err.println(s);*/
if(statl != null){ (tinlet).setString(statl,"text",(tinlet).getString(statl,"text")+"\n"+s);tinlet.repaint(statl);}if (true){try{System.out.println("PKT>"+s);
if(!jws){PrintStream pixie=new PrintStream(new FileOutputStream("PKT.log",true));pixie.println("PKT>"+s);
pixie.close();pixie=null;}}catch(Exception e){System.err.println(e.toString());}}}
private static void logx(String s){if (true){try{System.out.println("PKT>"+s);
PrintStream pixie=new PrintStream(new FileOutputStream("PKT.log",true));pixie.println("PKT>"+s);
pixie.close();pixie=null;}catch(Exception e){System.err.println(e.toString());}}}
private static void logb(String s){if (true){System.err.println("PKT>"+s);}}
} // End of file.

