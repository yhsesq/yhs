// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt;

import net.sf.pkt.ffind.*;
import net.sf.pkt.dialogs.AboutDialog;
import net.sf.pkt.dialogs.FontChooser;
import net.sf.pkt.dialogs.IO;
import net.sf.pkt.dialogs.SearchDialog;
import net.sf.pkt.dialogs.Settings;
import net.sf.pkt.dialogs.StartupDialog;
import net.sf.pkt.dialogs.URLDialog;
import net.sf.pkt.thinutil.XComboBox;
import net.sf.pkt.thinutil.XTree;
import net.sf.pkt.widgets.Attr;
import net.sf.pkt.widgets.TContainer;
import net.sf.pkt.widgets.dialog;
import net.sf.pkt.widgets.panel;
import net.sf.pkt.xml.Profile;
import net.sf.pkt.xml.TDocument;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import thinlet.FrameLauncher;
import thinlet.Thinlet;

public class PKTXUL extends Thinlet
{
    private File file;
    private boolean pasteCopy;
    private String aTitle;
    private String sColor;
    private String sFont;
    private String sIcon;
    Settings settings;
    URLDialog urlDialog;
    AboutDialog aboutDialog;
    SearchDialog searchDialog;
    StartupDialog startupDialog;
    private static File LOGFILE;
    private static Profile PROFILE;

    public static final void main(String args[])
        throws Exception
    {
        LOGFILE = File.createTempFile("pktxul", ".tmp"); LOGFILE.deleteOnExit();
        FileHandler filehandler = new FileHandler(LOGFILE.getAbsolutePath());
        filehandler.setFormatter(new SimpleFormatter());
        logger.addHandler(filehandler);
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.SEVERE);
        logger.warning("LOGFILE: " + LOGFILE.getAbsolutePath());
        sysinfo();
        PROFILE = new Profile(new File(PROFILE_PATH));
        PKTXUL pktxul = new PKTXUL();
        pktxul.uiFrame = new FrameLauncher("PKTXUL - GPL XUL Editor for Thinlet", pktxul, 800, 600);
        pktxul.updateMenu();
        pktxul.uiFrame.show();
    }

    private void updateMenu()
    {
        String as[] = PROFILE.getFiles();
        removeAll(menuReopen);
        if(as != null)
        {
            for(int i = 0; i < as.length; i++)
            {
                Object obj = Thinlet.create("menuitem");
                setString(obj, "text", as[i]);
                setMethod(obj, "action", "reopen(this.text)", this, this);
                add(menuReopen, obj);
            }

            setBoolean(menuReopen, "enabled", 0 < as.length);
        }
        setBoolean(menuPurge, "selected", PROFILE.isPurgeLog());
    }

    private void enable(Object aobj[])
    {
        for(int i = 0; i < aobj.length; i++)
            setBoolean(aobj[i], "enabled", true);

    }

    private void disable(Object aobj[])
    {
        for(int i = 0; i < aobj.length; i++)
            setBoolean(aobj[i], "enabled", false);

    }

    public final void exit()
    {
        exit("PKTXUL");
    }

    public final void exit(String s)
    {
        if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", s, 0))
            save();
        if(PROFILE.isPurgeLog())
            LOGFILE.deleteOnExit();
        System.exit(0);
    }

    public boolean destroy()
    {
        if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", "PKTXUL", 0))
            save();
        if(PROFILE.isPurgeLog())
            LOGFILE.deleteOnExit();
        return super.destroy();
    }

    public static Logger getLogger()
    {
        return logger;
    }

    private static void sysinfo()
    {
        Set set = System.getProperties().keySet();
        String s;
        for(Iterator iterator = set.iterator(); iterator.hasNext(); logger.info(s + ":" + System.getProperty(s)))
            s = (String)iterator.next();

    }

    public static final String VERSION = "PKTXUL 0.1 (Build 01)";
    public static final String BUILD = "01";
    public static final String EDITION = "(GPL Release) ";
    private static final String PROFILE_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + ".pktxul";
    private static final boolean MACOSX = System.getProperty("mrj.version") != null;
    private static final String PROFILE_NAME = "profile.xml";
    private static final String PROFILE_PATH = PROFILE_DIR + System.getProperty("file.separator") + "profile.xml";
    private static final Logger logger = Logger.getLogger("");
    private static final String TITLE = "PKTXUL - GPL XUL Editor for Thinlet";
    private static final String HOME = "http://pkt.sf.net/pktxul/";
    private static final String TLET = "http://www.thinlet.com";
    private static final String LICENSE = "http://www.gnu.org";
    private static final String THINLET_API = "http://pkt.sf.net/thinlet/doc/api/";
    private static final String THINLET_WIDGETS = "http://pkt.sf.net/thinlet/doc/widget/";
    private static final String XUL = "pxul.xml";
    private static final String XUL_XMLEDITOR = "xmleditor.xml";
    private static final String XUL_LOGVIEWER = "logviewer.xml";
    public static final String WIDGET_PREFIX;


    private PKTXUL()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("PLAF set to:" + UIManager.getSystemLookAndFeelClassName().toString());
        }
        catch(Exception exception)
        {
            logger.info(exception.getMessage());
        }
        try
        {
            add(super.parse("pxul.xml"));
            uiTree = find("DOMTree");
            uiProperties = find("Properties");
            uiStatus = find("lbl_Status");
            menuReopen = find("ReopenMenu");
            menuPurge = find("cbmi_purge");
            settings = new Settings(this, true);
            settings.register((net.sf.pkt.PKTXUL.class).getMethod("applySettings", new Class[0]));
            searchDialog = new SearchDialog(this, true);
            searchDialog.register((net.sf.pkt.PKTXUL.class).getMethod("searchAttr", new Class[0]));
            aboutDialog = new AboutDialog(this, true);
            urlDialog = new URLDialog(this, true);
            urlDialog.register((net.sf.pkt.PKTXUL.class).getMethod("openURL", new Class[0]));
            startupDialog = new StartupDialog(this, false);
            startupDialog.setXUL("http://pkt.sf.net/xul/startup.xml");
            setMethod(find("mi_About"), "action", "show()", this, aboutDialog);
            setMethod(find("mi_find"), "action", "show()", this, searchDialog);
            setMethod(find("mi_OpenURL"), "action", "show()", this, urlDialog);
            setMethod(find("mi_PrjSettings"), "action", "show()", this, settings);
            newDOM();
            setVisible(true);
            settings.setDefaults();
            applySettings();
            try
            {
                startupDialog.show();
            }
            catch(IOException ioexception) { }
        }
        catch(Exception exception1)
        {
            logger.severe(exception1.toString());
            exception1.printStackTrace();
        }
    }

    public void clickSync(String s)
    {
        removeAll(uiTree);
        XTree.populate(this, tdoc.getRootElement(), uiTree, s);
        treeEventHandler();
    }

    private TDocument tdoc;
    private Element _xnode;
    private Element _sel_xnode;
    private Element _lastFind;
    private String _attrName;
    private Object uiTree;
    private Object uiProperties;
    private Object uiStatus;
    private FrameLauncher uiFrame;
    private Object menuReopen;
    private Object menuPurge;

    private void updateTree()
    {
        removeAll(uiTree);
        XTree.populate(this, tdoc.getRootElement(), uiTree, TDocument.getID(_xnode));
        treeEventHandler();
    }

    public void preview()
    {try{
        Object obj;
        setBoolean(find("btn_vw"), "enabled", true);
        obj = find("pnl_Preview");
        removeAll(obj);
        InputStream inputstream;
        inputstream = tdoc.getXml4Preview(settings.getBuildpath());
        add(obj, super.parse(inputstream));
        setString(uiStatus, "text", "OK");
        inputstream.close();
    }catch(Exception e){}}

    public void showAttributes(Element element, Object obj)
    {
        removeAll(obj);
        setString(find("PropertyValue"), "text", "");
        String s = element.getName();
        if(element.getAttribute("name") != null)
        {
            String s1 = element.getAttribute("name").getValue();
            if(s1 != null && 0 < s1.length())
                s = s + "(" + s1 + ")";
        }
        setString(find("CompName"), "text", s);
        setBoolean(find("EditPanel"), "visible", false);
        Iterator iterator = TDocument.getAttributeNames(element);
        do
        {
            if(iterator == null || !iterator.hasNext())
                break;
            Attribute attribute = element.getAttribute((String)iterator.next());
            String s2 = attribute.getName();
            if(!"__XML_NODE_ID".equals(s2))
            {
                Object obj1 = create("row");
                Object obj2 = create("cell");
                Object obj3 = create("cell");
                setString(obj2, "name", "Name");
                setString(obj3, "name", "Value");
                setString(obj2, "text", s2);
                setString(obj3, "text", attribute.getValue());
                add(obj1, obj2);
                add(obj1, obj3);
                add(obj, obj1);
            }
        } while(true);
    }

    public void treeEventHandler()
    {
        _xnode = XTree.getSelectedElement(this, uiTree);
        if(_xnode != null)
            showAttributes(_xnode, uiProperties);
        boolean flag = _xnode != tdoc.getRootElement() && _xnode.getParent() != tdoc.getRootElement();
        boolean flag1 = _xnode.getParent() != null && _xnode != _xnode.getParent().getChildren().get(0);
        boolean flag2 = _xnode.getParent() != null && _xnode != _xnode.getParent().getChildren().get(_xnode.getParent().getChildren().size() - 1);
        setBoolean(find("btn_pull"), "enabled", flag);
        setBoolean(find("btn_Up"), "enabled", flag1);
        setBoolean(find("btn_Dn"), "enabled", flag2);
        Object obj = find("Widgets");
        Object obj1 = find("Toolbar");
        String s = _xnode.getName();
        Iterator iterator = null;
        try
        {
            Element element = (Element)Class.forName(WIDGET_PREFIX + s).newInstance();
            if((net.sf.pkt.widgets.TContainer.class).isAssignableFrom(element.getClass()))
                iterator = ((TContainer)element).getSubTagNames().iterator();
        }
        catch(Exception exception)
        {
            logger.info(".treeEvent(): " + exception.toString());
        }
        if(iterator != null)
        {
            hideToolbarButtons();
            removeAll(obj);
            do
            {
                if(iterator == null || !iterator.hasNext())
                    break;
                String s1 = iterator.next().toString();
                Object obj2 = create("choice");
                Object obj3 = find(obj1, s1);
                setString(obj2, "name", s1);
                setString(obj2, "text", s1);
                add(obj, obj2);
                if(obj3 != null)
                {
                    setBoolean(obj3, "visible", true);
                    setBoolean(obj3, "enabled", true);
                }
            } while(true);
            setString(obj, "text", "Widgets");
            setInteger(obj, "selected", -1);
            setBoolean(obj, "enabled", true);
        } else
        {
            disableToolbarButtons();
            setString(obj, "text", "Widget");
            setInteger(obj, "selected", -1);
            setBoolean(obj, "enabled", false);
        }
    }

    public void tableEventHandler()
    {
        Object obj = find("EditPanel");
        Object obj1 = find("PropertyValue");
        Object obj2 = find("btn_Clr");
        int i = getSelectedIndex(uiProperties);
        _attrName = null;
        if(i != -1)
        {
            Object obj3 = getItem(uiProperties, i);
            String s = getString(getItem(obj3, 0), "text");
            String s1 = getString(getItem(obj3, 1), "text");
            _attrName = getString(getItem(obj3, 0), "text");
            if(obj1 != null)
                remove(obj1);
            if(obj2 != null)
                remove(obj2);
            obj2 = create("button");
            setString(obj2, "text", "Clr");
            setString(obj2, "name", "btn_Clr");
            setMethod(obj2, "action", "clrAttr()", this, this);
            if(Attr.INTERGER_ATTR.contains(_attrName))
            {
                obj1 = create("spinbox");
                setString(obj1, "text", s1);
                setInteger(obj1, "columns", 0);
            } else
            if(Attr.BOOLEAN_ATTR.contains(_attrName))
            {
                obj1 = create("checkbox");
                setBoolean(obj1, "selected", "true".equals(s1));
                setString(obj1, "text", s);
                setChoice(obj1, "alignment", "left");
            } else
            if(Attr.ALIGNMENT_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.ALIGNMENT);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.HORALIGN_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.HORIZONTALS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.VERALIGN_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.VERTICALS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.ORALIGN_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.ORIENTATIONS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.PLACEALIGN_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.PLACEMENTS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.SELECTION_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.SELECTIONS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.SORT_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.SORTINGS);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.TYPE_ATTR.contains(_attrName))
            {
                obj1 = XComboBox.createDropDown(this, Attr.TYPES);
                setInteger(obj1, "weightx", 1);
            } else
            if(Attr.COLOR_ATTR.contains(_attrName))
            {
                obj1 = create("button");
                sColor = s1;
                aTitle = _attrName;
                setString(obj1, "text", "Pick a Color");
                setMethod(obj1, "action", "pickColor()", this, this);
                setInteger(obj1, "weightx", 1);
                add(obj, obj2);
            } else
            if(Attr.FONT_ATTR.contains(_attrName))
            {
                obj1 = create("button");
                sFont = s1;
                aTitle = _attrName;
                setString(obj1, "text", "Pick a Font");
                setMethod(obj1, "action", "pickFont()", this, this);
                setInteger(obj1, "weightx", 1);
                add(obj, obj2);
            } else
            if("icon".equals(_attrName))
            {
                obj1 = create("button");
                sIcon = s1;
                aTitle = _attrName;
                setString(obj1, "text", "Pick an Icon");
                setMethod(obj1, "action", "pickIcon()", this, this);
                setInteger(obj1, "weightx", 1);
                add(obj, obj2);
            } else
            if(Attr.METHOD_ATTR.contains(_attrName))
            {
                if(settings.getThinletSubClass() != null)
                {
                    String as[] = getPublicMethods();
                    if(0 < as.length)
                    {
                        obj1 = XComboBox.createDropDown(this, as);
                        setBoolean(obj1, "editable", true);
                        setString(obj1, "text", s1);
                    } else
                    {
                        obj1 = create("textfield");
                    }
                    setInteger(obj1, "weightx", 1);
                }
            } else
            {
                obj1 = create("textfield");
                setInteger(obj1, "weightx", 1);
                setString(obj1, "text", s1);
            }
            if("selected".equals(_attrName) && Attr.SELECT_INT.contains(_xnode.getName()))
            {
                obj1 = create("spinbox");
                setString(obj1, "text", s1);
                setInteger(obj1, "columns", 0);
            }
            setString(obj1, "name", "PropertyValue");
            add(obj, obj1, 0);
            setBoolean(obj, "visible", true);
            requestFocus(obj1);
        }
    }

    public void clrAttr()
    {
        sIcon = sColor = sFont = "";
        buttonEventHandler();
    }

    public void buttonEventHandler()
    {
        if(_xnode != null && _attrName != null)
        {
            boolean flag = "selected".equals(_attrName) && "combobox".equals(_xnode.getName());
            if(Attr.BOOLEAN_ATTR.contains(_attrName) && !flag)
            {
                boolean flag1 = getBoolean(find("PropertyValue"), "selected");
                _xnode.setAttribute(_attrName, String.valueOf(flag1));
            } else
            if(Attr.INTERGER_ATTR.contains(_attrName) && !flag)
            {
                int i = 0;
                try
                {
                    i = Integer.parseInt(getString(find("PropertyValue"), "text"));
                }
                catch(NumberFormatException numberformatexception)
                {
                    i = 0;
                }
                _xnode.setAttribute(_attrName, String.valueOf(i));
            } else
            if(Attr.COLOR_ATTR.contains(_attrName) && !flag)
                _xnode.setAttribute(_attrName, sColor);
            else
            if(Attr.FONT_ATTR.contains(_attrName) && !flag)
                _xnode.setAttribute(_attrName, sFont);
            else
            if("icon".equals(_attrName) && !flag)
            {
                _xnode.setAttribute(_attrName, sIcon);
            } else
            {
                String s = getString(find("PropertyValue"), "text");
                _xnode.setAttribute(_attrName, s);
            }
            showAttributes(_xnode, uiProperties);
            setBoolean(find("EditPanel"), "visible", false);
            if("name".equals(_attrName))
                updateTree();
            preview();
        }
    }

    public void addComponent(int i)
    {
        addComponent(getString(getItem(find("Widgets"), i), "name"));
    }

    public void addComponent(String s)
    {
        if(_xnode != null)
            try
            {
                Element element = (Element)Class.forName(WIDGET_PREFIX + s).newInstance();
                if(element != null)
                {
                    TDocument.insert(_xnode, element);
                    if((net.sf.pkt.widgets.TContainer.class).isAssignableFrom(element.getClass()))
                        _xnode = element;
                    updateTree();
                    preview();
                }
            }
            catch(Exception exception)
            {
                logger.severe(exception.toString());
            }
    }

    public void moveNode(String s)
    {
        if(_xnode != null && s != null)
        {
            if(s.toUpperCase().endsWith("UP"))
                TDocument.moveUp(_xnode);
            else
                TDocument.moveDn(_xnode);
            updateTree();
            preview();
        }
    }

    public void pullNode()
    {
        if(TDocument.pull(_xnode))
        {
            setString(uiStatus, "text", "Pulled node up in tree.");
            updateTree();
            preview();
        } else
        {
            setString(uiStatus, "text", "Cannot find an available node closer to the root.");
        }
    }

    public void copyNode()
    {
        if(_xnode != null && _xnode != tdoc.getRootElement())
        {
            pasteCopy = true;
            _sel_xnode = _xnode;
            setBoolean(find("btn_pst"), "enabled", true);
        }
    }

    public void cutNode()
    {
        copyNode();
        pasteCopy = false;
    }

    public void pasteNode()
    {
        if(_sel_xnode != null)
        {
            if(pasteCopy)
            {
                if(TDocument.copypaste(_xnode, _sel_xnode))
                    setString(uiStatus, "text", "Copied node pasted into tree.");
                else
                    setString(uiStatus, "text", _sel_xnode.getName() + ": cannot be pasted into " + _xnode.getName());
            } else
            if(TDocument.cutpaste(_xnode, _sel_xnode))
                setString(uiStatus, "text", "Cutted node pasted into tree.");
            else
                setString(uiStatus, "text", _sel_xnode.getName() + ": cannot be pasted into " + _xnode.getName());
            updateTree();
            preview();
        }
    }

    public void removeNode()
    {
        if(_xnode != null)
        {
            _xnode = TDocument.remove(_xnode);
            if(_xnode != null)
                updateTree();
            else
                newDOM();
            preview();
        }
    }

    private Element deepSearch(Element element, String s, boolean flag, boolean flag1)
    {
        Element element1 = null;
        if(_lastFind == null)
        {
            Iterator iterator = element.getAttributes().iterator();
            do
            {
                if(iterator == null || !iterator.hasNext())
                    break;
                Attribute attribute = (Attribute)iterator.next();
                if(flag || "name".equals(attribute.getName()))
                {
                    String s1 = attribute.getValue();
                    if(s1 != null && 0 < s1.length() && (s1.equals(s) || flag1 && (s1.startsWith(s) || s1.endsWith(s))))
                        element1 = element;
                }
            } while(true);
        }
        if(_lastFind != null && _lastFind == element)
            _lastFind = null;
        if(element1 == null)
        {
            for(Iterator iterator1 = element.getChildren().iterator(); element1 == null && iterator1 != null && iterator1.hasNext(); element1 = deepSearch((Element)iterator1.next(), s, flag, flag1));
        }
        return element1;
    }

    public void searchAttr()
    {
        _lastFind = null;
        String s = searchDialog.getSearchString();
        if(s != null && 0 < s.length())
        {
            _lastFind = deepSearch(tdoc.getRootElement(), s, searchDialog.isAll(), searchDialog.isPartial());
            if(_lastFind != null)
            {
                removeAll(uiTree);
                XTree.populate(this, tdoc.getRootElement(), uiTree, TDocument.getID(_lastFind));
                treeEventHandler();
            } else
            {
                setString(uiStatus, "text", "Search-String not found: " + s);
            }
        }
    }

    public void searchAttrAgain()
    {
        String s = searchDialog.getSearchString();
        if(s != null && 0 < s.length())
        {
            _lastFind = deepSearch(tdoc.getRootElement(), s, searchDialog.isAll(), searchDialog.isPartial());
            if(_lastFind != null)
            {
                removeAll(uiTree);
                XTree.populate(this, tdoc.getRootElement(), uiTree, TDocument.getID(_lastFind));
                treeEventHandler();
            } else
            {
                setString(uiStatus, "text", "Search-String not found: " + s);
            }
        }
    }

    public void applySettings()
    {
        Element element = tdoc.getRootElement();
        if(settings.isPanelType() != element.getName().equals("panel"))
        {
            Element element1 = new Element(settings.isPanelType() ? "panel" : "dialog");
            java.util.List list = settings.isPanelType() ? (new panel()).getAttributes() : (new dialog()).getAttributes();
label0:
            for(int i = 0; i < element.getAttributes().size(); i++)
            {
                Attribute attribute = (Attribute)element.getAttributes().get(i);
                int k = 0;
                do
                {
                    if(k >= list.size())
                        continue label0;
                    Attribute attribute1 = (Attribute)list.get(k);
                    if(attribute.getName().equals(attribute1.getName()))
                    {
                        element1.setAttribute((Attribute)attribute.clone());
                        continue label0;
                    }
                    k++;
                } while(true);
            }

            for(int j = 0; j < element.getChildren().size(); j++)
                element1.getChildren().add(((Element)element.getChildren().get(j)).clone());

            tdoc = new TDocument(element1);
            setString(uiStatus, "text", "Main container type set to " + (settings.isPanelType() ? "(panel)" : "(dialog)"));
        }
        _attrName = null;
        _xnode = tdoc.getRootElement();
        _xnode.setAttribute("property", "");
        if(settings.isUTF8_encoded())
            _xnode.setAttribute("property", "encoding=UTF-8;buildpath=" + settings.getBuildpath() + ";cls=" + settings.getThinletSubClass());
        else
            _xnode.setAttribute("property", "encoding=ISO-8859-1;buildpath=" + settings.getBuildpath() + ";cls=" + settings.getThinletSubClass());
        updateTree();
        preview();
    }

    public void setSettings()
    {
        settings.setDefaults();
        Attribute attribute = tdoc.getRootElement().getAttribute("property");
        if(attribute == null)
            attribute = new Attribute("property", "");
        StringTokenizer stringtokenizer = new StringTokenizer(attribute.getValue(), "=;");
        String s = "";
        String s1 = "";
        while(stringtokenizer.hasMoreTokens()) 
        {
            String s2 = stringtokenizer.nextToken();
            if(s.equals("buildpath") && !s2.equals("cls"))
                settings.setBuildpath(s2);
            if(s.equals("cls"))
                settings.setThinletSubClass(s2);
            s = s2;
        }
        settings.setPanelType("panel".equals(tdoc.getRootElement().getName()));
        settings.setUTF8_encoding(0 <= attribute.getValue().indexOf("UTF-8"));
        applySettings();
    }

    public void newDOM()
    {
        if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", "PKTXUL", 0))
            save();
        _attrName = null;
        file = null;
        tdoc = new TDocument(new panel());
        _xnode = tdoc.getRootElement();
        settings.setDefaults();
        applySettings();
        if(uiFrame != null)
            uiFrame.setTitle("PKTXUL - GPL XUL Editor for Thinlet");
    }


    public void hideToolbarButtons()
    {
        Object obj = find("Toolbar");
        Object aobj[] = getItems(obj);
        for(int i = 0; i < aobj.length; i++)
            if(!"separator".equals(getClass(aobj[i])))
                setBoolean(aobj[i], "visible", false);

    }

    public void disableToolbarButtons()
    {
        Object obj = find("Toolbar");
        Object aobj[] = getItems(obj);
        for(int i = 0; i < aobj.length; i++)
            if(!"separator".equals(getClass(aobj[i])))
                setBoolean(aobj[i], "enabled", false);

    }

    public void toogleToolbar(boolean flag)
    {
        setBoolean(find("Toolbar"), "visible", flag);
    }

    public void setPurgeLog(boolean flag)
    {
        PROFILE.setPurgeLog(flag);
    }

    public void open()
    {
        if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", "PKTXUL", 0))
            save();
        try
        {
            File file1 = IO.getFile(uiFrame, "Open", false, file == null ? null : file.getParentFile());
            if(file1 != null)
            {
                FileInputStream fileinputstream = new FileInputStream(file1);
                open(((InputStream) (fileinputstream)));
                fileinputstream.close();
                file = file1;
                setString(uiStatus, "text", "File loaded: " + file.getPath());
                uiFrame.setTitle("PKTXUL, a Thinlet Editor  -   " + file.getPath());
                PROFILE.addFile(file1);
                updateMenu();
            }
        }
        catch(Exception exception)
        {
            logger.severe(exception.getMessage());
            setString(uiStatus, "text", exception.getMessage());
        }
    }

    public void openURL()
    {
        try
        {
            file = null;
            URL url = new URL(urlDialog.geturl());
            urlDialog.close();
            if(url != null)
            {
                if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", "PKTXUL", 0))
                    save();
                InputStream inputstream = url.openConnection().getInputStream();
                open(inputstream);
                inputstream.close();
                setString(uiStatus, "text", "File loaded from URL: " + url.toString());
                uiFrame.setTitle("PKTXUL, a Thinlet Editor  -   " + url.toString());
            }
        }
        catch(Exception exception)
        {
            setString(uiStatus, "text", exception.getMessage());
        }
    }

    private void open(InputStream inputstream)
        throws JDOMException, IOException
    {
        tdoc = new TDocument(inputstream);
        _xnode = tdoc.getRootElement();
        _attrName = null;
        setSettings();
    }

    public void reopen(String s)
    {
        if(tdoc != null && tdoc.getRootElement() != null && 0 < tdoc.getRootElement().getChildren().size() && 0 == JOptionPane.showConfirmDialog(uiFrame, "Do you want to save ?", "PKTXUL", 0))
            save();
        try
        {
            File file1 = new File(s);
            if(file1 != null)
            {
                FileInputStream fileinputstream = new FileInputStream(file1);
                open(fileinputstream);
                fileinputstream.close();
                file = file1;
                setString(uiStatus, "text", "File loaded: " + file.getPath());
                uiFrame.setTitle("PKTXUL, a Thinlet Editor  -   " + file.getPath());
            }
        }
        catch(Exception exception)
        {
            logger.severe(exception.getMessage());
            setString(uiStatus, "text", exception.getMessage());
        }
    }

    public void save()
    {
        if(file != null)
            try
            {
                TDocument.writeXMLfile(tdoc.getRootElement(), file, settings.isUTF8_encoded());
                PROFILE.addFile(file);
                setString(uiStatus, "text", "File saved: " + file.getPath());
                uiFrame.setTitle("PKTXUL, a Thinlet Editor  -   " + file.getPath());
            }
            catch(Exception ioexception)
            {
                logger.severe(ioexception.toString());
                setString(uiStatus, "text", ioexception.getMessage());
                uiFrame.setTitle("PKTXUL, a Thinlet Editor");
            }
        else
            saveas();
    }

    public void saveas()
    {
        File file1 = IO.getFile(uiFrame, "Save", true, file == null ? null : file.getParentFile());
        if(file1 != null)
        {
            file = file1;
            save();
        }
    }

    public void exportNode()
    {
        File file1 = IO.getFile(uiFrame, "Export (Sub-)Tree ", true, file == null ? null : file.getParentFile());
        if(file1 != null)
            try
            {
                TDocument.writeXMLfile(_xnode, file1, settings.isUTF8_encoded());
                setString(uiStatus, "text", _xnode.getName() + " exported: " + file1.getPath());
            }
            catch(IOException ioexception)
            {
                setString(uiStatus, "text", ioexception.getMessage());
            }
    }

    public void importNode()
    {
        boolean flag = false;
        File file1 = IO.getFile(uiFrame, "Import (Sub-)Tree ", false, file == null ? null : file.getParentFile());
        if(file1 != null)
        {
            String s = null;
            String s1 = null;
            try
            {
                Document document = TDocument.readXMLfile(file1);
                s = document.getRootElement().getName();
                if((net.sf.pkt.widgets.TContainer.class).isAssignableFrom(_xnode.getClass()))
                {
                    Iterator iterator = ((TContainer)_xnode).getSubTagNames().iterator();
                    do
                    {
                        if(flag || iterator == null || !iterator.hasNext())
                            break;
                        if(s.equals(iterator.next()))
                        {
                            TDocument.insert(_xnode, document.detachRootElement());
                            flag = true;
                        }
                    } while(true);
                }
            }
            catch(Exception exception)
            {
                logger.info(".importNode(): " + exception.toString());
            }
            if(flag)
            {
                setString(uiStatus, "text", _xnode.getName() + " imported: " + file1.getPath());
                updateTree();
                preview();
            } else
            {
                setString(uiStatus, "text", s + ": cannot be imported into " + s1);
            }
        }
    }

    public void setLogLevel(String s)
    {
        logger.setLevel(Level.parse(s));
        logger.severe("LogLevel set to " + s);
        setString(uiStatus, "text", "Log Level set to " + logger.getLevel().toString());
    }

    public void showLog()
    {
        removeAll(find("pnl_Preview"));
        try
        {
            add(find("pnl_Preview"), super.parse("logviewer.xml"));
        }
        catch(IOException ioexception)
        {
            logger.info(".showLog(): " + ioexception.toString());
        }
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(LOGFILE));
            StringWriter stringwriter = new StringWriter();
            int i;
            while(-1 != (i = bufferedreader.read())) 
                stringwriter.write(i);
            bufferedreader.close();
            setString(find("ta_logviewer"), "text", stringwriter.toString());
        }
        catch(IOException ioexception1)
        {
            logger.info(".showLog(): " + ioexception1.toString());
        }
        disable(getItems(find("Menubar")));
        disable(getItems(find("Toolbar")));
        disable(getItems(find("pnl_Buttons")));
        setBoolean(find("DOMTree"), "enabled", false);
        setBoolean(find("Properties"), "enabled", false);
        setBoolean(find("EditPanel"), "visible", false);
    }

    public void editXML()
    {
        removeAll(find("pnl_Preview"));
        try
        {
            add(find("pnl_Preview"), super.parse("xmleditor.xml"));
        }
        catch(IOException ioexception)
        {
            logger.info(".editXML(): " + ioexception.toString());
        }
        setString(find("ta_xmleditor"), "text", TDocument.getPrettyXml(tdoc, settings.isUTF8_encoded()));
        disable(getItems(find("Menubar")));
        disable(getItems(find("Toolbar")));
        disable(getItems(find("pnl_Buttons")));
        setBoolean(find("Properties"), "enabled", false);
        setBoolean(find("EditPanel"), "visible", false);
        setBoolean(find("DOMTree"), "enabled", false);
    }

    public void xmlChanged()
    {
        setBoolean(find("btn_xmledit_ok"), "enabled", false);
    }

    public void xmlValidate()
    {
        try
        {
            SAXBuilder saxbuilder = new SAXBuilder();
            saxbuilder.build(new StringReader(getString(find("ta_xmleditor"), "text")));
            setString(uiStatus, "text", "OK");
            setBoolean(find("btn_xmledit_ok"), "enabled", true);
            setString(uiStatus, "text", "XML Validated");
        }
        catch(Exception exception)
        {
            setString(uiStatus, "text", "Error: " + exception.toString());
        }
    }

    public void okXML()
    {
        try
        {
            tdoc = new TDocument(getString(find("ta_xmleditor"), "text"));
            cancelXML();
        }
        catch(Exception exception)
        {
            setString(uiStatus, "text", exception.getMessage());
        }
    }

    public void cancelXML()
    {
        enable(getItems(find("Menubar")));
        enable(getItems(find("pnl_Buttons")));
        enable(getItems(find("Toolbar")));
        setBoolean(find("EditPanel"), "visible", true);
        setBoolean(find("Properties"), "enabled", true);
        setBoolean(find("DOMTree"), "enabled", true);
        _attrName = null;
        _xnode = tdoc.getRootElement();
        setSettings();
    }

    public Object pickColor()
    {
        String s = sColor == null ? "" : sColor;
        int i = 0;
        if(s.startsWith("#"))
            i = Integer.parseInt(s.substring(1), 16);
        else
        if(s.startsWith("0x"))
        {
            i = Integer.parseInt(s.substring(2), 16);
        } else
        {
            StringTokenizer stringtokenizer = new StringTokenizer(s, " \r\n\t,");
            if(3 <= stringtokenizer.countTokens())
                i = 0xff000000 | (Integer.parseInt(stringtokenizer.nextToken()) & 0xff) << 16 | (Integer.parseInt(stringtokenizer.nextToken()) & 0xff) << 8 | Integer.parseInt(stringtokenizer.nextToken()) & 0xff;
        }
        Color color = JColorChooser.showDialog(uiFrame, aTitle, new Color(i));
        sColor = color == null ? "" : "" + color.getRed() + "," + color.getGreen() + "," + color.getBlue();
        Object obj = find("PropertyValue");
        setString(obj, "text", sColor);
        return null;
    }

    public Object pickFont()
    {
        String s = sFont == null ? "" : sFont;
        String s1 = null;
        boolean flag = false;
        boolean flag1 = false;
        int i = 0;
        for(StringTokenizer stringtokenizer = new StringTokenizer(s); stringtokenizer.hasMoreTokens();)
        {
            String s2 = stringtokenizer.nextToken();
            if("bold".equalsIgnoreCase(s2))
                flag = true;
            else
            if("italic".equalsIgnoreCase(s2))
                flag1 = true;
            else
                try
                {
                    i = Integer.parseInt(s2);
                }
                catch(NumberFormatException numberformatexception)
                {
                    s1 = s1 != null ? s1 + " " + s2 : s2;
                }
        }

        if(s1 == null)
            s1 = uiFrame.getFont().getName();
        if(i == 0)
            i = uiFrame.getFont().getSize();
        Font font = new Font(s1, (flag ? 1 : 0) | (flag1 ? 2 : 0), i);
        font = FontChooser.showDialog(uiFrame, "Choose Font", font);
        if(font != null)
        {
            sFont = font.getName();
            if(font.isBold())
                sFont = sFont + " bold";
            if(font.isItalic())
                sFont = sFont + " italic";
            sFont = sFont + " " + font.getSize();
        } else
        {
            sFont = "";
        }
        Object obj = find("PropertyValue");
        setString(obj, "text", sFont);
        return null;
    }

    public Object pickIcon()
    {
        JFileChooser jfilechooser = new JFileChooser();jfilechooser.setAccessory(new FindAccessory(jfilechooser));
        Object obj = null;
        Object obj1 = null;
        if(settings.getBuildpath() != null)
        {
            File file1 = new File(settings.getBuildpath());
            if(file1.isDirectory())
                jfilechooser.setCurrentDirectory(file1);
        }
        jfilechooser.setDialogTitle("Choose Icon");
        jfilechooser.setDialogType(0);
        jfilechooser.showDialog(uiFrame, null);
        try
        {
            File file2 = jfilechooser.getSelectedFile();
            if(file2 != null)
            {
                StringBuffer stringbuffer = new StringBuffer(file2.getPath());
                for(int i = 0; 0 <= (i = stringbuffer.indexOf("\\"));)
                    stringbuffer.setCharAt(i, '/');

                sIcon = stringbuffer.toString();
                if(sIcon.startsWith(settings.getBuildpath()))
                    sIcon = "/" + sIcon.substring(settings.getBuildpath().length());
                Object obj3 = find("PropertyValue");
                setString(obj3, "text", sIcon);
            } else
            {
                sIcon = "";
            }
        }
        catch(Exception exception)
        {
            sIcon = "";
            getLogger().warning(".pickIcon(): " + exception.toString());
        }
        Object obj2 = find("PropertyValue");
        setString(obj2, "text", sIcon);
        return null;
    }

    private String[] getPublicMethods()
    {
        String as[] = new String[0];
        if(settings.getThinletSubClass() != null)
            try
            {
                Vector vector = new Vector();
                URL aurl[] = {
                    new URL("file://///" + settings.getBuildpath())
                };
                URLClassLoader urlclassloader = new URLClassLoader(aurl);
                Method amethod[] = new Method[0];
                Class class1 = urlclassloader.loadClass(settings.getThinletSubClass());
                try
                {
                    amethod = class1.getMethods();
                }
                catch(Error error)
                {
                    logger.severe(error.toString());
                    setString(uiStatus, "text", "Error during Introspection: " + error);
                }
                for(int i = 0; i < amethod.length; i++)
                {
                    if(!Modifier.isPublic(amethod[i].getModifiers()))
                        continue;
                    StringBuffer stringbuffer;
                    int k;
                    try
                    {
                        (thinlet.Thinlet.class).getMethod(amethod[i].getName(), amethod[i].getParameterTypes());
                        continue;
                    }
                    catch(Exception exception)
                    {
                        stringbuffer = new StringBuffer(amethod[i].getName());
                        k = 0;
                    }
                    for(; k < amethod[i].getParameterTypes().length; k++)
                    {
                        stringbuffer.append(0 != k ? ',' : '(');
                        String s = amethod[i].getParameterTypes()[k].getName();
                        stringbuffer.append(s.substring(s.lastIndexOf('.') + 1));
                        if(k + 1 == amethod[i].getParameterTypes().length)
                            stringbuffer.append(")");
                    }

                    vector.addElement(stringbuffer.toString());
                }

                as = new String[vector.size()];
                for(int j = 0; j < vector.size(); j++)
                    as[j] = vector.get(j).toString();

                Arrays.sort(as);
            }
            catch(ClassNotFoundException classnotfoundexception)
            {
                setString(uiStatus, "text", "Implementation class could not be found. " + classnotfoundexception.toString());
            }
            catch(SecurityException securityexception)
            {
                setString(uiStatus, "text", "SecurityException: " + securityexception.toString());
            }
            catch(MalformedURLException malformedurlexception)
            {
                System.err.println(malformedurlexception.getMessage());
            }
        return as;
    }

    static 
    {
        WIDGET_PREFIX = Attr.PackageName + ".";
    }
}
