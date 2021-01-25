package com.huawei.okhttp3;

import java.io.IOException;

public enum Protocol {
    HTTP_1_0("http/1.0"),
    HTTP_1_1("http/1.1"),
    SPDY_3("spdy/3.1"),
    HTTP_2("h2"),
    H2_PRIOR_KNOWLEDGE("h2_prior_knowledge"),
    QUIC("quic");
    
    private final String protocol;

    private Protocol(String protocol2) {
        this.protocol = protocol2;
    }

    public static Protocol get(String protocol2) throws IOException {
        if (protocol2.equals(HTTP_1_0.protocol)) {
            return HTTP_1_0;
        }
        if (protocol2.equals(HTTP_1_1.protocol)) {
            return HTTP_1_1;
        }
        if (protocol2.equals(H2_PRIOR_KNOWLEDGE.protocol)) {
            return H2_PRIOR_KNOWLEDGE;
        }
        if (protocol2.equals(HTTP_2.protocol)) {
            return HTTP_2;
        }
        if (protocol2.equals(SPDY_3.protocol)) {
            return SPDY_3;
        }
        if (protocol2.equals(QUIC.protocol)) {
            return QUIC;
        }
        throw new IOException("Unexpected protocol: " + protocol2);
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return this.protocol;
    }
}
