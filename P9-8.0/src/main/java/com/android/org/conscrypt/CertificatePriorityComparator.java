package com.android.org.conscrypt;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CertificatePriorityComparator implements Comparator<X509Certificate> {
    private static final Map<String, Integer> ALGORITHM_OID_PRIORITY_MAP = new HashMap();
    private static final Integer PRIORITY_MD5 = Integer.valueOf(6);
    private static final Integer PRIORITY_SHA1 = Integer.valueOf(5);
    private static final Integer PRIORITY_SHA224 = Integer.valueOf(4);
    private static final Integer PRIORITY_SHA256 = Integer.valueOf(3);
    private static final Integer PRIORITY_SHA384 = Integer.valueOf(2);
    private static final Integer PRIORITY_SHA512 = Integer.valueOf(1);
    private static final Integer PRIORITY_UNKNOWN = Integer.valueOf(-1);

    static {
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.13", PRIORITY_SHA512);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.12", PRIORITY_SHA384);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.11", PRIORITY_SHA256);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.14", PRIORITY_SHA224);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.5", PRIORITY_SHA1);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.113549.1.1.4", PRIORITY_MD5);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.10045.4.3.4", PRIORITY_SHA512);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.10045.4.3.3", PRIORITY_SHA384);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.10045.4.3.2", PRIORITY_SHA256);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.10045.4.3.1", PRIORITY_SHA224);
        ALGORITHM_OID_PRIORITY_MAP.put("1.2.840.10045.4.1", PRIORITY_SHA1);
    }

    public int compare(X509Certificate lhs, X509Certificate rhs) {
        boolean lhsSelfSigned = lhs.getSubjectDN().equals(lhs.getIssuerDN());
        boolean rhsSelfSigned = rhs.getSubjectDN().equals(rhs.getIssuerDN());
        if (lhsSelfSigned != rhsSelfSigned) {
            return rhsSelfSigned ? 1 : -1;
        }
        int result = compareStrength(rhs, lhs);
        if (result != 0) {
            return result;
        }
        result = rhs.getNotAfter().compareTo(lhs.getNotAfter());
        if (result != 0) {
            return result;
        }
        return rhs.getNotBefore().compareTo(lhs.getNotBefore());
    }

    private int compareStrength(X509Certificate lhs, X509Certificate rhs) {
        PublicKey lhsPublicKey = lhs.getPublicKey();
        PublicKey rhsPublicKey = rhs.getPublicKey();
        int result = compareKeyAlgorithm(lhsPublicKey, rhsPublicKey);
        if (result != 0) {
            return result;
        }
        result = compareKeySize(lhsPublicKey, rhsPublicKey);
        if (result != 0) {
            return result;
        }
        return compareSignatureAlgorithm(lhs, rhs);
    }

    private int compareKeyAlgorithm(PublicKey lhs, PublicKey rhs) {
        String lhsAlgorithm = lhs.getAlgorithm().toUpperCase(Locale.US);
        if (lhsAlgorithm.equals(rhs.getAlgorithm().toUpperCase(Locale.US))) {
            return 0;
        }
        if ("EC".equals(lhsAlgorithm)) {
            return 1;
        }
        return -1;
    }

    private int compareKeySize(PublicKey lhs, PublicKey rhs) {
        if (lhs.getAlgorithm().toUpperCase(Locale.US).equals(rhs.getAlgorithm().toUpperCase(Locale.US))) {
            return getKeySize(lhs) - getKeySize(rhs);
        }
        throw new IllegalArgumentException("Keys are not of the same type");
    }

    private int getKeySize(PublicKey pkey) {
        if (pkey instanceof ECPublicKey) {
            return ((ECPublicKey) pkey).getParams().getCurve().getField().getFieldSize();
        }
        if (pkey instanceof RSAPublicKey) {
            return ((RSAPublicKey) pkey).getModulus().bitLength();
        }
        throw new IllegalArgumentException("Unsupported public key type: " + pkey.getClass().getName());
    }

    private int compareSignatureAlgorithm(X509Certificate lhs, X509Certificate rhs) {
        Integer lhsPriority = (Integer) ALGORITHM_OID_PRIORITY_MAP.get(lhs.getSigAlgOID());
        Integer rhsPriority = (Integer) ALGORITHM_OID_PRIORITY_MAP.get(rhs.getSigAlgOID());
        if (lhsPriority == null) {
            lhsPriority = PRIORITY_UNKNOWN;
        }
        if (rhsPriority == null) {
            rhsPriority = PRIORITY_UNKNOWN;
        }
        return rhsPriority.intValue() - lhsPriority.intValue();
    }
}
