package android.util.proto;

public class WireTypeMismatchException extends ProtoParseException {
    public WireTypeMismatchException(String msg) {
        super(msg);
    }
}
