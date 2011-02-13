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
import java.awt.event.ActionListener;
import javax.swing.*;

public class Buttons extends JPanel
{

    private void createButton(String s, Color color, Color color1)
    {
        JButton jbutton = new JButton(s);
        jbutton.setMargin(new Insets(4, 4, 4, 4));
        jbutton.setFocusPainted(false);
        jbutton.addActionListener(listener);
        jbutton.setFont(Look.ButtonFont());
        jbutton.setBorderPainted(true);
        jbutton.setBorder(BorderFactory.createRaisedBevelBorder());
        jbutton.setBackground(color);
        jbutton.setForeground(color1);
        add(jbutton);
    }

    public Buttons(ActionListener actionlistener)
    {
        listener = actionlistener;
        setLayout(new GridLayout(8, 5, 2, 2));
        setBackground(new Color(0, 0, 0));
        Color color = Look.funcKeyBG();
        Color color1 = Look.funcKeyFG();
        Color color2 = Look.operatorKeyBG();
        Color color3 = Look.operatorKeyFG();
        Color color4 = Look.numberKeyBG();
        Color color5 = Look.numberKeyFG();
        createButton("x!", color, color1);
        createButton("x^y", color, color1);
        createButton("2^x", color, color1);
        createButton("C", color2, color3);
        createButton("CE", color2, color3);
        createButton("asin", color, color1);
        createButton("acos", color, color1);
        createButton("atan", color, color1);
        createButton("sinh", color, color1);
        createButton("cosh", color, color1);
        createButton("sin", color, color1);
        createButton("cos", color, color1);
        createButton("tan", color, color1);
        createButton("sqrt", color, color1);
        createButton("x^2", color, color1);
        createButton("e", color, color1);
        createButton("ln", color, color1);
        createButton("log", color, color1);
        createButton("STO", color, color1);
        createButton("RCL", color, color1);
        createButton("7", color4, color5);
        createButton("8", color4, color5);
        createButton("9", color4, color5);
        createButton("+/-", color, color1);
        createButton("1/x", color, color1);
        createButton("4", color4, color5);
        createButton("5", color4, color5);
        createButton("6", color4, color5);
        createButton("*", color2, color3);
        createButton("/", color2, color3);
        createButton("1", color4, color5);
        createButton("2", color4, color5);
        createButton("3", color4, color5);
        createButton("+", color2, color3);
        createButton("-", color2, color3);
        createButton("0", color4, color5);
        createButton(".", color4, color5);
        createButton("EE", color4, color5);
        createButton("=", color2, color3);
        createButton("%", color2, color3);
    }

    private ActionListener listener;
}
