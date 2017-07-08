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
    public static final NtlmPasswordAuthentication ANONYMOUS = null;
    static final String BLANK = "";
    static final NtlmPasswordAuthentication DEFAULT = null;
    static String DEFAULT_DOMAIN;
    static String DEFAULT_PASSWORD;
    static String DEFAULT_USERNAME;
    static final NtlmPasswordAuthentication GUEST = null;
    private static final int LM_COMPATIBILITY = 0;
    static final NtlmPasswordAuthentication NULL = null;
    private static final Random RANDOM = null;
    private static final byte[] S8 = null;
    private static LogStream log;
    byte[] ansiHash;
    byte[] challenge;
    byte[] clientChallenge;
    String domain;
    boolean hashesExternal;
    String password;
    byte[] unicodeHash;
    String username;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.smb.NtlmPasswordAuthentication.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.smb.NtlmPasswordAuthentication.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.smb.NtlmPasswordAuthentication.<clinit>():void");
    }

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

    public static byte[] getPreNTLMResponse(String password, byte[] challenge) {
        byte[] p14 = new byte[14];
        byte[] p21 = new byte[21];
        byte[] p24 = new byte[24];
        try {
            byte[] passwordBytes = password.toUpperCase().getBytes(ServerMessageBlock.OEM_ENCODING);
            int passwordLength = passwordBytes.length;
            if (passwordLength > 14) {
                passwordLength = 14;
            }
            System.arraycopy(passwordBytes, 0, p14, 0, passwordLength);
            E(p14, S8, p21);
            E(p21, challenge, p24);
            return p24;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Try setting jcifs.encoding=US-ASCII", uee);
        }
    }

    public static byte[] getNTLMResponse(String password, byte[] challenge) {
        byte[] uni = null;
        byte[] p21 = new byte[21];
        byte[] p24 = new byte[24];
        try {
            uni = password.getBytes(SmbConstants.UNI_ENCODING);
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
            logStream = log;
            if (LogStream.level > 0) {
                ex.printStackTrace(log);
            }
        }
        E(p21, challenge, p24);
        return p24;
    }

    public static byte[] getLMv2Response(String domain, String user, String password, byte[] challenge, byte[] clientChallenge) {
        try {
            byte[] hash = new byte[16];
            byte[] response = new byte[24];
            MD4 md4 = new MD4();
            md4.update(password.getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac = new HMACT64(md4.digest());
            hmac.update(user.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            hmac.update(domain.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac2 = new HMACT64(hmac.digest());
            hmac2.update(challenge);
            hmac2.update(clientChallenge);
            hmac2.digest(response, 0, 16);
            System.arraycopy(clientChallenge, 0, response, 16, 8);
            return response;
        } catch (Exception ex) {
            LogStream logStream = log;
            if (LogStream.level > 0) {
                ex.printStackTrace(log);
            }
            return null;
        }
    }

    public static byte[] getNTLM2Response(byte[] nTOWFv1, byte[] serverChallenge, byte[] clientChallenge) {
        byte[] sessionHash = new byte[8];
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(serverChallenge);
            md5.update(clientChallenge, 0, 8);
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

    public static byte[] nTOWFv1(String password) {
        if (password == null) {
            throw new RuntimeException("Password parameter is required");
        }
        try {
            MD4 md4 = new MD4();
            md4.update(password.getBytes(SmbConstants.UNI_ENCODING));
            return md4.digest();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee.getMessage());
        }
    }

    public static byte[] nTOWFv2(String domain, String username, String password) {
        try {
            MD4 md4 = new MD4();
            md4.update(password.getBytes(SmbConstants.UNI_ENCODING));
            HMACT64 hmac = new HMACT64(md4.digest());
            hmac.update(username.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
            hmac.update(domain.getBytes(SmbConstants.UNI_ENCODING));
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

    public static byte[] getLMv2Response(byte[] responseKeyLM, byte[] serverChallenge, byte[] clientChallenge) {
        return computeResponse(responseKeyLM, serverChallenge, clientChallenge, 0, clientChallenge.length);
    }

    public static byte[] getNTLMv2Response(byte[] responseKeyNT, byte[] serverChallenge, byte[] clientChallenge, long nanos1601, byte[] targetInfo) {
        int targetInfoLength;
        if (targetInfo != null) {
            targetInfoLength = targetInfo.length;
        } else {
            targetInfoLength = 0;
        }
        byte[] temp = new byte[((targetInfoLength + 28) + 4)];
        Encdec.enc_uint32le(257, temp, 0);
        Encdec.enc_uint32le(0, temp, 4);
        Encdec.enc_uint64le(nanos1601, temp, 8);
        System.arraycopy(clientChallenge, 0, temp, 16, 8);
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
            while (i < end) {
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

    public NtlmPasswordAuthentication(String domain, String username, String password) {
        this.hashesExternal = false;
        this.clientChallenge = null;
        this.challenge = null;
        if (username != null) {
            int ci = username.indexOf(64);
            if (ci > 0) {
                domain = username.substring(ci + 1);
                username = username.substring(0, ci);
            } else {
                ci = username.indexOf(92);
                if (ci > 0) {
                    domain = username.substring(0, ci);
                    username = username.substring(ci + 1);
                }
            }
        }
        this.domain = domain;
        this.username = username;
        this.password = password;
        initDefaults();
        if (domain == null) {
            this.domain = DEFAULT_DOMAIN;
        }
        if (username == null) {
            this.username = DEFAULT_USERNAME;
        }
        if (password == null) {
            this.password = DEFAULT_PASSWORD;
        }
    }

    public NtlmPasswordAuthentication(String domain, String username, byte[] challenge, byte[] ansiHash, byte[] unicodeHash) {
        this.hashesExternal = false;
        this.clientChallenge = null;
        this.challenge = null;
        if (domain == null || username == null || ansiHash == null || unicodeHash == null) {
            throw new IllegalArgumentException("External credentials cannot be null");
        }
        this.domain = domain;
        this.username = username;
        this.password = null;
        this.challenge = challenge;
        this.ansiHash = ansiHash;
        this.unicodeHash = unicodeHash;
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

    public String getName() {
        boolean d = this.domain.length() > 0 && !this.domain.equals("?");
        return d ? this.domain + "\\" + this.username : this.username;
    }

    public byte[] getAnsiHash(byte[] challenge) {
        if (this.hashesExternal) {
            return this.ansiHash;
        }
        switch (LM_COMPATIBILITY) {
            case SmbFile.FILE_NO_SHARE /*0*/:
            case Encdec.TIME_1970_SEC_32BE /*1*/:
                return getPreNTLMResponse(this.password, challenge);
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                return getNTLMResponse(this.password, challenge);
            case Encdec.TIME_1904_SEC_32BE /*3*/:
            case Encdec.TIME_1904_SEC_32LE /*4*/:
            case Encdec.TIME_1601_NANOS_64LE /*5*/:
                if (this.clientChallenge == null) {
                    this.clientChallenge = new byte[8];
                    RANDOM.nextBytes(this.clientChallenge);
                }
                return getLMv2Response(this.domain, this.username, this.password, challenge, this.clientChallenge);
            default:
                return getPreNTLMResponse(this.password, challenge);
        }
    }

    public byte[] getUnicodeHash(byte[] challenge) {
        if (this.hashesExternal) {
            return this.unicodeHash;
        }
        switch (LM_COMPATIBILITY) {
            case SmbFile.FILE_NO_SHARE /*0*/:
            case Encdec.TIME_1970_SEC_32BE /*1*/:
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                return getNTLMResponse(this.password, challenge);
            case Encdec.TIME_1904_SEC_32BE /*3*/:
            case Encdec.TIME_1904_SEC_32LE /*4*/:
            case Encdec.TIME_1601_NANOS_64LE /*5*/:
                return new byte[0];
            default:
                return getNTLMResponse(this.password, challenge);
        }
    }

    public byte[] getSigningKey(byte[] challenge) throws SmbException {
        switch (LM_COMPATIBILITY) {
            case SmbFile.FILE_NO_SHARE /*0*/:
            case Encdec.TIME_1970_SEC_32BE /*1*/:
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                byte[] signingKey = new byte[40];
                getUserSessionKey(challenge, signingKey, 0);
                System.arraycopy(getUnicodeHash(challenge), 0, signingKey, 16, 24);
                return signingKey;
            case Encdec.TIME_1904_SEC_32BE /*3*/:
            case Encdec.TIME_1904_SEC_32LE /*4*/:
            case Encdec.TIME_1601_NANOS_64LE /*5*/:
                throw new SmbException("NTLMv2 requires extended security (jcifs.smb.client.useExtendedSecurity must be true if jcifs.smb.lmCompatibility >= 3)");
            default:
                return null;
        }
    }

    public byte[] getUserSessionKey(byte[] challenge) {
        if (this.hashesExternal) {
            return null;
        }
        byte[] key = new byte[16];
        try {
            getUserSessionKey(challenge, key, 0);
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

    void getUserSessionKey(byte[] challenge, byte[] dest, int offset) throws SmbException {
        if (!this.hashesExternal) {
            try {
                MD4 md4 = new MD4();
                md4.update(this.password.getBytes(SmbConstants.UNI_ENCODING));
                switch (LM_COMPATIBILITY) {
                    case SmbFile.FILE_NO_SHARE /*0*/:
                    case Encdec.TIME_1970_SEC_32BE /*1*/:
                    case Encdec.TIME_1970_SEC_32LE /*2*/:
                        md4.update(md4.digest());
                        md4.digest(dest, offset, 16);
                        return;
                    case Encdec.TIME_1904_SEC_32BE /*3*/:
                    case Encdec.TIME_1904_SEC_32LE /*4*/:
                    case Encdec.TIME_1601_NANOS_64LE /*5*/:
                        if (this.clientChallenge == null) {
                            this.clientChallenge = new byte[8];
                            RANDOM.nextBytes(this.clientChallenge);
                        }
                        HMACT64 hmac = new HMACT64(md4.digest());
                        hmac.update(this.username.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
                        hmac.update(this.domain.toUpperCase().getBytes(SmbConstants.UNI_ENCODING));
                        byte[] ntlmv2Hash = hmac.digest();
                        hmac = new HMACT64(ntlmv2Hash);
                        hmac.update(challenge);
                        hmac.update(this.clientChallenge);
                        HMACT64 userKey = new HMACT64(ntlmv2Hash);
                        userKey.update(hmac.digest());
                        userKey.digest(dest, offset, 16);
                        return;
                    default:
                        md4.update(md4.digest());
                        md4.digest(dest, offset, 16);
                        return;
                }
            } catch (Throwable e) {
                throw new SmbException(BLANK, e);
            }
            throw new SmbException(BLANK, e);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof NtlmPasswordAuthentication) {
            NtlmPasswordAuthentication ntlm = (NtlmPasswordAuthentication) obj;
            if (ntlm.domain.toUpperCase().equals(this.domain.toUpperCase()) && ntlm.username.toUpperCase().equals(this.username.toUpperCase())) {
                if (this.hashesExternal && ntlm.hashesExternal) {
                    if (Arrays.equals(this.ansiHash, ntlm.ansiHash) && Arrays.equals(this.unicodeHash, ntlm.unicodeHash)) {
                        return true;
                    }
                    return false;
                } else if (!this.hashesExternal && this.password.equals(ntlm.password)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        return getName().toUpperCase().hashCode();
    }

    public String toString() {
        return getName();
    }

    static String unescape(String str) throws NumberFormatException, UnsupportedEncodingException {
        byte[] b = new byte[1];
        if (str == null) {
            return null;
        }
        int len = str.length();
        char[] out = new char[len];
        int state = 0;
        int i = 0;
        int j = 0;
        while (i < len) {
            int j2;
            switch (state) {
                case SmbFile.FILE_NO_SHARE /*0*/:
                    char ch = str.charAt(i);
                    if (ch != '%') {
                        j2 = j + 1;
                        out[j] = ch;
                        break;
                    }
                    state = 1;
                    j2 = j;
                    break;
                case Encdec.TIME_1970_SEC_32BE /*1*/:
                    b[0] = (byte) (Integer.parseInt(str.substring(i, i + 2), 16) & 255);
                    j2 = j + 1;
                    out[j] = new String(b, 0, 1, "ASCII").charAt(0);
                    i++;
                    state = 0;
                    break;
                default:
                    j2 = j;
                    break;
            }
            i++;
            j = j2;
        }
        return new String(out, 0, j);
    }
}
