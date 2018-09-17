package java.io;

public class NotActiveException extends ObjectStreamException {
    private static final long serialVersionUID = -3893467273049808895L;

    public NotActiveException(String reason) {
        super(reason);
    }
}
