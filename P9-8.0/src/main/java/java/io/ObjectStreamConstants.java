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
    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new SerializablePermission("enableSubclassImplementation");
    public static final SerializablePermission SUBSTITUTION_PERMISSION = new SerializablePermission("enableSubstitution");
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
}
