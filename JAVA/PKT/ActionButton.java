package net.sf.pkt.Unicode;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Hashtable;


/** utility to define an action that is used either as a button,
    a menu item, or in a toolbar.

    [1] as described in the book Java design patterns, a tutorial
         from Cooper, Addision Weseley, 2000.
*/
public abstract class ActionButton extends AbstractAction
{
  Hashtable properties;

  public ActionButton(String caption, Icon img)
  {
      properties = new Hashtable();
      properties.put(DEFAULT, caption);
      properties.put(NAME, caption);
      properties.put(SHORT_DESCRIPTION, caption);
      if(img!=null)
         properties.put(SMALL_ICON, img);
  }

  public void putValue(String key, Object value) {
      properties.put(key, value);
  }

  public Object getValue(String key) {
      return properties.get(key);
  }

  public abstract void actionPerformed(ActionEvent e);

  // AAARGGG !! :-(  the javax.swing.JMenuItem don't take other
  // properties then Icon and Name from the Action !,
  // so we must add the accelerator
  public JMenuItem getJMenuItem()
  {
      JMenuItem jm = new JMenuItem(this);
      Object o = getValue(Action.ACCELERATOR_KEY);
      if(o!=null)
         jm.setAccelerator((KeyStroke) o);
      return jm;
  }

}
