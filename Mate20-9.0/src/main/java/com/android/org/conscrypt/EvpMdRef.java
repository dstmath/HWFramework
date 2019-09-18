package com.android.org.conscrypt;

import java.security.NoSuchAlgorithmException;
import java.util.Locale;

final class EvpMdRef {
    static final String MGF1_ALGORITHM_NAME = "MGF1";
    static final String MGF1_OID = "1.2.840.113549.1.1.8";

    static final class MD5 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("md5");
        static final String JCA_NAME = "MD5";
        static final String OID = "1.2.840.113549.2.5";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private MD5() {
        }
    }

    static final class SHA1 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha1");
        static final String JCA_NAME = "SHA-1";
        static final String OID = "1.3.14.3.2.26";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private SHA1() {
        }
    }

    static final class SHA224 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha224");
        static final String JCA_NAME = "SHA-224";
        static final String OID = "2.16.840.1.101.3.4.2.4";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private SHA224() {
        }
    }

    static final class SHA256 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha256");
        static final String JCA_NAME = "SHA-256";
        static final String OID = "2.16.840.1.101.3.4.2.1";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private SHA256() {
        }
    }

    static final class SHA384 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha384");
        static final String JCA_NAME = "SHA-384";
        static final String OID = "2.16.840.1.101.3.4.2.2";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private SHA384() {
        }
    }

    static final class SHA512 {
        static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha512");
        static final String JCA_NAME = "SHA-512";
        static final String OID = "2.16.840.1.101.3.4.2.3";
        static final int SIZE_BYTES = NativeCrypto.EVP_MD_size(EVP_MD);

        private SHA512() {
        }
    }

    static String getJcaDigestAlgorithmStandardName(String algorithm) {
        String algorithmUpper = algorithm.toUpperCase(Locale.US);
        if ("SHA-256".equals(algorithmUpper) || "2.16.840.1.101.3.4.2.1".equals(algorithmUpper)) {
            return "SHA-256";
        }
        if ("SHA-512".equals(algorithmUpper) || "2.16.840.1.101.3.4.2.3".equals(algorithmUpper)) {
            return "SHA-512";
        }
        if ("SHA-1".equals(algorithmUpper) || "1.3.14.3.2.26".equals(algorithmUpper)) {
            return "SHA-1";
        }
        if ("SHA-384".equals(algorithmUpper) || "2.16.840.1.101.3.4.2.2".equals(algorithmUpper)) {
            return "SHA-384";
        }
        if ("SHA-224".equals(algorithmUpper) || "2.16.840.1.101.3.4.2.4".equals(algorithmUpper)) {
            return "SHA-224";
        }
        return null;
    }

    static long getEVP_MDByJcaDigestAlgorithmStandardName(String algorithm) throws NoSuchAlgorithmException {
        String algorithmUpper = algorithm.toUpperCase(Locale.US);
        if ("SHA-256".equals(algorithmUpper)) {
            return SHA256.EVP_MD;
        }
        if ("SHA-512".equals(algorithmUpper)) {
            return SHA512.EVP_MD;
        }
        if ("SHA-1".equals(algorithmUpper)) {
            return SHA1.EVP_MD;
        }
        if ("SHA-384".equals(algorithmUpper)) {
            return SHA384.EVP_MD;
        }
        if ("SHA-224".equals(algorithmUpper)) {
            return SHA224.EVP_MD;
        }
        throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
    }

    static int getDigestSizeBytesByJcaDigestAlgorithmStandardName(String algorithm) throws NoSuchAlgorithmException {
        String algorithmUpper = algorithm.toUpperCase(Locale.US);
        if ("SHA-256".equals(algorithmUpper)) {
            return SHA256.SIZE_BYTES;
        }
        if ("SHA-512".equals(algorithmUpper)) {
            return SHA512.SIZE_BYTES;
        }
        if ("SHA-1".equals(algorithmUpper)) {
            return SHA1.SIZE_BYTES;
        }
        if ("SHA-384".equals(algorithmUpper)) {
            return SHA384.SIZE_BYTES;
        }
        if ("SHA-224".equals(algorithmUpper)) {
            return SHA224.SIZE_BYTES;
        }
        throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
    }

    static String getJcaDigestAlgorithmStandardNameFromEVP_MD(long evpMdRef) {
        if (evpMdRef == MD5.EVP_MD) {
            return "MD5";
        }
        if (evpMdRef == SHA1.EVP_MD) {
            return "SHA-1";
        }
        if (evpMdRef == SHA224.EVP_MD) {
            return "SHA-224";
        }
        if (evpMdRef == SHA256.EVP_MD) {
            return "SHA-256";
        }
        if (evpMdRef == SHA384.EVP_MD) {
            return "SHA-384";
        }
        if (evpMdRef == SHA512.EVP_MD) {
            return "SHA-512";
        }
        throw new IllegalArgumentException("Unknown EVP_MD reference");
    }

    private EvpMdRef() {
    }
}
