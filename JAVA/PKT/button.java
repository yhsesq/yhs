// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            label, Attr

public class button extends label
{

    public button()
    {
        setName("button");
        setAttribute("name", "");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("mnemonic", "-1");
        setAttribute("alignment", Attr.ALIGNMENT[0]);
        setAttribute("action", "");
        setAttribute("type", "normal");
    }

    private static final String TAGNAME = "button";
}
