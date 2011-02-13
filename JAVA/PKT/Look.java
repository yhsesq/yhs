package net.sf.pkt.calc;
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

import java.awt.Color;
import java.awt.Font;

public class Look
{

    public Look()
    {
    }

    public static Color numberKeyBG()
    {
        return new Color(232, 222, 146);
    }

    public static Color numberKeyFG()
    {
        return new Color(48, 35, 0);
    }

    public static Color funcKeyBG()
    {
        return new Color(73, 37, 22);
    }

    public static Color funcKeyFG()
    {
        return new Color(250, 255, 206);
    }

    public static Color operatorKeyBG()
    {
        return new Color(202, 211, 184);
    }

    public static Color operatorKeyFG()
    {
        return new Color(48, 35, 0);
    }

    public static Font ButtonFont()
    {
        return new Font("Dialog", 1, 14);
    }

    public static Color DisplayBG()
    {
        return new Color(255, 255, 255);
    }

    public static Color DisplayFG()
    {
        return new Color(0, 0, 0);
    }

    public static Font DisplayFont()
    {
        return new Font("Dialog", 1, 16);
    }

    public static Color ChooserFG()
    {
        return new Color(250, 255, 206);
    }

    public static Color ChooserBG()
    {
        return new Color(20, 20, 20);
    }

    public static Color BackgroundColor()
    {
        return new Color(20, 20, 20);
    }
}
