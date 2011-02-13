// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;
import org.jdom.Element;
import org.jdom.Namespace;

// Referenced classes of package net.sf.pkt.widgets:
//            TContainer

public class header extends Element
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public header()
    {
        namespace = Namespace.NO_NAMESPACE;
        setName("header");
    }

    private static final String TAGNAME = "header";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("column");
        SUBTAGS.add("popupmenu");
    }
}
