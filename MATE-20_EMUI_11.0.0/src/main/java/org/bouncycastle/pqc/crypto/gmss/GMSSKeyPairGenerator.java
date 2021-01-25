package org.bouncycastle.pqc.crypto.gmss;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import java.util.Vector;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSRandom;
import org.bouncycastle.pqc.crypto.gmss.util.WinternitzOTSVerify;
import org.bouncycastle.pqc.crypto.gmss.util.WinternitzOTSignature;

public class GMSSKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    public static final String OID = "1.3.6.1.4.1.8301.3.1.3.3";
    private int[] K;
    private byte[][] currentRootSigs;
    private byte[][] currentSeeds;
    private GMSSDigestProvider digestProvider;
    private GMSSParameters gmssPS;
    private GMSSKeyGenerationParameters gmssParams;
    private GMSSRandom gmssRandom;
    private int[] heightOfTrees;
    private boolean initialized = false;
    private int mdLength;
    private Digest messDigestTree;
    private byte[][] nextNextSeeds;
    private int numLayer;
    private int[] otsIndex;

    public GMSSKeyPairGenerator(GMSSDigestProvider gMSSDigestProvider) {
        this.digestProvider = gMSSDigestProvider;
        this.messDigestTree = gMSSDigestProvider.get();
        this.mdLength = this.messDigestTree.getDigestSize();
        this.gmssRandom = new GMSSRandom(this.messDigestTree);
    }

    private AsymmetricCipherKeyPair genKeyPair() {
        int i;
        int i2;
        byte[] bArr;
        Vector vector;
        byte[] bArr2;
        if (!this.initialized) {
            initializeDefault();
        }
        int i3 = this.numLayer;
        byte[][][] bArr3 = new byte[i3][][];
        byte[][][] bArr4 = new byte[(i3 - 1)][][];
        Treehash[][] treehashArr = new Treehash[i3][];
        Treehash[][] treehashArr2 = new Treehash[(i3 - 1)][];
        Vector[] vectorArr = new Vector[i3];
        Vector[] vectorArr2 = new Vector[(i3 - 1)];
        Vector[][] vectorArr3 = new Vector[i3][];
        Vector[][] vectorArr4 = new Vector[(i3 - 1)][];
        int i4 = 0;
        while (true) {
            i = this.numLayer;
            if (i4 >= i) {
                break;
            }
            bArr3[i4] = (byte[][]) Array.newInstance(byte.class, this.heightOfTrees[i4], this.mdLength);
            int[] iArr = this.heightOfTrees;
            treehashArr[i4] = new Treehash[(iArr[i4] - this.K[i4])];
            if (i4 > 0) {
                int i5 = i4 - 1;
                bArr4[i5] = (byte[][]) Array.newInstance(byte.class, iArr[i4], this.mdLength);
                treehashArr2[i5] = new Treehash[(this.heightOfTrees[i4] - this.K[i4])];
            }
            vectorArr[i4] = new Vector();
            if (i4 > 0) {
                vectorArr2[i4 - 1] = new Vector();
            }
            i4++;
        }
        byte[][] bArr5 = (byte[][]) Array.newInstance(byte.class, i, this.mdLength);
        byte[][] bArr6 = (byte[][]) Array.newInstance(byte.class, this.numLayer - 1, this.mdLength);
        byte[][] bArr7 = (byte[][]) Array.newInstance(byte.class, this.numLayer, this.mdLength);
        int i6 = 0;
        while (true) {
            i2 = this.numLayer;
            if (i6 >= i2) {
                break;
            }
            System.arraycopy(this.currentSeeds[i6], 0, bArr7[i6], 0, this.mdLength);
            i6++;
            bArr6 = bArr6;
        }
        this.currentRootSigs = (byte[][]) Array.newInstance(byte.class, i2 - 1, this.mdLength);
        int i7 = this.numLayer - 1;
        while (i7 >= 0) {
            if (i7 == this.numLayer - 1) {
                bArr2 = null;
                vector = vectorArr[i7];
                bArr = bArr7[i7];
            } else {
                bArr2 = bArr5[i7 + 1];
                vector = vectorArr[i7];
                bArr = bArr7[i7];
            }
            GMSSRootCalc generateCurrentAuthpathAndRoot = generateCurrentAuthpathAndRoot(bArr2, vector, bArr, i7);
            int i8 = 0;
            while (i8 < this.heightOfTrees[i7]) {
                System.arraycopy(generateCurrentAuthpathAndRoot.getAuthPath()[i8], 0, bArr3[i7][i8], 0, this.mdLength);
                i8++;
                bArr3 = bArr3;
                vectorArr = vectorArr;
            }
            vectorArr3[i7] = generateCurrentAuthpathAndRoot.getRetain();
            treehashArr[i7] = generateCurrentAuthpathAndRoot.getTreehash();
            System.arraycopy(generateCurrentAuthpathAndRoot.getRoot(), 0, bArr5[i7], 0, this.mdLength);
            i7--;
            bArr3 = bArr3;
            vectorArr = vectorArr;
        }
        int i9 = this.numLayer - 2;
        while (i9 >= 0) {
            int i10 = i9 + 1;
            GMSSRootCalc generateNextAuthpathAndRoot = generateNextAuthpathAndRoot(vectorArr2[i9], bArr7[i10], i10);
            int i11 = 0;
            while (i11 < this.heightOfTrees[i10]) {
                System.arraycopy(generateNextAuthpathAndRoot.getAuthPath()[i11], 0, bArr4[i9][i11], 0, this.mdLength);
                i11++;
                vectorArr3 = vectorArr3;
            }
            vectorArr4[i9] = generateNextAuthpathAndRoot.getRetain();
            treehashArr2[i9] = generateNextAuthpathAndRoot.getTreehash();
            System.arraycopy(generateNextAuthpathAndRoot.getRoot(), 0, bArr6[i9], 0, this.mdLength);
            System.arraycopy(bArr7[i10], 0, this.nextNextSeeds[i9], 0, this.mdLength);
            i9--;
            vectorArr3 = vectorArr3;
        }
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) new GMSSPublicKeyParameters(bArr5[0], this.gmssPS), (AsymmetricKeyParameter) new GMSSPrivateKeyParameters(this.currentSeeds, this.nextNextSeeds, bArr3, bArr4, treehashArr, treehashArr2, vectorArr, vectorArr2, vectorArr3, vectorArr4, bArr6, this.currentRootSigs, this.gmssPS, this.digestProvider));
    }

    private GMSSRootCalc generateCurrentAuthpathAndRoot(byte[] bArr, Vector vector, byte[] bArr2, int i) {
        byte[] bArr3;
        int i2 = this.mdLength;
        byte[] bArr4 = new byte[i2];
        byte[] bArr5 = new byte[i2];
        byte[] nextSeed = this.gmssRandom.nextSeed(bArr2);
        GMSSRootCalc gMSSRootCalc = new GMSSRootCalc(this.heightOfTrees[i], this.K[i], this.digestProvider);
        gMSSRootCalc.initialize(vector);
        if (i == this.numLayer - 1) {
            bArr3 = new WinternitzOTSignature(nextSeed, this.digestProvider.get(), this.otsIndex[i]).getPublicKey();
        } else {
            this.currentRootSigs[i] = new WinternitzOTSignature(nextSeed, this.digestProvider.get(), this.otsIndex[i]).getSignature(bArr);
            bArr3 = new WinternitzOTSVerify(this.digestProvider.get(), this.otsIndex[i]).Verify(bArr, this.currentRootSigs[i]);
        }
        gMSSRootCalc.update(bArr3);
        int i3 = 0;
        int i4 = 3;
        int i5 = 1;
        while (true) {
            int[] iArr = this.heightOfTrees;
            if (i5 >= (1 << iArr[i])) {
                break;
            }
            if (i5 == i4 && i3 < iArr[i] - this.K[i]) {
                gMSSRootCalc.initializeTreehashSeed(bArr2, i3);
                i4 *= 2;
                i3++;
            }
            gMSSRootCalc.update(new WinternitzOTSignature(this.gmssRandom.nextSeed(bArr2), this.digestProvider.get(), this.otsIndex[i]).getPublicKey());
            i5++;
        }
        if (gMSSRootCalc.wasFinished()) {
            return gMSSRootCalc;
        }
        System.err.println("Baum noch nicht fertig konstruiert!!!");
        return null;
    }

    private GMSSRootCalc generateNextAuthpathAndRoot(Vector vector, byte[] bArr, int i) {
        byte[] bArr2 = new byte[this.numLayer];
        GMSSRootCalc gMSSRootCalc = new GMSSRootCalc(this.heightOfTrees[i], this.K[i], this.digestProvider);
        gMSSRootCalc.initialize(vector);
        int i2 = 0;
        int i3 = 3;
        int i4 = 0;
        while (true) {
            int[] iArr = this.heightOfTrees;
            if (i2 >= (1 << iArr[i])) {
                break;
            }
            if (i2 == i3 && i4 < iArr[i] - this.K[i]) {
                gMSSRootCalc.initializeTreehashSeed(bArr, i4);
                i3 *= 2;
                i4++;
            }
            gMSSRootCalc.update(new WinternitzOTSignature(this.gmssRandom.nextSeed(bArr), this.digestProvider.get(), this.otsIndex[i]).getPublicKey());
            i2++;
        }
        if (gMSSRootCalc.wasFinished()) {
            return gMSSRootCalc;
        }
        System.err.println("N�chster Baum noch nicht fertig konstruiert!!!");
        return null;
    }

    private void initializeDefault() {
        int[] iArr = {10, 10, 10, 10};
        initialize(new GMSSKeyGenerationParameters(CryptoServicesRegistrar.getSecureRandom(), new GMSSParameters(iArr.length, iArr, new int[]{3, 3, 3, 3}, new int[]{2, 2, 2, 2})));
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        return genKeyPair();
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        initialize(keyGenerationParameters);
    }

    public void initialize(int i, SecureRandom secureRandom) {
        GMSSKeyGenerationParameters gMSSKeyGenerationParameters;
        if (i <= 10) {
            int[] iArr = {10};
            gMSSKeyGenerationParameters = new GMSSKeyGenerationParameters(secureRandom, new GMSSParameters(iArr.length, iArr, new int[]{3}, new int[]{2}));
        } else if (i <= 20) {
            int[] iArr2 = {10, 10};
            gMSSKeyGenerationParameters = new GMSSKeyGenerationParameters(secureRandom, new GMSSParameters(iArr2.length, iArr2, new int[]{5, 4}, new int[]{2, 2}));
        } else {
            int[] iArr3 = {10, 10, 10, 10};
            gMSSKeyGenerationParameters = new GMSSKeyGenerationParameters(secureRandom, new GMSSParameters(iArr3.length, iArr3, new int[]{9, 9, 9, 3}, new int[]{2, 2, 2, 2}));
        }
        initialize(gMSSKeyGenerationParameters);
    }

    public void initialize(KeyGenerationParameters keyGenerationParameters) {
        this.gmssParams = (GMSSKeyGenerationParameters) keyGenerationParameters;
        this.gmssPS = new GMSSParameters(this.gmssParams.getParameters().getNumOfLayers(), this.gmssParams.getParameters().getHeightOfTrees(), this.gmssParams.getParameters().getWinternitzParameter(), this.gmssParams.getParameters().getK());
        this.numLayer = this.gmssPS.getNumOfLayers();
        this.heightOfTrees = this.gmssPS.getHeightOfTrees();
        this.otsIndex = this.gmssPS.getWinternitzParameter();
        this.K = this.gmssPS.getK();
        this.currentSeeds = (byte[][]) Array.newInstance(byte.class, this.numLayer, this.mdLength);
        this.nextNextSeeds = (byte[][]) Array.newInstance(byte.class, this.numLayer - 1, this.mdLength);
        SecureRandom secureRandom = CryptoServicesRegistrar.getSecureRandom();
        for (int i = 0; i < this.numLayer; i++) {
            secureRandom.nextBytes(this.currentSeeds[i]);
            this.gmssRandom.nextSeed(this.currentSeeds[i]);
        }
        this.initialized = true;
    }
}
