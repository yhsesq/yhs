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

public class CalcEngine
{

    public CalcEngine()
    {
        stackval = 0.0D;
        operator = 0;
        trigoption = 1;
    }

    private double reset()
    {
        stackval = 0.0D;
        operator = 0;
        return 0.0D;
    }

    private double binaryOp(double d, int i)
    {
        if(operator != 0)
            d = evaluate(d);
        operator = i;
        stackval = d;
        return d;
    }

    private double evaluate(double d)
    {
        double d1 = d;
        switch(operator)
        {
        case 1: // '\001'
            d1 = stackval + d;
            break;

        case 2: // '\002'
            d1 = stackval - d;
            break;

        case 3: // '\003'
            d1 = stackval * d;
            break;

        case 4: // '\004'
            d1 = stackval / d;
            break;

        case 5: // '\005'
            d1 = Math.pow(stackval, d);
            break;
        }
        operator = 0;
        stackval = 0.0D;
        return d1;
    }

    private double fakult(double d)
    {
        if(d < 0.0D)
            return Math.sqrt(-1D);
        long l = Math.round(Math.floor(d));
        double d1 = 1.0D;
        for(long l1 = 1L; l1 < l && d1 < 1E+110D; d1 *= l1)
            l1++;

        return d1;
    }

    public double process(double d, String s)
    {
        if(s == "+")
            return binaryOp(d, 1);
        if(s == "-")
            return binaryOp(d, 2);
        if(s == "*")
            return binaryOp(d, 3);
        if(s == "/")
            return binaryOp(d, 4);
        if(s == "x^y")
            return binaryOp(d, 5);
        double d1;
        if(trigoption == 0)
            d1 = 0.017453292519943295D;
        else
            d1 = 1.0D;
        if(s == "e")
            return Math.exp(d);
        if(s == "ln")
            return Math.log(d);
        if(s == "log")
            return Math.log(d) / Math.log(10D);
        if(s == "sin")
            return Math.sin(d1 * d);
        if(s == "cos")
            return Math.cos(d1 * d);
        if(s == "tan")
            return Math.tan(d1 * d);
        if(s == "asin")
            return Math.asin(d) / d1;
        if(s == "acos")
            return Math.acos(d) / d1;
        if(s == "atan")
            return Math.atan(d) / d1;
        if(s == "sinh")
            return 0.5D * (Math.exp(d) - Math.exp(-d));
        if(s == "cosh")
            return 0.5D * (Math.exp(d) + Math.exp(-d));
        if(s == "sqrt")
            return Math.sqrt(d);
        if(s == "x^2")
            return d * d;
        if(s == "1/x")
            return 1.0D / d;
        if(s == "2^x")
            return Math.pow(2D, d);
        if(s == "x!")
            return fakult(d);
        if(s == "C")
            return 0.0D;
        if(s == "CE")
            return reset();
        if(s == "pi")
            return 3.1415926535897931D;
        if(s == "chooseDeg")
        {
            trigoption = 0;
            return d;
        }
        if(s == "chooseRad")
        {
            trigoption = 1;
            return d;
        }
        if(s == "=")
            return evaluate(d);
        if(s == "%")
            return evaluate(0.01D * d);
        else
            return d;
    }

    private double stackval;
    private int operator;
    private int trigoption;
    public static final int NO_OP = 0;
    public static final int OP_PLUS = 1;
    public static final int OP_MINUS = 2;
    public static final int OP_MULT = 3;
    public static final int OP_DIV = 4;
    public static final int OP_POW = 5;
    public static final int TRIG_DEG = 0;
    public static final int TRIG_RAD = 1;
}
