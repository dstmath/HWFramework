package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public final class XMSSPublicKeyParameters extends AsymmetricKeyParameter implements XMSSStoreableObjectInterface {
    private final XMSSParameters params;
    private final byte[] publicSeed;
    private final byte[] root;

    public static class Builder {
        /* access modifiers changed from: private */
        public final XMSSParameters params;
        /* access modifiers changed from: private */
        public byte[] publicKey = null;
        /* access modifiers changed from: private */
        public byte[] publicSeed = null;
        /* access modifiers changed from: private */
        public byte[] root = null;

        public Builder(XMSSParameters xMSSParameters) {
            this.params = xMSSParameters;
        }

        public XMSSPublicKeyParameters build() {
            return new XMSSPublicKeyParameters(this);
        }

        public Builder withPublicKey(byte[] bArr) {
            this.publicKey = XMSSUtil.cloneArray(bArr);
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
    }

    private XMSSPublicKeyParameters(Builder builder) {
        super(false);
        byte[] access$300;
        this.params = builder.params;
        if (this.params != null) {
            int digestSize = this.params.getDigestSize();
            byte[] access$100 = builder.publicKey;
            if (access$100 != null) {
                if (access$100.length == digestSize + digestSize) {
                    this.root = XMSSUtil.extractBytesAtOffset(access$100, 0, digestSize);
                    access$300 = XMSSUtil.extractBytesAtOffset(access$100, 0 + digestSize, digestSize);
                } else {
                    throw new IllegalArgumentException("public key has wrong size");
                }
            } else {
                byte[] access$200 = builder.root;
                if (access$200 == null) {
                    access$200 = new byte[digestSize];
                } else if (access$200.length != digestSize) {
                    throw new IllegalArgumentException("length of root must be equal to length of digest");
                }
                this.root = access$200;
                access$300 = builder.publicSeed;
                if (access$300 == null) {
                    access$300 = new byte[digestSize];
                } else if (access$300.length != digestSize) {
                    throw new IllegalArgumentException("length of publicSeed must be equal to length of digest");
                }
            }
            this.publicSeed = access$300;
            return;
        }
        throw new NullPointerException("params == null");
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

    public byte[] toByteArray() {
        int digestSize = this.params.getDigestSize();
        byte[] bArr = new byte[(digestSize + digestSize)];
        XMSSUtil.copyBytesAtOffset(bArr, this.root, 0);
        XMSSUtil.copyBytesAtOffset(bArr, this.publicSeed, 0 + digestSize);
        return bArr;
    }
}
