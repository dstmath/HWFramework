package defpackage;

/* renamed from: m */
public class m {
    private int H;
    private String I;
    private byte[] mMsgData;
    private String mPackageName;
    private byte[] mToken;

    public m(String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        if (bArr != null && bArr2 != null) {
            this.mPackageName = str;
            this.mToken = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.mToken, 0, bArr.length);
            this.mMsgData = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.mMsgData, 0, bArr2.length);
            this.H = i;
            this.I = str2;
        }
    }

    public byte[] aC() {
        return this.mToken;
    }

    public byte[] aD() {
        return this.mMsgData;
    }

    public int aE() {
        return this.H;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String k() {
        return this.I;
    }
}
