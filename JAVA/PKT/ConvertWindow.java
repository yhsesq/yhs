// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// Decompiler options: fullnames safe 
// Source File Name:   ConvertWindow.java

package net.sf.pkt.hexed.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.*;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexEditor

public class ConvertWindow extends java.awt.Frame
    implements java.awt.event.ActionListener, java.awt.event.TextListener, java.awt.event.ItemListener
{

    public ConvertWindow(java.awt.Frame parent)
    {
        this();
        this.parent = parent;
    }

    public ConvertWindow()
    {
        super("Converter");
        setBackground(java.awt.Color.lightGray);
 //       setIconImage(net.sf.pkt.hexed.gui.HexEditor.icon);
        java.awt.Panel p2;
        add("North", ((java.awt.Component) (p2 = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        java.awt.Panel p;
        p2.add("North", ((java.awt.Component) (p = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        p.add("West", ((java.awt.Component) (typ1 = net.sf.pkt.hexed.gui.ConvertWindow.getTypeChoice())));
        java.awt.Panel p3;
        p.add("Center", ((java.awt.Component) (p3 = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        p3.add("West", ((java.awt.Component) (subtyp1 = net.sf.pkt.hexed.gui.ConvertWindow.getSubTypeChoice())));
        p3.add("Center", ((java.awt.Component) (input = new TextField(40))));
        input.addTextListener(((java.awt.event.TextListener) (this)));
        typ1.addItemListener(((java.awt.event.ItemListener) (this)));
        subtyp1.addItemListener(((java.awt.event.ItemListener) (this)));
        p2.add("Center", ((java.awt.Component) (p = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        p.add("Center", ((java.awt.Component) (result = new TextField(60))));
        java.awt.Button b;
        p.add("East", ((java.awt.Component) (b = new Button("^^ Up ^^"))));
        b.addActionListener(((java.awt.event.ActionListener) (this)));
        p2.add("South", ((java.awt.Component) (p = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        p.add("West", ((java.awt.Component) (typ2 = net.sf.pkt.hexed.gui.ConvertWindow.getTypeChoice())));
        p.add("Center", ((java.awt.Component) (p3 = new Panel(((java.awt.LayoutManager) (new BorderLayout()))))));
        p3.add("West", ((java.awt.Component) (subtyp2 = net.sf.pkt.hexed.gui.ConvertWindow.getSubTypeChoice())));
        p3.add("Center", ((java.awt.Component) (output = new TextField(40))));
        typ2.addItemListener(((java.awt.event.ItemListener) (this)));
        subtyp2.addItemListener(((java.awt.event.ItemListener) (this)));
        result.setEditable(false);
        output.setEditable(false);
        addWindowListener(((java.awt.event.WindowListener) (new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e)
            {
                dispose();
            }

        })));
        pack();
        show();
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
        java.lang.String cmd = e.getActionCommand();
        if("^^ Up ^^".equals(((java.lang.Object) (cmd))))
            input.setText(output.getText());
    }

    public void itemStateChanged(java.awt.event.ItemEvent e)
    {
        refreshView();
    }

    public void textValueChanged(java.awt.event.TextEvent e)
    {
        refreshView();
    }

    private void refreshView()
    {
        result.setText(net.sf.pkt.hexed.gui.ConvertWindow.bytes2hex(net.sf.pkt.hexed.gui.ConvertWindow.type2bytes(input.getText(), typ1.getSelectedIndex(), subtyp1.getSelectedIndex())));
        output.setText(net.sf.pkt.hexed.gui.ConvertWindow.bytes2type(net.sf.pkt.hexed.gui.ConvertWindow.hex2bytes(result.getText()), typ2.getSelectedIndex(), subtyp2.getSelectedIndex()));
    }

    public static java.lang.String bytes2hex(byte b[])
    {
        java.lang.StringBuffer sb = new StringBuffer();
        for(int i = 0; i < b.length; i++)
        {
            int by = b[i] & 0xff;
            sb.append(by >= 16 ? "" : "0").append(java.lang.Integer.toHexString(by)).append(" ");
        }

        return sb.toString().toUpperCase();
    }

    private static byte[] hex2bytes(java.lang.String s)
    {
        byte b[] = new byte[s.length() / 3];
        for(int i = 0; i < b.length; i++)
            b[i] = (byte)java.lang.Integer.parseInt(s.substring(i * 3, i * 3 + 2), 16);

        return b;
    }

    public static byte[] type2bytes(java.lang.String text, int typ, int subtyp)
    {
        byte array[];
        boolean signed;
        boolean bigendian;
        array = new byte[0];
        signed = subtyp % 2 == 1;
        bigendian = subtyp / 2 == 1;
        switch(typ){
case 0:
	ByteArrayOutputStream bx=new ByteArrayOutputStream();
        for(int i = 0; i < text.length(); i++)
        {
            byte bb[] = text.substring(i, i + 1).getBytes();
            if(bb.length==1){bx.write((byte)(bb[0]%256));}
        }
        array=bx.toByteArray();
          break;
case 1:
        array = new byte[text.length() * 2];
        for(int i = 0; i < text.length(); i++)
        {
            int cc = ((int) (text.charAt(i)));
            array[i * 2] = (byte)(cc % 256);
            array[i * 2 + 1] = (byte)(cc / 256);
        }

        break; /* Loop/switch isn't completed */
case 2:
        try
        {
            array = text.getBytes("UTF-8");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 3:
        array = new byte[text.length() / 2 + 1];
        int offs = 0;
        for(int i = 0; i < text.length(); i += 2)
        {
            for(; i < text.length() && text.charAt(i) == ' '; i++);
            if(i + 1 > text.length())
                break;
            if(i + 1 < text.length() && text.substring(i, i + 2).equals("0x"))
                i += 2;
            if(i + 1 > text.length())
                break;
            if(text.charAt(i) == ' ')
            {
                array[offs++] = 0;
                i--;
                continue;
            }
            if(i + 1 == text.length() || text.charAt(i + 1) == ' ')
            {
                try
                {
                    array[offs++] = (byte)java.lang.Integer.parseInt(text.substring(i, i + 1), 16);
                }
                catch(java.lang.NumberFormatException e)
                {
                    array[offs - 1] = 0;
                }
                i--;
                continue;
            }
            try
            {
                array[offs++] = (byte)java.lang.Integer.parseInt(text.substring(i, i + 2), 16);
            }
            catch(java.lang.NumberFormatException e)
            {
                array[offs - 1] = 0;
            }
        }

        byte array2[] = new byte[offs];
        java.lang.System.arraycopy(((java.lang.Object) (array)), 0, ((java.lang.Object) (array2)), 0, offs);
        array = new byte[offs];
        java.lang.System.arraycopy(((java.lang.Object) (array2)), 0, ((java.lang.Object) (array)), 0, offs);
        break; /* Loop/switch isn't completed */
case 6:
        try
        {
            int i = java.lang.Integer.parseInt(text);
            if(!signed && i >= 0 && i < 256 || signed && i >= -128 && i < 128)
                array = (new byte[] {
                    (byte)i
                });
            else
                array = new byte[0];
        }
        catch(java.lang.NumberFormatException e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 7:
        try{
        array = net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert(text, 2, signed, bigendian);
	}catch(Exception e){array=new byte[0];}
	break; /* Loop/switch isn't completed */
case 8:
        try{array = net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert(text, 4, signed, bigendian);
        }catch(Exception e){array=new byte[0];}break; /* Loop/switch isn't completed */
case 9:
        try{array = net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert(text, 8, signed, bigendian);
        }catch(Exception e){array=new byte[0];}break; /* Loop/switch isn't completed */
case 4:
        int bcount = 0;
        int bcoll = 0;
        int ccount = 0;
        byte collect[] = new byte[100];
        for(int i = 0; i < text.length(); i++)
        {
            switch(text.charAt(i))
            {
            case 48: // '0'
                bcoll *= 2;
                bcount++;
                break;

            case 49: // '1'
                bcoll *= 2;
                bcoll++;
                bcount++;
                break;
            }
            if(bcount == 8)
            {
                collect[ccount++] = (byte)bcoll;
                bcount = bcoll = 0;
            }
        }

        if(bcount != 0)
        {
            // java.lang.System.out.println(bcount);
            array = new byte[0];
            break; /* Loop/switch isn't completed */
        }
        array = new byte[ccount];
        if(!bigendian)
        {
            for(int i = 0; i < ccount; i++)
                array[i] = collect[ccount - i - 1];

        } else
        {
            java.lang.System.arraycopy(((java.lang.Object) (collect)), 0, ((java.lang.Object) (array)), 0, ccount);
        }
        break; /* Loop/switch isn't completed */
case 5:
        java.lang.String s;
        s = null;
        try
        {
            s = (java.lang.String)java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getContents(((java.lang.Object) (null))).getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
        }
        catch(java.awt.datatransfer.UnsupportedFlavorException ex)
        {
            s = null;
        }
        catch(java.io.IOException ex)
        {
            s = null;
        }
        if(s == null)
            return new byte[0];
        try{return s.getBytes("ISO-8859-1");}
        catch(java.io.UnsupportedEncodingException e){
        return s.getBytes();}
case 10:
        try
        {
            float f = java.lang.Float.parseFloat(text);
            array = net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert(java.lang.Float.floatToIntBits(f), 4, true, bigendian);
        }
        catch(Exception f)
        {
            return new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 11:
        try
        {
            double d = java.lang.Double.parseDouble(text);
            array = net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert(java.lang.Double.doubleToLongBits(d), 8, true, bigendian);
        }
        catch(Exception d)
        {
            return new byte[0]; 
        }break;
case 12:
        try
        {
            array = text.getBytes("US-ASCII");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 13:
        try
        {
            array = text.getBytes("ISO-8859-1");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 14:
        try
        {
            array = text.getBytes("UTF-16BE");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 15:
        try
        {
            array = text.getBytes("UTF-16LE");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
case 16:
        try
        {
            array = text.getBytes("UTF-16");
        }
        catch(Exception e)
        {
            array = new byte[0];
        }
        break; /* Loop/switch isn't completed */
default:
        array = new byte[0];
	}
        return array;
    }

    public static java.lang.String bytes2type(byte b[], int typ, int subtyp)
    {
        boolean signed;
        boolean bigendian;
        java.lang.StringBuffer sb;
        signed = subtyp % 2 == 1;
        bigendian = subtyp / 2 == 1;
        sb = new StringBuffer();
        switch(typ){
case 0:
        for(int i = 0; i < b.length; i++)
        {
            java.lang.String s = new String(new byte[] {
                b[i]
            });
            if(s.length() == 1 && s.charAt(0) != 0)
                sb.append(s);
            else
                sb.append("?");
        }

        return sb.toString();
case 1:
        for(int i = 0; i < b.length && b.length != i + 1; i += 2)
        {
            int ci = (b[i] & 0xff) + (b[i + 1] & 0xff) * 256;
            sb.append((char)ci);
        }

        java.lang.System.out.println(sb.toString());
        return sb.toString();
case 2:
        try{return new String(b, "UTF-8");}
        catch(java.io.UnsupportedEncodingException e){
        return "";}
case 3:
        for(int i = 0; i < b.length; i++)
            sb.append(i != 0 ? " " : "").append((b[i] & 0xff) >= 16 ? "" : "0").append(java.lang.Integer.toHexString(b[i] & 0xff).toUpperCase());
        return sb.toString();
case 6:
        return net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvert(b, 1, signed, bigendian);
case 7:
        return net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvert(b, 2, signed, bigendian);
case 8:
        return net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvert(b, 4, signed, bigendian);
case 9:
        return net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvert(b, 8, signed, bigendian);
case 4:
        for(int i = 0; i < b.length; i++)
        {
            for(int j = 7; j >= 0; j--)
                if((b[bigendian ? i : b.length - i - 1] & 1 << j) != 0)
                    sb.append('1');
                else
                    sb.append('0');

            sb.append(' ');
        }

        return sb.toString();
case 5:
        java.lang.String s;
        try
        {
            s = new String(b, "ISO-8859-1");
        }
        catch(java.io.UnsupportedEncodingException e)
        {
            s = new String(b);
            e.printStackTrace();
        }
        java.awt.datatransfer.StringSelection ss = new StringSelection(s);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(((java.awt.datatransfer.Transferable) (ss)), ((java.awt.datatransfer.ClipboardOwner) (ss)));
        return "Saved into clipboard";
case 10:
        int v = (int)net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvertL(b, 4, true, bigendian);
        try{return "" + java.lang.Float.intBitsToFloat(v);}
        catch(java.lang.IllegalArgumentException e){
        return "";}
case 11:
        long e = ((long) (net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvertL(b, 8, true, bigendian)));
        try{return "" + java.lang.Double.longBitsToDouble(((long) (e)));}
        catch(Exception x){
        return "";}
case 12:
        try{return new String(b, "US-ASCII");}
        catch(Exception exx){
        return "";}
case 13:
        try{return new String(b, "ISO-8859-1");}
        catch(Exception exx){
        return "";}
case 14:
        try{return new String(b, "UTF-16BE");}
        catch(Exception exx){
        return "";}
case 15:
        try{return new String(b, "UTF-16LE");}
        catch(Exception exx){
        return "";}
case 16:
        try{return new String(b, "UTF-16");}
        catch(Exception exx){
        return "";}
default:
        return "";
    }}

    public static byte[] makenumconvert(long num, int len, boolean signed, boolean bigendian)
    {
        return net.sf.pkt.hexed.gui.ConvertWindow.makenumconvert("" + num, len, signed, bigendian);
    }

    public static byte[] makenumconvert(java.lang.String text, int len, boolean signed, boolean bigendian)
    {
        byte array[];
        byte leer[];
        long max;
        long min;
        array = new byte[len];
        leer = new byte[0];
        max = 1L << len * 8;
        min = 0L;
        if(signed)
        {
            max /= 2L;
            min = -max;
        }
        long l = java.lang.Long.parseLong(text);
        if(len == 8 || l >= min && l < max)
        {
            for(int i = 0; i < len; i++)
            {
                array[i] = (byte)(int)(l & 255L);
                l >>= 8;
            }
        }
        
        if(bigendian)
        {
            byte rev[] = new byte[len];
            for(int i = 0; i < len; i++)
                rev[len - i - 1] = array[i];

            return rev;
        } else
        {
            return array;
        }
    }

    public static long makenumunconvertL(byte by[], int len, boolean signed, boolean bigendian)
    {
        byte b[] = by;
        long l = 0L;
        if(by.length < len)
        {
            b = new byte[len];
            if(bigendian)
            {
                for(int i = 0; i < by.length; i++)
                    b[by.length - i - 1] = by[i];

            } else
            {
                java.lang.System.arraycopy(((java.lang.Object) (by)), 0, ((java.lang.Object) (b)), 0, by.length);
            }
            if(by.length != 0 && b[by.length - 1] < 0 && signed)
            {
                for(int i = by.length; i < len; i++)
                    b[i] = -1;

            }
        } else
        if(bigendian)
        {
            b = new byte[len];
            for(int i = 0; i < len; i++)
                b[i] = by[by.length - 1 - i];

        }
        for(int i = len - 1; i >= 0; i--)
        {
            l <<= 8;
            l |= b[i] & 0xff;
        }

        if(len != 8)
        {
            long max = 1L << 8 * len;
            if(l >= max || l < 0L)
                return 0L;
            if(signed && l >= max / 2L)
                l -= max;
        }
        return l;
    }

    public static java.lang.String makenumunconvert(byte by[], int len, boolean signed, boolean bigendian)
    {
        return "" + net.sf.pkt.hexed.gui.ConvertWindow.makenumunconvertL(by, len, signed, bigendian);
    }

    public static java.awt.Choice getTypeChoice()
    {
        java.awt.Choice typ = new Choice();
        typ.add("ASCII text"); //0
        typ.add("Unicode text"); //1
        typ.add("UTF-8 text"); //2
        typ.add("Hex values"); //3
        typ.add("Bit mask");  //4
        typ.add("Clipboard"); //5
        typ.add("8-bit integer (byte)"); //6
        typ.add("16-bit integer (short)"); //7
        typ.add("32-bit integer (int)");  //8
        typ.add("64-bit integer (long)"); //9
        typ.add("32-bit float"); //10
        typ.add("64-bit float (double)"); //11
        typ.add("US-ASCII");
        typ.add("ISO-8859-1");
        typ.add("UTF-16BE text");
        typ.add("UTF-16LE text");
        typ.add("UTF-16 text");
        return typ;
    }

    public static java.awt.Choice getSubTypeChoice()
    {
        java.awt.Choice typ2 = new Choice();
        typ2.add("Unsigned Little Endian");
        typ2.add("Signed Little Endian");
        typ2.add("Unsigned Big Endian");
        typ2.add("Signed Big Endian");
        return typ2;
    }

    java.awt.Choice typ1;
    java.awt.Choice typ2;
    java.awt.Choice subtyp1;
    java.awt.Choice subtyp2;
    java.awt.TextField input;
    java.awt.TextField result;
    java.awt.TextField output;
    java.awt.Frame parent;
}
