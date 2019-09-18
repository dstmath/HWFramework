package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.Arrays;

public final class XMSSMTPrivateKeyParameters extends AsymmetricKeyParameter implements XMSSStoreableObjectInterface {
    private final BDSStateMap bdsState;
    private final long index;
    private final XMSSMTParameters params;
    private final byte[] publicSeed;
    private final byte[] root;
    private final byte[] secretKeyPRF;
    private final byte[] secretKeySeed;

    public static class Builder {
        /* access modifiers changed from: private */
        public BDSStateMap bdsState = null;
        /* access modifiers changed from: private */
        public long index = 0;
        /* access modifiers changed from: private */
        public final XMSSMTParameters params;
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

        public Builder(XMSSMTParameters xMSSMTParameters) {
            this.params = xMSSMTParameters;
        }

        public XMSSMTPrivateKeyParameters build() {
            return new XMSSMTPrivateKeyParameters(this);
        }

        public Builder withBDSState(BDSStateMap bDSStateMap) {
            this.bdsState = bDSStateMap;
            return this;
        }

        public Builder withIndex(long j) {
            this.index = j;
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

    private XMSSMTPrivateKeyParameters(Builder builder) {
        super(true);
        this.params = builder.params;
        if (this.params != null) {
            int digestSize = this.params.getDigestSize();
            byte[] access$100 = builder.privateKey;
            if (access$100 == null) {
                this.index = builder.index;
                byte[] access$400 = builder.secretKeySeed;
                if (access$400 == null) {
                    this.secretKeySeed = new byte[digestSize];
                } else if (access$400.length == digestSize) {
                    this.secretKeySeed = access$400;
                } else {
                    throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
                }
                byte[] access$500 = builder.secretKeyPRF;
                if (access$500 == null) {
                    access$500 = new byte[digestSize];
                } else if (access$500.length != digestSize) {
                    throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
                }
                this.secretKeyPRF = access$500;
                byte[] access$600 = builder.publicSeed;
                if (access$600 == null) {
                    this.publicSeed = new byte[digestSize];
                } else if (access$600.length == digestSize) {
                    this.publicSeed = access$600;
                } else {
                    throw new IllegalArgumentException("size of publicSeed needs to be equal size of digest");
                }
                byte[] access$700 = builder.root;
                if (access$700 == null) {
                    this.root = new byte[digestSize];
                } else if (access$700.length == digestSize) {
                    this.root = access$700;
                } else {
                    throw new IllegalArgumentException("size of root needs to be equal size of digest");
                }
                BDSStateMap access$800 = builder.bdsState;
                if (access$800 == null) {
                    if (!XMSSUtil.isIndexValid(this.params.getHeight(), builder.index) || access$600 == null || access$400 == null) {
                        this.bdsState = new BDSStateMap();
                        return;
                    }
                    access$800 = new BDSStateMap(this.params, builder.index, access$600, access$400);
                }
                this.bdsState = access$800;
            } else if (builder.xmss != null) {
                int height = this.params.getHeight();
                int i = (height + 7) / 8;
                this.index = XMSSUtil.bytesToXBigEndian(access$100, 0, i);
                if (XMSSUtil.isIndexValid(height, this.index)) {
                    int i2 = 0 + i;
                    this.secretKeySeed = XMSSUtil.extractBytesAtOffset(access$100, i2, digestSize);
                    int i3 = i2 + digestSize;
                    this.secretKeyPRF = XMSSUtil.extractBytesAtOffset(access$100, i3, digestSize);
                    int i4 = i3 + digestSize;
                    this.publicSeed = XMSSUtil.extractBytesAtOffset(access$100, i4, digestSize);
                    int i5 = i4 + digestSize;
                    this.root = XMSSUtil.extractBytesAtOffset(access$100, i5, digestSize);
                    int i6 = i5 + digestSize;
                    try {
                        BDSStateMap bDSStateMap = (BDSStateMap) XMSSUtil.deserialize(XMSSUtil.extractBytesAtOffset(access$100, i6, access$100.length - i6), BDSStateMap.class);
                        bDSStateMap.setXMSS(builder.xmss);
                        this.bdsState = bDSStateMap;
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
    public BDSStateMap getBDSState() {
        return this.bdsState;
    }

    public long getIndex() {
        return this.index;
    }

    public XMSSMTPrivateKeyParameters getNextKey() {
        BDSStateMap bDSStateMap = new BDSStateMap(this.bdsState, this.params, getIndex(), this.publicSeed, this.secretKeySeed);
        return new Builder(this.params).withIndex(this.index + 1).withSecretKeySeed(this.secretKeySeed).withSecretKeyPRF(this.secretKeyPRF).withPublicSeed(this.publicSeed).withRoot(this.root).withBDSState(bDSStateMap).build();
    }

    public XMSSMTParameters getParameters() {
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
        int height = (this.params.getHeight() + 7) / 8;
        byte[] bArr = new byte[(height + digestSize + digestSize + digestSize + digestSize)];
        XMSSUtil.copyBytesAtOffset(bArr, XMSSUtil.toBytesBigEndian(this.index, height), 0);
        int i = 0 + height;
        XMSSUtil.copyBytesAtOffset(bArr, this.secretKeySeed, i);
        int i2 = i + digestSize;
        XMSSUtil.copyBytesAtOffset(bArr, this.secretKeyPRF, i2);
        int i3 = i2 + digestSize;
        XMSSUtil.copyBytesAtOffset(bArr, this.publicSeed, i3);
        XMSSUtil.copyBytesAtOffset(bArr, this.root, i3 + digestSize);
        try {
            return Arrays.concatenate(bArr, XMSSUtil.serialize(this.bdsState));
        } catch (IOException e) {
            throw new IllegalStateException("error serializing bds state: " + e.getMessage(), e);
        }
    }
}
