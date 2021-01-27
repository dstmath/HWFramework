package javax.sip.address;

public interface Hop {
    String getHost();

    int getPort();

    String getTransport();

    boolean isURIRoute();

    void setURIRouteFlag();

    @Override // java.lang.Object
    String toString();
}
