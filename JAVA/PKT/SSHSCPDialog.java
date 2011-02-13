/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 * HEAVILY modified by ISNetworks, Seattle, WA.
 *                     www.isnetworks.com, info@isnetworks.com
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
 * $Date: 2001/02/13 22:06:34 $
 * $Name:  $
 *****************************************************************************/

/**
* This class has been completely rewritten from the original.  Its design
* is based on the LayoutManager tutorial at Sun's Java web site available at
* http://developer.java.sun.com/developer/onlineTraining/GUI/AWTLayoutMgr/shortcourse.html
*
* It implements a WS-FTP-like interface for browsing, manipulating, and transferring
* files in a secure way over SSH.  It logs into the remote machine using the same
* account and authentication method as the user used to log in.  
* Unfortunately, that means if you need root file access you'll need to log in as
* root through SSH
*/

package mindbright.ssh;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import com.isnetworks.ssh.*;

public class SSHSCPDialog extends Dialog {

	private Button        mUploadButton;
	private Button        mDownloadButton;

	private Panel         mFileDisplayPanel;
	private Panel         mMainBottomSectionPanel;

	/** Text area for error message display */
	private TextArea      mMessageTextArea;

	/** GUI for browsing file systems */
	private FileDisplay   mLocalFileDisplay;
	private FileDisplay   mRemoteFileDisplay;

	/** Back end for browsing file systems */
	private SSHRemoteFileBrowsingConsole mRemoteFileBrowser;
	private LocalFileBrowser mLocalFileBrowser;

	/** Frame to attach new dialog boxes to */
	private Frame mOwnerFrame;

	private SSHPropertyHandler mPropertyHandler;
	private SSHInteractor mInterator;


  /** Constructor 
   *  Overall, the GUI is composed of two parts:
   *    the bottom section (buttons, messages)
   *    the file-display section (two file displays & arrow buttons)
   */
	private SSHSCPDialog( Frame owner, SSHPropertyHandler propHandler, SSHInteractor interactor ) {
		super( owner, "SCP Files", false );
	
		mOwnerFrame = owner;
	
		mPropertyHandler = propHandler;
		mInterator = interactor;

		setLayout(new BorderLayout());
		setBackground(Color.lightGray);
		add("South",  getMainBottomSectionPanel());
		add("Center", getFileDisplayPanel());
		pack();

		mRemoteFileBrowser = new SSHRemoteFileBrowsingConsole( getRemoteFileDisplay(), mPropertyHandler, this );
		mLocalFileBrowser = new LocalFileBrowser( getLocalFileDisplay(), mPropertyHandler );

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				mRemoteFileBrowser.disconnect();			
			}});
	}


  /**
  * The main bottom part of the GUI.  Now just contains the error text area
  */
  private Panel getMainBottomSectionPanel() {
    if (mMainBottomSectionPanel == null) {
      mMainBottomSectionPanel = new Panel(new BorderLayout());
      getMainBottomSectionPanel().add("Center", getMessageTextArea());
    }
    return mMainBottomSectionPanel;
  }


  /** This is the message text area in the bottom part of the GUI.
   *  It is sized to 3 rows, 30 columns, which drives its preferred
   *  size (the preferred height is respected due to its position in
   *  the GUI.
   */
	private TextArea getMessageTextArea() {
		if (mMessageTextArea == null) {
			mMessageTextArea = new TextArea( "", 3, 30, TextArea.SCROLLBARS_VERTICAL_ONLY );
			mMessageTextArea.setEditable( false );
			mMessageTextArea.setFont( new Font( "Monospaced", Font.PLAIN, 11 ) );
			mMessageTextArea.setBackground(Color.white);
		}
		return mMessageTextArea;
	}


  /** This is the upper section of the GUI, containing the
   *  local & remote file displays and the direction buttons
   *  It is a big-bad-evil GridBagLayout (tm)
   *  The general idea is that the file displays expand
   *  horizontally to fill the remaining space equally and
   *  the arrow buttons float in the center between the
   *  two file displays.
   */
  private Panel getFileDisplayPanel() {
    if (mFileDisplayPanel == null) {
      mFileDisplayPanel = new Panel(new GridBagLayout());

      GridBagConstraints gbc = new GridBagConstraints();
 
      gbc.gridx      = 0;
      gbc.gridy      = 0;
      gbc.gridwidth  = 1;
      gbc.gridheight = 2;
      gbc.fill       = GridBagConstraints.BOTH;
      gbc.anchor     = GridBagConstraints.CENTER;
      gbc.weightx    = 0.5;
      gbc.weighty    = 1.0;
      mFileDisplayPanel.add(getLocalFileDisplay(), gbc);

      gbc.gridx      = 2;
      gbc.gridy      = 0;
      gbc.gridwidth  = 1;
      gbc.gridheight = 2;
      gbc.fill       = GridBagConstraints.BOTH;
      gbc.anchor     = GridBagConstraints.CENTER;
      gbc.weightx    = 0.5;
      gbc.weighty    = 1.0;
      mFileDisplayPanel.add(getRemoteFileDisplay(), gbc);

      gbc.gridx      = 1;
      gbc.gridy      = 0;
      gbc.gridwidth  = 1;
      gbc.gridheight = 1;
      gbc.fill       = GridBagConstraints.NONE;
      gbc.anchor     = GridBagConstraints.SOUTH;
      gbc.weightx    = 0.0;
      gbc.weighty    = 0.5;
      gbc.insets     = new Insets(0, 4, 2, 4);
      mFileDisplayPanel.add(getDownloadButton(), gbc);

      gbc.gridx      = 1;
      gbc.gridy      = 1;
      gbc.gridwidth  = 1;
      gbc.gridheight = 1;
      gbc.fill       = GridBagConstraints.NONE;
      gbc.anchor     = GridBagConstraints.NORTH;
      gbc.weightx    = 0.0;
      gbc.weighty    = 0.5;
      gbc.insets     = new Insets(2, 4, 0, 4);
      mFileDisplayPanel.add(getUploadButton(), gbc);
    }
    return mFileDisplayPanel;
  }

  /** An instance of FileDisplay for the local system */
  private FileDisplay getLocalFileDisplay() {
    if (mLocalFileDisplay == null) {
      mLocalFileDisplay = new FileDisplay( mOwnerFrame, "Local System", this );
    }
    return mLocalFileDisplay;
  }

  /** An instance of FileDisplay for the remote system */
  private FileDisplay getRemoteFileDisplay() {
    if (mRemoteFileDisplay == null) {
      mRemoteFileDisplay = new FileDisplay( mOwnerFrame, "Remote System", this );
    }
    return mRemoteFileDisplay;
  }

	/** A direction button pointing left */
	private Button getDownloadButton() {
		if (mDownloadButton == null) {
			mDownloadButton = new Button("<--");
			mDownloadButton.addActionListener( new ActionListener() {

				public void actionPerformed( ActionEvent e ) {

				    try {
						String  srvHost    = mPropertyHandler.getSrvHost();
						int     srvPort    = mPropertyHandler.getSrvPort();
						String  localFN    = "";
						
						String remoteFN = "";
						FileListItem[] selectedItems = mRemoteFileDisplay.getSelectedFiles();
						for( int i = 0; i < selectedItems.length; i++ ) {
							remoteFN += "\"" + getRemoteFileDisplay().getFileSystemLocationLabelText() + selectedItems[ i ].getName() + "\" ";
							localFN  += "\"" + selectedItems[ i ].getName() + "\" ";
						}
						remoteFN = remoteFN.trim();

						String  curDir     = getLocalFileDisplay().getFileSystemLocationLabelText();
						boolean recursive  = true;
						boolean background = false;

						SSHSCPGUIThread progress =
						    new SSHSCPGUIThread(srvHost, srvPort,
								mPropertyHandler,
								mPropertyHandler, mInterator,
								mOwnerFrame, curDir,
								localFN, remoteFN,
								recursive,
								background, false, SSHSCPDialog.this );
						if(mindbright.ssh.SSH.NETSCAPE_SECURITY_MODEL) {
						    try {
							netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
							netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
						    } catch (netscape.security.ForbiddenTargetException ex) {
							ex.printStackTrace();
							// !!!
						    }
						}
						Thread thread = new Thread( SSH.getThreadGroup(), progress, SSH.createThreadName() );
						thread.start();
				    } catch (Exception ee) {
						ee.printStackTrace();
						logError( ee );
				    }
				}			
			} );
		}
		return mDownloadButton;
	}

	/** A direction button pointing right */
	private Button getUploadButton() {
		if (mUploadButton == null) {
			mUploadButton = new Button("-->");
			mUploadButton.addActionListener( new ActionListener() {

				public void actionPerformed( ActionEvent e ) {

				    try {
						FileListItem file = getLocalFileDisplay().getSelectedFile();
						
						String  srvHost    = mPropertyHandler.getSrvHost();
						int     srvPort    = mPropertyHandler.getSrvPort();

						String  localFN    = "";
						FileListItem[] selectedItems = mLocalFileDisplay.getSelectedFiles();
						for( int i = 0; i < selectedItems.length; i++ ) {
							localFN += "\"" + selectedItems[ i ].getName() + "\" ";
						}
						localFN = localFN.trim();

						String  remoteFN   = "\"" + getRemoteFileDisplay().getFileSystemLocationLabelText() + "\"";

						String  curDir     = getLocalFileDisplay().getFileSystemLocationLabelText();
						boolean recursive  = true;
						boolean background = false;

						SSHSCPGUIThread progress =
						    new SSHSCPGUIThread(srvHost, srvPort,
								mPropertyHandler,
								mPropertyHandler, mInterator,
								mOwnerFrame, curDir,
								localFN, remoteFN,
								recursive,
								background, true, SSHSCPDialog.this );
						if(mindbright.ssh.SSH.NETSCAPE_SECURITY_MODEL) {
						    try {
							netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
							netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
						    } catch (netscape.security.ForbiddenTargetException ex) {
							ex.printStackTrace();
							// !!!
						    }
						}
						Thread thread = new Thread( SSH.getThreadGroup(), progress, SSH.createThreadName() );
						thread.start();
				    } catch (Exception ee) {
						logError( ee );
				    }
				}			
			} );
		}
		return mUploadButton;
	}

	public void refresh() {
		try {
			mRemoteFileBrowser.refresh();
			mLocalFileBrowser.refresh();
		}
		catch( SSHException e ) {
			logError( e );
		}
	}

	
	/**
	* Initialize the connection to the remote system and
	* start in the SSH home directory on the local system
	*/
	public void show( SSHPropertyHandler propsHandler ) {

		if(SSH.NETSCAPE_SECURITY_MODEL) {
			try {
				netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
				netscape.security.PrivilegeManager.enablePrivilege("TerminalEmulator");
			} catch (netscape.security.ForbiddenTargetException e) {
			// !!!
			}
		}


		setSize( 600, 500 );
		try {
			mRemoteFileBrowser.initialize();
			mLocalFileBrowser.initialize();
		}
		catch( SSHException e ) {
			logError( e );
		}
		super.show();
	}

	/**
	* An exception happened, so show the user the message in the text area
	*/
	public void logError( Exception e ) {
		mMessageTextArea.append( e.getMessage() + "\n" );
	}

	/**
	* Method signature from original MindTerm dialog box
	*/
    public static void show(String title, Frame p, SSHPropertyHandler props, SSHInteractor itor) {
		SSHSCPDialog dialog = new SSHSCPDialog( p, props, itor );
		dialog.show( props );
    }
}
