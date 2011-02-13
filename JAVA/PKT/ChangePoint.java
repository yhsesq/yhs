// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   ChangePoint.java

package net.sf.pkt.hexed.file;


public class ChangePoint
{

    public ChangePoint(ChangePoint toCopy, long newPos)
    {
        this(newPos, toCopy.after, toCopy.insAfter, toCopy.insBefore);
    }

    public ChangePoint(long pos, long afterPos, boolean insertAfter, boolean insertBefore)
    {
        if(pos < afterPos)
            throw new IllegalArgumentException("Invalid Position");
        if(insertBefore && insertAfter)
            throw new IllegalArgumentException("Useless point.");
        if(pos == afterPos && !insertBefore && !insertAfter)
        {
            throw new IllegalArgumentException("Useless point.");
        } else
        {
            this.pos = pos;
            after = afterPos;
            insAfter = insertAfter;
            insBefore = insertBefore;
            return;
        }
    }

    public long getPos()
    {
        return pos;
    }

    public long getAfterPos()
    {
        return after;
    }

    public boolean isInsertAfter()
    {
        return insAfter;
    }

    public boolean isInsertBefore()
    {
        return insBefore;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof ChangePoint)
            return ((ChangePoint)obj).pos == pos;
        else
            return false;
    }

    public String toString()
    {
        return getClass().getName() + ": " + after + ".." + pos + (insAfter ? " I>" : "") + (insBefore ? " I<" : "");
    }

    private long pos;
    private long after;
    private boolean insAfter;
    private boolean insBefore;
}
