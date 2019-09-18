package org.bouncycastle.pqc.crypto.xmss;

final class WOTSPlusSignature {
    private byte[][] signature;

    protected WOTSPlusSignature(WOTSPlusParameters wOTSPlusParameters, byte[][] bArr) {
        if (wOTSPlusParameters == null) {
            throw new NullPointerException("params == null");
        } else if (bArr == null) {
            throw new NullPointerException("signature == null");
        } else if (XMSSUtil.hasNullPointer(bArr)) {
            throw new NullPointerException("signature byte array == null");
        } else if (bArr.length == wOTSPlusParameters.getLen()) {
            int i = 0;
            while (i < bArr.length) {
                if (bArr[i].length == wOTSPlusParameters.getDigestSize()) {
                    i++;
                } else {
                    throw new IllegalArgumentException("wrong signature format");
                }
            }
            this.signature = XMSSUtil.cloneArray(bArr);
        } else {
            throw new IllegalArgumentException("wrong signature size");
        }
    }

    public byte[][] toByteArray() {
        return XMSSUtil.cloneArray(this.signature);
    }
}
