package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.pqc.crypto.xmss.XMSSAddress;
import org.bouncycastle.util.Pack;

final class HashTreeAddress extends XMSSAddress {
    private static final int PADDING = 0;
    private static final int TYPE = 2;
    private final int padding;
    private final int treeHeight;
    private final int treeIndex;

    protected static class Builder extends XMSSAddress.Builder<Builder> {
        /* access modifiers changed from: private */
        public int treeHeight = 0;
        /* access modifiers changed from: private */
        public int treeIndex = 0;

        protected Builder() {
            super(2);
        }

        /* access modifiers changed from: protected */
        public XMSSAddress build() {
            return new HashTreeAddress(this);
        }

        /* access modifiers changed from: protected */
        public Builder getThis() {
            return this;
        }

        /* access modifiers changed from: protected */
        public Builder withTreeHeight(int i) {
            this.treeHeight = i;
            return this;
        }

        /* access modifiers changed from: protected */
        public Builder withTreeIndex(int i) {
            this.treeIndex = i;
            return this;
        }
    }

    private HashTreeAddress(Builder builder) {
        super(builder);
        this.padding = 0;
        this.treeHeight = builder.treeHeight;
        this.treeIndex = builder.treeIndex;
    }

    /* access modifiers changed from: protected */
    public int getPadding() {
        return this.padding;
    }

    /* access modifiers changed from: protected */
    public int getTreeHeight() {
        return this.treeHeight;
    }

    /* access modifiers changed from: protected */
    public int getTreeIndex() {
        return this.treeIndex;
    }

    /* access modifiers changed from: protected */
    public byte[] toByteArray() {
        byte[] byteArray = super.toByteArray();
        Pack.intToBigEndian(this.padding, byteArray, 16);
        Pack.intToBigEndian(this.treeHeight, byteArray, 20);
        Pack.intToBigEndian(this.treeIndex, byteArray, 24);
        return byteArray;
    }
}
