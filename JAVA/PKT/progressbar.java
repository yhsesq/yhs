// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            Component, Attr

public class progressbar extends Component
{

    public progressbar()
    {
        setName("progressbar");
        setAttribute("orientation", Attr.ORIENTATIONS[0]);
        setAttribute("minimum", "0");
        setAttribute("maximum", "100");
        setAttribute("value", "0");
    }

    private static final String TAGNAME = "progressbar";
}
