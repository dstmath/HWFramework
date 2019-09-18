package org.bouncycastle.pqc.crypto.xmss;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;

final class WOTSPlus {
    private final KeyedHashFunctions khf;
    private final WOTSPlusParameters params;
    private byte[] publicSeed;
    private byte[] secretKeySeed;

    protected WOTSPlus(WOTSPlusParameters wOTSPlusParameters) {
        if (wOTSPlusParameters != null) {
            this.params = wOTSPlusParameters;
            int digestSize = wOTSPlusParameters.getDigestSize();
            this.khf = new KeyedHashFunctions(wOTSPlusParameters.getDigest(), digestSize);
            this.secretKeySeed = new byte[digestSize];
            this.publicSeed = new byte[digestSize];
            return;
        }
        throw new NullPointerException("params == null");
    }

    private byte[] chain(byte[] bArr, int i, int i2, OTSHashAddress oTSHashAddress) {
        int digestSize = this.params.getDigestSize();
        if (bArr == null) {
            throw new NullPointerException("startHash == null");
        } else if (bArr.length != digestSize) {
            throw new IllegalArgumentException("startHash needs to be " + digestSize + "bytes");
        } else if (oTSHashAddress == null) {
            throw new NullPointerException("otsHashAddress == null");
        } else if (oTSHashAddress.toByteArray() != null) {
            int i3 = i + i2;
            if (i3 > this.params.getWinternitzParameter() - 1) {
                throw new IllegalArgumentException("max chain length must not be greater than w");
            } else if (i2 == 0) {
                return bArr;
            } else {
                byte[] chain = chain(bArr, i, i2 - 1, oTSHashAddress);
                OTSHashAddress oTSHashAddress2 = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(oTSHashAddress.getOTSAddress()).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(i3 - 1).withKeyAndMask(0)).build();
                byte[] PRF = this.khf.PRF(this.publicSeed, oTSHashAddress2.toByteArray());
                byte[] PRF2 = this.khf.PRF(this.publicSeed, ((OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress2.getLayerAddress())).withTreeAddress(oTSHashAddress2.getTreeAddress())).withOTSAddress(oTSHashAddress2.getOTSAddress()).withChainAddress(oTSHashAddress2.getChainAddress()).withHashAddress(oTSHashAddress2.getHashAddress()).withKeyAndMask(1)).build()).toByteArray());
                byte[] bArr2 = new byte[digestSize];
                for (int i4 = 0; i4 < digestSize; i4++) {
                    bArr2[i4] = (byte) (chain[i4] ^ PRF2[i4]);
                }
                return this.khf.F(PRF, bArr2);
            }
        } else {
            throw new NullPointerException("otsHashAddress byte array == null");
        }
    }

    private List<Integer> convertToBaseW(byte[] bArr, int i, int i2) {
        if (bArr == null) {
            throw new NullPointerException("msg == null");
        } else if (i == 4 || i == 16) {
            int log2 = XMSSUtil.log2(i);
            if (i2 <= (bArr.length * 8) / log2) {
                ArrayList arrayList = new ArrayList();
                for (int i3 = 0; i3 < bArr.length; i3++) {
                    for (int i4 = 8 - log2; i4 >= 0; i4 -= log2) {
                        arrayList.add(Integer.valueOf((bArr[i3] >> i4) & (i - 1)));
                        if (arrayList.size() == i2) {
                            return arrayList;
                        }
                    }
                }
                return arrayList;
            }
            throw new IllegalArgumentException("outLength too big");
        } else {
            throw new IllegalArgumentException("w needs to be 4 or 16");
        }
    }

    private byte[] expandSecretKeySeed(int i) {
        if (i >= 0 && i < this.params.getLen()) {
            return this.khf.PRF(this.secretKeySeed, XMSSUtil.toBytesBigEndian((long) i, 32));
        }
        throw new IllegalArgumentException("index out of bounds");
    }

    /* access modifiers changed from: protected */
    public KeyedHashFunctions getKhf() {
        return this.khf;
    }

    /* access modifiers changed from: protected */
    public WOTSPlusParameters getParams() {
        return this.params;
    }

    /* access modifiers changed from: protected */
    public WOTSPlusPrivateKeyParameters getPrivateKey() {
        byte[][] bArr = new byte[this.params.getLen()][];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = expandSecretKeySeed(i);
        }
        return new WOTSPlusPrivateKeyParameters(this.params, bArr);
    }

    /* access modifiers changed from: protected */
    public WOTSPlusPublicKeyParameters getPublicKey(OTSHashAddress oTSHashAddress) {
        if (oTSHashAddress != null) {
            byte[][] bArr = new byte[this.params.getLen()][];
            OTSHashAddress oTSHashAddress2 = oTSHashAddress;
            for (int i = 0; i < this.params.getLen(); i++) {
                oTSHashAddress2 = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress2.getLayerAddress())).withTreeAddress(oTSHashAddress2.getTreeAddress())).withOTSAddress(oTSHashAddress2.getOTSAddress()).withChainAddress(i).withHashAddress(oTSHashAddress2.getHashAddress()).withKeyAndMask(oTSHashAddress2.getKeyAndMask())).build();
                bArr[i] = chain(expandSecretKeySeed(i), 0, this.params.getWinternitzParameter() - 1, oTSHashAddress2);
            }
            return new WOTSPlusPublicKeyParameters(this.params, bArr);
        }
        throw new NullPointerException("otsHashAddress == null");
    }

    /* access modifiers changed from: protected */
    public WOTSPlusPublicKeyParameters getPublicKeyFromSignature(byte[] bArr, WOTSPlusSignature wOTSPlusSignature, OTSHashAddress oTSHashAddress) {
        if (bArr == null) {
            throw new NullPointerException("messageDigest == null");
        } else if (bArr.length != this.params.getDigestSize()) {
            throw new IllegalArgumentException("size of messageDigest needs to be equal to size of digest");
        } else if (wOTSPlusSignature == null) {
            throw new NullPointerException("signature == null");
        } else if (oTSHashAddress != null) {
            List<Integer> convertToBaseW = convertToBaseW(bArr, this.params.getWinternitzParameter(), this.params.getLen1());
            int i = 0;
            for (int i2 = 0; i2 < this.params.getLen1(); i2++) {
                i += (this.params.getWinternitzParameter() - 1) - convertToBaseW.get(i2).intValue();
            }
            convertToBaseW.addAll(convertToBaseW(XMSSUtil.toBytesBigEndian((long) (i << (8 - ((this.params.getLen2() * XMSSUtil.log2(this.params.getWinternitzParameter())) % 8))), (int) Math.ceil(((double) (this.params.getLen2() * XMSSUtil.log2(this.params.getWinternitzParameter()))) / 8.0d)), this.params.getWinternitzParameter(), this.params.getLen2()));
            byte[][] bArr2 = new byte[this.params.getLen()][];
            for (int i3 = 0; i3 < this.params.getLen(); i3++) {
                oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(oTSHashAddress.getOTSAddress()).withChainAddress(i3).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
                bArr2[i3] = chain(wOTSPlusSignature.toByteArray()[i3], convertToBaseW.get(i3).intValue(), (this.params.getWinternitzParameter() - 1) - convertToBaseW.get(i3).intValue(), oTSHashAddress);
            }
            return new WOTSPlusPublicKeyParameters(this.params, bArr2);
        } else {
            throw new NullPointerException("otsHashAddress == null");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] getPublicSeed() {
        return XMSSUtil.cloneArray(this.publicSeed);
    }

    /* access modifiers changed from: protected */
    public byte[] getSecretKeySeed() {
        return XMSSUtil.cloneArray(getSecretKeySeed());
    }

    /* access modifiers changed from: protected */
    public byte[] getWOTSPlusSecretKey(byte[] bArr, OTSHashAddress oTSHashAddress) {
        return this.khf.PRF(bArr, ((OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(oTSHashAddress.getOTSAddress()).build()).toByteArray());
    }

    /* access modifiers changed from: package-private */
    public void importKeys(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            throw new NullPointerException("secretKeySeed == null");
        } else if (bArr.length != this.params.getDigestSize()) {
            throw new IllegalArgumentException("size of secretKeySeed needs to be equal to size of digest");
        } else if (bArr2 == null) {
            throw new NullPointerException("publicSeed == null");
        } else if (bArr2.length == this.params.getDigestSize()) {
            this.secretKeySeed = bArr;
            this.publicSeed = bArr2;
        } else {
            throw new IllegalArgumentException("size of publicSeed needs to be equal to size of digest");
        }
    }

    /* access modifiers changed from: protected */
    public WOTSPlusSignature sign(byte[] bArr, OTSHashAddress oTSHashAddress) {
        if (bArr == null) {
            throw new NullPointerException("messageDigest == null");
        } else if (bArr.length != this.params.getDigestSize()) {
            throw new IllegalArgumentException("size of messageDigest needs to be equal to size of digest");
        } else if (oTSHashAddress != null) {
            List<Integer> convertToBaseW = convertToBaseW(bArr, this.params.getWinternitzParameter(), this.params.getLen1());
            int i = 0;
            for (int i2 = 0; i2 < this.params.getLen1(); i2++) {
                i += (this.params.getWinternitzParameter() - 1) - convertToBaseW.get(i2).intValue();
            }
            convertToBaseW.addAll(convertToBaseW(XMSSUtil.toBytesBigEndian((long) (i << (8 - ((this.params.getLen2() * XMSSUtil.log2(this.params.getWinternitzParameter())) % 8))), (int) Math.ceil(((double) (this.params.getLen2() * XMSSUtil.log2(this.params.getWinternitzParameter()))) / 8.0d)), this.params.getWinternitzParameter(), this.params.getLen2()));
            byte[][] bArr2 = new byte[this.params.getLen()][];
            OTSHashAddress oTSHashAddress2 = oTSHashAddress;
            for (int i3 = 0; i3 < this.params.getLen(); i3++) {
                oTSHashAddress2 = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress2.getLayerAddress())).withTreeAddress(oTSHashAddress2.getTreeAddress())).withOTSAddress(oTSHashAddress2.getOTSAddress()).withChainAddress(i3).withHashAddress(oTSHashAddress2.getHashAddress()).withKeyAndMask(oTSHashAddress2.getKeyAndMask())).build();
                bArr2[i3] = chain(expandSecretKeySeed(i3), 0, convertToBaseW.get(i3).intValue(), oTSHashAddress2);
            }
            return new WOTSPlusSignature(this.params, bArr2);
        } else {
            throw new NullPointerException("otsHashAddress == null");
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifySignature(byte[] bArr, WOTSPlusSignature wOTSPlusSignature, OTSHashAddress oTSHashAddress) {
        if (bArr == null) {
            throw new NullPointerException("messageDigest == null");
        } else if (bArr.length != this.params.getDigestSize()) {
            throw new IllegalArgumentException("size of messageDigest needs to be equal to size of digest");
        } else if (wOTSPlusSignature == null) {
            throw new NullPointerException("signature == null");
        } else if (oTSHashAddress != null) {
            return XMSSUtil.areEqual(getPublicKeyFromSignature(bArr, wOTSPlusSignature, oTSHashAddress).toByteArray(), getPublicKey(oTSHashAddress).toByteArray());
        } else {
            throw new NullPointerException("otsHashAddress == null");
        }
    }
}
