// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   GoToDialog.java

package net.sf.pkt.hexed.gui;

import java.awt.*;
import java.awt.event.*;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexEditor, HexView

public class GoToDialog extends Dialog
    implements ActionListener
{

    public GoToDialog(HexEditor parent, long offset)
    {
        super(parent, "Go to position", true);
        this.parent = parent;
        setLayout(new GridLayout(3, 1));
        Panel p;
        add(p = new Panel(new BorderLayout()));
        p.add("West", new Label("Offset:"));
        p.add("Center", postf = new TextField(Long.toString(offset, 16)));
        postf.addActionListener(this);
        add(p = new Panel(new BorderLayout()));
        p.add("West", new Label("Mark length:"));
        p.add("Center", sizetf = new TextField("0"));
        sizetf.addActionListener(this);
        add(p = new Panel(new FlowLayout()));
        p.add(ok = new Button("OK"));
        ok.addActionListener(this);
        p.add(cancel = new Button("Cancel"));
        cancel.addActionListener(this);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
            }

        });
        pack();
        show();
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == cancel)
            dispose();
        else
            try
            {
                long wohin = Long.parseLong(postf.getText(), 16);
                long wieweit = Long.parseLong(sizetf.getText(), 16);
                if(parent.hv.goTo(wohin, wieweit))
                    dispose();
            }
            catch(NumberFormatException ex)
            {
                postf.setText(ex.toString());
                return;
            }
    }

    HexEditor parent;
    TextField postf;
    TextField sizetf;
    Button ok;
    Button cancel;
}
