package javax.net.ssl;

public interface HostnameVerifier {
    boolean verify(String str, SSLSession sSLSession);
}
