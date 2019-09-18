package huawei.android.security.securityprofile;

import android.os.Parcel;
import android.os.Parcelable;

public class ApkDigest implements Parcelable {
    public static final Parcelable.Creator<ApkDigest> CREATOR = new Parcelable.Creator<ApkDigest>() {
        public ApkDigest createFromParcel(Parcel in) {
            return new ApkDigest(in);
        }

        public ApkDigest[] newArray(int size) {
            return new ApkDigest[size];
        }
    };
    public String apkSignatureScheme;
    public String base64Digest;
    public String digestAlgorithm;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.apkSignatureScheme);
        out.writeString(this.digestAlgorithm);
        out.writeString(this.base64Digest);
    }

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
}
