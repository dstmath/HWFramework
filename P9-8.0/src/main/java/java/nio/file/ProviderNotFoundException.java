package java.nio.file;

public class ProviderNotFoundException extends RuntimeException {
    static final long serialVersionUID = -1880012509822920354L;

    public ProviderNotFoundException(String msg) {
        super(msg);
    }
}
