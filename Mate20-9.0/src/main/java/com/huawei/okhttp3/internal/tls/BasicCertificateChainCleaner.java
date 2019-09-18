package com.huawei.okhttp3.internal.tls;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;

public final class BasicCertificateChainCleaner extends CertificateChainCleaner {
    private static final int MAX_SIGNERS = 9;
    private final TrustRootIndex trustRootIndex;

    public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex2) {
        this.trustRootIndex = trustRootIndex2;
    }

    public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
        Deque<Certificate> queue = new ArrayDeque<>(chain);
        List<Certificate> result = new ArrayList<>();
        result.add(queue.removeFirst());
        boolean foundTrustedCertificate = false;
        for (int c = 0; c < 9; c++) {
            X509Certificate toVerify = (X509Certificate) result.get(result.size() - 1);
            X509Certificate trustedCert = this.trustRootIndex.findByIssuerAndSignature(toVerify);
            if (trustedCert != null) {
                if (result.size() > 1 || !toVerify.equals(trustedCert)) {
                    result.add(trustedCert);
                }
                if (verifySignature(trustedCert, trustedCert)) {
                    return result;
                }
                foundTrustedCertificate = true;
            } else {
                Iterator<Certificate> i = queue.iterator();
                while (i.hasNext()) {
                    X509Certificate signingCert = (X509Certificate) i.next();
                    if (verifySignature(toVerify, signingCert)) {
                        i.remove();
                        result.add(signingCert);
                    }
                }
                if (foundTrustedCertificate) {
                    return result;
                }
                throw new SSLPeerUnverifiedException("Failed to find a trusted cert that signed " + toVerify);
            }
        }
        throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
    }

    private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
        if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN())) {
            return false;
        }
        try {
            toVerify.verify(signingCert.getPublicKey());
            return true;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    public int hashCode() {
        return this.trustRootIndex.hashCode();
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof BasicCertificateChainCleaner) || !((BasicCertificateChainCleaner) other).trustRootIndex.equals(this.trustRootIndex)) {
            z = false;
        }
        return z;
    }
}
