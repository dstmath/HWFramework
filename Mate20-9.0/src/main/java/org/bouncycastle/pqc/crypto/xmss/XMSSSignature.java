package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature;
import org.bouncycastle.util.Pack;

public final class XMSSSignature extends XMSSReducedSignature implements XMSSStoreableObjectInterface {
    private final int index;
    private final byte[] random;

    public static class Builder extends XMSSReducedSignature.Builder {
        /* access modifiers changed from: private */
        public int index = 0;
        private final XMSSParameters params;
        /* access modifiers changed from: private */
        public byte[] random = null;

        public Builder(XMSSParameters xMSSParameters) {
            super(xMSSParameters);
            this.params = xMSSParameters;
        }

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
                int digestSize = this.params.getDigestSize();
                int len = this.params.getWOTSPlus().getParams().getLen();
                this.index = Pack.bigEndianToInt(bArr, 0);
                this.random = XMSSUtil.extractBytesAtOffset(bArr, 4, digestSize);
                withReducedSignature(XMSSUtil.extractBytesAtOffset(bArr, 4 + digestSize, (len * digestSize) + (this.params.getHeight() * digestSize)));
                return this;
            }
            throw new NullPointerException("signature == null");
        }
    }

    private XMSSSignature(Builder builder) {
        super(builder);
        this.index = builder.index;
        int digestSize = getParams().getDigestSize();
        byte[] access$100 = builder.random;
        if (access$100 == null) {
            access$100 = new byte[digestSize];
        } else if (access$100.length != digestSize) {
            throw new IllegalArgumentException("size of random needs to be equal to size of digest");
        }
        this.random = access$100;
    }

    public int getIndex() {
        return this.index;
    }

    public byte[] getRandom() {
        return XMSSUtil.cloneArray(this.random);
    }

    public byte[] toByteArray() {
        int digestSize = getParams().getDigestSize();
        byte[] bArr = new byte[(4 + digestSize + (getParams().getWOTSPlus().getParams().getLen() * digestSize) + (getParams().getHeight() * digestSize))];
        Pack.intToBigEndian(this.index, bArr, 0);
        XMSSUtil.copyBytesAtOffset(bArr, this.random, 4);
        byte[][] byteArray = getWOTSPlusSignature().toByteArray();
        int i = 4 + digestSize;
        for (byte[] copyBytesAtOffset : byteArray) {
            XMSSUtil.copyBytesAtOffset(bArr, copyBytesAtOffset, i);
            i += digestSize;
        }
        for (int i2 = 0; i2 < getAuthPath().size(); i2++) {
            XMSSUtil.copyBytesAtOffset(bArr, getAuthPath().get(i2).getValue(), i);
            i += digestSize;
        }
        return bArr;
    }
}
