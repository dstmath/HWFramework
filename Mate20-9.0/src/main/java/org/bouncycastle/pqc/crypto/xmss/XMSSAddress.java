package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.util.Pack;

public abstract class XMSSAddress {
    private final int keyAndMask;
    private final int layerAddress;
    private final long treeAddress;
    private final int type;

    protected static abstract class Builder<T extends Builder> {
        /* access modifiers changed from: private */
        public int keyAndMask = 0;
        /* access modifiers changed from: private */
        public int layerAddress = 0;
        /* access modifiers changed from: private */
        public long treeAddress = 0;
        /* access modifiers changed from: private */
        public final int type;

        protected Builder(int i) {
            this.type = i;
        }

        /* access modifiers changed from: protected */
        public abstract XMSSAddress build();

        /* access modifiers changed from: protected */
        public abstract T getThis();

        /* access modifiers changed from: protected */
        public T withKeyAndMask(int i) {
            this.keyAndMask = i;
            return getThis();
        }

        /* access modifiers changed from: protected */
        public T withLayerAddress(int i) {
            this.layerAddress = i;
            return getThis();
        }

        /* access modifiers changed from: protected */
        public T withTreeAddress(long j) {
            this.treeAddress = j;
            return getThis();
        }
    }

    protected XMSSAddress(Builder builder) {
        this.layerAddress = builder.layerAddress;
        this.treeAddress = builder.treeAddress;
        this.type = builder.type;
        this.keyAndMask = builder.keyAndMask;
    }

    public final int getKeyAndMask() {
        return this.keyAndMask;
    }

    /* access modifiers changed from: protected */
    public final int getLayerAddress() {
        return this.layerAddress;
    }

    /* access modifiers changed from: protected */
    public final long getTreeAddress() {
        return this.treeAddress;
    }

    public final int getType() {
        return this.type;
    }

    /* access modifiers changed from: protected */
    public byte[] toByteArray() {
        byte[] bArr = new byte[32];
        Pack.intToBigEndian(this.layerAddress, bArr, 0);
        Pack.longToBigEndian(this.treeAddress, bArr, 4);
        Pack.intToBigEndian(this.type, bArr, 12);
        Pack.intToBigEndian(this.keyAndMask, bArr, 28);
        return bArr;
    }
}
