package java.security.cert;

public abstract class CRL {
    private String type;

    public abstract boolean isRevoked(Certificate certificate);

    public abstract String toString();

    protected CRL(String type2) {
        this.type = type2;
    }

    public final String getType() {
        return this.type;
    }
}
