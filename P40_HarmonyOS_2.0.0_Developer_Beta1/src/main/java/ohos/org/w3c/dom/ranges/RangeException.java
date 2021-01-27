package ohos.org.w3c.dom.ranges;

public class RangeException extends RuntimeException {
    public static final short BAD_BOUNDARYPOINTS_ERR = 1;
    public static final short INVALID_NODE_TYPE_ERR = 2;
    public short code;

    public RangeException(short s, String str) {
        super(str);
        this.code = s;
    }
}
