// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   HexFile.java

package net.sf.pkt.hexed.file;

import java.io.*;
import java.util.*;

// Referenced classes of package net.sf.pkt.hexed.file:
//            ChangePoint, ChangePointEx, HexFileListener

public class HexFile
{

    public HexFile(HexFileListener hfl)
    {
        changes = new HashMap();
        size = 0L;
        origSize = 0L;
        cache = new byte[16384];
        splitMarks = new TreeSet();
        changePoints = new TreeMap();
        this.hfl = hfl;
        close();
    }

    public void revert()
    {
        changes.clear();
        changePoints.clear();
        size = origSize;
    }

    public void save()
    {
        if(raf == null || isReadOnly)
            return;
        if(!changePoints.isEmpty())
            try
            {
                saveChangePoints();
            }
            catch(IOException ex)
            {
                hfl.showError(ex.toString(), "JHEditor - Save file");
                return;
            }
        Iterator it = changes.keySet().iterator();
        do
        {
            if(!it.hasNext())
                break;
            Long key = (Long)it.next();
            Byte elem = (Byte)changes.get(key);
            try
            {
                raf.seek(key.longValue());
                raf.write(elem.byteValue());
            }
            catch(IOException ex)
            {
                hfl.showError(ex.toString(), "JHEditor - Save file");
                return;
            }
        } while(true);
        changes.clear();
        changePoints.clear();
        loadCache();
    }

    public boolean close()
    {
        return open(null, true) != 0;
    }

    public int open(File f, boolean ro)
    {
        int result = 0;
        isReadOnly = ro;
        file = f;
        changes.clear();
        changePoints.clear();
        try
        {
            if(raf != null)
                raf.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        if(f == null)
        {
            size = 0L;
            origSize = 0L;
            raf = null;
            cacheStart = -1L;
            cacheLen = 0;
            result = 1;
        } else
        {
            try
            {
                if(!ro)
                    try
                    {
                        raf = new RandomAccessFile(f, "rw");
                        result = 2;
                    }
                    catch(FileNotFoundException e) { }
                if(result == 0)
                {
                    raf = new RandomAccessFile(f, "r");
                    result = 1;
                }
                cacheStart = 0L;
                size = raf.length();
                origSize = size;
            }
            catch(IOException e)
            {
                hfl.showError(e.toString(), "JHEditor - Open file");
                file = null;
                hfl.refreshFileList();
                return 0;
            }
            loadCache();
        }
        hfl.refreshFileList();
        return result;
    }

    private void loadCache()
    {
        cacheLen = 0;
        if(raf == null)
            return;
        try
        {
            raf.seek(cacheStart);
            cacheLen = raf.read(cache);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public byte getByte(long address)
    {
        if(address >= size)
            throw new IllegalArgumentException("Access Outside!");
        Byte b = (Byte)changes.get(new Long(address));
        if(b != null)
            return b.byteValue();
        long rawAddress = getRawAddress(address);
        if(rawAddress == -1L)
            return 0;
        if(raf == null)
            return 0;
        if(rawAddress < cacheStart || rawAddress >= cacheStart + (long)cacheLen)
        {
            cacheStart = rawAddress - 1024L;
            if(cacheStart < 0L)
                cacheStart = 0L;
            loadCache();
        }
        if(rawAddress < cacheStart || rawAddress >= cacheStart + (long)cacheLen)
        {
            (new Exception("Cache miss at pos" + rawAddress + "(cache start at " + cacheStart + ", LOF = " + size + ")")).printStackTrace();
            return 0;
        } else
        {
            return cache[(int)(rawAddress - cacheStart)];
        }
    }

    private ChangePointEx getRelevantChangePoint(long address)
    {
        SortedMap cpBefore = changePoints.headMap(new Long(address + 1L));
        if(cpBefore.isEmpty())
            return null;
        Long posL = (Long)cpBefore.lastKey();
        ChangePoint cp = (ChangePoint)cpBefore.get(posL);
        if(cp == null)
            throw new RuntimeException("Oops, key for " + posL + " is null!");
        if(posL.longValue() > address)
            throw new RuntimeException("Ouch!");
        else
            return new ChangePointEx(cp, posL.longValue());
    }

    private long getRawAddress(long address)
    {
        ChangePointEx cp = getRelevantChangePoint(address);
        if(cp == null)
            return address;
        if(cp.isInsertAfter())
            return -1L;
        else
            return (address - cp.getExPos()) + cp.getPos();
    }

    public void setByte(long address, byte value)
    {
        if(address < size && address >= 0L)
            changes.put(new Long(address), new Byte(value));
    }

    public void insertBytes(long address, long count)
    {
        if(count != 1L)
        {
            for(long i = 0L; i < count; i++)
                insertBytes(address, 1L);

            return;
        }
        ChangePointEx relevant = getRelevantChangePoint(address);
        Long addressL = new Long(address);
        if(relevant == null)
        {
            long rawAddress = address;
            changePoints.put(addressL, new ChangePoint(rawAddress, rawAddress, false, true));
            moveEntries(address, 1, true);
            changePoints.put(new Long(address), new ChangePoint(-1L, -1L, true, false));
        } else
        if(relevant.isInsertAfter())
            moveEntries(address, 1, false);
        else
        if(relevant.getExPos() == address)
        {
            if(relevant.getPos() > relevant.getAfterPos())
            {
                if(relevant.getPos() == relevant.getAfterPos() + 1L && !relevant.isInsertBefore())
                    changePoints.remove(addressL);
                else
                    changePoints.put(addressL, new ChangePoint(relevant, relevant.getPos() - 1L));
                moveEntries(address, 1, false);
            } else
            if(relevant.isInsertBefore())
                moveEntries(address, 1, true);
            else
                throw new RuntimeException("What does '" + relevant + "'do?");
        } else
        {
            long rawAddress = getRawAddress(address);
            changePoints.put(addressL, new ChangePoint(rawAddress, rawAddress, false, true));
            moveEntries(address, 1, true);
            changePoints.put(new Long(address), new ChangePoint(-1L, -1L, true, false));
        }
        getCopyBufferLength();
    }

    public void deleteBytes(long address, long count)
    {
        if(address + count > size)
            return;
        if(count != 1L)
        {
            for(long i = 0L; i < count; i++)
                deleteBytes(address, 1L);

            return;
        }
        Long addressL = new Long(address);
        ChangePointEx relevant = getRelevantChangePoint(address);
        ChangePoint cp2 = (ChangePoint)changePoints.get(new Long(address + 1L));
        long d = address - (relevant != null ? relevant.getExPos() : 0L);
        if(relevant != null && relevant.isInsertAfter())
        {
            if(cp2 != null)
            {
                if(!cp2.isInsertBefore())
                    throw new RuntimeException("Assert failed");
                if(cp2.getPos() == cp2.getAfterPos() && d == 0L)
                    changePoints.remove(addressL);
                else
                    changePoints.put(addressL, new ChangePoint(cp2.getPos(), cp2.getAfterPos(), false, d != 0L));
            }
        } else
        if(cp2 != null)
        {
            if(cp2.isInsertAfter())
            {
                ChangePoint cp = (ChangePoint)changePoints.get(addressL);
                long killed = 0L;
                if(cp != null)
                    killed = cp.getPos() - cp.getAfterPos();
                if(cp != null && cp.isInsertBefore())
                    changePoints.remove(addressL);
                else
                    changePoints.put(addressL, cp2);
                SortedMap s = changePoints.tailMap(new Long(address + 2L));
                if(s.isEmpty())
                    throw new RuntimeException("Ouch");
                Long key = (Long)s.firstKey();
                cp = (ChangePoint)s.get(key);
                s.put(key, new ChangePoint(cp.getPos(), cp.getAfterPos() - 1L - killed, false, true));
            } else
            if(relevant == null)
                changePoints.put(addressL, new ChangePoint(cp2.getPos(), d, false, false));
            else
                changePoints.put(addressL, new ChangePoint(cp2.getPos(), relevant.getAfterPos() + d, false, relevant.isInsertBefore() & (d == 0L)));
        } else
        if(relevant == null)
            changePoints.put(addressL, new ChangePoint(address + 1L, address, false, false));
        else
            changePoints.put(addressL, new ChangePoint(relevant.getPos() + 1L + d, (d != 0L ? relevant.getPos() : relevant.getAfterPos()) + d, false, relevant.isInsertBefore() & (d == 0L)));
        moveEntries(address, -1, false);
        getCopyBufferLength();
    }

    private int getCopyBufferLength()
    {
        try{return saveChangePoints(null);}
        catch(IOException ex){
        ex.printStackTrace();}
        return 0;
    }

    private void saveChangePoints()
        throws IOException
    {
        int length = saveChangePoints(null);
        byte buffer[] = new byte[length + 1];
        saveChangePoints(buffer);
    }

    private int saveChangePoints(byte buffer[])
        throws IOException
    {
        if(changePoints.isEmpty())
            return 0;
        int startB = 0;
        int endB = 0;
        long startF = 0L;
        int len = 0;
        int maxLen = 0;
        SortedMap rest = changePoints;
        long startPos = 0L;
        long endPos0 = 0L;
        long rawPos = 0L;
        do
        {
            if(endPos0 >= size)
                break;
            if(rest.isEmpty())
            {
                endPos0 = size;
            } else
            {
                endPos0 = ((Long)rest.firstKey()).longValue();
                rest = rest.tailMap(new Long(endPos0 + 1L));
            }
            if(endPos0 != startPos)
            {
                for(long pos = startPos; pos < endPos0; pos += 16384L)
                {
                    long endPos = endPos0;
                    if(endPos - pos > 16384L)
                        endPos = pos + 16384L;
                    int blocklen = (int)(endPos - pos);
                    byte bb[] = new byte[blocklen];
                    rawPos = getRawAddress(pos);
                    if(rawPos != -1L && pos - rawPos != endPos - 1L - getRawAddress(endPos - 1L))
                    {
                        System.out.println();
                        throw new RuntimeException("Oops: " + pos + " " + rawPos + "/" + (endPos - 1L) + "/" + getRawAddress(endPos - 1L));
                    }
                    if(rawPos == -1L && getRawAddress(endPos - 1L) != -1L)
                        throw new RuntimeException("Oops!");
                    if(rawPos >= pos)
                    {
                        startF = endPos;
                        len = 0;
                        startB = endB = 0;
                        if(rawPos > pos && buffer != null)
                        {
                            raf.seek(rawPos);
                            raf.readFully(bb);
                            raf.seek(pos);
                            raf.write(bb);
                        }
                        continue;
                    }
                    if(rawPos != -1L && rawPos >= pos)
                        continue;
                    if(startF + (long)len != pos)
                        throw new RuntimeException(startF + "+" + len + " != " + pos);
                    if(rawPos != -1L && rawPos < startF)
                        throw new RuntimeException("Data lost");
                    len += bb.length;
                    if(len > maxLen)
                        maxLen = len;
                    if(buffer != null && len > buffer.length)
                        throw new RuntimeException("Buffer too small!");
                    if(buffer != null)
                    {
                        if(origSize > pos)
                        {
                            raf.seek(pos);
                            int readlen = (int)(endPos - pos);
                            if(endPos > origSize)
                                readlen = (int)(origSize - pos);
                            raf.readFully(bb, 0, readlen);
                        }
                        for(int i = 0; i < bb.length; i++)
                        {
                            buffer[endB++] = bb[i];
                            endB %= buffer.length;
                            if(endB == startB)
                                throw new RuntimeException("Overflow");
                        }

                    }
                    if(rawPos == -1L)
                    {
                        if(buffer != null)
                        {
                            bb = new byte[bb.length];
                            raf.seek(pos);
                            raf.write(bb);
                        }
                        continue;
                    }
                    do
                    {
                        if(rawPos <= startF)
                            break;
                        startF++;
                        len--;
                        if(buffer != null)
                        {
                            if(startB == endB)
                                throw new RuntimeException("Underflow");
                            startB = ++startB % buffer.length;
                        }
                    } while(true);
                    len = (int)((long)len - (endPos - pos));
                    startF += endPos - pos;
                    if(buffer == null)
                        continue;
                    for(int i = 0; i < bb.length; i++)
                    {
                        bb[i] = buffer[startB++];
                        startB %= buffer.length;
                    }

                    raf.seek(pos);
                    raf.write(bb);
                }

                startPos = endPos0;
            }
        } while(true);
        if(buffer != null)
        {
            raf.setLength(size);
            origSize = size;
        }
        return maxLen;
    }

    private void moveEntries(long minAddress, int howFar, boolean includeCP)
    {
        size += howFar;
        Map newChanges = new HashMap();
        Iterator it = changes.keySet().iterator();
        do
        {
            if(!it.hasNext())
                break;
            Long key = (Long)it.next();
            long k = key.longValue();
            Object val = changes.get(key);
            if(k >= minAddress)
            {
                it.remove();
                if(k + (long)howFar >= minAddress)
                    newChanges.put(new Long(k + (long)howFar), val);
            }
        } while(true);
        changes.putAll(newChanges);
        Map newChangePoints = new HashMap();
        it = changePoints.keySet().iterator();
        do
        {
            if(!it.hasNext())
                break;
            Long key = (Long)it.next();
            long k = key.longValue();
            Object val = changePoints.get(key);
            if(val == null)
                throw new RuntimeException("Not expected!");
            if(k >= minAddress + (long)(includeCP ? 0 : 1))
            {
                it.remove();
                if(k + (long)howFar >= minAddress + (long)(includeCP ? 0 : 1))
                    newChangePoints.put(new Long(k + (long)howFar), val);
            }
        } while(true);
        changePoints.putAll(newChangePoints);
        Set newSplitMarks = new HashSet();
        it = splitMarks.iterator();
        do
        {
            if(!it.hasNext())
                break;
            Long val = (Long)it.next();
            long v = val.longValue();
            if(v >= minAddress)
            {
                it.remove();
                if(v + (long)howFar >= minAddress)
                    newSplitMarks.add(new Long(v + (long)howFar));
            }
        } while(true);
        splitMarks.addAll(newSplitMarks);
    }

    public void undo(long address)
    {
        changes.remove(new Long(address));
    }

    public boolean isChanged(long address)
    {
        return changes.containsKey(new Long(address));
    }

    public boolean isChanged()
    {
        return !changes.isEmpty() || !changePoints.isEmpty();
    }

    public boolean isInserted(long address)
    {
        return getRawAddress(address) == -1L;
    }

    public boolean isGapBefore(long address)
    {
        ChangePoint cp = (ChangePoint)changePoints.get(new Long(address));
        if(cp == null || cp.isInsertAfter())
            return false;
        return cp.getPos() != cp.getAfterPos();
    }

    public long getSize()
    {
        return size;
    }

    public long getPosition(byte ba[], long from, boolean icase)
    {
        long p = from;
label0:
        do
        {
            if(p >= size - (long)ba.length)
                break;
            if(isequal(getByte(p), ba[0], icase))
            {
                int i = 1;
                do
                {
                    if(i >= ba.length)
                        break;
                    if(!isequal(getByte(p + (long)i), ba[i], icase))
                    {
                        p++;
                        continue label0;
                    }
                    i++;
                } while(true);
                return p;
            }
            p++;
        } while(true);
        p = 0L;
label1:
        do
        {
            if(p >= from)
                break;
            if(getByte(p) == ba[0])
            {
                int i = 1;
                do
                {
                    if(i >= ba.length)
                        break;
                    if(getByte(p + (long)i) != ba[i])
                    {
                        p++;
                        continue label1;
                    }
                    i++;
                } while(true);
                return p;
            }
            p++;
        } while(true);
        return -1L;
    }

    private boolean isequal(byte b1, byte b2, boolean icase)
    {
        if(!icase)
            return b1 == b2;
        else
            return Character.toUpperCase((char)b1) == Character.toUpperCase((char)b2);
    }

    public void setSplitMarks(TreeSet splitMarks)
    {
        this.splitMarks = splitMarks;
    }

    public TreeSet getSplitMarks()
    {
        return splitMarks;
    }

    public String getName()
    {
        return file != null ? file.getName() : null;
    }

    public void writeByteBlock(long start, long end, BufferedOutputStream out)
        throws IOException
    {
        if(!changePoints.isEmpty())
        {
            byte bb[] = new byte[1];
            for(long i = start; i < end; i++)
            {
                long adr = getRawAddress(i);
                Byte val = (Byte)changes.get(new Long(i));
                if(val != null)
                    bb[0] = val.byteValue();
                else
                if(adr != -1L)
                {
                    raf.seek(adr);
                    raf.readFully(bb);
                } else
                {
                    bb[0] = 0;
                }
                out.write(bb);
            }

            return;
        }
        TreeSet changepos = new TreeSet();
        Iterator it = changes.keySet().iterator();
        do
        {
            if(!it.hasNext())
                break;
            Long elem = (Long)it.next();
            long elemv = elem.longValue();
            if(elemv >= start && elemv < end)
                changepos.add(elem);
        } while(true);
        it = changepos.iterator();
        Long nextElem = null;
        if(it.hasNext())
            nextElem = (Long)it.next();
        cacheStart = start;
        if(raf == null)
        {
            Arrays.fill(cache, (byte)0);
            cacheLen = cache.length;
        } else
        {
            loadCache();
        }
        do
        {
            while(nextElem != null && nextElem.longValue() < cacheStart + (long)cacheLen) 
            {
                cache[(int)(nextElem.longValue() - cacheStart)] = getByte(nextElem.longValue());
                if(it.hasNext())
                    nextElem = (Long)it.next();
                else
                    nextElem = null;
            }
            if(cacheStart + (long)cacheLen > end)
                cacheLen = (int)(end - cacheStart);
            out.write(cache, 0, cacheLen);
            cacheStart += cacheLen;
            if(cacheStart != end)
            {
                if(cacheStart > end)
                    throw new RuntimeException("Internal error 143");
            } else
            {
                return;
            }
        } while(true);
    }

    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    Map changes;
    long size;
    long origSize;
    RandomAccessFile raf;
    byte cache[];
    long cacheStart;
    int cacheLen;
    File file;
    boolean isReadOnly;
    HexFileListener hfl;
    private TreeSet splitMarks;
    private TreeMap changePoints;
}
