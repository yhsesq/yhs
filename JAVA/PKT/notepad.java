package net.sf.pkt.edit;
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
//  Original public domain code by Simon Scott 
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
import javax.swing.*;
import java.io.*;
import net.sf.pkt.ffind.*;

public class notepad extends JFrame {
        String title;
        String last="new";
        JFileChooser fc;
        JTextArea ta;
	JTextField findtext;
	JTextField replacetext;
	JMenuItem find,findnc,findr,findrnc,findra,findranc;
        JMenuItem nw,opn,sv,sva,cl;
        JMenuItem cut,cpy,pst;
        JMenuItem bld,itl,pli;
        JMenuItem mono,dial,serf;
        JMenuItem s8,s10,s12,s14,s16,s18;
        JMenuItem red,blue,green,black,white;
        JMenuItem bred,bblue,bgreen,bblack,bwhite;

    public notepad() {startit("");}

    public notepad(String addedtext) {startit(addedtext);}

    public void startit(String addedtext)
	{title="PKT Text Editor";
        setTitle(title);
        setSize(400,425);
        JMenuBar mb=new JMenuBar();
        setJMenuBar(mb);       
        JMenu fl=new JMenu("File");
        mb.add(fl);       
        JMenu ed=new JMenu("Edit");
        mb.add(ed);       
        JMenu fo=new JMenu("Format");
        mb.add(fo);
        JMenu hp=new JMenu("Help");
	findtext=new JTextField("FindText");
	mb.add(findtext);
	JMenu fm=new JMenu("Find");
	mb.add(fm);
	replacetext=new JTextField("ReplaceText");
	mb.add(replacetext);

	find = new JMenuItem("Find");
        fm.add(find);
	findnc = new JMenuItem("Find (Ignore Case)");
        fm.add(findnc);
	findr = new JMenuItem("Find & Replace");
        fm.add(findr);
	findrnc = new JMenuItem("Find & Replace (Ignore Case)");
        fm.add(findrnc);
	findra = new JMenuItem("Find & Replace All");
        fm.add(findra);
	findranc = new JMenuItem("Find & Replace All (Ignore Case)");
        fm.add(findranc);
        nw = new JMenuItem("New");
        fl.add(nw);
        opn = new JMenuItem("Open");
        fl.add(opn);
        sv = new JMenuItem("Save");
        fl.add(sv);
        sva = new JMenuItem("Save As...");
        fl.add(sva);
        cl = new JMenuItem("Close");
        fl.add(cl);       
        cut = new JMenuItem("Cut");
        ed.add(cut);
        cpy = new JMenuItem("Copy");
        ed.add(cpy);
        pst = new JMenuItem("Paste");
        ed.add(pst);       
        bld = new JMenuItem("Bold");
        fo.add(bld);
        itl = new JMenuItem("Italic");
        fo.add(itl);
        pli = new JMenuItem("Plain");
        fo.add(pli);       
        JMenu fnt=new JMenu("Font");
        fo.add(fnt);       
        mono = new JMenuItem("Monospace");
        fnt.add(mono);
        dial = new JMenuItem("Dialog");
        fnt.add(dial);
        serf = new JMenuItem("Serif");
        fnt.add(serf);       
        JMenu fntSiz=new JMenu("Font Size");
        fo.add(fntSiz);
        s8 = new JMenuItem("8");
        fntSiz.add(s8);
        s10 = new JMenuItem("10");
        fntSiz.add(s10);
        s12 = new JMenuItem("12");
        fntSiz.add(s12);
        s14 = new JMenuItem("14");
        fntSiz.add(s14);
        s16 = new JMenuItem("16");
        fntSiz.add(s16);
        s18 = new JMenuItem("18");
        fntSiz.add(s18);       
        JMenu fntColor=new JMenu("Font Color");
        fo.add(fntColor);
        white = new JMenuItem("White");
        fntColor.add(white);       
        red = new JMenuItem("Red");
        fntColor.add(red);
        green = new JMenuItem("Green");
        fntColor.add(green);
        blue = new JMenuItem("Blue");
        fntColor.add(blue);
        black = new JMenuItem("Black");
        fntColor.add(black);       
        JMenu bfntColor=new JMenu("Background Color");
        fo.add(bfntColor);
        bwhite = new JMenuItem("White");
        bfntColor.add(bwhite);       
        bred = new JMenuItem("Red");
        bfntColor.add(bred);
        bgreen = new JMenuItem("Green");
        bfntColor.add(bgreen);
        bblue = new JMenuItem("Blue");
        bfntColor.add(bblue);
        bblack = new JMenuItem("Black");
        bfntColor.add(bblack);       
        MenuListener ml=new MenuListener();       
        findnc.addActionListener(ml);
        find.addActionListener(ml);
        findr.addActionListener(ml);
        findrnc.addActionListener(ml);
        findra.addActionListener(ml);
        findranc.addActionListener(ml);
        nw.addActionListener(ml);
        opn.addActionListener(ml);
        sv.addActionListener(ml);
        sva.addActionListener(ml);
        cl.addActionListener(ml);
        cut.addActionListener(ml);
        cpy.addActionListener(ml);
        pst.addActionListener(ml);
        bld.addActionListener(ml);
        itl.addActionListener(ml);
        pli.addActionListener(ml);
        mono.addActionListener(ml);
        dial.addActionListener(ml);
        serf.addActionListener(ml);
        s8.addActionListener(ml);
        s10.addActionListener(ml);
        s12.addActionListener(ml);
        s14.addActionListener(ml);
        s16.addActionListener(ml);
        s18.addActionListener(ml);
        white.addActionListener(ml);
        red.addActionListener(ml);
        blue.addActionListener(ml);
        green.addActionListener(ml);
        black.addActionListener(ml);
        bwhite.addActionListener(ml);
        bred.addActionListener(ml);
        bblue.addActionListener(ml);
        bgreen.addActionListener(ml);
        bblack.addActionListener(ml);
        ta = new JTextArea(5,20);
        ta.setMargin(new Insets(2,2,2,2));
        JScrollPane sp = new JScrollPane(ta);
        fc = new JFileChooser();fc.setAccessory(new FindAccessory(fc));       
        getContentPane().add(sp);
        ta.setText(addedtext);
	show();
    } // ends constructor
       
public class MenuListener implements ActionListener{
	public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();               
            if (event.getSource() == cl){
				doExit();}
            if (event.getSource() == find){try{
		int ffind=0;
	if ((ta.getText()!=null) && (findtext.getText() != null)){
		ffind=find(ta.getText(),findtext.getText(),0,false);}
	if (ffind != -1){ta.setCaretPosition(ffind);}
            }catch(Exception e){}}
            if (event.getSource() == findnc){try{
		int ffind=0;
	if ((ta.getText()!=null) && (findtext.getText() != null)){
		ffind=find(ta.getText(),findtext.getText(),0,true);}
	if (ffind != -1){ta.setCaretPosition(ffind);}
            }catch(Exception e){}}
            if (event.getSource() == findr){try{
	if ((ta.getText()!=null) && (findtext.getText() != null)){
	ta.setText(replacefirst(ta.getText(),findtext.getText(),replacetext.getText(),false));}
            }catch(Exception e){}}
            if (event.getSource() == findrnc){try{
	if ((ta.getText()!=null) && (findtext.getText() != null)){
	ta.setText(replacefirst(ta.getText(),findtext.getText(),replacetext.getText(),true));}
            }catch(Exception e){}}
            if (event.getSource() == findra){try{
	if ((ta.getText()!=null) && (findtext.getText() != null)){
	ta.setText(replace(ta.getText(),findtext.getText(),replacetext.getText(),false));}
            }catch(Exception e){}}
            if (event.getSource() == findranc){try{
	if ((ta.getText()!=null) && (findtext.getText() != null)){
	ta.setText(replace(ta.getText(),findtext.getText(),replacetext.getText(),true));}
            }catch(Exception e){}}
            if (event.getSource() == nw){
                ta.setText("");
                setTitle("PKT Text Editor");
                last="new";
            }                       
            if (event.getSource() == opn){
                int returnVal = fc.showOpenDialog(notepad.this);
                if (returnVal == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    try{
                        BufferedReader inputStream = new BufferedReader(new FileReader(file.getPath()));
                        String inputLine;
                        ta.setText("");
                        last="sav";
                        setTitle("PKT Text Editor ["+file.getName()+"]");
                            while((inputLine = inputStream.readLine()) != null)ta.append(inputLine+"\n");
                    }
                    catch(FileNotFoundException e){
                        JOptionPane.showMessageDialog(null, "File Not Found", "", JOptionPane.WARNING_MESSAGE );
                    }
                    catch(Exception e){log("Error :"+e.toString()+" : "+file.getAbsoluteFile());}
				}
            }
			if (event.getSource() == sv ){
                if(last.equals("sav")){
                    try{
                        File file = fc.getSelectedFile();
                        FileWriter outputStream=new FileWriter(file.getPath());
                        outputStream.write(ta.getText());
                        outputStream.close();
                        last="sav";
                        setTitle("PKT Text Editor ["+file.getName()+"]");
                    }
                    catch(Exception e){log(e.toString());}
				}
                else if(last.equals("new")){
                    int returnVal = fc.showSaveDialog(notepad.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    try{
                        File file = fc.getSelectedFile();
                        FileWriter outputStream=new FileWriter(file.getPath()+".txt");
                        last="sav";
                        setTitle("PKT Text Editor ["+file.getName()+"]");
                        outputStream.write(ta.getText());
						outputStream.close();
                    }
                    catch(Exception e){log("Exception:"+e.toString());}
                }
            }
            if (event.getSource() == sva ){
                int returnVal = fc.showSaveDialog(notepad.this);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                    try{
						File file = fc.getSelectedFile();
						FileWriter outputStream=new FileWriter(file.getPath()+".txt");
						last="sav";
						setTitle("PKT Text Editor ["+file.getName()+"]");
						outputStream.write(ta.getText());
						outputStream.close();
                    }
                    catch(Exception e){log("Error:"+e.toString());}
			}
            if (event.getSource() == cut) ta.cut();
            if (event.getSource() == cpy) ta.copy();
            if (event.getSource() == pst) ta.paste();
            if (event.getSource() == bld)ta.setFont(new Font(ta.getFont().getName(),Font.BOLD,ta.getFont().getSize()));
            if (event.getSource() == itl) ta.setFont(new Font(ta.getFont().getName(),Font.ITALIC,ta.getFont().getSize()));
            if (event.getSource() == pli) ta.setFont(new Font(ta.getFont().getName(),Font.PLAIN,ta.getFont().getSize()));
            if (event.getSource() == mono)ta.setFont(new Font("MonoSpaced",ta.getFont().getStyle(),ta.getFont().getSize()));
            if (event.getSource() == dial) ta.setFont(new Font("Dialog",ta.getFont().getStyle(),ta.getFont().getSize()));
			if (event.getSource() == serf) ta.setFont(new Font("Serif",ta.getFont().getStyle(),ta.getFont().getSize()));
            if (event.getSource() == s8) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),8));
            if (event.getSource() == s10) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),10));
            if (event.getSource() == s12) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),12));
            if (event.getSource() == s14) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),14));
            if (event.getSource() == s16) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),16));
            if (event.getSource() == s18) ta.setFont(new Font(ta.getFont().getName(),ta.getFont().getStyle(),18));
            if (event.getSource() == white) { ta.setForeground(Color.white);ta.setCaretColor(Color.white);}
            if (event.getSource() == red) { ta.setForeground(Color.red);ta.setCaretColor(Color.red);}
            if (event.getSource() == blue) { ta.setForeground(Color.blue);ta.setCaretColor(Color.blue);}
            if (event.getSource() == green) { ta.setForeground(Color.green);ta.setCaretColor(Color.green);}
            if (event.getSource() == black) { ta.setForeground(Color.black);ta.setCaretColor(Color.black);}
            if (event.getSource() == bwhite) ta.setBackground(Color.white);
            if (event.getSource() == bred) ta.setBackground(Color.red);
	    if (event.getSource() == bblue) ta.setBackground(Color.blue);
            if (event.getSource() == bgreen) ta.setBackground(Color.green);
            if (event.getSource() == bblack) ta.setBackground(Color.black);
	}
}
private static int find(String str, String pattern, int fromoffset, boolean ignorecase) {
int reti=-1;
for(int i=fromoffset;i<str.length();i++){
if (str.regionMatches(ignorecase,i,pattern,0,pattern.length())){reti=i; break;}
}return reti;}
private static String replace(String str, String pattern, String replace, boolean ignorecase) {
int s = 0; int e = 0;
StringBuffer result = new StringBuffer();
if(pattern == null || pattern.equals("")) return str;
while ((e = find(str,pattern,s,ignorecase)) >= 0) {
result.append(str.substring(s, e)); result.append(replace);s = e+pattern.length();}
result.append(str.substring(s)); return result.toString();}
private static String replacefirst(String str, String pattern, String replace, boolean ignorecase) {
int s = 0; int e = 0;
StringBuffer result = new StringBuffer();
if(pattern == null || pattern.equals("")) return str;
if ((e = find(str,pattern,s,ignorecase)) >= 0) {
result.append(str.substring(s, e)); result.append(replace);s = e+pattern.length();}
result.append(str.substring(s)); return result.toString();}
    public void doExit(){dispose();}
    public static void log(String args){
JOptionPane.showMessageDialog(null, args, "", JOptionPane.WARNING_MESSAGE );}
    public static void main(String[] args){
	     new notepad().show();
	}
}
