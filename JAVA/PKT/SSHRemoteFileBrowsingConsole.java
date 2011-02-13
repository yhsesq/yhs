/******************************************************************************
 *
 * Copyright (c) 2001 by ISNetworks, Seattle, WA.
 *                       www.isnetworks.com, info@isnetworks.com
 * Based on MindTerm from Mindbright Technology AB, Stockholm, Sweden.
 *                        www.mindbright.se, info@mindbright.se
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
 *****************************************************************************/

/**
* Browser for files on remote machine.  When initialized it kicks up a new
* SSH connection to the remote machine.  The file list is populated using 
* results from the "ls" command and manipulations are done with simple
* shell commands.  The response to each command is also processed by this
* class and the GUI is updated asynchronously
*/

package mindbright.ssh;

import java.io.*;
import java.util.*;

import mindbright.terminal.Terminal;
import mindbright.security.Cipher;
import com.isnetworks.ssh.*;
import java.awt.event.*;

public class SSHRemoteFileBrowsingConsole extends AbstractFileBrowser implements SSHConsole {

	/** Connection to remote machine */
	private SSHClient mClient;

	/** Response from remote machine's standard out stream */
	private String mOutput = "";

	/** Name of current directory on remote machine */
	private String mCurrentDirectory;

	/** Place to report errors to */
	private SSHSCPDialog mErrorLog;

	public SSHRemoteFileBrowsingConsole( FileDisplay fileDisplay, SSHPropertyHandler propertyHandler, SSHSCPDialog errorLog ) {
		super(  fileDisplay, propertyHandler );
		mErrorLog = errorLog;
	}

	/**
	* Kick up a new connection to the remote machine, killing the current
	* one if it's still active
	*/
	public void initialize() throws SSHException {
		try {
			disconnect();
		    mClient = new SSHClient( mPropertyHandler, new SSHClientUserAdaptor( mPropertyHandler.getSrvHost(), mPropertyHandler.getSrvPort() ) );//SimpleClientUser( mPropertyHandler.getSrvHost(), mPropertyHandler.getSrvPort() ) );
		    mClient.setConsole( this );
			mClient.activateTunnels = false;
			mClient.bootSSH( false );
			refresh();
		}
		catch( IOException e ) {
			throw new SSHException( "Error when connecting with remote machine" );
		}
	}

	/**
	* Shut down the connection to the remote machine if it's active
	*/
	public void disconnect() {
		if ( mClient != null && mClient.isConnected() ) {
		    mClient.forcedDisconnect();
		}
	}
	
	/**
	* Rather ugly way to get the current directory on the server and a list
	* of files
	*/
	public void refresh() throws SSHException {
		try {
			StringBuffer command = new StringBuffer();
			command.append( "echo ****START PWD****\n" );
			command.append( "pwd\n" );
			command.append( "echo ****END PWD****\n" );
			command.append( "ls -A -L -p -1\n" );
			command.append( "echo ****END LS****\n" );
			mClient.stdinWriteString( command.toString() );	
		}
		catch( IOException e ) {
			throw new SSHException( "Error sending command to remote machine" );
		}
	}
	
	/**
	* Executes a "mkdir" on the remote machine
	*/
	public void makeDirectory( String directoryName ) throws SSHException {
		try {
			StringBuffer command = new StringBuffer();
			command.append( "mkdir \"" );
			command.append( directoryName );
			command.append( "\"\n" );
			mClient.stdinWriteString( command.toString() );	
			
			refresh();
		}
		catch( IOException e ) {
			throw new SSHException( "Error sending command to remote machine" );
		}
	}
	
	/**
	* Executes a "mv" on the remote machine
	*/
	public void rename( FileListItem oldFile, String newName ) throws SSHException {
		try {
			StringBuffer command = new StringBuffer();
			command.append( "mv \"" );
			command.append( oldFile.getAbsolutePath() );
			command.append( "\" \"" );
			command.append( oldFile.getParent() );
			command.append( newName );
			command.append( "\"\n" );

			mClient.stdinWriteString( command.toString() );	
			
			refresh();
		}
		catch( IOException e ) {
			throw new SSHException( "Error sending command to remote machine" );
		}	
	}
	
	/**
	* Does a "cd" on the remote machine
	*/
	public void changeDirectory( String directoryName ) throws SSHException {
		try {
			StringBuffer command = new StringBuffer();
			command.append( "cd \"" );
			command.append( directoryName );
			command.append( "\"\n" );
			mClient.stdinWriteString( command.toString() );	
			
			refresh();
		}
		catch( IOException e ) {
			throw new SSHException( "Error sending command to remote machine" );
		}		
	}
	
	/**
	* Does a "rmdir" for directories in the array and a "rm" for files
	* Will not delete non-empty directories
	*/
	public void delete( FileListItem[] fileListItem ) throws SSHException {
		try {
			StringBuffer command = new StringBuffer();

			for( int i = 0; i < fileListItem.length; i++ ) {
				if ( fileListItem[ i ].isDirectory() ) {
					command.append( "rmdir \"" );
				}
				else {
					command.append( "rm -f \"" );
				}
				command.append( fileListItem[ i ].getAbsolutePath() );
				command.append( "\"\n" );
			}

			mClient.stdinWriteString( command.toString() );	
			
			refresh();
		}
		catch( IOException e ) {
			throw new SSHException( "Error sending command to remote machine" );
		}
	}
	
	public Terminal getTerminal() {
		return null;
	}

	/**
	* Collect output from the remote machine and parse it into a directory
	* listing when enough has built up.  May not work if directory listing
	* from "ls" is not of the exact form that's expected
	*/
	public void stdoutWriteString( byte[] str ) {
		mOutput = mOutput + new String( str );
		
		// Check if there's enough output to parse yet
		while ( mOutput.indexOf( "****END LS****" ) != -1 ) {
			StringTokenizer st = new StringTokenizer( mOutput, "\n" );
			
			//  Ignore "****START PWD****"
			String line = st.nextToken();

			mCurrentDirectory = st.nextToken();
			if ( !mCurrentDirectory.endsWith( "/" ) ) {
				mCurrentDirectory += "/";
			}

			//  Ignore "****END PWD****"
			line = st.nextToken();

			Vector v = new Vector();

			if ( !mCurrentDirectory.equals( "/" ) ) {
				v.addElement( new FileListItem( "..", mCurrentDirectory, true ) );
			}

			// Parse all the file listings, knowing we're done when we hit a "****"
			while ( !( line = st.nextToken() ).startsWith( "****" ) ) {
				boolean directory = line.endsWith( "/" );
				String name = line;
				if ( directory ) {
					name = line.substring( 0, line.length() - 1 );
				}
				v.addElement( new FileListItem( name, mCurrentDirectory, directory ) );
			}			

			// Sort the array
			FileListItem.sort( v );
			mFileDisplay.setFileList( v, mCurrentDirectory );
			
			// Delete the output we've already consumed
			mOutput = mOutput.substring( mOutput.indexOf( "****END LS****" ) + "****END LS****".length() );
		}
	}

	/**
	* An error occurred when executing a server command, report it
	* to the user
	*/
	public void stderrWriteString(byte[] str) {
		String errorMessage = new String( str );
		mErrorLog.logError( new SSHException( "Error: " + errorMessage.trim() ) );
	}

	public void print(String str) {
	}

	public void println(String str) {
	}

	public void serverConnect(SSHChannelController controller, Cipher sndCipher) {
		//    realConsole.serverConnect(controller, sndCipher);
	}

	public void serverDisconnect(String reason) {
		//    realConsole.serverDisconnect(reason);
	}
	
	/**
	* User double clicked on a file in the list.  Check if it's a directory
	* and change to it if it is.
	*/
	public void fileDoubleClicked( FileListItem fileListItem ) throws SSHException {
		if ( fileListItem.isDirectory() ) {
			try {
				StringBuffer command = new StringBuffer();
				command.append( "cd \"" );
				command.append( fileListItem.getAbsolutePath() );
				command.append( "\"\n" );
				
				mClient.stdinWriteString( command.toString() );
				
				refresh();
			}
			catch( IOException ex ) {
				throw new SSHException( "Error sending command to remote machine" );
			}
		}	
	}
}
