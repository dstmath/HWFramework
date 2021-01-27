package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature;
import org.bouncycastle.util.Encodable;
import org.bouncycastle.util.Pack;

public final class XMSSSignature extends XMSSReducedSignature implements XMSSStoreableObjectInterface, Encodable {
    private final int index;
    private final byte[] random;

    public static class Builder extends XMSSReducedSignature.Builder {
        private int index = 0;
        private final XMSSParameters params;
        private byte[] random = null;

        public Builder(XMSSParameters xMSSParameters) {
            super(xMSSParameters);
            this.params = xMSSParameters;
        }

        @Override // org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature.Builder
        public XMSSSignature build() {
            return new XMSSSignature(this);
        }

        public Builder withIndex(int i) {
            this.index = i;
            return this;
        }

        public Builder withRandom(byte[] bArr) {
            this.random = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withSignature(byte[] bArr) {
            if (bArr != null) {
                int treeDigestSize = this.params.getTreeDigestSize();
                int len = this.params.getWOTSPlus().getParams().getLen();
                this.index = Pack.bigEndianToInt(bArr, 0);
                this.random = XMSSUtil.extractBytesAtOffset(bArr, 4, treeDigestSize);
                withReducedSignature(XMSSUtil.extractBytesAtOffset(bArr, 4 + treeDigestSize, (len * treeDigestSize) + (this.params.getHeight() * treeDigestSize)));
                return this;
            }
            throw new NullPointerException("signature == null");
        }
    }

    private XMSSSignature(Builder builder) {
        super(builder);
        this.index = builder.index;
        int treeDigestSize = getParams().getTreeDigestSize();
        byte[] bArr = builder.random;
        if (bArr == null) {
            bArr = new byte[treeDigestSize];
        } else if (bArr.length != treeDigestSize) {
            throw new IllegalArgumentException("size of random needs to be equal to size of digest");
        }
        this.random = bArr;
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return toByteArray();
    }

    public int getIndex() {
        return this.index;
    }

    public byte[] getRandom() {
        return XMSSUtil.cloneArray(this.random);
    }

    @Override // org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature, org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface
    public byte[] toByteArray() {
        byte[][] byteArray;
        int treeDigestSize = getParams().getTreeDigestSize();
        byte[] bArr = new byte[(treeDigestSize + 4 + (getParams().getWOTSPlus().getParams().getLen() * treeDigestSize) + (getParams().getHeight() * treeDigestSize))];
        Pack.intToBigEndian(this.index, bArr, 0);
        XMSSUtil.copyBytesAtOffset(bArr, this.random, 4);
        int i = 4 + treeDigestSize;
        for (byte[] bArr2 : getWOTSPlusSignature().toByteArray()) {
            XMSSUtil.copyBytesAtOffset(bArr, bArr2, i);
            i += treeDigestSize;
        }
        for (int i2 = 0; i2 < getAuthPath().size(); i2++) {
            XMSSUtil.copyBytesAtOffset(bArr, getAuthPath().get(i2).getValue(), i);
            i += treeDigestSize;
        }
        return bArr;
    }
}
