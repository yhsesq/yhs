// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class FontChooser extends JDialog
{

    private FontChooser(Frame frame, boolean flag)
    {
        super(frame, flag);
        currentFont = null;
        currentStyle = -1;
        currentSize = -1;
        ok = false;
        initComponents();
        setListValues(jFontList, GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        setListValues(jStyleList, styleList);
        setListValues(jSizeList, sizeList);
        setCurrentFont(jSample.getFont());
        pack();
    }

    private FontChooser(Frame frame, boolean flag, Font font)
    {
        this(frame, flag);
        setCurrentFont(font);
    }

    private void setListValues(JList jlist, String as[])
    {
        if(jlist.getModel() instanceof DefaultListModel)
        {
            DefaultListModel defaultlistmodel = (DefaultListModel)jlist.getModel();
            defaultlistmodel.removeAllElements();
            for(int i = 0; i < as.length; i++)
                defaultlistmodel.addElement(as[i]);

        }
    }

    private void setSampleFont()
    {
        if(currentFont != null && currentStyle >= 0 && currentSize > 0)
            jSample.setFont(new Font(currentFont, currentStyle, currentSize));
    }

    private String styleToString(int i)
    {
        String s = "";
        if((i & 1) == 1)
        {
            if(s.length() > 0)
                s = s + ",";
            s = s + "Bold";
        }
        if((i & 2) == 2)
        {
            if(s.length() > 0)
                s = s + ",";
            s = s + "Italic";
        }
        if(s.length() <= 0 && (i & 0) == 0)
            s = "Plain";
        return s;
    }

    public Font getCurrentFont()
    {
        return jSample.getFont();
    }

    public void setCurrentFont(Font font)
    {
        if(font == null)
            font = jSample.getFont();
        jFont.setText(font.getName());
        jFontActionPerformed(null);
        jStyle.setText(styleToString(font.getStyle()));
        jStyleActionPerformed(null);
        jSize.setText(Integer.toString(font.getSize()));
        jSizeActionPerformed(null);
    }

    public static Font showDialog(Frame frame, String s, Font font)
    {
        FontChooser fontchooser = new FontChooser(frame, true, font);
        Point point = frame.getLocation();
        Dimension dimension = frame.getSize();
        Dimension dimension1 = fontchooser.getSize();
        int i = point.x + (dimension.width - dimension1.width) / 2;
        int j = point.y + (dimension.height - dimension1.height) / 2;
        if(i < 0)
            i = 0;
        if(j < 0)
            j = 0;
        if(s != null)
            fontchooser.setTitle(s);
        fontchooser.setLocation(i, j);
        fontchooser.setVisible(true);
        Font font1 = null;
        if(fontchooser.ok)
            font1 = fontchooser.getCurrentFont();
        fontchooser.dispose();
        return font1;
    }

    private void initComponents()
    {
        jPanel3 = new JPanel();
        jFont = new JTextField();
        jScrollPane1 = new JScrollPane();
        jFontList = new JList();
        jPanel4 = new JPanel();
        jStyle = new JTextField();
        jScrollPane2 = new JScrollPane();
        jStyleList = new JList();
        jPanel5 = new JPanel();
        jSize = new JTextField();
        jScrollPane3 = new JScrollPane();
        jSizeList = new JList();
        jPanel1 = new JPanel();
        jScrollPane4 = new JScrollPane();
        jSample = new JTextArea();
        jButtons = new JPanel();
        jOk = new JButton();
        jCancel = new JButton();
        jLabel6 = new JLabel();
        getContentPane().setLayout(new GridBagLayout());
        setTitle("Font Chooser");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent windowevent)
            {
                closeDialog(windowevent);
            }

        });
        jPanel3.setLayout(new GridBagLayout());
        jPanel3.setBorder(new TitledBorder(new EtchedBorder(), " Font "));
        jFont.setColumns(24);
        jFont.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                jFontActionPerformed(actionevent);
            }

        });
        GridBagConstraints gridbagconstraints1 = new GridBagConstraints();
        gridbagconstraints1.gridwidth = 0;
        gridbagconstraints1.fill = 2;
        gridbagconstraints1.insets = new Insets(0, 3, 0, 3);
        gridbagconstraints1.anchor = 18;
        gridbagconstraints1.weightx = 1.0D;
        jPanel3.add(jFont, gridbagconstraints1);
        jFontList.setModel(new DefaultListModel());
        jFontList.setSelectionMode(0);
        jFontList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent listselectionevent)
            {
                jFontListValueChanged(listselectionevent);
            }

        });
        jScrollPane1.setViewportView(jFontList);
        gridbagconstraints1 = new GridBagConstraints();
        gridbagconstraints1.fill = 1;
        gridbagconstraints1.insets = new Insets(3, 3, 3, 3);
        gridbagconstraints1.anchor = 18;
        gridbagconstraints1.weightx = 1.0D;
        gridbagconstraints1.weighty = 1.0D;
        jPanel3.add(jScrollPane1, gridbagconstraints1);
        GridBagConstraints gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.fill = 1;
        gridbagconstraints.insets = new Insets(5, 5, 0, 0);
        gridbagconstraints.weightx = 0.5D;
        gridbagconstraints.weighty = 1.0D;
        getContentPane().add(jPanel3, gridbagconstraints);
        jPanel4.setLayout(new GridBagLayout());
        jPanel4.setBorder(new TitledBorder(new EtchedBorder(), " Style "));
        jStyle.setColumns(18);
        jStyle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                jStyleActionPerformed(actionevent);
            }

        });
        GridBagConstraints gridbagconstraints2 = new GridBagConstraints();
        gridbagconstraints2.gridwidth = 0;
        gridbagconstraints2.fill = 2;
        gridbagconstraints2.insets = new Insets(0, 3, 0, 3);
        gridbagconstraints2.anchor = 18;
        gridbagconstraints2.weightx = 1.0D;
        jPanel4.add(jStyle, gridbagconstraints2);
        jStyleList.setModel(new DefaultListModel());
        jStyleList.setVisibleRowCount(4);
        jStyleList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent listselectionevent)
            {
                jStyleListValueChanged(listselectionevent);
            }

        });
        jScrollPane2.setViewportView(jStyleList);
        gridbagconstraints2 = new GridBagConstraints();
        gridbagconstraints2.fill = 1;
        gridbagconstraints2.insets = new Insets(3, 3, 3, 3);
        gridbagconstraints2.anchor = 18;
        gridbagconstraints2.weightx = 0.5D;
        gridbagconstraints2.weighty = 1.0D;
        jPanel4.add(jScrollPane2, gridbagconstraints2);
        gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.fill = 1;
        gridbagconstraints.insets = new Insets(5, 5, 0, 0);
        gridbagconstraints.weightx = 0.375D;
        gridbagconstraints.weighty = 1.0D;
        getContentPane().add(jPanel4, gridbagconstraints);
        jPanel5.setLayout(new GridBagLayout());
        jPanel5.setBorder(new TitledBorder(new EtchedBorder(), " Size "));
        jSize.setColumns(6);
        jSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                jSizeActionPerformed(actionevent);
            }

        });
        GridBagConstraints gridbagconstraints3 = new GridBagConstraints();
        gridbagconstraints3.gridwidth = 0;
        gridbagconstraints3.fill = 2;
        gridbagconstraints3.insets = new Insets(0, 3, 0, 3);
        gridbagconstraints3.anchor = 18;
        gridbagconstraints3.weightx = 1.0D;
        jPanel5.add(jSize, gridbagconstraints3);
        jSizeList.setModel(new DefaultListModel());
        jSizeList.setVisibleRowCount(4);
        jSizeList.setSelectionMode(0);
        jSizeList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent listselectionevent)
            {
                jSizeListValueChanged(listselectionevent);
            }

        });
        jScrollPane3.setViewportView(jSizeList);
        gridbagconstraints3 = new GridBagConstraints();
        gridbagconstraints3.fill = 1;
        gridbagconstraints3.insets = new Insets(3, 3, 3, 3);
        gridbagconstraints3.anchor = 18;
        gridbagconstraints3.weightx = 0.25D;
        gridbagconstraints3.weighty = 1.0D;
        jPanel5.add(jScrollPane3, gridbagconstraints3);
        gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.gridwidth = 0;
        gridbagconstraints.fill = 1;
        gridbagconstraints.insets = new Insets(5, 5, 0, 5);
        gridbagconstraints.weightx = 0.125D;
        gridbagconstraints.weighty = 1.0D;
        getContentPane().add(jPanel5, gridbagconstraints);
        jPanel1.setLayout(new GridBagLayout());
        jPanel1.setBorder(new TitledBorder(new EtchedBorder(), " Sample "));
        jSample.setWrapStyleWord(true);
        jSample.setLineWrap(true);
        jSample.setColumns(20);
        jSample.setRows(3);
        jSample.setText("The quick brown fox jumped over the lazy dog.");
        jScrollPane4.setViewportView(jSample);
        GridBagConstraints gridbagconstraints4 = new GridBagConstraints();
        gridbagconstraints4.fill = 1;
        gridbagconstraints4.insets = new Insets(0, 3, 3, 3);
        gridbagconstraints4.weightx = 1.0D;
        gridbagconstraints4.weighty = 1.0D;
        jPanel1.add(jScrollPane4, gridbagconstraints4);
        gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.gridwidth = 0;
        gridbagconstraints.fill = 1;
        gridbagconstraints.insets = new Insets(0, 5, 0, 5);
        gridbagconstraints.anchor = 18;
        gridbagconstraints.weightx = 1.0D;
        getContentPane().add(jPanel1, gridbagconstraints);
        jButtons.setLayout(new GridBagLayout());
        jOk.setMnemonic(79);
        jOk.setText("OK");
        jOk.setRequestFocusEnabled(false);
        jOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                jOkActionPerformed(actionevent);
            }

        });
        GridBagConstraints gridbagconstraints5 = new GridBagConstraints();
        gridbagconstraints5.insets = new Insets(5, 5, 5, 0);
        gridbagconstraints5.anchor = 17;
        jButtons.add(jOk, gridbagconstraints5);
        jCancel.setMnemonic(67);
        jCancel.setText("Cancel");
        jCancel.setRequestFocusEnabled(false);
        jCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                jCancelActionPerformed(actionevent);
            }

        });
        gridbagconstraints5 = new GridBagConstraints();
        gridbagconstraints5.gridwidth = 0;
        gridbagconstraints5.insets = new Insets(5, 5, 5, 5);
        gridbagconstraints5.anchor = 17;
        gridbagconstraints5.weightx = 1.0D;
        jButtons.add(jCancel, gridbagconstraints5);
        gridbagconstraints5 = new GridBagConstraints();
        gridbagconstraints5.weightx = 1.0D;
        jButtons.add(jLabel6, gridbagconstraints5);
        gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.gridwidth = 0;
        gridbagconstraints.anchor = 16;
        gridbagconstraints.weightx = 1.0D;
        getContentPane().add(jButtons, gridbagconstraints);
    }

    private void jCancelActionPerformed(ActionEvent actionevent)
    {
        setVisible(false);
    }

    private void jOkActionPerformed(ActionEvent actionevent)
    {
        ok = true;
        setVisible(false);
    }

    private void jSizeActionPerformed(ActionEvent actionevent)
    {
        int i = 0;
        try
        {
            i = Integer.parseInt(jSize.getText());
        }
        catch(Exception exception) { }
        if(i > 0)
        {
            currentSize = i;
            setSampleFont();
        }
    }

    private void jStyleActionPerformed(ActionEvent actionevent)
    {
        StringTokenizer stringtokenizer = new StringTokenizer(jStyle.getText(), ",");
        int i = 0;
        do
        {
            if(!stringtokenizer.hasMoreTokens())
                break;
            String s = stringtokenizer.nextToken().trim();
            if(s.equalsIgnoreCase("Plain"))
                i |= 0;
            else
            if(s.equalsIgnoreCase("Bold"))
                i |= 1;
            else
            if(s.equalsIgnoreCase("Italic"))
                i |= 2;
        } while(true);
        if(i >= 0)
        {
            currentStyle = i;
            setSampleFont();
        }
    }

    private void jFontActionPerformed(ActionEvent actionevent)
    {
        DefaultListModel defaultlistmodel = (DefaultListModel)jFontList.getModel();
        if(defaultlistmodel.indexOf(jFont.getText()) >= 0)
        {
            currentFont = jFont.getText();
            setSampleFont();
        }
    }

    private void jStyleListValueChanged(ListSelectionEvent listselectionevent)
    {
        String s = new String();
        Object aobj[] = jStyleList.getSelectedValues();
        if(aobj.length > 0)
        {
            int i = 0;
            do
            {
                if(i >= aobj.length)
                    break;
                String s1 = (String)aobj[i];
                if(s1.equalsIgnoreCase("Plain"))
                {
                    s = "Plain";
                    break;
                }
                if(s.length() > 0)
                    s = s + ",";
                s = s + (String)aobj[i];
                i++;
            } while(true);
        } else
        {
            s = styleToString(currentStyle);
        }
        jStyle.setText(s);
        jStyleActionPerformed(null);
    }

    private void jSizeListValueChanged(ListSelectionEvent listselectionevent)
    {
        String s = (String)jSizeList.getSelectedValue();
        if(s == null || s.length() <= 0)
            s = Integer.toString(currentSize);
        jSize.setText(s);
        jSizeActionPerformed(null);
    }

    private void jFontListValueChanged(ListSelectionEvent listselectionevent)
    {
        String s = (String)jFontList.getSelectedValue();
        if(s == null || s.length() <= 0)
            s = currentFont;
        jFont.setText(s);
        jFontActionPerformed(null);
    }

    private void closeDialog(WindowEvent windowevent)
    {
        setVisible(false);
    }

    String styleList[] = {
        "Plain", "Bold", "Italic"
    };
    String sizeList[] = {
        "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", 
        "22", "24", "30", "36", "48", "72"
    };
    String currentFont;
    int currentStyle;
    int currentSize;
    public boolean ok;
    private JPanel jPanel3;
    private JTextField jFont;
    private JScrollPane jScrollPane1;
    private JList jFontList;
    private JPanel jPanel4;
    private JTextField jStyle;
    private JScrollPane jScrollPane2;
    private JList jStyleList;
    private JPanel jPanel5;
    private JTextField jSize;
    private JScrollPane jScrollPane3;
    private JList jSizeList;
    private JPanel jPanel1;
    private JScrollPane jScrollPane4;
    private JTextArea jSample;
    private JPanel jButtons;
    private JButton jOk;
    private JButton jCancel;
    private JLabel jLabel6;









}
