package org.bouncycastle.pqc.crypto.rainbow;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.rainbow.util.ComputeInField;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;

public class RainbowKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private short[][] A1;
    private short[][] A1inv;
    private short[][] A2;
    private short[][] A2inv;
    private short[] b1;
    private short[] b2;
    private boolean initialized = false;
    private Layer[] layers;
    private int numOfLayers;
    private short[][] pub_quadratic;
    private short[] pub_scalar;
    private short[][] pub_singular;
    private RainbowKeyGenerationParameters rainbowParams;
    private SecureRandom sr;
    private int[] vi;

    private void compactPublicKey(short[][][] sArr) {
        int length = sArr.length;
        int length2 = sArr[0].length;
        this.pub_quadratic = (short[][]) Array.newInstance(short.class, new int[]{length, ((length2 + 1) * length2) / 2});
        for (int i = 0; i < length; i++) {
            int i2 = 0;
            int i3 = 0;
            while (i2 < length2) {
                int i4 = i3;
                for (int i5 = i2; i5 < length2; i5++) {
                    if (i5 == i2) {
                        this.pub_quadratic[i][i4] = sArr[i][i2][i5];
                    } else {
                        this.pub_quadratic[i][i4] = GF2Field.addElem(sArr[i][i2][i5], sArr[i][i5][i2]);
                    }
                    i4++;
                }
                i2++;
                i3 = i4;
            }
        }
    }

    private void computePublicKey() {
        ComputeInField computeInField = new ComputeInField();
        int i = 0;
        int i2 = this.vi[this.vi.length - 1] - this.vi[0];
        int i3 = this.vi[this.vi.length - 1];
        short[][][] sArr = (short[][][]) Array.newInstance(short.class, new int[]{i2, i3, i3});
        this.pub_singular = (short[][]) Array.newInstance(short.class, new int[]{i2, i3});
        this.pub_scalar = new short[i2];
        short[] sArr2 = new short[i3];
        int i4 = 0;
        int i5 = 0;
        while (i4 < this.layers.length) {
            short[][][] coeffAlpha = this.layers[i4].getCoeffAlpha();
            short[][][] coeffBeta = this.layers[i4].getCoeffBeta();
            short[][] coeffGamma = this.layers[i4].getCoeffGamma();
            short[] coeffEta = this.layers[i4].getCoeffEta();
            int length = coeffAlpha[i].length;
            int length2 = coeffBeta[i].length;
            int i6 = i;
            while (i6 < length) {
                int i7 = i;
                while (i7 < length) {
                    while (i < length2) {
                        int i8 = i2;
                        int i9 = i3;
                        int i10 = i7 + length2;
                        short[] multVect = computeInField.multVect(coeffAlpha[i6][i7][i], this.A2[i10]);
                        int i11 = i5 + i6;
                        sArr[i11] = computeInField.addSquareMatrix(sArr[i11], computeInField.multVects(multVect, this.A2[i]));
                        this.pub_singular[i11] = computeInField.addVect(computeInField.multVect(this.b2[i], multVect), this.pub_singular[i11]);
                        this.pub_singular[i11] = computeInField.addVect(computeInField.multVect(this.b2[i10], computeInField.multVect(coeffAlpha[i6][i7][i], this.A2[i])), this.pub_singular[i11]);
                        this.pub_scalar[i11] = GF2Field.addElem(this.pub_scalar[i11], GF2Field.multElem(GF2Field.multElem(coeffAlpha[i6][i7][i], this.b2[i10]), this.b2[i]));
                        i++;
                        i2 = i8;
                        i3 = i9;
                        i4 = i4;
                        coeffEta = coeffEta;
                        coeffAlpha = coeffAlpha;
                    }
                    int i12 = i2;
                    int i13 = i3;
                    int i14 = i4;
                    short[][][] sArr3 = coeffAlpha;
                    short[] sArr4 = coeffEta;
                    i7++;
                    i = 0;
                }
                int i15 = i2;
                int i16 = i3;
                int i17 = i4;
                short[][][] sArr5 = coeffAlpha;
                short[] sArr6 = coeffEta;
                for (int i18 = 0; i18 < length2; i18++) {
                    for (int i19 = 0; i19 < length2; i19++) {
                        short[] multVect2 = computeInField.multVect(coeffBeta[i6][i18][i19], this.A2[i18]);
                        int i20 = i5 + i6;
                        sArr[i20] = computeInField.addSquareMatrix(sArr[i20], computeInField.multVects(multVect2, this.A2[i19]));
                        this.pub_singular[i20] = computeInField.addVect(computeInField.multVect(this.b2[i19], multVect2), this.pub_singular[i20]);
                        this.pub_singular[i20] = computeInField.addVect(computeInField.multVect(this.b2[i18], computeInField.multVect(coeffBeta[i6][i18][i19], this.A2[i19])), this.pub_singular[i20]);
                        this.pub_scalar[i20] = GF2Field.addElem(this.pub_scalar[i20], GF2Field.multElem(GF2Field.multElem(coeffBeta[i6][i18][i19], this.b2[i18]), this.b2[i19]));
                    }
                }
                for (int i21 = 0; i21 < length2 + length; i21++) {
                    int i22 = i5 + i6;
                    this.pub_singular[i22] = computeInField.addVect(computeInField.multVect(coeffGamma[i6][i21], this.A2[i21]), this.pub_singular[i22]);
                    this.pub_scalar[i22] = GF2Field.addElem(this.pub_scalar[i22], GF2Field.multElem(coeffGamma[i6][i21], this.b2[i21]));
                }
                int i23 = i5 + i6;
                this.pub_scalar[i23] = GF2Field.addElem(this.pub_scalar[i23], sArr6[i6]);
                i6++;
                i2 = i15;
                i3 = i16;
                i4 = i17;
                coeffEta = sArr6;
                coeffAlpha = sArr5;
                i = 0;
            }
            int i24 = i2;
            int i25 = i3;
            i5 += length;
            i4++;
            i = 0;
        }
        short[][][] sArr7 = (short[][][]) Array.newInstance(short.class, new int[]{i2, i3, i3});
        short[][] sArr8 = (short[][]) Array.newInstance(short.class, new int[]{i2, i3});
        short[] sArr9 = new short[i2];
        for (int i26 = 0; i26 < i2; i26++) {
            for (int i27 = 0; i27 < this.A1.length; i27++) {
                sArr7[i26] = computeInField.addSquareMatrix(sArr7[i26], computeInField.multMatrix(this.A1[i26][i27], sArr[i27]));
                sArr8[i26] = computeInField.addVect(sArr8[i26], computeInField.multVect(this.A1[i26][i27], this.pub_singular[i27]));
                sArr9[i26] = GF2Field.addElem(sArr9[i26], GF2Field.multElem(this.A1[i26][i27], this.pub_scalar[i27]));
            }
            sArr9[i26] = GF2Field.addElem(sArr9[i26], this.b1[i26]);
        }
        this.pub_singular = sArr8;
        this.pub_scalar = sArr9;
        compactPublicKey(sArr7);
    }

    private void generateF() {
        this.layers = new Layer[this.numOfLayers];
        int i = 0;
        while (i < this.numOfLayers) {
            int i2 = i + 1;
            this.layers[i] = new Layer(this.vi[i], this.vi[i2], this.sr);
            i = i2;
        }
    }

    private void generateL1() {
        int i = this.vi[this.vi.length - 1] - this.vi[0];
        this.A1 = (short[][]) Array.newInstance(short.class, new int[]{i, i});
        this.A1inv = null;
        ComputeInField computeInField = new ComputeInField();
        while (this.A1inv == null) {
            for (int i2 = 0; i2 < i; i2++) {
                for (int i3 = 0; i3 < i; i3++) {
                    this.A1[i2][i3] = (short) (this.sr.nextInt() & 255);
                }
            }
            this.A1inv = computeInField.inverse(this.A1);
        }
        this.b1 = new short[i];
        for (int i4 = 0; i4 < i; i4++) {
            this.b1[i4] = (short) (this.sr.nextInt() & 255);
        }
    }

    private void generateL2() {
        int i;
        int i2 = this.vi[this.vi.length - 1];
        this.A2 = (short[][]) Array.newInstance(short.class, new int[]{i2, i2});
        this.A2inv = null;
        ComputeInField computeInField = new ComputeInField();
        while (true) {
            if (this.A2inv != null) {
                break;
            }
            for (int i3 = 0; i3 < i2; i3++) {
                for (int i4 = 0; i4 < i2; i4++) {
                    this.A2[i3][i4] = (short) (this.sr.nextInt() & 255);
                }
            }
            this.A2inv = computeInField.inverse(this.A2);
        }
        this.b2 = new short[i2];
        for (i = 0; i < i2; i++) {
            this.b2[i] = (short) (this.sr.nextInt() & 255);
        }
    }

    private void initializeDefault() {
        initialize(new RainbowKeyGenerationParameters(CryptoServicesRegistrar.getSecureRandom(), new RainbowParameters()));
    }

    private void keygen() {
        generateL1();
        generateL2();
        generateF();
        computePublicKey();
    }

    public AsymmetricCipherKeyPair genKeyPair() {
        if (!this.initialized) {
            initializeDefault();
        }
        keygen();
        RainbowPrivateKeyParameters rainbowPrivateKeyParameters = new RainbowPrivateKeyParameters(this.A1inv, this.b1, this.A2inv, this.b2, this.vi, this.layers);
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new RainbowPublicKeyParameters(this.vi[this.vi.length - 1] - this.vi[0], this.pub_quadratic, this.pub_singular, this.pub_scalar), (AsymmetricKeyParameter) rainbowPrivateKeyParameters);
    }

    public AsymmetricCipherKeyPair generateKeyPair() {
        return genKeyPair();
    }

    public void init(KeyGenerationParameters keyGenerationParameters) {
        initialize(keyGenerationParameters);
    }

    public void initialize(KeyGenerationParameters keyGenerationParameters) {
        this.rainbowParams = (RainbowKeyGenerationParameters) keyGenerationParameters;
        this.sr = this.rainbowParams.getRandom();
        this.vi = this.rainbowParams.getParameters().getVi();
        this.numOfLayers = this.rainbowParams.getParameters().getNumOfLayers();
        this.initialized = true;
    }
}
