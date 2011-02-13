// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            textfield

public class textarea extends textfield
{

    public textarea()
    {
        setName("textarea");
        setAttribute("rows", "0");
        setAttribute("wrap", Boolean.FALSE.toString());
        setAttribute("border", Boolean.TRUE.toString());
        removeAttribute("perform");
    }

    private static final String TAGNAME = "textarea";
}
