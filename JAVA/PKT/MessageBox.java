// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   MessageBox.java

package net.sf.pkt.hexed.gui;

import java.awt.*;
import java.awt.event.*;

public class MessageBox extends Dialog
    implements ActionListener
{

    public MessageBox(Frame parent, String message, String title)
    {
        super(parent, title, true);
        clicked = "Cancel";
        setup(message, false);
    }

    public MessageBox(Frame parent, String message, String title, boolean ync)
    {
        super(parent, title, true);
        clicked = "Cancel";
        setup(message, ync);
    }

    public MessageBox(Dialog parent, String message, String title)
    {
        super(parent, title, true);
        clicked = "Cancel";
        setup(message, false);
    }

    private void setup(String message, boolean ync)
    {
        add("Center", new Label(message));
        Panel p;
        add("South", p = new Panel());
        if(ync)
        {
            Button b;
            p.add(b = new Button("Yes"));
            b.addActionListener(this);
            p.add(b = new Button("No"));
            b.addActionListener(this);
            p.add(b = new Button("Cancel"));
            b.addActionListener(this);
        } else
        {
            Button b;
            p.add(b = new Button("OK"));
            b.addActionListener(this);
        }
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
            }

        });
        pack();
        Dimension scs = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((scs.width - getWidth()) / 2, (scs.height - getHeight()) / 2);
        show();
    }

    public void actionPerformed(ActionEvent e)
    {
        clicked = e.getActionCommand();
        dispose();
    }

    public String clicked;
}
