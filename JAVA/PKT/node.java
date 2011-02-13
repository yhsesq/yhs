// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            choice, TContainer

public class node extends choice
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public node()
    {
        setName("node");
        setAttribute("selected", Boolean.FALSE.toString());
        setAttribute("expanded", Boolean.FALSE.toString());
    }

    private static final String TAGNAME = "node";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("node");
    }
}
