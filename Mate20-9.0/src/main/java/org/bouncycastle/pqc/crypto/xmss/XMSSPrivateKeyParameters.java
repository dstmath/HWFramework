package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public final class XMSSPrivateKeyParameters extends AsymmetricKeyParameter implements XMSSStoreableObjectInterface {
    private final BDS bdsState;
    private final XMSSParameters params;
    private final byte[] publicSeed;
    private final byte[] root;
    private final byte[] secretKeyPRF;
    private final byte[] secretKeySeed;

    public static class Builder {
        /* access modifiers changed from: private */
        public BDS bdsState = null;
        /* access modifiers changed from: private */
        public int index = 0;
        /* access modifiers changed from: private */
        public final XMSSParameters params;
        /* access modifiers changed from: private */
        public byte[] privateKey = null;
        /* access modifiers changed from: private */
        public byte[] publicSeed = null;
        /* access modifiers changed from: private */
        public byte[] root = null;
        /* access modifiers changed from: private */
        public byte[] secretKeyPRF = null;
        /* access modifiers changed from: private */
        public byte[] secretKeySeed = null;
        /* access modifiers changed from: private */
        public XMSSParameters xmss = null;

        public Builder(XMSSParameters xMSSParameters) {
            this.params = xMSSParameters;
        }

        public XMSSPrivateKeyParameters build() {
            return new XMSSPrivateKeyParameters(this);
        }

        public Builder withBDSState(BDS bds) {
            this.bdsState = bds;
            return this;
        }

        public Builder withIndex(int i) {
            this.index = i;
            return this;
        }

        public Builder withPrivateKey(byte[] bArr, XMSSParameters xMSSParameters) {
            this.privateKey = XMSSUtil.cloneArray(bArr);
            this.xmss = xMSSParameters;
            return this;
        }

        public Builder withPublicSeed(byte[] bArr) {
            this.publicSeed = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withRoot(byte[] bArr) {
            this.root = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withSecretKeyPRF(byte[] bArr) {
            this.secretKeyPRF = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withSecretKeySeed(byte[] bArr) {
            this.secretKeySeed = XMSSUtil.cloneArray(bArr);
            return this;
        }
    }

    private XMSSPrivateKeyParameters(Builder builder) {
        super(true);
        this.params = builder.params;
        if (this.params != null) {
            int digestSize = this.params.getDigestSize();
            byte[] access$100 = builder.privateKey;
            if (access$100 == null) {
                byte[] access$300 = builder.secretKeySeed;
                if (access$300 == null) {
                    this.secretKeySeed = new byte[digestSize];
                } else if (access$300.length == digestSize) {
                    this.secretKeySeed = access$300;
                } else {
                    throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
                }
                byte[] access$400 = builder.secretKeyPRF;
                if (access$400 == null) {
                    access$400 = new byte[digestSize];
                } else if (access$400.length != digestSize) {
                    throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
                }
                this.secretKeyPRF = access$400;
                byte[] access$500 = builder.publicSeed;
                if (access$500 == null) {
                    this.publicSeed = new byte[digestSize];
                } else if (access$500.length == digestSize) {
                    this.publicSeed = access$500;
                } else {
                    throw new IllegalArgumentException("size of publicSeed needs to be equal size of digest");
                }
                byte[] access$600 = builder.root;
                if (access$600 == null) {
                    this.root = new byte[digestSize];
                } else if (access$600.length == digestSize) {
                    this.root = access$600;
                } else {
                    throw new IllegalArgumentException("size of root needs to be equal size of digest");
                }
                BDS access$700 = builder.bdsState;
                if (access$700 != null) {
                    this.bdsState = access$700;
                } else {
                    this.bdsState = (builder.index >= (1 << this.params.getHeight()) + -2 || access$500 == null || access$300 == null) ? new BDS(this.params, builder.index) : new BDS(this.params, access$500, access$300, (OTSHashAddress) new OTSHashAddress.Builder().build(), builder.index);
                }
            } else if (builder.xmss != null) {
                int height = this.params.getHeight();
                int bigEndianToInt = Pack.bigEndianToInt(access$100, 0);
                if (XMSSUtil.isIndexValid(height, (long) bigEndianToInt)) {
                    this.secretKeySeed = XMSSUtil.extractBytesAtOffset(access$100, 4, digestSize);
                    int i = 4 + digestSize;
                    this.secretKeyPRF = XMSSUtil.extractBytesAtOffset(access$100, i, digestSize);
                    int i2 = i + digestSize;
                    this.publicSeed = XMSSUtil.extractBytesAtOffset(access$100, i2, digestSize);
                    int i3 = i2 + digestSize;
                    this.root = XMSSUtil.extractBytesAtOffset(access$100, i3, digestSize);
                    int i4 = i3 + digestSize;
                    try {
                        BDS bds = (BDS) XMSSUtil.deserialize(XMSSUtil.extractBytesAtOffset(access$100, i4, access$100.length - i4), BDS.class);
                        bds.setXMSS(builder.xmss);
                        bds.validate();
                        if (bds.getIndex() == bigEndianToInt) {
                            this.bdsState = bds;
                            return;
                        }
                        throw new IllegalStateException("serialized BDS has wrong index");
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e.getMessage(), e);
                    } catch (ClassNotFoundException e2) {
                        throw new IllegalArgumentException(e2.getMessage(), e2);
                    }
                } else {
                    throw new IllegalArgumentException("index out of bounds");
                }
            } else {
                throw new NullPointerException("xmss == null");
            }
        } else {
            throw new NullPointerException("params == null");
        }
    }

    /* access modifiers changed from: package-private */
    public BDS getBDSState() {
        return this.bdsState;
    }

    public int getIndex() {
        return this.bdsState.getIndex();
    }

    public XMSSPrivateKeyParameters getNextKey() {
        Builder withRoot;
        BDS bds;
        if (getIndex() < (1 << this.params.getHeight()) - 1) {
            withRoot = new Builder(this.params).withSecretKeySeed(this.secretKeySeed).withSecretKeyPRF(this.secretKeyPRF).withPublicSeed(this.publicSeed).withRoot(this.root);
            bds = this.bdsState.getNextState(this.publicSeed, this.secretKeySeed, (OTSHashAddress) new OTSHashAddress.Builder().build());
        } else {
            withRoot = new Builder(this.params).withSecretKeySeed(this.secretKeySeed).withSecretKeyPRF(this.secretKeyPRF).withPublicSeed(this.publicSeed).withRoot(this.root);
            bds = new BDS(this.params, getIndex() + 1);
        }
        return withRoot.withBDSState(bds).build();
    }

    public XMSSParameters getParameters() {
        return this.params;
    }

    public byte[] getPublicSeed() {
        return XMSSUtil.cloneArray(this.publicSeed);
    }

    public byte[] getRoot() {
        return XMSSUtil.cloneArray(this.root);
    }

    public byte[] getSecretKeyPRF() {
        return XMSSUtil.cloneArray(this.secretKeyPRF);
    }

    public byte[] getSecretKeySeed() {
        return XMSSUtil.cloneArray(this.secretKeySeed);
    }

    public byte[] toByteArray() {
        int digestSize = this.params.getDigestSize();
        byte[] bArr = new byte[(4 + digestSize + digestSize + digestSize + digestSize)];
        Pack.intToBigEndian(this.bdsState.getIndex(), bArr, 0);
        XMSSUtil.copyBytesAtOffset(bArr, this.secretKeySeed, 4);
        int i = 4 + digestSize;
        XMSSUtil.copyBytesAtOffset(bArr, this.secretKeyPRF, i);
        int i2 = i + digestSize;
        XMSSUtil.copyBytesAtOffset(bArr, this.publicSeed, i2);
        XMSSUtil.copyBytesAtOffset(bArr, this.root, i2 + digestSize);
        try {
            return Arrays.concatenate(bArr, XMSSUtil.serialize(this.bdsState));
        } catch (IOException e) {
            throw new RuntimeException("error serializing bds state: " + e.getMessage());
        }
    }
}
