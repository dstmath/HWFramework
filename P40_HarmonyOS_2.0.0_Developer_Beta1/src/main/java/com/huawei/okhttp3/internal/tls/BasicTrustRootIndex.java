package com.huawei.okhttp3.internal.tls;

import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

@Deprecated
public final class BasicTrustRootIndex implements TrustRootIndex {
    private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts = new LinkedHashMap();

    public BasicTrustRootIndex(X509Certificate... caCerts) {
        for (X509Certificate caCert : caCerts) {
            X500Principal subject = caCert.getSubjectX500Principal();
            Set<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(subject);
            if (subjectCaCerts == null) {
                subjectCaCerts = new LinkedHashSet(1);
                this.subjectToCaCerts.put(subject, subjectCaCerts);
            }
            subjectCaCerts.add(caCert);
        }
    }

    @Override // com.huawei.okhttp3.internal.tls.TrustRootIndex
    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        Set<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(cert.getIssuerX500Principal());
        if (subjectCaCerts == null) {
            return null;
        }
        for (X509Certificate caCert : subjectCaCerts) {
            try {
                cert.verify(caCert.getPublicKey());
                return caCert;
            } catch (Exception e) {
            }
        }
        return null;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BasicTrustRootIndex) || !((BasicTrustRootIndex) other).subjectToCaCerts.equals(this.subjectToCaCerts)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.subjectToCaCerts.hashCode();
    }
}
