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
        byte[] targetInfo = new byte[((serverLength > 0 ? serverLength + 4 : 0) + i2 + 4)];
        int offset = 0;
        if (domainLength > 0) {
            writeUShort(targetInfo, 0, 2);
            int offset2 = 0 + 2;
            writeUShort(targetInfo, offset2, domainLength);
            System.arraycopy(domain, 0, targetInfo, offset2 + 2, domainLength);
            offset = domainLength + 4;
        }
        if (serverLength > 0) {
            writeUShort(targetInfo, offset, 1);
            int offset3 = offset + 2;
            writeUShort(targetInfo, offset3, serverLength);
            System.arraycopy(server, 0, targetInfo, offset3 + 2, serverLength);
        }
        DEFAULT_TARGET_INFORMATION = targetInfo;
    }

    public Type2Message() {
        this(getDefaultFlags(), (byte[]) null, (String) null);
    }

    public Type2Message(Type1Message type1) {
        this(type1, (byte[]) null, (String) null);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public Type2Message(Type1Message type1, byte[] challenge2, String target2) {
        this(r0, challenge2, target2);
        int defaultFlags = getDefaultFlags(type1);
        if (type1 != null && target2 == null && type1.getFlag(4)) {
            target2 = getDefaultDomain();
        }
    }

    public Type2Message(int flags, byte[] challenge2, String target2) {
        setFlags(flags);
        setChallenge(challenge2);
        setTarget(target2);
        if (target2 != null) {
            setTargetInformation(getDefaultTargetInformation());
        }
    }

    public Type2Message(byte[] material) throws IOException {
        parse(material);
    }

    public byte[] getChallenge() {
        return this.challenge;
    }

    public void setChallenge(byte[] challenge2) {
        this.challenge = challenge2;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target2) {
        this.target = target2;
    }

    public byte[] getTargetInformation() {
        return this.targetInformation;
    }

    public void setTargetInformation(byte[] targetInformation2) {
        this.targetInformation = targetInformation2;
    }

    public byte[] getContext() {
        return this.context;
    }

    public void setContext(byte[] context2) {
        this.context = context2;
    }

    public byte[] toByteArray() {
        int i = 0;
        try {
            String targetName = getTarget();
            byte[] challenge2 = getChallenge();
            byte[] context2 = getContext();
            byte[] targetInformation2 = getTargetInformation();
            int flags = getFlags();
            byte[] target2 = new byte[0];
            if ((flags & 4) != 0) {
                if (targetName == null || targetName.length() == 0) {
                    flags &= -5;
                } else {
                    target2 = (flags & 1) != 0 ? targetName.getBytes(SmbConstants.UNI_ENCODING) : targetName.toUpperCase().getBytes(getOEMEncoding());
                }
            }
            if (targetInformation2 != null) {
                flags |= NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO;
                if (context2 == null) {
                    context2 = new byte[8];
                }
            }
            int data = 32;
            if (context2 != null) {
                data = 32 + 8;
            }
            if (targetInformation2 != null) {
                data += 8;
            }
            int length = target2.length + data;
            if (targetInformation2 != null) {
                i = targetInformation2.length;
            }
            byte[] type2 = new byte[(i + length)];
            System.arraycopy(NTLMSSP_SIGNATURE, 0, type2, 0, 8);
            writeULong(type2, 8, 2);
            writeSecurityBuffer(type2, 12, data, target2);
            writeULong(type2, 20, flags);
            if (challenge2 == null) {
                challenge2 = new byte[8];
            }
            System.arraycopy(challenge2, 0, type2, 24, 8);
            if (context2 != null) {
                System.arraycopy(context2, 0, type2, 32, 8);
            }
            if (targetInformation2 != null) {
                writeSecurityBuffer(type2, 40, target2.length + data, targetInformation2);
            }
            return type2;
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage());
        }
    }

    public String toString() {
        String target2 = getTarget();
        byte[] challenge2 = getChallenge();
        byte[] context2 = getContext();
        byte[] targetInformation2 = getTargetInformation();
        return "Type2Message[target=" + target2 + ",challenge=" + (challenge2 == null ? "null" : "<" + challenge2.length + " bytes>") + ",context=" + (context2 == null ? "null" : "<" + context2.length + " bytes>") + ",targetInformation=" + (targetInformation2 == null ? "null" : "<" + targetInformation2.length + " bytes>") + ",flags=0x" + Hexdump.toHexString(getFlags(), 8) + "]";
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
        for (int i = 0; i < 8; i++) {
            if (material[i] != NTLMSSP_SIGNATURE[i]) {
                throw new IOException("Not an NTLMSSP message.");
            }
        }
        if (readULong(material, 8) != 2) {
            throw new IOException("Not a Type 2 message.");
        }
        int flags = readULong(material, 20);
        setFlags(flags);
        String target2 = null;
        byte[] bytes = readSecurityBuffer(material, 12);
        if (bytes.length != 0) {
            new String(bytes, (flags & 1) != 0 ? SmbConstants.UNI_ENCODING : getOEMEncoding());
        }
        setTarget(target2);
        int i2 = 24;
        while (true) {
            if (i2 >= 32) {
                break;
            } else if (material[i2] != 0) {
                byte[] challenge2 = new byte[8];
                System.arraycopy(material, 24, challenge2, 0, 8);
                setChallenge(challenge2);
                break;
            } else {
                i2++;
            }
        }
        int offset = readULong(material, 16);
        if (offset != 32 && material.length != 32) {
            int i3 = 32;
            while (true) {
                if (i3 >= 40) {
                    break;
                } else if (material[i3] != 0) {
                    byte[] context2 = new byte[8];
                    System.arraycopy(material, 32, context2, 0, 8);
                    setContext(context2);
                    break;
                } else {
                    i3++;
                }
            }
            if (offset != 40 && material.length != 40) {
                byte[] bytes2 = readSecurityBuffer(material, 40);
                if (bytes2.length != 0) {
                    setTargetInformation(bytes2);
                }
            }
        }
    }
}
