// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            textfield, TContainer

public class combobox extends textfield
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public combobox()
    {
        setName("combobox");
        setAttribute("icon", "");
        setAttribute("selected", "-1");
        setAttribute("action", "");
    }

    private static final String TAGNAME = "combobox";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("choice");
    }
}
