// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.io.*;
import java.net.URL;
import thinlet.Thinlet;

// Referenced classes of package net.sf.pkt.dialogs:
//            ThinDialog

public final class StartupDialog extends ThinDialog
{

    public StartupDialog(Thinlet thinlet, boolean flag)
    {
        xul = "";
        this.thinlet = thinlet;
        maintain = flag;
    }

    public void setXUL(String s)
    {
        xul = s;
    }

    public void show()
        throws IOException
    {
        if(dialog == null)
        {
            BufferedInputStream bufferedinputstream = new BufferedInputStream((new URL(xul)).openStream());
            dialog = thinlet.parse(bufferedinputstream);
            bufferedinputstream.close();
        }
        thinlet.add(dialog);
        thinlet.setMethod(thinlet.find(dialog, "btn_OK"), "action", "close()", dialog, this);
    }

    private static final String UI_CLOSE_BUTTON = "btn_OK";
    private String xul;
}
