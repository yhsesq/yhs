// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import org.jdom.Element;
import org.jdom.Namespace;

public class choice extends Element
{

    public choice()
    {
        namespace = Namespace.NO_NAMESPACE;
        setName("choice");
        setAttribute("name", "");
        setAttribute("enabled", Boolean.TRUE.toString());
        setAttribute("tooltip", "");
        setAttribute("text", "");
        setAttribute("icon", "");
        setAttribute("alignment", "left");
        setAttribute("property", "");
        setAttribute("font", "");
        setAttribute("foreground", "");
        setAttribute("background", "");
    }

    private static final String TAGNAME = "choice";
}
