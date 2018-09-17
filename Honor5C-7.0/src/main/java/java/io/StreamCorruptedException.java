package java.io;

public class StreamCorruptedException extends ObjectStreamException {
    private static final long serialVersionUID = 8983558202217591746L;

    public StreamCorruptedException(String reason) {
        super(reason);
    }
}
