// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;
import org.jdom.Namespace;

// Referenced classes of package net.sf.pkt.widgets:
//            Component, TContainer

public class panel extends Component
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public panel()
    {
        namespace = Namespace.NO_NAMESPACE;
        setName("panel");
        setAttribute("columns", "0");
        setAttribute("top", "0");
        setAttribute("left", "0");
        setAttribute("bottom", "0");
        setAttribute("right", "0");
        setAttribute("gap", "0");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("border", Boolean.FALSE.toString());
        setAttribute("scrollable", Boolean.FALSE.toString());
    }

    private static final String TAGNAME = "panel";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("label");
        SUBTAGS.add("button");
        SUBTAGS.add("togglebutton");
        SUBTAGS.add("checkbox");
        SUBTAGS.add("radiobutton");
        SUBTAGS.add("combobox");
        SUBTAGS.add("spinbox");
        SUBTAGS.add("slider");
        SUBTAGS.add("progressbar");
        SUBTAGS.add("textfield");
        SUBTAGS.add("parameter");
        SUBTAGS.add("passwordfield");
        SUBTAGS.add("textarea");
        SUBTAGS.add("panel");
        SUBTAGS.add("splitpane");
        SUBTAGS.add("tabbedpane");
        SUBTAGS.add("list");
        SUBTAGS.add("table");
        SUBTAGS.add("tree");
        SUBTAGS.add("separator");
        SUBTAGS.add("menubar");
        SUBTAGS.add("popupmenu");
    }
}
