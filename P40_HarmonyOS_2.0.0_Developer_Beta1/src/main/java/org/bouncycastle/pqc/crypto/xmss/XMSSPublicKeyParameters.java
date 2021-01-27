package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.io.PrintStream;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.Pack;

public final class XMSSPublicKeyParameters extends XMSSKeyParameters implements XMSSStoreableObjectInterface, Encodable {
    private final int oid;
    private final XMSSParameters params;
    private final byte[] publicSeed;
    private final byte[] root;

    public static class Builder {
        private final XMSSParameters params;
        private byte[] publicKey = null;
        private byte[] publicSeed = null;
        private byte[] root = null;

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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    private XMSSPublicKeyParameters(Builder builder) {
        super(false, builder.params.getTreeDigest());
        byte[] bArr;
        int i;
        int i2 = 0;
        this.params = builder.params;
        XMSSParameters xMSSParameters = this.params;
        if (xMSSParameters != null) {
            int treeDigestSize = xMSSParameters.getTreeDigestSize();
            byte[] bArr2 = builder.publicKey;
            if (bArr2 != null) {
                if (bArr2.length == treeDigestSize + treeDigestSize) {
                    this.oid = 0;
                    this.root = XMSSUtil.extractBytesAtOffset(bArr2, 0, treeDigestSize);
                    i = treeDigestSize + 0;
                } else {
                    int i3 = treeDigestSize + 4 + treeDigestSize;
                    if (bArr2.length == i3) {
                        this.oid = Pack.bigEndianToInt(bArr2, 0);
                        this.root = XMSSUtil.extractBytesAtOffset(bArr2, 4, treeDigestSize);
                        i = 4 + treeDigestSize;
                    } else {
                        PrintStream printStream = System.err;
                        printStream.println(bArr2.length + " " + i3);
                        throw new IllegalArgumentException("public key has wrong size");
                    }
                }
                bArr = XMSSUtil.extractBytesAtOffset(bArr2, i, treeDigestSize);
            } else {
                this.oid = this.params.getOid() != null ? this.params.getOid().getOid() : i2;
                byte[] bArr3 = builder.root;
                if (bArr3 == null) {
                    bArr3 = new byte[treeDigestSize];
                } else if (bArr3.length != treeDigestSize) {
                    throw new IllegalArgumentException("length of root must be equal to length of digest");
                }
                this.root = bArr3;
                bArr = builder.publicSeed;
                if (bArr == null) {
                    bArr = new byte[treeDigestSize];
                } else if (bArr.length != treeDigestSize) {
                    throw new IllegalArgumentException("length of publicSeed must be equal to length of digest");
                }
            }
            this.publicSeed = bArr;
            return;
        }
        throw new NullPointerException("params == null");
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return toByteArray();
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

    @Override // org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface
    public byte[] toByteArray() {
        byte[] bArr;
        int treeDigestSize = this.params.getTreeDigestSize();
        int i = this.oid;
        int i2 = 0;
        if (i != 0) {
            bArr = new byte[(treeDigestSize + 4 + treeDigestSize)];
            Pack.intToBigEndian(i, bArr, 0);
            i2 = 4;
        } else {
            bArr = new byte[(treeDigestSize + treeDigestSize)];
        }
        XMSSUtil.copyBytesAtOffset(bArr, this.root, i2);
        XMSSUtil.copyBytesAtOffset(bArr, this.publicSeed, i2 + treeDigestSize);
        return bArr;
    }
}
