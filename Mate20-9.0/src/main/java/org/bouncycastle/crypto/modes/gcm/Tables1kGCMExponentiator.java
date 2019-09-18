package org.bouncycastle.crypto.modes.gcm;

import java.util.Vector;
import org.bouncycastle.util.Arrays;

public class Tables1kGCMExponentiator implements GCMExponentiator {
    private Vector lookupPowX2;

    private void ensureAvailable(int i) {
        int size = this.lookupPowX2.size();
        if (size <= i) {
            long[] jArr = (long[]) this.lookupPowX2.elementAt(size - 1);
            do {
                jArr = Arrays.clone(jArr);
                GCMUtil.square(jArr, jArr);
                this.lookupPowX2.addElement(jArr);
                size++;
            } while (size <= i);
        }
    }

    public void exponentiateX(long j, byte[] bArr) {
        long[] oneAsLongs = GCMUtil.oneAsLongs();
        int i = 0;
        while (j > 0) {
            if ((1 & j) != 0) {
                ensureAvailable(i);
                GCMUtil.multiply(oneAsLongs, (long[]) this.lookupPowX2.elementAt(i));
            }
            i++;
            j >>>= 1;
        }
        GCMUtil.asBytes(oneAsLongs, bArr);
    }

    public void init(byte[] bArr) {
        long[] asLongs = GCMUtil.asLongs(bArr);
        if (this.lookupPowX2 == null || !Arrays.areEqual(asLongs, (long[]) this.lookupPowX2.elementAt(0))) {
            this.lookupPowX2 = new Vector(8);
            this.lookupPowX2.addElement(asLongs);
        }
    }
}
