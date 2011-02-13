// PKTXUL XUL Editor for thinlets. pkt.sf.net
// (C) 2004 Yohann Sulaiman
// This program is released under the terms of the GNU GPL www.gnu.org 

package net.sf.pkt;

import java.io.IOException;
import java.net.*;

public class MCSender
{

    public MCSender(String s, int i)
    {
        ms = null;
        groupName = s;
        port = i;
    }

    public void sendMsg(String s)
    {
        try
        {
            InetAddress inetaddress = InetAddress.getByName(groupName);
            byte abyte0[] = (InetAddress.getLocalHost().toString() + "," + s + ",").getBytes();
            DatagramPacket datagrampacket = new DatagramPacket(abyte0, abyte0.length, inetaddress, port);
            ms = new MulticastSocket(port);
            ms.joinGroup(inetaddress);
            for(int i = 0; i < 10; i++)
                ms.send(datagrampacket);

            ms.leaveGroup(inetaddress);
        }
        catch(IOException ioexception) { }
        finally
        {
            if(ms != null)
                try
                {
                    ms.close();
                }
                catch(Exception exception1) { }
        }
    }

    int port;
    String groupName;
    MulticastSocket ms;
}
