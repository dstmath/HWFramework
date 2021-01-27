package org.apache.http.conn.ssl;

import javax.net.ssl.SSLException;

@Deprecated
public class StrictHostnameVerifier extends AbstractVerifier {
    @Override // org.apache.http.conn.ssl.X509HostnameVerifier
    public final void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, true);
    }

    @Override // java.lang.Object
    public final String toString() {
        return "STRICT";
    }
}
