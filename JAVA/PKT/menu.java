// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

// Referenced classes of package net.sf.pkt.widgets:
//            choice, TContainer

public class menu extends choice
    implements TContainer
{

    public Vector getSubTagNames()
    {
        return SUBTAGS;
    }

    public menu()
    {
        setName("menu");
        setAttribute("mnemonic", "-1");
    }

    private static final String TAGNAME = "menu";
    private static final Vector SUBTAGS;

    static 
    {
        SUBTAGS = new Vector();
        SUBTAGS.add("menu");
        SUBTAGS.add("menuitem");
        SUBTAGS.add("checkboxmenuitem");
        SUBTAGS.add("radiobuttonmenuitem");
        SUBTAGS.add("separator");
    }
}
