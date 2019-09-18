package java.util;

public class MissingResourceException extends RuntimeException {
    private static final long serialVersionUID = -4876345176062000401L;
    private String className;
    private String key;

    public MissingResourceException(String s, String className2, String key2) {
        super(s);
        this.className = className2;
        this.key = key2;
    }

    MissingResourceException(String message, String className2, String key2, Throwable cause) {
        super(message, cause);
        this.className = className2;
        this.key = key2;
    }

    public String getClassName() {
        return this.className;
    }

    public String getKey() {
        return this.key;
    }
}
