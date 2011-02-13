// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            label

public class radiobutton extends label
{

    public radiobutton()
    {
        setName("checkbox");
        setAttribute("name", "");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("mnemonic", "-1");
        setAttribute("alignment", "center");
        setAttribute("selected", Boolean.FALSE.toString());
        setAttribute("group", "");
        setAttribute("action", "");
    }

    private static final String TAGNAME = "checkbox";
}
