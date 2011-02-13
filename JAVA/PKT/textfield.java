// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            Component

public class textfield extends Component
{

    public textfield()
    {
        setName("textfield");
        setAttribute("text", "");
        setAttribute("columns", "0");
        setAttribute("editable", Boolean.TRUE.toString());
        setAttribute("start", "0");
        setAttribute("end", "0");
        setAttribute("action", "");
        setAttribute("perform", "");
        setAttribute("caret", "");
        setAttribute("insert", "");
        setAttribute("remove", "");
    }

    private static final String TAGNAME = "textfield";
}
