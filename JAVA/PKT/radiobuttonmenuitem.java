// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;


// Referenced classes of package net.sf.pkt.widgets:
//            radiobutton

public class radiobuttonmenuitem extends radiobutton
{

    public radiobuttonmenuitem()
    {
        setName("checkboxmenuitem");
        setAttribute("mnemonic", "-1");
        setAttribute("accelerator", "");
    }

    private static final String TAGNAME = "checkboxmenuitem";
}