package jcifs.ntlmssp;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbNamedPipe;
import jcifs.util.Encdec;
import jcifs.util.HMACT64;
import jcifs.util.Hexdump;
import jcifs.util.MD4;
import jcifs.util.RC4;

public class Type3Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = null;
    private static final int DEFAULT_FLAGS = 0;
    private static final String DEFAULT_PASSWORD = null;
    private static final String DEFAULT_USER = null;
    private static final String DEFAULT_WORKSTATION = null;
    private static final int LM_COMPATIBILITY = 0;
    static final long MILLISECONDS_BETWEEN_1970_AND_1601 = 11644473600000L;
    private static final SecureRandom RANDOM = null;
    private String domain;
    private byte[] lmResponse;
    private byte[] masterKey;
    private byte[] ntResponse;
    private byte[] sessionKey;
    private String user;
    private String workstation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.ntlmssp.Type3Message.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.ntlmssp.Type3Message.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.ntlmssp.Type3Message.<clinit>():void");
    }

    public Type3Message() {
        this.masterKey = null;
        this.sessionKey = null;
        setFlags(getDefaultFlags());
        setDomain(getDefaultDomain());
        setUser(getDefaultUser());
        setWorkstation(getDefaultWorkstation());
    }

    public Type3Message(Type2Message type2) {
        this.masterKey = null;
        this.sessionKey = null;
        setFlags(getDefaultFlags(type2));
        setWorkstation(getDefaultWorkstation());
        String domain = getDefaultDomain();
        setDomain(domain);
        String user = getDefaultUser();
        setUser(user);
        String password = getDefaultPassword();
        switch (LM_COMPATIBILITY) {
            case LM_COMPATIBILITY /*0*/:
            case Encdec.TIME_1970_SEC_32BE /*1*/:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                byte[] nt = getNTResponse(type2, password);
                setLMResponse(nt);
                setNTResponse(nt);
            case Encdec.TIME_1904_SEC_32BE /*3*/:
            case Encdec.TIME_1904_SEC_32LE /*4*/:
            case Encdec.TIME_1601_NANOS_64LE /*5*/:
                byte[] clientChallenge = new byte[8];
                RANDOM.nextBytes(clientChallenge);
                setLMResponse(getLMv2Response(type2, domain, user, password, clientChallenge));
            default:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
        }
    }

    public Type3Message(Type2Message type2, String password, String domain, String user, String workstation, int flags) {
        this.masterKey = null;
        this.sessionKey = null;
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
            case LM_COMPATIBILITY /*0*/:
            case Encdec.TIME_1970_SEC_32BE /*1*/:
                if ((getFlags() & SmbConstants.WRITE_OWNER) == 0) {
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
                    System.arraycopy(type2.getChallenge(), LM_COMPATIBILITY, sessionNonce, LM_COMPATIBILITY, 8);
                    System.arraycopy(clientChallenge, LM_COMPATIBILITY, sessionNonce, 8, 8);
                    MD4 md4 = new MD4();
                    md4.update(responseKeyNT);
                    hmac = new HMACT64(md4.digest());
                    hmac.update(sessionNonce);
                    byte[] ntlm2SessionKey = hmac.digest();
                    if ((getFlags() & SmbConstants.GENERIC_WRITE) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        exchangedKey = new byte[16];
                        new RC4(ntlm2SessionKey).update(this.masterKey, LM_COMPATIBILITY, 16, exchangedKey, LM_COMPATIBILITY);
                        setSessionKey(exchangedKey);
                        return;
                    }
                    this.masterKey = ntlm2SessionKey;
                    setSessionKey(this.masterKey);
                }
            case Encdec.TIME_1970_SEC_32LE /*2*/:
                byte[] nt = getNTResponse(type2, password);
                setLMResponse(nt);
                setNTResponse(nt);
            case Encdec.TIME_1904_SEC_32BE /*3*/:
            case Encdec.TIME_1904_SEC_32LE /*4*/:
            case Encdec.TIME_1601_NANOS_64LE /*5*/:
                responseKeyNT = NtlmPasswordAuthentication.nTOWFv2(domain, user, password);
                clientChallenge = new byte[8];
                RANDOM.nextBytes(clientChallenge);
                setLMResponse(getLMv2Response(type2, domain, user, password, clientChallenge));
                byte[] clientChallenge2 = new byte[8];
                RANDOM.nextBytes(clientChallenge2);
                setNTResponse(getNTLMv2Response(type2, responseKeyNT, clientChallenge2));
                if ((getFlags() & 16) == 16) {
                    hmac = new HMACT64(responseKeyNT);
                    hmac.update(this.ntResponse, LM_COMPATIBILITY, 16);
                    byte[] userSessionKey = hmac.digest();
                    if ((getFlags() & SmbConstants.GENERIC_WRITE) != 0) {
                        this.masterKey = new byte[16];
                        RANDOM.nextBytes(this.masterKey);
                        exchangedKey = new byte[16];
                        new RC4(userSessionKey).update(this.masterKey, LM_COMPATIBILITY, 16, exchangedKey, LM_COMPATIBILITY);
                        setSessionKey(exchangedKey);
                        return;
                    }
                    this.masterKey = userSessionKey;
                    setSessionKey(this.masterKey);
                }
            default:
                setLMResponse(getLMResponse(type2, password));
                setNTResponse(getNTResponse(type2, password));
        }
    }

    public Type3Message(int flags, byte[] lmResponse, byte[] ntResponse, String domain, String user, String workstation) {
        this.masterKey = null;
        this.sessionKey = null;
        setFlags(flags);
        setLMResponse(lmResponse);
        setNTResponse(ntResponse);
        setDomain(domain);
        setUser(user);
        setWorkstation(workstation);
    }

    public Type3Message(byte[] material) throws IOException {
        this.masterKey = null;
        this.sessionKey = null;
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
            int domainLength = domain != null ? domain.length : LM_COMPATIBILITY;
            String userName = getUser();
            byte[] user = null;
            if (!(userName == null || userName.length() == 0)) {
                user = unicode ? userName.getBytes(SmbConstants.UNI_ENCODING) : userName.toUpperCase().getBytes(oem);
            }
            int userLength = user != null ? user.length : LM_COMPATIBILITY;
            String workstationName = getWorkstation();
            byte[] workstation = null;
            if (!(workstationName == null || workstationName.length() == 0)) {
                workstation = unicode ? workstationName.getBytes(SmbConstants.UNI_ENCODING) : workstationName.toUpperCase().getBytes(oem);
            }
            int workstationLength = workstation != null ? workstation.length : LM_COMPATIBILITY;
            byte[] lmResponse = getLMResponse();
            int lmLength = lmResponse != null ? lmResponse.length : LM_COMPATIBILITY;
            byte[] ntResponse = getNTResponse();
            int ntLength = ntResponse != null ? ntResponse.length : LM_COMPATIBILITY;
            byte[] sessionKey = getSessionKey();
            Object type3 = new byte[((((((domainLength + 64) + userLength) + workstationLength) + lmLength) + ntLength) + (sessionKey != null ? sessionKey.length : LM_COMPATIBILITY))];
            System.arraycopy(NTLMSSP_SIGNATURE, LM_COMPATIBILITY, type3, LM_COMPATIBILITY, 8);
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
        return SmbNamedPipe.PIPE_TYPE_TRANSACT | ((type2.getFlags() & 1) != 0 ? 1 : 2);
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
        return NtlmPasswordAuthentication.getNTLMv2Response(responseKeyNT, type2.getChallenge(), clientChallenge, (System.currentTimeMillis() + MILLISECONDS_BETWEEN_1970_AND_1601) * 10000, type2.getTargetInformation());
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
        for (int i = LM_COMPATIBILITY; i < 8; i++) {
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
