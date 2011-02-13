package net.sf.pkt.Unicode;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import net.sf.pkt.Unicode.*;

public class VKBD extends JFrame
{
private static Keyboard keyboard;
    public VKBD()
    { doKeyboard();
    }

 public void doKeyboard()
 {
      setTitle("PKT Virtual Unicode Keyboard");
      setDefaultCloseOperation(2);
      keyboard = new Keyboard(this, null, null);
      (getContentPane()).add(keyboard);
      //keyboard.parentFrame=jf;
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing( WindowEvent e) { doExit();dispose(); }
      });
      pack();
      setSize(610,300);
      // setVisible(true);
      show();
 }
 public static void main (String[] a){new VKBD();}

 public void doExit(){dispose();keyboard=null;dispose();}

}
