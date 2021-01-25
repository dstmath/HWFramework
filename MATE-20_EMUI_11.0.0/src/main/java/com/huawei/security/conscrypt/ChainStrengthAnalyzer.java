package com.huawei.security.conscrypt;

import android.text.TextUtils;
import com.huawei.hsm.permission.StubController;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

public final class ChainStrengthAnalyzer {
    private static int MIN_EC_FIELD_SIZE_BITS = 160;
    private static int MIN_RSA_MODULUS_LEN_BITS = StubController.PERMISSION_CAMERA;
    private static String[] SIGNATURE_ALGORITHM_OID_BLACKLIST = {"1.2.840.113549.1.1.2", "1.2.840.113549.1.1.3", "1.2.840.113549.1.1.4"};
    private static String X25519_ALGORITHM_OID = "1.3.101.110";

    public static void check(X509Certificate[] chain) throws CertificateException {
        for (X509Certificate cert : chain) {
            try {
                checkCert(cert);
            } catch (CertificateException e) {
                throw new CertificateException("Unacceptable certificate: " + cert.getSubjectX500Principal(), e);
            }
        }
    }

    public static void check(List<X509Certificate> chain) throws CertificateException {
        for (X509Certificate cert : chain) {
            try {
                checkCert(cert);
            } catch (CertificateException e) {
                throw new CertificateException("Unacceptable certificate: " + cert.getSubjectX500Principal(), e);
            }
        }
    }

    public static void checkCert(X509Certificate cert) throws CertificateException {
        checkKeyLength(cert);
        checkSignatureAlgorithm(cert);
    }

    private static void checkKeyLength(X509Certificate cert) throws CertificateException {
        Object pubkey = cert.getPublicKey();
        if (pubkey instanceof RSAPublicKey) {
            if (((RSAPublicKey) pubkey).getModulus().bitLength() < MIN_RSA_MODULUS_LEN_BITS) {
                throw new CertificateException("RSA modulus is < " + MIN_RSA_MODULUS_LEN_BITS + " bits");
            }
        } else if (pubkey instanceof ECPublicKey) {
            if (((ECPublicKey) pubkey).getParams().getCurve().getField().getFieldSize() < MIN_EC_FIELD_SIZE_BITS) {
                throw new CertificateException("EC key field size is < " + MIN_EC_FIELD_SIZE_BITS + " bits");
            }
        } else if (!TextUtils.equals(((PublicKey) pubkey).getAlgorithm(), X25519_ALGORITHM_OID)) {
            throw new CertificateException("Rejecting unknown key class " + pubkey.getClass().getName());
        }
    }

    private static void checkSignatureAlgorithm(X509Certificate cert) throws CertificateException {
        String oid = cert.getSigAlgOID();
        for (String blacklisted : SIGNATURE_ALGORITHM_OID_BLACKLIST) {
            if (TextUtils.equals(oid, blacklisted)) {
                throw new CertificateException("Signature uses an insecure hash function: " + oid);
            }
        }
    }
}
