package net.sf.pkt.convert; 
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
import java.awt.event.*; 
import java.applet.*; 

public class PKTConv extends Applet { 
  boolean isStandalone = false; 
  List lstFrom = new List(); 
  List lstTo = new List(); 
  Button butConvert = new Button(); 
  TextField txtResult = new TextField(); 
  Choice chCategory = new Choice(); 
  TextField txtNumber = new TextField(); 
  Label lblTo = new Label(); 
  Label lblConvertNum = new Label(); 
  Label lblCategory = new Label(); 
  Panel bevelPanel1 = new Panel(new GridLayout(3,3)); 
  Label lblConverter = new Label(); 

  /* The Category Selected */ 
  String strCategory = ""; 
  /* The number Entered */ 
   double iNum = 0.0; 

  /* Matrix to store the Date */ 
  double arryLength[][]; 
  double arryArea[][]; 
  double arryVolume[][]; 
  double arryMass[][]; 

  Panel bevelPanel2 = new Panel(new GridLayout(3,3)); 

  //Construct the applet 
  public PKTConv() { 
  /* Create the array objects and populate it*/ 
  arryLength = new double[10][10]; 
  populateArryLength(); 

  arryArea   = new double[10][10]; 
  populateArryArea(); 

  arryVolume = new double[10][10]; 
  populateArryVolume(); 

  arryMass = new double[10][10]; 
  populateArryMass(); 

  }//constructor() 

  //Initialize the applet 
  public void init() { 
    try { jbInit(); } catch (Exception e) { e.printStackTrace(); } 
  }//init() 

  //Component initialization 
  private void jbInit() throws Exception{ 
    this.setBackground(new Color(192, 255, 192)); 
//    this.setBackground(Color.black); 
    lstFrom.setBackground(Color.white); 
    lblTo.setForeground(Color.blue); 
    lblTo.setFont(new Font("Dialog", 1, 14)); 
    lblTo.setText("TO"); 
    butConvert.setLabel("Convert Now"); 
    butConvert.setFont(new Font("Dialog", 1, 12)); 
    txtResult.setForeground(Color.green); 
    txtResult.setBackground(Color.darkGray); 
    txtResult.setEditable(false); 
    butConvert.addActionListener(new PKTConv_butConvert_actionAdapter(this)); 
    txtNumber.setText("1.0"); 
    chCategory.addItemListener(new PKTConv_chCategory_itemAdapter(this)); 
    lblConvertNum.setFont(new Font("Dialog", 1, 12)); 
    lblConvertNum.setText("Convert Number:"); 
    lblCategory.setFont(new Font("Dialog", 1, 12)); 
    lblCategory.setText("Category:"); 
    lblConverter.setBackground(Color.lightGray); 
    lblConverter.setForeground(Color.red); 
    lblConverter.setFont(new Font("Dialog", 1, 13)); 
    lblConverter.setText("METRIC CONVERTER"); 
    this.add(bevelPanel1); 
//    bevelPanel1.add(lblTo); 
    bevelPanel1.add(txtNumber);
    bevelPanel1.add(lstFrom);
    bevelPanel1.add(lstTo);
    bevelPanel1.add(chCategory);
    bevelPanel1.add(txtResult);
    bevelPanel1.add(butConvert);
//    bevelPanel1.add(lblConvertNum);
//    bevelPanel1.add(lblCategory);
    bevelPanel1.add(bevelPanel2);
//    bevelPanel2.add(lblConverter);

  /*Initially populete the components with default values  */ 
    InitializeComponents(); 
    setVisible(true);
  }//jbInit() 

  //Get Applet information 
  public String getAppletInfo() { 
    return "Applet Information"; 
  }//getApletInfo() 

  //Get parameter info 
  public String[][] getParameterInfo() { 
    return null; 
  }//getParameterInfo() 

/** 
*   Name : InitializeComponents() 
*   Description: This is a function which populates the 
*   choice list and the list box items when the applet is loaded 
*   @param : void 
*   @return : void 
*/ 
    public void InitializeComponents(){ 
     chCategory.addItem("Length"); 
     chCategory.addItem("Temperature"); 
     chCategory.addItem("Area"); 
     chCategory.addItem("Volume"); 
     chCategory.addItem("Mass"); 
     }//InitializeComponents() 

/** 
 *    Name : populateArryLength() 
 *   Description : This is a function which populates the 
 *   array by the initial values 
 *   The sequence of the elements is : 
 *   CM, M ,in ,ft, yd , Mile 
*/ 
    public void populateArryLength(){ 
   /* First ROW */ 
    arryLength[0][0] = 1.0; 
    arryLength[0][1] = 0.01; 
    arryLength[0][2] = 0.3937008; 
    arryLength[0][3] = 0.03280840; 
    arryLength[0][4] = 0.01093613; 
    arryLength[0][5] = 0.00000621372; 
   /* Second ROW */ 
    arryLength[1][0] = 100.0; 
    arryLength[1][1] = 1.0; 
    arryLength[1][2] = 39.37008; 
    arryLength[1][3] = 3.280840; 
    arryLength[1][4] = 1.093613; 
    arryLength[1][5] = 0.0006213712; 
    /* THIRD ROW */ 
    arryLength[2][0] = 2.54; 
    arryLength[2][1] = 0.0254; 
    arryLength[2][2] = 1.0; 
    arryLength[2][3] = 0.08333333; 
    arryLength[2][4] = 0.02777777; 
    arryLength[2][5] = 0.00001578283; 
    /* FOURTH ROW */ 
    arryLength[3][0] = 30.48; 
    arryLength[3][1] = 0.3048; 
    arryLength[3][2] = 12.0; 
    arryLength[3][3] = 1.0; 
    arryLength[3][4] = 0.333333333; 
    arryLength[3][5] = 0.0001893939; 
    /* FIFTH ROW */ 
    arryLength[4][0] = 91.44; 
    arryLength[4][1] = 0.9144; 
    arryLength[4][2] = 36.0; 
    arryLength[4][3] = 3.0; 
    arryLength[4][4] = 1.0; 
    arryLength[4][5] = 0.0005681818; 
    /* SIXTH ROW */ 
    arryLength[5][0] = 160934.4; 
    arryLength[5][1] = 1609.344; 
    arryLength[5][2] = 63360; 
    arryLength[5][3] = 5280.0; 
    arryLength[5][4] = 1760.0; 
    arryLength[5][5] = 1.0; 
    }//populateArryLength() 
  

/** 
 *    Name : populateArryArea() 
 *   Description : This is a function which populates the 
 *   array by the initial values 
 *   The sequence of the elements is : 
 *   CM2, M2 ,in2 ,ft2, yd2 , Mile2 
*/ 
    public void populateArryArea(){ 
   /* First ROW */ 
    arryArea[0][0] = 1.0; 
    arryArea[0][1] = 0.0004; 
    arryArea[0][2] = 0.1550003; 
    arryArea[0][3] = 0.001076391; 
    arryArea[0][4] = 0.0001195990; 
    arryArea[0][5] = 0.00000000003861022; 
   /* Second ROW */ 
    arryArea[1][0] = 10000.0; 
    arryArea[1][1] = 1.0; 
    arryArea[1][2] = 1550.003; 
    arryArea[1][3] = 10.76391; 
    arryArea[1][4] = 1.195990; 
    arryArea[1][5] = 0.0000003861022; 
    /* THIRD ROW */ 
    arryArea[2][0] = 6.4516; 
    arryArea[2][1] = 0.00064516; 
    arryArea[2][2] = 1.0; 
    arryArea[2][3] = 0.006944444440; 
    arryArea[2][4] = 0.00077160490; 
    arryArea[2][5] = 0.00000000024909770; 
    /* FOURTH ROW */ 
    arryArea[3][0] = 929.03040; 
    arryArea[3][1] = 0.092903040; 
    arryArea[3][2] = 144.0; 
    arryArea[3][3] = 1.0; 
    arryArea[3][4] = 0.1111111110; 
    arryArea[3][5] = 0.000000035870070; 
    /* FIFTH ROW */ 
    arryArea[4][0] = 8361.2730; 
    arryArea[4][1] = 0.83612730; 
    arryArea[4][2] = 1296.0; 
    arryArea[4][3] = 9.0; 
    arryArea[4][4] = 1.0; 
    arryArea[4][5] = 0.00000032283060; 
    /* SIXTH ROW */ 
    arryArea[5][0] = 25899880000.0; 
    arryArea[5][1] = 2589988.0; 
    arryArea[5][2] = 4014490000.0; 
    arryArea[5][3] = 27878400.0; 
    arryArea[5][4] = 3097600.0; 
    arryArea[5][5] = 1.0; 
    }//populateArryArea() 
  

/** 
 *   Name : populateArryVolume() 
 *   Description : This is a function which populates the 
 *   array by the initial values 
 *   The sequence of the elements is : 
 *   M3, CM3 , liter , in3 , ft3 , qt ,gal 
*/ 
    public void populateArryVolume(){ 
   /* First ROW */ 
    arryVolume[0][0] = 1.0; 
    arryVolume[0][1] = 1000000.0; 
    arryVolume[0][2] = 1000.0; 
    arryVolume[0][3] = 61023.74; 
    arryVolume[0][4] = 35.31467; 
    arryVolume[0][5] = 1056.688; 
    arryVolume[0][6] = 264.1721; 
   /* Second ROW */ 
    arryVolume[1][0] = 0.000001; 
    arryVolume[1][1] = 1.0; 
    arryVolume[1][2] = 0.001; 
    arryVolume[1][3] = 0.06102374; 
    arryVolume[1][4] = 0.0003531467; 
    arryVolume[1][5] = 0.001056688; 
    arryVolume[1][6] = 0.000264172; 
    /* THIRD ROW */ 
    arryVolume[2][0] = 0.001; 
    arryVolume[2][1] = 1000.0; 
    arryVolume[2][2] = 1.0; 
    arryVolume[2][3] = 61.02374; 
    arryVolume[2][4] = 0.03531467; 
    arryVolume[2][5] = 1.056688; 
    arryVolume[2][6] = 0.2641721; 

    /* FOURTH ROW */ 
    arryVolume[3][0] = 0.00001638706; 
    arryVolume[3][1] = 16.38706; 
    arryVolume[3][2] = 0.01638706; 
    arryVolume[3][3] = 1.0; 
    arryVolume[3][4] = 0.0005787037; 
    arryVolume[3][5] = 0.01731602; 
    arryVolume[3][6] = 0.004329004; 

    /* FIFTH ROW */ 
    arryVolume[4][0] = 0.02831685; 
    arryVolume[4][1] = 28316.85; 
    arryVolume[4][2] = 28.31685; 
    arryVolume[4][3] = 1728.0; 
    arryVolume[4][4] = 1.0; 
    arryVolume[4][5] = 2.992208; 
    arryVolume[4][6] = 7.480520; 

    /* SIXTH ROW */ 
    arryVolume[5][0] = 0.000946353; 
    arryVolume[5][1] = 946.353; 
    arryVolume[5][2] = 0.946353; 
    arryVolume[5][3] = 57.75; 
    arryVolume[5][4] = 0.0342014; 
    arryVolume[5][5] = 1.0; 
    arryVolume[5][6] = 0.25; 

    /* SEVENTH ROW */ 
    arryVolume[6][0] = 0.003785412; 
    arryVolume[6][1] = 3785.412; 
    arryVolume[6][2] = 3.785412; 
    arryVolume[6][3] = 231.0; 
    arryVolume[6][4] = 0.1336806; 
    arryVolume[6][5] = 4.0; 
    arryVolume[6][6] = 1.0; 
    }//populateArryVolume() 
  

/** 
 *    Name : populateArryMass() 
 *   Description : This is a function which populates the 
 *   array by the initial values 
 *   The sequence of the elements is : 
 *   g,kg,oz,lb,mtric ton,ton 
*/ 
    public void populateArryMass(){ 
   /* First ROW */ 
    arryMass[0][0] = 1.0; 
    arryMass[0][1] = 0.001; 
    arryMass[0][2] = 0.03527396; 
    arryMass[0][3] = 0.002204623; 
    arryMass[0][4] = 0.000001; 
    arryMass[0][5] = 0.000001102311; 
   /* Second ROW */ 
    arryMass[1][0] = 1000.0; 
    arryMass[1][1] = 1.0; 
    arryMass[1][2] = 35.27396; 
    arryMass[1][3] = 2.204623; 
    arryMass[1][4] = 0.001; 
    arryMass[1][5] = 0.001102311; 
    /* THIRD ROW */ 
    arryMass[2][0] = 28.34925; 
    arryMass[2][1] = 0.02834952; 
    arryMass[2][2] = 1.0; 
    arryMass[2][3] = 0.0625; 
    arryMass[2][4] = 0.0002834952; 
    arryMass[2][5] = 0.00003125; 
    /* FOURTH ROW */ 
    arryMass[3][0] = 453.5924; 
    arryMass[3][1] = 0.4535924; 
    arryMass[3][2] = 16.0; 
    arryMass[3][3] = 1.0; 
    arryMass[3][4] = 0.0004535924; 
    arryMass[3][5] = 0.0005; 
    /* FIFTH ROW */ 
    arryMass[4][0] = 1000000; 
    arryMass[4][1] = 1000.0; 
    arryMass[4][2] = 35273.96; 
    arryMass[4][3] = 2204.623; 
    arryMass[4][4] = 1.0; 
    arryMass[4][5] = 1.102311; 
    /* SIXTH ROW */ 
    arryMass[5][0] = 907184.7; 
    arryMass[5][1] = 907.1847; 
    arryMass[5][2] = 32000.0; 
    arryMass[5][3] = 2000.0; 
    arryMass[5][4] = 0.9071847; 
    arryMass[5][5] = 1.0; 
    }//populateArryMass() 
  

  void chCategory_itemStateChanged(ItemEvent e) { 
     strCategory = chCategory.getSelectedItem(); 
     LoadListBoxes(strCategory); 

  }//itemStateChanged 

  /** 
  *  Name : LoadListBoxes() 
  * Description : Loads the list boxes with appropriate data 
  * depending upon the categorey selected 
  *  @param  : String : the category which is selected 
  *  @return : void 
  */ 
   public void LoadListBoxes(String strCategory){ 
 /* First Clear all the elements in list */ 
   lstFrom.removeAll(); 
   lstTo.removeAll(); 

/* LENGTH */ 
   if(strCategory.equalsIgnoreCase("Length")){ 
    lstFrom.addItem("Centimeter(cm)"); 
    lstFrom.addItem("Meter(m)"); 
    lstFrom.addItem("Inch(in)"); 
    lstFrom.addItem("Foot(ft)"); 
    lstFrom.addItem("Yard(yd)"); 
    lstFrom.addItem("mile(mile)"); 

    lstTo.addItem("Centimeter(cm)"); 
    lstTo.addItem("Meter(m)"); 
    lstTo.addItem("Inch(in)"); 
    lstTo.addItem("Foot(ft)"); 
    lstTo.addItem("Yard(yd)"); 
    lstTo.addItem("mile(mile)"); 
    }//if(Length). 

/* TEMPERATURE */ 
   if(strCategory.equalsIgnoreCase("Temperature")){ 
    lstFrom.addItem("Celcius(C)"); 
    lstFrom.addItem("Farenheit(F)"); 

    lstTo.addItem("Celcius(C)"); 
    lstTo.addItem("Farenheit(F)"); 
   }//if("Temperature") 

/* AREA */ 
   if(strCategory.equalsIgnoreCase("Area")){ 
    lstFrom.addItem("CentimeterSquare(cm2)"); 
    lstFrom.addItem("meterSquare(m2)"); 
    lstFrom.addItem("inchSquare(in2)"); 
    lstFrom.addItem("footSquare(ft2)"); 
    lstFrom.addItem("yardSquare(yd2)"); 
    lstFrom.addItem("mileSquare(mile2)"); 

    lstTo.addItem("CentimeterSquare(cm2)"); 
    lstTo.addItem("meterSquare(m2)"); 
    lstTo.addItem("inchSquare(in2)"); 
    lstTo.addItem("footSquare(ft2)"); 
    lstTo.addItem("yardSquare(yd2)"); 
    lstTo.addItem("mileSquare(mile2)"); 
   }//if("Area") 

/* VOLUME */ 
   if(strCategory.equalsIgnoreCase("Volume")){ 
    lstFrom.addItem("MeterCube(m3)"); 
    lstFrom.addItem("CentimeterCube(cm3)"); 
    lstFrom.addItem("Liter(liter)"); 
    lstFrom.addItem("InchCube(in3)"); 
    lstFrom.addItem("footCube(ft3)"); 
    lstFrom.addItem("QT(qt)"); 
    lstFrom.addItem("Gallon(ga1)"); 

    lstTo.addItem("MeterCube(m3)"); 
    lstTo.addItem("CentimeterCube(cm3)"); 
    lstTo.addItem("Liter(liter)"); 
    lstTo.addItem("InchCube(in3)"); 
    lstTo.addItem("footCube(ft3)"); 
    lstTo.addItem("QT(qt)"); 
    lstTo.addItem("Gallon(ga1)"); 

   }//if("Volume") 

/* MASS */ 
   if(strCategory.equalsIgnoreCase("Mass")){ 
    lstFrom.addItem("Gram(g)"); 
    lstFrom.addItem("KiloGram(Kg)"); 
    lstFrom.addItem("OZ (avdp)"); 
    lstFrom.addItem("lb (avdp)"); 
    lstFrom.addItem("metric ton"); 
    lstFrom.addItem("Ton(ton)"); 

    lstTo.addItem("Gram(g)"); 
    lstTo.addItem("KiloGram(Kg)"); 
    lstTo.addItem("OZ (avdp)"); 
    lstTo.addItem("lb (avdp)"); 
    lstTo.addItem("metric ton"); 
    lstTo.addItem("Ton(ton)"); 
   }//if("Mass") 

 }//LoadListBoxes() 

  void butConvert_actionPerformed(ActionEvent e) { 

/* The Selected indexes from list boxes */ 
   int ilstFrom = -1; 
   int ilstTo = -1; 
/* multiplication factor */ 
   double dbFactor =0.0; 
   double resultNum = 0.0; 

   /* Get the number to be converted */ 
    try{ 
    iNum = (new Double(txtNumber.getText())).doubleValue(); 
    }catch (NumberFormatException nfe){ 
    }//catch 

    /* get the indexes of the selected list items */ 
    ilstFrom = lstFrom.getSelectedIndex(); 
    ilstTo   = lstTo.getSelectedIndex(); 

   /* Confirm that both the list boxes are selected */ 
    if(ilstFrom == -1 || ilstTo == -1){ 
    }//if(ilstFrom == -1 || ilstTo == -1) 

   /* If the category is length */ 
   if(strCategory.equalsIgnoreCase("Length")){ 
      dbFactor = arryLength[ilstFrom][ilstTo]; 
      resultNum = dbFactor * iNum; 
      txtResult.setText("" + resultNum); 
     }//if("Length") 

   /* If the category is Temperature */ 
   if(strCategory.equalsIgnoreCase("Temperature")){ 
      if(ilstFrom == 0 && ilstTo ==0){ 
      resultNum = iNum; 
      } 
      if(ilstFrom == 0 && ilstTo == 1){ 
      resultNum = (iNum * 1.8) + 32 ; 
      } 
      if(ilstFrom == 1 && ilstTo == 0){ 
      resultNum = 0.55 *(iNum - 32 ) ; 
      } 
      if(ilstFrom == 1 && ilstTo == 1){ 
      resultNum = iNum ; 
      } 
      txtResult.setText("" + resultNum); 
     }//if("Temperature") 

   /* If the category is Area */ 
   if(strCategory.equalsIgnoreCase("Area")){ 
      dbFactor = arryArea[ilstFrom][ilstTo]; 
      resultNum = dbFactor * iNum; 
      txtResult.setText("" + resultNum); 
     }//if("Area") 

   /* If the category is Volume */ 
   if(strCategory.equalsIgnoreCase("Volume")){ 
      dbFactor = arryVolume[ilstFrom][ilstTo]; 
      resultNum = dbFactor * iNum; 
      txtResult.setText("" + resultNum); 
     }//if("Volume") 

   /* If the category is Mass */ 
   if(strCategory.equalsIgnoreCase("mass")){ 
      dbFactor = arryMass[ilstFrom][ilstTo]; 
      resultNum = dbFactor * iNum; 
      txtResult.setText("" + resultNum); 
     }//if("Volume") 

  }//butConvert_... 

}//applet 

class PKTConv_chCategory_itemAdapter implements java.awt.event.ItemListener{ 
  PKTConv adaptee; 

  PKTConv_chCategory_itemAdapter(PKTConv adaptee) { 
    this.adaptee = adaptee; 
  } 

  public void itemStateChanged(ItemEvent e) { 
    adaptee.chCategory_itemStateChanged(e); 
  } 
}//class 

class PKTConv_butConvert_actionAdapter implements java.awt.event.ActionListener { 
  PKTConv adaptee; 

  PKTConv_butConvert_actionAdapter(PKTConv adaptee) { 
    this.adaptee = adaptee; 
  } 

  public void actionPerformed(ActionEvent e) { 
    adaptee.butConvert_actionPerformed(e); 
  } 
}//class 
