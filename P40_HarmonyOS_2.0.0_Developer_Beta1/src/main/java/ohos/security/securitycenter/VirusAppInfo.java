package ohos.security.securitycenter;

import java.util.Arrays;

public class VirusAppInfo {
    public static final int CATEGORY_RISK = 1;
    public static final int CATEGORY_UNKNOWN = -1;
    public static final int CATEGORY_VIRUS = 2;
    private final int mCategory;
    private final String mPackageName;
    private final byte[] mSha256;

    public VirusAppInfo(int i, String str, byte[] bArr) {
        this.mCategory = i;
        this.mPackageName = str;
        if (bArr == null) {
            this.mSha256 = new byte[0];
            return;
        }
        this.mSha256 = new byte[bArr.length];
        System.arraycopy(bArr, 0, this.mSha256, 0, bArr.length);
    }

    public int getCategory() {
        return this.mCategory;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public byte[] getSha256() {
        byte[] bArr = this.mSha256;
        byte[] bArr2 = new byte[bArr.length];
        System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
        return bArr2;
    }

    public String toString() {
        return "VirusAppInfo{Category=" + this.mCategory + ", PackageName='" + this.mPackageName + "', Sha256=" + Arrays.toString(this.mSha256) + '}';
    }
}
