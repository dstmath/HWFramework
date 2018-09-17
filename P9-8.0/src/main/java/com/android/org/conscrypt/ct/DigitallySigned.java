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

    public DigitallySigned(HashAlgorithm hashAlgorithm, SignatureAlgorithm signatureAlgorithm, byte[] signature) {
        this.hashAlgorithm = hashAlgorithm;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signature = signature;
    }

    public DigitallySigned(int hashAlgorithm, int signatureAlgorithm, byte[] signature) {
        this(HashAlgorithm.valueOf(hashAlgorithm), SignatureAlgorithm.valueOf(signatureAlgorithm), signature);
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
        } catch (Throwable e) {
            throw new SerializationException(e);
        }
    }

    public static DigitallySigned decode(byte[] input) throws SerializationException {
        return decode(new ByteArrayInputStream(input));
    }
}
