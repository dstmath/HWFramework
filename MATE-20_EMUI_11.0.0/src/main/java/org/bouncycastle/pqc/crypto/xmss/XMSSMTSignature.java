package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Encodable;

public final class XMSSMTSignature implements XMSSStoreableObjectInterface, Encodable {
    private final long index;
    private final XMSSMTParameters params;
    private final byte[] random;
    private final List<XMSSReducedSignature> reducedSignatures;

    public static class Builder {
        private long index = 0;
        private final XMSSMTParameters params;
        private byte[] random = null;
        private List<XMSSReducedSignature> reducedSignatures = null;
        private byte[] signature = null;

        public Builder(XMSSMTParameters xMSSMTParameters) {
            this.params = xMSSMTParameters;
        }

        public XMSSMTSignature build() {
            return new XMSSMTSignature(this);
        }

        public Builder withIndex(long j) {
            this.index = j;
            return this;
        }

        public Builder withRandom(byte[] bArr) {
            this.random = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withReducedSignatures(List<XMSSReducedSignature> list) {
            this.reducedSignatures = list;
            return this;
        }

        public Builder withSignature(byte[] bArr) {
            this.signature = Arrays.clone(bArr);
            return this;
        }
    }

    private XMSSMTSignature(Builder builder) {
        this.params = builder.params;
        XMSSMTParameters xMSSMTParameters = this.params;
        if (xMSSMTParameters != null) {
            int treeDigestSize = xMSSMTParameters.getTreeDigestSize();
            byte[] bArr = builder.signature;
            if (bArr != null) {
                int len = this.params.getWOTSPlus().getParams().getLen();
                int ceil = (int) Math.ceil(((double) this.params.getHeight()) / 8.0d);
                int height = ((this.params.getHeight() / this.params.getLayers()) + len) * treeDigestSize;
                if (bArr.length == ceil + treeDigestSize + (this.params.getLayers() * height)) {
                    this.index = XMSSUtil.bytesToXBigEndian(bArr, 0, ceil);
                    if (XMSSUtil.isIndexValid(this.params.getHeight(), this.index)) {
                        int i = ceil + 0;
                        this.random = XMSSUtil.extractBytesAtOffset(bArr, i, treeDigestSize);
                        this.reducedSignatures = new ArrayList();
                        for (int i2 = i + treeDigestSize; i2 < bArr.length; i2 += height) {
                            this.reducedSignatures.add(new XMSSReducedSignature.Builder(this.params.getXMSSParameters()).withReducedSignature(XMSSUtil.extractBytesAtOffset(bArr, i2, height)).build());
                        }
                        return;
                    }
                    throw new IllegalArgumentException("index out of bounds");
                }
                throw new IllegalArgumentException("signature has wrong size");
            }
            this.index = builder.index;
            byte[] bArr2 = builder.random;
            if (bArr2 == null) {
                this.random = new byte[treeDigestSize];
            } else if (bArr2.length == treeDigestSize) {
                this.random = bArr2;
            } else {
                throw new IllegalArgumentException("size of random needs to be equal to size of digest");
            }
            List<XMSSReducedSignature> list = builder.reducedSignatures;
            this.reducedSignatures = list == null ? new ArrayList<>() : list;
            return;
        }
        throw new NullPointerException("params == null");
    }

    @Override // org.bouncycastle.util.Encodable
    public byte[] getEncoded() throws IOException {
        return toByteArray();
    }

    public long getIndex() {
        return this.index;
    }

    public byte[] getRandom() {
        return XMSSUtil.cloneArray(this.random);
    }

    public List<XMSSReducedSignature> getReducedSignatures() {
        return this.reducedSignatures;
    }

    @Override // org.bouncycastle.pqc.crypto.xmss.XMSSStoreableObjectInterface
    public byte[] toByteArray() {
        int treeDigestSize = this.params.getTreeDigestSize();
        int len = this.params.getWOTSPlus().getParams().getLen();
        int ceil = (int) Math.ceil(((double) this.params.getHeight()) / 8.0d);
        int height = ((this.params.getHeight() / this.params.getLayers()) + len) * treeDigestSize;
        byte[] bArr = new byte[(ceil + treeDigestSize + (this.params.getLayers() * height))];
        XMSSUtil.copyBytesAtOffset(bArr, XMSSUtil.toBytesBigEndian(this.index, ceil), 0);
        int i = ceil + 0;
        XMSSUtil.copyBytesAtOffset(bArr, this.random, i);
        int i2 = i + treeDigestSize;
        for (XMSSReducedSignature xMSSReducedSignature : this.reducedSignatures) {
            XMSSUtil.copyBytesAtOffset(bArr, xMSSReducedSignature.toByteArray(), i2);
            i2 += height;
        }
        return bArr;
    }
}
