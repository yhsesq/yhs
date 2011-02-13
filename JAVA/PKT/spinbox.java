// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            Component

public class spinbox extends Component
{

    public spinbox()
    {
        setName("spinbox");
        setAttribute("text", "");
        setAttribute("columns", "0");
        setAttribute("editable", Boolean.TRUE.toString());
        setAttribute("action", "");
        setAttribute("minimum", String.valueOf(0x80000000));
        setAttribute("maximum", String.valueOf(0x7fffffff));
        setAttribute("value", "0");
        setAttribute("step", "1");
    }

    private static final String TAGNAME = "spinbox";
}
