// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   FileDropTargetAdapter.java

package net.sf.pkt.hexed.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;

public abstract class FileDropTargetAdapter extends DropTargetAdapter
{

    public FileDropTargetAdapter()
    {
        df = DataFlavor.javaFileListFlavor;
    }

    public void dragEnter(DropTargetDragEvent evt)
    {
        myDrag(evt);
    }

    public void dragOver(DropTargetDragEvent evt)
    {
        myDrag(evt);
    }

    public void dropActionChanged(DropTargetDragEvent evt)
    {
        myDrag(evt);
    }

    protected void myDrag(DropTargetDragEvent evt)
    {
        if(!evt.isDataFlavorSupported(df))
            evt.rejectDrag();
        else
        if(evt.getDropAction() != 1)
            evt.acceptDrag(1);
    }

    public void drop(DropTargetDropEvent evt)
    {
        if(!evt.isDataFlavorSupported(df))
        {
            evt.rejectDrop();
        } else
        {
            evt.acceptDrop(1);
            try
            {
                java.util.List l = (java.util.List)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                File fls[] = (File[])l.toArray(new File[l.size()]);
                evt.dropComplete(drop(fls));
            }
            catch(UnsupportedFlavorException e)
            {
                e.printStackTrace();
                evt.dropComplete(false);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                evt.dropComplete(false);
            }
        }
    }

    public abstract boolean drop(File afile[]);

    protected final DataFlavor df;
    protected final int action = 1;
}
