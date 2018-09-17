package jcifs.ntlmssp;

import java.io.IOException;
import java.net.UnknownHostException;
import jcifs.Config;
import jcifs.netbios.NbtAddress;
import jcifs.util.Hexdump;

public class Type2Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = Config.getProperty("jcifs.smb.client.domain", null);
    private static final int DEFAULT_FLAGS;
    private static final byte[] DEFAULT_TARGET_INFORMATION;
    private byte[] challenge;
    private byte[] context;
    private String target;
    private byte[] targetInformation;

    static {
        int i;
        int i2;
        if (Config.getBoolean("jcifs.smb.client.useUnicode", true)) {
            i = 1;
        } else {
            i = 2;
        }
        DEFAULT_FLAGS = i | 512;
        byte[] domain = new byte[0];
        if (DEFAULT_DOMAIN != null) {
            try {
                domain = DEFAULT_DOMAIN.getBytes(SmbConstants.UNI_ENCODING);
            } catch (IOException e) {
            }
        }
        int domainLength = domain.length;
        byte[] server = new byte[0];
        try {
            String host = NbtAddress.getLocalHost().getHostName();
            if (host != null) {
                try {
                    server = host.getBytes(SmbConstants.UNI_ENCODING);
                } catch (IOException e2) {
                }
            }
        } catch (UnknownHostException e3) {
        }
        int serverLength = server.length;
        if (domainLength > 0) {
            i2 = domainLength + 4;
        } else {
            i2 = 0;
        }
        byte[] targetInfo = new byte[(((serverLength > 0 ? serverLength + 4 : 0) + i2) + 4)];
        int offset = 0;
        if (domainLength > 0) {
            NtlmMessage.writeUShort(targetInfo, 0, 2);
            offset = 0 + 2;
            NtlmMessage.writeUShort(targetInfo, offset, domainLength);
            System.arraycopy(domain, 0, targetInfo, offset + 2, domainLength);
            offset = domainLength + 4;
        }
        if (serverLength > 0) {
            NtlmMessage.writeUShort(targetInfo, offset, 1);
            offset += 2;
            NtlmMessage.writeUShort(targetInfo, offset, serverLength);
            System.arraycopy(server, 0, targetInfo, offset + 2, serverLength);
        }
        DEFAULT_TARGET_INFORMATION = targetInfo;
    }

    public Type2Message() {
        this(getDefaultFlags(), null, null);
    }

    public Type2Message(Type1Message type1) {
        this(type1, null, null);
    }

    public Type2Message(Type1Message type1, byte[] challenge, String target) {
        int defaultFlags = getDefaultFlags(type1);
        if (type1 != null && target == null && type1.getFlag(4)) {
            target = getDefaultDomain();
        }
        this(defaultFlags, challenge, target);
    }

    public Type2Message(int flags, byte[] challenge, String target) {
        setFlags(flags);
        setChallenge(challenge);
        setTarget(target);
        if (target != null) {
            setTargetInformation(getDefaultTargetInformation());
        }
    }

    public Type2Message(byte[] material) throws IOException {
        parse(material);
    }

    public byte[] getChallenge() {
        return this.challenge;
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public byte[] getTargetInformation() {
        return this.targetInformation;
    }

    public void setTargetInformation(byte[] targetInformation) {
        this.targetInformation = targetInformation;
    }

    public byte[] getContext() {
        return this.context;
    }

    public void setContext(byte[] context) {
        this.context = context;
    }

    public byte[] toByteArray() {
        int i = 0;
        try {
            String targetName = getTarget();
            Object challenge = getChallenge();
            byte[] context = getContext();
            byte[] targetInformation = getTargetInformation();
            int flags = getFlags();
            byte[] target = new byte[0];
            if ((flags & 4) != 0) {
                if (targetName == null || targetName.length() == 0) {
                    flags &= -5;
                } else {
                    target = (flags & 1) != 0 ? targetName.getBytes(SmbConstants.UNI_ENCODING) : targetName.toUpperCase().getBytes(NtlmMessage.getOEMEncoding());
                }
            }
            if (targetInformation != null) {
                flags |= NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO;
                if (context == null) {
                    context = new byte[8];
                }
            }
            int data = 32;
            if (context != null) {
                data = 32 + 8;
            }
            if (targetInformation != null) {
                data += 8;
            }
            int length = target.length + data;
            if (targetInformation != null) {
                i = targetInformation.length;
            }
            byte[] type2 = new byte[(i + length)];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type2, 0, 8);
            NtlmMessage.writeULong(type2, 8, 2);
            NtlmMessage.writeSecurityBuffer(type2, 12, data, target);
            NtlmMessage.writeULong(type2, 20, flags);
            if (challenge == null) {
                challenge = new byte[8];
            }
            System.arraycopy(challenge, 0, type2, 24, 8);
            if (context != null) {
                System.arraycopy(context, 0, type2, 32, 8);
            }
            if (targetInformation != null) {
                NtlmMessage.writeSecurityBuffer(type2, 40, target.length + data, targetInformation);
            }
            return type2;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String target = getTarget();
        byte[] challenge = getChallenge();
        byte[] context = getContext();
        byte[] targetInformation = getTargetInformation();
        return "Type2Message[target=" + target + ",challenge=" + (challenge == null ? "null" : "<" + challenge.length + " bytes>") + ",context=" + (context == null ? "null" : "<" + context.length + " bytes>") + ",targetInformation=" + (targetInformation == null ? "null" : "<" + targetInformation.length + " bytes>") + ",flags=0x" + Hexdump.toHexString(getFlags(), 8) + "]";
    }

    public static int getDefaultFlags() {
        return DEFAULT_FLAGS;
    }

    public static int getDefaultFlags(Type1Message type1) {
        if (type1 == null) {
            return DEFAULT_FLAGS;
        }
        int type1Flags = type1.getFlags();
        int flags = 512 | ((type1Flags & 1) != 0 ? 1 : 2);
        if ((type1Flags & 4) == 0 || getDefaultDomain() == null) {
            return flags;
        }
        return flags | 65540;
    }

    public static String getDefaultDomain() {
        return DEFAULT_DOMAIN;
    }

    public static byte[] getDefaultTargetInformation() {
        return DEFAULT_TARGET_INFORMATION;
    }

    private void parse(byte[] material) throws IOException {
        int i;
        for (i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (NtlmMessage.readULong(material, 8) != 2) {
            throw new IOException("Not a Type 2 message.");
        }
        int flags = NtlmMessage.readULong(material, 20);
        setFlags(flags);
        String target = null;
        byte[] bytes = NtlmMessage.readSecurityBuffer(material, 12);
        if (bytes.length != 0) {
            target = new String(bytes, (flags & 1) != 0 ? SmbConstants.UNI_ENCODING : NtlmMessage.getOEMEncoding());
        }
        setTarget(target);
        for (i = 24; i < 32; i++) {
            if (material[i] != (byte) 0) {
                byte[] challenge = new byte[8];
                System.arraycopy(material, 24, challenge, 0, 8);
                setChallenge(challenge);
                break;
            }
        }
        int offset = NtlmMessage.readULong(material, 16);
        if (offset != 32 && material.length != 32) {
            for (i = 32; i < 40; i++) {
                if (material[i] != (byte) 0) {
                    byte[] context = new byte[8];
                    System.arraycopy(material, 32, context, 0, 8);
                    setContext(context);
                    break;
                }
            }
            if (offset != 40 && material.length != 40) {
                bytes = NtlmMessage.readSecurityBuffer(material, 40);
                if (bytes.length != 0) {
                    setTargetInformation(bytes);
                }
            }
        }
    }
}
