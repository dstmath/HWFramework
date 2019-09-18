package org.bouncycastle.pqc.crypto.xmss;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature;

public final class XMSSMTSignature implements XMSSStoreableObjectInterface {
    private final long index;
    private final XMSSMTParameters params;
    private final byte[] random;
    private final List<XMSSReducedSignature> reducedSignatures;

    public static class Builder {
        /* access modifiers changed from: private */
        public long index = 0;
        /* access modifiers changed from: private */
        public final XMSSMTParameters params;
        /* access modifiers changed from: private */
        public byte[] random = null;
        /* access modifiers changed from: private */
        public List<XMSSReducedSignature> reducedSignatures = null;
        /* access modifiers changed from: private */
        public byte[] signature = null;

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
            this.signature = bArr;
            return this;
        }
    }

    private XMSSMTSignature(Builder builder) {
        this.params = builder.params;
        if (this.params != null) {
            int digestSize = this.params.getDigestSize();
            byte[] access$100 = builder.signature;
            if (access$100 != null) {
                int len = this.params.getWOTSPlus().getParams().getLen();
                int ceil = (int) Math.ceil(((double) this.params.getHeight()) / 8.0d);
                int height = ((this.params.getHeight() / this.params.getLayers()) + len) * digestSize;
                if (access$100.length == ceil + digestSize + (this.params.getLayers() * height)) {
                    this.index = XMSSUtil.bytesToXBigEndian(access$100, 0, ceil);
                    if (XMSSUtil.isIndexValid(this.params.getHeight(), this.index)) {
                        int i = 0 + ceil;
                        this.random = XMSSUtil.extractBytesAtOffset(access$100, i, digestSize);
                        this.reducedSignatures = new ArrayList();
                        for (int i2 = i + digestSize; i2 < access$100.length; i2 += height) {
                            this.reducedSignatures.add(new XMSSReducedSignature.Builder(this.params.getXMSSParameters()).withReducedSignature(XMSSUtil.extractBytesAtOffset(access$100, i2, height)).build());
                        }
                        return;
                    }
                    throw new IllegalArgumentException("index out of bounds");
                }
                throw new IllegalArgumentException("signature has wrong size");
            }
            this.index = builder.index;
            byte[] access$300 = builder.random;
            if (access$300 == null) {
                this.random = new byte[digestSize];
            } else if (access$300.length == digestSize) {
                this.random = access$300;
            } else {
                throw new IllegalArgumentException("size of random needs to be equal to size of digest");
            }
            this.reducedSignatures = builder.reducedSignatures == null ? new ArrayList<>() : builder.reducedSignatures;
            return;
        }
        throw new NullPointerException("params == null");
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

    public byte[] toByteArray() {
        int digestSize = this.params.getDigestSize();
        int len = this.params.getWOTSPlus().getParams().getLen();
        int ceil = (int) Math.ceil(((double) this.params.getHeight()) / 8.0d);
        int height = ((this.params.getHeight() / this.params.getLayers()) + len) * digestSize;
        byte[] bArr = new byte[(ceil + digestSize + (this.params.getLayers() * height))];
        XMSSUtil.copyBytesAtOffset(bArr, XMSSUtil.toBytesBigEndian(this.index, ceil), 0);
        int i = 0 + ceil;
        XMSSUtil.copyBytesAtOffset(bArr, this.random, i);
        int i2 = i + digestSize;
        for (XMSSReducedSignature byteArray : this.reducedSignatures) {
            XMSSUtil.copyBytesAtOffset(bArr, byteArray.toByteArray(), i2);
            i2 += height;
        }
        return bArr;
    }
}
