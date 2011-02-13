// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            Component, TContainer, Attr

public class menubar extends Component
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public menubar()
    {
        setName("menubar");
        setAttribute("placement", Attr.PLACEMENTS[0]);
    }

    private static final String TAGNAME = "menubar";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("menu");
    }
}
