package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;

public class IvParameterSpec implements AlgorithmParameterSpec {
    private byte[] iv;

    public IvParameterSpec(byte[] iv) {
        this(iv, 0, iv.length);
    }

    public IvParameterSpec(byte[] iv, int offset, int len) {
        if (iv == null) {
            throw new IllegalArgumentException("IV missing");
        } else if (iv.length - offset < len) {
            throw new IllegalArgumentException("IV buffer too short for given offset/length combination");
        } else if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("len is negative");
        } else {
            this.iv = new byte[len];
            System.arraycopy(iv, offset, this.iv, 0, len);
        }
    }

    public byte[] getIV() {
        return (byte[]) this.iv.clone();
    }
}
