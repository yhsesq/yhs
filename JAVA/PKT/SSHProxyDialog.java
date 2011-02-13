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

public final class SSHProxyDialog {
    private static Dialog     proxyDialog = null;
    private static Choice     choicePrxType;
    private static Checkbox   cbNeedAuth;
    private static TextField  textPrxHost, textPrxPort, textPrxUser, textPrxPasswd;
    private static String[]   prxTypes;

    private static SSHPropertyHandler propsHandler;

    public static void show(String title, Frame parent,
			    SSHPropertyHandler props) {
	propsHandler = props;
	if(proxyDialog == null) {
	    prxTypes = SSH.getProxyTypes();

	    proxyDialog = new Dialog(parent, title, true);

	    Label              lbl;
	    GridBagLayout      grid  = new GridBagLayout();
	    GridBagConstraints gridc = new GridBagConstraints();
	    proxyDialog.setLayout(grid);
	    Button             b;
	    ItemListener       il;

	    gridc.fill   = GridBagConstraints.HORIZONTAL;
	    gridc.anchor = GridBagConstraints.WEST;
	    gridc.gridwidth = 1;
	    gridc.insets = new Insets(4, 4, 0, 4);

	    gridc.gridy = 0;
	    lbl = new Label("Proxy type:");
	    gridc.gridwidth = 2;
	    grid.setConstraints(lbl, gridc);
	    proxyDialog.add(lbl);
	    choicePrxType = new Choice();
	    for(int i = 0; i < prxTypes.length; i++) {
		choicePrxType.add(prxTypes[i]);
	    }
	    grid.setConstraints(choicePrxType, gridc);
	    proxyDialog.add(choicePrxType);
	    choicePrxType.addItemListener(il = new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if(e.getSource() == choicePrxType) {
			textPrxPort.setText(String.valueOf(SSH.defaultProxyPorts[SSH.getProxyType(choicePrxType.getSelectedItem())]));
		    }
		    updateFromType();
		}
	    });

	    gridc.gridy = 1;
	    lbl = new Label("Server:");
	    grid.setConstraints(lbl, gridc);
	    proxyDialog.add(lbl);
	    gridc.gridwidth = 4;
	    textPrxHost = new TextField("", 16);
	    grid.setConstraints(textPrxHost, gridc);
	    proxyDialog.add(textPrxHost);
	    gridc.gridwidth = 1;
	    lbl = new Label("Port:");
	    grid.setConstraints(lbl, gridc);
	    proxyDialog.add(lbl);
	    textPrxPort = new TextField("", 4);
	    grid.setConstraints(textPrxPort, gridc);
	    proxyDialog.add(textPrxPort);

	    gridc.gridy = 2;
	    gridc.gridwidth = 2;
	    lbl = new Label("Username:");
	    grid.setConstraints(lbl, gridc);
	    proxyDialog.add(lbl);
	    textPrxUser = new TextField("", 10);
	    grid.setConstraints(textPrxUser, gridc);
	    proxyDialog.add(textPrxUser);
	    lbl = new Label("Password:");
	    grid.setConstraints(lbl, gridc);
	    proxyDialog.add(lbl);
	    textPrxPasswd = new TextField("", 10);
	    textPrxPasswd.setEchoChar('*');
	    grid.setConstraints(textPrxPasswd, gridc);
	    proxyDialog.add(textPrxPasswd);

	    gridc.gridy = 3;
	    gridc.gridwidth = 4;
	    cbNeedAuth = new Checkbox("Need authentication");
	    grid.setConstraints(cbNeedAuth, gridc);
	    proxyDialog.add(cbNeedAuth);
	    cbNeedAuth.addItemListener(il);

	    Panel bp = new Panel(new FlowLayout());
	    bp.add(b = new Button("OK"));
	    b.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			String prxTypeStr = choicePrxType.getSelectedItem();
			propsHandler.setProperty("proxytype", prxTypeStr);
			if(!"none".equalsIgnoreCase(prxTypeStr)) {
			    propsHandler.setProperty("proxyhost", textPrxHost.getText());
			    propsHandler.setProperty("proxyport", textPrxPort.getText());
			}
			if(cbNeedAuth.getState()) {
			    propsHandler.setProperty("proxyuser", textPrxUser.getText());
			    propsHandler.setProperty("prxpassword", textPrxPasswd.getText());
			} else if("socks4".equals(prxTypeStr)) {
			    propsHandler.setProperty("proxyuser", textPrxUser.getText());
			}
			proxyDialog.setVisible(false);
		    } catch (Exception ee) {
			// !!!
		    }
		}
	    });
	    bp.add(b = new Button("Cancel"));
	    b.addActionListener(new AWTConvenience.CloseAction(proxyDialog));

	    gridc.gridy = 4;
	    gridc.anchor = GridBagConstraints.CENTER;
	    gridc.fill = GridBagConstraints.NONE;
	    gridc.gridwidth = GridBagConstraints.REMAINDER;
	    grid.setConstraints(bp, gridc);
	    proxyDialog.add(bp);

	    proxyDialog.addWindowListener(new AWTConvenience.CloseAdapter(b));

	    AWTConvenience.setBackgroundOfChildren(proxyDialog);

	    proxyDialog.setResizable(true);
	    proxyDialog.pack();
	}

	proxyDialog.setTitle(title);

	String prxType = propsHandler.getProperty("proxytype");
	choicePrxType.select(prxType);
	String prxUser = propsHandler.getProperty("proxyuser");
	boolean needAuth = (prxUser != null && (prxUser.trim().length() > 0));
	cbNeedAuth.setState(needAuth);
	textPrxHost.setText(propsHandler.getProperty("proxyhost"));
	textPrxPort.setText(propsHandler.getProperty("proxyport"));
	textPrxUser.setText(propsHandler.getProperty("proxyuser"));

	updateFromType();

	AWTConvenience.placeDialog(proxyDialog);

	proxyDialog.setVisible(true);
    }

    private static void updateFromType() {
	boolean proxyEnable = false;
	boolean authEnable  = false;
	String  proxyType   = choicePrxType.getSelectedItem();
	int     type        = 0;

	try {
	    type = SSH.getProxyType(proxyType);
	    switch(type) {
	    case SSH.PROXY_NONE:
		break;
	    case SSH.PROXY_HTTP:
	    case SSH.PROXY_SOCKS5_DNS:
	    case SSH.PROXY_SOCKS5_IP:
		authEnable = true;
		// Fall through
	    case SSH.PROXY_SOCKS4:
		proxyEnable = true;
		break;
	    }
	} catch (Exception ee) {
	    // !!!
	}
	textPrxHost.setEnabled(proxyEnable);
	textPrxPort.setEnabled(proxyEnable);
	cbNeedAuth.setEnabled(authEnable);

	if(!authEnable)
	    cbNeedAuth.setState(false);

	boolean needAuth = cbNeedAuth.getState();

	textPrxUser.setEnabled(needAuth);
	textPrxPasswd.setEnabled(needAuth);

	if(proxyEnable) {
	    if(textPrxHost.getText().length() == 0)
		textPrxHost.setText(propsHandler.getProperty("proxyhost"));
	    if(textPrxPort.getText().length() == 0)
		textPrxPort.setText(propsHandler.getProperty("proxyport"));
	} else {
	    textPrxHost.setText("");
	    textPrxPort.setText("");
	}

	if(needAuth) {
	    if(textPrxUser.getText().length() == 0)
		textPrxUser.setText(propsHandler.getProperty("proxyuser"));
	} else {
	    textPrxUser.setText("");
	    textPrxPasswd.setText("");
	}

	if(type == SSH.PROXY_SOCKS4) {
	    textPrxUser.setEnabled(true);
	    String user = propsHandler.getProperty("proxyuser");
	    if(textPrxUser.getText().length() == 0) {
		if(user == null)
		    user = "anonymous";
		textPrxUser.setText(user);
	    }
	}

	if(proxyEnable)
	    textPrxHost.requestFocus();
    }
}
