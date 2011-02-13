// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   FileListing.java

package net.sf.pkt.hexed.gui;

import java.util.ArrayList;
import java.util.List;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            JoinerWindow, HexEditor

public class FileListing
{

    public FileListing()
    {
    }

    public static synchronized void registerEditor(HexEditor file)
    {
        if(!editors.contains(file))
        {
            editors.add(file);
            if(JoinerWindow.instance != null)
                JoinerWindow.instance.refreshView();
        }
    }

    public static synchronized void unregisterEditor(HexEditor file)
    {
        if(editors.contains(file))
        {
            editors.remove(file);
            if(JoinerWindow.instance != null)
                JoinerWindow.instance.refreshView();
        }
    }

    public static void refresh()
    {
        if(JoinerWindow.instance != null)
            JoinerWindow.instance.refreshView();
    }

    public static synchronized List getEditors()
    {
        return editors;
    }

    public static List editors = new ArrayList();

}
