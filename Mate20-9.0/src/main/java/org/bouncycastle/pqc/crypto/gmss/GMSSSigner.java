package org.bouncycastle.pqc.crypto.gmss;

import java.lang.reflect.Array;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.pqc.crypto.MessageSigner;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSRandom;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSUtil;
import org.bouncycastle.pqc.crypto.gmss.util.WinternitzOTSVerify;
import org.bouncycastle.pqc.crypto.gmss.util.WinternitzOTSignature;
import org.bouncycastle.util.Arrays;

public class GMSSSigner implements MessageSigner {
    private byte[][][] currentAuthPaths;
    private GMSSDigestProvider digestProvider;
    private GMSSParameters gmssPS;
    private GMSSRandom gmssRandom;
    private GMSSUtil gmssUtil = new GMSSUtil();
    private int[] index;
    GMSSKeyParameters key;
    private int mdLength;
    private Digest messDigestOTS;
    private Digest messDigestTrees;
    private int numLayer;
    private WinternitzOTSignature ots;
    private byte[] pubKeyBytes;
    private SecureRandom random;
    private byte[][] subtreeRootSig;

    public GMSSSigner(GMSSDigestProvider gMSSDigestProvider) {
        this.digestProvider = gMSSDigestProvider;
        this.messDigestTrees = gMSSDigestProvider.get();
        this.messDigestOTS = this.messDigestTrees;
        this.mdLength = this.messDigestTrees.getDigestSize();
        this.gmssRandom = new GMSSRandom(this.messDigestTrees);
    }

    private void initSign() {
        this.messDigestTrees.reset();
        GMSSPrivateKeyParameters gMSSPrivateKeyParameters = (GMSSPrivateKeyParameters) this.key;
        if (gMSSPrivateKeyParameters.isUsed()) {
            throw new IllegalStateException("Private key already used");
        } else if (gMSSPrivateKeyParameters.getIndex(0) < gMSSPrivateKeyParameters.getNumLeafs(0)) {
            this.gmssPS = gMSSPrivateKeyParameters.getParameters();
            this.numLayer = this.gmssPS.getNumOfLayers();
            byte[] bArr = gMSSPrivateKeyParameters.getCurrentSeeds()[this.numLayer - 1];
            byte[] bArr2 = new byte[this.mdLength];
            byte[] bArr3 = new byte[this.mdLength];
            System.arraycopy(bArr, 0, bArr3, 0, this.mdLength);
            this.ots = new WinternitzOTSignature(this.gmssRandom.nextSeed(bArr3), this.digestProvider.get(), this.gmssPS.getWinternitzParameter()[this.numLayer - 1]);
            byte[][][] currentAuthPaths2 = gMSSPrivateKeyParameters.getCurrentAuthPaths();
            this.currentAuthPaths = new byte[this.numLayer][][];
            for (int i = 0; i < this.numLayer; i++) {
                this.currentAuthPaths[i] = (byte[][]) Array.newInstance(byte.class, new int[]{currentAuthPaths2[i].length, this.mdLength});
                for (int i2 = 0; i2 < currentAuthPaths2[i].length; i2++) {
                    System.arraycopy(currentAuthPaths2[i][i2], 0, this.currentAuthPaths[i][i2], 0, this.mdLength);
                }
            }
            this.index = new int[this.numLayer];
            System.arraycopy(gMSSPrivateKeyParameters.getIndex(), 0, this.index, 0, this.numLayer);
            this.subtreeRootSig = new byte[(this.numLayer - 1)][];
            for (int i3 = 0; i3 < this.numLayer - 1; i3++) {
                byte[] subtreeRootSig2 = gMSSPrivateKeyParameters.getSubtreeRootSig(i3);
                this.subtreeRootSig[i3] = new byte[subtreeRootSig2.length];
                System.arraycopy(subtreeRootSig2, 0, this.subtreeRootSig[i3], 0, subtreeRootSig2.length);
            }
            gMSSPrivateKeyParameters.markUsed();
        } else {
            throw new IllegalStateException("No more signatures can be generated");
        }
    }

    private void initVerify() {
        this.messDigestTrees.reset();
        GMSSPublicKeyParameters gMSSPublicKeyParameters = (GMSSPublicKeyParameters) this.key;
        this.pubKeyBytes = gMSSPublicKeyParameters.getPublicKey();
        this.gmssPS = gMSSPublicKeyParameters.getParameters();
        this.numLayer = this.gmssPS.getNumOfLayers();
    }

    public byte[] generateSignature(byte[] bArr) {
        byte[] bArr2 = new byte[this.mdLength];
        byte[] signature = this.ots.getSignature(bArr);
        byte[] concatenateArray = this.gmssUtil.concatenateArray(this.currentAuthPaths[this.numLayer - 1]);
        byte[] intToBytesLittleEndian = this.gmssUtil.intToBytesLittleEndian(this.index[this.numLayer - 1]);
        byte[] bArr3 = new byte[(intToBytesLittleEndian.length + signature.length + concatenateArray.length)];
        System.arraycopy(intToBytesLittleEndian, 0, bArr3, 0, intToBytesLittleEndian.length);
        System.arraycopy(signature, 0, bArr3, intToBytesLittleEndian.length, signature.length);
        System.arraycopy(concatenateArray, 0, bArr3, intToBytesLittleEndian.length + signature.length, concatenateArray.length);
        byte[] bArr4 = new byte[0];
        for (int i = (this.numLayer - 1) - 1; i >= 0; i--) {
            byte[] concatenateArray2 = this.gmssUtil.concatenateArray(this.currentAuthPaths[i]);
            byte[] intToBytesLittleEndian2 = this.gmssUtil.intToBytesLittleEndian(this.index[i]);
            byte[] bArr5 = new byte[bArr4.length];
            System.arraycopy(bArr4, 0, bArr5, 0, bArr4.length);
            bArr4 = new byte[(bArr5.length + intToBytesLittleEndian2.length + this.subtreeRootSig[i].length + concatenateArray2.length)];
            System.arraycopy(bArr5, 0, bArr4, 0, bArr5.length);
            System.arraycopy(intToBytesLittleEndian2, 0, bArr4, bArr5.length, intToBytesLittleEndian2.length);
            System.arraycopy(this.subtreeRootSig[i], 0, bArr4, bArr5.length + intToBytesLittleEndian2.length, this.subtreeRootSig[i].length);
            System.arraycopy(concatenateArray2, 0, bArr4, bArr5.length + intToBytesLittleEndian2.length + this.subtreeRootSig[i].length, concatenateArray2.length);
        }
        byte[] bArr6 = new byte[(bArr3.length + bArr4.length)];
        System.arraycopy(bArr3, 0, bArr6, 0, bArr3.length);
        System.arraycopy(bArr4, 0, bArr6, bArr3.length, bArr4.length);
        return bArr6;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (z) {
            if (cipherParameters instanceof ParametersWithRandom) {
                ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
                this.random = parametersWithRandom.getRandom();
                this.key = (GMSSPrivateKeyParameters) parametersWithRandom.getParameters();
            } else {
                this.random = CryptoServicesRegistrar.getSecureRandom();
                this.key = (GMSSPrivateKeyParameters) cipherParameters;
            }
            initSign();
            return;
        }
        this.key = (GMSSPublicKeyParameters) cipherParameters;
        initVerify();
    }

    public boolean verifySignature(byte[] bArr, byte[] bArr2) {
        this.messDigestOTS.reset();
        int i = this.numLayer - 1;
        int i2 = 0;
        while (i >= 0) {
            WinternitzOTSVerify winternitzOTSVerify = new WinternitzOTSVerify(this.digestProvider.get(), this.gmssPS.getWinternitzParameter()[i]);
            int signatureLength = winternitzOTSVerify.getSignatureLength();
            int bytesToIntLittleEndian = this.gmssUtil.bytesToIntLittleEndian(bArr2, i2);
            int i3 = i2 + 4;
            byte[] bArr3 = new byte[signatureLength];
            System.arraycopy(bArr2, i3, bArr3, 0, signatureLength);
            int i4 = i3 + signatureLength;
            byte[] Verify = winternitzOTSVerify.Verify(bArr, bArr3);
            if (Verify == null) {
                System.err.println("OTS Public Key is null in GMSSSignature.verify");
                return false;
            }
            byte[][] bArr4 = (byte[][]) Array.newInstance(byte.class, new int[]{this.gmssPS.getHeightOfTrees()[i], this.mdLength});
            int i5 = i4;
            for (byte[] arraycopy : bArr4) {
                System.arraycopy(bArr2, i5, arraycopy, 0, this.mdLength);
                i5 += this.mdLength;
            }
            byte[] bArr5 = new byte[this.mdLength];
            int length = (1 << bArr4.length) + bytesToIntLittleEndian;
            byte[] bArr6 = Verify;
            for (int i6 = 0; i6 < bArr4.length; i6++) {
                byte[] bArr7 = new byte[(this.mdLength << 1)];
                if (length % 2 == 0) {
                    System.arraycopy(bArr6, 0, bArr7, 0, this.mdLength);
                    System.arraycopy(bArr4[i6], 0, bArr7, this.mdLength, this.mdLength);
                } else {
                    System.arraycopy(bArr4[i6], 0, bArr7, 0, this.mdLength);
                    System.arraycopy(bArr6, 0, bArr7, this.mdLength, bArr6.length);
                    length--;
                }
                length /= 2;
                this.messDigestTrees.update(bArr7, 0, bArr7.length);
                bArr6 = new byte[this.messDigestTrees.getDigestSize()];
                this.messDigestTrees.doFinal(bArr6, 0);
            }
            i--;
            i2 = i5;
            bArr = bArr6;
        }
        return Arrays.areEqual(this.pubKeyBytes, bArr);
    }
}
