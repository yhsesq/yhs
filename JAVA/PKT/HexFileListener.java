// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   HexFileListener.java

package net.sf.pkt.hexed.file;


public interface HexFileListener
{

    public abstract void showError(String s, String s1);

    public abstract void refreshFileList();
}
