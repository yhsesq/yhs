package net.sf.pkt.clock;
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

import java.applet.Applet;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.Toolkit;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public class worldclock extends Applet
    implements Runnable
{

    public String trailSpace(String s, int i)
    {
        for(; s.length() < i; s = s + " ");
        return s;
    }

    public void stop()
    {
        if(m_worldclock != null)
        {
            m_worldclock.stop();
            m_worldclock = null;
        }
    }


    public boolean isDst(Date date)
    {
        boolean flag = false;
        int i = date.getMonth();
        if(i > 2 && i < 10)
        {
            int j = date.getDate() - 1;
            if(i == 3)
            {
                int k = (new Date(date.getYear(), 3, 1)).getDay();
                flag = j + k > 6;
            } else
            if(i == 9)
            {
                int l = 6 - (new Date(date.getYear(), 9, 31)).getDay();
                flag = 30 - (j - l) > 6;
            } else
            {
                flag = true;
            }
        }
        return flag;
    }

    void msg(String s)
    {
        //System.out.println(s);
    }

    void msg(double d)
    {
        Double double1 = new Double(d);
        //System.out.println(double1.toString());
    }

    void msg(int i)
    {
        Integer integer = new Integer(i);
        //System.out.println(integer.toString());
    }

    public String makeDigitTimeString(Date date)
    {
        String s = "";
        String as[] = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
        };
        String as1[] = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", 
            "Nov", "Dec"
        };
        s = as[date.getDay()];
        s += " " + as1[date.getMonth()];
        s += " " + leadChar(date.getDate(), " ", 2);
        String s1 = "";
        int i = date.getHours();
        if(ampmMode)
        {
            s1 = i < 12 ? " AM" : " PM";
            i %= 12;
            i = i != 0 ? i : 12;
        }
        s += "  " + leadChar(i, " ", 2);
        s += ":" + leadChar(date.getMinutes(), "0", 2);
        s += ":" + leadChar(date.getSeconds(), "0", 2);
        s += ampmMode ? s1 : "";
        return s;
    }

    String getFontName(String s)
    {
        String s1 = "";
        String as[] = Toolkit.getDefaultToolkit().getFontList();
        for(int i = 0; i < as.length; i++)
            if(as[i].equalsIgnoreCase(s))
                s1 = as[i];

        return s1;
    }

    void editOptionList(String as[])
    {
        for(int j = 0; j < as.length; j++)
        {
            int i = as[j].indexOf("=");
            if(i > 0)
            {
                String s = as[j].substring(0, i).toLowerCase();
                String s1 = as[j].substring(i + 1).toLowerCase();
                if(!s.equals("noclicks") || atHome)
                    optionList.put(s, s1);
            }
        }

    }

    void editOptionList()
    {
        for(int i = 0; i < defaultList.length; i++)
        {
            String s = null;
            if(s != null && (!s.equals("noclicks") || atHome))
                optionList.put(defaultList[i][0].toLowerCase(), s);
        }

    }

    public void makeTzChoice()
    {
        tzChoice = new java.awt.List(3, false);
        int j = 0;
        do
        {
            int i = j - 11;
            String s = "GMT" + (i >= 0 ? "+" : "-") + leadChar((int)Math.abs(i), "0", 2);
            tzChoice.addItem(s);
        } while(++j < 24);
        add(tzChoice);
        setTzChoice();
    }

    public void update(Graphics g)
    {
        myDraw(g);
    }

    public void start()
    {
        if(m_worldclock == null)
        {
            m_worldclock = new Thread(this);
            m_worldclock.start();
        }
    }

    public String getAppletInfo()
    {
        return "PKT";
    }

    void readOptionList()
    {
        for(Enumeration enumeration = optionList.keys(); enumeration.hasMoreElements();)
        {
            String s = enumeration.nextElement().toString().toLowerCase();
            try
            {
                if(s.equals("menucolor"))
                    menuColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("menutextcolor"))
                    menuTextColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("charttextcolor"))
                    chartTextColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("titlecolor"))
                    titleColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("daycolor"))
                    dayColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("nightcolor"))
                    nightColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("homecolor"))
                    homeZoneColor = getColor(optionList.get(s).toString());
                else
                if(s.equals("ampm"))
                    ampmMode = optionList.get(s).toString().equals("true");
                else
                if(s.equals("dst"))
                {
                    String s1 = optionList.get(s).toString();
                    if(!s1.equals("auto"))
                        dstMode = s1.equals("true");
                } else
                if(s.equals("oneclick"))
                    oneClick = optionList.get(s).toString();
                else
                if(s.equals("twoclicks"))
                    twoClicks = optionList.get(s).toString();
                else
                if(s.equals("timezone"))
                {
                    String s2 = optionList.get(s).toString();
                    if(!s2.equals("system"))
                        localTimeZone = Double.valueOf(s2).doubleValue();
                }
            }
            catch(NumberFormatException numberformatexception)
            {
                PrintError(numberformatexception.toString());
            }
        }

    }

    String timeString(Date date, double d)
    {
        String s;
        if(d == -12D)
        {
            s = "   Zone       Center         Place                       Date        Time";
        } else
        {
            s = "GMT" + (d < 0.0D ? "-" : "+") + leadChar((int)Math.abs(d), "0", 2) + "   ";
            s += "Lng. " + leadChar((int)Math.abs(d * 15D), "0", 3) + (d > 0.0D ? "E" : "W") + "   ";
            s += trailSpace(placeNames[(int)d + 11], 16) + "  ";
            s += makeDigitTimeString(date);
            if(dstMode)
                s += " (DT)";
        }
        return s;
    }

    public void myDraw(Graphics g)
    { if( ibuff != null ){
        testRender(ibuff);
        g.drawImage(applet, 0, yDiv, this);}
    }

    void testRender(Graphics g)
    {try{
        theTime = new Date();
        if(theTime.getTime() > oldTime.getTime() || forceNewDraw)
        {
            forceNewDraw = false;
            int j = iHeight - yDiv;
            int k = iWidth;
            int j1 = j / 35;
            double d = 0.040000000000000001D;
            double d1 = (double)j / 80D;
            gmtTime = new Date();
            gmtTime.setTime((long)((double)theTime.getTime() - localTimeZone * 3600000D));
            g.setColor(menuColor);
            g.fillRect(0, 0, iWidth, yDiv);
            double d2 = k / 32;
            double d3 = j;
            for(double d4 = -12D; d4 <= 12D; d4++)
            {
                tzTime = new Date();
                tzTime.setTime((long)((double)gmtTime.getTime() + d4 * 3600000D));
                if(dstMode){
                    tzTime.setTime((long)((double)tzTime.getTime() + 3600000D));}
                int l = (int)(d3 + d3 * (d4 - 12D) * d);
                int i1 = (int)(d3 + d3 * (d4 - 13D) * d);
                int i = tzTime.getHours();
                double d5 = localTimeZone - (double)(dstMode ? 1 : 0);
                if(d4 == -12D){
                    g.setColor(titleColor);}
                else
                if(d4 == d5){
                    g.setColor(homeZoneColor);}
                else
                if(i == 6 || i == 18){
                    g.setColor(blendColor);}
                else{
                    g.setColor(i <= 6 || i >= 18 ? nightColor : dayColor);
		    }
                g.fillRect(0, i1, k, l - i1);
                g.setColor(chartTextColor);
                g.setFont(new Font("Serif", 0, j1));
                g.drawString(timeString(tzTime, d4), (int)d2, (int)((double)l - d1));
            }

        }
        oldTime = theTime;
    }catch(Exception e){PrintError("Err:"+e.toString());}}

    public void setTzChoice()
    {
        int i = (int)(localTimeZone + 11D);
        if(dstMode)
            i--;
        tzChoice.select(i);
        tzChoice.makeVisible(i - 1);
    }

    public worldclock()
    {
        AppName = "PKTClock";
        atHome = true;
        defaultHeight = 500;
        defaultWidth = 600;
        menuTextColor = Color.black;
        chartTextColor = Color.black;
        menuColor = Color.white;
        titleColor = Color.magenta;
        dayColor = Color.yellow;
        nightColor = Color.gray;
        homeZoneColor = Color.red;
        ampmMode = true;
        colorValues = (new Color[] {
            Color.black, Color.blue, Color.cyan, Color.darkGray, Color.gray, Color.green, Color.lightGray, Color.magenta, Color.orange, Color.pink, 
            Color.red, Color.white, Color.yellow
        });
        defaultList = (new String[][] {
            new String[] {
                "TimeZone", "system", "Sets special time zone: -12 to 12 hours inc. half-hours"
            }, new String[] {
                "menuColor", "gray", "Sets color of background at top"
            }, new String[] {
                "TitleColor", "green", "Sets color of chart title (first line)"
            }, new String[] {
                "menuTextColor", "blue", "Sets color of menu text"
            }, new String[] {
                "chartTextColor", "black", "Sets color of chart text"
            }, new String[] {
                "dayColor", "yellow", "Sets color of chart's daytime part"
            }, new String[] {
                "nightColor", "lightgray", "Sets color of chart's nighttime part"
            }, new String[] {
                "homeColor", "red", "Sets color of user's home time zone"
            }, new String[] {
                "ampm", "false", "If false, show 24 hour digital time display"
            }, new String[] {
                "dst", "auto", "True, force daylight time. False, force standard. Otherwise calculate"
            }, new String[] {
                "noclicks", AppName + " ", " "
            }, new String[] {
                "oneclick", AppName + " ", " "
            }, new String[] {
                "twoclicks", " ", " "
            }
        });
    }

    public void paint(Graphics g)
    {try{
        myDraw(g);
    }catch(Exception e){PrintError(e.toString());}}

    public void wait(int i)
    {
        try
        {
            Thread.sleep(i);
            return;
        }
        catch(InterruptedException _ex)
        {
            return;
        }
    }

    Color getColor(String s)
    {
        if(colorList == null)
        {
            colorList = new Hashtable();
            for(int i = 0; i < colorNames.length; i++)
                colorList.put(colorNames[i], colorValues[i]);

        }
        s = s.toLowerCase();
        try
        {
            if(s.substring(0, 1).equals("#"))
                s = s.substring(1);
            int j = Integer.valueOf(s, 16).intValue();
            return new Color(j);
        }
        catch(NumberFormatException _ex) { }
        if(colorList.containsKey(s))
            return (Color)colorList.get(s);
        else
            return Color.black;
    }

    void PrintError(String s)
    {
        //System.out.println(s);
    }

    public String leadChar(String s, String s1, int i)
    {
        for(; s.length() < i; s = s1 + s);
        return s;
    }

    public String leadChar(int i, String s, int j)
    {
        String s1;
        for(s1 = (new Integer(i)).toString(); s1.length() < j; s1 = s + s1);
        return s1;
    }

    void appTestSize()
    {
        try
        {
            if(applet == null || iWidth != size().width || iHeight != size().height)
            {
                iWidth = 500; // size().width;
                iHeight = 600; // size().height;
                yDiv = 75;
                applet = createImage(iWidth, iHeight - yDiv);
                ibuff = applet.getGraphics();
                return;
            }
        }
        catch(Exception illegalargumentexception)
        {
            PrintError(illegalargumentexception.toString());
        }
    }

    void createOptionList()
    {
        optionList = new Hashtable();
        for(int i = 0; i < defaultList.length; i++)
            optionList.put(defaultList[i][0].toLowerCase(), defaultList[i][1]);

    }

    void setupControls()
    {
        int i = iWidth / 35;
        i = i <= 14 ? i : 14;
        setFont(new Font("TimesRoman", 1, i));
        setForeground(menuTextColor);
        setBackground(menuColor);
        add(ampmClockY);
        add(ampmClockN);
        add(dstTimeY);
        add(dstTimeN);
        makeTzChoice();
    }

    public boolean action(Event event, Object obj)
    {
        boolean flag = false;
        Object obj1 = event.target;
        if(obj1 instanceof Checkbox)
        {
            Checkbox checkbox = (Checkbox)obj1;
            String s = checkbox.getLabel();
            if(s.equals("24 Hr"))
            {
                ampmMode = false;
                flag = true;
            } else
            if(s.equals("AM/PM"))
            {
                ampmMode = true;
                flag = true;
            } else
            if(s.equals("Daylight"))
            {
                dstMode = true;
                setTzChoice();
                flag = true;
            } else
            if(s.equals("Standard"))
            {
                dstMode = false;
                setTzChoice();
                flag = true;
            }
        } else
        if(obj1 instanceof java.awt.List)
        {
            java.awt.List list = (java.awt.List)obj1;
            int i = 11;
            if(dstMode)
                i--;
            localTimeZone = list.getSelectedIndex() - i;
            flag = true;
        }
        if(flag)
        {
            forceNewDraw = true;
            repaint();
        }
        return flag;
    }

    public void run()
    {
        blendColor = new Color((dayColor.getRed() + nightColor.getRed()) / 2, (dayColor.getGreen() + nightColor.getGreen()) / 2, (dayColor.getBlue() + nightColor.getBlue()) / 2);
        do
            try
            {
                appTestSize();
                repaint();
                wait(999);
            }
            catch(Exception _ex)
            {
                stop();this.destroy();
            }
        while(true);
    }

    public void init()
    {
        resize(defaultWidth, defaultHeight);
        if(!m_fStandAlone)
        {
            dstMode = isDst(new Date());
            localTimeZone = -(double)(new Date()).getTimezoneOffset() / 60D;
            createOptionList();
            editOptionList();
            readOptionList();
        }
        ampmClock = new CheckboxGroup();
        ampmClockY = new Checkbox("AM/PM", ampmClock, false);
        ampmClockN = new Checkbox("24 Hr", ampmClock, true);
        dstTime = new CheckboxGroup();
        dstTimeY = new Checkbox("Daylight", dstTime, dstMode);
        dstTimeN = new Checkbox("Standard", dstTime, !dstMode);
        oldTime = new Date("1/1/72");
        appTestSize();
        setupControls();
    }

public void destroy(){stop();this.destroy();}

    public String trailSpace(int i, int j)
    {
        String s;
        for(s = (new Integer(i)).toString(); s.length() < j; s = s + " ");
        return s;
    }

    private static Thread m_worldclock;
    String AppName;
    String docBase;
    boolean atHome;
    boolean m_fStandAlone;
    boolean forceNewDraw;
    static Image applet;
    static Graphics ibuff;
    String noClicks;
    String oneClick;
    String twoClicks;
    int defaultHeight;
    int defaultWidth;
    int iWidth;
    int iHeight;
    int yDiv;
    double localTimeZone;
    Date theTime;
    Date oldTime;
    Date gmtTime;
    Date tzTime;
    CheckboxGroup dstTime;
    Checkbox dstTimeY;
    Checkbox dstTimeN;
    CheckboxGroup ampmClock;
    Checkbox ampmClockY;
    Checkbox ampmClockN;
    Color menuTextColor;
    Color chartTextColor;
    Color menuColor;
    Color titleColor;
    Color dayColor;
    Color nightColor;
    Color blendColor;
    Color homeZoneColor;
    java.awt.List tzChoice;
    public final double hoursToMilliseconds = 3600000D;
    boolean ampmMode;
    boolean dstMode;
    String placeNames[] = {
        "Samoa", "Hawaii", "Juneau", "San Francisco", "Denver", "Chicago", "New York (NY)", "Caracas", "Rio De Janeiro", "Recife", 
        "Azores", "London", "Paris", "Cairo", "Moscow", "Baku", "Karachi", "Dacca", "Bangkok", "Hong Kong", 
        "Tokyo", "Sydney", "Noumea", "Wellington"
    };
    String colorNames[] = {
        "black", "blue", "cyan", "darkgray", "gray", "green", "lightgray", "magenta", "orange", "pink", 
        "red", "white", "yellow"
    };
    Color colorValues[];
    Hashtable colorList;
    String defaultList[][];
    Hashtable optionList;
}
