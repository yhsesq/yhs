// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            Component, TContainer, Attr

public class tree extends Component
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public tree()
    {
        setName("tree");
        setAttribute("selection", Attr.SELECTIONS[0]);
        setAttribute("action", "");
        setAttribute("perform", "");
        setAttribute("expanded", "");
        setAttribute("collapse", "");
        setAttribute("angle", Boolean.FALSE.toString());
        setAttribute("line", Boolean.TRUE.toString());
    }

    private static final String TAGNAME = "tree";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("node");
        SUBTAGS.add("popupmenu");
    }
}
