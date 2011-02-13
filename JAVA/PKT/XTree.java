// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.thinutil;

import net.sf.pkt.xml.TDocument;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import thinlet.Thinlet;

public class XTree
{

    public XTree()
    {
    }

    public static boolean populate(Thinlet thinlet, Element element, Object obj, String s)
    {
        boolean flag = false;
        String s1 = element.getName();
        String s2 = TDocument.getID(element);
        String s3 = element.getAttributeValue("name");
        Thinlet _tmp = thinlet;
        Object obj1 = Thinlet.create("node");
        thinlet.setString(obj1, "name", s2);
        thinlet.setString(obj1, "text", s1 + "(" + s3 + ")");
        if(s2.equals(s))
        {
            thinlet.setBoolean(obj1, "selected", true);
            flag = true;
        }
        thinlet.add(obj, obj1);
        for(Iterator iterator = element.getChildren().iterator(); iterator != null && iterator.hasNext();)
            flag |= populate(thinlet, (Element)iterator.next(), obj1, s);

        thinlet.setBoolean(obj1, "expanded", flag);
        return flag;
    }

    public static Element getSelectedElement(Thinlet thinlet, Object obj)
    {
        Object obj1 = thinlet.getSelectedItem(obj);
        return obj1 == null ? null : TDocument.getElem(thinlet.getString(obj1, "name"));
    }

    public static Object getParent(Thinlet thinlet, Object obj, Object obj1)
    {
        Object obj2 = null;
        int i = thinlet.getCount(obj);
        for(int j = 0; j < i && obj2 == null; j++)
        {
            Object obj3 = thinlet.getItem(obj, j);
            if(obj3 == obj1)
                obj2 = obj;
            else
                obj2 = getParent(thinlet, obj3, obj1);
        }

        return obj2;
    }
}
