package com.android.org.conscrypt.ct;

import java.security.cert.X509Certificate;

public interface CTPolicy {
    boolean doesResultConformToPolicy(CTVerificationResult cTVerificationResult, String str, X509Certificate[] x509CertificateArr);
}
