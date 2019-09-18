package javax.net.ssl;

public abstract class SNIMatcher {
    private final int type;

    public abstract boolean matches(SNIServerName sNIServerName);

    protected SNIMatcher(int type2) {
        if (type2 < 0) {
            throw new IllegalArgumentException("Server name type cannot be less than zero");
        } else if (type2 <= 255) {
            this.type = type2;
        } else {
            throw new IllegalArgumentException("Server name type cannot be greater than 255");
        }
    }

    public final int getType() {
        return this.type;
    }
}
