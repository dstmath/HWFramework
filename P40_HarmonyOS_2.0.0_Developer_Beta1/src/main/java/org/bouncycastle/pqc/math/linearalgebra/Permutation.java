package org.bouncycastle.pqc.math.linearalgebra;

import java.security.SecureRandom;
import org.bouncycastle.util.Arrays;

public class Permutation {
    private int[] perm;

    public Permutation(int i) {
        if (i > 0) {
            this.perm = new int[i];
            for (int i2 = i - 1; i2 >= 0; i2--) {
                this.perm[i2] = i2;
            }
            return;
        }
        throw new IllegalArgumentException("invalid length");
    }

    public Permutation(int i, SecureRandom secureRandom) {
        if (i > 0) {
            this.perm = new int[i];
            int[] iArr = new int[i];
            for (int i2 = 0; i2 < i; i2++) {
                iArr[i2] = i2;
            }
            int i3 = i;
            for (int i4 = 0; i4 < i; i4++) {
                int nextInt = RandUtils.nextInt(secureRandom, i3);
                i3--;
                this.perm[i4] = iArr[nextInt];
                iArr[nextInt] = iArr[i3];
            }
            return;
        }
        throw new IllegalArgumentException("invalid length");
    }

    public Permutation(byte[] bArr) {
        if (bArr.length > 4) {
            int OS2IP = LittleEndianConversions.OS2IP(bArr, 0);
            int ceilLog256 = IntegerFunctions.ceilLog256(OS2IP - 1);
            if (bArr.length == (OS2IP * ceilLog256) + 4) {
                this.perm = new int[OS2IP];
                for (int i = 0; i < OS2IP; i++) {
                    this.perm[i] = LittleEndianConversions.OS2IP(bArr, (i * ceilLog256) + 4, ceilLog256);
                }
                if (!isPermutation(this.perm)) {
                    throw new IllegalArgumentException("invalid encoding");
                }
                return;
            }
            throw new IllegalArgumentException("invalid encoding");
        }
        throw new IllegalArgumentException("invalid encoding");
    }

    public Permutation(int[] iArr) {
        if (isPermutation(iArr)) {
            this.perm = IntUtils.clone(iArr);
            return;
        }
        throw new IllegalArgumentException("array is not a permutation vector");
    }

    private boolean isPermutation(int[] iArr) {
        int length = iArr.length;
        boolean[] zArr = new boolean[length];
        for (int i = 0; i < length; i++) {
            if (iArr[i] < 0 || iArr[i] >= length || zArr[iArr[i]]) {
                return false;
            }
            zArr[iArr[i]] = true;
        }
        return true;
    }

    public Permutation computeInverse() {
        Permutation permutation = new Permutation(this.perm.length);
        for (int length = this.perm.length - 1; length >= 0; length--) {
            permutation.perm[this.perm[length]] = length;
        }
        return permutation;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Permutation)) {
            return false;
        }
        return IntUtils.equals(this.perm, ((Permutation) obj).perm);
    }

    public byte[] getEncoded() {
        int length = this.perm.length;
        int ceilLog256 = IntegerFunctions.ceilLog256(length - 1);
        byte[] bArr = new byte[((length * ceilLog256) + 4)];
        LittleEndianConversions.I2OSP(length, bArr, 0);
        for (int i = 0; i < length; i++) {
            LittleEndianConversions.I2OSP(this.perm[i], bArr, (i * ceilLog256) + 4, ceilLog256);
        }
        return bArr;
    }

    public int[] getVector() {
        return IntUtils.clone(this.perm);
    }

    public int hashCode() {
        return Arrays.hashCode(this.perm);
    }

    public Permutation rightMultiply(Permutation permutation) {
        int length = permutation.perm.length;
        int[] iArr = this.perm;
        if (length == iArr.length) {
            Permutation permutation2 = new Permutation(iArr.length);
            for (int length2 = this.perm.length - 1; length2 >= 0; length2--) {
                permutation2.perm[length2] = this.perm[permutation.perm[length2]];
            }
            return permutation2;
        }
        throw new IllegalArgumentException("length mismatch");
    }

    public String toString() {
        String str = "[" + this.perm[0];
        for (int i = 1; i < this.perm.length; i++) {
            str = str + ", " + this.perm[i];
        }
        return str + "]";
    }
}
