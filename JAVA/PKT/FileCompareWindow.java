// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   FileCompareWindow.java

package net.sf.pkt.hexed.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import net.sf.pkt.hexed.file.HexFile;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexEditor, FileListing

public class FileCompareWindow extends Frame
    implements ActionListener, ItemListener
{

    public FileCompareWindow(Frame startedFrom)
    {
        super("Compare files");
        toFocus = new HexEditor[2];
        ignoreNextFocus = false;
        setBackground(Color.lightGray);
        editors = FileListing.getEditors();
        setLayout(cl = new CardLayout());
        Panel p;
        add("Start", p = new Panel(new BorderLayout()));
        p.add("North", p = new Panel(new GridLayout(4, 1)));
        p.add(file1 = makeChoice());
        p.add(file2 = makeChoice());
        p.add(align = new Checkbox("Align windows", true));
        Button b;
        p.add(b = new Button("Compare!"));
        b.addActionListener(this);
        add("Results", results = new List());
        results.add("Please click Compare first! 00000000");
        results.addItemListener(this);
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        Insets i = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        setLocation(startedFrom.getWidth() + startedFrom.getX(), i.top);
        pack();
        setSize(getWidth(), h - i.top - i.bottom);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
            }

            public void windowActivated(WindowEvent e)
            {
                if(ignoreNextFocus)
                    ignoreNextFocus = false;
                else
                    refocus();
            }

        });
        show();
    }

    private Choice makeChoice()
    {
        Choice c = new Choice();
        for(java.util.Iterator i = editors.iterator(); i.hasNext();)
        {
            String name = ((HexEditor)i.next()).getFile().getName();
            if(name == null)
                c.add("(dummy file)");
            else
                c.add(name);
        }

        return c;
    }

    public void actionPerformed(ActionEvent e)
    {
        he1 = (HexEditor)editors.get(file1.getSelectedIndex());
        hf1 = he1.getFile();
        he2 = (HexEditor)editors.get(file2.getSelectedIndex());
        hf2 = he2.getFile();
        if(align.getState())
        {
            int xMin = Math.min(he1.getX(), he2.getX());
            int xMe = xMin + Math.max(he1.getWidth(), he2.getWidth());
            if(xMe < getX())
                xMe = getX();
            int width = xMe - xMin;
            int height = getHeight() / 2;
            setLocation(xMe, getY());
            he1.setEditorBounds(xMin, getY(), width, height);
            he2.setEditorBounds(xMin, getY() + height, width, height);
            toFocus[0] = he1;
            toFocus[1] = he2;
            refocus();
        } else
        {
            toFocus[0] = toFocus[1] = null;
        }
        results.removeAll();
        int minMatch = 16;
        int maxResync = 1024;
        long p1 = 0L;
        long p2 = 0L;
        do
        {
            if(p1 >= hf1.getSize() || p2 >= hf2.getSize())
                break;
            if(matches(p1, p2, minMatch))
            {
                long i;
                for(i = minMatch; p1 + i < hf1.getSize() && p2 + i < hf2.getSize() && hf1.getByte(p1 + i) == hf2.getByte(p2 + i); i++);
                results.add("Identical [" + p1 + "-" + (p1 + i) + "|" + p2 + "-" + (p2 + i) + "]");
                p1 += i;
                p2 += i;
                continue;
            }
            long poi1 = 0L;
            long poi2 = 0L;
            poi1 = 0L;
label0:
            do
            {
                if(poi1 >= (long)maxResync)
                    break;
                poi2 = poi1;
                if(matches(p1 + poi1, p2 + poi1, minMatch))
                    break;
                for(poi2 = poi1 - 1L; poi2 >= 0L; poi2--)
                {
                    if(matches(p1 + poi1, p2 + poi2, minMatch))
                        break label0;
                    if(matches(p1 + poi2, p2 + poi1, minMatch))
                    {
                        long h = poi1;
                        poi1 = poi2;
                        poi2 = h;
                        break label0;
                    }
                }

                poi1++;
            } while(true);
            if(poi1 >= (long)maxResync)
                break;
            results.add(makeChanged(p1, p1 + poi1, p2, p2 + poi2));
            p1 += poi1;
            p2 += poi2;
        } while(true);
        if(p1 != hf1.getSize() || p2 != hf2.getSize())
            results.add(makeChanged(p1, hf1.getSize(), p2, hf2.getSize()));
        cl.show(this, "Results");
    }

    public void itemStateChanged(ItemEvent e)
    {
        String it = results.getSelectedItem();
        if(it != null)
        {
            int pos = it.indexOf("[");
            java.util.StringTokenizer st = new java.util.StringTokenizer(it.substring(pos + 1, it.length() - 1), "|-");
            long from1 = Long.parseLong(st.nextToken());
            long to1 = Long.parseLong(st.nextToken());
            long from2 = Long.parseLong(st.nextToken());
            long to2 = Long.parseLong(st.nextToken());
            he1.notifySelection(from1, to1);
            he2.notifySelection(from2, to2);
        }
    }

    private String makeChanged(long f1, long t1, long f2, long t2)
    {
        if(f1 == t1 || f2 == t2)
            return "Added/Removed [" + f1 + "-" + t1 + "|" + f2 + "-" + t2 + "]";
        if(t1 - f1 == t2 - f2)
            return "Replaced [" + f1 + "-" + t1 + "|" + f2 + "-" + t2 + "]";
        else
            return "Changed [" + f1 + "-" + t1 + "|" + f2 + "-" + t2 + "]";
    }

    private boolean matches(long p1, long p2, long len)
    {
        if(p1 + len > hf1.getSize() || p2 + len > hf2.getSize())
            return false;
        for(int i = 0; (long)i < len; i++)
            if(hf1.getByte(p1 + (long)i) != hf2.getByte(p2 + (long)i))
                return false;

        return true;
    }

    private void refocus()
    {
        for(int i = 0; i < 2; i++)
            if(toFocus[i] != null)
                toFocus[i].requestFocus();

        ignoreNextFocus = true;
        requestFocus();
    }

    Choice file1;
    Choice file2;
    Checkbox align;
    java.util.List editors;
    List results;
    CardLayout cl;
    HexFile hf1;
    HexFile hf2;
    HexEditor he1;
    HexEditor he2;
    HexEditor toFocus[];
    boolean ignoreNextFocus;

}
