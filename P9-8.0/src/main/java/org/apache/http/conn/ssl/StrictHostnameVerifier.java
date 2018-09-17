package org.apache.http.conn.ssl;

import javax.net.ssl.SSLException;

@Deprecated
public class StrictHostnameVerifier extends AbstractVerifier {
    public final void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
        verify(host, cns, subjectAlts, true);
    }

    public final String toString() {
        return "STRICT";
    }
}
