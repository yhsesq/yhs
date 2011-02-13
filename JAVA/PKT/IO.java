// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import net.sf.pkt.PKTXUL;
import net.sf.pkt.ffind.*;
import java.awt.Frame;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class IO
{

    public IO()
    {
    }

    public static File getFile(Frame frame, String s, boolean flag, File file)
    {
        File file1 = null;
        JFileChooser jfilechooser = new JFileChooser();jfilechooser.setAccessory(new FindAccessory(jfilechooser));
        jfilechooser.setFileFilter(new FileFilter() {

            public String getDescription()
            {
                return "XML Files";
            }

            public boolean accept(File file2)
            {
                return file2.isDirectory() || file2.getName().toLowerCase().endsWith(".xml");
            }

        });
        try
        {
            jfilechooser.setCurrentDirectory(file);
        }
        catch(Exception exception) { }
        jfilechooser.setDialogTitle(s);
        jfilechooser.setDialogType(flag ? 1 : 0);
        jfilechooser.showDialog(frame, null);
        try
        {
            file1 = jfilechooser.getSelectedFile();
        }
        catch(Exception exception1)
        {
            PKTXUL.getLogger().warning(exception1.toString());
            exception1.printStackTrace();
        }
        return file1;
    }
}
