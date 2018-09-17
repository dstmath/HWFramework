package com.huawei.android.pushagent.datatype.b;

public class b {
    private byte[] ha;

    public b(byte[] bArr) {
        if (bArr != null && bArr.length > 0) {
            this.ha = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.ha, 0, bArr.length);
        }
    }

    public byte[] wx(int i, int i2) {
        if (i > this.ha.length || i + i2 > this.ha.length) {
            throw new IndexOutOfBoundsException("out of index");
        }
        byte[] bArr = new byte[i2];
        System.arraycopy(this.ha, i, bArr, 0, i2);
        byte[] bArr2 = new byte[(this.ha.length - i2)];
        System.arraycopy(this.ha, 0, bArr2, 0, i);
        System.arraycopy(this.ha, i + i2, bArr2, i, (this.ha.length - i) - i2);
        this.ha = bArr2;
        return bArr;
    }

    public byte[] ww(int i) {
        if (i == 0) {
            return new byte[0];
        }
        return wx(0, i);
    }
}
