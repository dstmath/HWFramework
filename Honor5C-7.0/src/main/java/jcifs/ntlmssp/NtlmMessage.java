package jcifs.ntlmssp;

public abstract class NtlmMessage implements NtlmFlags {
    protected static final byte[] NTLMSSP_SIGNATURE = null;
    private static final String OEM_ENCODING = null;
    protected static final String UNI_ENCODING = "UTF-16LE";
    private int flags;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.ntlmssp.NtlmMessage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.ntlmssp.NtlmMessage.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: jcifs.ntlmssp.NtlmMessage.<clinit>():void");
    }

    public abstract byte[] toByteArray();

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean getFlag(int flag) {
        return (getFlags() & flag) != 0;
    }

    public void setFlag(int flag, boolean value) {
        setFlags(value ? getFlags() | flag : getFlags() & (flag ^ -1));
    }

    static int readULong(byte[] src, int index) {
        return (((src[index] & 255) | ((src[index + 1] & 255) << 8)) | ((src[index + 2] & 255) << 16)) | ((src[index + 3] & 255) << 24);
    }

    static int readUShort(byte[] src, int index) {
        return (src[index] & 255) | ((src[index + 1] & 255) << 8);
    }

    static byte[] readSecurityBuffer(byte[] src, int index) {
        int length = readUShort(src, index);
        byte[] buffer = new byte[length];
        System.arraycopy(src, readULong(src, index + 4), buffer, 0, length);
        return buffer;
    }

    static void writeULong(byte[] dest, int offset, int ulong) {
        dest[offset] = (byte) (ulong & 255);
        dest[offset + 1] = (byte) ((ulong >> 8) & 255);
        dest[offset + 2] = (byte) ((ulong >> 16) & 255);
        dest[offset + 3] = (byte) ((ulong >> 24) & 255);
    }

    static void writeUShort(byte[] dest, int offset, int ushort) {
        dest[offset] = (byte) (ushort & 255);
        dest[offset + 1] = (byte) ((ushort >> 8) & 255);
    }

    static void writeSecurityBuffer(byte[] dest, int offset, int bodyOffset, byte[] src) {
        int length;
        if (src != null) {
            length = src.length;
        } else {
            length = 0;
        }
        if (length != 0) {
            writeUShort(dest, offset, length);
            writeUShort(dest, offset + 2, length);
            writeULong(dest, offset + 4, bodyOffset);
            System.arraycopy(src, 0, dest, bodyOffset, length);
        }
    }

    static String getOEMEncoding() {
        return OEM_ENCODING;
    }
}
