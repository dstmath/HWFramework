package java.nio.file;

public class AtomicMoveNotSupportedException extends FileSystemException {
    static final long serialVersionUID = 5402760225333135579L;

    public AtomicMoveNotSupportedException(String source, String target, String reason) {
        super(source, target, reason);
    }
}
