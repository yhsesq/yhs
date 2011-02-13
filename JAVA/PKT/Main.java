// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   Main.java

package net.sf.pkt.hexed;

import java.io.PrintStream;
import net.sf.pkt.hexed.gui.HexEditor;

public class Main
{

    public Main()
    {
    }

    public static void main(String args[])
    {
        boolean roflag = false;
        if(args.length != 0)
        {
            int i = 0;
            do
            {
                if(i >= args.length)
                    break;
                if("-ro".equals(args[i]))
                    roflag = true;
                else
                if("-rw".equals(args[i]))
                    roflag = false;
                else
                if("-help".equals(args[i]))
                {
                    printHelp();
                } else
                {
                    if("--".equals(args[i]))
                    {
                        StringBuffer coll = new StringBuffer();
                        for(int j = i + 1; j < args.length; j++)
                        {
                            coll.append(args[j]);
                            if(j != args.length - 1)
                                coll.append(" ");
                        }

                        new HexEditor(coll.toString(), roflag);
                        break;
                    }
                    if(args[i].startsWith("-") || args[i].startsWith("/"))
                    {
                        System.out.println("Unknown command switch.\n");
                        printHelp();
                    } else
                    {
                        new HexEditor(args[i], roflag);
                    }
                }
                i++;
            } while(true);
        } else
        {
            new HexEditor();
        }
    }

    public static void printHelp()
    {
        System.out.println("PKTHexEditor");
        System.out.println("\nUsage:\n\njava -jar HexEditor [options] filenames\n\n-ro   open following files read-only\n-rw   open following files read-write\n-help show this help screen\n--    all following parameters are one filename\n\n");
    }

    public static final String VERSION = "1.1";
}
