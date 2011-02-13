// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.dialogs;

import net.sf.pkt.PKTXUL;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Logger;
import thinlet.Thinlet;

public class ThinDialog
{

    public ThinDialog()
    {
        maintain = true;
        handlers = new Vector();
    }

    public void show()
        throws IOException
    {
    }

    public void update()
    {
        exeEventhandlers("update");
    }

    public void close()
    {
        if(thinlet != null)
        {
            thinlet.remove(dialog);
            if(!maintain && dialog != null)
                dialog = null;
        }
    }

    public void register(Method method)
    {
        if(method != null)
            handlers.addElement(method);
    }

    public boolean unregister(Method method)
    {
        boolean flag = false;
        int i = 0;
        do
        {
            if(i >= handlers.size())
                break;
            if(handlers.elementAt(i).equals(method))
            {
                handlers.removeElementAt(i);
                flag = true;
                break;
            }
            i++;
        } while(true);
        return flag;
    }

    private void exeEventhandlers(String s)
    {
        if(!handlers.isEmpty())
        {
            for(Enumeration enumeration = handlers.elements(); enumeration.hasMoreElements();)
            {
                Method method = (Method)enumeration.nextElement();
                try
                {
                    method.invoke(thinlet, new Class[0]);
                }
                catch(Exception exception)
                {
                    PKTXUL.getLogger().warning(exception.toString());
                    exception.printStackTrace();
                }
            }

        }
    }

    protected Object dialog;
    protected Thinlet thinlet;
    protected boolean maintain;
    private Vector handlers;
}
