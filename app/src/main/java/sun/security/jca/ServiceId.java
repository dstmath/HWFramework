package sun.security.jca;

public final class ServiceId {
    public final String algorithm;
    public final String type;

    public ServiceId(String type, String algorithm) {
        this.type = type;
        this.algorithm = algorithm;
    }
}
