package com.android.org.conscrypt.ct;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DigitallySigned {
    private final HashAlgorithm hashAlgorithm;
    private final byte[] signature;
    private final SignatureAlgorithm signatureAlgorithm;

    public enum HashAlgorithm {
        NONE,
        MD5,
        SHA1,
        SHA224,
        SHA256,
        SHA384,
        SHA512;
        
        private static HashAlgorithm[] values;

        static {
            values = values();
        }

        public static HashAlgorithm valueOf(int ord) {
            try {
                return values[ord];
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid hash algorithm " + ord, e);
            }
        }
    }

    public enum SignatureAlgorithm {
        ANONYMOUS,
        RSA,
        DSA,
        ECDSA;
        
        private static SignatureAlgorithm[] values;

        static {
            values = values();
        }

        public static SignatureAlgorithm valueOf(int ord) {
            try {
                return values[ord];
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Invalid signature algorithm " + ord, e);
            }
        }
    }

    public DigitallySigned(HashAlgorithm hashAlgorithm2, SignatureAlgorithm signatureAlgorithm2, byte[] signature2) {
        this.hashAlgorithm = hashAlgorithm2;
        this.signatureAlgorithm = signatureAlgorithm2;
        this.signature = signature2;
    }

    public DigitallySigned(int hashAlgorithm2, int signatureAlgorithm2, byte[] signature2) {
        this(HashAlgorithm.valueOf(hashAlgorithm2), SignatureAlgorithm.valueOf(signatureAlgorithm2), signature2);
    }

    public HashAlgorithm getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public String getAlgorithm() {
        return String.format("%swith%s", new Object[]{this.hashAlgorithm, this.signatureAlgorithm});
    }

    public static DigitallySigned decode(InputStream input) throws SerializationException {
        try {
            return new DigitallySigned(Serialization.readNumber(input, 1), Serialization.readNumber(input, 1), Serialization.readVariableBytes(input, 2));
        } catch (IllegalArgumentException e) {
            throw new SerializationException((Throwable) e);
        }
    }

    public static DigitallySigned decode(byte[] input) throws SerializationException {
        return decode((InputStream) new ByteArrayInputStream(input));
    }
}
