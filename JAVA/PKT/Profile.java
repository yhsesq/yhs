// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.xml;

import net.sf.pkt.PKTXUL;
import java.io.*;
import java.util.List;
import java.util.logging.Logger;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class Profile
{

    public Profile(File file1)
    {
        file = file1;
        try
        {
            jdoc = (new SAXBuilder()).build(file1);
        }
        catch(Exception exception)
        {
            jdoc = new Document(new Element("profile"));
            jdoc.getRootElement().getChildren().add(new Element("files"));
            jdoc.getRootElement().getChildren().add(new Element("log"));
            jdoc.getRootElement().getChild("log").setAttribute("purge", String.valueOf(true));
            file1.getParentFile().mkdir();
        }
    }

    public File getWorkDirectory()
    {
        File file1 = null;
        try
        {
            Element element = jdoc.getRootElement().getChild("files");
            if(0 < element.getChildren().size())
            {
                String s = ((Element)jdoc.getRootElement().getChild("files").getChildren().get(0)).getAttribute("abspath").getValue();
                File file2 = new File(s);
                file1 = file2.getParentFile();
            }
        }
        catch(Exception exception)
        {
            PKTXUL.getLogger().severe(exception.toString());
            exception.printStackTrace();
        }
        return file1;
    }

    public String[] getFiles()
    {
        Element element = jdoc.getRootElement().getChild("files");
        String as[] = new String[element.getChildren().size()];
        for(int i = 0; i < as.length; i++)
            as[i] = ((Element)element.getChildren().get(i)).getAttribute("abspath").getValue();

        return as;
    }

    public boolean isPurgeLog()
    {
        boolean flag = false;
        try
        {
            Element element = jdoc.getRootElement().getChild("log");
            flag = element.getAttribute("purge").getBooleanValue();
        }
        catch(DataConversionException dataconversionexception) { }
        return flag;
    }

    public void setPurgeLog(boolean flag)
    {
        Element element = jdoc.getRootElement().getChild("log");
        element.setAttribute("purge", String.valueOf(flag));
        write();
    }

    public void addFile(File file1)
    {
        Element element = new Element("file");
        Element element1 = jdoc.getRootElement().getChild("files");
        element.setAttribute("abspath", file1.getAbsolutePath());
        element1.getChildren().add(0, element);
        for(int i = element1.getChildren().size() - 1; 0 < i; i--)
            if(file1.getAbsolutePath().equals(((Element)element1.getChildren().get(i)).getAttribute("abspath").getValue()))
                element1.getChildren().remove(i);

        for(; 7 < element1.getChildren().size(); element1.getChildren().remove(element1.getChildren().size() - 1));
        write();
    }

    private void write()
    {
        XMLOutputter xmloutputter = new XMLOutputter("  ", true);
        xmloutputter.setTextNormalize(true);
        try
        {
            FileWriter filewriter = new FileWriter(file);
            xmloutputter.output(jdoc, filewriter);
            filewriter.close();
        }
        catch(IOException ioexception)
        {
            PKTXUL.getLogger().severe(ioexception.toString());
            ioexception.printStackTrace();
        }
    }

    public static final int MAXFILES = 7;
    private File file;
    private Document jdoc;
}
