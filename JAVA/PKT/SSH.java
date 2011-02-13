/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * Modified by James Huang and ISNetworks to group all threads under a single
 * thread group. Grep on JH_Mod for all the changes. - Jan. 21, 2002
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
 * $Date: 2001/02/08 18:42:12 $
 * $Name:  $
 *****************************************************************************/
package mindbright.ssh;

import java.io.*;
import java.math.BigInteger;

import mindbright.security.*;

public abstract class SSH {

  private static ThreadGroup threadGroup;
  private static int thrdPart = 1; //JH_Mod
  public static synchronized String createThreadName() { return "SSH-"+(thrdPart++); } // JH_Mod

   public static ThreadGroup getThreadGroup()
   {
       if( threadGroup == null ) {
	    if(SSH.NETSCAPE_SECURITY_MODEL) {
		try {
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadGroupAccess");
		    netscape.security.PrivilegeManager.enablePrivilege("UniversalThreadAccess");
		} catch (netscape.security.ForbiddenTargetException e) {
		    e.printStackTrace();
		    // !!!
		}
	    }
	    threadGroup = new ThreadGroup("Mindbright SSH"); // JH_Mod
       }
       return threadGroup;
   }
    
  public static boolean DEBUG     = false;
  public static boolean DEBUGMORE = false;

  public final static boolean NETSCAPE_SECURITY_MODEL = false;

  public final static int    SSH_VER_MAJOR = 1;
  public final static int    SSH_VER_MINOR = 5;
  public final static String VER_SSHPKG    = "v1.2.1SCP3COMPRPKTSRNG";
  public final static String VER_MINDTERM  = "MindTerm " + VER_SSHPKG;
  public final static String VER_MINDTUNL  = "MindTunnel " + VER_SSHPKG;
  public final static String CVS_NAME      = "$Name:  $";
  public final static String CVS_DATE      = "$Date: 2001/02/08 18:42:12 $";

  public final static int    DEFAULTPORT        = 22;
  public final static int    SESSION_KEY_LENGTH = 256; // !!! Must be multiple of 8
  public final static int    SERVER_KEY_LENGTH  = 768;
  public final static int    HOST_KEY_LENGTH    = 1024;

  public final static int    PROTOFLAG_SCREEN_NUMBER	= 1;
  public final static int    PROTOFLAG_HOST_IN_FWD_OPEN	= 2;

  public final static int    MSG_ANY                        = -1; // !!! Not part of protocol
  public final static int    MSG_NONE                       = 0;
  public final static int    MSG_DISCONNECT                 = 1;
  public final static int    SMSG_PUBLIC_KEY                = 2;
  public final static int    CMSG_SESSION_KEY               = 3;
  public final static int    CMSG_USER                      = 4;
  public final static int    CMSG_AUTH_RHOSTS               = 5;
  public final static int    CMSG_AUTH_RSA                  = 6;
  public final static int    SMSG_AUTH_RSA_CHALLENGE        = 7;
  public final static int    CMSG_AUTH_RSA_RESPONSE         = 8;
  public final static int    CMSG_AUTH_PASSWORD             = 9;
  public final static int    CMSG_REQUEST_PTY               = 10;
  public final static int    CMSG_WINDOW_SIZE               = 11;
  public final static int    CMSG_EXEC_SHELL                = 12;
  public final static int    CMSG_EXEC_CMD                  = 13;
  public final static int    SMSG_SUCCESS                   = 14;
  public final static int    SMSG_FAILURE                   = 15;
  public final static int    CMSG_STDIN_DATA                = 16;
  public final static int    SMSG_STDOUT_DATA               = 17;
  public final static int    SMSG_STDERR_DATA               = 18;
  public final static int    CMSG_EOF                       = 19;
  public final static int    SMSG_EXITSTATUS                = 20;
  public final static int    MSG_CHANNEL_OPEN_CONFIRMATION  = 21;
  public final static int    MSG_CHANNEL_OPEN_FAILURE       = 22;
  public final static int    MSG_CHANNEL_DATA               = 23;
  public final static int    MSG_CHANNEL_CLOSE              = 24;
  public final static int    MSG_CHANNEL_CLOSE_CONFIRMATION = 25;
  public final static int    MSG_CHANNEL_INPUT_EOF          = 24;
  public final static int    MSG_CHANNEL_OUTPUT_CLOSED      = 25;
  // OBSOLETE                CMSG_X11_REQUEST_FORWARDING    = 26;
  public final static int    SMSG_X11_OPEN                  = 27;
  public final static int    CMSG_PORT_FORWARD_REQUEST      = 28;
  public final static int    MSG_PORT_OPEN                  = 29;
  public final static int    CMSG_AGENT_REQUEST_FORWARDING  = 30;
  public final static int    SMSG_AGENT_OPEN                = 31;
  public final static int    MSG_IGNORE                     = 32;
  public final static int    CMSG_EXIT_CONFIRMATION         = 33;
  public final static int    CMSG_X11_REQUEST_FORWARDING    = 34;
  public final static int    CMSG_AUTH_RHOSTS_RSA           = 35;
  public final static int    MSG_DEBUG                      = 36;
  public final static int    CMSG_REQUEST_COMPRESSION       = 37;
  public final static int    CMSG_MAX_PACKET_SIZE           = 38;
  public final static int    CMSG_AUTH_TIS                  = 39;
  public final static int    SMSG_AUTH_TIS_CHALLENGE        = 40;
  public final static int    CMSG_AUTH_TIS_RESPONSE         = 41;

  public final static int    CMSG_AUTH_SDI                  = 16; // !!! OUCH
  public final static int    CMSG_ACM_OK                    = 64;
  public final static int    CMSG_ACM_ACCESS_DENIED         = 65;
  public final static int    CMSG_ACM_NEXT_CODE_REQUIRED    = 66;
  public final static int    CMSG_ACM_NEXT_CODE             = 67;
  public final static int    CMSG_ACM_NEW_PIN_REQUIRED      = 68;
  public final static int    CMSG_ACM_NEW_PIN_ACCEPTED      = 69;
  public final static int    CMSG_ACM_NEW_PIN_REJECTED      = 70;
  public final static int    CMSG_ACM_NEW_PIN               = 71;

  public final static int    IDX_CIPHER_CLASS = 0;
  public final static int    IDX_CIPHER_NAME  = 1;

  public final static String[][] cipherClasses = {
    { "NoEncrypt", "none"     }, // No encryption
    { "IDEA",      "idea"     }, // IDEA in CFB mode
    { "DES",       "des"      }, // DES in CBC mode
    { "DES3",      "3des"     }, // Triple-DES in CBC mode
    { null,        "tss"      }, // An experimental stream cipher
    { "RC4",       "rc4"      }, // RC4
    { "Blowfish",  "blowfish" },  // Bruce Schneier's Blowfish
    { null,        "reserved" }  // (not implemented now, might be though... see above)
  };
  public final static int    CIPHER_NONE     = 0; // No encryption
  public final static int    CIPHER_IDEA     = 1; // IDEA in CFB mode
  public final static int    CIPHER_DES      = 2; // DES in CBC mode
  public final static int    CIPHER_3DES     = 3; // Triple-DES in CBC mode
  public final static int    CIPHER_TSS      = 4; // An experimental stream cipher
  public final static int    CIPHER_RC4      = 5; // RC4
  public final static int    CIPHER_BLOWFISH = 6; // Bruce Schneier's Blowfish */
  public final static int    CIPHER_RESERVED = 7; // Reserved for 40 bit crippled encryption,
                                                  // Bernard Perrot <perrot@lal.in2p3.fr>
  public final static int    CIPHER_NOTSUPPORTED = 8; // Indicates an unsupported cipher
  public final static int    CIPHER_DEFAULT  = CIPHER_3DES; // Triple-DES is default block-cipher

  public final static String[] authTypeDesc = {
    "_N/A_",
    "rhosts",
    "rsa",
    "passwd",
    "rhostsrsa",
    "tis",
    "kerberos",
    "kerbtgt",
    "sdi-token"
  };
  public final static int    AUTH_RHOSTS       = 1;
  public final static int    AUTH_RSA          = 2;
  public final static int    AUTH_PASSWORD     = 3;
  public final static int    AUTH_RHOSTS_RSA   = 4;
  public final static int    AUTH_TIS          = 5;
  public final static int    AUTH_KERBEROS     = 6;
  public final static int    PASS_KERBEROS_TGT = 7;

  public final static int    AUTH_SDI          = 8;


  public final static int    AUTH_NOTSUPPORTED = authTypeDesc.length;
  public final static int    AUTH_DEFAULT      = AUTH_PASSWORD;


  final static String[] proxyTypes = { "none", "http", "socks4",
				       "socks5-proxy-dns",
				       "socks5-local-dns" };

  final static int[] defaultProxyPorts  = { 0, 8080, 1080, 1080, 1080 };


  public final static int    PROXY_NONE         = 0;
  public final static int    PROXY_HTTP         = 1;
  public final static int    PROXY_SOCKS4       = 2;
  public final static int    PROXY_SOCKS5_DNS   = 3;
  public final static int    PROXY_SOCKS5_IP    = 4;
  public final static int    PROXY_NOTSUPPORTED = proxyTypes.length;


  public final static int    TTY_OP_END	     = 0;
  public final static int    TTY_OP_ISPEED   = 192;
  public final static int    TTY_OP_OSPEED   = 193;

  // These are special "channels" not associated with a channel-number
  // in "SSH-sense".
  //
  public final static int    MAIN_CHAN_NUM    = -1;
  public final static int    CONNECT_CHAN_NUM = -2;
  public final static int    LISTEN_CHAN_NUM  = -3;
  public final static int    UNKNOWN_CHAN_NUM = -4;

  // Default name of file containing set of known hosts
  //
  public final static String KNOWN_HOSTS_FILE = "known_hosts";

  // When verifying the server's host-key to the set of known hosts, the
  // possible outcome is one of these.
  //
  public final static int SRV_HOSTKEY_KNOWN   = 0;
  public final static int SRV_HOSTKEY_NEW     = 1;
  public final static int SRV_HOSTKEY_CHANGED = 2;

  public static SecureRandom secureRandom;

  //
  //
  protected byte[] sessionKey;
  protected byte[] sessionId;

  //
  //
  protected Cipher sndCipher;
  protected Cipher rcvCipher;
  protected int    cipherType;
  protected static int compressionLevel=0;

  // Server data fields
  //
  protected byte[]  srvCookie;
  protected KeyPair srvServerKey;
  protected KeyPair srvHostKey;
  protected int     protocolFlags;
  protected int     supportedCiphers;
  protected int     supportedAuthTypes;

  protected boolean isAnSSHClient = true;

  public static String getVersionId(boolean client) {
      String idStr = "SSH-" + SSH_VER_MAJOR + "." + SSH_VER_MINOR + "-";
      idStr += (client ? VER_MINDTERM : VER_MINDTUNL);
      return idStr;
  }

  public static String[] getProxyTypes() {
    return proxyTypes;
  }

  public static int getProxyType(String typeName) throws IllegalArgumentException {
    int i;
    for(i = 0; i < proxyTypes.length; i++) {
      if(proxyTypes[i].equalsIgnoreCase(typeName))
	break;
    }
    if(i == PROXY_NOTSUPPORTED)
      throw new IllegalArgumentException("Proxytype " + typeName + " not supported");

    return i;
  }

  public static String listSupportedProxyTypes() {
    String list = "";
    int    i;
    for(i = 0; i < proxyTypes.length; i++) {
      list += proxyTypes[i] + " ";
    }
    return list;
  }

  public static String getCipherName(int cipherType) {
    return cipherClasses[cipherType][IDX_CIPHER_NAME];
  }

  public static int getCipherType(String cipherName) {
    int i;
    for(i = 0; i < cipherClasses.length; i++) {
      String clN = cipherClasses[i][IDX_CIPHER_CLASS];
      String ciN = cipherClasses[i][IDX_CIPHER_NAME];
      if(ciN.equalsIgnoreCase(cipherName)) {
	if(cipherClasses[i][0] == null)
	  i = cipherClasses.length;
	break;
      }
    }
    return i;
  }

  public static String getAuthName(int authType) {
    return authTypeDesc[authType];
  }

  public static int getAuthType(String authName) throws IllegalArgumentException {
    int i;
    for(i = 1; i < SSH.authTypeDesc.length; i++) {
      if(SSH.authTypeDesc[i].equalsIgnoreCase(authName))
	break;
    }
    if(i == AUTH_NOTSUPPORTED)
      throw new IllegalArgumentException("Authtype " + authName + " not supported");

    return i;
  }

  static int cntListSize(String authList) {
    int cnt = 1;
    int i   = 0, n;
    while(i < authList.length() && (n = authList.indexOf(',', i)) != -1) {
      i = n + 1;
      cnt++;
    }
    return cnt;
  }

  public static int[] getAuthTypes(String authList) throws IllegalArgumentException {
    int len = cntListSize(authList);
    int[] authTypes = new int[len];
    int r, l = 0;
    String type;

    for(int i = 0; i < len; i++) {
      r = authList.indexOf(',', l);
      if(r == -1)
	r = authList.length();
      type = authList.substring(l, r).trim();
      authTypes[i] = getAuthType(type);
      l = r + 1;
    }

    return authTypes;
  }

  public static String listSupportedCiphers() {
    String list = "";
    int    i;
    for(i = 0; i < cipherClasses.length; i++) {
      if(cipherClasses[i][0] != null)
	list += cipherClasses[i][1] + " ";
    }
    return list;
  }

  public static String[] getCiphers() {
    int i, n = 0;
    for(i = 0; i < cipherClasses.length; i++) {
      if(cipherClasses[i][0] != null)
	n++;
    }
    String[] ciphers = new String[n];
    n = 0;
    for(i = 0; i < cipherClasses.length; i++) {
      if(cipherClasses[i][0] != null)
	ciphers[n++] = cipherClasses[i][1];
    }
    return ciphers;
  }

  public static String listSupportedAuthTypes() {
    String list = "";
    int    i;
    for(i = 1; i < authTypeDesc.length; i++) {
      list += authTypeDesc[i] + " ";
    }
    return list;
  }

  public static String[] getAuthTypeList() {
    String[] auths = new String[authTypeDesc.length];
    for(int i = 1; i < authTypeDesc.length; i++) {
      auths[i - 1] = authTypeDesc[i];
    }
    auths[authTypeDesc.length - 1] = "custom...";
    return auths;
  }

  boolean isCipherSupported(int cipherType) {
    int cipherMask = (0x01 << cipherType);
    if((cipherMask & supportedCiphers) != 0)
      return true;
    return false;
  }

  boolean isAuthTypeSupported(int authType) {
    int authTypeMask = (0x01 << authType);
    if((authTypeMask & supportedAuthTypes) != 0)
      return true;
    return false;
  }

  boolean isProtocolFlagSet(int protFlag) {
    int protFlagMask = (0x01 << protFlag);
    if((protFlagMask & protocolFlags) != 0)
      return true;
    return false;
  }

  public static void initSeedGenerator() {
    if(secureRandom != null)
      return;
    secureRandom = new SecureRandom();
  }

  public static SecureRandom secureRandom() {
    if(secureRandom == null) {
      secureRandom = new SecureRandom();
    }
    return secureRandom;
  }

  public static void log(String msg) {
    if(DEBUG) System.out.println(msg);
  }

  public static void logExtra(String msg) {
    if(DEBUGMORE) System.out.println(msg);
  }

  public static void logDebug(String msg) {
    if(DEBUG) System.out.println(msg);
  }

  public static void logIgnore(SSHPduInputStream pdu) {
    if(DEBUG) System.out.println("MSG_IGNORE received...(len = " + pdu.length + ")");
  }

  void generateSessionId() throws IOException {
    byte[]        message;
    byte[]        srvKey = ((RSAPublicKey)srvServerKey.getPublic()).getN().toByteArray();
    byte[]        hstKey = ((RSAPublicKey)srvHostKey.getPublic()).getN().toByteArray();
    int           i, len = srvKey.length + hstKey.length + srvCookie.length;

    if(srvKey[0] == 0)
      len -= 1;
    if(hstKey[0] == 0)
      len -= 1;

    message = new byte[len];

    if(hstKey[0] == 0) {
      System.arraycopy(hstKey, 1, message, 0, hstKey.length - 1);
      len = hstKey.length - 1;
    } else {
      System.arraycopy(hstKey, 0, message, 0, hstKey.length);
      len = hstKey.length;
    }

    if(srvKey[0] == 0) {
      System.arraycopy(srvKey, 1, message, len, srvKey.length - 1);
      len += srvKey.length - 1;
    } else {
      System.arraycopy(srvKey, 0, message, len, srvKey.length);
      len += srvKey.length;
    }

    System.arraycopy(srvCookie, 0, message, len, srvCookie.length);

    try {
      MessageDigest md5;
      md5 = MessageDigest.getInstance("MD5");
      md5.update(message);
      sessionId = md5.digest();
    } catch(Exception e) {
      throw new IOException("MD5 not implemented, can't generate session-id");
    }
  }

  protected void initClientCipher() throws IOException {
    initCipher(false);
  }

  protected void initServerCipher() throws IOException {
    initCipher(true);
  }

  protected void initCipher(boolean server) throws IOException {
    sndCipher = Cipher.getInstance(cipherClasses[cipherType][0]);
    rcvCipher = Cipher.getInstance(cipherClasses[cipherType][0]);

    if(sndCipher == null) {
      throw new IOException("Cipher " + cipherClasses[cipherType][1] + " not found, can't use it");
    }

    if(cipherType == CIPHER_RC4) {
      if(server) {
	int    len = sessionKey.length / 2;
	byte[] key = new byte[len];
	System.arraycopy(sessionKey, 0, key, 0, len);
	sndCipher.setKey(key);
	System.arraycopy(sessionKey, len, key, 0, len);
	rcvCipher.setKey(key);
      } else {
	int    len = sessionKey.length / 2;
	byte[] key = new byte[len];
	System.arraycopy(sessionKey, 0, key, 0, len);
	rcvCipher.setKey(key);
	System.arraycopy(sessionKey, len, key, 0, len);
	sndCipher.setKey(key);
      }
    } else {
      sndCipher.setKey(sessionKey);
      rcvCipher.setKey(sessionKey);
    }
  }

  public static String generateKeyFiles(KeyPair kp, String fileName, String passwd, String comment)
    throws IOException {
    SSHRSAKeyFile.createKeyFile(kp, passwd, fileName, comment);
    RSAPublicKey pubKey = (RSAPublicKey)kp.getPublic();
    SSHRSAPublicKeyString pks = new SSHRSAPublicKeyString("", comment,
							  pubKey.getE(), pubKey.getN());
    pks.toFile(fileName + ".pub");
    return pks.toString();
  }

  public static KeyPair generateRSAKeyPair(int bits, SecureRandom secRand) {
    KeyPair    kp;
    RSACipher  cipher;
    BigInteger p;
    BigInteger q;
    BigInteger t;
    BigInteger p_1;
    BigInteger q_1;
    BigInteger phi;
    BigInteger G;
    BigInteger F;
    BigInteger e;
    BigInteger d;
    BigInteger u;
    BigInteger n;
    BigInteger one = new BigInteger("1");

    for(;;) {
      int l = secRand.secureLevel;
      secRand.secureLevel = 2;
      p = new BigInteger(bits / 2, 64, secRand);
      q = new BigInteger(bits - (bits / 2), 64, secRand);
      secRand.secureLevel = l;

      if(p.compareTo(q) == 0) {
	continue;
      } else if(q.compareTo(p) < 0) {
	t = q;
	q = p;
	p = t;
      }

      t = p.gcd(q);
      if(t.compareTo(one) != 0) {
	continue;
      }

      p_1 = p.subtract(one);
      q_1 = q.subtract(one);
      phi = p_1.multiply(q_1);
      G   = p_1.gcd(q_1);
      F   = phi.divide(G);

      e   = one.shiftLeft(5);
      e   = e.subtract(one);
      do {
	e = e.add(one.add(one));
	t = e.gcd(phi);
      } while(t.compareTo(one) != 0);

      // !!! d = e.modInverse(F);
      d = e.modInverse(phi);
      n = p.multiply(q);
      u = p.modInverse(q);

      kp = new KeyPair(new RSAPublicKey(e, n),
		       new RSAPrivateKey(e, n, d, u, p, q));

      // !!!
      break;
    }

    return kp;
  }

  /* !!! USED FOR DEBUG !!!
  void printSrvKeys() {
    BigInteger big;
    byte[] theId = new byte[sessionId.length + 1];
    theId[0] = 0;
    System.arraycopy(sessionId, 0, theId, 1, sessionId.length);
    big = new BigInteger(theId);
    System.out.println("sessionId: " + big.toString(16));
    byte[] theKey = new byte[sessionKey.length + 1];
    theKey[0] = 0;
    System.arraycopy(sessionKey, 0, theKey, 1, sessionKey.length);
    big = new BigInteger(theKey);
    System.out.println("sessionkey: " + big.toString(16));

    System.out.println("srvkey n: " + ((RSAPublicKey)srvServerKey.getPublic()).getN().toString(16));
    System.out.println("srvkey e: " + ((RSAPublicKey)srvServerKey.getPublic()).getE().toString(16));
    System.out.println("srvkey bits: " +  ((RSAPublicKey)srvServerKey.getPublic()).bitLength());
    System.out.println("hstkey n: " + ((RSAPublicKey)srvHostKey.getPublic()).getN().toString(16));
    System.out.println("hstkey e: " + ((RSAPublicKey)srvHostKey.getPublic()).getE().toString(16));
    System.out.println("hstkey bits: " +  ((RSAPublicKey)srvHostKey.getPublic()).bitLength());
  }
  */

}

