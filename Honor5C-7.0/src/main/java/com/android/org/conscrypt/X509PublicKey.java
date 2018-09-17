package com.android.org.conscrypt;

import java.security.PublicKey;
import java.util.Arrays;

public class X509PublicKey implements PublicKey {
    private static final long serialVersionUID = -8610156854731664298L;
    private final String algorithm;
    private final byte[] encoded;

    public X509PublicKey(String algorithm, byte[] encoded) {
        this.algorithm = algorithm;
        this.encoded = encoded;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getEncoded() {
        return this.encoded;
    }

    public String toString() {
        return "X509PublicKey [algorithm=" + this.algorithm + ", encoded=" + Arrays.toString(this.encoded) + "]";
    }

    public int hashCode() {
        return (((this.algorithm == null ? 0 : this.algorithm.hashCode()) + 31) * 31) + Arrays.hashCode(this.encoded);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        X509PublicKey other = (X509PublicKey) obj;
        if (this.algorithm == null) {
            if (other.algorithm != null) {
                return false;
            }
        } else if (!this.algorithm.equals(other.algorithm)) {
            return false;
        }
        return Arrays.equals(this.encoded, other.encoded);
    }
}
