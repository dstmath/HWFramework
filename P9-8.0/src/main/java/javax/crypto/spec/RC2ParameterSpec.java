package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class RC2ParameterSpec implements AlgorithmParameterSpec {
    private int effectiveKeyBits;
    private byte[] iv;

    public RC2ParameterSpec(int effectiveKeyBits) {
        this.iv = null;
        this.effectiveKeyBits = effectiveKeyBits;
    }

    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv) {
        this(effectiveKeyBits, iv, 0);
    }

    public RC2ParameterSpec(int effectiveKeyBits, byte[] iv, int offset) {
        this.iv = null;
        this.effectiveKeyBits = effectiveKeyBits;
        if (iv == null) {
            throw new IllegalArgumentException("IV missing");
        } else if (iv.length - offset < 8) {
            throw new IllegalArgumentException("IV too short");
        } else {
            this.iv = new byte[8];
            System.arraycopy(iv, offset, this.iv, 0, 8);
        }
    }

    public int getEffectiveKeyBits() {
        return this.effectiveKeyBits;
    }

    public byte[] getIV() {
        return this.iv == null ? null : (byte[]) this.iv.clone();
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RC2ParameterSpec)) {
            return false;
        }
        RC2ParameterSpec other = (RC2ParameterSpec) obj;
        if (this.effectiveKeyBits == other.effectiveKeyBits) {
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
        return retval + this.effectiveKeyBits;
    }
}
