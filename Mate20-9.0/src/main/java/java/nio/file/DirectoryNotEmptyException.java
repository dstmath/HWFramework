package java.nio.file;

public class DirectoryNotEmptyException extends FileSystemException {
    static final long serialVersionUID = 3056667871802779003L;

    public DirectoryNotEmptyException(String dir) {
        super(dir);
    }
}
