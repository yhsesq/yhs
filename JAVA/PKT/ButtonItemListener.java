package net.sf.pkt.clock;
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import javax.swing.*;

public class ButtonItemListener extends Observable
    implements ItemListener
{

    ButtonItemListener(JCalendar calendar)
    {
        this.calendar = calendar;
    }

    public void itemStateChanged(ItemEvent e)
    {
        if(e.getStateChange() == 1)
        {
            JToggleButton button = (JToggleButton)e.getItem();
            calendar.setDay(button.getText());
            Integer temp = (Integer)calendar.yearCombo.getSelectedItem();
            calendar.setYear(temp.toString());
            temp = new Integer(calendar.monthCombo.getSelectedIndex() + 1);
            calendar.setMonth(temp.toString());
            setChanged();
            notifyObservers();
        }
    }

    JCalendar calendar;
}
