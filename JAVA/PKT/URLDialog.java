// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.io.IOException;
import thinlet.Thinlet;

// Referenced classes of package net.sf.pkt.dialogs:
//            ThinDialog

public class URLDialog extends ThinDialog
{

    public URLDialog(Thinlet thinlet, boolean flag)
    {
        surl = "";
        this.thinlet = thinlet;
        maintain = flag;
        setDefaults();
    }

    public void setDefaults()
    {
        surl = "http://pkt.sf.net/xul/demo2.xml";
    }

    public void show()
        throws IOException
    {
        if(dialog == null)
            dialog = thinlet.parse("urldialog.xml");
        thinlet.add(dialog);
        thinlet.setMethod(thinlet.find(dialog, "btn_OK"), "action", "update()", dialog, this);
        thinlet.setMethod(thinlet.find(dialog, "btn_Cancel"), "action", "close()", dialog, this);
        thinlet.setString(thinlet.find(dialog, "tf_url"), "text", surl);
    }

    public void update()
    {
        String s = thinlet.getString(thinlet.find(dialog, "tf_url"), "text");
        if(s != null)
        {
            surl = s;
            super.update();
        }
        close();
    }

    public String geturl()
    {
        return surl;
    }

    public void seturl(String s)
    {
        surl = s;
    }

    public static final String XUL = "urldialog.xml";
    private static final String DEFAULT_URL = "http://pkt.sf.net/xul/demo2.xml";
    private static final String UI_URL = "tf_url";
    private static final String UI_OK_BUTTON = "btn_OK";
    private static final String UI_CANCEL_BUTTON = "btn_Cancel";
    private String surl;
}
