package jcifs.ntlmssp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import jcifs.Config;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
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
        String domain = getDefaultDomain();
        setDomain(domain);
        String user = getDefaultUser();
        setUser(user);
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
                setLMResponse(getLMv2Response(type2, domain, user, password, clientChallenge));
                return;
            default:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
                return;
        }
    }

    public Type3Message(Type2Message type2, String password, String domain, String user, String workstation, int flags) {
        setFlags(getDefaultFlags(type2) | flags);
        if (workstation == null) {
            workstation = getDefaultWorkstation();
        }
        setWorkstation(workstation);
        setDomain(domain);
        setUser(user);
        byte[] clientChallenge;
        byte[] responseKeyNT;
        HMACT64 hmac;
        byte[] exchangedKey;
        switch (LM_COMPATIBILITY) {
            case 0:
            case 1:
                if ((getFlags() & 524288) == 0) {
                    setLMResponse(getLMResponse(type2, password));
                    setNTResponse(getNTResponse(type2, password));
                    return;
                }
                clientChallenge = new byte[24];
                RANDOM.nextBytes(clientChallenge);
                Arrays.fill(clientChallenge, 8, 24, (byte) 0);
                responseKeyNT = NtlmPasswordAuthentication.nTOWFv1(password);
                byte[] ntlm2Response = NtlmPasswordAuthentication.getNTLM2Response(responseKeyNT, type2.getChallenge(), clientChallenge);
                setLMResponse(clientChallenge);
                setNTResponse(ntlm2Response);
                if ((getFlags() & 16) == 16) {
                    Object sessionNonce = new byte[16];
                    System.arraycopy(type2.getChallenge(), 0, sessionNonce, 0, 8);
                    System.arraycopy(clientChallenge, 0, sessionNonce, 8, 8);
                    MD4 md4 = new MD4();
                    md4.update(responseKeyNT);
                    hmac = new HMACT64(md4.digest());
                    hmac.update(sessionNonce);
                    byte[] ntlm2SessionKey = hmac.digest();
                    if ((getFlags() & 1073741824) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        exchangedKey = new byte[16];
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
                responseKeyNT = NtlmPasswordAuthentication.nTOWFv2(domain, user, password);
                clientChallenge = new byte[8];
                RANDOM.nextBytes(clientChallenge);
                setLMResponse(getLMv2Response(type2, domain, user, password, clientChallenge));
                byte[] clientChallenge2 = new byte[8];
                RANDOM.nextBytes(clientChallenge2);
                setNTResponse(getNTLMv2Response(type2, responseKeyNT, clientChallenge2));
                if ((getFlags() & 16) == 16) {
                    hmac = new HMACT64(responseKeyNT);
                    hmac.update(this.ntResponse, 0, 16);
                    byte[] userSessionKey = hmac.digest();
                    if ((getFlags() & 1073741824) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        exchangedKey = new byte[16];
                        new RC4(userSessionKey).update(this.masterKey, 0, 16, exchangedKey, 0);
                        setSessionKey(exchangedKey);
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

    public Type3Message(int flags, byte[] lmResponse, byte[] ntResponse, String domain, String user, String workstation) {
        setFlags(flags);
        setLMResponse(lmResponse);
        setNTResponse(ntResponse);
        setDomain(domain);
        setUser(user);
        setWorkstation(workstation);
    }

    public Type3Message(byte[] material) throws IOException {
        parse(material);
    }

    public byte[] getLMResponse() {
        return this.lmResponse;
    }

    public void setLMResponse(byte[] lmResponse) {
        this.lmResponse = lmResponse;
    }

    public byte[] getNTResponse() {
        return this.ntResponse;
    }

    public void setNTResponse(byte[] ntResponse) {
        this.ntResponse = ntResponse;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWorkstation() {
        return this.workstation;
    }

    public void setWorkstation(String workstation) {
        this.workstation = workstation;
    }

    public byte[] getMasterKey() {
        return this.masterKey;
    }

    public byte[] getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] toByteArray() {
        try {
            int flags = getFlags();
            boolean unicode = (flags & 1) != 0;
            String oem = unicode ? null : NtlmMessage.getOEMEncoding();
            String domainName = getDomain();
            byte[] domain = null;
            if (!(domainName == null || domainName.length() == 0)) {
                domain = unicode ? domainName.getBytes(SmbConstants.UNI_ENCODING) : domainName.getBytes(oem);
            }
            int domainLength = domain != null ? domain.length : 0;
            String userName = getUser();
            byte[] user = null;
            if (!(userName == null || userName.length() == 0)) {
                user = unicode ? userName.getBytes(SmbConstants.UNI_ENCODING) : userName.toUpperCase().getBytes(oem);
            }
            int userLength = user != null ? user.length : 0;
            String workstationName = getWorkstation();
            byte[] workstation = null;
            if (!(workstationName == null || workstationName.length() == 0)) {
                workstation = unicode ? workstationName.getBytes(SmbConstants.UNI_ENCODING) : workstationName.toUpperCase().getBytes(oem);
            }
            int workstationLength = workstation != null ? workstation.length : 0;
            byte[] lmResponse = getLMResponse();
            int lmLength = lmResponse != null ? lmResponse.length : 0;
            byte[] ntResponse = getNTResponse();
            int ntLength = ntResponse != null ? ntResponse.length : 0;
            byte[] sessionKey = getSessionKey();
            Object type3 = new byte[((((((domainLength + 64) + userLength) + workstationLength) + lmLength) + ntLength) + (sessionKey != null ? sessionKey.length : 0))];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type3, 0, 8);
            NtlmMessage.writeULong(type3, 8, 3);
            NtlmMessage.writeSecurityBuffer(type3, 12, 64, lmResponse);
            int offset = 64 + lmLength;
            NtlmMessage.writeSecurityBuffer(type3, 20, offset, ntResponse);
            offset += ntLength;
            NtlmMessage.writeSecurityBuffer(type3, 28, offset, domain);
            offset += domainLength;
            NtlmMessage.writeSecurityBuffer(type3, 36, offset, user);
            offset += userLength;
            NtlmMessage.writeSecurityBuffer(type3, 44, offset, workstation);
            NtlmMessage.writeSecurityBuffer(type3, 52, offset + workstationLength, sessionKey);
            NtlmMessage.writeULong(type3, 60, flags);
            return type3;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String user = getUser();
        String domain = getDomain();
        String workstation = getWorkstation();
        byte[] lmResponse = getLMResponse();
        byte[] ntResponse = getNTResponse();
        byte[] sessionKey = getSessionKey();
        return "Type3Message[domain=" + domain + ",user=" + user + ",workstation=" + workstation + ",lmResponse=" + (lmResponse == null ? "null" : "<" + lmResponse.length + " bytes>") + ",ntResponse=" + (ntResponse == null ? "null" : "<" + ntResponse.length + " bytes>") + ",sessionKey=" + (sessionKey == null ? "null" : "<" + sessionKey.length + " bytes>") + ",flags=0x" + Hexdump.toHexString(getFlags(), 8) + "]";
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

    public static byte[] getLMv2Response(Type2Message type2, String domain, String user, String password, byte[] clientChallenge) {
        if (type2 == null || domain == null || user == null || password == null || clientChallenge == null) {
            return null;
        }
        return NtlmPasswordAuthentication.getLMv2Response(domain, user, password, type2.getChallenge(), clientChallenge);
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
        for (int i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (NtlmMessage.readULong(material, 8) != 3) {
            throw new IOException("Not a Type 3 message.");
        }
        int flags;
        String charset;
        byte[] lmResponse = NtlmMessage.readSecurityBuffer(material, 12);
        int lmResponseOffset = NtlmMessage.readULong(material, 16);
        byte[] ntResponse = NtlmMessage.readSecurityBuffer(material, 20);
        int ntResponseOffset = NtlmMessage.readULong(material, 24);
        byte[] domain = NtlmMessage.readSecurityBuffer(material, 28);
        int domainOffset = NtlmMessage.readULong(material, 32);
        byte[] user = NtlmMessage.readSecurityBuffer(material, 36);
        int userOffset = NtlmMessage.readULong(material, 40);
        byte[] workstation = NtlmMessage.readSecurityBuffer(material, 44);
        int workstationOffset = NtlmMessage.readULong(material, 48);
        byte[] _sessionKey = null;
        if (lmResponseOffset == 52 || ntResponseOffset == 52 || domainOffset == 52 || userOffset == 52 || workstationOffset == 52) {
            flags = 514;
            charset = NtlmMessage.getOEMEncoding();
        } else {
            _sessionKey = NtlmMessage.readSecurityBuffer(material, 52);
            flags = NtlmMessage.readULong(material, 60);
            charset = (flags & 1) != 0 ? SmbConstants.UNI_ENCODING : NtlmMessage.getOEMEncoding();
        }
        setSessionKey(_sessionKey);
        setFlags(flags);
        setLMResponse(lmResponse);
        setNTResponse(ntResponse);
        setDomain(new String(domain, charset));
        setUser(new String(user, charset));
        setWorkstation(new String(workstation, charset));
    }
}
