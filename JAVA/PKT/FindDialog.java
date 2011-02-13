// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   FindDialog.java

package net.sf.pkt.hexed.gui;

import java.awt.*;
import java.awt.event.*;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            ConvertWindow, HexView

public class FindDialog extends Dialog
    implements ActionListener, ItemListener, TextListener
{

    public FindDialog(Frame owner)
    {
        super(owner, "Find", true);
        setLayout(new GridLayout(3, 1, 5, 5));
        Panel p;
        add(p = new Panel(new BorderLayout()));
        p.add("West", typ = ConvertWindow.getTypeChoice());
        typ.addItemListener(this);
        Panel p2;
        p.add("Center", p2 = new Panel(new BorderLayout()));
        p2.add("West", typ2 = ConvertWindow.getSubTypeChoice());
        typ2.addItemListener(this);
        p2.add("Center", tf = new TextField(50));
        tf.addTextListener(this);
        tf.addActionListener(this);
        add(preview = new TextField(50));
        preview.setEditable(false);
        add(p = new Panel(new GridLayout(1, 3, 0, 0)));
        p.add(ignoreCase = new Checkbox("Ignore case"));
        Button b;
        p.add(b = new Button("OK"));
        b.addActionListener(this);
        p.add(b = new Button("Cancel"));
        b.addActionListener(this);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
            }

            public void windowOpened(WindowEvent e)
            {
                tf.requestFocus();
            }

        });
        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if(e.getSource() == tf || "OK".equals(cmd))
        {
            buildArray();
            result = array;
            ignorecase = ignoreCase.getState();
            dispose();
        } else
        if("Cancel".equals(cmd))
        {
            result = null;
            ignorecase = false;
            dispose();
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        buildArray();
    }

    public void textValueChanged(TextEvent e)
    {
        buildArray();
    }

    private void buildArray()
    {
        String text = tf.getText();
        array = ConvertWindow.type2bytes(text, typ.getSelectedIndex(), typ2.getSelectedIndex());
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < array.length; i++)
            sb.append(HexView.makeHex(array[i])).append(" ");

        preview.setText(sb.toString());
    }

    private TextField tf;
    private TextField preview;
    private Choice typ;
    private Choice typ2;
    private Checkbox ignoreCase;
    boolean ignorecase;
    byte result[];
    private byte array[];

}
