package jcifs.ntlmssp;

import java.io.IOException;
import jcifs.smb.SmbNamedPipe;
import jcifs.util.Hexdump;

public class Type2Message extends NtlmMessage {
    private static final String DEFAULT_DOMAIN = null;
    private static final int DEFAULT_FLAGS = 0;
    private static final byte[] DEFAULT_TARGET_INFORMATION = null;
    private byte[] challenge;
    private byte[] context;
    private String target;
    private byte[] targetInformation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.ntlmssp.Type2Message.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.ntlmssp.Type2Message.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.ntlmssp.Type2Message.<clinit>():void");
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
        int flags = SmbNamedPipe.PIPE_TYPE_TRANSACT | ((type1Flags & 1) != 0 ? 1 : 2);
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
            if (material[i] != null) {
                byte[] challenge = new byte[8];
                System.arraycopy(material, 24, challenge, 0, 8);
                setChallenge(challenge);
                break;
            }
        }
        int offset = NtlmMessage.readULong(material, 16);
        if (offset != 32 && material.length != 32) {
            for (i = 32; i < 40; i++) {
                if (material[i] != null) {
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
