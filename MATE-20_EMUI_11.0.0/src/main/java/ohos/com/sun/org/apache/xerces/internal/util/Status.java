package ohos.com.sun.org.apache.xerces.internal.util;

public enum Status {
    SET(-3, false),
    UNKNOWN(-2, false),
    RECOGNIZED(-1, false),
    NOT_SUPPORTED(0, true),
    NOT_RECOGNIZED(1, true),
    NOT_ALLOWED(2, true);
    
    private boolean isExceptional;
    private final short type;

    private Status(short s, boolean z) {
        this.type = s;
        this.isExceptional = z;
    }

    public short getType() {
        return this.type;
    }

    public boolean isExceptional() {
        return this.isExceptional;
    }
}
