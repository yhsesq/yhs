// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            Component, Attr

public class label extends Component
{

    public label()
    {
        setName("label");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("mnemonic", "-1");
        setAttribute("alignment", Attr.HORIZONTALS[2]);
        setAttribute("for", "");
    }

    private static final String TAGNAME = "label";
}
