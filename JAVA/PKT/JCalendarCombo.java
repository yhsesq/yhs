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
import java.io.PrintStream;
import java.util.*;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.JTextComponent;

public class JCalendarCombo extends JPanel
    implements Observer
{

    public JCalendarCombo()
    {
        condition = false;
        isApplet = false;
        tempVar1 = 1001;
        tempVar2 = 1002;
        tempVar3 = 1004;
        separatorChar = '/';
        isEnabled = true;
        startYear = 1901;
        endYear = 2099;
        isApplet = false;
        calendar = new JCalendar();
        initializeJCalendarCombo();
    }

    public JCalendarCombo(boolean isApplet)
    {
        condition = false;
        this.isApplet = false;
        tempVar1 = 1001;
        tempVar2 = 1002;
        tempVar3 = 1004;
        separatorChar = '/';
        isEnabled = true;
        startYear = 1901;
        endYear = 2099;
        this.isApplet = isApplet;
        calendar = new JCalendar();
        initializeJCalendarCombo();
    }

    public JCalendarCombo(int firstDay, boolean showCurrentDate, boolean isApplet)
    {
        condition = false;
        this.isApplet = false;
        tempVar1 = 1001;
        tempVar2 = 1002;
        tempVar3 = 1004;
        separatorChar = '/';
        isEnabled = true;
        startYear = 1901;
        endYear = 2099;
        this.isApplet = isApplet;
        calendar = new JCalendar(firstDay, showCurrentDate);
        initializeJCalendarCombo();
    }

    public JCalendarCombo(int firstDay, boolean showCurrentDate, int startYear, int endYear, boolean isApplet)
    {
        condition = false;
        this.isApplet = false;
        tempVar1 = 1001;
        tempVar2 = 1002;
        tempVar3 = 1004;
        separatorChar = '/';
        isEnabled = true;
        this.startYear = startYear;
        this.endYear = endYear;
        this.isApplet = isApplet;
        calendar = new JCalendar(firstDay, showCurrentDate, startYear, endYear);
        initializeJCalendarCombo();
    }

    private final void initializeJCalendarCombo()
    {
        textField = new JTextField(10);
        textField.setEditable(false);
        textField.setBackground(new Color(255, 255, 255));
        button = new BasicArrowButton(5);
        setLayout(new BorderLayout());
        add(textField, "Center");
        add(button, "East");
        setSelectedDate();
        buttonActionListener = new ButtonActionListener(this);
        button.addActionListener(buttonActionListener);
        textFieldMouseListener = new TextFieldMouseListener(this);
        textField.addMouseListener(textFieldMouseListener);
        addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent e)
            {
                if(condition)
                {
                    condition = false;
                    setSelectedDate();
                    window.setVisible(false);
                } else
                {
                    setSelectedDate();
                }
            }

            public void ancestorMoved(AncestorEvent e)
            {
                if(condition)
                {
                    condition = false;
                    setSelectedDate();
                    window.setVisible(false);
                }
            }

            public void ancestorRemoved(AncestorEvent e)
            {
                if(condition)
                {
                    condition = false;
                    setSelectedDate();
                    window.setVisible(false);
                }
            }

        });
        calendar.buttonItemListener.addObserver(this);
    }

    protected final void setSelectedDate()
    {
        textField.setText(formatDate());
        condition = false;
    }

    public final void setSelectedDate(int year, int month, int day)
    {
        calendar.setDay((new Integer(day)).toString());
        calendar.setMonth((new Integer(month)).toString());
        calendar.setYear((new Integer(year)).toString());
        calendar.showCalendarForDate(year, month);
        textField.setText(formatDate());
        condition = false;
    }

    public final void setDate(Calendar temp)
    {
        calendar.setDay((new Integer(temp.get(5))).toString());
        calendar.setMonth((new Integer(temp.get(2))).toString());
        calendar.setYear((new Integer(temp.get(1))).toString());
        calendar.showCalendarForDate(temp.get(1), temp.get(2));
        textField.setText(formatDate());
        condition = false;
    }

    public final void setDateFormat(int tempVar1, int tempVar2, int tempVar3, char separatorChar)
    {
        if(tempVar1 != 1001 && tempVar1 != 1002 && tempVar1 != 1004 && tempVar1 != 1003)
        {
            System.out.println("Invalid Date Format. Setting Default Format");
            return;
        }
        if(tempVar2 != 1001 && tempVar2 != 1002 && tempVar2 != 1004 && tempVar2 != 1003)
        {
            System.out.println("Invalid Date Format. Setting Default Format");
            return;
        }
        if(tempVar3 != 1001 && tempVar3 != 1002 && tempVar3 != 1004 && tempVar3 != 1003)
        {
            System.out.println("Invalid Date Format. Setting Default Format");
            return;
        } else
        {
            this.tempVar1 = tempVar1;
            this.tempVar2 = tempVar2;
            this.tempVar3 = tempVar3;
            this.separatorChar = separatorChar;
            return;
        }
    }

    public final void setEnabled(boolean isEnabled)
    {
        textField.setEnabled(isEnabled);
        button.setEnabled(isEnabled);
        if(isEnabled)
        {
            button.addActionListener(buttonActionListener);
            textField.addMouseListener(textFieldMouseListener);
        } else
        {
            button.removeActionListener(buttonActionListener);
            textField.removeMouseListener(textFieldMouseListener);
        }
        this.isEnabled = isEnabled;
    }

    public final String getSelectedDate()
    {
        return textField.getText();
    }

    public final Calendar getDate()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set((new Integer(getSelectedYear())).intValue(), (new Integer(getSelectedMonth())).intValue(), (new Integer(getSelectedDay())).intValue());
        return calendar;
    }

    public final String getSelectedDay()
    {
        return calendar.getDay();
    }

    public final String getSelectedMonth()
    {
        return calendar.getMonth();
    }

    public final String getSelectedYear()
    {
        return calendar.getYear();
    }

    public final boolean getEnabled()
    {
        return isEnabled;
    }

    public void update(Observable observable, Object object)
    {
        window.setVisible(false);
        setSelectedDate();
    }

    private String formatDate()
    {
        String date;
        if(tempVar1 == 1001)
            date = calendar.getDay() + separatorChar;
        else
        if(tempVar1 == 1002)
            date = calendar.getMonth() + separatorChar;
        else
        if(tempVar1 == 1004)
            date = calendar.getYear() + separatorChar;
        else
            date = calendar.getYear().substring(2, 4) + separatorChar;
        if(tempVar2 == 1001)
            date = date + calendar.getDay() + separatorChar;
        else
        if(tempVar2 == 1002)
            date = date + calendar.getMonth() + separatorChar;
        else
        if(tempVar2 == 1004)
            date = date + calendar.getYear() + separatorChar;
        else
            date = date + calendar.getYear().substring(2, 4) + separatorChar;
        if(tempVar3 == 1001)
            date = date + calendar.getDay();
        else
        if(tempVar3 == 1002)
            date = date + calendar.getMonth();
        else
        if(tempVar3 == 1004)
            date = date + calendar.getYear();
        else
            date = date + calendar.getYear().substring(2, 4);
        return date;
    }

    protected boolean condition;
    protected boolean isApplet;
    private int tempVar1;
    private int tempVar2;
    private int tempVar3;
    private char separatorChar;
    protected JWindow window;
    protected JCalendar calendar;
    protected JTextField textField;
    protected JButton button;
    private int startYear;
    private int endYear;
    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int DAY = 1001;
    public static final int MONTH = 1002;
    public static final int YEAR_SMALL = 1003;
    public static final int YEAR_BIG = 1004;
    private ButtonActionListener buttonActionListener;
    private TextFieldMouseListener textFieldMouseListener;
    private boolean isEnabled;
}
