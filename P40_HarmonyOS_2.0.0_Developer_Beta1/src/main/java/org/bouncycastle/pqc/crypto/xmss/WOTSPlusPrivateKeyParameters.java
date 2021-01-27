package org.bouncycastle.pqc.crypto.xmss;

final class WOTSPlusPrivateKeyParameters {
    private final byte[][] privateKey;

    protected WOTSPlusPrivateKeyParameters(WOTSPlusParameters wOTSPlusParameters, byte[][] bArr) {
        if (wOTSPlusParameters == null) {
            throw new NullPointerException("params == null");
        } else if (bArr == null) {
            throw new NullPointerException("privateKey == null");
        } else if (XMSSUtil.hasNullPointer(bArr)) {
            throw new NullPointerException("privateKey byte array == null");
        } else if (bArr.length == wOTSPlusParameters.getLen()) {
            for (byte[] bArr2 : bArr) {
                if (bArr2.length != wOTSPlusParameters.getTreeDigestSize()) {
                    throw new IllegalArgumentException("wrong privateKey format");
                }
            }
            this.privateKey = XMSSUtil.cloneArray(bArr);
        } else {
            throw new IllegalArgumentException("wrong privateKey format");
        }
    }

    /* access modifiers changed from: protected */
    public byte[][] toByteArray() {
        return XMSSUtil.cloneArray(this.privateKey);
    }
}
