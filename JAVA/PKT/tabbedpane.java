// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            Component, TContainer, Attr

public class tabbedpane extends Component
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public tabbedpane()
    {
        setName("tabbedpane");
        setAttribute("placement", Attr.PLACEMENTS[0]);
        setAttribute("selected", "0");
        setAttribute("action", "");
    }

    private static final String TAGNAME = "tabbedpane";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("tab");
        SUBTAGS.add("popupmenu");
    }
}
