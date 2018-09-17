package com.huawei.android.pushagent.datatype.b;

public class a {
    private byte[] gv;
    private String gw;
    private int gx;
    private String gy;
    private byte[] gz;

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public a(String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        if (bArr != null && bArr.length != 0 && bArr2 != null && bArr2.length != 0) {
            this.gy = str;
            this.gz = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.gz, 0, bArr.length);
            this.gv = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.gv, 0, bArr2.length);
            this.gx = i;
            this.gw = str2;
        }
    }

    public String wt() {
        return this.gy;
    }

    public byte[] getToken() {
        return this.gz;
    }

    public byte[] ws() {
        return this.gv;
    }

    public int wu() {
        return this.gx;
    }

    public String wv() {
        return this.gw;
    }
}
