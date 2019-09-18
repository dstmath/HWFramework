package com.android.org.bouncycastle.crypto.modes.gcm;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Pack;
import java.lang.reflect.Array;

public class Tables8kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private int[][][] M;

    public void init(byte[] H2) {
        int j = 4;
        if (this.M == null) {
            this.M = (int[][][]) Array.newInstance(int.class, new int[]{32, 16, 4});
        } else if (Arrays.areEqual(this.H, H2)) {
            return;
        }
        this.H = Arrays.clone(H2);
        GCMUtil.asInts(H2, this.M[1][8]);
        for (int j2 = 4; j2 >= 1; j2 >>= 1) {
            GCMUtil.multiplyP(this.M[1][j2 + j2], this.M[1][j2]);
        }
        int i = 0;
        GCMUtil.multiplyP(this.M[1][1], this.M[0][8]);
        while (true) {
            int j3 = j;
            if (j3 < 1) {
                break;
            }
            GCMUtil.multiplyP(this.M[0][j3 + j3], this.M[0][j3]);
            j = j3 >> 1;
        }
        while (true) {
            int i2 = i;
            for (int j4 = 2; j4 < 16; j4 += j4) {
                for (int k = 1; k < j4; k++) {
                    GCMUtil.xor(this.M[i2][j4], this.M[i2][k], this.M[i2][j4 + k]);
                }
            }
            i = i2 + 1;
            if (i != 32) {
                if (i > 1) {
                    for (int j5 = 8; j5 > 0; j5 >>= 1) {
                        GCMUtil.multiplyP8(this.M[i - 2][j5], this.M[i][j5]);
                    }
                }
            } else {
                return;
            }
        }
    }

    public void multiplyH(byte[] x) {
        int[] z = new int[4];
        for (int i = 15; i >= 0; i--) {
            int[] m = this.M[i + i][x[i] & 15];
            z[0] = z[0] ^ m[0];
            z[1] = z[1] ^ m[1];
            z[2] = z[2] ^ m[2];
            z[3] = z[3] ^ m[3];
            int[] m2 = this.M[i + i + 1][(x[i] & 240) >>> 4];
            z[0] = z[0] ^ m2[0];
            z[1] = z[1] ^ m2[1];
            z[2] = z[2] ^ m2[2];
            z[3] = z[3] ^ m2[3];
        }
        Pack.intToBigEndian(z, x, 0);
    }
}
