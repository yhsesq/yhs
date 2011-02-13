// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   HexEditor.java

package net.sf.pkt.hexed.gui;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.*;
import java.util.Collection;
import java.util.TreeSet;
import net.sf.pkt.hexed.file.HexFile;
import net.sf.pkt.hexed.file.HexFileListener;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexView, MessageBox, ConvertWindow, GoToDialog, 
//            FileCompareWindow, FileListing, JoinerWindow, FileDropTargetAdapter

public class HexEditor extends Frame
    implements ActionListener, ItemListener, HexFileListener
{

    public HexEditor(String filename, boolean ro)
    {
        this(new File(filename), ro);
    }

    public HexEditor(File file, boolean ro)
    {
        this(false);
        openFile(file, null, ro);
        show();
        hv.requestFocus();
    }

    public HexEditor()
    {
        this(true);
    }

    public HexEditor(boolean initshow)
    {
        super("PKTHexEditor 1.1");
        file = new HexFile(this);
        FileDropTargetAdapter fdta = new FileDropTargetAdapter() {

            public boolean drop(File f[])
            {
                if(f.length == 1)
                {
                    openFile(f[0], null, false);
                    hv.repaint();
                } else
                {
                    for(int i = 0; i < f.length; i++)
                        new HexEditor(f[i], false);

                }
                return true;
            }

        };
        setDropTarget(new DropTarget(this, 1, fdta, true, null));
        sb = new Scrollbar(1, 0, 25, 0, (int)file.getSize() / 16 + 1);
        sb.setFocusable(false);
        add("Center", hv = new HexView(file, sb, this));
        add("East", sb);
        sb.addAdjustmentListener(hv);
        Panel p;
        add("South", p = new Panel(new BorderLayout()));
        p.setBackground(Color.lightGray);
        Panel p2;
        p.add("North", p2 = new Panel() {

            public Dimension getPreferredSize()
            {
                return new Dimension(5, 5);
            }

        });
        p2.setBackground(Color.black);
        p.add("West", type = ConvertWindow.getTypeChoice());
        type.select(3);
        p.add("Center", p2 = new Panel(new BorderLayout()));
        p2.add("West", subType = ConvertWindow.getSubTypeChoice());
        p2.add("Center", convertresult = new TextField(""));
        p2.add("East", offsetlabel = new Label("Offset: XXXXXXXXYYYYYYYY"));
        convertresult.setEditable(false);
        type.addItemListener(this);
        subType.addItemListener(this);
        MenuBar mb = new MenuBar();
        Menu m;
        mb.add(m = new Menu("File", true));
        MenuItem mi;
        m.add(mi = new MenuItem("New Window", new MenuShortcut(73)));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("New"));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Open", new MenuShortcut(79)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Open read-only", new MenuShortcut(79, true)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Save", new MenuShortcut(83)));
        mi.addActionListener(this);
        saveItem = mi;
        mi.setEnabled(false);
        m.add(mi = new MenuItem("Save as", new MenuShortcut(83, true)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Revert"));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("Exit", new MenuShortcut(81)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Exit all instances", new MenuShortcut(81, true)));
        mi.addActionListener(this);
        mb.add(m = new Menu("Edit"));
        m.add(mi = new MenuItem("Cut", new MenuShortcut(88)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Copy", new MenuShortcut(67)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Paste", new MenuShortcut(86)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Paste (insert)", new MenuShortcut(86, true)));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("Find", new MenuShortcut(70)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Next", new MenuShortcut(78)));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("Insert"));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Delete"));
        mi.addActionListener(this);
        mb.add(m = new Menu("View"));
        m.add(mi = new MenuItem("Go to position...", new MenuShortcut(71)));
        mi.addActionListener(this);
        m.addSeparator();
        for(int i = 0; i < 9; i++)
        {
            m.add(mi = new MenuItem("Scroll to " + (char)(49 + i) + "0%", new MenuShortcut(49 + i)));
            mi.setActionCommand("" + (char)(49 + i));
            mi.addActionListener(hv);
        }

        mb.add(m = new Menu("Tools"));
        m.add(mi = new MenuItem("Converter", new MenuShortcut(67, true)));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("Add/Remove Split mark", new MenuShortcut(77)));
        mi.addActionListener(this);
        m.add(mi = new MenuItem("Join Split parts to file...", new MenuShortcut(74)));
        mi.addActionListener(this);
        m.addSeparator();
        m.add(mi = new MenuItem("Compare Files..."));
        mi.addActionListener(this);
        setMenuBar(mb);
        FileListing.registerEditor(this);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                if(savedOk())
                {
                    FileListing.unregisterEditor(HexEditor.this);
                    dispose();
                    // if(--HexEditor.instanceCount == 0)
                    //      System.exit(0);
                }
            }

        });
        pack();
        instanceCount++;
        setLocation(instanceCount * 30, instanceCount * 30);
        if(initshow)
        {
            show();
            hv.requestFocus();
        }
    }

    private boolean savedOk()
    {
        if(file.isChanged() && !file.isReadOnly())
        {
            MessageBox mb = new MessageBox(this, "Do you want to save changes?", "PKTHexEditor", true);
            if(mb.clicked.equals("Yes"))
                file.save();
            else
            if(mb.clicked.equals("Cancel"))
                return false;
        }
        return true;
    }

    public void itemStateChanged(ItemEvent e)
    {
        refreshStatus();
    }

    public void refreshStatus()
    {
        byte what[] = hv.getStatusSource();
        convertresult.setText(ConvertWindow.bytes2type(what, type.getSelectedIndex(), subType.getSelectedIndex()));
        offsetlabel.setText("Offset: " + hv.makeHexOffset(hv.cursor));
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if("Exit all instances".equals(cmd))
        {
            if(savedOk())
                dispose();
        } else
        if("Exit".equals(cmd))
        {
            if(savedOk())
            {
                dispose();
                if(--instanceCount == 0)
                  {}//  System.exit(0);
            }
        } else
        if("New Window".equals(cmd))
            new HexEditor();
        else
        if("New".equals(cmd))
        {
            file.close();
            hv.goToStart();
            setTitle("PKTHexEditor 1.1");
            setReadOnly(true);
        } else
        if("Save".equals(cmd))
        {
            if(!file.isReadOnly())
                file.save();
        } else
        if("Save as".equals(cmd))
        {
            FileDialog fd = new FileDialog(this, "Save as", 1);
            fd.show();
            if(fd.getFile() != null)
            {
                File f = new File(fd.getDirectory(), fd.getFile());
                saveAs(f);
            }
        } else
        if("Revert".equals(cmd))
            file.revert();
        else
        if("Open".equals(cmd))
        {
            FileDialog fd = new FileDialog(this, "Select file");
            fd.show();
            if(fd.getFile() != null)
            {
                File f = new File(fd.getDirectory(), fd.getFile());
                openFile(f, null, false);
            }
        } else
        if("Open read-only".equals(cmd))
        {
            FileDialog fd = new FileDialog(this, "Select file");
            fd.show();
            if(fd.getFile() != null)
            {
                File f = new File(fd.getDirectory(), fd.getFile());
                openFile(f, null, true);
            }
        } else
        if("Cut".equals(cmd))
            hv.copy(true);
        else
        if("Copy".equals(cmd))
            hv.copy(false);
        else
        if("Paste".equals(cmd))
            hv.paste(false);
        else
        if("Paste (insert)".equals(cmd))
            hv.paste(true);
        else
        if("Find".equals(cmd))
            hv.doFind(true, this);
        else
        if("Next".equals(cmd))
            hv.doFind(false, this);
        else
        if("Insert".equals(cmd))
            hv.insertByte();
        else
        if("Delete".equals(cmd))
            hv.deleteByte();
        else
        if("Converter".equals(cmd))
            new ConvertWindow(this);
        else
        if("Go to position...".equals(cmd))
            new GoToDialog(this, hv.cursor);
        else
        if("Add/Remove Split mark".equals(cmd))
            hv.toggleSplitMark();
        else
        if("Join Split parts to file...".equals(cmd))
            JoinerWindow.showJoiner();
        else
        if("Compare Files...".equals(cmd))
        {
            new FileCompareWindow(this);
        } else
        {
            System.out.println("-");
            (new Throwable()).printStackTrace();
        }
        hv.repaint();
    }

    private void setReadOnly(boolean state)
    {
        if(state != file.isReadOnly())
        {
            throw new RuntimeException("Oops");
        } else
        {
            saveItem.setEnabled(!state);
            return;
        }
    }

public void doExit(){
                    FileListing.unregisterEditor(HexEditor.this);
                    dispose();
}

    public boolean openFile(File f, Collection marks, boolean readonly)
    {
        int result = file.open(f, readonly);
        if(result > 0)
        {
            readonly = result == 1;
            setTitle(f.getName() + " " + (readonly ? "[read only]" : "") + " - PKTHexEditor " + "1.1");
            hv.goToStart();
            setReadOnly(readonly);
            if(marks != null)
                file.getSplitMarks().addAll(marks);
            return true;
        } else
        {
            file.close();
            setTitle("PKTHexEditor 1.1");
            setReadOnly(true);
            hv.goToStart();
            return false;
        }
    }

    public void saveAs(File f)
    {
        try
        {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            file.writeByteBlock(0L, file.getSize(), out);
            out.flush();
            out.close();
            openFile(f, null, false);
        }
        catch(IOException e)
        {
            new MessageBox(this, e.toString(), "PKTHexEditor - Save As");
        }
    }

    public void update(Graphics g)
    {
        super.update(g);
        hv.update(hv.getGraphics());
    }

    public void showError(String message, String title)
    {
        new MessageBox(this, message, title);
    }

    public void notifySelection(long from, long to)
    {
        hv.notifySelection(from, to);
    }

    public void setEditorBounds(int x, int y, int width, int height)
    {
        setLocation(x, y);
        setSize(width, height);
        validate();
    }

    public HexFile getFile()
    {
        return file;
    }

    public void refreshFileList()
    {
        FileListing.refresh();
    }

    static Class _mthclass$(String x0)
    {
        try{return Class.forName(x0);}
        catch(ClassNotFoundException x1){
        throw new NoClassDefFoundError(x1.getMessage());}
    }

    HexView hv;
    HexFile file;
    Scrollbar sb;
    MenuItem saveItem;
    Choice type;
    Choice subType;
    TextField convertresult;
    Label offsetlabel;
    static int instanceCount = 0;

    static 
    {
//        icon = Toolkit.getDefaultToolkit().createImage((net.sf.pkt.hexed.Main.class).getResource("/res/icon.gif"));
    }

}
