package org.bouncycastle.pqc.jcajce.spec;

import java.security.spec.AlgorithmParameterSpec;
import org.bouncycastle.util.Arrays;

public class RainbowParameterSpec implements AlgorithmParameterSpec {
    private static final int[] DEFAULT_VI = {6, 12, 17, 22, 33};
    private int[] vi;

    public RainbowParameterSpec() {
        this.vi = DEFAULT_VI;
    }

    public RainbowParameterSpec(int[] iArr) {
        this.vi = iArr;
        checkParams();
    }

    private void checkParams() {
        int[] iArr;
        int i;
        int[] iArr2 = this.vi;
        if (iArr2 == null) {
            throw new IllegalArgumentException("no layers defined.");
        } else if (iArr2.length > 1) {
            int i2 = 0;
            do {
                iArr = this.vi;
                if (i2 < iArr.length - 1) {
                    i = iArr[i2];
                    i2++;
                } else {
                    return;
                }
            } while (i < iArr[i2]);
            throw new IllegalArgumentException("v[i] has to be smaller than v[i+1]");
        } else {
            throw new IllegalArgumentException("Rainbow needs at least 1 layer, such that v1 < v2.");
        }
    }

    public int getDocumentLength() {
        int[] iArr = this.vi;
        return iArr[iArr.length - 1] - iArr[0];
    }

    public int getNumOfLayers() {
        return this.vi.length - 1;
    }

    public int[] getVi() {
        return Arrays.clone(this.vi);
    }
}
