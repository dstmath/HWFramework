package com.android.okhttp.internal.tls;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.security.auth.x500.X500Principal;

public final class RealTrustRootIndex implements TrustRootIndex {
    private final Map<X500Principal, List<X509Certificate>> subjectToCaCerts = new LinkedHashMap();

    public RealTrustRootIndex(X509Certificate... caCerts) {
        for (X509Certificate caCert : caCerts) {
            X500Principal subject = caCert.getSubjectX500Principal();
            List<X509Certificate> subjectCaCerts = (List) this.subjectToCaCerts.get(subject);
            if (subjectCaCerts == null) {
                subjectCaCerts = new ArrayList(1);
                this.subjectToCaCerts.put(subject, subjectCaCerts);
            }
            subjectCaCerts.add(caCert);
        }
    }

    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        List<X509Certificate> subjectCaCerts = (List) this.subjectToCaCerts.get(cert.getIssuerX500Principal());
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
}
