/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: josh $
 * $Date: 2001/02/03 00:47:01 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.awt.*;
import java.awt.event.*;

import mindbright.util.AWTConvenience;

public final class SSHTunnelDialog {
    private static Dialog    basicTunnelsDialog = null;
    private static List      tunnelList;
    private static TextField remoteHost, remotePort, localPort;
    private static Choice    protoChoice;
    private final static String[] protos = { "general", "ftp", "telnet", "smtp", "http", "pop2", "pop3", "nntp", "imap" };
    final static int[]    servs  = {  0, 21, 23, 25, 80, 109, 110, 119, 143 };

    private static SSHPropertyHandler propsHandler;
    private static Frame              parent;
    private static SSHClient          client;

    public static void show(String title, SSHClient cli,
			    SSHPropertyHandler props, Frame p) {
	propsHandler = props;
	parent       = p;
	client       = cli;
	if(basicTunnelsDialog == null) {
	    basicTunnelsDialog = new Dialog(parent, title, true);

	    GridBagLayout       grid  = new GridBagLayout();
	    GridBagConstraints  gridc = new GridBagConstraints();
	    Label               lbl;
	    Button              b;
	    ActionListener      al;

	    basicTunnelsDialog.setLayout(grid);

	    gridc.fill      = GridBagConstraints.NONE;
	    gridc.anchor    = GridBagConstraints.WEST;
	    gridc.gridy     = 0;
	    gridc.insets    = new Insets(4, 4, 0, 4);

	    lbl = new Label("Current local tunnels:");
	    gridc.gridwidth = 2;
	    grid.setConstraints(lbl, gridc);
	    basicTunnelsDialog.add(lbl);

	    gridc.fill      = GridBagConstraints.BOTH;
	    gridc.anchor    = GridBagConstraints.WEST;
	    gridc.insets    = new Insets(4, 4, 4, 4);
	    gridc.weightx   = 1.0;
	    gridc.weighty   = 1.0;
	    gridc.gridwidth = 4;
	    gridc.gridy     = 1;

	    tunnelList = new List(8);
	    grid.setConstraints(tunnelList, gridc);
	    basicTunnelsDialog.add(tunnelList);
	    tunnelList.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int i = tunnelList.getSelectedIndex();
		    if(i != -1) {
			SSHClient.LocalForward fwd = (SSHClient.LocalForward) client.localForwards.elementAt(i);
			localPort.setText(String.valueOf(fwd.localPort));
			remotePort.setText(String.valueOf(fwd.remotePort));
			remoteHost.setText(fwd.remoteHost);
			for(i = 1; i < servs.length; i++) {
			    if(fwd.remotePort == servs[i]) {
				protoChoice.select(protos[i]);
				break;
			    }
			}
			if(i == servs.length)
			    protoChoice.select("general");
		    }
		}
	    });

	    gridc.fill      = GridBagConstraints.NONE;
	    gridc.weighty   = 0;
	    gridc.gridy     = 2;
	    gridc.gridwidth = 1;

	    lbl = new Label("Local port:");
	    grid.setConstraints(lbl, gridc);
	    basicTunnelsDialog.add(lbl);
	    gridc.fill      = GridBagConstraints.HORIZONTAL;
	    gridc.weightx   = 1.0;
	    localPort = new TextField("", 5);
	    grid.setConstraints(localPort, gridc);
	    basicTunnelsDialog.add(localPort);

	    lbl = new Label("Protocol:");
	    grid.setConstraints(lbl, gridc);
	    gridc.fill      = GridBagConstraints.NONE;
	    basicTunnelsDialog.add(lbl);
	    protoChoice = new Choice();
	    for(int i = 0; i < protos.length; i++) {
		protoChoice.add(protos[i]);
	    }
	    protoChoice.select("general");
	    grid.setConstraints(protoChoice, gridc);
	    basicTunnelsDialog.add(protoChoice);
	    protoChoice.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    String it = (String)e.getItem();
		    int i;
		    for(i = 0; i < protos.length; i++)
			if(it.equals(protos[i]))
			    break;
		    if(i > 0) {
			remotePort.setText(String.valueOf(servs[i]));
		    }
		}
	    });

	    gridc.gridy     = 3;
	    lbl = new Label("Remote host:");
	    grid.setConstraints(lbl, gridc);
	    basicTunnelsDialog.add(lbl);
	    gridc.fill      = GridBagConstraints.HORIZONTAL;
	    gridc.weightx   = 1.0;
	    gridc.gridwidth = 3;
	    remoteHost = new TextField("", 16);
	    grid.setConstraints(remoteHost, gridc);
	    basicTunnelsDialog.add(remoteHost);
	    gridc.gridy     = 4;
	    gridc.fill      = GridBagConstraints.NONE;
	    gridc.gridwidth = 1;
	    lbl = new Label("Remote port:");
	    grid.setConstraints(lbl, gridc);
	    basicTunnelsDialog.add(lbl);
	    remotePort = new TextField("", 5);
	    gridc.fill      = GridBagConstraints.HORIZONTAL;
	    gridc.weightx   = 0.9;
	    grid.setConstraints(remotePort, gridc);
	    basicTunnelsDialog.add(remotePort);

	    b = new Button("Add");
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String rh = remoteHost.getText();
		    String plug = "general";
		    int    lp = -1, rp = -1;
		    try {
			lp = Integer.valueOf(localPort.getText()).intValue();
			rp = Integer.valueOf(remotePort.getText()).intValue();
			if(lp < 1 || lp > 65535) {
			    lp = -1;
			    throw new NumberFormatException();
			}
			if(rp < 1 || rp > 65535) {
			    rp = -1;
			    throw new NumberFormatException();
			}
		    } catch (NumberFormatException ee) {
			if(lp == -1) {
			    localPort.setText("");
			    localPort.requestFocus();
			} else {
			    remotePort.setText("");
			    remotePort.requestFocus();
			}
			return;
		    }
		    if(protoChoice.getSelectedItem().equals("ftp"))
			plug = "ftp";
		    try {
			propsHandler.setProperty("local" + client.localForwards.size(),
							"/" + plug + "/" + lp + ":" + rh + ":" +  rp);
			if(client.isOpened())
			    SSHMiscDialogs.alert("Tunnel Notice",
						 "Tunnel is now open and operational",
						 parent);
		    } catch (Throwable ee) {
			SSHMiscDialogs.alert("Tunnel Notice",
					     "Could not open tunnel: " +
					     ee.getMessage(), parent);
		    }
		    updateTunnelList();
		}
	    });
	    gridc.fill = GridBagConstraints.HORIZONTAL;
	    gridc.weightx   = 0.1;
	    grid.setConstraints(b, gridc);
	    basicTunnelsDialog.add(b);
	    b = new Button("Delete");
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int i = tunnelList.getSelectedIndex();
		    if(i != -1) {
			propsHandler.removeLocalTunnelAt(i, true);
		    }
		    updateTunnelList();
		}
	    });
	    grid.setConstraints(b, gridc);
	    basicTunnelsDialog.add(b);
      
	    b = new Button("Close Dialog");
	    b.addActionListener(new AWTConvenience.CloseAction(basicTunnelsDialog));
	    gridc.gridy     = 5;
	    gridc.fill      = GridBagConstraints.NONE;
	    gridc.gridwidth = GridBagConstraints.REMAINDER;
	    gridc.anchor    = GridBagConstraints.CENTER;
	    gridc.ipady     = 2;
	    gridc.ipadx     = 2;
	    grid.setConstraints(b, gridc);
	    basicTunnelsDialog.add(b);

	    basicTunnelsDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

	    AWTConvenience.setBackgroundOfChildren(basicTunnelsDialog);

	    basicTunnelsDialog.setResizable(true);
	    basicTunnelsDialog.pack();
	}
	updateTunnelList();

	basicTunnelsDialog.setTitle(title);

	AWTConvenience.placeDialog(basicTunnelsDialog);
	localPort.requestFocus();
	basicTunnelsDialog.setVisible(true);
    }

    private static void updateTunnelList() {
	tunnelList.removeAll();
	for(int i = 0; i < client.localForwards.size(); i++) {
	    SSHClient.LocalForward fwd = (SSHClient.LocalForward) client.localForwards.elementAt(i);
	    String plugStr = (fwd.plugin.equals("general") ? "" : " (plugin: " + fwd.plugin + ")");
	    tunnelList.add("local: " + fwd.localPort + " -> remote: " + fwd.remoteHost + "/" +
			   fwd.remotePort + plugStr);
	}
    }

}
