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


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.text.JTextComponent;

public class TextFieldMouseListener extends MouseAdapter
{

    protected TextFieldMouseListener(JCalendarCombo calendarCombo)
    {
        this.calendarCombo = calendarCombo;
    }

    public void mousePressed(MouseEvent e)
    {
        calendarCombo.textField.selectAll();
    }

    public void mouseReleased(MouseEvent e)
    {
        if(!calendarCombo.condition)
        {
            calendarCombo.condition = true;
            Frame parentWindow = null;
            if(!calendarCombo.isApplet)
            {
                parentWindow = (Frame)calendarCombo.textField.getTopLevelAncestor();
                calendarCombo.window = new JWindow(parentWindow);
            } else
            {
                calendarCombo.window = new JWindow(parentWindow);
            }
            calendarCombo.window.getContentPane().setLayout(new BorderLayout());
            calendarCombo.calendar.initializeCalendar();
            calendarCombo.window.getContentPane().add(calendarCombo.calendar, "Center");
            calendarCombo.window.pack();
            Point textFieldLocation = calendarCombo.textField.getLocationOnScreen();
            Dimension size = calendarCombo.textField.getSize();
            Dimension windowSize = calendarCombo.window.getSize();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if(textFieldLocation.x - (windowSize.width - size.width) <= 0 && textFieldLocation.y + size.height + windowSize.height >= screenSize.height)
                calendarCombo.window.setLocation(0, textFieldLocation.y - windowSize.height);
            else
            if(textFieldLocation.x - (windowSize.width - size.width) <= 0)
                calendarCombo.window.setLocation(0, textFieldLocation.y + size.height);
            else
            if(textFieldLocation.y + size.height + windowSize.height >= screenSize.height)
                calendarCombo.window.setLocation(textFieldLocation.x - (windowSize.width - size.width), textFieldLocation.y - windowSize.height);
            else
                calendarCombo.window.setLocation(textFieldLocation.x - (windowSize.width - size.width), textFieldLocation.y + size.height);
            calendarCombo.window.pack();
            calendarCombo.window.setVisible(true);
        } else
        {
            calendarCombo.window.setVisible(false);
            calendarCombo.setSelectedDate();
            calendarCombo.condition = false;
        }
    }

    private JCalendarCombo calendarCombo;
}
