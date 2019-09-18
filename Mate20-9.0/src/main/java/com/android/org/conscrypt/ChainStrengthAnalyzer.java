package com.android.org.conscrypt;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

public final class ChainStrengthAnalyzer {
    private static final int MIN_DSA_P_LEN_BITS = 1024;
    private static final int MIN_DSA_Q_LEN_BITS = 160;
    private static final int MIN_EC_FIELD_SIZE_BITS = 160;
    private static final int MIN_RSA_MODULUS_LEN_BITS = 1024;
    private static final String[] SIGNATURE_ALGORITHM_OID_BLACKLIST = {"1.2.840.113549.1.1.2", "1.2.840.113549.1.1.3", "1.2.840.113549.1.1.4"};

    public static final void check(X509Certificate[] chain) throws CertificateException {
        int length = chain.length;
        int i = 0;
        while (i < length) {
            X509Certificate cert = chain[i];
            try {
                checkCert(cert);
                i++;
            } catch (CertificateException e) {
                throw new CertificateException("Unacceptable certificate: " + cert.getSubjectX500Principal(), e);
            }
        }
    }

    public static final void check(List<X509Certificate> chain) throws CertificateException {
        for (X509Certificate cert : chain) {
            try {
                checkCert(cert);
            } catch (CertificateException e) {
                throw new CertificateException("Unacceptable certificate: " + cert.getSubjectX500Principal(), e);
            }
        }
    }

    public static final void checkCert(X509Certificate cert) throws CertificateException {
        checkKeyLength(cert);
        checkSignatureAlgorithm(cert);
    }

    private static void checkKeyLength(X509Certificate cert) throws CertificateException {
        PublicKey publicKey = cert.getPublicKey();
        if (publicKey instanceof RSAPublicKey) {
            if (((RSAPublicKey) publicKey).getModulus().bitLength() < 1024) {
                throw new CertificateException("RSA modulus is < 1024 bits");
            }
        } else if ((publicKey instanceof ECPublicKey) != 0) {
            if (((ECPublicKey) publicKey).getParams().getCurve().getField().getFieldSize() < 160) {
                throw new CertificateException("EC key field size is < 160 bits");
            }
        } else if ((publicKey instanceof DSAPublicKey) != 0) {
            int pLength = ((DSAPublicKey) publicKey).getParams().getP().bitLength();
            int qLength = ((DSAPublicKey) publicKey).getParams().getQ().bitLength();
            if (pLength < 1024 || qLength < 160) {
                throw new CertificateException("DSA key length is < (1024, 160) bits");
            }
        } else {
            throw new CertificateException("Rejecting unknown key class " + publicKey.getClass().getName());
        }
    }

    private static void checkSignatureAlgorithm(X509Certificate cert) throws CertificateException {
        String oid = cert.getSigAlgOID();
        String[] strArr = SIGNATURE_ALGORITHM_OID_BLACKLIST;
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            if (!oid.equals(strArr[i])) {
                i++;
            } else {
                throw new CertificateException("Signature uses an insecure hash function: " + oid);
            }
        }
    }
}
