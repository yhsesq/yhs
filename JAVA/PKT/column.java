// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            Component, Attr

public class column extends Component
{

    public column()
    {
        setName("column");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("mnemonic", "-1");
        setAttribute("alignment", Attr.HORIZONTALS[2]);
        setAttribute("width", "80");
        setAttribute("property", "");
        setAttribute("sort", Attr.SORTINGS[0]);
    }

    private static final String TAGNAME = "column";
}
