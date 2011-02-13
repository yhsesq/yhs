// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


public class VAlign
{

    private VAlign(String s)
    {
        myName = s;
    }

    public String toString()
    {
        return myName;
    }

    public static final VAlign fill = new VAlign("fill");
    public static final VAlign center = new VAlign("center");
    public static final VAlign top = new VAlign("top");
    public static final VAlign bottom = new VAlign("bottom");
    private final String myName;

}
