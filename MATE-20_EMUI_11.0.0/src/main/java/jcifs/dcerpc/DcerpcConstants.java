package jcifs.dcerpc;

public interface DcerpcConstants {
    public static final int DCERPC_CONC_MPX = 16;
    public static final int DCERPC_DID_NOT_EXECUTE = 32;
    public static final int DCERPC_FIRST_FRAG = 1;
    public static final int DCERPC_LAST_FRAG = 2;
    public static final int DCERPC_MAYBE = 64;
    public static final int DCERPC_OBJECT_UUID = 128;
    public static final int DCERPC_PENDING_CANCEL = 4;
    public static final int DCERPC_RESERVED_1 = 8;
    public static final UUID DCERPC_UUID_SYNTAX_NDR = new UUID("8a885d04-1ceb-11c9-9fe8-08002b104860");
}
