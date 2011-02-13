//
//  Copyright (C) 1997, 1998 Olivetti & Oracle Research Laboratory
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//
package mindbright.vnc;

import java.awt.*;

//
// The panel which implements the user authentication scheme
//

public class authenticationPanel extends Panel {

  Label title, retry, prompt;
  public TextField sshUser;
  public TextField sshPassword;
  public TextField vncHost;
  public TextField vncPassword;
  Button ok;

  //
  // Constructor.
  //

  public authenticationPanel() {

    title = new Label("SSH/VNC Authentication",Label.CENTER);
    title.setFont(new Font("Helvetica", Font.BOLD, 18));

    sshUser = new TextField(10);
    sshUser.setForeground(Color.black);
    sshUser.setBackground(Color.white);

    sshPassword = new TextField(10);
    sshPassword.setForeground(Color.black);
    sshPassword.setBackground(Color.white);
    sshPassword.setEchoCharacter('*');

    vncHost = new TextField(10);
    vncHost.setForeground(Color.black);
    vncHost.setBackground(Color.white);

    vncPassword = new TextField(10);
    vncPassword.setForeground(Color.black);
    vncPassword.setBackground(Color.white);
    vncPassword.setEchoCharacter('*');

    ok = new Button("Connect");

    retry = new Label("",Label.CENTER);
    retry.setFont(new Font("Courier", Font.BOLD, 12));

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gridbag);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.CENTER;
    gridbag.setConstraints(title,gbc);
    add(title);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gridbag.setConstraints(retry,gbc);
    add(retry);

    gbc.fill   = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridwidth = 1;

    gbc.gridy = 2;
    prompt    = new Label("SSH User",Label.CENTER);
    gridbag.setConstraints(prompt,gbc);
    add(prompt);
    gridbag.setConstraints(sshUser,gbc);
    add(sshUser);

    gbc.gridy = 3;
    prompt    = new Label("SSH Password",Label.CENTER);
    gridbag.setConstraints(prompt,gbc);
    add(prompt);
    gridbag.setConstraints(sshPassword,gbc);
    add(sshPassword);

    gbc.gridy = 4;
    prompt    = new Label("VNC Host [:display]",Label.CENTER);
    gridbag.setConstraints(prompt,gbc);
    add(prompt);
    gridbag.setConstraints(vncHost,gbc);
    add(vncHost);

    gbc.gridy = 5;
    prompt    = new Label("VNC Password",Label.CENTER);
    gridbag.setConstraints(prompt,gbc);
    add(prompt);
    gridbag.setConstraints(vncPassword,gbc);
    add(vncPassword);

    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridy = 6;
    gbc.insets = new Insets(5,5,5,5);
    gridbag.setConstraints(ok,gbc);
    add(ok);

    sshUser.requestFocus();
  }

  //
  // action() is called when a button is pressed or return is pressed in the
  // password text field.
  //

  public synchronized boolean action(Event evt, Object arg) {
    if ((evt.target == vncPassword) || (evt.target == ok)) {
	if(sshUser.getText().length() != 0     &&
	   sshPassword.getText().length() != 0 &&
	   vncHost.getText().length() != 0     &&
	   vncPassword.getText().length() != 0) {
	    retry.setText("Connecting...");
	    notify();
	} else {
	  retry.setText("Please fill in all fields");
	}
	return true;
    }
    return false;
  }

  //
  // retry().
  //

  public void retry(String reason) {
    retry.setText(reason);
  }

}
