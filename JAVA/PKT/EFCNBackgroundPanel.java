package net.sf.pkt.Unicode;

  /**
   *  A panel used as background for panels.
   *  Has some gradient and other drawing stuff on it.
   *
   *  Note: You have to call setOpaque(false) for all
   *        swing objects, which you add to this panel,
   *        otherwise you wont see the panel color gradient.
   *
   *  Copied from EFCN : http://62.53.146.182
   */


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.beans.*;


public class EFCNBackgroundPanel extends JPanel
{

  // The gradient types :
  public static int ApplyUpperLeftCornerHighLight = 0;
  public static int ApplyVerticalHighLight = 1;

  // Some predefined color keystrings :
  public static String ActiveTitleBackground = "InternalFrame.activeTitleBackground";
  public static String PanelBackground       = "Panel.background";

  // The gradient strength :
  public static int LightGradientStrength  = 0;
  public static int MediumGradientStrength = 1;
  public static int StrongGradientStrength = 2;


  private Color lightColor = new Color(190,190,250);
  private Color mediumColor   = new Color(120,120,180);
  private Color darkColor   = new Color(80,80,120);
  float xGradient;

  private Color basisColor; // around this the gradient will be

  // gradient look : (defaults)
  private int gradientType     = ApplyUpperLeftCornerHighLight;
  private int gradientStrength = MediumGradientStrength;
  private String colorKey      = ActiveTitleBackground;


  private boolean useParentBackGround = false; // when a background color has been assigned





 /**
  *  Creates a panel where you can pass
  *  theGradientType = ApplyUpperLeftCornerHighLight
  *                 or ApplyVerticalHighLight
  *
  *  and
  *  theGradientStrength = LightGradientStrength
  *                     or MediumGradientStrength
  *                     or StrongGradientStrength
  *
  *  and
  *  theColorKey = null
  *             or ActiveTitleBackground
  *             or PanelBackground
  *             or any valid theme colorkey.
  *
  */
  public EFCNBackgroundPanel( int    theGradientType,
                              int    theGradientStrength,
                              String theColorKey          )
  {
    this( new BorderLayout(),true,theGradientType,theGradientStrength,theColorKey );
  }





 /**
  *  Creates a panel where you can pass
  *  theGradientType = ApplyUpperLeftCornerHighLight
  *                 or ApplyVerticalHighLight
  *
  *  and
  *  theGradientStrength = LightGradientStrength
  *                     or MediumGradientStrength
  *                     or StrongGradientStrength
  *
  *  and
  *  theColorKey = null
  *             or ActiveTitleBackground
  *             or PanelBackground
  *             or any valid theme colorkey.
  *
  */
  public EFCNBackgroundPanel( LayoutManager layout,
                              int    theGradientType,
                              int    theGradientStrength,
                              String theColorKey    )
  {
    this(layout,true,theGradientType,theGradientStrength,theColorKey);
  }




 /**
  *  Creates a panel where you can pass
  *  theGradientType = ApplyUpperLeftCornerHighLight
  *                 or ApplyVerticalHighLight
  *
  *  and
  *  theGradientStrength = LightGradientStrength
  *                     or MediumGradientStrength
  *                     or StrongGradientStrength
  *
  *  and
  *  theColorKey = null
  *             or ActiveTitleBackground
  *             or PanelBackground
  *             or any valid theme colorkey.
  *
  */
  public EFCNBackgroundPanel( LayoutManager layout,
                              boolean isDoubleBuffered,
                              int    theGradientType,
                              int    theGradientStrength,
                              String theColorKey   )
  {
    super(layout,isDoubleBuffered);
    this.gradientType = theGradientType;
    this.gradientStrength = theGradientStrength;
    this.updateSpecialUI(); // sets basisColor, startColor and endColor
    if( colorKey != null )
     {
       this.colorKey = theColorKey;
     } // else use the default
    // scale the gradient along with the current font size :
    float unitSize = UIManager.getFont("TextField.font").getSize2D();
    this.xGradient = unitSize;

    // additional tasks on lookand feel changes :
    this.addPropertyChangeListener(
     new PropertyChangeListener()
     {
        public void propertyChange(PropertyChangeEvent e)
        {
         if( !useParentBackGround ) // only if setBackground was never called
          {
            String name = e.getPropertyName();
            if (name.equals("ancestor"))
             {
               SwingUtilities.invokeLater( new Runnable()
                {
                   public void run()
                   {
                    updateSpecialUI();
                    // rescale the gradient along with the current font size :
                    float unitSize = UIManager.getFont("TextField.font").getSize2D();
                    xGradient = unitSize;
                   }
                });
             }
          }
        }
     });

  } // Constructor



 /**
  *  Seta a fixed background color, and with that : turns out the
  *  UIManager update mechanism.
  */
  public void setBackground( Color bgColor )
  {
    super.setBackground(bgColor);
    this.useParentBackGround = true; // turns off UIManager special update
    this.basisColor = super.getBackground();
    this.calculateColors();
  }



 /**
  * Calculate the start and endcolor of the gradient
  * taking the basisColor as center color :
  */
  private void calculateColors()
  {
    int colorOffset = 50; // medium gradient strength
    if( this.gradientStrength == LightGradientStrength )
     {
       colorOffset = 30;
     }
    if( this.gradientStrength == StrongGradientStrength )
     {
       colorOffset = 70;
     }
    int rBase = this.basisColor.getRed();
    int gBase = this.basisColor.getGreen();
    int bBase = this.basisColor.getBlue();
    // start color is lighter :
    int rStart = rBase + colorOffset;
    int gStart = gBase + colorOffset;
    int bStart = bBase + colorOffset;
    if(  (rStart <= 255) && (gStart <= 255) && (bStart <= 255) )
     {
       this.lightColor = new Color( rStart,gStart,bStart );
     } else
     {
       if( rStart > 255 ) rStart = 255;
       if( gStart > 255 ) gStart = 255;
       if( bStart > 255 ) bStart = 255;
       this.lightColor = new Color( rStart,gStart,bStart );
     }

    this.mediumColor = this.basisColor;

    rStart = rBase - colorOffset;
    gStart = gBase - colorOffset;
    bStart = bBase - colorOffset;
    if(  (rStart >= 0) && (gStart >= 0) && (bStart >= 0) )
     {
       this.darkColor = new Color( rStart,gStart,bStart );
     } else
     {
       if( rStart < 0 ) rStart = 0;
       if( gStart < 0 ) gStart = 0;
       if( bStart < 0 ) bStart = 0;
       this.darkColor = new Color( rStart,gStart,bStart );
     }

  }


 /**
  *  Must be called, when the lf theme changes.
  *  Called by the propertychange listener above.
  */
  public void updateSpecialUI()
  {
    // Derive the basisColor :
    Color color = UIManager.getColor(this.colorKey);
    if( color == null )
     {
       color = new Color(250,170,150); // give it a green color to show
     }                                 // that the key was invalid.
    // give a little bit more blue
    int r = color.getRed()   - 3;
    int g = color.getGreen() - 3;
    int b = color.getBlue()  + 6;
    // level out grayscale value a bit :
    if( r+g+b > 384 )
     {
       r -= 10;
       g -= 10;
       b -= 10;
     } else
     {
       r += 10;
       g += 10;
       b += 10;
     }
    // keep in range :
    if( r < 0 ) r = 0; if( r > 255 ) r = 255;
    if( g < 0 ) g = 0; if( g > 255 ) g = 255;
    if( b < 0 ) b = 0; if( b > 255 ) b = 255;
    // and set it as basis :
    this.basisColor = new Color(r,g,b);
    // Calculate the start and endColors from that :
    this.calculateColors();
  }


 /**
  *  Overwritten paint method to have a slight color gradient.
  */
  public void paint( Graphics g )
  {
    Graphics2D graphics2D = (Graphics2D)g;
    final Paint savePaint = graphics2D.getPaint();
    if( this.gradientType == ApplyUpperLeftCornerHighLight )
     {
      GradientPaint upperLeftGradientPaint =
                    new GradientPaint( 0f,0f,
                                       lightColor,
                                       xGradient,xGradient*5.0f,
                                       mediumColor );

      graphics2D.setPaint( upperLeftGradientPaint );
      graphics2D.fill( graphics2D.getClip() );
     }
    else if( this.gradientType == ApplyVerticalHighLight )
     {

      float gradientLength = xGradient;
      if( gradientLength > this.getHeight()/2.5f )
       {
         gradientLength = this.getHeight()/2.5f;
       }
      GradientPaint upperVerticalGradientPaint =
                    new GradientPaint( 0f,0f,
                                       this.lightColor,
                                       0f, gradientLength,
                                       this.mediumColor );

      GradientPaint lowerVerticalGradientPaint =
                    new GradientPaint( 0f,getHeight(),
                                       this.darkColor,
                                       0f,getHeight() - gradientLength,
                                       this.mediumColor );

      Shape saveClip = graphics2D.getClip();

      graphics2D.setPaint( lowerVerticalGradientPaint );
      graphics2D.fill( graphics2D.getClip() );

      Rectangle r = new Rectangle( 0,0,getWidth(),1+getHeight()/2 );
      graphics2D.setPaint( upperVerticalGradientPaint );
      graphics2D.fill( r );

      graphics2D.setClip(saveClip);
     }
    graphics2D.setPaint( savePaint );
    super.paintChildren(graphics2D);
  } // paint




 /**
  *  Overwitten, so it doesnt clear all, but
  *  one has to call super, so children are properly rendered.
  */
  public void update( Graphics g )
  {
   //super.update(g);
   paint(g);
  }


} // EFCNBackgroundPanel




