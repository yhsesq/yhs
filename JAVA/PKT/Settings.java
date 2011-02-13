// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.io.IOException;
import thinlet.Thinlet;

// Referenced classes of package net.sf.pkt.dialogs:
//            ThinDialog

public class Settings extends ThinDialog
{

    public Settings(Thinlet thinlet, boolean flag)
    {
        this.thinlet = thinlet;
        maintain = flag;
        setDefaults();
    }

    public void setDefaults()
    {
        panelType = true;
        utf_encoded = false;
        buildpath = "";
        thinletSubClass = "";
    }

    public void show()
        throws IOException
    {
        if(dialog == null)
            dialog = thinlet.parse("project.xml");
        thinlet.add(dialog);
        thinlet.setMethod(thinlet.find(dialog, "btn_OK"), "action", "update()", dialog, this);
        thinlet.setMethod(thinlet.find(dialog, "btn_Cancel"), "action", "close()", dialog, this);
        thinlet.setBoolean(thinlet.find(dialog, "cb_PanelType"), "selected", panelType);
        thinlet.setBoolean(thinlet.find(dialog, "cb_DialogType"), "selected", !panelType);
        thinlet.setBoolean(thinlet.find(dialog, "cb_UTF"), "selected", utf_encoded);
        thinlet.setBoolean(thinlet.find(dialog, "cb_ISO"), "selected", !utf_encoded);
        thinlet.setString(thinlet.find(dialog, "tf_BuildPath"), "text", buildpath);
        thinlet.setString(thinlet.find(dialog, "tf_Class"), "text", thinletSubClass);
    }

    public void update()
    {
        if(needsUpdate())
        {
            panelType = thinlet.getBoolean(thinlet.find(dialog, "cb_PanelType"), "selected");
            utf_encoded = thinlet.getBoolean(thinlet.find(dialog, "cb_UTF"), "selected");
            buildpath = thinlet.getString(thinlet.find(dialog, "tf_BuildPath"), "text").trim();
            thinletSubClass = thinlet.getString(thinlet.find(dialog, "tf_Class"), "text").trim();
            super.update();
        }
        close();
    }

    protected boolean needsUpdate()
    {
        boolean flag = false;
        if(panelType != thinlet.getBoolean(thinlet.find(dialog, "cb_PanelType"), "selected"))
            flag = true;
        else
        if(utf_encoded != thinlet.getBoolean(thinlet.find(dialog, "cb_UTF"), "selected"))
            flag = true;
        else
        if(!buildpath.equals(thinlet.getString(thinlet.find(dialog, "tf_BuildPath"), "text").trim()))
            flag = true;
        else
        if(!thinletSubClass.equals(thinlet.getString(thinlet.find(dialog, "tf_Class"), "text").trim()))
            flag = true;
        return flag;
    }

    public String getThinletSubClass()
    {
        return thinletSubClass;
    }

    public void setThinletSubClass(String s)
    {
        thinletSubClass = s;
    }

    public String getBuildpath()
    {
        return buildpath;
    }

    public void setBuildpath(String s)
    {
        buildpath = s;
    }

    public boolean isPanelType()
    {
        return panelType;
    }

    public void setPanelType(boolean flag)
    {
        panelType = flag;
    }

    public boolean isUTF8_encoded()
    {
        return utf_encoded;
    }

    public void setUTF8_encoding(boolean flag)
    {
        utf_encoded = flag;
    }

    public static final String XUL = "project.xml";
    private static final String UI_CB_PANEL = "cb_PanelType";
    private static final String UI_CB_DIALOG = "cb_DialogType";
    private static final String UI_CB_UTF = "cb_UTF";
    private static final String UI_CB_ISO = "cb_ISO";
    private static final String UI_BUILDPATH = "tf_BuildPath";
    private static final String UI_CLASS = "tf_Class";
    private static final String UI_OK_BUTTON = "btn_OK";
    private static final String UI_CANCEL_BUTTON = "btn_Cancel";
    private boolean panelType;
    private boolean utf_encoded;
    private String buildpath;
    private String thinletSubClass;
}
