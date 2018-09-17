package java.nio.file;

public class ProviderMismatchException extends IllegalArgumentException {
    static final long serialVersionUID = 4990847485741612530L;

    public ProviderMismatchException(String msg) {
        super(msg);
    }
}
