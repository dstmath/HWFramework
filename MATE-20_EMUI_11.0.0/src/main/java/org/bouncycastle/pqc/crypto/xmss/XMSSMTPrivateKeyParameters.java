package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Encodable;

public final class XMSSMTPrivateKeyParameters extends XMSSMTKeyParameters implements XMSSStoreableObjectInterface, Encodable {
    private volatile BDSStateMap bdsState;
    private volatile long index;
    private final XMSSMTParameters params;
    private final byte[] publicSeed;
    private final byte[] root;
    private final byte[] secretKeyPRF;
    private final byte[] secretKeySeed;
    private volatile boolean used;

    public static class Builder {
        private BDSStateMap bdsState = null;
        private long index = 0;
        private long maxIndex = -1;
        private final XMSSMTParameters params;
        private byte[] privateKey = null;
        private byte[] publicSeed = null;
        private byte[] root = null;
        private byte[] secretKeyPRF = null;
        private byte[] secretKeySeed = null;
        private XMSSParameters xmss = null;

        public Builder(XMSSMTParameters xMSSMTParameters) {
            this.params = xMSSMTParameters;
        }

        public XMSSMTPrivateKeyParameters build() {
            return new XMSSMTPrivateKeyParameters(this);
        }

        public Builder withBDSState(BDSStateMap bDSStateMap) {
            if (bDSStateMap.getMaxIndex() == 0) {
                this.bdsState = new BDSStateMap(bDSStateMap, (1 << this.params.getHeight()) - 1);
            } else {
                this.bdsState = bDSStateMap;
            }
            return this;
        }

        public Builder withIndex(long j) {
            this.index = j;
            return this;
        }

        public Builder withMaxIndex(long j) {
            this.maxIndex = j;
            return this;
        }

        public Builder withPrivateKey(byte[] bArr) {
            this.privateKey = XMSSUtil.cloneArray(bArr);
            this.xmss = this.params.getXMSSParameters();
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
        super(true, builder.params.getTreeDigest());
        this.params = builder.params;
        XMSSMTParameters xMSSMTParameters = this.params;
        if (xMSSMTParameters != null) {
            int treeDigestSize = xMSSMTParameters.getTreeDigestSize();
            byte[] bArr = builder.privateKey;
            if (bArr == null) {
                this.index = builder.index;
                byte[] bArr2 = builder.secretKeySeed;
                if (bArr2 == null) {
                    this.secretKeySeed = new byte[treeDigestSize];
                } else if (bArr2.length == treeDigestSize) {
                    this.secretKeySeed = bArr2;
                } else {
                    throw new IllegalArgumentException("size of secretKeySeed needs to be equal size of digest");
                }
                byte[] bArr3 = builder.secretKeyPRF;
                if (bArr3 == null) {
                    bArr3 = new byte[treeDigestSize];
                } else if (bArr3.length != treeDigestSize) {
                    throw new IllegalArgumentException("size of secretKeyPRF needs to be equal size of digest");
                }
                this.secretKeyPRF = bArr3;
                byte[] bArr4 = builder.publicSeed;
                if (bArr4 == null) {
                    this.publicSeed = new byte[treeDigestSize];
                } else if (bArr4.length == treeDigestSize) {
                    this.publicSeed = bArr4;
                } else {
                    throw new IllegalArgumentException("size of publicSeed needs to be equal size of digest");
                }
                byte[] bArr5 = builder.root;
                if (bArr5 == null) {
                    this.root = new byte[treeDigestSize];
                } else if (bArr5.length == treeDigestSize) {
                    this.root = bArr5;
                } else {
                    throw new IllegalArgumentException("size of root needs to be equal size of digest");
                }
                BDSStateMap bDSStateMap = builder.bdsState;
                if (bDSStateMap == null) {
                    bDSStateMap = (!XMSSUtil.isIndexValid(this.params.getHeight(), builder.index) || bArr4 == null || bArr2 == null) ? new BDSStateMap(builder.maxIndex + 1) : new BDSStateMap(this.params, builder.index, bArr4, bArr2);
                }
                this.bdsState = bDSStateMap;
                if (builder.maxIndex >= 0 && builder.maxIndex != this.bdsState.getMaxIndex()) {
                    throw new IllegalArgumentException("maxIndex set but not reflected in state");
                }
            } else if (builder.xmss != null) {
                int height = this.params.getHeight();
                int i = (height + 7) / 8;
                this.index = XMSSUtil.bytesToXBigEndian(bArr, 0, i);
                if (XMSSUtil.isIndexValid(height, this.index)) {
                    int i2 = i + 0;
                    this.secretKeySeed = XMSSUtil.extractBytesAtOffset(bArr, i2, treeDigestSize);
                    int i3 = i2 + treeDigestSize;
                    this.secretKeyPRF = XMSSUtil.extractBytesAtOffset(bArr, i3, treeDigestSize);
                    int i4 = i3 + treeDigestSize;
                    this.publicSeed = XMSSUtil.extractBytesAtOffset(bArr, i4, treeDigestSize);
                    int i5 = i4 + treeDigestSize;
                    this.root = XMSSUtil.extractBytesAtOffset(bArr, i5, treeDigestSize);
                    int i6 = i5 + treeDigestSize;
                    try {
                        this.bdsState = ((BDSStateMap) XMSSUtil.deserialize(XMSSUtil.extractBytesAtOffset(bArr, i6, bArr.length - i6), BDSStateMap.class)).withWOTSDigest(builder.xmss.getTreeDigestOID());
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

    public XMSSMTPrivateKeyParameters extractKeyShard(int i) {
        XMSSMTPrivateKeyParameters build;
        if (i >= 1) {
            synchronized (this) {
                long j = (long) i;
                if (j <= getUsagesRemaining()) {
                    build = new Builder(this.params).withSecretKeySeed(this.secretKeySeed).withSecretKeyPRF(this.secretKeyPRF).withPublicSeed(this.publicSeed).withRoot(this.root).withIndex(getIndex()).withBDSState(new BDSStateMap(this.bdsState, (getIndex() + j) - 1)).build();
                    for (int i2 = 0; i2 != i; i2++) {
                        rollKey();
                    }
                } else {
                    throw new IllegalArgumentException("usageCount exceeds usages remaining");
                }
            }
            return build;
        }
        throw new IllegalArgumentException("cannot ask for a shard with 0 keys");
    }

    /* access modifiers changed from: package-private */
    public BDSStateMap getBDSState() {
        return this.bdsState;
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        byte[] byteArray;
        synchronized (this) {
            byteArray = toByteArray();
        }
        return byteArray;
    }

    public long getIndex() {
        return this.index;
    }

    public XMSSMTPrivateKeyParameters getNextKey() {
        XMSSMTPrivateKeyParameters extractKeyShard;
        synchronized (this) {
            extractKeyShard = extractKeyShard(1);
        }
        return extractKeyShard;
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

    public long getUsagesRemaining() {
        long maxIndex;
        synchronized (this) {
            maxIndex = (this.bdsState.getMaxIndex() - getIndex()) + 1;
        }
        return maxIndex;
    }

    /* access modifiers changed from: package-private */
    public XMSSMTPrivateKeyParameters rollKey() {
        synchronized (this) {
            if (getIndex() < this.bdsState.getMaxIndex()) {
                this.bdsState.updateState(this.params, this.index, this.publicSeed, this.secretKeySeed);
                this.index++;
            } else {
                this.index = this.bdsState.getMaxIndex() + 1;
                this.bdsState = new BDSStateMap(this.bdsState.getMaxIndex());
            }
            this.used = false;
        }
        return this;
    }

    @Override // org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface
    public byte[] toByteArray() {
        byte[] concatenate;
        synchronized (this) {
            int treeDigestSize = this.params.getTreeDigestSize();
            int height = (this.params.getHeight() + 7) / 8;
            byte[] bArr = new byte[(height + treeDigestSize + treeDigestSize + treeDigestSize + treeDigestSize)];
            XMSSUtil.copyBytesAtOffset(bArr, XMSSUtil.toBytesBigEndian(this.index, height), 0);
            int i = height + 0;
            XMSSUtil.copyBytesAtOffset(bArr, this.secretKeySeed, i);
            int i2 = i + treeDigestSize;
            XMSSUtil.copyBytesAtOffset(bArr, this.secretKeyPRF, i2);
            int i3 = i2 + treeDigestSize;
            XMSSUtil.copyBytesAtOffset(bArr, this.publicSeed, i3);
            XMSSUtil.copyBytesAtOffset(bArr, this.root, i3 + treeDigestSize);
            try {
                concatenate = Arrays.concatenate(bArr, XMSSUtil.serialize(this.bdsState));
            } catch (IOException e) {
                throw new IllegalStateException("error serializing bds state: " + e.getMessage(), e);
            }
        }
        return concatenate;
    }
}
