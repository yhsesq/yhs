package net.sf.pkt.browse;
//
//       _/_/_/_/  _/  _/ _/_/_/_/_/_/
//      _/    _/  _/ _/       _/
//     _/    _/  _/_/        _/
//    _/_/_/_/  _/ _/       _/
//   _/        _/   _/     _/
//  _/        _/     _/   _/
//
//  This file is part of PKT (an XML Universal Packet Archiver
//  tool). See http://pkt.sourceforge.net for details of PKT.
//
//  Copyright (C) 2000-2004 Yohann Sulaiman (yhs@users.sf.net)
//
//  PKT is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  PKT is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.URL;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class PKTBrowse extends JFrame
    implements ActionListener
{
    class LinkActivator
        implements HyperlinkListener
    {

        public void hyperlinkUpdate(HyperlinkEvent he)
        {
            javax.swing.event.HyperlinkEvent.EventType type = he.getEventType();
            if(type == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)
            {
                openURL(he.getURL().toExternalForm());
                addHistory(he.getURL().toExternalForm());
            }
        }

        LinkActivator()
        {
        }
    }


    public PKTBrowse(String urlString)
    {
        setTitle("PKTBrowse 0.1");
        Toolkit kit = Toolkit.getDefaultToolkit();
        createUI(urlString);
        setVisible(true);
    }

    public static void main(String args[])
    {
        if(args.length == 0)
            home = "http://pkt.sourceforge.net/tutorial.html";
        else
            home = args[0];
        new PKTBrowse(home);
    }

    protected void createUI(String urlString)
    {
        content = getContentPane();
        content.setLayout(new BorderLayout());
        JToolBar goBar = new JToolBar();
        goBar.setFloatable(false);
        goBar.add(new JLabel("Go "));
        goField = new JTextField(urlString, 40);
        Toolkit kit = Toolkit.getDefaultToolkit();
        JButton backButton = new JButton("<<");
        goBar.add(goField);
        goBar.add(backButton);
        browserWindow = new JEditorPane();
        browserWindow.setEditable(false);
        content.add(goBar, "North");
        content.add(new JScrollPane(browserWindow), "Center");
        openURL(urlString);
        addHistory(urlString);
        goField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
                openURL(ae.getActionCommand());
                addHistory(ae.getActionCommand());
            }

        });
        backButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae)
            {
		if(history.size()>1){
                openURL((String)PKTBrowse.history.get(PKTBrowse.history.size() - 2));
                delHistory();}
            }

        });
        browserWindow.addHyperlinkListener(new LinkActivator());
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        makeMenu();
        jmiFileExit.addActionListener(this);
        setJMenuBar(jmb);
        pack();
        setSize((int)d.getWidth() / 2, (int)d.getHeight() / 2);
        setDefaultCloseOperation(2);
    }

    protected void openURL(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            browserWindow.setPage(url);
            goField.setText(url.toExternalForm());
        }
        catch(Exception e)
        {
            //System.out.println("Couldn't open " + urlString + ": " + e);
        }
    }

    private void addHistory(String urlString)
    {
        history.add(urlString);
        //System.out.println("added " + urlString);
    }

    private void delHistory()
    {
        history.removeElementAt(history.size() - 1);
    }

    public static void makeMenu()
    {
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        jmiFileExit = new JMenuItem("Exit", 120);
        jmiFileExit.setAccelerator(KeyStroke.getKeyStroke(81, 2));
        fileMenu.add(jmiFileExit);
        jmb.add(fileMenu);
    }

    public void actionPerformed(ActionEvent e)
    {
        String actionCommand = e.getActionCommand();
        if("New".equals(actionCommand))
            {}//System.out.println("new");
        else
        if("Close".equals(actionCommand))
            {}//System.out.println("close");
        else
            {content.removeAll();jmb.removeAll();dispose();}
    }

    public void doExit(){content.removeAll();jmb.removeAll();dispose();}
    private final String version = "PKTBrowse 0.1";
    private final boolean debug = false;
    private static String home;
    protected static JEditorPane browserWindow;
    protected static JTextField goField;
    private static Vector history = new Vector();
    private static Container content;
    private static JMenu fileMenu = new JMenu();
    private static JMenuBar jmb = new JMenuBar();
    private static JMenuItem jmiFileNew;
    private static JMenuItem jmiFileClose;
    private static JMenuItem jmiFileExit;

}
