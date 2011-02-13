// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   ChangePointEx.java

package net.sf.pkt.hexed.file;


// Referenced classes of package net.sf.pkt.hexed.file:
//            ChangePoint

public class ChangePointEx extends ChangePoint
{

    public ChangePointEx(ChangePoint cp, long exPos)
    {
        super(cp.getPos(), cp.getAfterPos(), cp.isInsertAfter(), cp.isInsertBefore());
        this.exPos = exPos;
    }

    public long getExPos()
    {
        return exPos;
    }

    private long exPos;
}
