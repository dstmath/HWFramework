package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.pqc.crypto.xmss.XMSSAddress;
import org.bouncycastle.util.Pack;

final class OTSHashAddress extends XMSSAddress {
    private static final int TYPE = 0;
    private final int chainAddress;
    private final int hashAddress;
    private final int otsAddress;

    protected static class Builder extends XMSSAddress.Builder<Builder> {
        /* access modifiers changed from: private */
        public int chainAddress = 0;
        /* access modifiers changed from: private */
        public int hashAddress = 0;
        /* access modifiers changed from: private */
        public int otsAddress = 0;

        protected Builder() {
            super(0);
        }

        /* access modifiers changed from: protected */
        public XMSSAddress build() {
            return new OTSHashAddress(this);
        }

        /* access modifiers changed from: protected */
        public Builder getThis() {
            return this;
        }

        /* access modifiers changed from: protected */
        public Builder withChainAddress(int i) {
            this.chainAddress = i;
            return this;
        }

        /* access modifiers changed from: protected */
        public Builder withHashAddress(int i) {
            this.hashAddress = i;
            return this;
        }

        /* access modifiers changed from: protected */
        public Builder withOTSAddress(int i) {
            this.otsAddress = i;
            return this;
        }
    }

    private OTSHashAddress(Builder builder) {
        super(builder);
        this.otsAddress = builder.otsAddress;
        this.chainAddress = builder.chainAddress;
        this.hashAddress = builder.hashAddress;
    }

    /* access modifiers changed from: protected */
    public int getChainAddress() {
        return this.chainAddress;
    }

    /* access modifiers changed from: protected */
    public int getHashAddress() {
        return this.hashAddress;
    }

    /* access modifiers changed from: protected */
    public int getOTSAddress() {
        return this.otsAddress;
    }

    /* access modifiers changed from: protected */
    public byte[] toByteArray() {
        byte[] byteArray = super.toByteArray();
        Pack.intToBigEndian(this.otsAddress, byteArray, 16);
        Pack.intToBigEndian(this.chainAddress, byteArray, 20);
        Pack.intToBigEndian(this.hashAddress, byteArray, 24);
        return byteArray;
    }
}
