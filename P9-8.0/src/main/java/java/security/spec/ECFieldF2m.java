package java.security.spec;

import java.math.BigInteger;
import java.util.Arrays;

public class ECFieldF2m implements ECField {
    private int[] ks;
    private int m;
    private BigInteger rp;

    public ECFieldF2m(int m) {
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        this.m = m;
        this.ks = null;
        this.rp = null;
    }

    public ECFieldF2m(int m, BigInteger rp) {
        this.m = m;
        this.rp = rp;
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        }
        int bitCount = this.rp.bitCount();
        if (this.rp.testBit(0) && (this.rp.testBit(m) ^ 1) == 0 && (bitCount == 3 || bitCount == 5)) {
            BigInteger temp = this.rp.clearBit(0).clearBit(m);
            this.ks = new int[(bitCount - 2)];
            for (int i = this.ks.length - 1; i >= 0; i--) {
                int index = temp.getLowestSetBit();
                this.ks[i] = index;
                temp = temp.clearBit(index);
            }
            return;
        }
        throw new IllegalArgumentException("rp does not represent a valid reduction polynomial");
    }

    public ECFieldF2m(int m, int[] ks) {
        this.m = m;
        this.ks = (int[]) ks.clone();
        if (m <= 0) {
            throw new IllegalArgumentException("m is not positive");
        } else if (this.ks.length == 1 || this.ks.length == 3) {
            int i = 0;
            while (i < this.ks.length) {
                if (this.ks[i] < 1 || this.ks[i] > m - 1) {
                    throw new IllegalArgumentException("ks[" + i + "] is out of range");
                } else if (i == 0 || this.ks[i] < this.ks[i - 1]) {
                    i++;
                } else {
                    throw new IllegalArgumentException("values in ks are not in descending order");
                }
            }
            this.rp = BigInteger.ONE;
            this.rp = this.rp.setBit(m);
            for (int bit : this.ks) {
                this.rp = this.rp.setBit(bit);
            }
        } else {
            throw new IllegalArgumentException("length of ks is neither 1 nor 3");
        }
    }

    public int getFieldSize() {
        return this.m;
    }

    public int getM() {
        return this.m;
    }

    public BigInteger getReductionPolynomial() {
        return this.rp;
    }

    public int[] getMidTermsOfReductionPolynomial() {
        if (this.ks == null) {
            return null;
        }
        return (int[]) this.ks.clone();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ECFieldF2m)) {
            return false;
        }
        boolean equals;
        if (this.m == ((ECFieldF2m) obj).m) {
            equals = Arrays.equals(this.ks, ((ECFieldF2m) obj).ks);
        } else {
            equals = false;
        }
        return equals;
    }

    public int hashCode() {
        return (this.m << 5) + (this.rp == null ? 0 : this.rp.hashCode());
    }
}
