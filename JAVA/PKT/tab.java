// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            choice, TContainer

public class tab extends choice
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public tab()
    {
        setName("tab");
        setAttribute("text", "");
        setAttribute("mnemonic", "-1");
    }

    private static final String TAGNAME = "tab";
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
        SUBTAGS.add("passwordfield");
        SUBTAGS.add("textarea");
        SUBTAGS.add("panel");
        SUBTAGS.add("splitpane");
        SUBTAGS.add("tabbedpane");
        SUBTAGS.add("list");
        SUBTAGS.add("table");
        SUBTAGS.add("tree");
        SUBTAGS.add("separator");
        SUBTAGS.add("popupmenu");
    }
}
