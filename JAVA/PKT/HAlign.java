// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


public class HAlign
{

    private HAlign(String s)
    {
        myName = s;
    }

    public String toString()
    {
        return myName;
    }

    public static final HAlign fill = new HAlign("fill");
    public static final HAlign center = new HAlign("center");
    public static final HAlign left = new HAlign("left");
    public static final HAlign right = new HAlign("right");
    private final String myName;

}
