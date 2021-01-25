package org.apache.http.conn.ssl;

@Deprecated
public class AllowAllHostnameVerifier extends AbstractVerifier {
    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, String[] cns, String[] subjectAlts) {
    }

    @Override // java.lang.Object
    public final String toString() {
        return "ALLOW_ALL";
    }
}
