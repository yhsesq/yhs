// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 
// Source File Name:   HexView.java

package net.sf.pkt.hexed.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.TreeSet;
import net.sf.pkt.hexed.file.HexFile;

// Referenced classes of package net.sf.pkt.hexed.gui:
//            HexEditor, FindDialog, FileListing

public class HexView extends Canvas
    implements AdjustmentListener, KeyListener, ActionListener
{

    public HexView(HexFile f, Scrollbar s, Frame parent)
    {
        startpos = 0L;
        hexing = true;
        halfchar = ' ';
        visiblerange = 16;
        mousex = -1;
        wideOffsets = false;
        splitmarks = f.getSplitMarks();
        addKeyListener(this);
        this.parent = parent;
        file = f;
        scroll = s;
        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e)
            {
                repaint();
            }

        });
        font = new Font("Courier", 0, 12);
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e)
            {
                mousex = e.getX();
                mousey = e.getY();
                mouseshift = e.isShiftDown();
                repaint();
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e)
            {
                mousex = e.getX();
                mousey = e.getY();
                mouseshift = true;
                repaint();
            }

        });
    }

    public void update(Graphics g)
    {
        int height = getHeight();
        int width = getWidth();
        if(buffer == null || buffer.getHeight(this) != height || buffer.getWidth(this) != width)
            buffer = createImage(width, height);
        Graphics bufg = buffer.getGraphics();
        bufg.setColor(Color.white);
        bufg.fillRect(0, 0, width, height);
        paint(bufg);
        g.drawImage(buffer, 0, 0, this);
    }

    public void paint(Graphics g)
    {
        int os = wideOffsets ? 17 : 9;
        int h = getHeight();
        int w = getWidth();
        g.setFont(font);
        long filesize = file.getSize();
        FontMetrics fm = getFontMetrics(font);
        int xskip = fm.charWidth('*');
        int yskip = fm.getHeight();
        int ystart = fm.getAscent();
        h /= yskip;
        w /= xskip;
        if(mousex != -1)
        {
            int xx = (mousex + xskip / 2) / xskip;
            int yy = mousey / yskip;
            if(xx < os)
            {
                xx = 0;
                hexing = true;
            } else
            if(xx < 50 + os)
            {
                xx = (xx - os) / 3;
                hexing = true;
            } else
            {
                xx = xx - 50 - os;
                hexing = false;
            }
            if(xx > 15)
                xx = 15;
            halfchar = ' ';
            cursor = startpos + (long)(yy * 16) + (long)xx;
            validateCursor();
            if(!mouseshift)
                mark = cursor;
            mousex = -1;
        }
        visiblerange = h * 16;
        if(visiblerange < 16)
            visiblerange = 16;
        if(filesize > 0x7ffffffffL)
            scroll.setValues(0, h, 0, 0);
        else
            scroll.setValues((int)(startpos / 16L), h, 0, (int)(filesize / 16L + 1L));
        if(w < 66 + os)
        {
            g.setColor(Color.black);
            g.drawString("window width too small.", 0, ystart);
            return;
        }
        int i = 0;
label0:
        do
        {
            if(i >= h)
                break;
            g.setPaintMode();
            g.setColor(Color.blue);
            if((long)(i * 16) + startpos > filesize)
                break;
            g.drawString(makeHexOffsetLeft((long)(i * 16) + startpos), 0, ystart + i * yskip);
            for(int j = 0; j < 16; j++)
            {
                long offs = (long)(j + i * 16) + startpos;
                if(offs > filesize)
                    break label0;
                if(offs != filesize)
                {
                    byte b = file.getByte(offs);
                    if(file.isChanged(offs) || offs == cursor && halfchar != ' ')
                        g.setColor(Color.red);
                    else
                        g.setColor(Color.black);
                    if(offs >= mark && offs < cursor || offs >= cursor && offs < mark)
                    {
                        Color oldcol = g.getColor();
                        g.setColor(Color.black);
                        if(file.isInserted(offs))
                            g.setColor(Color.blue);
                        g.fillRect((j + 50 + os) * xskip, i * yskip, xskip, yskip);
                        g.fillRect((j * 3 + os) * xskip, i * yskip, xskip * 3, yskip);
                        g.setColor(oldcol != Color.black ? Color.yellow : Color.white);
                    } else
                    if(file.isInserted(offs))
                    {
                        Color oldcol = g.getColor();
                        g.setColor(Color.yellow);
                        g.fillRect((j + 50 + os) * xskip, i * yskip, xskip, yskip);
                        g.fillRect((j * 3 + os) * xskip, i * yskip, xskip * 3, yskip);
                        g.setColor(oldcol);
                    }
                    if(offs != cursor || halfchar == ' ')
                        g.drawString(makeHex(b), (j * 3 + os) * xskip, i * yskip + ystart);
                    else
                        g.drawChars(new char[] {
                            halfchar, '-'
                        }, 0, 2, (j * 3 + os) * xskip, i * yskip + ystart);
                    if((b & 0xff) < 32)
                        b = 46;
                    g.drawString(new String(new byte[] {
                        b
                    }), (j + 50 + os) * xskip, ystart + i * yskip);
                }
                if(splitmarks.contains(new Long(offs)))
                {
                    g.setColor(Color.green);
                    int x1 = ((os * 2 - 1) * xskip) / 2;
                    int x2 = (((j * 6 + os * 2) - 1) * xskip) / 2;
                    int x3 = ((97 + os * 2) * xskip) / 2;
                    int x4 = (j + 50 + os) * xskip;
                    int x5 = ((133 + os * 2) * xskip) / 2;
                    g.drawLine(x1, (i + 1) * yskip + 2, x2, (i + 1) * yskip + 2);
                    g.drawLine(x2, i * yskip + 2, x2, (i + 1) * yskip + 2);
                    g.drawLine(x2, i * yskip + 2, x3, i * yskip + 2);
                    g.drawLine(x3, (i + 1) * yskip + 2, x4, (i + 1) * yskip + 2);
                    g.drawLine(x4, i * yskip + 2, x4, (i + 1) * yskip + 2);
                    g.drawLine(x4, i * yskip + 2, x5, i * yskip + 2);
                }
                if(file.isGapBefore(offs))
                {
                    g.setColor(Color.red);
                    g.fillRect((j * 3 + os) * xskip - 3, i * yskip, 1, yskip);
                    g.fillRect((j + 50 + os) * xskip - 1, i * yskip, 1, yskip);
                }
                if(offs != cursor)
                    continue;
                g.setColor(Color.magenta);
                if(hexing)
                {
                    g.fillRect((j * 3 + os + (halfchar != ' ' ? 1 : 0)) * xskip - 1, i * yskip, 2, yskip);
                    if(offs != filesize)
                        g.fillRect((j + 50 + os) * xskip, (i + 1) * yskip - 1, xskip, 2);
                    continue;
                }
                g.fillRect((j + 50 + os) * xskip - 1, i * yskip, 2, yskip);
                if(offs != filesize)
                    g.fillRect((j * 3 + os) * xskip, (i + 1) * yskip - 1, xskip * 2, 2);
            }

            i++;
        } while(true);
        ((HexEditor)parent).refreshStatus();
    }

    private String makeHexOffsetLeft(long offs)
    {
        String hs = "00000000" + Long.toHexString(offs);
        if(wideOffsets)
        {
            hs = "00000000" + hs;
            return hs.substring(hs.length() - 16);
        }
        if(hs.length() > 24)
            return "OVERFLOW";
        if(hs.length() > 16)
        {
            hs = hs.substring(hs.length() - 16);
            int ii = (int)((offs / 16L) % 16L);
            hs = (ii >= 10 ? 32 : hs.charAt(ii)) + "|" + hs.substring(10);
        } else
        {
            hs = hs.substring(hs.length() - 8);
        }
        return hs;
    }

    static String makeHexOffset(long offs)
    {
        String hs = Long.toHexString(offs);
        if(hs.length() > 16)
        {
            hs = "..." + hs.substring(hs.length() - 5);
        } else
        {
            hs = "0000000000000000" + hs;
            hs = hs.substring(hs.length() - 16);
        }
        return hs;
    }

    public static String makeHex(byte b)
    {
        String buf = "00" + Integer.toHexString(b).toUpperCase();
        return buf.substring(buf.length() - 2);
    }

    public Dimension getMinimumSize()
    {
        FontMetrics fm = getFontMetrics(font);
        return new Dimension(75 * fm.charWidth('*'), fm.getHeight());
    }

    public Dimension getPreferredSize()
    {
        FontMetrics fm = getFontMetrics(font);
        return new Dimension(88 * fm.charWidth('*'), 25 * fm.getHeight() + 4);
    }

    public void adjustmentValueChanged(AdjustmentEvent e)
    {
        boolean nomark = mark == cursor;
        startpos = (long)e.getValue() * 16L;
        if(cursor < startpos)
            cursor = startpos;
        if(cursor >= startpos + (long)visiblerange)
            cursor = (startpos + (long)visiblerange) - 1L;
        repaint();
        if(nomark)
            mark = cursor;
    }

    public void keyPressed(KeyEvent e)
    {
        if(!e.isAltDown() && !e.isMetaDown() && !e.isControlDown())
        {
            switch(e.getKeyCode())
            {
            case 9: // '\t'
            case 117: // 'u'
                hexing = !hexing;
                halfchar = ' ';
                break;

            case 10: // '\n'
                if(e.isShiftDown())
                {
                    long temp = cursor;
                    cursor = mark;
                    mark = temp;
                } else
                {
                    hexing = !hexing;
                    halfchar = ' ';
                }
                break;

            case 8: // '\b'
                if(halfchar != ' ')
                    halfchar = ' ';
                else
                    file.undo(--cursor);
                break;

            case 127: // '\177'
                deleteByte();
                break;

            case 155: 
                insertByte();
                break;

            case 37: // '%'
                cursor--;
                break;

            case 39: // '\''
                cursor++;
                break;

            case 38: // '&'
                cursor -= 16L;
                break;

            case 40: // '('
                cursor += 16L;
                break;

            case 34: // '"'
                cursor += visiblerange;
                break;

            case 33: // '!'
                cursor -= visiblerange;
                break;

            case 36: // '$'
                cursor = 0L;
                break;

            case 35: // '#'
                cursor = file.getSize();
                break;

            case 114: // 'r'
                doFind(false, parent);
                repaint();
                return;

            case 120: // 'x'
                wideOffsets = !wideOffsets;
                repaint();
                return;

            default:
                return;
            }
            validateCursor();
            if(!e.isShiftDown())
                mark = cursor;
        } else
        {
            return;
        }
        e.consume();
        repaint();
    }

    public void actionPerformed(ActionEvent e)
    {
        int num = e.getActionCommand().charAt(0) - 48;
        cursor = (file.getSize() * (long)num) / 10L;
        validateCursor();
        mark = cursor;
        repaint();
    }

    private void validateCursor()
    {
        if(cursor > file.getSize())
            cursor = file.getSize();
        if(cursor < 0L)
            cursor = 0L;
        if(cursor < startpos)
        {
            long newpos = startpos - 16L * ((startpos - cursor) / 16L);
            if(cursor < newpos)
                newpos -= 16L;
            startpos = newpos;
        }
        if(cursor >= startpos + (long)visiblerange)
        {
            long newpos = startpos + 16L * ((cursor - startpos - (long)visiblerange) / 16L);
            if(cursor >= newpos + (long)visiblerange)
                newpos += 16L;
            startpos = newpos;
        }
    }

    public void keyReleased(KeyEvent keyevent)
    {
    }

    public void keyTyped(KeyEvent e)
    {
        char ch;
        if((ch = e.getKeyChar()) != '\uFFFF' && (e.getModifiers() & 0xe) == 0)
        {
            if(cursor == file.getSize())
                return;
            if(!hexing)
            {
                byte by[] = ("" + ch).getBytes();
                if(by.length == 1 && (by[0] & 0xff) > 31 && by[0] != 127)
                {
                    file.setByte(cursor, by[0]);
                    cursor++;
                    validateCursor();
                    mark = cursor;
                    e.consume();
                    repaint();
                }
            } else
            if("0123456789ABCDEFabcdef".indexOf(ch) != -1)
            {
                if(halfchar == ' ')
                {
                    halfchar = Character.toUpperCase(ch);
                } else
                {
                    int b = Integer.parseInt("" + halfchar + ch, 16);
                    file.setByte(cursor, (byte)b);
                    halfchar = ' ';
                    cursor++;
                    validateCursor();
                    mark = cursor;
                }
                e.consume();
                repaint();
            }
        }
    }

    public void goToStart()
    {
        cursor = 0L;
        mark = 0L;
        validateCursor();
        splitmarks.clear();
        if(file.getSize() > 0xffffffffL)
            wideOffsets = true;
    }

    public void copy(boolean cut)
    {
        StringSelection ss = new StringSelection(getSelection());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
        if(cut && cursor != mark)
            deleteByte();
    }

    public void paste(boolean insert)
    {
        String s = null;
        try
        {
            s = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
        }
        catch(UnsupportedFlavorException ex)
        {
            s = null;
        }
        catch(IOException ex)
        {
            s = null;
        }
        if(s != null)
        {
            if(insert)
            {
                long insLen = s.length();
                if(mark < cursor)
                {
                    insLen -= cursor - mark;
                    cursor = mark;
                }
                insLen += cursor - mark;
                if(insLen > 0L)
                    file.insertBytes(cursor, insLen);
                else
                if(insLen < 0L)
                    file.deleteBytes(cursor, -insLen);
                FileListing.refresh();
            }
            setSelection(s);
        }
    }

    public void deleteByte()
    {
        halfchar = ' ';
        if(cursor == mark)
            file.deleteBytes(cursor, 1L);
        else
        if(cursor < mark)
        {
            file.deleteBytes(cursor, mark - cursor);
            mark = cursor;
        } else
        {
            file.deleteBytes(mark, cursor - mark);
            cursor = mark;
        }
        FileListing.refresh();
    }

    public void insertByte()
    {
        halfchar = ' ';
        if(cursor == mark)
            file.insertBytes(cursor, 1L);
        else
            file.insertBytes(cursor, Math.abs(cursor - mark));
        FileListing.refresh();
    }

    public boolean isFocusable()
    {
        return true;
    }

    public boolean goTo(long pos, long size)
    {
        if(pos + size > file.getSize())
            return false;
        if(pos < 0L)
            return false;
        if(size < 0L)
        {
            return false;
        } else
        {
            cursor = pos;
            validateCursor();
            mark = cursor;
            cursor = pos + size;
            validateCursor();
            return true;
        }
    }

    public String getSelection()
    {
        StringBuffer sb = new StringBuffer();
        long b = mark;
        long en = cursor;
        boolean here = false;
        if(b > en)
        {
            en = mark;
            b = cursor;
        }
        for(long i = b; i < en;)
            try
            {
                sb.append(new String(new byte[] {
                    file.getByte(i)
                }, "ISO-8859-1"));
                continue;
            }
            catch(UnsupportedEncodingException e)
            {
                sb.append(new String(new byte[] {
                    file.getByte(i)
                }));
                if(!here)
                {
                    e.printStackTrace();
                    here = true;
                }
                i++;
            }

        return sb.toString();
    }

    public void setSelection(String what)
    {
        boolean here = false;
        mark = cursor;
        for(int i = 0; i < what.length(); i++)
        {
            byte b[];
            try
            {
                b = what.substring(i, i + 1).getBytes("ISO-8859-1");
            }
            catch(UnsupportedEncodingException e)
            {
                b = what.substring(i, i + 1).getBytes();
                if(!here)
                {
                    e.printStackTrace();
                    here = true;
                }
            }
            if(b.length == 1)
            {
                file.setByte(cursor, b[0]);
                cursor++;
            }
        }

        repaint();
    }

    public void doFind(boolean dialog, Frame editor)
    {
        if(dialog || findlast == null)
        {
            FindDialog f = new FindDialog(parent);
            f.show();
            findlast = f.result;
            ignorecase = f.ignorecase;
        }
        if(findlast != null)
        {
            long p = file.getPosition(findlast, cursor, ignorecase);
            if(p != -1L)
            {
                cursor = p;
                validateCursor();
                mark = cursor;
                cursor = p + (long)findlast.length;
                validateCursor();
            } else
            {
                mark = cursor;
            }
        }
    }

    public byte[] getStatusSource()
    {
        byte what[];
        if(cursor == mark)
        {
            long mrk = cursor + 16L;
            if(mrk > file.getSize())
                mrk = file.getSize();
            what = new byte[(int)(mrk - cursor)];
            for(int i = 0; i < what.length; i++)
                what[i] = file.getByte(cursor + (long)i);

        } else
        {
            long beg = cursor;
            long end = mark;
            if(beg > end)
            {
                end = cursor;
                beg = mark;
            }
            if(end - beg > 1024L)
                return new byte[0];
            what = new byte[(int)(end - beg)];
            for(int i = 0; (long)i < end - beg; i++)
                what[i] = file.getByte(beg + (long)i);

        }
        return what;
    }

    public void toggleSplitMark()
    {
        Long lg = new Long(cursor);
        if(!splitmarks.remove(lg))
            splitmarks.add(lg);
        repaint();
        System.out.println(splitmarks.size());
        FileListing.refresh();
    }

    public void notifySelection(long from, long to)
    {
        cursor = to;
        validateCursor();
        mark = to;
        cursor = from;
        validateCursor();
        repaint();
    }

    long startpos;
    HexFile file;
    Font font;
    Scrollbar scroll;
    long cursor;
    long mark;
    boolean hexing;
    char halfchar;
    int visiblerange;
    int mousex;
    int mousey;
    boolean mouseshift;
    boolean ignorecase;
    Image buffer;
    byte findlast[];
    Frame parent;
    TreeSet splitmarks;
    boolean wideOffsets;
}
