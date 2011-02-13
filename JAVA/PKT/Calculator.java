package net.sf.pkt.calc;
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
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class Calculator extends JFrame
{

    public Calculator()
    {
        super("PKTCalc");
        setDefaultCloseOperation(2);
        TheDisplay = new JTextField(14);
        input = new Input(TheDisplay);
        Container container = getContentPane();
        GridBagLayout gridbaglayout = new GridBagLayout();
        container.setLayout(gridbaglayout);
        container.setBackground(Look.BackgroundColor());
        GridBagConstraints gridbagconstraints = new GridBagConstraints();
        gridbagconstraints.weighty = 1.0D;
        gridbagconstraints.gridx = 0;
        gridbagconstraints.gridy = 0;
        TheDisplay.setFont(Look.DisplayFont());
        TheDisplay.setText("0");
        TheDisplay.setEditable(false);
        TheDisplay.setHorizontalAlignment(4);
        TheDisplay.setForeground(Look.DisplayFG());
        TheDisplay.setBackground(Look.DisplayBG());
        TheDisplay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(10, 10, 4, 10, Look.BackgroundColor()), BorderFactory.createLoweredBevelBorder()));
        gridbaglayout.setConstraints(TheDisplay, gridbagconstraints);
        container.add(TheDisplay);
        gridbagconstraints.gridx = 0;
        gridbagconstraints.gridy = 1;
        chooser = new Chooser(input);
        gridbaglayout.setConstraints(chooser, gridbagconstraints);
        container.add(chooser);
        gridbagconstraints.gridx = 0;
        gridbagconstraints.gridy = 2;
        buttons = new Buttons(input);
        buttons.setBorder(BorderFactory.createMatteBorder(4, 10, 10, 10, Look.BackgroundColor()));
        gridbaglayout.setConstraints(buttons, gridbagconstraints);
        container.add(buttons);
        JMenuBar jmenubar = new JMenuBar();
        JMenu jmenu = new JMenu("File");
        JMenuItem jmenuitem = new JMenuItem("Exit");
        jmenuitem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
               dispose();
            }

        });
        jmenu.add(jmenuitem);
        jmenubar.add(jmenu);
        jmenubar.add(Box.createHorizontalGlue());
        setJMenuBar(jmenubar);
        pack();
	setVisible(true);
    }

  public void doExit(){dispose();}

    public void updateDisplay(String s)
    {
        TheDisplay.setText(s);
    }

    public static void main(String args[])
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch(Exception exception) { }
        Calculator calculator = new Calculator();
    }

    JTextField TheDisplay;
    Buttons buttons;
    Chooser chooser;
    Input input;
}
