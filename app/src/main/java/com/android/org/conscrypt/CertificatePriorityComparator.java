package com.android.org.conscrypt;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public final class CertificatePriorityComparator implements Comparator<X509Certificate> {
    private static final Map<String, Integer> ALGORITHM_OID_PRIORITY_MAP = null;
    private static final Integer PRIORITY_MD5 = null;
    private static final Integer PRIORITY_SHA1 = null;
    private static final Integer PRIORITY_SHA224 = null;
    private static final Integer PRIORITY_SHA256 = null;
    private static final Integer PRIORITY_SHA384 = null;
    private static final Integer PRIORITY_SHA512 = null;
    private static final Integer PRIORITY_UNKNOWN = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.CertificatePriorityComparator.<clinit>():void");
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
