// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.io.IOException;
import thinlet.Thinlet;

// Referenced classes of package net.sf.pkt.dialogs:
//            ThinDialog

public class AboutDialog extends ThinDialog
{

    public AboutDialog(Thinlet thinlet, boolean flag)
    {
        this.thinlet = thinlet;
        maintain = flag;
    }

    public void show()
        throws IOException
    {
        if(dialog == null)
            dialog = thinlet.parse("about.xml");
        thinlet.add(dialog);
        thinlet.setString(thinlet.find(dialog, "ta_About"), "text", CREDITS);
        thinlet.setMethod(thinlet.find(dialog, "btn_Close"), "action", "close()", dialog, this);
    }

    private static final String XUL = "about.xml";
    private static final String UI_CLOSE_BUTTON = "btn_Close";
    private static final String UI_TEXT_AREA = "ta_About";
    private static String CREDITS;

    static 
    {
        CREDITS = "";
        CREDITS = CREDITS + "PKTXUL - GPL XUL Editor for Thinlet\n";
        CREDITS = CREDITS + "Version: PKTXUL 0.1 (GPL Release) ";
        CREDITS = CREDITS + "\nLicense : http://www.gnu.org/gpl\n(C) 2004 yhs at users.sf.net\n";
        CREDITS = CREDITS + "\n";
    }
}
