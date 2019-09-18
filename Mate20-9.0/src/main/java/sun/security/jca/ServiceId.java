package sun.security.jca;

public final class ServiceId {
    public final String algorithm;
    public final String type;

    public ServiceId(String type2, String algorithm2) {
        this.type = type2;
        this.algorithm = algorithm2;
    }
}
