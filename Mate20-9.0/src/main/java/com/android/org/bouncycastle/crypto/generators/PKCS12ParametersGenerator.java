package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.Digest;
import com.android.org.bouncycastle.crypto.ExtendedDigest;
import com.android.org.bouncycastle.crypto.PBEParametersGenerator;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;

public class PKCS12ParametersGenerator extends PBEParametersGenerator {
    public static final int IV_MATERIAL = 2;
    public static final int KEY_MATERIAL = 1;
    public static final int MAC_MATERIAL = 3;
    private Digest digest;
    private int u;
    private int v;

    public PKCS12ParametersGenerator(Digest digest2) {
        this.digest = digest2;
        if (digest2 instanceof ExtendedDigest) {
            this.u = digest2.getDigestSize();
            this.v = ((ExtendedDigest) digest2).getByteLength();
            return;
        }
        throw new IllegalArgumentException("Digest " + digest2.getAlgorithmName() + " unsupported");
    }

    private void adjust(byte[] a, int aOff, byte[] b) {
        int x = (b[b.length - 1] & 255) + (a[(b.length + aOff) - 1] & 255) + 1;
        a[(b.length + aOff) - 1] = (byte) x;
        int x2 = x >>> 8;
        for (int i = b.length - 2; i >= 0; i--) {
            int x3 = x2 + (b[i] & 255) + (a[aOff + i] & 255);
            a[aOff + i] = (byte) x3;
            x2 = x3 >>> 8;
        }
    }

    private byte[] generateDerivedKey(int idByte, int n) {
        byte[] S;
        byte[] P;
        int i = n;
        byte[] D = new byte[this.v];
        byte[] dKey = new byte[i];
        int i2 = 0;
        for (int i3 = 0; i3 != D.length; i3++) {
            D[i3] = (byte) idByte;
        }
        int i4 = idByte;
        int i5 = 1;
        if (this.salt == null || this.salt.length == 0) {
            S = new byte[0];
        } else {
            S = new byte[(this.v * (((this.salt.length + this.v) - 1) / this.v))];
            for (int i6 = 0; i6 != S.length; i6++) {
                S[i6] = this.salt[i6 % this.salt.length];
            }
        }
        if (this.password == null || this.password.length == 0) {
            P = new byte[0];
        } else {
            P = new byte[(this.v * (((this.password.length + this.v) - 1) / this.v))];
            for (int i7 = 0; i7 != P.length; i7++) {
                P[i7] = this.password[i7 % this.password.length];
            }
        }
        byte[] I = new byte[(S.length + P.length)];
        System.arraycopy(S, 0, I, 0, S.length);
        System.arraycopy(P, 0, I, S.length, P.length);
        byte[] B = new byte[this.v];
        int c = ((this.u + i) - 1) / this.u;
        byte[] A = new byte[this.u];
        int i8 = 1;
        while (i8 <= c) {
            this.digest.update(D, i2, D.length);
            this.digest.update(I, i2, I.length);
            this.digest.doFinal(A, i2);
            for (int j = i5; j < this.iterationCount; j++) {
                this.digest.update(A, i2, A.length);
                this.digest.doFinal(A, i2);
            }
            for (int j2 = i2; j2 != B.length; j2++) {
                B[j2] = A[j2 % A.length];
            }
            for (int j3 = i2; j3 != I.length / this.v; j3++) {
                adjust(I, this.v * j3, B);
            }
            if (i8 == c) {
                i2 = 0;
                System.arraycopy(A, 0, dKey, (i8 - 1) * this.u, dKey.length - ((i8 - 1) * this.u));
            } else {
                System.arraycopy(A, i2, dKey, (i8 - 1) * this.u, A.length);
            }
            i8++;
            i5 = 1;
        }
        return dKey;
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        int keySize2 = keySize / 8;
        return new KeyParameter(generateDerivedKey(1, keySize2), 0, keySize2);
    }

    public CipherParameters generateDerivedParameters(int keySize, int ivSize) {
        int keySize2 = keySize / 8;
        int ivSize2 = ivSize / 8;
        byte[] dKey = generateDerivedKey(1, keySize2);
        return new ParametersWithIV(new KeyParameter(dKey, 0, keySize2), generateDerivedKey(2, ivSize2), 0, ivSize2);
    }

    public CipherParameters generateDerivedMacParameters(int keySize) {
        int keySize2 = keySize / 8;
        return new KeyParameter(generateDerivedKey(3, keySize2), 0, keySize2);
    }
}
