package com.android.org.conscrypt;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public interface CertPinManager {
    void checkChainPinning(String str, List<X509Certificate> list) throws CertificateException;
}
