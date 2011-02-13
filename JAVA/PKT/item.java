// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            choice

public class item extends choice
{

    public item()
    {
        setName("item");
        setAttribute("selected", Boolean.FALSE.toString());
    }

    private static final String TAGNAME = "item";
}
