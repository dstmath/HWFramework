package jcifs.ntlmssp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import jcifs.Config;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbConstants;
import jcifs.util.HMACT64;
import jcifs.util.Hexdump;
import jcifs.util.MD4;
import jcifs.util.RC4;

public class Type3Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = Config.getProperty("jcifs.smb.client.domain", null);
    private static final int DEFAULT_FLAGS;
    private static final String DEFAULT_PASSWORD = Config.getProperty("jcifs.smb.client.password", null);
    private static final String DEFAULT_USER = Config.getProperty("jcifs.smb.client.username", null);
    private static final String DEFAULT_WORKSTATION;
    private static final int LM_COMPATIBILITY = Config.getInt("jcifs.smb.lmCompatibility", 3);
    static final long MILLISECONDS_BETWEEN_1970_AND_1601 = 11644473600000L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private String domain;
    private byte[] lmResponse;
    private byte[] masterKey = null;
    private byte[] ntResponse;
    private byte[] sessionKey = null;
    private String user;
    private String workstation;

    static {
        int i = 1;
        if (!Config.getBoolean("jcifs.smb.client.useUnicode", true)) {
            i = 2;
        }
        DEFAULT_FLAGS = i | 512;
        String defaultWorkstation = null;
        try {
            defaultWorkstation = NbtAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        DEFAULT_WORKSTATION = defaultWorkstation;
    }

    public Type3Message() {
        setFlags(getDefaultFlags());
        setDomain(getDefaultDomain());
        setUser(getDefaultUser());
        setWorkstation(getDefaultWorkstation());
    }

    public Type3Message(Type2Message type2) {
        setFlags(getDefaultFlags(type2));
        setWorkstation(getDefaultWorkstation());
        String domain2 = getDefaultDomain();
        setDomain(domain2);
        String user2 = getDefaultUser();
        setUser(user2);
        String password = getDefaultPassword();
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
                return;
            case 2:
                byte[] nt = getNTResponse(type2, password);
                setLMResponse(nt);
                setNTResponse(nt);
                return;
            case 3:
            case 4:
            case 5:
                byte[] clientChallenge = new byte[8];
                RANDOM.nextBytes(clientChallenge);
                setLMResponse(getLMv2Response(type2, domain2, user2, password, clientChallenge));
                return;
            default:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
                return;
        }
    }

    public Type3Message(Type2Message type2, String password, String domain2, String user2, String workstation2, int flags) {
        setFlags(getDefaultFlags(type2) | flags);
        setWorkstation(workstation2 == null ? getDefaultWorkstation() : workstation2);
        setDomain(domain2);
        setUser(user2);
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
                if ((getFlags() & 524288) == 0) {
                    setLMResponse(getLMResponse(type2, password));
                    setNTResponse(getNTResponse(type2, password));
                    return;
                }
                byte[] clientChallenge = new byte[24];
                RANDOM.nextBytes(clientChallenge);
                Arrays.fill(clientChallenge, 8, 24, (byte) 0);
                byte[] responseKeyNT = NtlmPasswordAuthentication.nTOWFv1(password);
                byte[] ntlm2Response = NtlmPasswordAuthentication.getNTLM2Response(responseKeyNT, type2.getChallenge(), clientChallenge);
                setLMResponse(clientChallenge);
                setNTResponse(ntlm2Response);
                if ((getFlags() & 16) == 16) {
                    byte[] sessionNonce = new byte[16];
                    System.arraycopy(type2.getChallenge(), 0, sessionNonce, 0, 8);
                    System.arraycopy(clientChallenge, 0, sessionNonce, 8, 8);
                    MD4 md4 = new MD4();
                    md4.update(responseKeyNT);
                    HMACT64 hmac = new HMACT64(md4.digest());
                    hmac.update(sessionNonce);
                    byte[] ntlm2SessionKey = hmac.digest();
                    if ((getFlags() & 1073741824) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        byte[] exchangedKey = new byte[16];
                        new RC4(ntlm2SessionKey).update(this.masterKey, 0, 16, exchangedKey, 0);
                        setSessionKey(exchangedKey);
                        return;
                    }
                    this.masterKey = ntlm2SessionKey;
                    setSessionKey(this.masterKey);
                    return;
                }
                return;
            case 2:
                byte[] nt = getNTResponse(type2, password);
                setLMResponse(nt);
                setNTResponse(nt);
                return;
            case 3:
            case 4:
            case 5:
                byte[] responseKeyNT2 = NtlmPasswordAuthentication.nTOWFv2(domain2, user2, password);
                byte[] clientChallenge2 = new byte[8];
                RANDOM.nextBytes(clientChallenge2);
                setLMResponse(getLMv2Response(type2, domain2, user2, password, clientChallenge2));
                byte[] clientChallenge22 = new byte[8];
                RANDOM.nextBytes(clientChallenge22);
                setNTResponse(getNTLMv2Response(type2, responseKeyNT2, clientChallenge22));
                if ((getFlags() & 16) == 16) {
                    HMACT64 hmac2 = new HMACT64(responseKeyNT2);
                    hmac2.update(this.ntResponse, 0, 16);
                    byte[] userSessionKey = hmac2.digest();
                    if ((getFlags() & 1073741824) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        byte[] exchangedKey2 = new byte[16];
                        new RC4(userSessionKey).update(this.masterKey, 0, 16, exchangedKey2, 0);
                        setSessionKey(exchangedKey2);
                        return;
                    }
                    this.masterKey = userSessionKey;
                    setSessionKey(this.masterKey);
                    return;
                }
                return;
            default:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
                return;
        }
    }

    public Type3Message(int flags, byte[] lmResponse2, byte[] ntResponse2, String domain2, String user2, String workstation2) {
        setFlags(flags);
        setLMResponse(lmResponse2);
        setNTResponse(ntResponse2);
        setDomain(domain2);
        setUser(user2);
        setWorkstation(workstation2);
    }

    public Type3Message(byte[] material) throws IOException {
        parse(material);
    }

    public byte[] getLMResponse() {
        return this.lmResponse;
    }

    public void setLMResponse(byte[] lmResponse2) {
        this.lmResponse = lmResponse2;
    }

    public byte[] getNTResponse() {
        return this.ntResponse;
    }

    public void setNTResponse(byte[] ntResponse2) {
        this.ntResponse = ntResponse2;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain2) {
        this.domain = domain2;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user2) {
        this.user = user2;
    }

    public String getWorkstation() {
        return this.workstation;
    }

    public void setWorkstation(String workstation2) {
        this.workstation = workstation2;
    }

    public byte[] getMasterKey() {
        return this.masterKey;
    }

    public byte[] getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(byte[] sessionKey2) {
        this.sessionKey = sessionKey2;
    }

    @Override // jcifs.ntlmssp.NtlmMessage
    public byte[] toByteArray() {
        try {
            int flags = getFlags();
            boolean unicode = (flags & 1) != 0;
            String oem = unicode ? null : getOEMEncoding();
            String domainName = getDomain();
            byte[] domain2 = null;
            if (!(domainName == null || domainName.length() == 0)) {
                domain2 = unicode ? domainName.getBytes(SmbConstants.UNI_ENCODING) : domainName.getBytes(oem);
            }
            int domainLength = domain2 != null ? domain2.length : 0;
            String userName = getUser();
            byte[] user2 = null;
            if (!(userName == null || userName.length() == 0)) {
                user2 = unicode ? userName.getBytes(SmbConstants.UNI_ENCODING) : userName.toUpperCase().getBytes(oem);
            }
            int userLength = user2 != null ? user2.length : 0;
            String workstationName = getWorkstation();
            byte[] workstation2 = null;
            if (!(workstationName == null || workstationName.length() == 0)) {
                workstation2 = unicode ? workstationName.getBytes(SmbConstants.UNI_ENCODING) : workstationName.toUpperCase().getBytes(oem);
            }
            int workstationLength = workstation2 != null ? workstation2.length : 0;
            byte[] lmResponse2 = getLMResponse();
            int lmLength = lmResponse2 != null ? lmResponse2.length : 0;
            byte[] ntResponse2 = getNTResponse();
            int ntLength = ntResponse2 != null ? ntResponse2.length : 0;
            byte[] sessionKey2 = getSessionKey();
            byte[] type3 = new byte[(domainLength + 64 + userLength + workstationLength + lmLength + ntLength + (sessionKey2 != null ? sessionKey2.length : 0))];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type3, 0, 8);
            writeULong(type3, 8, 3);
            writeSecurityBuffer(type3, 12, 64, lmResponse2);
            int offset = 64 + lmLength;
            writeSecurityBuffer(type3, 20, offset, ntResponse2);
            int offset2 = offset + ntLength;
            writeSecurityBuffer(type3, 28, offset2, domain2);
            int offset3 = offset2 + domainLength;
            writeSecurityBuffer(type3, 36, offset3, user2);
            int offset4 = offset3 + userLength;
            writeSecurityBuffer(type3, 44, offset4, workstation2);
            writeSecurityBuffer(type3, 52, offset4 + workstationLength, sessionKey2);
            writeULong(type3, 60, flags);
            return type3;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String user2 = getUser();
        String domain2 = getDomain();
        String workstation2 = getWorkstation();
        byte[] lmResponse2 = getLMResponse();
        byte[] ntResponse2 = getNTResponse();
        byte[] sessionKey2 = getSessionKey();
        return "Type3Message[domain=" + domain2 + ",user=" + user2 + ",workstation=" + workstation2 + ",lmResponse=" + (lmResponse2 == null ? "null" : "<" + lmResponse2.length + " bytes>") + ",ntResponse=" + (ntResponse2 == null ? "null" : "<" + ntResponse2.length + " bytes>") + ",sessionKey=" + (sessionKey2 == null ? "null" : "<" + sessionKey2.length + " bytes>") + ",flags=0x" + Hexdump.toHexString(getFlags(), 8) + "]";
    }

    public static int getDefaultFlags() {
        return DEFAULT_FLAGS;
    }

    public static int getDefaultFlags(Type2Message type2) {
        if (type2 == null) {
            return DEFAULT_FLAGS;
        }
        return 512 | ((type2.getFlags() & 1) != 0 ? 1 : 2);
    }

    public static byte[] getLMResponse(Type2Message type2, String password) {
        if (type2 == null || password == null) {
            return null;
        }
        return NtlmPasswordAuthentication.getPreNTLMResponse(password, type2.getChallenge());
    }

    public static byte[] getLMv2Response(Type2Message type2, String domain2, String user2, String password, byte[] clientChallenge) {
        if (type2 == null || domain2 == null || user2 == null || password == null || clientChallenge == null) {
            return null;
        }
        return NtlmPasswordAuthentication.getLMv2Response(domain2, user2, password, type2.getChallenge(), clientChallenge);
    }

    public static byte[] getNTLMv2Response(Type2Message type2, byte[] responseKeyNT, byte[] clientChallenge) {
        if (type2 == null || responseKeyNT == null || clientChallenge == null) {
            return null;
        }
        return NtlmPasswordAuthentication.getNTLMv2Response(responseKeyNT, type2.getChallenge(), clientChallenge, (System.currentTimeMillis() + 11644473600000L) * 10000, type2.getTargetInformation());
    }

    public static byte[] getNTResponse(Type2Message type2, String password) {
        if (type2 == null || password == null) {
            return null;
        }
        return NtlmPasswordAuthentication.getNTLMResponse(password, type2.getChallenge());
    }

    public static String getDefaultDomain() {
        return DEFAULT_DOMAIN;
    }

    public static String getDefaultUser() {
        return DEFAULT_USER;
    }

    public static String getDefaultPassword() {
        return DEFAULT_PASSWORD;
    }

    public static String getDefaultWorkstation() {
        return DEFAULT_WORKSTATION;
    }

    private void parse(byte[] material) throws IOException {
        int flags;
        String charset;
        for (int i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (readULong(material, 8) != 3) {
            throw new IOException("Not a Type 3 message.");
        }
        byte[] lmResponse2 = readSecurityBuffer(material, 12);
        int lmResponseOffset = readULong(material, 16);
        byte[] ntResponse2 = readSecurityBuffer(material, 20);
        int ntResponseOffset = readULong(material, 24);
        byte[] domain2 = readSecurityBuffer(material, 28);
        int domainOffset = readULong(material, 32);
        byte[] user2 = readSecurityBuffer(material, 36);
        int userOffset = readULong(material, 40);
        byte[] workstation2 = readSecurityBuffer(material, 44);
        int workstationOffset = readULong(material, 48);
        byte[] _sessionKey = null;
        if (lmResponseOffset == 52 || ntResponseOffset == 52 || domainOffset == 52 || userOffset == 52 || workstationOffset == 52) {
            flags = 514;
            charset = getOEMEncoding();
        } else {
            _sessionKey = readSecurityBuffer(material, 52);
            flags = readULong(material, 60);
            charset = (flags & 1) != 0 ? SmbConstants.UNI_ENCODING : getOEMEncoding();
        }
        setSessionKey(_sessionKey);
        setFlags(flags);
        setLMResponse(lmResponse2);
        setNTResponse(ntResponse2);
        setDomain(new String(domain2, charset));
        setUser(new String(user2, charset));
        setWorkstation(new String(workstation2, charset));
    }
}
