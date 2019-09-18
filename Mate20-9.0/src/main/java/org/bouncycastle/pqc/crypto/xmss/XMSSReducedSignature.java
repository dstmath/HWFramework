package org.bouncycastle.pqc.crypto.xmss;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class XMSSReducedSignature implements XMSSStoreableObjectInterface {
    private final List<XMSSNode> authPath;
    private final XMSSParameters params;
    private final WOTSPlusSignature wotsPlusSignature;

    public static class Builder {
        /* access modifiers changed from: private */
        public List<XMSSNode> authPath = null;
        /* access modifiers changed from: private */
        public final XMSSParameters params;
        /* access modifiers changed from: private */
        public byte[] reducedSignature = null;
        /* access modifiers changed from: private */
        public WOTSPlusSignature wotsPlusSignature = null;

        public Builder(XMSSParameters xMSSParameters) {
            this.params = xMSSParameters;
        }

        public XMSSReducedSignature build() {
            return new XMSSReducedSignature(this);
        }

        public Builder withAuthPath(List<XMSSNode> list) {
            this.authPath = list;
            return this;
        }

        public Builder withReducedSignature(byte[] bArr) {
            this.reducedSignature = XMSSUtil.cloneArray(bArr);
            return this;
        }

        public Builder withWOTSPlusSignature(WOTSPlusSignature wOTSPlusSignature) {
            this.wotsPlusSignature = wOTSPlusSignature;
            return this;
        }
    }

    protected XMSSReducedSignature(Builder builder) {
        List<XMSSNode> list;
        this.params = builder.params;
        if (this.params != null) {
            int digestSize = this.params.getDigestSize();
            int len = this.params.getWOTSPlus().getParams().getLen();
            int height = this.params.getHeight();
            byte[] access$100 = builder.reducedSignature;
            if (access$100 != null) {
                if (access$100.length == (len * digestSize) + (height * digestSize)) {
                    byte[][] bArr = new byte[len][];
                    int i = 0;
                    for (int i2 = 0; i2 < bArr.length; i2++) {
                        bArr[i2] = XMSSUtil.extractBytesAtOffset(access$100, i, digestSize);
                        i += digestSize;
                    }
                    this.wotsPlusSignature = new WOTSPlusSignature(this.params.getWOTSPlus().getParams(), bArr);
                    list = new ArrayList<>();
                    for (int i3 = 0; i3 < height; i3++) {
                        list.add(new XMSSNode(i3, XMSSUtil.extractBytesAtOffset(access$100, i, digestSize)));
                        i += digestSize;
                    }
                } else {
                    throw new IllegalArgumentException("signature has wrong size");
                }
            } else {
                WOTSPlusSignature access$200 = builder.wotsPlusSignature;
                if (access$200 == null) {
                    access$200 = new WOTSPlusSignature(this.params.getWOTSPlus().getParams(), (byte[][]) Array.newInstance(byte.class, new int[]{len, digestSize}));
                }
                this.wotsPlusSignature = access$200;
                list = builder.authPath;
                if (list == null) {
                    list = new ArrayList<>();
                } else if (list.size() != height) {
                    throw new IllegalArgumentException("size of authPath needs to be equal to height of tree");
                }
            }
            this.authPath = list;
            return;
        }
        throw new NullPointerException("params == null");
    }

    public List<XMSSNode> getAuthPath() {
        return this.authPath;
    }

    public XMSSParameters getParams() {
        return this.params;
    }

    public WOTSPlusSignature getWOTSPlusSignature() {
        return this.wotsPlusSignature;
    }

    public byte[] toByteArray() {
        int digestSize = this.params.getDigestSize();
        byte[] bArr = new byte[((this.params.getWOTSPlus().getParams().getLen() * digestSize) + (this.params.getHeight() * digestSize))];
        byte[][] byteArray = this.wotsPlusSignature.toByteArray();
        int i = 0;
        for (byte[] copyBytesAtOffset : byteArray) {
            XMSSUtil.copyBytesAtOffset(bArr, copyBytesAtOffset, i);
            i += digestSize;
        }
        for (int i2 = 0; i2 < this.authPath.size(); i2++) {
            XMSSUtil.copyBytesAtOffset(bArr, this.authPath.get(i2).getValue(), i);
            i += digestSize;
        }
        return bArr;
    }
}
