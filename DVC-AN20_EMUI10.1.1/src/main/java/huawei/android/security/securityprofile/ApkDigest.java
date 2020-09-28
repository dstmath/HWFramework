package huawei.android.security.securityprofile;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ApkDigest implements Parcelable {
    public static final Parcelable.Creator<ApkDigest> CREATOR = new Parcelable.Creator<ApkDigest>() {
        /* class huawei.android.security.securityprofile.ApkDigest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApkDigest createFromParcel(Parcel in) {
            return new ApkDigest(in);
        }

        @Override // android.os.Parcelable.Creator
        public ApkDigest[] newArray(int size) {
            return new ApkDigest[size];
        }
    };
    public String apkSignatureScheme;
    public String base64Digest;
    public String digestAlgorithm;

    private ApkDigest(Parcel in) {
        this.apkSignatureScheme = in.readString();
        this.digestAlgorithm = in.readString();
        this.base64Digest = in.readString();
    }

    public ApkDigest(String apkSignatureScheme2, String digestAlgorithm2, String base64Digest2) {
        this.apkSignatureScheme = apkSignatureScheme2;
        this.digestAlgorithm = digestAlgorithm2;
        this.base64Digest = base64Digest2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.apkSignatureScheme);
        out.writeString(this.digestAlgorithm);
        out.writeString(this.base64Digest);
    }

    public String toString() {
        return "base64Digest: " + this.base64Digest + System.lineSeparator() + "digestAlgorithm: " + this.digestAlgorithm + System.lineSeparator() + "apkSignatureScheme: " + this.apkSignatureScheme;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(this.base64Digest) && !TextUtils.isEmpty(this.apkSignatureScheme) && !TextUtils.isEmpty(this.digestAlgorithm);
    }
}
