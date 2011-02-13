// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.xml;

import net.sf.pkt.PKTXUL;
import net.sf.pkt.widgets.Attr;
import net.sf.pkt.widgets.TContainer;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

// Referenced classes of package net.sf.pkt.xml:
//            SortedVector

public class TDocument extends Document
{

    public static Element getElem(String s)
    {
        return (Element)hashtable.get(s);
    }

    public static String getID(Element element)
    {
        return element.getAttributeValue("__XML_NODE_ID");
    }

    public static Element getParent(String s)
    {
        return getElem(s).getParent();
    }

    public static Iterator getAttributeNames(Element element)
    {
        return (new SortedVector(element.getAttributes().iterator())).iterator();
    }

    public static void insert(Element element, Element element1)
    {
        setUniqueID(element1);
        element.getChildren().add(element1);
    }

    public static void moveUp(Element element)
    {
        Element element1 = element.getParent();
        if(element1 != null)
        {
            List list = element1.getChildren();
            int i = 1;
            do
            {
                if(i >= list.size())
                    break;
                if(element.equals(list.get(i)))
                {
                    list.add(i - 1, list.remove(i));
                    break;
                }
                i++;
            } while(true);
        }
    }

    public static void moveDn(Element element)
    {
        Element element1 = element.getParent();
        if(element1 != null)
        {
            List list = element1.getChildren();
            int i = 0;
            do
            {
                if(i >= list.size() - 1)
                    break;
                if(element.equals(list.get(i)))
                {
                    list.add(i + 1, list.remove(i));
                    break;
                }
                i++;
            } while(true);
        }
    }

    public static Element remove(Element element)
    {
        Element element1 = element.getParent();
        String s = getID(element);
        if(element1 != null)
        {
            element1.getChildren().remove(element);
            hashtable.remove(s);
        }
        return element1;
    }

    public static boolean pull(Element element)
    {
        String s = element.getName();
        Element element1;
label0:
        for(Element element2 = element.getParent(); null != (element1 = element2.getParent()); element2 = element1)
        {
            if(!(net.sf.pkt.widgets.TContainer.class).isAssignableFrom(element1.getClass()))
                continue;
            Iterator iterator = ((TContainer)element1).getSubTagNames().iterator();
            do
                if(iterator == null || !iterator.hasNext())
                    continue label0;
            while(!s.equals(iterator.next().toString()));
            element.detach();
            element1.getChildren().add(element);
            return true;
        }

        return false;
    }

    public static boolean copypaste(Element element, Element element1)
    {
label0:
        {
            String s = element1.getName();
            if(!(net.sf.pkt.widgets.TContainer.class).isAssignableFrom(element.getClass()))
                break label0;
            Iterator iterator = ((TContainer)element).getSubTagNames().iterator();
            do
                if(iterator == null || !iterator.hasNext())
                    break label0;
            while(!s.equals(iterator.next().toString()));
            insert(element, (Element)element1.clone());
            return true;
        }
        return false;
    }

    public static boolean cutpaste(Element element, Element element1)
    {
label0:
        {
            String s = element1.getName();
            if(!(net.sf.pkt.widgets.TContainer.class).isAssignableFrom(element.getClass()))
                break label0;
            Iterator iterator = ((TContainer)element).getSubTagNames().iterator();
            do
                if(iterator == null || !iterator.hasNext())
                    break label0;
            while(!s.equals(iterator.next().toString()));
            element1.detach();
            element.getChildren().add(element1);
            return true;
        }
        return false;
    }

    private static void setUniqueID(Element element)
    {
        element.setAttribute("__XML_NODE_ID", String.valueOf(nodeId));
        hashtable.put(String.valueOf(nodeId++), element);
        for(Iterator iterator = element.getChildren().iterator(); iterator != null && iterator.hasNext(); setUniqueID((Element)iterator.next()));
    }

    public TDocument()
    {
        hashtable = new Hashtable();
        nodeId = 0;
    }

    public TDocument(Element element)
    {
        this();
        setRootElement(element);
        insertDefaultProperties(getRootElement());
    }

    public TDocument(String s)
        throws JDOMException, IOException
    {
        this();
        Document document = (new SAXBuilder()).build(new StringReader(s));
        document = getObjectDOM(document);
        setRootElement(document.detachRootElement());
        insertDefaultProperties(getRootElement());
    }

    public TDocument(InputStream inputstream)
        throws JDOMException, IOException
    {
        this();
        Document document = (new SAXBuilder()).build(inputstream);
        document = getObjectDOM(document);
        setRootElement(document.detachRootElement());
        insertDefaultProperties(getRootElement());
    }

    private static Element getObjectElement(Element element)
    {
        Element element1 = null;
        try
        {
            element1 = (Element)Class.forName(PKTXUL.WIDGET_PREFIX + element.getName()).newInstance();
            for(int i = 0; i < element.getAttributes().size(); i++)
            {
                Attribute attribute = (Attribute)element.getAttributes().get(i);
                element1.setAttribute((Attribute)attribute.clone());
            }

            for(int j = 0; j < element.getChildren().size(); j++)
                element1.getChildren().add(getObjectElement((Element)element.getChildren().get(j)));

        }
        catch(InstantiationException instantiationexception)
        {
            System.err.println(instantiationexception.getMessage());
        }
        catch(IllegalAccessException illegalaccessexception)
        {
            System.err.println(illegalaccessexception.getMessage());
        }
        catch(ClassNotFoundException classnotfoundexception)
        {
            System.err.println(classnotfoundexception.getMessage());
        }
        return element1;
    }

    private static Document getObjectDOM(Document document)
    {
        Document document1 = new Document();
        document1.setRootElement(getObjectElement(document.getRootElement()));
        return document1;
    }

    public Document setRootElement(Element element)
    {
        setUniqueID(element);
        return super.setRootElement(element);
    }

    public InputStream getXml4Preview(String s)
        throws IOException
    {
        Document document = (Document)clone();
        XMLOutputter xmloutputter = new XMLOutputter();
        StringWriter stringwriter = new StringWriter();
        adjust(document.getRootElement(), true, s);
        xmloutputter.output(document, stringwriter);
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(stringwriter.toString().getBytes());
        stringwriter.close();
        return bytearrayinputstream;
    }

    public static void adjust(Element element, boolean flag, String s)
    {
        String s1 = element.getName();
        String s2 = getID(element);
        Element element1 = null;
        try
        {
            element1 = (Element)Class.forName("net.sf.pkt.widgets." + s1).newInstance();
        }
        catch(InstantiationException instantiationexception) { }
        catch(IllegalAccessException illegalaccessexception) { }
        catch(ClassNotFoundException classnotfoundexception) { }
        for(Iterator iterator = element.getChildren().iterator(); iterator != null && iterator.hasNext(); adjust((Element)iterator.next(), flag, s));
        if(flag)
        {
            if(element1.getAttribute("name") != null)
                element.setAttribute(new Attribute("name", s2));
            if(element1.getAttribute("action") != null)
                element.setAttribute(new Attribute("action", "clickSync(this.name)"));
        }
        List list = element.getAttributes();
        for(int i = list.size() - 1; 0 <= i; i--)
        {
            Attribute attribute = (Attribute)list.get(i);
            String s3 = attribute.getName();
            Attribute attribute1 = element1.getAttribute(s3);
            if(attribute1 != null && attribute1.getValue().equals(attribute.getValue()))
            {
                element.removeAttribute(attribute);
                continue;
            }
            if("__XML_NODE_ID".equals(s3))
            {
                element.removeAttribute(attribute);
                continue;
            }
            if(!flag)
                continue;
            if(s != null && "icon".equals(s3))
            {
                attribute.setValue("file:///" + s + attribute.getValue());
                continue;
            }
            if("accelerator".equals(s3))
            {
                element.removeAttribute(attribute);
                continue;
            }
            if(Attr.METHOD_ATTR.contains(s3) && !"action".equals(s3))
                element.removeAttribute(attribute);
        }

    }

    public static void insertDefaultProperties(Element element)
    {
        try
        {
            Element element1 = (Element)Class.forName("net.sf.pkt.widgets." + element.getName()).newInstance();
            Iterator iterator = element1.getAttributes().iterator();
            do
            {
                if(iterator == null || !iterator.hasNext())
                    break;
                Attribute attribute = (Attribute)iterator.next();
                String s = attribute.getName();
                if(element.getAttribute(s) == null)
                    element.setAttribute((Attribute)element1.getAttribute(s).clone());
            } while(true);
            for(Iterator iterator1 = element.getChildren().iterator(); iterator1 != null && iterator1.hasNext(); insertDefaultProperties((Element)iterator1.next()));
        }
        catch(Exception exception)
        {
            PKTXUL.getLogger().severe(exception.toString());
            exception.printStackTrace();
        }
    }

    public static void writeXMLfile(Element element, File file, boolean flag)
        throws IOException
    {
        Document document = new Document();
        XMLOutputter xmloutputter = new XMLOutputter("  ", true);
        document.setRootElement((Element)element.clone());
        adjust(document.getRootElement(), false, null);
        xmloutputter.setEncoding(flag ? "UTF-8" : "ISO-8859-1");
        xmloutputter.setTextNormalize(true);
        FileWriter filewriter = new FileWriter(file);
        xmloutputter.output(document, filewriter);
        filewriter.close();
    }

    public static String getPrettyXml(Document document, boolean flag)
    {
        Document document1 = (Document)document.clone();
        XMLOutputter xmloutputter = new XMLOutputter("  ", true);
        xmloutputter.setEncoding(flag ? "UTF-8" : "ISO-8859-1");
        xmloutputter.setTextNormalize(true);
        adjust(document1.getRootElement(), false, null);
        return xmloutputter.outputString(document1);
    }

    public static Document readXMLfile(File file)
        throws JDOMException, IOException
    {
        SAXBuilder saxbuilder = new SAXBuilder();
        FileReader filereader = new FileReader(file);
        Document document = saxbuilder.build(filereader);
        document = getObjectDOM(document);
        filereader.close();
        insertDefaultProperties(document.getRootElement());
        return document;
    }

    public static int size(Element element)
    {
        int i = 1;
        if(element.getChildren() != null)
        {
            for(int j = 0; j < element.getChildren().size(); j++)
                i += size((Element)element.getChildren().get(j));

        }
        return i;
    }

    private static final String WIDGET_PREFIX = "net.sf.pkt.widgets.";
    private static int nodeId = 0;
    private static Hashtable hashtable = new Hashtable();
    public static final String UTF = "UTF-8";
    public static final String ISO = "ISO-8859-1";
    public static final String ID_KEY = "__XML_NODE_ID";

}
