package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class RC5ParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv;
    private int rounds;
    private int version;
    private int wordSize;

    public RC5ParameterSpec(int version2, int rounds2, int wordSize2) {
        this.iv = null;
        this.version = version2;
        this.rounds = rounds2;
        this.wordSize = wordSize2;
    }

    public RC5ParameterSpec(int version2, int rounds2, int wordSize2, byte[] iv2) {
        this(version2, rounds2, wordSize2, iv2, 0);
    }

    public RC5ParameterSpec(int version2, int rounds2, int wordSize2, byte[] iv2, int offset) {
        this.iv = null;
        this.version = version2;
        this.rounds = rounds2;
        this.wordSize = wordSize2;
        if (iv2 != null) {
            int blockSize = (wordSize2 / 8) * 2;
            if (iv2.length - offset >= blockSize) {
                this.iv = new byte[blockSize];
                System.arraycopy(iv2, offset, this.iv, 0, blockSize);
                return;
            }
            throw new IllegalArgumentException("IV too short");
        }
        throw new IllegalArgumentException("IV missing");
    }

    public int getVersion() {
        return this.version;
    }

    public int getRounds() {
        return this.rounds;
    }

    public int getWordSize() {
        return this.wordSize;
    }

    public byte[] getIV() {
        if (this.iv == null) {
            return null;
        }
        return (byte[]) this.iv.clone();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC5ParameterSpec)) {
            return false;
        }
        RC5ParameterSpec other = (RC5ParameterSpec) obj;
        if (!(this.version == other.version && this.rounds == other.rounds && this.wordSize == other.wordSize && Arrays.equals(this.iv, other.iv))) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int retval = 0;
        if (this.iv != null) {
            for (int i = 1; i < this.iv.length; i++) {
                retval += this.iv[i] * i;
            }
        }
        return retval + this.version + this.rounds + this.wordSize;
    }
}
