// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.thinutil;

import thinlet.Thinlet;

public class XComboBox
{

    public XComboBox()
    {
    }

    public static Object createDropDown(Thinlet thinlet, String as[])
    {
        Thinlet _tmp = thinlet;
        Object obj = Thinlet.create("combobox");
        thinlet.setBoolean(obj, "editable", false);
        thinlet.setInteger(obj, "columns", 24);
        for(int i = 0; i < as.length; i++)
        {
            Thinlet _tmp1 = thinlet;
            Object obj1 = Thinlet.create("choice");
            thinlet.setString(obj1, "text", as[i]);
            thinlet.add(obj, obj1);
        }

        if(0 < as.length)
            thinlet.setInteger(obj, "selected", 0);
        return obj;
    }
}
