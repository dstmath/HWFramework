package org.bouncycastle.pqc.crypto.xmss;

final class WOTSPlusPublicKeyParameters {
    private final byte[][] publicKey;

    protected WOTSPlusPublicKeyParameters(WOTSPlusParameters wOTSPlusParameters, byte[][] bArr) {
        if (wOTSPlusParameters == null) {
            throw new NullPointerException("params == null");
        } else if (bArr == null) {
            throw new NullPointerException("publicKey == null");
        } else if (XMSSUtil.hasNullPointer(bArr)) {
            throw new NullPointerException("publicKey byte array == null");
        } else if (bArr.length == wOTSPlusParameters.getLen()) {
            int i = 0;
            while (i < bArr.length) {
                if (bArr[i].length == wOTSPlusParameters.getDigestSize()) {
                    i++;
                } else {
                    throw new IllegalArgumentException("wrong publicKey format");
                }
            }
            this.publicKey = XMSSUtil.cloneArray(bArr);
        } else {
            throw new IllegalArgumentException("wrong publicKey size");
        }
    }

    /* access modifiers changed from: protected */
    public byte[][] toByteArray() {
        return XMSSUtil.cloneArray(this.publicKey);
    }
}
