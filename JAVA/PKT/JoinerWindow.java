// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   JoinerWindow.java

package net.sf.pkt.hexed.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import net.sf.pkt.hexed.file.HexFile;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexEditor, MessageBox, FileListing, HexView

public class JoinerWindow extends Frame
    implements ActionListener
{

    public JoinerWindow()
    {
        super("Join Split parts");
 //       setIconImage(HexEditor.icon);
        setLayout(new GridLayout(1, 2));
        Panel p;
        add(p = new Panel(new BorderLayout()));
        Button b;
        p.add("North", b = new Button("Refresh & Clear"));
        b.addActionListener(this);
        p.add("Center", leftlist = new List(20));
        p.add("South", b = new Button("Add >>"));
        b.addActionListener(this);
        add(p = new Panel(new BorderLayout()));
        p.add("North", b = new Button("Save file..."));
        b.addActionListener(this);
        p.add("Center", rightlist = new List(20));
        p.add("South", b = new Button("Remove"));
        b.addActionListener(this);
        leftlist.add("[0] (dummy file with long name) (1) 0000000000000000-000000000000000F");
        pack();
        refreshView();
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
                JoinerWindow.instance = null;
            }

        });
        show();
    }

    void refreshView()
    {
        java.util.List editors = FileListing.getEditors();
        leftlist.removeAll();
        rightlist.removeAll();
        rightlist.add("---end---");
        rightlist.select(0);
        for(int i = 0; i < editors.size(); i++)
        {
            HexFile elem = ((HexEditor)editors.get(i)).getFile();
            String nm = elem.getName();
            if(nm == null)
                nm = "(dummy file)";
            if(nm == null)
                continue;
            nm = "[" + (i + 1) + "] " + nm;
            java.util.TreeSet sm = elem.getSplitMarks();
            java.util.Iterator it = sm.iterator();
            int j = 0;
            long lastIndex;
            long newIndex;
            for(lastIndex = 0L; it.hasNext(); lastIndex = newIndex)
            {
                newIndex = ((Long)it.next()).longValue();
                leftlist.add(nm + " (" + ++j + ") " + HexView.makeHexOffset(lastIndex) + "-" + HexView.makeHexOffset(newIndex));
            }

            leftlist.add(nm + " (" + ++j + ") " + HexView.makeHexOffset(lastIndex) + "-" + HexView.makeHexOffset(elem.getSize()));
        }

    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if("Add >>".equals(cmd))
        {
            if(rightlist.getSelectedIndex() != -1 && leftlist.getSelectedIndex() != -1)
                rightlist.add(leftlist.getSelectedItem(), rightlist.getSelectedIndex());
        } else
        if("Remove".equals(cmd))
        {
            if(rightlist.getSelectedIndex() != -1 && !rightlist.getSelectedItem().equals("---end---"))
            {
                int si = rightlist.getSelectedIndex();
                rightlist.remove(si);
                rightlist.select(si);
            }
        } else
        if("Refresh & Clear".equals(cmd))
            refreshView();
        else
        if("Save file...".equals(cmd))
            saveFile();
        else
            System.out.println(cmd);
    }

    private void saveFile()
    {try{
        File f;
        java.util.TreeSet newmarks;
        long offs;
        FileDialog fd = new FileDialog(this, "New Filename:", 1);
        fd.show();
        if(fd.getFile() == null)
            return;
        f = new File(fd.getDirectory(), fd.getFile());
        newmarks = new java.util.TreeSet();
        offs = 0L;
        BufferedOutputStream out;
        int i;
        out = new BufferedOutputStream(new FileOutputStream(f));
        i = 0;
while(true){
        String item;
        int j;
        if(i >= rightlist.getItemCount() - 1)
            break; 
        if(i != 0)
            newmarks.add(new Long(offs));
        item = rightlist.getItem(i);
        j = item.indexOf("] ");
        if(j == -1)
            return;
        int findex;
        System.out.println(item.substring(1, j));
        findex = Integer.parseInt(item.substring(1, j)) - 1;
        j = item.lastIndexOf(" ");
        if(j == -1)
            return;
        item = item.substring(j + 1);
        j = item.indexOf("-");
        long start = Long.parseLong(item.substring(0, j), 16);
        long end = Long.parseLong(item.substring(j + 1), 16);
        HexFile hf = ((HexEditor)FileListing.getEditors().get(findex)).getFile();
        hf.writeByteBlock(start, end, out);
        offs += end - start;
        i++;
        }
        out.flush();
        out.close();
        HexEditor he = new HexEditor();
        he.openFile(f, newmarks, false);
// break;
        }catch(IOException e){
        new MessageBox(this, e.toString(), "JHEditor - Save Joined File");}
    }

    public static void showJoiner()
    {
        if(instance == null)
            instance = new JoinerWindow();
        else
            instance.show();
    }

    static JoinerWindow instance = null;
    List leftlist;
    List rightlist;

}
