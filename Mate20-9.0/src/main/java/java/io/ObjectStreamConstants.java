package java.io;

public interface ObjectStreamConstants {
    public static final int PROTOCOL_VERSION_1 = 1;
    public static final int PROTOCOL_VERSION_2 = 2;
    public static final byte SC_BLOCK_DATA = 8;
    public static final byte SC_ENUM = 16;
    public static final byte SC_EXTERNALIZABLE = 4;
    public static final byte SC_SERIALIZABLE = 2;
    public static final byte SC_WRITE_METHOD = 1;
    public static final short STREAM_MAGIC = -21267;
    public static final short STREAM_VERSION = 5;
    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new SerializablePermission("enableSubclassImplementation");
    public static final SerializablePermission SUBSTITUTION_PERMISSION = new SerializablePermission("enableSubstitution");
    public static final byte TC_ARRAY = 117;
    public static final byte TC_BASE = 112;
    public static final byte TC_BLOCKDATA = 119;
    public static final byte TC_BLOCKDATALONG = 122;
    public static final byte TC_CLASS = 118;
    public static final byte TC_CLASSDESC = 114;
    public static final byte TC_ENDBLOCKDATA = 120;
    public static final byte TC_ENUM = 126;
    public static final byte TC_EXCEPTION = 123;
    public static final byte TC_LONGSTRING = 124;
    public static final byte TC_MAX = 126;
    public static final byte TC_NULL = 112;
    public static final byte TC_OBJECT = 115;
    public static final byte TC_PROXYCLASSDESC = 125;
    public static final byte TC_REFERENCE = 113;
    public static final byte TC_RESET = 121;
    public static final byte TC_STRING = 116;
    public static final int baseWireHandle = 8257536;
}
