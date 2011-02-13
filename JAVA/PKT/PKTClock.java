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

import net.sf.pkt.clock.*;
import java.awt.*;
import java.awt.event.*;

public class PKTClock extends Frame implements ActionListener {
  worldclock myApplet;

  public PKTClock() { // constructor
    super("PKTWorldClock"); // define frame title
    setBounds(10, 10, 510, 660); // this time use a predefined frame size/position
    setLayout(new BorderLayout());
    setBackground(Color.black);
    addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){doExit();}});
    Panel p= new Panel(); 
    p.setLayout(new FlowLayout());
    JCalendarCombo caltest = new JCalendarCombo();
    p.add(caltest);
    // Define File menu and with Exit menu item
    Button exitMenuItem = new Button("Exit");
    exitMenuItem.addActionListener (this);
    p.add(exitMenuItem);    
    // define the applet and add to the frame
    add(p,BorderLayout.NORTH);
    myApplet = new worldclock();
    add(myApplet,BorderLayout.CENTER);
    // call applet's init method (since it is not
    // automatically called in a Java application)
    setBounds(10, 10, 510, 660); // this time use a predefined frame size/position
    pack();setVisible(true);
    myApplet.init();
    myApplet.start();
    setBounds(10, 10, 510, 660); // this time use a predefined frame size/position
  } // end constructor

public static void main (String[] args) {
  // define frame, its size and make it visible
  PKTClock myFrame = new PKTClock();
  } // end main method

public void doExit(){
        myApplet.stop();dispose();}

  public void actionPerformed(ActionEvent evt) {

    if (evt.getSource() instanceof Button) {
      String menuLabel = ((Button)evt.getSource()).getLabel();

      if(menuLabel.equals("Exit")) {
        // close application, when exit is selected
        doExit();
      } // end if
    } // end if
  } // end ActionPerformed
} // end class
