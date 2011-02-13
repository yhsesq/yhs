// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.widgets;

import java.util.Vector;

public final class Attr
{

    private Attr()
    {
    }

    static Class _mthclass$(String s) throws Exception
    {
        return Class.forName(s);
//        ClassNotFoundException classnotfoundexception;
//        throw new NoClassDefFoundError(classnotfoundexception.getMessage());
    }

    public static final String PackageName;
    public static final String ALIGNMENT[] = {
        "center", "left", "right"
    };
    public static final String HORIZONTALS[] = {
        "fill", "center", "left", "right"
    };
    public static final String VERTICALS[] = {
        "fill", "center", "top", "bottom"
    };
    public static final String PLACEMENTS[] = {
        "top", "left", "bottom", "right"
    };
    public static final String ORIENTATIONS[] = {
        "horizontal", "vertical"
    };
    public static final String TYPES[] = {
        "normal", "default", "cancel", "link"
    };
    public static final String SORTINGS[] = {
        "", "ascent", "descent"
    };
    public static final String SELECTIONS[] = {
        "single", "interval", "multiple"
    };
    public static final Vector SELECT_INT;
    public static final Vector ALIGNMENT_ATTR;
    public static final Vector BOOLEAN_ATTR;
    public static final Vector COLOR_ATTR;
    public static final Vector FONT_ATTR;
    public static final Vector HORALIGN_ATTR;
    public static final Vector INTERGER_ATTR;
    public static final Vector METHOD_ATTR;
    public static final Vector ORALIGN_ATTR;
    public static final Vector PLACEALIGN_ATTR;
    public static final Vector SELECTION_ATTR;
    public static final Vector SORT_ATTR;
    public static final Vector TYPE_ATTR;
    public static final Vector VERALIGN_ATTR;
    public static final String ACCELERATOR = "accelerator";
    public static final String ACTION = "action";
    public static final String ALIGN = "alignment";
    public static final String ANGLE = "angle";
    public static final String BACKGROUND = "background";
    public static final String BLOCK = "block";
    public static final String BORDER = "border";
    public static final String BOTTOM = "bottom";
    public static final String CANCEL = "cancel";
    public static final String CARET = "caret";
    public static final String CLOSABLE = "closable";
    public static final String COLLAPSE = "collapse";
    public static final String COLUMNS = "columns";
    public static final String COLSPAN = "colspan";
    public static final String DEFAULT = "default";
    public static final String DIVIDER = "divider";
    public static final String EDITABLE = "editable";
    public static final String ENABLED = "enabled";
    public static final String END = "end";
    public static final String EXPANDED = "expanded";
    public static final String FOCUSLOST = "focuslost";
    public static final String FOCUSGAINED = "focusgained";
    public static final String FONT = "font";
    public static final String FOR = "for";
    public static final String FOREGROUND = "foreground";
    public static final String GAP = "gap";
    public static final String GROUP = "group";
    public static final String HALIGN = "halign";
    public static final String HEIGHT = "height";
    public static final String INIT = "init";
    public static final String INSERT = "insert";
    public static final String ICON = "icon";
    public static final String ICONIFIABLE = "iconifiable";
    public static final String I18N = "i18n";
    public static final String LEFT = "left";
    public static final String LINE = "line";
    public static final String LINK = "link";
    public static final String MINIMUM = "minimum";
    public static final String MAXIMIZABLE = "maximizable";
    public static final String MAXIMUM = "maximum";
    public static final String MENUSSHOWN = "menushown";
    public static final String MODAL = "modal";
    public static final String MONIC = "mnemonic";
    public static final String NAME = "name";
    public static final String NORMAL = "normal";
    public static final String ORIENTATION = "orientation";
    public static final String PERFORM = "perform";
    public static final String PLACEMENT = "placement";
    public static final String PROPERTY = "property";
    public static final String REMOVE = "remove";
    public static final String RESIZABLE = "resizable";
    public static final String RIGHT = "right";
    public static final String ROWS = "rows";
    public static final String ROWSPAN = "rowspan";
    public static final String SCROLLABLE = "scrollable";
    public static final String SELECTED = "selected";
    public static final String SELECTION = "selection";
    public static final String SORT = "sort";
    public static final String START = "start";
    public static final String STEP = "step";
    public static final String TEXT = "text";
    public static final String TOOLTIP = "tooltip";
    public static final String TOP = "top";
    public static final String TYPE = "type";
    public static final String UNIT = "unit";
    public static final String WIDTH = "width";
    public static final String VALUE = "value";
    public static final String VALIGN = "valign";
    public static final String VISIBLE = "visible";
    public static final String WEIGHTX = "weightx";
    public static final String WEIGHTY = "weighty";
    public static final String WRAP = "wrap";

    static 
    {
        PackageName = (net.sf.pkt.widgets.Component.class).getPackage().getName();
        SELECT_INT = new Vector();
        ALIGNMENT_ATTR = new Vector();
        BOOLEAN_ATTR = new Vector();
        COLOR_ATTR = new Vector();
        FONT_ATTR = new Vector();
        HORALIGN_ATTR = new Vector();
        INTERGER_ATTR = new Vector();
        METHOD_ATTR = new Vector();
        ORALIGN_ATTR = new Vector();
        PLACEALIGN_ATTR = new Vector();
        SELECTION_ATTR = new Vector();
        SORT_ATTR = new Vector();
        TYPE_ATTR = new Vector();
        VERALIGN_ATTR = new Vector();
        BOOLEAN_ATTR.addElement("angle");
        BOOLEAN_ATTR.addElement("border");
        BOOLEAN_ATTR.addElement("closable");
        BOOLEAN_ATTR.addElement("enabled");
        BOOLEAN_ATTR.addElement("editable");
        BOOLEAN_ATTR.addElement("expanded");
        BOOLEAN_ATTR.addElement("iconifiable");
        BOOLEAN_ATTR.addElement("i18n");
        BOOLEAN_ATTR.addElement("line");
        BOOLEAN_ATTR.addElement("maximizable");
        BOOLEAN_ATTR.addElement("modal");
        BOOLEAN_ATTR.addElement("resizable");
        BOOLEAN_ATTR.addElement("scrollable");
        BOOLEAN_ATTR.addElement("selected");
        BOOLEAN_ATTR.addElement("visible");
        BOOLEAN_ATTR.addElement("wrap");
        COLOR_ATTR.addElement("foreground");
        COLOR_ATTR.addElement("background");
        INTERGER_ATTR.addElement("block");
        INTERGER_ATTR.addElement("bottom");
        INTERGER_ATTR.addElement("columns");
        INTERGER_ATTR.addElement("colspan");
        INTERGER_ATTR.addElement("divider");
        INTERGER_ATTR.addElement("end");
        INTERGER_ATTR.addElement("gap");
        INTERGER_ATTR.addElement("height");
        INTERGER_ATTR.addElement("left");
        INTERGER_ATTR.addElement("maximum");
        INTERGER_ATTR.addElement("minimum");
        INTERGER_ATTR.addElement("mnemonic");
        INTERGER_ATTR.addElement("right");
        INTERGER_ATTR.addElement("rows");
        INTERGER_ATTR.addElement("rowspan");
        INTERGER_ATTR.addElement("start");
        INTERGER_ATTR.addElement("top");
        INTERGER_ATTR.addElement("unit");
        INTERGER_ATTR.addElement("value");
        INTERGER_ATTR.addElement("width");
        INTERGER_ATTR.addElement("weightx");
        INTERGER_ATTR.addElement("weighty");
        ALIGNMENT_ATTR.addElement("alignment");
        FONT_ATTR.addElement("font");
        HORALIGN_ATTR.addElement("halign");
        METHOD_ATTR.addElement("action");
        METHOD_ATTR.addElement("caret");
        METHOD_ATTR.addElement("collapse");
        METHOD_ATTR.addElement("expanded");
        METHOD_ATTR.addElement("focuslost");
        METHOD_ATTR.addElement("focusgained");
        METHOD_ATTR.addElement("init");
        METHOD_ATTR.addElement("insert");
        METHOD_ATTR.addElement("menushown");
        METHOD_ATTR.addElement("perform");
        METHOD_ATTR.addElement("remove");
        VERALIGN_ATTR.addElement("valign");
        ORALIGN_ATTR.addElement("orientation");
        PLACEALIGN_ATTR.addElement("placement");
        SELECTION_ATTR.addElement("selection");
        SORT_ATTR.addElement("sort");
        TYPE_ATTR.addElement("type");
        SELECT_INT.addElement("combobox");
        SELECT_INT.addElement("tabbedpane");
    }
}
