package org.bouncycastle.pqc.crypto.rainbow;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.pqc.crypto.rainbow.util.RainbowUtil;
import org.bouncycastle.util.Arrays;

public class Layer {
    private short[][][] coeff_alpha;
    private short[][][] coeff_beta;
    private short[] coeff_eta;
    private short[][] coeff_gamma;
    private int oi;
    private int vi;
    private int viNext;

    public Layer(byte b, byte b2, short[][][] sArr, short[][][] sArr2, short[][] sArr3, short[] sArr4) {
        this.vi = b & 255;
        this.viNext = b2 & 255;
        this.oi = this.viNext - this.vi;
        this.coeff_alpha = sArr;
        this.coeff_beta = sArr2;
        this.coeff_gamma = sArr3;
        this.coeff_eta = sArr4;
    }

    public Layer(int i, int i2, SecureRandom secureRandom) {
        this.vi = i;
        this.viNext = i2;
        this.oi = i2 - i;
        int i3 = this.oi;
        this.coeff_alpha = (short[][][]) Array.newInstance(short.class, i3, i3, this.vi);
        int i4 = this.oi;
        int i5 = this.vi;
        this.coeff_beta = (short[][][]) Array.newInstance(short.class, i4, i5, i5);
        this.coeff_gamma = (short[][]) Array.newInstance(short.class, this.oi, this.viNext);
        int i6 = this.oi;
        this.coeff_eta = new short[i6];
        for (int i7 = 0; i7 < i6; i7++) {
            for (int i8 = 0; i8 < this.oi; i8++) {
                for (int i9 = 0; i9 < this.vi; i9++) {
                    this.coeff_alpha[i7][i8][i9] = (short) (secureRandom.nextInt() & GF2Field.MASK);
                }
            }
        }
        for (int i10 = 0; i10 < i6; i10++) {
            for (int i11 = 0; i11 < this.vi; i11++) {
                for (int i12 = 0; i12 < this.vi; i12++) {
                    this.coeff_beta[i10][i11][i12] = (short) (secureRandom.nextInt() & GF2Field.MASK);
                }
            }
        }
        for (int i13 = 0; i13 < i6; i13++) {
            for (int i14 = 0; i14 < this.viNext; i14++) {
                this.coeff_gamma[i13][i14] = (short) (secureRandom.nextInt() & GF2Field.MASK);
            }
        }
        for (int i15 = 0; i15 < i6; i15++) {
            this.coeff_eta[i15] = (short) (secureRandom.nextInt() & GF2Field.MASK);
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Layer)) {
            return false;
        }
        Layer layer = (Layer) obj;
        return this.vi == layer.getVi() && this.viNext == layer.getViNext() && this.oi == layer.getOi() && RainbowUtil.equals(this.coeff_alpha, layer.getCoeffAlpha()) && RainbowUtil.equals(this.coeff_beta, layer.getCoeffBeta()) && RainbowUtil.equals(this.coeff_gamma, layer.getCoeffGamma()) && RainbowUtil.equals(this.coeff_eta, layer.getCoeffEta());
    }

    public short[][][] getCoeffAlpha() {
        return this.coeff_alpha;
    }

    public short[][][] getCoeffBeta() {
        return this.coeff_beta;
    }

    public short[] getCoeffEta() {
        return this.coeff_eta;
    }

    public short[][] getCoeffGamma() {
        return this.coeff_gamma;
    }

    public int getOi() {
        return this.oi;
    }

    public int getVi() {
        return this.vi;
    }

    public int getViNext() {
        return this.viNext;
    }

    public int hashCode() {
        return (((((((((((this.vi * 37) + this.viNext) * 37) + this.oi) * 37) + Arrays.hashCode(this.coeff_alpha)) * 37) + Arrays.hashCode(this.coeff_beta)) * 37) + Arrays.hashCode(this.coeff_gamma)) * 37) + Arrays.hashCode(this.coeff_eta);
    }

    public short[][] plugInVinegars(short[] sArr) {
        int i = this.oi;
        short[][] sArr2 = (short[][]) Array.newInstance(short.class, i, i + 1);
        short[] sArr3 = new short[this.oi];
        int i2 = 0;
        for (int i3 = 0; i3 < this.oi; i3++) {
            for (int i4 = 0; i4 < this.vi; i4++) {
                for (int i5 = 0; i5 < this.vi; i5++) {
                    sArr3[i3] = GF2Field.addElem(sArr3[i3], GF2Field.multElem(GF2Field.multElem(this.coeff_beta[i3][i4][i5], sArr[i4]), sArr[i5]));
                }
            }
        }
        for (int i6 = 0; i6 < this.oi; i6++) {
            for (int i7 = 0; i7 < this.oi; i7++) {
                for (int i8 = 0; i8 < this.vi; i8++) {
                    sArr2[i6][i7] = GF2Field.addElem(sArr2[i6][i7], GF2Field.multElem(this.coeff_alpha[i6][i7][i8], sArr[i8]));
                }
            }
        }
        for (int i9 = 0; i9 < this.oi; i9++) {
            for (int i10 = 0; i10 < this.vi; i10++) {
                sArr3[i9] = GF2Field.addElem(sArr3[i9], GF2Field.multElem(this.coeff_gamma[i9][i10], sArr[i10]));
            }
        }
        for (int i11 = 0; i11 < this.oi; i11++) {
            for (int i12 = this.vi; i12 < this.viNext; i12++) {
                short[] sArr4 = sArr2[i11];
                int i13 = this.vi;
                sArr4[i12 - i13] = GF2Field.addElem(this.coeff_gamma[i11][i12], sArr2[i11][i12 - i13]);
            }
        }
        for (int i14 = 0; i14 < this.oi; i14++) {
            sArr3[i14] = GF2Field.addElem(sArr3[i14], this.coeff_eta[i14]);
        }
        while (true) {
            int i15 = this.oi;
            if (i2 >= i15) {
                return sArr2;
            }
            sArr2[i2][i15] = sArr3[i2];
            i2++;
        }
    }
}
