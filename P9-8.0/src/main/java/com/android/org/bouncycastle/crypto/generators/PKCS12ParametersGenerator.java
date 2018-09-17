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

    public PKCS12ParametersGenerator(Digest digest) {
        this.digest = digest;
        if (digest instanceof ExtendedDigest) {
            this.u = digest.getDigestSize();
            this.v = ((ExtendedDigest) digest).getByteLength();
            return;
        }
        throw new IllegalArgumentException("Digest " + digest.getAlgorithmName() + " unsupported");
    }

    private void adjust(byte[] a, int aOff, byte[] b) {
        int x = ((b[b.length - 1] & 255) + (a[(b.length + aOff) - 1] & 255)) + 1;
        a[(b.length + aOff) - 1] = (byte) x;
        x >>>= 8;
        for (int i = b.length - 2; i >= 0; i--) {
            x += (b[i] & 255) + (a[aOff + i] & 255);
            a[aOff + i] = (byte) x;
            x >>>= 8;
        }
    }

    private byte[] generateDerivedKey(int idByte, int n) {
        int i;
        byte[] S;
        byte[] P;
        byte[] D = new byte[this.v];
        byte[] dKey = new byte[n];
        for (i = 0; i != D.length; i++) {
            D[i] = (byte) idByte;
        }
        if (this.salt == null || this.salt.length == 0) {
            S = new byte[0];
        } else {
            S = new byte[(this.v * (((this.salt.length + this.v) - 1) / this.v))];
            for (i = 0; i != S.length; i++) {
                S[i] = this.salt[i % this.salt.length];
            }
        }
        if (this.password == null || this.password.length == 0) {
            P = new byte[0];
        } else {
            P = new byte[(this.v * (((this.password.length + this.v) - 1) / this.v))];
            for (i = 0; i != P.length; i++) {
                P[i] = this.password[i % this.password.length];
            }
        }
        byte[] I = new byte[(S.length + P.length)];
        System.arraycopy(S, 0, I, 0, S.length);
        System.arraycopy(P, 0, I, S.length, P.length);
        byte[] B = new byte[this.v];
        int c = ((this.u + n) - 1) / this.u;
        byte[] A = new byte[this.u];
        for (i = 1; i <= c; i++) {
            int j;
            this.digest.update(D, 0, D.length);
            this.digest.update(I, 0, I.length);
            this.digest.doFinal(A, 0);
            for (j = 1; j < this.iterationCount; j++) {
                this.digest.update(A, 0, A.length);
                this.digest.doFinal(A, 0);
            }
            for (j = 0; j != B.length; j++) {
                B[j] = A[j % A.length];
            }
            for (j = 0; j != I.length / this.v; j++) {
                adjust(I, this.v * j, B);
            }
            if (i == c) {
                System.arraycopy(A, 0, dKey, (i - 1) * this.u, dKey.length - ((i - 1) * this.u));
            } else {
                System.arraycopy(A, 0, dKey, (i - 1) * this.u, A.length);
            }
        }
        return dKey;
    }

    public CipherParameters generateDerivedParameters(int keySize) {
        keySize /= 8;
        return new KeyParameter(generateDerivedKey(1, keySize), 0, keySize);
    }

    public CipherParameters generateDerivedParameters(int keySize, int ivSize) {
        keySize /= 8;
        ivSize /= 8;
        byte[] dKey = generateDerivedKey(1, keySize);
        return new ParametersWithIV(new KeyParameter(dKey, 0, keySize), generateDerivedKey(2, ivSize), 0, ivSize);
    }

    public CipherParameters generateDerivedMacParameters(int keySize) {
        keySize /= 8;
        return new KeyParameter(generateDerivedKey(3, keySize), 0, keySize);
    }
}
