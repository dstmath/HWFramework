package java.nio.file;

public class NotLinkException extends FileSystemException {
    static final long serialVersionUID = -388655596416518021L;

    public NotLinkException(String file) {
        super(file);
    }

    public NotLinkException(String file, String other, String reason) {
        super(file, other, reason);
    }
}
