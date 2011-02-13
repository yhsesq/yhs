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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

public class Input
    implements ActionListener
{

    public Input(JTextField jtextfield)
    {
        calc = jtextfield;
        number_maker = new NumberMaker();
        engine = new CalcEngine();
        storage = new Storage();
        makeComLinks();
    }

    private void makeComLinks()
    {
        Integer integer = new Integer(0);
        Integer integer1 = new Integer(1);
        Integer integer2 = new Integer(2);
        comLinks = new HashMap();
        comLinks.put("0", integer);
        comLinks.put("1", integer);
        comLinks.put("2", integer);
        comLinks.put("3", integer);
        comLinks.put("4", integer);
        comLinks.put("5", integer);
        comLinks.put("6", integer);
        comLinks.put("7", integer);
        comLinks.put("8", integer);
        comLinks.put("9", integer);
        comLinks.put(".", integer);
        comLinks.put("EE", integer);
        comLinks.put("+/-", integer);
        comLinks.put("=", integer1);
        comLinks.put("%", integer1);
        comLinks.put("+", integer1);
        comLinks.put("-", integer1);
        comLinks.put("*", integer1);
        comLinks.put("/", integer1);
        comLinks.put("1/x", integer1);
        comLinks.put("e", integer1);
        comLinks.put("ln", integer1);
        comLinks.put("log", integer1);
        comLinks.put("sin", integer1);
        comLinks.put("cos", integer1);
        comLinks.put("tan", integer1);
        comLinks.put("asin", integer1);
        comLinks.put("acos", integer1);
        comLinks.put("atan", integer1);
        comLinks.put("sinh", integer1);
        comLinks.put("cosh", integer1);
        comLinks.put("x!", integer1);
        comLinks.put("x^y", integer1);
        comLinks.put("2^x", integer1);
        comLinks.put("C", integer1);
        comLinks.put("CE", integer1);
        comLinks.put("sqrt", integer1);
        comLinks.put("x^2", integer1);
        comLinks.put("chooseDeg", integer1);
        comLinks.put("chooseRad", integer1);
        comLinks.put("STO", integer2);
        comLinks.put("RCL", integer2);
    }

    public void actionPerformed(ActionEvent actionevent)
    {
        String s = new String("0123456789.");
        String s1 = actionevent.getActionCommand();
        Integer integer = (Integer)comLinks.get(s1);
        switch(integer.intValue())
        {
        case 0: // '\0'
            number_maker.process(s1);
            break;

        case 1: // '\001'
            number_maker.setValue(engine.process(number_maker.getValue(), s1));
            break;

        case 2: // '\002'
            number_maker.setValue(storage.process(number_maker.getValue(), s1));
            break;
        }
        calc.setText(number_maker.toString());
    }

    private NumberMaker number_maker;
    private CalcEngine engine;
    private Storage storage;
    private JTextField calc;
    private HashMap comLinks;
    private static final int NUMBER = 0;
    private static final int ENGINE = 1;
    private static final int STORAGE = 2;
}
