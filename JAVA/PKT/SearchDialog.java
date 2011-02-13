// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.io.IOException;
import thinlet.Thinlet;

// Referenced classes of package net.sf.pkt.dialogs:
//            ThinDialog

public class SearchDialog extends ThinDialog
{

    public SearchDialog(Thinlet thinlet, boolean flag)
    {
        stext = "";
        this.thinlet = thinlet;
        maintain = flag;
        setDefaults();
    }

    public void setDefaults()
    {
        stext = "";
        partial = true;
        all = true;
    }

    public void show()
        throws IOException
    {
        if(dialog == null)
            dialog = thinlet.parse("search.xml");
        thinlet.add(dialog);
        thinlet.setMethod(thinlet.find(dialog, "btn_OK"), "action", "update()", dialog, this);
        thinlet.setMethod(thinlet.find(dialog, "btn_Cancel"), "action", "close()", dialog, this);
        thinlet.setString(thinlet.find(dialog, "tf_Text"), "text", stext);
        thinlet.setBoolean(thinlet.find(dialog, "chk_partial"), "selected", partial);
        thinlet.setBoolean(thinlet.find(dialog, "chk_all"), "selected", all);
        thinlet.setBoolean(thinlet.find(dialog, "chk_names"), "selected", !all);
    }

    public void update()
    {
        String s = thinlet.getString(thinlet.find(dialog, "tf_Text"), "text");
        all = thinlet.getBoolean(thinlet.find(dialog, "chk_all"), "selected");
        partial = thinlet.getBoolean(thinlet.find(dialog, "chk_partial"), "selected");
        if(s != null)
        {
            s = s.trim();
            if(0 < s.length())
            {
                stext = s;
                thinlet.setBoolean(thinlet.find("mi_findnext"), "enabled", true);
                super.update();
            }
        }
        close();
    }

    public String getSearchString()
    {
        return stext;
    }

    public boolean isAll()
    {
        return all;
    }

    public boolean isPartial()
    {
        return partial;
    }

    public static final String XUL = "search.xml";
    private static final String UI_SEARCH_TEXT = "tf_Text";
    private static final String UI_PARTIAL_CHECK = "chk_partial";
    private static final String UI_SEARCH_NAMES = "chk_names";
    private static final String UI_SEARCH_ALL = "chk_all";
    private static final String UI_OK_BUTTON = "btn_OK";
    private static final String UI_CANCEL_BUTTON = "btn_Cancel";
    private String stext;
    private boolean partial;
    private boolean all;
}
