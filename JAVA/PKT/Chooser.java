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

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Chooser extends JPanel
{

    public Chooser(ActionListener actionlistener)
    {
        setBackground(new Color(0, 0, 0));
        JRadioButton jradiobutton = new JRadioButton("Deg");
        jradiobutton.setActionCommand("chooseDeg");
        JRadioButton jradiobutton1 = new JRadioButton("Rad");
        jradiobutton1.setActionCommand("chooseRad");
        group = new ButtonGroup();
        group.add(jradiobutton);
        group.add(jradiobutton1);
        setForeground(Look.ChooserFG());
        setBackground(Look.ChooserBG());
        jradiobutton.setForeground(Look.ChooserFG());
        jradiobutton.setBackground(Look.ChooserBG());
        jradiobutton1.setForeground(Look.ChooserFG());
        jradiobutton1.setBackground(Look.ChooserBG());
        jradiobutton.setFocusPainted(false);
        jradiobutton1.setFocusPainted(false);
        add(jradiobutton);
        add(jradiobutton1);
        jradiobutton1.setSelected(true);
        jradiobutton.addActionListener(actionlistener);
        jradiobutton1.addActionListener(actionlistener);
    }

    private ButtonGroup group;
}
