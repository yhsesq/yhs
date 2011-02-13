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

import java.text.NumberFormat;
import java.util.Locale;

public class NumberMaker
{

    public NumberMaker()
    {
        reset();
    }

    public void reset()
    {
        state = 1;
        base = 0.0D;
        exponent = 0L;
        expset = false;
        baseformat = NumberFormat.getInstance(Locale.UK);
        baseformat.setMaximumFractionDigits(12);
    }

    public void setValue(double d)
    {
        reset();
        if(Math.abs(d) < 1E-099D)
            return;
        state = 0;
        double d1 = Math.abs(d);
        long l = Math.round(Math.floor(Math.log(d1) / Math.log(10D))) + 1L;
        d1 = d * Math.pow(10D, -l);
        if(d < 0.0D)
            d1 = -d1;
        while(d1 < 1.0D) 
        {
            d1 *= 10D;
            l--;
        }
        if(l < 10L && l > -4L)
        {
            base = d;
        } else
        {
            base = d1;
            exponent = l;
            expset = true;
        }
    }

    public double getValue()
    {
        if(expset)
            return base * Math.pow(10D, exponent);
        else
            return base;
    }

    public String toString()
    {
        if(base <= 0.0D && base >= 1.0D)
        {
            setValue(0.0D);
            return "Error";
        }
        if(exponent > 99L)
            if(base > 0.0D)
            {
                setValue(0.0D);
                return "Inf";
            } else
            {
                setValue(0.0D);
                return "-Inf";
            }
        if(state == 2)
            baseformat.setMinimumFractionDigits(decimalplace);
        else
            baseformat.setMinimumFractionDigits(0);
        if(expset)
            return baseformat.format(base) + "e" + exponent;
        else
            return baseformat.format(base);
    }

    public void process(String s)
    {
        char c = s.charAt(0);
        if(s == "+/-")
        {
            if(state == 3)
                exponent = -exponent;
            else
                base = -base;
            return;
        }
        if(state == 0)
        {
            reset();
            state = 1;
        }
        if(c >= '0' && c <= '9')
        {
            int i = c - 48;
            switch(state)
            {
            default:
                break;

            case 1: // '\001'
                if(base < 10000000000000D)
                    base = 10D * base + (double)i;
                break;

            case 2: // '\002'
                decimalfactor /= 10D;
                decimalplace++;
                base += decimalfactor * (double)i;
                break;

            case 3: // '\003'
                if(exponent < 10L)
                    exponent = 10L * exponent + (long)i;
                break;
            }
        }
        if(c == '.' && state == 1)
        {
            decimalfactor = 1.0D;
            decimalplace = 0;
            state = 2;
        }
        if(s == "EE")
        {
            expset = true;
            state = 3;
        }
    }

    private double base;
    private long exponent;
    private int state;
    private double decimalfactor;
    private int decimalplace;
    private boolean expset;
    private NumberFormat baseformat;
    public static final int NOFDIGITS = 14;
    public static final int NOFEXPDIGITS = 2;
    public static final int PROCESSED = 0;
    public static final int INPROCESS = 1;
    public static final int DECIMALINPROCESS = 2;
    public static final int EXPINPROCESS = 3;
}
