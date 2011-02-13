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

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Hashtable;
import java.util.Properties;
import java.util.NoSuchElementException;
import java.util.Enumeration;

import mindbright.net.*;
import mindbright.terminal.*;
import mindbright.security.*;
import mindbright.util.EncryptedProperties;


public final class SSHPropertyHandler implements SSHClientUser, SSHAuthenticator, ProxyAuthenticator {

    static public final int PROP_NAME    = 0;
    static public final int PROP_VALUE   = 1;
    static public final int PROP_DESC    = 2;
    static public final int PROP_ALLOWED = 3;

    static public final String PROPS_FILE_EXT  = ".mtp";
    static public final String GLOB_PROPS_FILE = "mindterm" + PROPS_FILE_EXT;
    static public final String DEF_IDFILE = "identity";

    static public final Properties defaultProperties    = new Properties();
    static public final Hashtable  defaultPropertyNames = new Hashtable();
    static public final String[][] defaultPropDesc = {
	{ "server",   null,           "name of server to connect to", "" },
	{ "realsrv",  null,           "real address of sshd if it is behind a firewall", "" },
	{ "localhst", "0.0.0.0",      "address to use as localhost", "" },
	{ "port",     String.valueOf(SSH.DEFAULTPORT),
	  "port on server to connect to", "" },
	{ "proxytype",  "none",       "type of proxy server to connect through", SSH.listSupportedProxyTypes() },
	{ "proxyhost",  null,         "name of proxy server to connect through", "" },
	{ "proxyport",  null,         "port on proxy server to connect through", "" },
	{ "proxyuser",  null,         "username for authentication on proxy server", "" },
	{ "proxyproto",  null,        "protocol for proxy connection (e.g. 'http://')", "" },
	{ "usrname",  null,           "username to login as", "" },
	{ "password",  null,          "password for normal authentication", "" },
	{ "tispassword",  null,       "password for TIS authentication", "" },
	{ "rsapassword",  null,       "password for RSA authentication (key file)", "" },
	{ "prxpassword",  null,       "password for proxy authentication", "" },
	{ "cipher",   SSH.getCipherName(SSH.CIPHER_DEFAULT),
	  "name of block cipher to use",
	  ("( " + SSH.listSupportedCiphers() + ")") },
	{ "authtyp",  "passwd",      "method of authentication",
	  ("( " + SSH.listSupportedAuthTypes() + ")") },
	{ "idfile",   DEF_IDFILE,    "name of file containing identity (rsa key)", "" },
	{ "display",  "localhost:0", "local display definition (i.e. <host>:<screen>)", "" },
	{ "mtu",      "0",           "maximum packet size to use (0 means use default)",
	  "(4096 - 256k)" },
	{ "escseq",   "~$",          "sequence of characters to type to enter local command shell", "" },
	{ "secrand",  "0",           "level of security in random seed (for generating session key)",
	  "(0-2, 0=low and 2=high)" },
	{ "alive",    "0",           "Connection keep-alive interval in seconds (0 means none)", "(0-600)" },
	{ "compression",    "0",     "Compression Level (0 means none, 1=fast, 9=slow,best )", "(0-9)" },
	{ "x11fwd",   "false",       "indicates whether X11 display is forwarded or not", "(true/false)" },
	{ "prvport",  "false",       "indicates whether to use a privileged port or not (locally)", "(true/false)" },
	{ "forcpty",  "true",        "indicates whether to allocate a pty or not", "(true/false)" },
	{ "remfwd",   "false",       "indicates whether we allow remote connects to local forwards", "(true/false)" },
	{ "idhost",   "true",        "indicates whether to check host's host key in 'known_hosts'", "(true/false)" },
	{ "portftp",  "false",       "indicates whether to enable ftp 'PORT' command support", "(true/false)" },
    };

    static {
	for(int i = 0; i < defaultPropDesc.length; i++) {
	    String name  = defaultPropDesc[i][PROP_NAME];
	    String value = defaultPropDesc[i][PROP_VALUE];
	    defaultPropertyNames.put(name, "");
	    if(value != null)
		defaultProperties.put(name, value);
	}
    }

    String        sshHomeDir;
    String        knownHosts;
    SSHRSAKeyFile keyFile;

    SSHClient           client;
    SSHInteractor       interactor;
    EncryptedProperties props;
    boolean             activeProps;

    protected String currentPropsFile;
    protected String currentAlias;

    boolean autoSaveProps;
    boolean autoLoadProps;
    boolean savePasswords;
    boolean readonly;

    private String propertyPassword;

    public Properties initTermProps;

    protected boolean propsChanged;

    public SSHPropertyHandler(Properties initProps) {
	this.knownHosts = SSH.KNOWN_HOSTS_FILE;

	setProperties(initProps);

	this.activeProps  = false;
	this.propsChanged = false;
    }

    public SSHPropertyHandler(SSHPropertyHandler clone) {
	this(clone.props);
	this.sshHomeDir       = clone.sshHomeDir;
	this.keyFile          = clone.keyFile;
	this.initTermProps    = clone.initTermProps;
	this.propertyPassword = clone.propertyPassword;
	this.readonly         = true;
    }

    public static SSHPropertyHandler fromFile(String fileName, String password) throws IOException {
	SSHPropertyHandler fileProps = new SSHPropertyHandler(new Properties());
	fileProps.setPropertyPassword(password);
	fileProps.loadAbsoluteFile(fileName, false);
	return fileProps;
    }

    public void setInteractor(SSHInteractor interactor) {
	this.interactor = interactor;
    }

    public void setClient(SSHClient client) {
	this.client = client;
    }

    public void setAutoLoadProps(boolean value) {
	if(sshHomeDir != null)
	    autoLoadProps = value;
    }

    public void setAutoSaveProps(boolean value) {
	if(sshHomeDir != null)
	    autoSaveProps = value;
    }

    public void setSavePasswords(boolean value) {
	savePasswords = value;
    }

    public void setReadOnly(boolean value) {
	readonly = value;
    }

    public boolean isReadOnly() {
	return readonly;
    }

    public void setPropertyPassword(String password) {
	if(password != null)
	    this.propertyPassword = password;
    }

    public boolean emptyPropertyPassword() {
	return propertyPassword == null;
    }

    public void setSSHHomeDir(String sshHomeDir) {
	if(sshHomeDir == null || sshHomeDir.trim().length() == 0) {
	    return;
	}

	if(sshHomeDir != null && !sshHomeDir.endsWith(File.separator))
	    sshHomeDir += File.separator;

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	try {
	    // sshHomeDir always ends with a trailing File.separator. Strip before we
	    // try to create it (some platforms don't like ending 'separator' in name)
	    //
	    File sshDir = new File(sshHomeDir.substring(0, sshHomeDir.length() - 1));
	    if(!sshDir.exists()) {
		if(interactor.askConfirmation("MindTerm home directory: '" + sshHomeDir +
					      "' does not exist, create it?", true)) {
		    try {
			sshDir.mkdir();
		    } catch (Throwable t) {
			interactor.alert("Could not create home directory, file operations disabled.");
			sshHomeDir = null;
		    }
		} else {
		    interactor.report("No home directory, file operations disabled.");
		    sshHomeDir = null;
		}
	    }
	} catch (Throwable t) {
	    if(interactor != null && interactor.isVerbose())
		interactor.report("Can't access local file system, file operations disabled.");
	    sshHomeDir = null;
	}
	this.sshHomeDir = sshHomeDir;
	if(this.sshHomeDir == null) {
	    autoSaveProps = false;
	    autoLoadProps = false;
	}

	if(interactor != null)
	    interactor.propsStateChanged(this);
    }

    public String getSSHHomeDir() {
	return sshHomeDir;
    }

    //
    // Methods delegated to Properties and other property related methods
    //
    public static boolean isProperty(String key) {
	return defaultPropertyNames.containsKey(key) ||
	    (key.indexOf("local") == 0) || (key.indexOf("remote") == 0);
    }

    public String getProperty(String key) {
	return props.getProperty(key);
    }

    public void setProperty(String key, String value)
	throws IllegalArgumentException, NoSuchElementException
    {
	if(value == null)
	    return;

	boolean equalProp  = !(value.equals(getProperty(key)));

	validateProperty(key, value);

	if(activeProps)
	    activateProperty(key, value);

	if(equalProp) {
	    if(interactor != null)
		interactor.propsStateChanged(this);
	    propsChanged = equalProp;
	}

	props.put(key, value);
    }

    final void validateProperty(String key, String value)
	throws IllegalArgumentException, NoSuchElementException {
	//
	// Some sanity checks...
	//
	if(key.equals("cipher")) {
	    if(SSH.getCipherType(value) == SSH.CIPHER_NOTSUPPORTED)
		throw new IllegalArgumentException("Cipher " + value + " not supported");
	    //
	} else if(key.equals("authtyp")) {
	    SSH.getAuthTypes(value);
	    //
	} else if(key.equals("x11fwd")  || key.equals("prvport") ||
		  key.equals("forcpty") || key.equals("remfwd")  ||
		  key.equals("idhost")  || key.equals("portftp")) {
	    if(!(value.equals("true") || value.equals("false")))
		throw new IllegalArgumentException("Value for " + key + " must be 'true' or 'false'");
	    //
	} else if(key.equals("port") || key.equals("proxyport") || key.equals("mtu") || key.equals("compression") ||
		  key.equals("secrand") || key.equals("alive")) {
	    try {
		int val = Integer.valueOf(value).intValue();
		if((key.equals("port") || key.equals("proxyport")) && (val > 65535 || val < 0)) {
		    throw new IllegalArgumentException("Not a valid port number: " + value);
		} else if(key.equals("mtu") && val != 0 && (val > (256*1024) || val < 4096)) {
		    throw new IllegalArgumentException("Mtu must be between 4k and 256k");
		} else if(key.equals("alive")) {
		    if(val < 0 || val > 600)
			throw new IllegalArgumentException("Alive interval must be 0-600");
		} else if(key.equals("secrand")) {
		    if(val < 0 || val > 2)
			throw new IllegalArgumentException("Secrand must be 0-2");
} else if(key.equals("compression")) {
		    if(val < 0 || val > 9)
			throw new IllegalArgumentException("Compression Level must be 0-9");
		}
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("Value for " + key + " must be an integer");
	    }
	    //
	} else if(key.equals("server")) {
	    if(client != null && client.isOpened()) {
		throw new IllegalArgumentException("Server can only be set while not connected");
	    }
	} else if(key.equals("realsrv") || key.equals("localhst")) {
	    try {
		InetAddress.getByName(value);
	    } catch (UnknownHostException e) {
		throw new IllegalArgumentException(key + " address must be a legal/known host name");
	    }
	} else if(key.equals("proxytype")) {
	    SSH.getProxyType(value);
	} else if(key.startsWith("local") || key.startsWith("remote")) {
	    try {
		if(value.startsWith("/general/"))
		    value = value.substring(9);
		if(key.startsWith("local"))
		    addLocalPortForward(value, false);
		else
		    addRemotePortForward(value, false);
	    } catch (Exception e) {
		throw new IllegalArgumentException("Not a valid port forward: " + key + " : " + value);
	    }
	} else if(!isProperty(key)) {
	    throw new NoSuchElementException("Unknown ssh property '" + key + "'");
	}
    }

    void activateProperty(String key, String value) {
	//
	// The properties that needs an action to "activated"
	//
	if(key.equals("remfwd")) {
	    try {
		SSHListenChannel.setAllowRemoteConnect((new Boolean(value)).booleanValue());
	    } catch (Throwable t) {
		// Ignore if we don't have the SSHListenChannel class
	    }
	} else if(key.equals("portftp")) {
	    client.havePORTFtp = (new Boolean(value)).booleanValue();
	    if(client.havePORTFtp && SSHProtocolPlugin.getPlugin("ftp") != null) {
		SSHProtocolPlugin.getPlugin("ftp").initiate(client);
	    }
	    //
	} else if(key.equals("alive")) {
	    client.setAliveInterval(Integer.valueOf(value).intValue());
	} else if(key.equals("secrand")) {
	    SecureRandom.secureLevel = Integer.valueOf(value).intValue();
	    //
	} else if(key.equals("realsrv")) {
	    try {
		if(value != null && value.length() > 0)
		    client.setServerRealAddr(InetAddress.getByName(value));
		else
		    client.setServerRealAddr(null);
	    } catch (UnknownHostException e) {
		// !!!
	    }
	} else if(key.equals("localhst")) {
	    try {
		client.setLocalAddr(value);
	    } catch (UnknownHostException e) {
		throw new IllegalArgumentException("localhost address must be a legal/known host name");
	    }
	} else if(key.startsWith("local")) {
	    int n = Integer.parseInt(key.substring(5));
	    if(n > client.localForwards.size())
		throw new IllegalArgumentException("Port forwards must be given in unbroken sequence");
	    if(value.startsWith("/general/"))
		value = value.substring(9);
	    try {
		addLocalPortForward(value, true);
	    } catch (IOException e) {
		throw new IllegalArgumentException("Error creating tunnel: " + e.getMessage());
	    }
	} else if(key.startsWith("remote")) {
	    try {
		int n = Integer.parseInt(key.substring(6));
		if(n > client.remoteForwards.size())
		    throw new IllegalArgumentException("Port forwards must be given in unbroken sequence");
		if(value.startsWith("/general/"))
		    value = value.substring(9);
		addRemotePortForward(value, true);
	    } catch (Exception e) {
		throw new IllegalArgumentException("Not a valid port forward: " + key + " : " + value);
	    }
	}
    }

    public void setProperties(Properties newProps) throws IllegalArgumentException,
    NoSuchElementException
    {
	props = new EncryptedProperties(defaultProperties);
	mergeProperties(newProps);
    }

    public Properties getProperties() {
	return props;
    }

    public void mergeProperties(Properties newProps) throws IllegalArgumentException,
    NoSuchElementException
    {
	String name, value;
	Enumeration enum;
	int i;

	enum = newProps.propertyNames();
	while(enum.hasMoreElements()) {
	    name  = (String)enum.nextElement();
	    value = newProps.getProperty(name);
	    if(!isProperty(name))
		throw new NoSuchElementException("Unknown ssh property '" + name + "'");
	    props.put(name, value);
	}
    }

    public Properties getInitTerminalProperties() {
	return initTermProps;
    }

    public void activateProperties() {
	if(activeProps)
	    return;

	String name, value;
	Enumeration enum = defaultPropertyNames.keys();

	activeProps = true;

	while(enum.hasMoreElements()) {
	    name  = (String)enum.nextElement();
	    value = props.getProperty(name);
	    if(value != null)
		activateProperty(name, value);
	}
	int i = 0;
	while((value = props.getProperty("local" + i)) != null) {
	    activateProperty("local" + i, value);
	    i++;
	}
	i = 0;
	while((value = props.getProperty("remote" + i)) != null) {
	    activateProperty("remote" + i, value);
	    i++;
	}
    }

    public void passivateProperties() {
	activeProps = false;
    }

    private void saveProperties(String fname) throws IOException {
	FileOutputStream f;
	Terminal         term      = getTerminal();
	Properties       termProps = (term != null ? term.getProperties() : null);

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	if(termProps != null) {
	    Enumeration e = termProps.keys();
	    while(e.hasMoreElements()) {
		String key = (String)e.nextElement();
		String val = termProps.getProperty(key);
		props.put(key, val);
	    }
	}

	f = new FileOutputStream(fname);

	if(savePasswords) {
	    // !!! REMOVE
	    if(propertyPassword == null) {
		propertyPassword = "";
	    }
	    // TODO: should take default cipher from defaultProperties
	    props.save(f, "MindTerm ssh settings",
		       propertyPassword, SSH.cipherClasses[SSH.CIPHER_DEFAULT][0]);
	} else {
	    String prxPwd, stdPwd, tisPwd, rsaPwd;
	    stdPwd = props.getProperty("password");
	    prxPwd = props.getProperty("prxpassword");
	    tisPwd = props.getProperty("tispassword");
	    rsaPwd = props.getProperty("rsapassword");
	    clearPasswords();
	    props.save(f, "MindTerm ssh settings");
	    if(stdPwd != null) props.put("password", stdPwd);
	    if(prxPwd != null) props.put("prxpassword", prxPwd);
	    if(tisPwd != null) props.put("tispassword", tisPwd);
	    if(rsaPwd != null) props.put("rsapassword", rsaPwd);
	}

	f.close();

	propsChanged = false;
	if(term != null)
	    term.setPropsChanged(false);

	interactor.propsStateChanged(this);
    }

    private void loadProperties(String fname, boolean promptPwd) throws IOException {
	Terminal term = getTerminal();

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	FileInputStream f     = new FileInputStream(fname);
	byte[]          bytes = new byte[f.available()];
	f.read(bytes);
	ByteArrayInputStream bytein = new ByteArrayInputStream(bytes);
	f.close();

	EncryptedProperties loadProps = new EncryptedProperties();

	try {
	    loadProps.load(bytein, "");
	} catch (AccessDeniedException e) {
	    try {
		bytein.reset();
		loadProps.load(bytein, propertyPassword);
	    } catch (AccessDeniedException ee) {
		try {
		    if(promptPwd) {
			bytein.reset();
			propertyPassword = interactor.promptPassword("File " + fname + " password: ");
			loadProps.load(bytein, propertyPassword);
		    } else {
			throw new AccessDeniedException("");
		    }
		} catch (AccessDeniedException eee) {
		    clearServerSetting();
		    throw new SSHClient.AuthFailException("Access denied for '" + fname + "'");
		}
	    }
	}

	savePasswords = !loadProps.isNormalPropsFile();

	Enumeration enum;
	String      name;

	Properties sshProps  = new Properties();
	Properties termProps = new Properties();

	enum = loadProps.keys();
	while(enum.hasMoreElements()) {
	    name = (String)enum.nextElement();
	    if(isProperty(name))
		sshProps.put(name, loadProps.getProperty(name));
	    else if(TerminalDefProps.isProperty(name))
		termProps.put(name, loadProps.getProperty(name));
	    else {
		if(interactor != null)
		    interactor.report("Unknown property '" + name + "' found in file: " + fname);
		else
		    System.out.println("Unknown property '" + name + "' found in file: " + fname);
	    }
	}

	if(client != null)
	    client.clearAllForwards();

	passivateProperties();

	setProperties(sshProps);

	initTermProps = termProps;

	if(term != null) {
	    term.setProperties(initTermProps, false);
	    term.setPropsChanged(false);
	}

	propsChanged = false;
	if(interactor != null)
	    interactor.propsStateChanged(this);
    }

    final void clearPasswords() {
	props.remove("password");
	props.remove("tispassword");
	props.remove("rsapassword");
	props.remove("prxpassword");
    }

    final void clearServerSetting() {
	setProperty("server", "");
	currentPropsFile = null;
	currentAlias     = null;
	if(interactor != null)
	    interactor.propsStateChanged(this);
    }

    final void clearAllForwards() {
	int i = 0;
	if(client != null)
	    client.clearAllForwards();
	for(i = 0; i < 1024; i++) {
	    String key = "local" + i;
	    if(!props.containsKey(key))
		break;
	    props.remove(key);
	}
	for(i = 0; i < 1024; i++) {
	    String key = "remote" + i;
	    if(!props.containsKey(key))
		break;
	    props.remove(key);
	}
    }

    public boolean wantSave() {
	boolean somePropsChanged = (propsChanged ||
				    (getTerminal() != null ?
				     getTerminal().getPropsChanged() : false));
	return (!isReadOnly() && somePropsChanged && sshHomeDir != null &&
		currentAlias != null);
    }

    public final void checkSave() throws IOException {
	if(autoSaveProps) {
	    saveCurrentFile();
	}
    }

    public void saveCurrentFile() throws IOException {
	if(currentPropsFile != null && wantSave())
	    saveProperties(currentPropsFile);
    }

    public void saveAsCurrentFile(String fileName) throws IOException {
	propsChanged     = true;
	currentPropsFile = fileName;
	saveCurrentFile();
	currentAlias     = null;
    }

    public void loadAbsoluteFile(String fileName, boolean promptPwd) throws IOException {
	currentAlias     = null;
	currentPropsFile = fileName;

	loadProperties(currentPropsFile, promptPwd);
	if(interactor != null)
	    interactor.propsStateChanged(this);
    }

    public void setAlias(String alias) {
	if(sshHomeDir == null)
	    return;
	currentAlias     = alias;
	currentPropsFile = sshHomeDir + alias + PROPS_FILE_EXT;
    }

    public String getAlias() {
	return currentAlias;
    }

    public void loadAliasFile(String alias, boolean promptPwd) throws IOException {
	String oldAlias = currentAlias;
	setAlias(alias);
	if(oldAlias == null || !oldAlias.equals(alias)) {
	    loadProperties(currentPropsFile, promptPwd);
	}
    }

    public String[] availableAliases() {
	if(sshHomeDir == null)
	    return null;

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	// sshHomeDir always ends with a trailing File.separator. Strip before we
	// try to create it (some platforms don't like ending 'separator' in name)
	//
	File dir = new File(sshHomeDir.substring(0, sshHomeDir.length() - 1));
	String[] list, alist;
	int  i, cnt = 0;

	list = dir.list();
	for(i = 0; i < list.length; i++) {
	    if(!list[i].endsWith(PROPS_FILE_EXT)) {
		list[i] = null;
		cnt++;
	    }
	}
	if(cnt == list.length)
	    return null;
	alist = new String[list.length - cnt];
	cnt = 0;
	for(i = 0; i < list.length; i++) {
	    if(list[i] != null) {
		int pi = list[i].lastIndexOf(PROPS_FILE_EXT);
		alist[cnt++] = list[i].substring(0, pi);
	    }
	}

	return alist;
    }

    public boolean isAlias(String alias) {
	String[] aliases = availableAliases();
	boolean  isAlias = false;
	if(aliases != null) {
	    for(int i = 0; i < aliases.length; i++)
		if(alias.equals(aliases[i])) {
		    isAlias = true;
		    break;
		}
	}
	return isAlias;
    }

    public boolean isAbsolutFile(String fileName) {
	if(sshHomeDir == null)
	    return false;

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	File file = new File(fileName);
	return (file.isFile() && file.exists());
    }

    public Terminal getTerminal() {
	if(client == null || client.console == null)
	    return null;
	Terminal term = client.console.getTerminal();
	return term;
    }

    public void removeLocalTunnelAt(int idx, boolean kill) {
	int i, sz = client.localForwards.size();
	props.remove("local" + idx);
	for(i = idx; i < sz - 1; i++) {
	    props.put("local" + idx, props.get("local" + (idx + 1)));
	    props.remove("local" + idx + 1);
	}
	propsChanged = true;
	if(kill) {
	    SSHClient.LocalForward fwd = (SSHClient.LocalForward)client.localForwards.elementAt(idx);
	    client.delLocalPortForward(fwd.localHost, fwd.localPort);
	} else {
	    client.localForwards.removeElementAt(idx);
	}
    }

    public void removeRemoteTunnelAt(int idx) {
	int i, sz = client.remoteForwards.size();
	props.remove("remote" + idx);
	for(i = idx; i < sz - 1; i++) {
	    props.put("remote" + idx, props.get("remote" + (idx + 1)));
	    props.remove("remote" + idx + 1);
	}
	propsChanged = true;
	client.remoteForwards.removeElementAt(idx);
    }

    public void addLocalPortForward(String fwdSpec, boolean commit) throws IllegalArgumentException,
    IOException {
	int    localPort;
	String remoteHost;
	int    remotePort;
	int    d1, d2, d3;
	String tmp, plugin;
	String localHost = null;

	if(fwdSpec.charAt(0) == '/') {
	    int i = fwdSpec.lastIndexOf('/');
	    if(i == 0)
		throw new IllegalArgumentException("Invalid port forward spec. " + fwdSpec);
	    plugin = fwdSpec.substring(1, i);
	    fwdSpec = fwdSpec.substring(i + 1);
	} else
	    plugin = "general";

	d1 = fwdSpec.indexOf(':');
	d2 = fwdSpec.lastIndexOf(':');
	if(d1 == d2)
	    throw new IllegalArgumentException("Invalid port forward spec. " + fwdSpec);

	d3 = fwdSpec.indexOf(':', d1 + 1);

	if(d3 != d2) {
	    localHost = fwdSpec.substring(0, d1);
	    localPort = Integer.parseInt(fwdSpec.substring(d1 + 1, d3));
	    remoteHost = fwdSpec.substring(d3 + 1, d2);
	} else {
	    localPort = Integer.parseInt(fwdSpec.substring(0, d1));
	    remoteHost = fwdSpec.substring(d1 + 1, d2);
	}

	tmp        = fwdSpec.substring(d2 + 1);
	remotePort = Integer.parseInt(tmp);
	if(commit) {
	    if(localHost == null)
		client.addLocalPortForward(localPort, remoteHost, remotePort, plugin);
	    else
		client.addLocalPortForward(localHost, localPort, remoteHost, remotePort, plugin);
	}
    }

    public void addRemotePortForward(String fwdSpec, boolean commit) throws IllegalArgumentException {
	int    remotePort;
	int    localPort;
	String localHost;
	int    d1, d2;
	String tmp, plugin;

	if(fwdSpec.charAt(0) == '/') {
	    int i = fwdSpec.lastIndexOf('/');
	    if(i == 0)
		throw new IllegalArgumentException("Invalid port forward spec.");
	    plugin = fwdSpec.substring(1, i);
	    fwdSpec = fwdSpec.substring(i + 1);
	} else
	    plugin = "general";

	d1 = fwdSpec.indexOf(':');
	d2 = fwdSpec.lastIndexOf(':');
	if(d1 == d2)
	    throw new IllegalArgumentException("Invalid port forward spec.");

	tmp        = fwdSpec.substring(0, d1);
	remotePort = Integer.parseInt(tmp);
	localHost  = fwdSpec.substring(d1 + 1, d2);
	tmp        = fwdSpec.substring(d2 + 1);
	localPort  = Integer.parseInt(tmp);
	if(commit) {
	    client.addRemotePortForward(remotePort, localHost, localPort, plugin);
	}
    }

    //
    // SSHAuthenticator interface
    //
    public String getUsername(SSHClientUser origin) throws IOException {
	String username = getProperty("usrname");
	if(!interactor.quietPrompts() || (username == null || username.equals(""))) {
	    String username2 = interactor.promptLine(getProperty("server") + " login: ", username);
	    if(!username2.equals(username)) {
		clearPasswords();
		username = username2;
	    }
	    setProperty("usrname", username); // Changing the user name does not save new properties...
	}
	return username;
    }

    public String getPassword(SSHClientUser origin) throws IOException {
	String password = getProperty("password");
	if(password == null) {
	    password = interactor.promptPassword(getProperty("usrname") + "@" +
						 getProperty("server") + "'s password: ");
	    setProperty("password", password);
	}
	return password;
    }

    public String getChallengeResponse(SSHClientUser origin, String challenge) throws IOException {
	String tisPassword = getProperty("tispassword");
	if(tisPassword == null) {
	    tisPassword = interactor.promptPassword(challenge);
	    setProperty("tispassword", tisPassword);
	}
	return tisPassword;
    }

    public int[] getAuthTypes(SSHClientUser origin) {
	return SSH.getAuthTypes(getProperty("authtyp"));
    }

    public int getCipher(SSHClientUser origin) {
	return SSH.getCipherType(getProperty("cipher"));
    }

    public SSHRSAKeyFile getIdentityFile(SSHClientUser origin) throws IOException {
	String idFile = getProperty("idfile");
	if(idFile.indexOf(File.separator) == -1) {
	    idFile = sshHomeDir + idFile;
	}

	if(SSH.NETSCAPE_SECURITY_MODEL) {
	    try {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
	    } catch (netscape.security.ForbiddenTargetException e) {
		// !!!
	    }
	}

	keyFile = new SSHRSAKeyFile(idFile);
	return keyFile;
    }

    public String getIdentityPassword(SSHClientUser origin) throws IOException {
	String rsaPassword = getProperty("rsapassword");
	if(rsaPassword == null) {
	    rsaPassword = interactor.promptPassword("key file '" + keyFile.getComment() +
						    "' password: ");
	    setProperty("rsapassword", rsaPassword);
	}
	return rsaPassword;
    }

    public boolean verifyKnownHosts(RSAPublicKey hostPub) throws IOException {
	if(!Boolean.valueOf(getProperty("idhost")).booleanValue()) {
	    return true;
	}

	File        tmpFile;
	String      fileName     = null;
	InputStream knownHostsIn = null;
	int         hostCheck    = 0;
	boolean     confirm      = true;

	SSHRSAPublicKeyFile file = null;

	knownHostsIn = this.getClass().getResourceAsStream("/defaults/known_hosts.txt");

	try {
	    boolean tryingResource = true;
	    while(tryingResource) {
		if(knownHostsIn != null) {
		    fileName = "<resource>/defaults/known_hosts.txt";
		    if(interactor.isVerbose())
			interactor.report("Found preinstalled 'known_hosts' file.");
		} else {
		    tryingResource = false;
		    if(sshHomeDir == null) {
			if(interactor.isVerbose())
			    interactor.report("File operations disabled, server identity can't be verified");
			return true;
		    }

		    if(SSH.NETSCAPE_SECURITY_MODEL) {
			try {
			    netscape.security.PrivilegeManager.enablePrivilege("UniversalFileAccess");
				netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
			} catch (netscape.security.ForbiddenTargetException e) {
			    // !!!
			}
		    }

		    fileName = sshHomeDir + knownHosts;
		    tmpFile = new File(fileName);

		    if(!tmpFile.exists()) {
			if(interactor.askConfirmation("File '"  + fileName + "' not found, create it?", true)) {
			    FileOutputStream f = new FileOutputStream(tmpFile);
			    f.close();
			} else {
			    interactor.report("Verification of server key disabled in this session.");
			    return true;
			}
		    }

		    knownHostsIn = new FileInputStream(fileName);
		}

		file = new SSHRSAPublicKeyFile(knownHostsIn, fileName, true);

		if((hostCheck = file.checkPublic(hostPub.getN(), getProperty("server"))) ==
		   SSH.SRV_HOSTKEY_KNOWN)
		    return true;

		if(tryingResource) {
		    if(!interactor.askConfirmation("Host was not found in preinstalled 'known_hosts' file! Continue anyway?", false))
			return false;
		}

		knownHostsIn = null;
	    }

	    if(hostCheck == SSH.SRV_HOSTKEY_NEW) {
		if(interactor.isVerbose())
		    interactor.report("Host key not found from the list of known hosts.");
		if(!interactor.askConfirmation("Do you want to add this host to your set of known hosts", true)) {
		    interactor.report("Verification of server key disabled in this session.");
		    return true;
		}
		confirm = true;
	    } else {
		interactor.alert("WARNING: HOST IDENTIFICATION HAS CHANGED! " +
				 "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY, " +
				 "ONLY PROCEED IF YOU KNOW WHAT YOU ARE DOING!");
		confirm = interactor.askConfirmation("Do you want to replace the identification of this host?",
						     false);
		file.removePublic(getProperty("server"));
	    }

	    if(confirm) {
		file.addPublic(getProperty("server"), null, hostPub.getE(), hostPub.getN());
		tmpFile      = new File(fileName + ".tmp");
		File oldFile = new File(fileName);
		oldFile.renameTo(tmpFile);
		try {
		    file.saveToFile(fileName);
		} catch (IOException e) {
		    oldFile = new File(fileName);
		    tmpFile.renameTo(oldFile);
		    throw e;
		}
		tmpFile.delete();
	    } else {
		return false;
	    }
	} finally {
	    try { knownHostsIn.close(); } catch (Exception e) {}  
	}

	return true;
    }

    //
    // ProxyAuthenticator interface
    //

    public String getProxyUsername(String type, String challenge) throws IOException {
	String username = getProperty("proxyuser");
	if(!interactor.quietPrompts() || (username == null || username.equals(""))) {
	    String chStr = (challenge != null ? (" '" + challenge + "'") : "");
	    username = interactor.promptLine(type + chStr + " username: ", username);
	    setProperty("proxyuser", username);
	}
	return username;
    }

    public String getProxyPassword(String type, String challenge) throws IOException {
	String prxPassword = getProperty("prxpassword");
	if(prxPassword == null) {
	    String chStr = (challenge != null ? (" '" + challenge + "'") : "");
	    prxPassword = interactor.promptPassword(type + chStr + " password: ");
	    setProperty("prxpassword", prxPassword);
	}
	return prxPassword;
    }

    //
    // SSHClientUser interface
    //

    public String getSrvHost() throws IOException {
	String host = getProperty("server");

	if(!interactor.quietPrompts() || (host == null || host.equals(""))) {
	    if(currentAlias != null)
		host = currentAlias;
	    do {
		host = interactor.promptLine("SSH Server/Alias: ", host);
		host = host.trim();
	    } while ("".equals(host));

	    if(autoLoadProps) {
		if(isAlias(host)) {
		    loadAliasFile(host, true);
		} else if(isAbsolutFile(host)) {
		    loadAbsoluteFile(host, true);
		} else if(sshHomeDir != null) {
		    String pwdChk = "";
		    String alias;
		    do {
			alias = interactor.promptLine("No settings file for " + host +
						      " found.\n\rSave as alias: ", host);
			alias = alias.trim();
			if(savePasswords) {
			    pwdChk = interactor.promptPassword(alias + " file password: ");
			    if(pwdChk.length() > 0)
				propertyPassword = interactor.promptPassword(alias + " password again: ");
			}
		    } while ("".equals(alias) ||
			     (!pwdChk.equals("") && !pwdChk.equals(propertyPassword)));
		    setAlias(alias);
		    setProperty("server", host);

		    // Might be same host/user/pwd but we don't know, it's a
		    // different alias so we better clear stuff here so the user
		    // can change "identity" in another alias (otherwise if
		    // quietPrompts are used the user might not get a chance to
		    // do this). Also, tunnels are no longer "auto-transfered"
		    // between aliases.
		    //
		    clearPasswords();
		    clearAllForwards();
		    props.remove("usrname");
		    propsChanged = true;
		}
		host = getProperty("server");
	    } else {
		setProperty("server", host);
	    }
	}

	activateProperties();

	return host;
    }

    public int getSrvPort() {
	return Integer.valueOf(getProperty("port")).intValue();
    }

    public Socket getProxyConnection() throws IOException {
	String proxyType  = getProperty("proxytype");
	int proxyTypeId   = SSH.PROXY_NONE;

	try {
	    proxyTypeId = SSH.getProxyType(proxyType);
	} catch (IllegalArgumentException e) {
	    throw new IOException(e.getMessage());
	}

	if(proxyTypeId == SSH.PROXY_NONE) {
	    return null;
	}

	String prxHost = getProperty("proxyhost");
	int    prxPort = -1;

	try {
	    prxPort = Integer.valueOf(getProperty("proxyport")).intValue();
	} catch (Exception e) {
	    prxPort = -1;
	}

	if(prxHost == null || prxPort == -1) {
	    throw new IOException("When 'proxytype' is set, 'proxyhost' and 'proxyport' must also be set");
	}

	String sshHost = getProperty("server");
	int    sshPort = getSrvPort();
	String prxProt = getProperty("proxyproto");

	Socket proxySocket = null;

	switch(proxyTypeId) {
	case SSH.PROXY_HTTP:
	    proxySocket = WebProxyTunnelSocket.getProxy(sshHost, sshPort, prxHost, prxPort, prxProt,
							this, "MindTerm/" + SSH.CVS_NAME);
	    break;
	case SSH.PROXY_SOCKS4:
	    proxySocket = SocksProxySocket.getSocks4Proxy(sshHost, sshPort, prxHost, prxPort,
							  getProxyUsername("SOCKS4", null));
	    break;
	case SSH.PROXY_SOCKS5_DNS:
	    proxySocket = SocksProxySocket.getSocks5Proxy(sshHost, sshPort,
							  prxHost, prxPort,
							  false, this);
	    break;
	case SSH.PROXY_SOCKS5_IP:
	    proxySocket = SocksProxySocket.getSocks5Proxy(sshHost, sshPort,
							  prxHost, prxPort,
							  true, this);
	    break;
	}

	return proxySocket;
    }

    public String getDisplay() {
	return getProperty("display");
    }

    public int getMaxPacketSz() {
	return Integer.valueOf(getProperty("mtu")).intValue();
    }

    public int getAliveInterval() {
	return Integer.valueOf(getProperty("alive")).intValue();
    }

public int getCompressionLevel() {
	return Integer.valueOf(getProperty("compression")).intValue();
     }

    public boolean wantX11Forward() {
	return Boolean.valueOf(getProperty("x11fwd")).booleanValue();
    }

    public boolean wantPrivileged() {
	return Boolean.valueOf(getProperty("prvport")).booleanValue();
    }

    public boolean wantPTY() {
	return Boolean.valueOf(getProperty("forcpty")).booleanValue();
    }

    public SSHInteractor getInteractor() {
	return interactor;
    }

}
