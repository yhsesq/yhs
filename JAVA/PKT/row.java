// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;
import org.jdom.Element;
import org.jdom.Namespace;

// Referenced classes of package net.sf.pkt.widgets:
//            TContainer

public class row extends Element
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public row()
    {
        namespace = Namespace.NO_NAMESPACE;
        setName("row");
        setAttribute("selected", Boolean.FALSE.toString());
        setAttribute("property", "");
        setAttribute("perform", "");
    }

    private static final String TAGNAME = "row";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("cell");
        SUBTAGS.add("popupmenu");
    }
}
