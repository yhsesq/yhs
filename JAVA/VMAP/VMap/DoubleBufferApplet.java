package vmap;
// Double buffering class for JDK 1.1 AWT
// This file is released under the terms of the GNU GPL v2.0 or later.
// VMap/J 1.0 Double Buffering Toolkit

import java.applet.*;
import java.awt.*;
import java.net.*;

public class DoubleBufferApplet extends Applet {
  Image offscreenImage;
  Graphics offg;
  URL myURL;

  public void update(Graphics g) {
    Dimension d = getSize();
    // Create the offscreen buffer if necessary.
    if (offscreenImage == null ||
        offscreenImage.getWidth(null) != d.width ||
        offscreenImage.getHeight(null) != d.height) {
      // first time, or after resize
      offscreenImage = createImage(d.width, d.height);
      if (offg != null)
        offg.dispose();
      offg = offscreenImage.getGraphics();
    }

    // Call paint with the offscreen buffer.
    paint(offg);
 
    // Draw the buffer to the screen.
    g.drawImage(offscreenImage, 0, 0, this);
  }


  public void destroy() {
    if (offg != null)
      offg.dispose();
  }    

   public URL whereami() { 
 try { myURL= ((Applet)getParent()).getCodeBase();} catch(Exception E){}
      return myURL;
        }
}
