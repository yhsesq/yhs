// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import org.jdom.Element;
import org.jdom.Namespace;

public abstract class Component extends Element
{

    public Component()
    {
        namespace = Namespace.NO_NAMESPACE;
        setAttribute("name", "");
        setAttribute("enabled", Boolean.TRUE.toString());
        setAttribute("visible", Boolean.TRUE.toString());
        setAttribute("tooltip", "");
        setAttribute("font", "");
        setAttribute("foreground", "");
        setAttribute("background", "");
        setAttribute("width", "0");
        setAttribute("height", "0");
        setAttribute("rowspan", "1");
        setAttribute("colspan", "1");
        setAttribute("weightx", "0");
        setAttribute("weighty", "0");
        setAttribute("halign", "fill");
        setAttribute("valign", "fill");
        setAttribute("property", "");
        setAttribute("i18n", Boolean.FALSE.toString());
        setAttribute("init", "");
        setAttribute("focuslost", "");
        setAttribute("focusgained", "");
    }
}
