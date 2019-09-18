package org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsAuthentication {
    TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException;

    void notifyServerCertificate(Certificate certificate) throws IOException;
}
