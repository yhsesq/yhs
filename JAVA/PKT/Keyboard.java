package net.sf.pkt.Unicode;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.URL;

/** if you love that, send a mail to stephan@www.snowraver.org
  * Modifications by Yohann Sulaiman yhs@users.sf.net
  * This program is released under the terms of the GNU
  * GPL v2.0 or any later version.
  * (C) 2004 Yohann Sulaiman
 */
public class Keyboard extends EFCNBackgroundPanel
              implements ActionListener, KeyListener  {

  private final Font dialogFont = new Font("Dialog", Font.PLAIN, 10);

  private final String[] allFontNames;
  private String[] supportedFontNames ;
  private String actualFont ="";

  private static JPanel keyboardPanel;
  private final JTextPane textArea;
  private final JScrollPane scrollPane;

  private final JTextField phoneticTF;
  private final JTextField completionLabel;

  private final String[] keyboardNames;
  private final String[][][] keyboards;
  private String[][] actualKeyboard = new String[0][0];
  private final JComboBox keyboardsCB;
  private final JComboBox fontsCB;
  private final FontComboBoxModel fontComboBoxModel;
  private final int[] lines, cols, showLimit;
  private final JTextField fontSizeTF = new JTextField("18",3);
  private static JButton viewKeyboardButton;
  private static JButton cutbutton;
  private static JButton copybutton;
  private static JButton pastebutton;

  // eventually existing parent
  private static JFrame parentFrame;
  private static JDialog keyboardDialog;

  public Keyboard(JFrame parentFrame, String theKeyboardName, String[][] chars)
  {
     super(EFCNBackgroundPanel.ApplyUpperLeftCornerHighLight,
           EFCNBackgroundPanel.LightGradientStrength,
           EFCNBackgroundPanel.ActiveTitleBackground);

     setLayout(new BorderLayout());
     this.parentFrame = parentFrame; parentFrame.setDefaultCloseOperation(2);

     allFontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
     supportedFontNames = allFontNames;

     if(chars!=null)
     {
         keyboardNames = new String[] {theKeyboardName, "English", "Arabic", "Russian", "Hiragana", "Katakana", "Tamil", "Hebrew","Tibetan", "Greek", "Thai", "Braille"};
         keyboards = new String[][][] {chars, englishLetters, arabicLetters, russianLetters, hiraganaLetters, katakanaLetters,tamilLetters,hebrewLetters,tibetanLetters,greekLetters, thaiLetters, brailleLetters};
         lines = new int[]{16,5,5,17,17,11, 3,14, 3,5,10};
         cols  = new int[]{ 5,10,5, 5, 5, 6,11,10,11,15,28};
         showLimit = new int[]{chars.length,100,35,84,84,66,33,200,33,90,280};
     }
     else
     {
         keyboardNames = new String[] { "English", "Arabic", "Russian", "Hiragana", "Katakana", "Tamil", "Hebrew","Tibetan","Greek", "Thai", "Braille"};
         keyboards = new String[][][] { englishLetters, arabicLetters, russianLetters, hiraganaLetters, katakanaLetters,tamilLetters,hebrewLetters,tibetanLetters,greekLetters,thaiLetters,brailleLetters};
         lines = new int[]{ 5,5,17,17,11, 3,14, 3,5,10};
         cols  = new int[]{ 10,5, 5, 5, 6,11,10,11,15,28};
         showLimit = new int[]{100,35,84,84,66,33,200,33,90,280};
     }

     keyboardsCB = new JComboBox(keyboardNames);
     keyboardsCB.setSelectedIndex(0); // russian

     keyboardPanel = new JPanel();
     keyboardPanel.setOpaque(false);

     JPanel northPanel = new JPanel(new BorderLayout());
     northPanel.setOpaque(false);
     add(northPanel, BorderLayout.CENTER);

     JPanel phoneticPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     phoneticPanel.setOpaque(false);
     phoneticPanel.add(new JLabel("Phonetic Type: "));
     phoneticTF = new JTextField("",6);
     phoneticPanel.add(phoneticTF);
     phoneticTF.addKeyListener(this);

     completionLabel = new JTextField(".. possible matching ..", 30);
     completionLabel.setEditable(false);
     completionLabel.setFont(dialogFont);
     phoneticPanel.add(completionLabel);

     northPanel.add(phoneticPanel, BorderLayout.SOUTH);
     textArea = new JTextPane();
     scrollPane = new JScrollPane(textArea);
     northPanel.add(scrollPane, BorderLayout.CENTER);
     JPanel keyAndFontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
     keyAndFontPanel.setOpaque(false);
     northPanel.add(keyAndFontPanel, BorderLayout.NORTH);

     viewKeyboardButton = new JButton();
     viewKeyboardButton.setFont(dialogFont);
     viewKeyboardButton.setText("Hide Keyboard");
     viewKeyboardButton.setToolTipText("Show the keyboard");
     keyAndFontPanel.add(viewKeyboardButton);

     viewKeyboardButton.addActionListener(new ActionListener()
     {
         public void actionPerformed(ActionEvent e)
         { 
     	   if(keyboardDialog==null){dodialog();}keyboardDialog.setVisible(true);
         }
     });

	dodialog();
     keyAndFontPanel.add(keyboardsCB);

     fontComboBoxModel = new FontComboBoxModel();
     fontsCB = new JComboBox(fontComboBoxModel);
     fontsCB.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
             setFonts();
         }
     });
     fontsCB.setFont(dialogFont);

     keyAndFontPanel.add(fontsCB);
     fontSizeTF.setFont(dialogFont);
     keyAndFontPanel.add(fontSizeTF);
     fontSizeTF.addActionListener(new ActionListener()
     {
       public void actionPerformed(ActionEvent e)
       {
          setFonts();
       }
     });

     fontSizeTF.addFocusListener(new FocusAdapter()
     {
       public void focusGained(FocusEvent e)
       {
          fontSizeTF.setSelectionStart(0);
          fontSizeTF.setSelectionEnd(fontSizeTF.getText().length());

       }
     });

     JButton browseUnicodeButton = new JButton(new BrowseUnicodeAction());
     //keyAndFontPanel.add(browseUnicodeButton);
     browseUnicodeButton.setFont(dialogFont);

     createKeyboard(keyboards[0],lines[0],cols[0], showLimit[0]);

     keyboardsCB.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e)
        {
            keyboardCBAction();
        }
     });
     keyboardsCB.setFont(dialogFont);

     phoneticTF.requestFocus();

     cutbutton = new JButton();
     cutbutton.setFont(dialogFont);
     cutbutton.setText("Cut");
     cutbutton.setToolTipText("Cut to Clipboard");
     keyAndFontPanel.add(cutbutton);
     cutbutton.addActionListener(new ActionListener()
     {
         public void actionPerformed(ActionEvent e)
         { textArea.cut();
         }
     });

     copybutton = new JButton();
     copybutton.setFont(dialogFont);
     copybutton.setText("Copy");
     copybutton.setToolTipText("Copy to Clipboard");
     keyAndFontPanel.add(copybutton);
     copybutton.addActionListener(new ActionListener()
     {
         public void actionPerformed(ActionEvent e)
         { textArea.copy();
         }
     });

     pastebutton = new JButton();
     pastebutton.setFont(dialogFont);
     pastebutton.setText("Paste");
     pastebutton.setToolTipText("Paste from Clipboard");
     keyAndFontPanel.add(pastebutton);
     pastebutton.addActionListener(new ActionListener()
     {
         public void actionPerformed(ActionEvent e)
         { textArea.paste();
         }
     });



//     EventQueue.invokeLater(new Runnable()
//     {
//       public void run()
//       {
         keyboardDialog.setLocation(300,100);
         keyboardDialog.setVisible(true);
//     }});


  }

public static void dodialog(){
     if(keyboardDialog!=null){keyboardDialog.dispose();
     keyboardDialog=null;}
     keyboardDialog = new JDialog(parentFrame, "Virtual Keyboard", false);
     keyboardDialog.setDefaultCloseOperation(2);
     keyboardDialog.getContentPane().setLayout(new BorderLayout());
     keyboardDialog.getContentPane().add(keyboardPanel);
     keyboardDialog.addWindowListener(new WindowAdapter()
     {
       public void windowActivated(WindowEvent e)
       {
          viewKeyboardButton.setText("Hide Keyboard");
          viewKeyboardButton.setEnabled(false);
       }
       public void windowClosing(WindowEvent e)
       {
          viewKeyboardButton.setText("Show Keyboard");
          viewKeyboardButton.setEnabled(true);
	  keyboardDialog.dispose();
       }
     });
         keyboardDialog.setLocation(300,100);
         keyboardDialog.pack();
				}

  public static void doKeyboard()
  {
       final JFrame jf = new JFrame("PKT Virtual Unicode Keyboard");
       final Keyboard keyboard = new Keyboard(jf, null, null);
       jf.getContentPane().add(keyboard);
       //keyboard.parentFrame=jf;
       jf.addWindowListener(new WindowAdapter()
       {
          public void windowClosing( WindowEvent e) { doExit();jf.dispose(); }  // this one is important
       });
       jf.pack();
       jf.setSize(480,300);
       jf.setVisible(true);
  }

public static void doExit(){keyboardDialog.dispose();parentFrame.dispose();}

  private void keyboardCBAction()
  {
      int sel = keyboardsCB.getSelectedIndex();
      if(sel>=0)
      {
          createKeyboard(keyboards[sel], lines[sel], cols[sel], showLimit[sel]);
      }
  }


  private void setFonts()
  {
     setFonts((String) fontsCB.getSelectedItem());
  }

  private void setFonts(String fontName)
  {
     actualFont = fontName;
     int size = Integer.parseInt(fontSizeTF.getText());
     Font font = new Font(fontName, Font.PLAIN, size);
     textArea.setFont(font);
     keyboardPanel.setFont(font);
     for(int i=0; i<keyboardPanel.getComponentCount(); i++)
     {
        keyboardPanel.getComponent(i).setFont(font);
     }

/*     Dimension dim = new Dimension(size*40, size*10);

     textArea.setSize(dim);
     textArea.setMinimumSize(dim);
     textArea.setMaximumSize(dim);
     textArea.setPreferredSize(dim);

     scrollPane.setSize(dim);
     scrollPane.setMinimumSize(dim);
     scrollPane.setMaximumSize(dim);
     scrollPane.setPreferredSize(dim);*/

     keyboardDialog.pack();

/*     if(parentFrame!=null)
     {
        parentFrame.pack();
     }*/
     updateUI();

  }

  private void createKeyboard(String[][] letters, int lines, int cols, int showLimit)
  {
   if(keyboardDialog==null){dodialog();}
     // use the first letter to detect which fonts can represent this char
     supportedFontNames = getFontsSupporting(allFontNames, (char) Integer.parseInt(letters[0][0], 16));
     if(supportedFontNames.length==0)
     {
         String kse = (String) keyboardsCB.getSelectedItem();
         JOptionPane.showMessageDialog(this, "No font supporting the "+kse+" keyboard is installed on your system.\nYou have to install one, for example, \"Unicode-16 Standard\" from the Unicode Consortium.");
         supportedFontNames = new String[]{"Dialog"};
     }

     matchFound.removeAllElements();
     actualKeyboard = letters;
     keyboardPanel.removeAll();
     GridLayout grl = new GridLayout(lines,cols);
     keyboardPanel.setLayout(grl);

     // only display up to the limit, other keys, usually small letters
     // are only accessed through the phonetic type
     int limit = Math.min(showLimit, letters.length);
     for(int i=0; i<limit; i++)
     {
        if( letters[i] !=null)
        {
            int nc = Integer.parseInt(letters[i][0], 16);
            JButton jb = new JButton(""+(char)nc);
            jb.setBorder(BorderFactory.createRaisedBevelBorder());
            keyboardPanel.add(jb);
            String ttt = letters[i][1];
            if(letters[i].length>2) ttt+=" ["+letters[i][2]+"]";
            jb.setToolTipText(ttt);
            jb.addActionListener(this);
        }
        else
        {
            keyboardPanel.add(new JPanel());
        }
     }

     keyboardDialog.pack();
     keyboardDialog.setTitle((String)keyboardsCB.getSelectedItem()+" Keyboard");

     fontComboBoxModel.setSelectedItem(supportedFontNames[0]);
     fontsCB.setSelectedItem(supportedFontNames[0]);
  }

  /** a button of the kyeboard pressed
   */
  public synchronized void actionPerformed(ActionEvent e)
  {
     JButton src = (JButton)e.getSource();
     try
     {
      textArea.getDocument().insertString(textArea.getCaretPosition(),src.getText(),null);
     }
     catch(Exception ex)
     {
      ex.printStackTrace();
     }
  }


  public static void main(String[] args)
  {
  }
  //
  // Phonetic field pressed
  //
  public void keyPressed(java.awt.event.KeyEvent keyEvent)     {    }

  public synchronized void keyReleased(java.awt.event.KeyEvent keyEvent)
  {
     processPhoneticBuffer();
  }

  public void keyTyped(java.awt.event.KeyEvent keyEvent)
  {
     // key released only is not sufficent, we need this too
     processPhoneticBuffer();      
     //System.out.println("char="+(int) keyEvent.getKeyChar());
  }
  
  final Vector matchFound = new Vector();
  
  private synchronized void processPhoneticBuffer()
  {
     String buf = phoneticTF.getText();
     Vector startWithFound = new Vector();
     
     for(int i=0; i<actualKeyboard.length; i++)
     {
        // only if the key has phonetic infotmation
        if(actualKeyboard[i]!=null && actualKeyboard[i].length>2)
        {
           if(actualKeyboard[i][2].startsWith(buf))
           {
               startWithFound.addElement(new Integer(i));
               if(actualKeyboard[i][2].equals(buf))
               {
                   matchFound.addElement(new Integer(i));
               }
           }
        }
     }
     // if only one were found : BINGO. 
     completionLabel.setText("");
     if(startWithFound.size()==1)
     {
        int i = ((Integer) startWithFound.elementAt(0)).intValue();
        phoneticTF.setText("");
        matchFound.removeAllElements();
        int nc = Integer.parseInt(actualKeyboard[i][0], 16);
        try
        {
         textArea.getDocument().insertString(textArea.getCaretPosition(),""+(char)nc,null);
        }
        catch(Exception ex)
        {
         ex.printStackTrace();
        }
     }
     else if(startWithFound.size()>1)
     { 
        StringBuffer completion = new StringBuffer();
        for(int j=0; j< startWithFound.size(); j++)
        {
           int i = ((Integer) startWithFound.elementAt(j)).intValue();
           completion.append(actualKeyboard[i][2]);
           completion.append(" ");
        }
        completionLabel.setText(completion.toString());
     }
     else if(startWithFound.size()==0)
     {
        // nothing found, look at previous old matches  
        if(matchFound.size()>0)
        {
           int i = ((Integer) matchFound.elementAt(0)).intValue();
           // remove the chars corresponding to the recognized
           String tmp = phoneticTF.getText();
           phoneticTF.setText( tmp.substring(actualKeyboard[i][2].length()));
           matchFound.removeAllElements();
           int nc = Integer.parseInt(actualKeyboard[i][0], 16);
           try
           {
            textArea.getDocument().insertString(textArea.getCaretPosition(),""+(char)nc,null);
           }
           catch(Exception ex)
           {
            ex.printStackTrace();
           }

           // must reparse
           processPhoneticBuffer();
        }
        else
        {
           completionLabel.setText("No Completion");
        }
     }
  }
  
  public String[] getFontsSupporting(String[] fontNames, char ch)
  {
    Vector supportingFonts = new Vector();
    for(int i=0; i<fontNames.length; i++)
    {
        Font f = new Font(fontNames[i], Font.PLAIN, 12);
        if(f.canDisplay(ch)) supportingFonts.addElement(fontNames[i]);
    }
    String[] rep = new String[supportingFonts.size()];
    supportingFonts.copyInto(rep);
    return rep;
  }
  
  //
  // ComboBoxModel
  //
 
 class FontComboBoxModel implements ComboBoxModel
 {
      private String selectedItem=null;     
      final private Vector listeners = new Vector();
      
      public FontComboBoxModel()
      {
         selectedItem=supportedFontNames[0];
      }

      public void addListDataListener(javax.swing.event.ListDataListener listDataListener)
      {
          listeners.addElement(listDataListener);
      }

      public Object getElementAt(int param)   { return supportedFontNames[param];  }

      public Object getSelectedItem() {   return selectedItem;   }

      public int getSize()        {  return supportedFontNames.length;   }

      public void removeListDataListener(ListDataListener listDataListener)
      {
          listeners.removeElement(listDataListener);
      }
      public void setSelectedItem(Object obj)
      {
          selectedItem = (String) obj;
          for(int i=0; i<listeners.size(); i++)
          {
             ListDataListener ldl = (ListDataListener)listeners.elementAt(i);
             ldl.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED, 0,supportedFontNames.length));
          }
      }
  
 }
 
  // the actions
  class BrowseUnicodeAction extends ActionButton
  {
      public BrowseUnicodeAction()
      {
         super("Browse Unicode", null);
      }

      public void actionPerformed(java.awt.event.ActionEvent actionEvent)
      {
          //new UnicodeViewer(false);
      }
      
  }
 
  //
  // SOME PREDEFINED KEYBOARDS
  //
  final public static String[][] russianLetters = {
    {"0410", "CYRILLIC CAPITAL LETTER A", "A"},
    {"0411", "CYRILLIC CAPITAL LETTER BE", "B"},
    {"0412", "CYRILLIC CAPITAL LETTER VE", "V"},
    {"0413", "CYRILLIC CAPITAL LETTER GHE", "G"},
    {"0414", "CYRILLIC CAPITAL LETTER DE", "D"},
    {"0415", "CYRILLIC CAPITAL LETTER IE", "IE"},
    {"0401", "CYRILLIC CAPITAL LETTER IO", "IO"},
    {"0416", "CYRILLIC CAPITAL LETTER ZHE", "J"},
    {"0417", "CYRILLIC CAPITAL LETTER ZE", "Z"},
    {"0418", "CYRILLIC CAPITAL LETTER I", "I"},
    {"0419", "CYRILLIC CAPITAL LETTER SHORT I", "IS"},
    {"041A", "CYRILLIC CAPITAL LETTER KA", "K"},
    {"041B", "CYRILLIC CAPITAL LETTER EL", "L"},
    {"041C", "CYRILLIC CAPITAL LETTER EM", "M"},
    {"041D", "CYRILLIC CAPITAL LETTER EN", "N"},
    {"041E", "CYRILLIC CAPITAL LETTER O", "O"},
    {"041F", "CYRILLIC CAPITAL LETTER PE", "P"},
    {"0420", "CYRILLIC CAPITAL LETTER ER", "R"},
    {"0421", "CYRILLIC CAPITAL LETTER ES", "S"},
    {"0422", "CYRILLIC CAPITAL LETTER TE", "T"},
    {"0423", "CYRILLIC CAPITAL LETTER U", "U"},
    {"0424", "CYRILLIC CAPITAL LETTER EF", "F"},
    {"0425", "CYRILLIC CAPITAL LETTER HA", "X"},
    {"0426", "CYRILLIC CAPITAL LETTER TSE", "TS"},
    {"0427", "CYRILLIC CAPITAL LETTER CHE", "CH"},
    {"0428", "CYRILLIC CAPITAL LETTER SHA", "SH"},
    {"0429", "CYRILLIC CAPITAL LETTER SHCHA", "SSH"},
    {"042A", "CYRILLIC CAPITAL LETTER HARD SIGN"},
    {"042B", "CYRILLIC CAPITAL LETTER YERU"},
    {"042C", "CYRILLIC CAPITAL LETTER SOFT SIGN"},
    {"042D", "CYRILLIC CAPITAL LETTER E", "E"},
    {"042E", "CYRILLIC CAPITAL LETTER YU", "OU"},
    {"042F", "CYRILLIC CAPITAL LETTER YA", "IA"},
    {"0020", "SPACE", " "},
    {"0301", "COMBINING ACCUTE ACCENT", "'"},
    
    
    {"0430", "CYRILLIC SMALL LETTER A", "a"},
    {"0431", "CYRILLIC SMALL LETTER BE", "b"},
    {"0432", "CYRILLIC SMALL LETTER VE", "v"},
    {"0433", "CYRILLIC SMALL LETTER GHE", "g"},
    {"0434", "CYRILLIC SMALL LETTER DE", "d"},
    {"0435", "CYRILLIC SMALL LETTER IE", "ie"},
    {"0451", "CYRILLIC SMALL LETTER IO", "io"},
    {"0436", "CYRILLIC SMALL LETTER ZHE", "j"},
    {"0437", "CYRILLIC SMALL LETTER ZE", "z"},
    {"0438", "CYRILLIC SMALL LETTER I", "i"},
    {"0439", "CYRILLIC SMALL LETTER SHORT I", "is"},
    {"043A", "CYRILLIC SMALL LETTER KA", "k"},
    {"043B", "CYRILLIC SMALL LETTER EL", "l"},
    {"043C", "CYRILLIC SMALL LETTER EM", "m"},
    {"043D", "CYRILLIC SMALL LETTER EN", "n"},
    {"043E", "CYRILLIC SMALL LETTER O", "o"},
    {"043F", "CYRILLIC SMALL LETTER PE", "p"},
    {"0440", "CYRILLIC SMALL LETTER ER", "r"},
    {"0441", "CYRILLIC SMALL LETTER ES", "s"},
    {"0442", "CYRILLIC SMALL LETTER TE", "t"},
    {"0443", "CYRILLIC SMALL LETTER U", "u"},
    {"0444", "CYRILLIC SMALL LETTER EF", "f"},
    {"0445", "CYRILLIC SMALL LETTER HA", "x"},
    {"0446", "CYRILLIC SMALL LETTER TSE", "ts"},
    {"0447", "CYRILLIC SMALL LETTER CHE", "ch"},
    {"0448", "CYRILLIC SMALL LETTER SHA", "sh"},
    {"0449", "CYRILLIC SMALL LETTER SHCHA", "ssh"},
    {"044A", "CYRILLIC SMALL LETTER HARD SIGN"},
    {"044B", "CYRILLIC SMALL LETTER YERU"},
    {"044C", "CYRILLIC SMALL LETTER SOFT SIGN"},
    {"044D", "CYRILLIC SMALL LETTER E", "e"},
    {"044E", "CYRILLIC SMALL LETTER YU", "ou"},
    {"044F", "CYRILLIC SMALL LETTER YA", "ia"}    
  };

  final public static String[][] hiraganaLetters = {
    {"3042", "HIRAGANA LETTER A", "A"},
    {"3044", "HIRAGANA LETTER I", "I"},
    {"3046", "HIRAGANA LETTER U", "U"},
    {"3048", "HIRAGANA LETTER E", "E"},
    {"304A", "HIRAGANA LETTER O", "O"},
    {"304B", "HIRAGANA LETTER KA", "KA"},
    {"304D", "HIRAGANA LETTER KI", "KI"},
    {"304F", "HIRAGANA LETTER KU", "KU"},
    {"3051", "HIRAGANA LETTER KE", "KE"},
    {"3053", "HIRAGANA LETTER KO", "KO"},
    {"304C", "HIRAGANA LETTER GA", "GA"},
    {"304E", "HIRAGANA LETTER GI", "GI"},
    {"3050", "HIRAGANA LETTER GU", "GU"},
    {"3052", "HIRAGANA LETTER GE", "GE"},
    {"3054", "HIRAGANA LETTER GO", "GO"},
    {"3055", "HIRAGANA LETTER SA", "SA"},
    {"3057", "HIRAGANA LETTER SHI", "SHI"},
    {"3059", "HIRAGANA LETTER SU", "SU"},
    {"305B", "HIRAGANA LETTER SE", "SE"},
    {"305D", "HIRAGANA LETTER SO", "SO"},
    {"3056", "HIRAGANA LETTER ZA", "ZA"},
    {"3058", "HIRAGANA LETTER JI", "JI"},
    {"305A", "HIRAGANA LETTER ZU", "ZU"},
    {"305C", "HIRAGANA LETTER ZE", "ZE"},
    {"305E", "HIRAGANA LETTER ZO", "ZO"},
    {"305F", "HIRAGANA LETTER TA", "TA"},
    {"3061", "HIRAGANA LETTER TI", "TI"},
    {"3064", "HIRAGANA LETTER TSU", "TSU"},
    {"3066", "HIRAGANA LETTER TE", "TE"},
    {"3068", "HIRAGANA LETTER TO", "TO"},
    {"3060", "HIRAGANA LETTER DA", "DA"},
    {"3062", "HIRAGANA LETTER DJI", "DJI"},
    {"3065", "HIRAGANA LETTER DU", "DU"},
    {"3067", "HIRAGANA LETTER DE", "DE"},
    {"3069", "HIRAGANA LETTER DO", "DO"},
    {"306A", "HIRAGANA LETTER NA", "NA"},
    {"306B", "HIRAGANA LETTER NI", "NI"},
    {"306C", "HIRAGANA LETTER NU", "NU"},
    {"306D", "HIRAGANA LETTER NE", "NE"},
    {"306E", "HIRAGANA LETTER NO", "NO"},
    {"306F", "HIRAGANA LETTER HA", "HA"},
    {"3072", "HIRAGANA LETTER HI", "HI"},
    {"3075", "HIRAGANA LETTER FU", "FU"},
    {"3078", "HIRAGANA LETTER HE", "HE"},
    {"307B", "HIRAGANA LETTER HO", "HO"},
    {"3070", "HIRAGANA LETTER BA", "BA"},
    {"3073", "HIRAGANA LETTER BI", "BI"},
    {"3076", "HIRAGANA LETTER BU", "BU"},
    {"3079", "HIRAGANA LETTER BE", "BE"},
    {"307C", "HIRAGANA LETTER BO", "BO"},
    {"3071", "HIRAGANA LETTER PA", "PA"},
    {"3074", "HIRAGANA LETTER PI", "PI"},
    {"3077", "HIRAGANA LETTER PU", "PU"},
    {"307A", "HIRAGANA LETTER PE", "PE"},
    {"307D", "HIRAGANA LETTER PO", "PO"},
    {"307E", "HIRAGANA LETTER MA", "MA"},
    {"307F", "HIRAGANA LETTER MI", "MI"},
    {"3080", "HIRAGANA LETTER MU", "MU"},
    {"3081", "HIRAGANA LETTER ME", "ME"},
    {"3082", "HIRAGANA LETTER MO", "MO"},
    {"3084", "HIRAGANA LETTER YA", "YA"},
    null,
    {"3086", "HIRAGANA LETTER YU", "YU"},
    null,
    {"3088", "HIRAGANA LETTER YO", "YO"},
    {"3089", "HIRAGANA LETTER RA", "RA"},
    {"308A", "HIRAGANA LETTER RI", "RI"},
    {"308B", "HIRAGANA LETTER RU", "RU"},
    {"308C", "HIRAGANA LETTER RE", "RE"},
    {"308D", "HIRAGANA LETTER RO", "RO"},
    {"308F", "HIRAGANA LETTER WA", "WA"},
    null,
    null,
    null,
    {"3092", "HIRAGANA LETTER WO", "WO"},
    {"3093", "HIRAGANA LETTER N", "N"},
    null,
    {"3094", "HIRAGANA LETTER VU", "VU"},
    null,
   {"0020", "SPACE", " "},
    null,
    null,
    null,
    null,
    null,
    {"3041", "HIRAGANA LETTER SMALL A", "a"},
    {"3043", "HIRAGANA LETTER SMALL I", "i"},
    {"3045", "HIRAGANA LETTER SMALL U", "u"},
    {"3047", "HIRAGANA LETTER SMALL E", "e"},
    {"3049", "HIRAGANA LETTER SMALL O", "o"},
    {"3063", "HIRAGANA LETTER SMALL TU", "tu"},
    {"3083", "HIRAGANA LETTER SMALL YA", "ya"},
    {"3085", "HIRAGANA LETTER SMALL YU", "yu"},
    {"3087", "HIRAGANA LETTER SMALL YO", "yo"},
    {"308E", "HIRAGANA LETTER SMALL WA", "wa"},
    {"30FC", "KATAKANA-HIRAGANA PROLONGED SOUND MARK"},
    {"FF70", "HALFWIDTH KATAKANA-HIRAGANA PROLONGED SOUND MARK"}
  };
  
  
  final public static String[][] katakanaLetters = {
    {"30A2", "KATAKANA LETTER A", "A"},
    {"30A4", "KATAKANA LETTER I", "I"},
    {"30A6", "KATAKANA LETTER U", "U"},
    {"30A8", "KATAKANA LETTER E", "E"},
    {"30AA", "KATAKANA LETTER O", "O"},
    {"30AB", "KATAKANA LETTER KA", "KA"},
    {"30AD", "KATAKANA LETTER KI", "KI"},
    {"30AF", "KATAKANA LETTER KU", "KU"},
    {"30B1", "KATAKANA LETTER KE", "KE"},
    {"30B3", "KATAKANA LETTER KO", "KO"},
    {"30AC", "KATAKANA LETTER GA", "GA"},
    {"30AE", "KATAKANA LETTER GI", "GI"},
    {"30B0", "KATAKANA LETTER GU", "GU"},
    {"30B2", "KATAKANA LETTER GE", "GE"},
    {"30B4", "KATAKANA LETTER GO", "GO"},
    {"30B5", "KATAKANA LETTER SA", "SA"},
    {"30B7", "KATAKANA LETTER SI", "SI"},
    {"30B9", "KATAKANA LETTER SU", "SU"},
    {"30BB", "KATAKANA LETTER SE", "SE"},
    {"30BD", "KATAKANA LETTER SO", "SO"},
    {"30B6", "KATAKANA LETTER ZA", "ZA"},
    {"30B8", "KATAKANA LETTER ZI", "ZI"},
    {"30BA", "KATAKANA LETTER ZU", "ZU"},
    {"30BC", "KATAKANA LETTER ZE", "ZE"},
    {"30BE", "KATAKANA LETTER ZO", "ZO"},
    {"30BF", "KATAKANA LETTER TA", "TA"},
    {"30C1", "KATAKANA LETTER TI", "TI"},
    {"30C4", "KATAKANA LETTER TSU", "TSU"},
    {"30C6", "KATAKANA LETTER TE", "TE"},
    {"30C8", "KATAKANA LETTER TO", "TO"},
    {"30C0", "KATAKANA LETTER DA", "DA"},
    {"30C2", "KATAKANA LETTER DJI", "DJI"},
    {"30C5", "KATAKANA LETTER DU", "DU"},
    {"30C7", "KATAKANA LETTER DE", "DE"},
    {"30C9", "KATAKANA LETTER DO", "DO"},
    {"30CA", "KATAKANA LETTER NA", "NA"},
    {"30CB", "KATAKANA LETTER NI", "NI"},
    {"30CC", "KATAKANA LETTER NU", "NU"},
    {"30CD", "KATAKANA LETTER NE", "NE"},
    {"30CE", "KATAKANA LETTER NO", "NO"},
    {"30CF", "KATAKANA LETTER HA", "HA"},
    {"30D2", "KATAKANA LETTER HI", "HI"},
    {"30D5", "KATAKANA LETTER HU", "HU"},
    {"30D8", "KATAKANA LETTER HE", "HE"},
    {"30DB", "KATAKANA LETTER HO", "HO"},
    {"30D0", "KATAKANA LETTER BA", "BA"},
    {"30D3", "KATAKANA LETTER BI", "BI"},
    {"30D6", "KATAKANA LETTER BU", "BU"},
    {"30D9", "KATAKANA LETTER BE", "BE"},
    {"30DC", "KATAKANA LETTER BO", "BO"},
    {"30D1", "KATAKANA LETTER PA", "PA"},
    {"30D4", "KATAKANA LETTER PI", "PI"},
    {"30D7", "KATAKANA LETTER PU", "PU"},
    {"30DA", "KATAKANA LETTER PE", "PE"},
    {"30DD", "KATAKANA LETTER PO", "PO"},
    {"30DE", "KATAKANA LETTER MA", "MA"},
    {"30DF", "KATAKANA LETTER MI", "MI"},
    {"30E0", "KATAKANA LETTER MU", "MU"},
    {"30E1", "KATAKANA LETTER ME", "ME"},
    {"30E2", "KATAKANA LETTER MO", "MO"},
    {"30E4", "KATAKANA LETTER YA", "YA"},
    null,
    {"30E6", "KATAKANA LETTER YU", "YU"},
    null,
    {"30E8", "KATAKANA LETTER YO", "YO"},
    {"30E9", "KATAKANA LETTER RA", "RA"},
    {"30EA", "KATAKANA LETTER RI", "RI"},
    {"30EB", "KATAKANA LETTER RU", "RU"},
    {"30EC", "KATAKANA LETTER RE", "RE"},
    {"30ED", "KATAKANA LETTER RO", "RO"},
    {"30EF", "KATAKANA LETTER WA", "WA"},
    null,
    null,
    null,
    {"30F2", "KATAKANA LETTER WO", "WO"},
    {"30F3", "KATAKANA LETTER N", "N"},
    null,
    {"0020", "SPACE", " "},
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {"30A1", "KATAKANA LETTER SMALL A", "a"},
    {"30A3", "KATAKANA LETTER SMALL I", "i"},
    {"30A5", "KATAKANA LETTER SMALL U", "u"},
    {"30A7", "KATAKANA LETTER SMALL E", "e"},
    {"30A9", "KATAKANA LETTER SMALL O", "o"},
    {"30C3", "KATAKANA LETTER SMALL TSU", "tsu"},
    {"30E3", "KATAKANA LETTER SMALL YA", "ya"},
    {"30E5", "KATAKANA LETTER SMALL YU", "yu"},
    {"30E7", "KATAKANA LETTER SMALL YO", "yo"},
    {"30EE", "KATAKANA LETTER SMALL WA", "wa"},
    {"30F5", "KATAKANA LETTER SMALL KA", "ka"},
    {"30F6", "KATAKANA LETTER SMALL KE", "ke"},
    {"30FB", "KATAKANA MIDDLE DOT"},
    {"30FC", "KATAKANA-HIRAGANA PROLONGED SOUND MARK"}
  };

  final public static String[][] englishLetters = 
  {
      {"0071", "ENGLISH LETTER Q", "q"},
      {"0077", "ENGLISH LETTER W", "w"}, 
      {"0065", "ENGLISH LETTER E", "e"},
      {"0072", "ENGLISH LETTER R", "r"},
      {"0074", "ENGLISH LETTER T", "t"},
      {"0079", "ENGLISH LETTER Y", "y"},
      {"0075", "ENGLISH LETTER U", "u"},
      {"0069", "ENGLISH LETTER I", "i"},
      {"006F", "ENGLISH LETTER O", "o"},
      {"0070", "ENGLISH LETTER P", "p"},
      null,
      {"0061", "ENGLISH LETTER A", "a"},
      {"0073", "ENGLISH LETTER S", "s"},
      {"0064", "ENGLISH LETTER D", "d"},
      {"0066", "ENGLISH LETTER F", "f"},
      {"0067", "ENGLISH LETTER G", "g"},
      {"0068", "ENGLISH LETTER H", "h"},
      {"006A", "ENGLISH LETTER J", "j"},
      {"006B", "ENGLISH LETTER K", "k"},
      {"006C", "ENGLISH LETTER L", "l"},
      null,
      null,
      {"007A", "ENGLISH LETTER Z", "z"},
      {"0078", "ENGLISH LETTER X", "x"},
      {"0063", "ENGLISH LETTER C", "c"},
      {"0076", "ENGLISH LETTER V", "v"},
      {"0062", "ENGLISH LETTER B", "b"},
      {"006E", "ENGLISH LETTER N", "n"},
      {"006D", "ENGLISH LETTER M", "m"},
      null,
      {"0030", "ENGLISH DIGIT ZERO","0"},
      {"0031", "ENGLISH DIGIT ONE","1"},
      {"0032", "ENGLISH DIGIT TWO","2"},
      {"0033", "ENGLISH DIGIT THREE","3"},
      {"0034", "ENGLISH DIGIT FOUR","4"},
      {"0035", "ENGLISH DIGIT FIVE","5"},
      {"0036", "ENGLISH DIGIT SIX","6"},
      {"0037", "ENGLISH DIGIT SEVEN","7"},
      {"0038", "ENGLISH DIGIT EIGHT","8"},
      {"0039", "ENGLISH DIGIT NINE","9"},
      null,
      {"002E", "ENGLISH DECIMAL SEPARATOR", "."},
      {"002C", "ENGLISH THOUSANDS SEPARATOR",","},      
      null,
      null,
      {"0020", "SPACE", " "}
  };

  final public static String[][] arabicLetters = 
  {
      {"0627", "ARABIC LETTER ALEF", "a"},
      {"0628", "ARABIC LETTER BEH", "b"}, 
      {"0629", "ARABIC LETTER TEH MARBUTA", "tm"},
      {"062A", "ARABIC LETTER TEH", "t"},
      {"062B", "ARABIC LETTER THEH", "th"},
      {"062C", "ARABIC LETTER JEEM", "j"},
      {"062D", "ARABIC LETTER HAH", "h-"},
      {"062E", "ARABIC LETTER KHAH", "kh"},
      {"062F", "ARABIC LETTER DAL", "d"},
      {"0630", "ARABIC LETTER THAL", "dh"},
      {"0631", "ARABIC LETTER REH", "r"},
      {"0632", "ARABIC LETTER ZAIN", "z"},
      {"0633", "ARABIC LETTER SEEN", "s"},
      {"0634", "ARABIC LETTER SHEEN", "ch"},
      {"0635", "ARABIC LETTER SAD", "ss"},
      {"0636", "ARABIC LETTER DAD", "dd"},
      {"0637", "ARABIC LETTER TAH", "tt"},
      {"0638", "ARABIC LETTER ZAH", "dhh"},
      {"0639", "ARABIC LETTER AIN", "'a"},
      {"063A", "ARABIC LETTER GHAIN", "r2"},
      {"0641", "ARABIC LETTER FEH", "f"},
      {"0642", "ARABIC LETTER QAF", "q'"},
      {"0643", "ARABIC LETTER KAF", "k"},
      {"0644", "ARABIC LETTER LAM", "l"},
      {"0645", "ARABIC LETTER MEEM", "m"},
      {"0646", "ARABIC LETTER NOON", "n"},
      {"0647", "ARABIC LETTER HEH", "h"},
      {"0648", "ARABIC LETTER WAW", "ou"},
      {"064A", "ARABIC LETTER YEH", "y"},
      null,
      {"0660", "ARABIC-INDIC DIGIT ZERO","0"},
      {"0661", "ARABIC-INDIC DIGIT ONE","1"},
      {"0662", "ARABIC-INDIC DIGIT TWO","2"},
      {"0663", "ARABIC-INDIC DIGIT THREE","3"},
      {"0664", "ARABIC-INDIC DIGIT FOUR","4"},
      {"0665", "ARABIC-INDIC DIGIT FIVE","5"},
      {"0666", "ARABIC-INDIC DIGIT SIX","6"},
      {"0667", "ARABIC-INDIC DIGIT SEVEN","7"},
      {"0668", "ARABIC-INDIC DIGIT EIGHT","8"},
      {"0669", "ARABIC-INDIC DIGIT NINE","9"},
      {"066B", "ARABIC DECIMAL SEPARATOR"},
      {"066C", "ARABIC THOUSANDS SEPARATOR"},      
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      {"0020", "SPACE", " "}
  };

  final public static String[][] tamilLetters = 
  {
    {"0B85", "TAMIL LETTER A", "A"},
    {"0B86", "TAMIL LETTER AA", "AA"},
    {"0B87", "TAMIL LETTER I", "I"},
    {"0B88", "TAMIL LETTER II", "II"},
    {"0B89", "TAMIL LETTER U","U"},
    {"0B8A", "TAMIL LETTER UU","UU"},
    {"0B8E", "TAMIL LETTER E","E"},
    {"0B8F", "TAMIL LETTER EE","EE"},
    {"0B90", "TAMIL LETTER AI","AI"},
    {"0B92", "TAMIL LETTER O","O"},
    {"0B93", "TAMIL LETTER OO","OO"},
    {"0B94", "TAMIL LETTER AU","AU"},
    {"0B95", "TAMIL LETTER KA","KA"},
    {"0B99", "TAMIL LETTER NGA","NGA"},
    {"0B9A", "TAMIL LETTER CA","CA"},
    {"0B9C", "TAMIL LETTER JA","JA"},
    {"0B9E", "TAMIL LETTER NYA","NYA"},
    {"0B9F", "TAMIL LETTER TTA","TTA"},
    {"0BA3", "TAMIL LETTER NNA","NNA"},
    {"0BA4", "TAMIL LETTER TA","TA"},
    {"0BA8", "TAMIL LETTER NA","NA"},
    {"0BA9", "TAMIL LETTER NNNA","NNNA"},
    {"0BAA", "TAMIL LETTER PA","PA"},
    {"0BAE", "TAMIL LETTER MA","MA"},
    {"0BAF", "TAMIL LETTER YA","YA"},
    {"0BB0", "TAMIL LETTER RA","RA"},
    {"0BB1", "TAMIL LETTER RRA","RRA"},
    {"0BB2", "TAMIL LETTER LA","LA"},
    {"0BB3", "TAMIL LETTER LLA","LLA"},
    {"0BB4", "TAMIL LETTER LLLA","LLLA"},
    {"0BB5", "TAMIL LETTER VA","VA"},
    {"0BB7", "TAMIL LETTER SSA","SSA"},
    {"0BB8", "TAMIL LETTER SA","SA"},
    {"0BB9", "TAMIL LETTER HA","HA"},
    {"0BBE", "TAMIL VOWEL SIGN AA"},
    {"0BBF", "TAMIL VOWEL SIGN I"},
    {"0BC0", "TAMIL VOWEL SIGN II"},
    {"0BC1", "TAMIL VOWEL SIGN U"},
    {"0BC2", "TAMIL VOWEL SIGN UU"},
    {"0BC6", "TAMIL VOWEL SIGN E"},
    {"0BC7", "TAMIL VOWEL SIGN EE"},
    {"0BC8", "TAMIL VOWEL SIGN AI"},
    {"0BCA", "TAMIL VOWEL SIGN O"},
    {"0BCB", "TAMIL VOWEL SIGN OO"},
    {"0BCC", "TAMIL VOWEL SIGN AU"},
    {"0BCD", "TAMIL SIGN VIRAMA","VI"},
    {"0BD7", "TAMIL AU LENGTH MARK"},
    {"0BE7", "TAMIL DIGIT ONE", "1"},
    {"0BE8", "TAMIL DIGIT TWO", "2"},
    {"0BE9", "TAMIL DIGIT THREE", "3"},
    {"0BEA", "TAMIL DIGIT FOUR", "4"},
    {"0BEB", "TAMIL DIGIT FIVE", "5"},
    {"0BEC", "TAMIL DIGIT SIX", "6"},
    {"0BED", "TAMIL DIGIT SEVEN", "7"},
    {"0BEE", "TAMIL DIGIT EIGHT", "8"},
    {"0BEF", "TAMIL DIGIT NINE", "9"},
    {"0BF0", "TAMIL NUMBER TEN", "10"},
    {"0BF1", "TAMIL NUMBER ONE HUNDRED", "100"},
    {"0BF2", "TAMIL NUMBER ONE THOUSAND", "1000"},
    {"0B82", "TAMIL SIGN ANUSVARA"},
    {"0B83", "TAMIL SIGN VISARGA"},
    {"0020", "SPACE", " "}
  };
  
  
  final public static String[][] hebrewLetters = 
  {
    {"05D0", "HEBREW LETTER ALEF"},
    {"05D1", "HEBREW LETTER BET"},
    {"05D2", "HEBREW LETTER GIMEL"},
    {"05D3", "HEBREW LETTER DALET"},
    {"05D4", "HEBREW LETTER HE"},
    {"05D5", "HEBREW LETTER VAV"},
    {"05D6", "HEBREW LETTER ZAYIN"},
    {"05D7", "HEBREW LETTER HET"},
    {"05D8", "HEBREW LETTER TET"},
    {"05D9", "HEBREW LETTER YOD"},
    {"05DA", "HEBREW LETTER FINAL KAF"},
    {"05DB", "HEBREW LETTER KAF"},
    {"05DC", "HEBREW LETTER LAMED"},
    {"05DD", "HEBREW LETTER FINAL MEM"},
    {"05DE", "HEBREW LETTER MEM"},
    {"05DF", "HEBREW LETTER FINAL NUN"},
    {"05E0", "HEBREW LETTER NUN"},
    {"05E1", "HEBREW LETTER SAMEKH"},
    {"05E2", "HEBREW LETTER AYIN"},
    {"05E3", "HEBREW LETTER FINAL PE"},
    {"05E4", "HEBREW LETTER PE"},
    {"05E5", "HEBREW LETTER FINAL TSADI"},
    {"05E6", "HEBREW LETTER TSADI"},
    {"05E7", "HEBREW LETTER QOF"},
    {"05E8", "HEBREW LETTER RESH"},
    {"05E9", "HEBREW LETTER SHIN"},
    {"05EA", "HEBREW LETTER TAV"},
    {"05F3", "HEBREW PUNCTUATION GERESH"},
    {"05F4", "HEBREW PUNCTUATION GERSHAYIM"},
    {"FB1E", "HEBREW POINT JUDEO-SPANISH VARIKA"},
    {"FB20", "HEBREW LETTER ALTERNATIVE AYIN"},
    {"FB29", "HEBREW LETTER ALTERNATIVE PLUS SIGN"},
    {"0020", "SPACE", " "}
  };

  final public static String[][] greekLetters = 
 {
    {"03B1", "GREEK SMALL LETTER ALPHA","a"},
    {"03B2", "GREEK SMALL LETTER BETA", "b"},
    {"03B3", "GREEK SMALL LETTER GAMMA","g"},
    {"03B4", "GREEK SMALL LETTER DELTA","d"},
    {"03B5", "GREEK SMALL LETTER EPSILON","e"},
    {"03B6", "GREEK SMALL LETTER ZETA", "z"},
    {"03B7", "GREEK SMALL LETTER ETA", "eta"},
    {"03B8", "GREEK SMALL LETTER THETA","t"},
    {"03B9", "GREEK SMALL LETTER IOTA", "i"},
    {"03BA", "GREEK SMALL LETTER KAPPA","k"},
    {"03BB", "GREEK SMALL LETTER LAMDA","l"},
    {"03BC", "GREEK SMALL LETTER MU","m"},
    {"03BD", "GREEK SMALL LETTER NU","n"},
    {"03BE", "GREEK SMALL LETTER XI","x"},
    {"03BF", "GREEK SMALL LETTER OMICRON","o"},
    {"03C0", "GREEK SMALL LETTER PI","p"},
    {"03C1", "GREEK SMALL LETTER RHO","r"},
    {"03C2", "GREEK SMALL LETTER FINAL SIGMA","fs"},
    {"03C3", "GREEK SMALL LETTER SIGMA","s"},
    {"03C4", "GREEK SMALL LETTER TAU","t"},
    {"03C5", "GREEK SMALL LETTER UPSILON","u"},
    {"03C6", "GREEK SMALL LETTER PHI","f"},
    {"03C7", "GREEK SMALL LETTER CHI","c"},
    {"03C8", "GREEK SMALL LETTER PSI","ps"},
    {"03C9", "GREEK SMALL LETTER OMEGA","w"},
    {"03DA", "GREEK LETTER STIGMA","ST"},
    {"03DC", "GREEK LETTER DIGAMMA","DI"},
    {"03DE", "GREEK LETTER KOPPA","KO"},
    {"03E0", "GREEK LETTER SAMPI","SA"},
    {"03F3", "GREEK LETTER YOT","Y"},
    {"2129", "TURNED GREEK SMALL LETTER IOTA", "io"},
     null,
    {"0020", "SPACE", " "},  
    {"0391", "GREEK CAPITAL LETTER ALPHA","A"},
    {"0392", "GREEK CAPITAL LETTER BETA","B"},
    {"0393", "GREEK CAPITAL LETTER GAMMA","G"},
    {"0394", "GREEK CAPITAL LETTER DELTA","D"},
    {"0395", "GREEK CAPITAL LETTER EPSILON","E"},
    {"0396", "GREEK CAPITAL LETTER ZETA","Z"},
    {"0397", "GREEK CAPITAL LETTER ETA","E"},
    {"0398", "GREEK CAPITAL LETTER THETA","T"},
    {"0399", "GREEK CAPITAL LETTER IOTA","I"},
    {"039A", "GREEK CAPITAL LETTER KAPPA","K"},
    {"039B", "GREEK CAPITAL LETTER LAMDA","L"},
    {"039C", "GREEK CAPITAL LETTER MU","M"},
    {"039D", "GREEK CAPITAL LETTER NU","N"},
    {"039E", "GREEK CAPITAL LETTER XI","X"},
    {"039F", "GREEK CAPITAL LETTER OMICRON","O"},
    {"03A0", "GREEK CAPITAL LETTER PI","P"},
    {"03A1", "GREEK CAPITAL LETTER RHO","R"},
    {"03A3", "GREEK CAPITAL LETTER SIGMA","S"},
    {"03A4", "GREEK CAPITAL LETTER TAU","T"},
    {"03A5", "GREEK CAPITAL LETTER UPSILON","U"},
    {"03A6", "GREEK CAPITAL LETTER PHI","F"},
    {"03A7", "GREEK CAPITAL LETTER CHI","C"},
    {"03A8", "GREEK CAPITAL LETTER PSI","PS"},
    {"03A9", "GREEK CAPITAL LETTER OMEGA","W"}
};
  
  final public static String[][] tibetanLetters = 
  {
    {"0F40", "TIBETAN LETTER KA","KA"},
    {"0F41", "TIBETAN LETTER KHA","KHA"},
    {"0F42", "TIBETAN LETTER GA","GA"},
    {"0F43", "TIBETAN LETTER GHA","GHA"},
    {"0F44", "TIBETAN LETTER NGA","NGA"},
    {"0F45", "TIBETAN LETTER CA","CA"},
    {"0F46", "TIBETAN LETTER CHA","CHA"},
    {"0F47", "TIBETAN LETTER JA","JA"},
    {"0F49", "TIBETAN LETTER NYA","NYA"},
    {"0F4A", "TIBETAN LETTER TTA","TTA"},
    {"0F4B", "TIBETAN LETTER TTHA","TTHA"},
    {"0F4C", "TIBETAN LETTER DDA","DDA"},
    {"0F4D", "TIBETAN LETTER DDHA","DDHA"},
    {"0F4E", "TIBETAN LETTER NNA","NNA"},
    {"0F4F", "TIBETAN LETTER TA","TA"},
    {"0F50", "TIBETAN LETTER THA","THA"},
    {"0F51", "TIBETAN LETTER DA","DA"},
    {"0F52", "TIBETAN LETTER DHA","DHA"},
    {"0F53", "TIBETAN LETTER NA","NA"},
    {"0F54", "TIBETAN LETTER PA","PA"},
    {"0F55", "TIBETAN LETTER PHA","PHA"},
    {"0F56", "TIBETAN LETTER BA","BA"},
    {"0F57", "TIBETAN LETTER BHA","BHA"},
    {"0F58", "TIBETAN LETTER MA","MA"},
    {"0F59", "TIBETAN LETTER TSA","TSA"},
    {"0F5A", "TIBETAN LETTER TSHA","TSHA"},
    {"0F5B", "TIBETAN LETTER DZA","DZA"},
    {"0F5C", "TIBETAN LETTER DZHA","DZHA"},
    {"0F5D", "TIBETAN LETTER WA","WA"},
    {"0F5E", "TIBETAN LETTER ZHA","ZHA"},
    {"0F5F", "TIBETAN LETTER ZA","ZA"},
    {"0F60", "TIBETAN LETTER -A","-A"},
    {"0F61", "TIBETAN LETTER YA","YA"},
    {"0F62", "TIBETAN LETTER RA","RA"},
    {"0F63", "TIBETAN LETTER LA","LA"},
    {"0F64", "TIBETAN LETTER SHA","SHA"},
    {"0F65", "TIBETAN LETTER SSA","SSA"},
    {"0F66", "TIBETAN LETTER SA","SA"},
    {"0F67", "TIBETAN LETTER HA", "HA"},
    {"0F68", "TIBETAN LETTER A", "A"},
    {"0F69", "TIBETAN LETTER KSSA", "KSSA"},
    null,
    null,
    null,    
    {"0F71", "TIBETAN VOWEL SIGN AA"},
    {"0F72", "TIBETAN VOWEL SIGN I"},
    {"0F73", "TIBETAN VOWEL SIGN II"},
    {"0F74", "TIBETAN VOWEL SIGN U"},
    {"0F75", "TIBETAN VOWEL SIGN UU"},
    {"0F76", "TIBETAN VOWEL SIGN VOCALIC R"},
    {"0F77", "TIBETAN VOWEL SIGN VOCALIC RR"},
    {"0F78", "TIBETAN VOWEL SIGN VOCALIC L"},
    {"0F79", "TIBETAN VOWEL SIGN VOCALIC LL"},
    {"0F7A", "TIBETAN VOWEL SIGN E"},
    {"0F7B", "TIBETAN VOWEL SIGN EE"},
    {"0F7C", "TIBETAN VOWEL SIGN O"},
    {"0F7D", "TIBETAN VOWEL SIGN OO"},
    {"0F7E", "TIBETAN SIGN RJES SU NGA RO"},
    {"0F7F", "TIBETAN SIGN RNAM BCAD"},
    {"0F80", "TIBETAN VOWEL SIGN REVERSED I"},
    {"0F81", "TIBETAN VOWEL SIGN REVERSED II"},
    {"0F82", "TIBETAN SIGN NYI ZLA NAA DA"},
    {"0F83", "TIBETAN SIGN SNA LDAN"},
    {"0F84", "TIBETAN MARK HALANTA"},
    {"0F85", "TIBETAN MARK PALUTA"},
    {"0F86", "TIBETAN SIGN LCI RTAGS"},
    {"0F87", "TIBETAN SIGN YANG RTAGS"},
    {"0F88", "TIBETAN SIGN LCE TSA CAN"},
    {"0F89", "TIBETAN SIGN MCHU CAN"},
    {"0F8A", "TIBETAN SIGN GRU CAN RGYINGS"},
    {"0F8B", "TIBETAN SIGN GRU MED RGYINGS"},
    {"0F90", "TIBETAN SUBJOINED LETTER KA"},
    {"0F00", "TIBETAN SYLLABLE OM"},
    {"0F01", "TIBETAN MARK GTER YIG MGO TRUNCATED A"},
    {"0F02", "TIBETAN MARK GTER YIG MGO -UM RNAM BCAD MA"},
    {"0F03", "TIBETAN MARK GTER YIG MGO -UM GTER TSHEG MA"},
    {"0F04", "TIBETAN MARK INITIAL YIG MGO MDUN MA"},
    {"0F05", "TIBETAN MARK CLOSING YIG MGO SGAB MA"},
    {"0F06", "TIBETAN MARK CARET YIG MGO PHUR SHAD MA"},
    {"0F07", "TIBETAN MARK YIG MGO TSHEG SHAD MA"},
    {"0F08", "TIBETAN MARK SBRUL SHAD"},
    {"0F09", "TIBETAN MARK BSKUR YIG MGO"},
    {"0F0A", "TIBETAN MARK BKA- SHOG YIG MGO"},
    {"0F0B", "TIBETAN MARK INTERSYLLABIC TSHEG"},
    {"0F0C", "TIBETAN MARK DELIMITER TSHEG BSTAR"},
    {"0F0D", "TIBETAN MARK SHAD"},
    {"0F0E", "TIBETAN MARK NYIS SHAD"},
    {"0F0F", "TIBETAN MARK TSHEG SHAD"},
    {"0F10", "TIBETAN MARK NYIS TSHEG SHAD"},
    {"0F11", "TIBETAN MARK RIN CHEN SPUNGS SHAD"},
    {"0F12", "TIBETAN MARK RGYA GRAM SHAD"},
    {"0F13", "TIBETAN MARK CARET -DZUD RTAGS ME LONG CAN"},
    {"0F14", "TIBETAN MARK GTER TSHEG"},
    {"0F15", "TIBETAN LOGOTYPE SIGN CHAD RTAGS"},
    {"0F16", "TIBETAN LOGOTYPE SIGN LHAG RTAGS"},
    {"0F17", "TIBETAN ASTROLOGICAL SIGN SGRA GCAN -CHAR RTAGS"},
    {"0F18", "TIBETAN ASTROLOGICAL SIGN -KHYUD PA"},
    {"0F19", "TIBETAN ASTROLOGICAL SIGN SDONG TSHUGS"},
    {"0F1A", "TIBETAN SIGN RDEL DKAR GCIG"},
    {"0F1B", "TIBETAN SIGN RDEL DKAR GNYIS"},
    {"0F1C", "TIBETAN SIGN RDEL DKAR GSUM"},
    {"0F1D", "TIBETAN SIGN RDEL NAG GCIG"},
    {"0F1E", "TIBETAN SIGN RDEL NAG GNYIS"},
    {"0F1F", "TIBETAN SIGN RDEL DKAR RDEL NAG"},
    null,
    null,
    null,
    null,
    null,
    null,
    {"0F20", "TIBETAN DIGIT ZERO","0"},
    {"0F21", "TIBETAN DIGIT ONE","1"},
    {"0F22", "TIBETAN DIGIT TWO","2"},
    {"0F23", "TIBETAN DIGIT THREE","3"},
    {"0F24", "TIBETAN DIGIT FOUR","4"},
    {"0F25", "TIBETAN DIGIT FIVE","5"},
    {"0F26", "TIBETAN DIGIT SIX","6"},
    {"0F27", "TIBETAN DIGIT SEVEN","7"},
    {"0F28", "TIBETAN DIGIT EIGHT","8"},
    {"0F29", "TIBETAN DIGIT NINE","9"},
    null,
    {"0F33", "TIBETAN DIGIT HALF ZERO"},
    {"0F2A", "TIBETAN DIGIT HALF ONE"},
    {"0F2B", "TIBETAN DIGIT HALF TWO"},
    {"0F2C", "TIBETAN DIGIT HALF THREE"},
    {"0F2D", "TIBETAN DIGIT HALF FOUR"},
    {"0F2E", "TIBETAN DIGIT HALF FIVE"},
    {"0F2F", "TIBETAN DIGIT HALF SIX"},
    {"0F30", "TIBETAN DIGIT HALF SEVEN"},
    {"0F31", "TIBETAN DIGIT HALF EIGHT"},
    {"0F32", "TIBETAN DIGIT HALF NINE"},
    null,
    {"0F34", "TIBETAN MARK BSDUS RTAGS"},
    {"0F35", "TIBETAN MARK NGAS BZUNG NYI ZLA"},
    {"0F36", "TIBETAN MARK CARET -DZUD RTAGS BZHI MIG CAN"},
    {"0F37", "TIBETAN MARK NGAS BZUNG SGOR RTAGS"},
    {"0F38", "TIBETAN MARK CHE MGO"},
    {"0F39", "TIBETAN MARK TSA -PHRU"},
    {"0F3A", "TIBETAN MARK GUG RTAGS GYON"},
    {"0F3B", "TIBETAN MARK GUG RTAGS GYAS"},
    {"0F3C", "TIBETAN MARK ANG KHANG GYON"},
    {"0F3D", "TIBETAN MARK ANG KHANG GYAS"},
    {"0F3E", "TIBETAN SIGN YAR TSHES"},
    {"0F3F", "TIBETAN SIGN MAR TSHES"},
    {"0020", "SPACE", " "}
    
};
 
  final public static String[][] thaiLetters = 
 {
    {"0E01", "THAI CHARACTER KO KAI"},
    {"0E02", "THAI CHARACTER KHO KHAI"},
    {"0E03", "THAI CHARACTER KHO KHUAT"},
    {"0E04", "THAI CHARACTER KHO KHWAI"},
    {"0E05", "THAI CHARACTER KHO KHON"},
    {"0E06", "THAI CHARACTER KHO RAKHANG"},
    {"0E07", "THAI CHARACTER NGO NGU"},
    {"0E08", "THAI CHARACTER CHO CHAN"},
    {"0E09", "THAI CHARACTER CHO CHING"},
    {"0E0A", "THAI CHARACTER CHO CHANG"},
    {"0E0B", "THAI CHARACTER SO SO"},
    {"0E0C", "THAI CHARACTER CHO CHOE"},
    {"0E0D", "THAI CHARACTER YO YING"},
    {"0E0E", "THAI CHARACTER DO CHADA"},
    {"0E0F", "THAI CHARACTER TO PATAK"},
    {"0E10", "THAI CHARACTER THO THAN"},
    {"0E11", "THAI CHARACTER THO NANGMONTHO"},
    {"0E12", "THAI CHARACTER THO PHUTHAO"},
    {"0E13", "THAI CHARACTER NO NEN"},
    {"0E14", "THAI CHARACTER DO DEK"},
    {"0E15", "THAI CHARACTER TO TAO"},
    {"0E16", "THAI CHARACTER THO THUNG"},
    {"0E17", "THAI CHARACTER THO THAHAN"},
    {"0E18", "THAI CHARACTER THO THONG"},
    {"0E19", "THAI CHARACTER NO NU"},
    {"0E1A", "THAI CHARACTER BO BAIMAI"},
    {"0E1B", "THAI CHARACTER PO PLA"},
    {"0E1C", "THAI CHARACTER PHO PHUNG"},
    {"0E1D", "THAI CHARACTER FO FA"},
    {"0E1E", "THAI CHARACTER PHO PHAN"},
    {"0E1F", "THAI CHARACTER FO FAN"},
    {"0E20", "THAI CHARACTER PHO SAMPHAO"},
    {"0E21", "THAI CHARACTER MO MA"},
    {"0E22", "THAI CHARACTER YO YAK"},
    {"0E23", "THAI CHARACTER RO RUA"},
    {"0E24", "THAI CHARACTER RU"},
    {"0E25", "THAI CHARACTER LO LING"},
    {"0E26", "THAI CHARACTER LU"},
    {"0E27", "THAI CHARACTER WO WAEN"},
    {"0E28", "THAI CHARACTER SO SALA"},
    {"0E29", "THAI CHARACTER SO RUSI"},
    {"0E2A", "THAI CHARACTER SO SUA"},
    {"0E2B", "THAI CHARACTER HO HIP"},
    {"0E2C", "THAI CHARACTER LO CHULA"},
    {"0E2D", "THAI CHARACTER O ANG"},
    {"0E2E", "THAI CHARACTER HO NOKHUK"},
    {"0E2F", "THAI CHARACTER PAIYANNOI"},
    {"0E30", "THAI CHARACTER SARA A"},
    {"0E31", "THAI CHARACTER MAI HAN-AKAT"},
    {"0E32", "THAI CHARACTER SARA AA"},
    {"0E33", "THAI CHARACTER SARA AM"},
    {"0E34", "THAI CHARACTER SARA I"},
    {"0E35", "THAI CHARACTER SARA II"},
    {"0E36", "THAI CHARACTER SARA UE"},
    {"0E37", "THAI CHARACTER SARA UEE"},
    {"0E38", "THAI CHARACTER SARA U"},
    {"0E39", "THAI CHARACTER SARA UU"},
    {"0E3A", "THAI CHARACTER PHINTHU"},
    {"0E3F", "THAI CURRENCY SYMBOL BAHT"},
    {"0E40", "THAI CHARACTER SARA E"},
    {"0E41", "THAI CHARACTER SARA AE"},
    {"0E42", "THAI CHARACTER SARA O"},
    {"0E43", "THAI CHARACTER SARA AI MAIMUAN"},
    {"0E44", "THAI CHARACTER SARA AI MAIMALAI"},
    {"0E45", "THAI CHARACTER LAKKHANGYAO"},
    {"0E46", "THAI CHARACTER MAIYAMOK"},
    {"0E47", "THAI CHARACTER MAITAIKHU"},
    {"0E48", "THAI CHARACTER MAI EK"},
    {"0E49", "THAI CHARACTER MAI THO"},
    {"0E4A", "THAI CHARACTER MAI TRI"},
    {"0E4B", "THAI CHARACTER MAI CHATTAWA"},
    {"0E4C", "THAI CHARACTER THANTHAKHAT"},
    {"0E4D", "THAI CHARACTER NIKHAHIT"},
    {"0E4E", "THAI CHARACTER YAMAKKAN"},
    {"0E4F", "THAI CHARACTER FONGMAN"},
    null,
    {"0E50", "THAI DIGIT ZERO"},
    {"0E51", "THAI DIGIT ONE"},
    {"0E52", "THAI DIGIT TWO"},
    {"0E53", "THAI DIGIT THREE"},
    {"0E54", "THAI DIGIT FOUR"},
    {"0E55", "THAI DIGIT FIVE"},
    {"0E56", "THAI DIGIT SIX"},
    {"0E57", "THAI DIGIT SEVEN"},
    {"0E58", "THAI DIGIT EIGHT"},
    {"0E59", "THAI DIGIT NINE"},
    null,
    {"0E5A", "THAI CHARACTER ANGKHANKHU"},
    {"0E5B", "THAI CHARACTER KHOMUT"},
    {"0020", "SPACE", " "}
};

  final public static String[][] brailleLetters = 
 {
    {"2800", "BRAILLE PATTERN BLANK"},
    {"2801", "BRAILLE PATTERN DOTS-1"},
    {"2802", "BRAILLE PATTERN DOTS-2"},
    {"2803", "BRAILLE PATTERN DOTS-12"},
    {"2804", "BRAILLE PATTERN DOTS-3"},
    {"2805", "BRAILLE PATTERN DOTS-13"},
    {"2806", "BRAILLE PATTERN DOTS-23"},
    {"2807", "BRAILLE PATTERN DOTS-123"},
    {"2808", "BRAILLE PATTERN DOTS-4"},
    {"2809", "BRAILLE PATTERN DOTS-14"},
    {"280A", "BRAILLE PATTERN DOTS-24"},
    {"280B", "BRAILLE PATTERN DOTS-124"},
    {"280C", "BRAILLE PATTERN DOTS-34"},
    {"280D", "BRAILLE PATTERN DOTS-134"},
    {"280E", "BRAILLE PATTERN DOTS-234"},
    {"280F", "BRAILLE PATTERN DOTS-1234"},
    {"2810", "BRAILLE PATTERN DOTS-5"},
    {"2811", "BRAILLE PATTERN DOTS-15"},
    {"2812", "BRAILLE PATTERN DOTS-25"},
    {"2813", "BRAILLE PATTERN DOTS-125"},
    {"2814", "BRAILLE PATTERN DOTS-35"},
    {"2815", "BRAILLE PATTERN DOTS-135"},
    {"2816", "BRAILLE PATTERN DOTS-235"},
    {"2817", "BRAILLE PATTERN DOTS-1235"},
    {"2818", "BRAILLE PATTERN DOTS-45"},
    {"2819", "BRAILLE PATTERN DOTS-145"},
    {"281A", "BRAILLE PATTERN DOTS-245"},
    {"281B", "BRAILLE PATTERN DOTS-1245"},
    {"281C", "BRAILLE PATTERN DOTS-345"},
    {"281D", "BRAILLE PATTERN DOTS-1345"},
    {"281E", "BRAILLE PATTERN DOTS-2345"},
    {"281F", "BRAILLE PATTERN DOTS-12345"},
    {"2820", "BRAILLE PATTERN DOTS-6"},
    {"2821", "BRAILLE PATTERN DOTS-16"},
    {"2822", "BRAILLE PATTERN DOTS-26"},
    {"2823", "BRAILLE PATTERN DOTS-126"},
    {"2824", "BRAILLE PATTERN DOTS-36"},
    {"2825", "BRAILLE PATTERN DOTS-136"},
    {"2826", "BRAILLE PATTERN DOTS-236"},
    {"2827", "BRAILLE PATTERN DOTS-1236"},
    {"2828", "BRAILLE PATTERN DOTS-46"},
    {"2829", "BRAILLE PATTERN DOTS-146"},
    {"282A", "BRAILLE PATTERN DOTS-246"},
    {"282B", "BRAILLE PATTERN DOTS-1246"},
    {"282C", "BRAILLE PATTERN DOTS-346"},
    {"282D", "BRAILLE PATTERN DOTS-1346"},
    {"282E", "BRAILLE PATTERN DOTS-2346"},
    {"282F", "BRAILLE PATTERN DOTS-12346"},
    {"2830", "BRAILLE PATTERN DOTS-56"},
    {"2831", "BRAILLE PATTERN DOTS-156"},
    {"2832", "BRAILLE PATTERN DOTS-256"},
    {"2833", "BRAILLE PATTERN DOTS-1256"},
    {"2834", "BRAILLE PATTERN DOTS-356"},
    {"2835", "BRAILLE PATTERN DOTS-1356"},
    {"2836", "BRAILLE PATTERN DOTS-2356"},
    {"2837", "BRAILLE PATTERN DOTS-12356"},
    {"2838", "BRAILLE PATTERN DOTS-456"},
    {"2839", "BRAILLE PATTERN DOTS-1456"},
    {"283A", "BRAILLE PATTERN DOTS-2456"},
    {"283B", "BRAILLE PATTERN DOTS-12456"},
    {"283C", "BRAILLE PATTERN DOTS-3456"},
    {"283D", "BRAILLE PATTERN DOTS-13456"},
    {"283E", "BRAILLE PATTERN DOTS-23456"},
    {"283F", "BRAILLE PATTERN DOTS-123456"},
    {"2840", "BRAILLE PATTERN DOTS-7"},
    {"2841", "BRAILLE PATTERN DOTS-17"},
    {"2842", "BRAILLE PATTERN DOTS-27"},
    {"2843", "BRAILLE PATTERN DOTS-127"},
    {"2844", "BRAILLE PATTERN DOTS-37"},
    {"2845", "BRAILLE PATTERN DOTS-137"},
    {"2846", "BRAILLE PATTERN DOTS-237"},
    {"2847", "BRAILLE PATTERN DOTS-1237"},
    {"2848", "BRAILLE PATTERN DOTS-47"},
    {"2849", "BRAILLE PATTERN DOTS-147"},
    {"284A", "BRAILLE PATTERN DOTS-247"},
    {"284B", "BRAILLE PATTERN DOTS-1247"},
    {"284C", "BRAILLE PATTERN DOTS-347"},
    {"284D", "BRAILLE PATTERN DOTS-1347"},
    {"284E", "BRAILLE PATTERN DOTS-2347"},
    {"284F", "BRAILLE PATTERN DOTS-12347"},
    {"2850", "BRAILLE PATTERN DOTS-57"},
    {"2851", "BRAILLE PATTERN DOTS-157"},
    {"2852", "BRAILLE PATTERN DOTS-257"},
    {"2853", "BRAILLE PATTERN DOTS-1257"},
    {"2854", "BRAILLE PATTERN DOTS-357"},
    {"2855", "BRAILLE PATTERN DOTS-1357"},
    {"2856", "BRAILLE PATTERN DOTS-2357"},
    {"2857", "BRAILLE PATTERN DOTS-12357"},
    {"2858", "BRAILLE PATTERN DOTS-457"},
    {"2859", "BRAILLE PATTERN DOTS-1457"},
    {"285A", "BRAILLE PATTERN DOTS-2457"},
    {"285B", "BRAILLE PATTERN DOTS-12457"},
    {"285C", "BRAILLE PATTERN DOTS-3457"},
    {"285D", "BRAILLE PATTERN DOTS-13457"},
    {"285E", "BRAILLE PATTERN DOTS-23457"},
    {"285F", "BRAILLE PATTERN DOTS-123457"},
    {"2860", "BRAILLE PATTERN DOTS-67"},
    {"2861", "BRAILLE PATTERN DOTS-167"},
    {"2862", "BRAILLE PATTERN DOTS-267"},
    {"2863", "BRAILLE PATTERN DOTS-1267"},
    {"2864", "BRAILLE PATTERN DOTS-367"},
    {"2865", "BRAILLE PATTERN DOTS-1367"},
    {"2866", "BRAILLE PATTERN DOTS-2367"},
    {"2867", "BRAILLE PATTERN DOTS-12367"},
    {"2868", "BRAILLE PATTERN DOTS-467"},
    {"2869", "BRAILLE PATTERN DOTS-1467"},
    {"286A", "BRAILLE PATTERN DOTS-2467"},
    {"286B", "BRAILLE PATTERN DOTS-12467"},
    {"286C", "BRAILLE PATTERN DOTS-3467"},
    {"286D", "BRAILLE PATTERN DOTS-13467"},
    {"286E", "BRAILLE PATTERN DOTS-23467"},
    {"286F", "BRAILLE PATTERN DOTS-123467"},
    {"2870", "BRAILLE PATTERN DOTS-567"},
    {"2871", "BRAILLE PATTERN DOTS-1567"},
    {"2872", "BRAILLE PATTERN DOTS-2567"},
    {"2873", "BRAILLE PATTERN DOTS-12567"},
    {"2874", "BRAILLE PATTERN DOTS-3567"},
    {"2875", "BRAILLE PATTERN DOTS-13567"},
    {"2876", "BRAILLE PATTERN DOTS-23567"},
    {"2877", "BRAILLE PATTERN DOTS-123567"},
    {"2878", "BRAILLE PATTERN DOTS-4567"},
    {"2879", "BRAILLE PATTERN DOTS-14567"},
    {"287A", "BRAILLE PATTERN DOTS-24567"},
    {"287B", "BRAILLE PATTERN DOTS-124567"},
    {"287C", "BRAILLE PATTERN DOTS-34567"},
    {"287D", "BRAILLE PATTERN DOTS-134567"},
    {"287E", "BRAILLE PATTERN DOTS-234567"},
    {"287F", "BRAILLE PATTERN DOTS-1234567"},
    {"2880", "BRAILLE PATTERN DOTS-8"},
    {"2881", "BRAILLE PATTERN DOTS-18"},
    {"2882", "BRAILLE PATTERN DOTS-28"},
    {"2883", "BRAILLE PATTERN DOTS-128"},
    {"2884", "BRAILLE PATTERN DOTS-38"},
    {"2885", "BRAILLE PATTERN DOTS-138"},
    {"2886", "BRAILLE PATTERN DOTS-238"},
    {"2887", "BRAILLE PATTERN DOTS-1238"},
    {"2888", "BRAILLE PATTERN DOTS-48"},
    {"2889", "BRAILLE PATTERN DOTS-148"},
    {"288A", "BRAILLE PATTERN DOTS-248"},
    {"288B", "BRAILLE PATTERN DOTS-1248"},
    {"288C", "BRAILLE PATTERN DOTS-348"},
    {"288D", "BRAILLE PATTERN DOTS-1348"},
    {"288E", "BRAILLE PATTERN DOTS-2348"},
    {"288F", "BRAILLE PATTERN DOTS-12348"},
    {"2890", "BRAILLE PATTERN DOTS-58"},
    {"2891", "BRAILLE PATTERN DOTS-158"},
    {"2892", "BRAILLE PATTERN DOTS-258"},
    {"2893", "BRAILLE PATTERN DOTS-1258"},
    {"2894", "BRAILLE PATTERN DOTS-358"},
    {"2895", "BRAILLE PATTERN DOTS-1358"},
    {"2896", "BRAILLE PATTERN DOTS-2358"},
    {"2897", "BRAILLE PATTERN DOTS-12358"},
    {"2898", "BRAILLE PATTERN DOTS-458"},
    {"2899", "BRAILLE PATTERN DOTS-1458"},
    {"289A", "BRAILLE PATTERN DOTS-2458"},
    {"289B", "BRAILLE PATTERN DOTS-12458"},
    {"289C", "BRAILLE PATTERN DOTS-3458"},
    {"289D", "BRAILLE PATTERN DOTS-13458"},
    {"289E", "BRAILLE PATTERN DOTS-23458"},
    {"289F", "BRAILLE PATTERN DOTS-123458"},
    {"28A0", "BRAILLE PATTERN DOTS-68"},
    {"28A1", "BRAILLE PATTERN DOTS-168"},
    {"28A2", "BRAILLE PATTERN DOTS-268"},
    {"28A3", "BRAILLE PATTERN DOTS-1268"},
    {"28A4", "BRAILLE PATTERN DOTS-368"},
    {"28A5", "BRAILLE PATTERN DOTS-1368"},
    {"28A6", "BRAILLE PATTERN DOTS-2368"},
    {"28A7", "BRAILLE PATTERN DOTS-12368"},
    {"28A8", "BRAILLE PATTERN DOTS-468"},
    {"28A9", "BRAILLE PATTERN DOTS-1468"},
    {"28AA", "BRAILLE PATTERN DOTS-2468"},
    {"28AB", "BRAILLE PATTERN DOTS-12468"},
    {"28AC", "BRAILLE PATTERN DOTS-3468"},
    {"28AD", "BRAILLE PATTERN DOTS-13468"},
    {"28AE", "BRAILLE PATTERN DOTS-23468"},
    {"28AF", "BRAILLE PATTERN DOTS-123468"},
    {"28B0", "BRAILLE PATTERN DOTS-568"},
    {"28B1", "BRAILLE PATTERN DOTS-1568"},
    {"28B2", "BRAILLE PATTERN DOTS-2568"},
    {"28B3", "BRAILLE PATTERN DOTS-12568"},
    {"28B4", "BRAILLE PATTERN DOTS-3568"},
    {"28B5", "BRAILLE PATTERN DOTS-13568"},
    {"28B6", "BRAILLE PATTERN DOTS-23568"},
    {"28B7", "BRAILLE PATTERN DOTS-123568"},
    {"28B8", "BRAILLE PATTERN DOTS-4568"},
    {"28B9", "BRAILLE PATTERN DOTS-14568"},
    {"28BA", "BRAILLE PATTERN DOTS-24568"},
    {"28BB", "BRAILLE PATTERN DOTS-124568"},
    {"28BC", "BRAILLE PATTERN DOTS-34568"},
    {"28BD", "BRAILLE PATTERN DOTS-134568"},
    {"28BE", "BRAILLE PATTERN DOTS-234568"},
    {"28BF", "BRAILLE PATTERN DOTS-1234568"},
    {"28C0", "BRAILLE PATTERN DOTS-78"},
    {"28C1", "BRAILLE PATTERN DOTS-178"},
    {"28C2", "BRAILLE PATTERN DOTS-278"},
    {"28C3", "BRAILLE PATTERN DOTS-1278"},
    {"28C4", "BRAILLE PATTERN DOTS-378"},
    {"28C5", "BRAILLE PATTERN DOTS-1378"},
    {"28C6", "BRAILLE PATTERN DOTS-2378"},
    {"28C7", "BRAILLE PATTERN DOTS-12378"},
    {"28C8", "BRAILLE PATTERN DOTS-478"},
    {"28C9", "BRAILLE PATTERN DOTS-1478"},
    {"28CA", "BRAILLE PATTERN DOTS-2478"},
    {"28CB", "BRAILLE PATTERN DOTS-12478"},
    {"28CC", "BRAILLE PATTERN DOTS-3478"},
    {"28CD", "BRAILLE PATTERN DOTS-13478"},
    {"28CE", "BRAILLE PATTERN DOTS-23478"},
    {"28CF", "BRAILLE PATTERN DOTS-123478"},
    {"28D0", "BRAILLE PATTERN DOTS-578"},
    {"28D1", "BRAILLE PATTERN DOTS-1578"},
    {"28D2", "BRAILLE PATTERN DOTS-2578"},
    {"28D3", "BRAILLE PATTERN DOTS-12578"},
    {"28D4", "BRAILLE PATTERN DOTS-3578"},
    {"28D5", "BRAILLE PATTERN DOTS-13578"},
    {"28D6", "BRAILLE PATTERN DOTS-23578"},
    {"28D7", "BRAILLE PATTERN DOTS-123578"},
    {"28D8", "BRAILLE PATTERN DOTS-4578"},
    {"28D9", "BRAILLE PATTERN DOTS-14578"},
    {"28DA", "BRAILLE PATTERN DOTS-24578"},
    {"28DB", "BRAILLE PATTERN DOTS-124578"},
    {"28DC", "BRAILLE PATTERN DOTS-34578"},
    {"28DD", "BRAILLE PATTERN DOTS-134578"},
    {"28DE", "BRAILLE PATTERN DOTS-234578"},
    {"28DF", "BRAILLE PATTERN DOTS-1234578"},
    {"28E0", "BRAILLE PATTERN DOTS-678"},
    {"28E1", "BRAILLE PATTERN DOTS-1678"},
    {"28E2", "BRAILLE PATTERN DOTS-2678"},
    {"28E3", "BRAILLE PATTERN DOTS-12678"},
    {"28E4", "BRAILLE PATTERN DOTS-3678"},
    {"28E5", "BRAILLE PATTERN DOTS-13678"},
    {"28E6", "BRAILLE PATTERN DOTS-23678"},
    {"28E7", "BRAILLE PATTERN DOTS-123678"},
    {"28E8", "BRAILLE PATTERN DOTS-4678"},
    {"28E9", "BRAILLE PATTERN DOTS-14678"},
    {"28EA", "BRAILLE PATTERN DOTS-24678"},
    {"28EB", "BRAILLE PATTERN DOTS-124678"},
    {"28EC", "BRAILLE PATTERN DOTS-34678"},
    {"28ED", "BRAILLE PATTERN DOTS-134678"},
    {"28EE", "BRAILLE PATTERN DOTS-234678"},
    {"28EF", "BRAILLE PATTERN DOTS-1234678"},
    {"28F0", "BRAILLE PATTERN DOTS-5678"},
    {"28F1", "BRAILLE PATTERN DOTS-15678"},
    {"28F2", "BRAILLE PATTERN DOTS-25678"},
    {"28F3", "BRAILLE PATTERN DOTS-125678"},
    {"28F4", "BRAILLE PATTERN DOTS-35678"},
    {"28F5", "BRAILLE PATTERN DOTS-135678"},
    {"28F6", "BRAILLE PATTERN DOTS-235678"},
    {"28F7", "BRAILLE PATTERN DOTS-1235678"},
    {"28F8", "BRAILLE PATTERN DOTS-45678"},
    {"28F9", "BRAILLE PATTERN DOTS-145678"},
    {"28FA", "BRAILLE PATTERN DOTS-245678"},
    {"28FB", "BRAILLE PATTERN DOTS-1245678"},
    {"28FC", "BRAILLE PATTERN DOTS-345678"},
    {"28FD", "BRAILLE PATTERN DOTS-1345678"},
    {"28FE", "BRAILLE PATTERN DOTS-2345678"},
    {"28FF", "BRAILLE PATTERN DOTS-12345678"},
    null,
    {"0020", "SPACE", " "}
  };


}
