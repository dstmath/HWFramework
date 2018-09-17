package java.nio.file;

public class FileSystemNotFoundException extends RuntimeException {
    static final long serialVersionUID = 7999581764446402397L;

    public FileSystemNotFoundException(String msg) {
        super(msg);
    }
}
