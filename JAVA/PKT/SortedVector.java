// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt.xml;

import java.util.*;
import org.jdom.Attribute;

class SortedVector extends Vector
{

    public SortedVector(Iterator iterator)
    {
        for(; iterator != null && iterator.hasNext(); addElement(((Attribute)iterator.next()).getName()));
        if(0 < size())
        {
            trimToSize();
            Arrays.sort(elementData);
        }
    }
}
