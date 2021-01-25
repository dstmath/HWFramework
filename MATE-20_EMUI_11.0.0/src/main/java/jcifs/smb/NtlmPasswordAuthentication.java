package jcifs.smb;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.Arrays;
import java.util.Random;
import jcifs.Config;
import jcifs.util.DES;
import jcifs.util.Encdec;
import jcifs.util.HMACT64;
import jcifs.util.LogStream;
import jcifs.util.MD4;

public final class NtlmPasswordAuthentication implements Principal, Serializable {
    public static final NtlmPasswordAuthentication ANONYMOUS = new NtlmPasswordAuthentication(BLANK, BLANK, BLANK);
    static final String BLANK = "";
    static final NtlmPasswordAuthentication DEFAULT = new NtlmPasswordAuthentication(null);
    static String DEFAULT_DOMAIN;
    static String DEFAULT_PASSWORD;
    static String DEFAULT_USERNAME;
    static final NtlmPasswordAuthentication GUEST = new NtlmPasswordAuthentication("?", "GUEST", BLANK);
    private static final int LM_COMPATIBILITY = Config.getInt("jcifs.smb.lmCompatibility", 3);
    static final NtlmPasswordAuthentication NULL = new NtlmPasswordAuthentication(BLANK, BLANK, BLANK);
    private static final Random RANDOM = new Random();
    private static final byte[] S8 = {75, 71, 83, 33, 64, 35, 36, 37};
    private static LogStream log = LogStream.getInstance();
    byte[] ansiHash;
    byte[] challenge;
    byte[] clientChallenge;
    String domain;
    boolean hashesExternal;
    String password;
    byte[] unicodeHash;
    String username;

    private static void E(byte[] key, byte[] data, byte[] e) {
        byte[] key7 = new byte[7];
        byte[] e8 = new byte[8];
        for (int i = 0; i < key.length / 7; i++) {
            System.arraycopy(key, i * 7, key7, 0, 7);
            new DES(key7).encrypt(data, e8);
            System.arraycopy(e8, 0, e, i * 8, 8);
        }
    }

    static void initDefaults() {
        if (DEFAULT_DOMAIN == null) {
            DEFAULT_DOMAIN = Config.getProperty("jcifs.smb.client.domain", "?");
            DEFAULT_USERNAME = Config.getProperty("jcifs.smb.client.username", "GUEST");
            DEFAULT_PASSWORD = Config.getProperty("jcifs.smb.client.password", BLANK);
        }
    }

    public static byte[] getPreNTLMResponse(String password2, byte[] challenge2) {
        byte[] p14 = new byte[14];
        byte[] p21 = new byte[21];
        byte[] p24 = new byte[24];
        try {
            byte[] passwordBytes = password2.toUpperCase().getBytes(ServerMessageBlock.OEM_ENCODING);
            int passwordLength = passwordBytes.length;
            if (passwordLength > 14) {
                passwordLength = 14;
            }
            System.arraycopy(passwordBytes, 0, p14, 0, passwordLength);
            E(p14, S8, p21);
            E(p21, challenge2, p24);
            return p24;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Try setting jcifs.encoding=US-ASCII", uee);
        }
    }

    public static byte[] getNTLMResponse(String password2, byte[] challenge2) {
        byte[] uni = null;
        byte[] p21 = new byte[21];
        byte[] p24 = new byte[24];
        try {
            uni = password2.getBytes(SmbConstants.UNI_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            LogStream logStream = log;
            if (LogStream.level > 0) {
                uee.printStackTrace(log);
            }
        }
        MD4 md4 = new MD4();
        md4.update(uni);
        try {
            md4.digest(p21, 0, 16);
        } catch (Exception ex) {
            LogStream logStream2 = log;
            if (LogStream.level > 0) {
                ex.printStackTrace(log);
            }
        }
        E(p21, challenge2, p24);
        return p24;
    }

    public static byte[] getLMv2Response(String domain2, String user, String password2, byte[] challenge2, byte[] clientChallenge2) {
        try {
            byte[] bArr = new byte[16];
            byte[] response = new byte[24];
            MD4 md4 = new MD4();
            md4.update(password2.getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac = new HMACT64(md4.digest());
            hmac.update(user.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            hmac.update(domain2.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac2 = new HMACT64(hmac.digest());
            hmac2.update(challenge2);
            hmac2.update(clientChallenge2);
            hmac2.digest(response, 0, 16);
            System.arraycopy(clientChallenge2, 0, response, 16, 8);
            return response;
        } catch (Exception ex) {
            LogStream logStream = log;
            if (LogStream.level > 0) {
                ex.printStackTrace(log);
            }
            return null;
        }
    }

    public static byte[] getNTLM2Response(byte[] nTOWFv1, byte[] serverChallenge, byte[] clientChallenge2) {
        byte[] sessionHash = new byte[8];
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(serverChallenge);
            md5.update(clientChallenge2, 0, 8);
            System.arraycopy(md5.digest(), 0, sessionHash, 0, 8);
            byte[] key = new byte[21];
            System.arraycopy(nTOWFv1, 0, key, 0, 16);
            byte[] ntResponse = new byte[24];
            E(key, sessionHash, ntResponse);
            return ntResponse;
        } catch (GeneralSecurityException gse) {
            LogStream logStream = log;
            if (LogStream.level > 0) {
                gse.printStackTrace(log);
            }
            throw new RuntimeException("MD5", gse);
        }
    }

    public static byte[] nTOWFv1(String password2) {
        if (password2 == null) {
            throw new RuntimeException("Password parameter is required");
        }
        try {
            MD4 md4 = new MD4();
            md4.update(password2.getBytes(SmbConstants.UNI_ENCODING));
            return md4.digest();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }
    }

    public static byte[] nTOWFv2(String domain2, String username2, String password2) {
        try {
            MD4 md4 = new MD4();
            md4.update(password2.getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac = new HMACT64(md4.digest());
            hmac.update(username2.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            hmac.update(domain2.getBytes(SmbConstants.UNI_ENCODING));
            return hmac.digest();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }
    }

    static byte[] computeResponse(byte[] responseKey, byte[] serverChallenge, byte[] clientData, int offset, int length) {
        HMACT64 hmac = new HMACT64(responseKey);
        hmac.update(serverChallenge);
        hmac.update(clientData, offset, length);
        byte[] mac = hmac.digest();
        byte[] ret = new byte[(mac.length + clientData.length)];
        System.arraycopy(mac, 0, ret, 0, mac.length);
        System.arraycopy(clientData, 0, ret, mac.length, clientData.length);
        return ret;
    }

    public static byte[] getLMv2Response(byte[] responseKeyLM, byte[] serverChallenge, byte[] clientChallenge2) {
        return computeResponse(responseKeyLM, serverChallenge, clientChallenge2, 0, clientChallenge2.length);
    }

    public static byte[] getNTLMv2Response(byte[] responseKeyNT, byte[] serverChallenge, byte[] clientChallenge2, long nanos1601, byte[] targetInfo) {
        int targetInfoLength;
        if (targetInfo != null) {
            targetInfoLength = targetInfo.length;
        } else {
            targetInfoLength = 0;
        }
        byte[] temp = new byte[(targetInfoLength + 28 + 4)];
        Encdec.enc_uint32le(257, temp, 0);
        Encdec.enc_uint32le(0, temp, 4);
        Encdec.enc_uint64le(nanos1601, temp, 8);
        System.arraycopy(clientChallenge2, 0, temp, 16, 8);
        Encdec.enc_uint32le(0, temp, 24);
        if (targetInfo != null) {
            System.arraycopy(targetInfo, 0, temp, 28, targetInfoLength);
        }
        Encdec.enc_uint32le(0, temp, targetInfoLength + 28);
        return computeResponse(responseKeyNT, serverChallenge, temp, 0, temp.length);
    }

    public NtlmPasswordAuthentication(String userInfo) {
        this.hashesExternal = false;
        this.clientChallenge = null;
        this.challenge = null;
        this.password = null;
        this.username = null;
        this.domain = null;
        if (userInfo != null) {
            try {
                userInfo = unescape(userInfo);
            } catch (UnsupportedEncodingException e) {
            }
            int end = userInfo.length();
            int i = 0;
            int u = 0;
            while (true) {
                if (i >= end) {
                    break;
                }
                char c = userInfo.charAt(i);
                if (c == ';') {
                    this.domain = userInfo.substring(0, i);
                    u = i + 1;
                } else if (c == ':') {
                    this.password = userInfo.substring(i + 1);
                    break;
                }
                i++;
            }
            this.username = userInfo.substring(u, i);
        }
        initDefaults();
        if (this.domain == null) {
            this.domain = DEFAULT_DOMAIN;
        }
        if (this.username == null) {
            this.username = DEFAULT_USERNAME;
        }
        if (this.password == null) {
            this.password = DEFAULT_PASSWORD;
        }
    }

    public NtlmPasswordAuthentication(String domain2, String username2, String password2) {
        this.hashesExternal = false;
        this.clientChallenge = null;
        this.challenge = null;
        if (username2 != null) {
            int ci = username2.indexOf(64);
            if (ci > 0) {
                domain2 = username2.substring(ci + 1);
                username2 = username2.substring(0, ci);
            } else {
                int ci2 = username2.indexOf(92);
                if (ci2 > 0) {
                    domain2 = username2.substring(0, ci2);
                    username2 = username2.substring(ci2 + 1);
                }
            }
        }
        this.domain = domain2;
        this.username = username2;
        this.password = password2;
        initDefaults();
        if (domain2 == null) {
            this.domain = DEFAULT_DOMAIN;
        }
        if (username2 == null) {
            this.username = DEFAULT_USERNAME;
        }
        if (password2 == null) {
            this.password = DEFAULT_PASSWORD;
        }
    }

    public NtlmPasswordAuthentication(String domain2, String username2, byte[] challenge2, byte[] ansiHash2, byte[] unicodeHash2) {
        this.hashesExternal = false;
        this.clientChallenge = null;
        this.challenge = null;
        if (domain2 == null || username2 == null || ansiHash2 == null || unicodeHash2 == null) {
            throw new IllegalArgumentException("External credentials cannot be null");
        }
        this.domain = domain2;
        this.username = username2;
        this.password = null;
        this.challenge = challenge2;
        this.ansiHash = ansiHash2;
        this.unicodeHash = unicodeHash2;
        this.hashesExternal = true;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    @Override // java.security.Principal
    public String getName() {
        return this.domain.length() > 0 && !this.domain.equals("?") ? this.domain + "\\" + this.username : this.username;
    }

    public byte[] getAnsiHash(byte[] challenge2) {
        if (this.hashesExternal) {
            return this.ansiHash;
        }
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
                return getPreNTLMResponse(this.password, challenge2);
            case 2:
                return getNTLMResponse(this.password, challenge2);
            case 3:
            case 4:
            case 5:
                if (this.clientChallenge == null) {
                    this.clientChallenge = new byte[8];
                    RANDOM.nextBytes(this.clientChallenge);
                }
                return getLMv2Response(this.domain, this.username, this.password, challenge2, this.clientChallenge);
            default:
                return getPreNTLMResponse(this.password, challenge2);
        }
    }

    public byte[] getUnicodeHash(byte[] challenge2) {
        if (this.hashesExternal) {
            return this.unicodeHash;
        }
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
            case 2:
                return getNTLMResponse(this.password, challenge2);
            case 3:
            case 4:
            case 5:
                return new byte[0];
            default:
                return getNTLMResponse(this.password, challenge2);
        }
    }

    public byte[] getSigningKey(byte[] challenge2) throws SmbException {
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
            case 2:
                byte[] signingKey = new byte[40];
                getUserSessionKey(challenge2, signingKey, 0);
                System.arraycopy(getUnicodeHash(challenge2), 0, signingKey, 16, 24);
                return signingKey;
            case 3:
            case 4:
            case 5:
                throw new SmbException("NTLMv2 requires extended security (jcifs.smb.client.useExtendedSecurity must be true if jcifs.smb.lmCompatibility >= 3)");
            default:
                return null;
        }
    }

    public byte[] getUserSessionKey(byte[] challenge2) {
        if (this.hashesExternal) {
            return null;
        }
        byte[] key = new byte[16];
        try {
            getUserSessionKey(challenge2, key, 0);
            return key;
        } catch (Exception ex) {
            LogStream logStream = log;
            if (LogStream.level <= 0) {
                return key;
            }
            ex.printStackTrace(log);
            return key;
        }
    }

    /* access modifiers changed from: package-private */
    public void getUserSessionKey(byte[] challenge2, byte[] dest, int offset) throws SmbException {
        if (!this.hashesExternal) {
            try {
                MD4 md4 = new MD4();
                md4.update(this.password.getBytes(SmbConstants.UNI_ENCODING));
                switch (LM_COMPATIBILITY) {
                    case 0:
                    case 1:
                    case 2:
                        md4.update(md4.digest());
                        md4.digest(dest, offset, 16);
                        return;
                    case 3:
                    case 4:
                    case 5:
                        if (this.clientChallenge == null) {
                            this.clientChallenge = new byte[8];
                            RANDOM.nextBytes(this.clientChallenge);
                        }
                        HMACT64 hmac = new HMACT64(md4.digest());
                        hmac.update(this.username.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
                        hmac.update(this.domain.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
                        byte[] ntlmv2Hash = hmac.digest();
                        HMACT64 hmac2 = new HMACT64(ntlmv2Hash);
                        hmac2.update(challenge2);
                        hmac2.update(this.clientChallenge);
                        HMACT64 userKey = new HMACT64(ntlmv2Hash);
                        userKey.update(hmac2.digest());
                        userKey.digest(dest, offset, 16);
                        return;
                    default:
                        md4.update(md4.digest());
                        md4.digest(dest, offset, 16);
                        return;
                }
            } catch (Exception e) {
                throw new SmbException(BLANK, e);
            }
        }
    }

    @Override // java.security.Principal, java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof NtlmPasswordAuthentication) {
            NtlmPasswordAuthentication ntlm = (NtlmPasswordAuthentication) obj;
            if (ntlm.domain.toUpperCase().equals(this.domain.toUpperCase()) && ntlm.username.toUpperCase().equals(this.username.toUpperCase())) {
                if (this.hashesExternal && ntlm.hashesExternal) {
                    return Arrays.equals(this.ansiHash, ntlm.ansiHash) && Arrays.equals(this.unicodeHash, ntlm.unicodeHash);
                }
                if (!this.hashesExternal && this.password.equals(ntlm.password)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // java.security.Principal, java.lang.Object
    public int hashCode() {
        return getName().toUpperCase().hashCode();
    }

    @Override // java.security.Principal, java.lang.Object
    public String toString() {
        return getName();
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003b: APUT  
      (r0v0 'b' byte[] A[D('b' byte[]), IMMUTABLE_TYPE])
      (0 ??[int, short, byte, char])
      (wrap: byte : 0x003a: CAST (r8v5 byte A[IMMUTABLE_TYPE]) = (byte) (wrap: int : 0x0038: ARITH  (r8v4 int) = (wrap: int : 0x0034: INVOKE  (r8v3 int) = 
      (wrap: java.lang.String : 0x002e: INVOKE  (r8v2 java.lang.String) = 
      (r12v0 'str' java.lang.String A[D('str' java.lang.String)])
      (r2v1 'i' int A[D('i' int)])
      (wrap: int : 0x002c: ARITH  (r8v1 int) = (r2v1 'i' int A[D('i' int)]) + (2 int))
     type: VIRTUAL call: java.lang.String.substring(int, int):java.lang.String)
      (16 int)
     type: STATIC call: java.lang.Integer.parseInt(java.lang.String, int):int) & (255 int)))
     */
    static String unescape(String str) throws NumberFormatException, UnsupportedEncodingException {
        int j;
        byte[] b = new byte[1];
        if (str == null) {
            return null;
        }
        int len = str.length();
        char[] out = new char[len];
        int state = 0;
        int i = 0;
        int j2 = 0;
        while (i < len) {
            switch (state) {
                case 0:
                    char ch = str.charAt(i);
                    if (ch != '%') {
                        j = j2 + 1;
                        out[j2] = ch;
                        break;
                    } else {
                        state = 1;
                        j = j2;
                        break;
                    }
                case 1:
                    b[0] = (byte) (Integer.parseInt(str.substring(i, i + 2), 16) & 255);
                    j = j2 + 1;
                    out[j2] = new String(b, 0, 1, "ASCII").charAt(0);
                    i++;
                    state = 0;
                    break;
                default:
                    j = j2;
                    break;
            }
            i++;
            j2 = j;
        }
        return new String(out, 0, j2);
    }
}
