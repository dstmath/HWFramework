package com.android.org.bouncycastle.crypto.modes.gcm;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Pack;
import java.lang.reflect.Array;

public class Tables8kGCMMultiplier implements GCMMultiplier {
    private byte[] H;
    private int[][][] M;

    public void init(byte[] H) {
        int j;
        if (this.M == null) {
            this.M = (int[][][]) Array.newInstance(Integer.TYPE, new int[]{32, 16, 4});
        } else if (Arrays.areEqual(this.H, H)) {
            return;
        }
        this.H = Arrays.clone(H);
        GCMUtil.asInts(H, this.M[1][8]);
        for (j = 4; j >= 1; j >>= 1) {
            GCMUtil.multiplyP(this.M[1][j + j], this.M[1][j]);
        }
        GCMUtil.multiplyP(this.M[1][1], this.M[0][8]);
        for (j = 4; j >= 1; j >>= 1) {
            GCMUtil.multiplyP(this.M[0][j + j], this.M[0][j]);
        }
        int i = 0;
        while (true) {
            for (j = 2; j < 16; j += j) {
                for (int k = 1; k < j; k++) {
                    GCMUtil.xor(this.M[i][j], this.M[i][k], this.M[i][j + k]);
                }
            }
            i++;
            if (i != 32) {
                if (i > 1) {
                    for (j = 8; j > 0; j >>= 1) {
                        GCMUtil.multiplyP8(this.M[i - 2][j], this.M[i][j]);
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
            m = this.M[(i + i) + 1][(x[i] & 240) >>> 4];
            z[0] = z[0] ^ m[0];
            z[1] = z[1] ^ m[1];
            z[2] = z[2] ^ m[2];
            z[3] = z[3] ^ m[3];
        }
        Pack.intToBigEndian(z, x, 0);
    }
}
