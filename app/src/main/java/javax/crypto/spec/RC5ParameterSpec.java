package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class RC5ParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv;
    private int rounds;
    private int version;
    private int wordSize;

    public RC5ParameterSpec(int version, int rounds, int wordSize) {
        this.iv = null;
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
    }

    public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv) {
        this(version, rounds, wordSize, iv, 0);
    }

    public RC5ParameterSpec(int version, int rounds, int wordSize, byte[] iv, int offset) {
        this.iv = null;
        this.version = version;
        this.rounds = rounds;
        this.wordSize = wordSize;
        if (iv == null) {
            throw new IllegalArgumentException("IV missing");
        }
        int blockSize = (wordSize / 8) * 2;
        if (iv.length - offset < blockSize) {
            throw new IllegalArgumentException("IV too short");
        }
        this.iv = new byte[blockSize];
        System.arraycopy(iv, offset, this.iv, 0, blockSize);
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
        return this.iv == null ? null : (byte[]) this.iv.clone();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC5ParameterSpec)) {
            return false;
        }
        RC5ParameterSpec other = (RC5ParameterSpec) obj;
        if (this.version == other.version && this.rounds == other.rounds && this.wordSize == other.wordSize) {
            z = Arrays.equals(this.iv, other.iv);
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
        return retval + ((this.version + this.rounds) + this.wordSize);
    }
}
