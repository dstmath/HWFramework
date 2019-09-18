package com.android.org.conscrypt;

import java.security.PublicKey;
import java.util.Arrays;

final class X509PublicKey implements PublicKey {
    private static final long serialVersionUID = -8610156854731664298L;
    private final String algorithm;
    private final byte[] encoded;

    X509PublicKey(String algorithm2, byte[] encoded2) {
        this.algorithm = algorithm2;
        this.encoded = encoded2;
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
        return (31 * ((31 * 1) + (this.algorithm == null ? 0 : this.algorithm.hashCode()))) + Arrays.hashCode(this.encoded);
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
        if (!Arrays.equals(this.encoded, other.encoded)) {
            return false;
        }
        return true;
    }
}
