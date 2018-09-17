package java.io;

public interface ObjectStreamConstants {
    public static final int PROTOCOL_VERSION_1 = 1;
    public static final int PROTOCOL_VERSION_2 = 2;
    public static final byte SC_BLOCK_DATA = (byte) 8;
    public static final byte SC_ENUM = (byte) 16;
    public static final byte SC_EXTERNALIZABLE = (byte) 4;
    public static final byte SC_SERIALIZABLE = (byte) 2;
    public static final byte SC_WRITE_METHOD = (byte) 1;
    public static final short STREAM_MAGIC = (short) -21267;
    public static final short STREAM_VERSION = (short) 5;
    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = null;
    public static final SerializablePermission SUBSTITUTION_PERMISSION = null;
    public static final byte TC_ARRAY = (byte) 117;
    public static final byte TC_BASE = (byte) 112;
    public static final byte TC_BLOCKDATA = (byte) 119;
    public static final byte TC_BLOCKDATALONG = (byte) 122;
    public static final byte TC_CLASS = (byte) 118;
    public static final byte TC_CLASSDESC = (byte) 114;
    public static final byte TC_ENDBLOCKDATA = (byte) 120;
    public static final byte TC_ENUM = (byte) 126;
    public static final byte TC_EXCEPTION = (byte) 123;
    public static final byte TC_LONGSTRING = (byte) 124;
    public static final byte TC_MAX = (byte) 126;
    public static final byte TC_NULL = (byte) 112;
    public static final byte TC_OBJECT = (byte) 115;
    public static final byte TC_PROXYCLASSDESC = (byte) 125;
    public static final byte TC_REFERENCE = (byte) 113;
    public static final byte TC_RESET = (byte) 121;
    public static final byte TC_STRING = (byte) 116;
    public static final int baseWireHandle = 8257536;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectStreamConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectStreamConstants.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectStreamConstants.<clinit>():void");
    }
}
