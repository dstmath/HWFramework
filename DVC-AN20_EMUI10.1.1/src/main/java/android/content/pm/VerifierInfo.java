package android.content.pm;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import java.security.PublicKey;

public class VerifierInfo implements Parcelable {
    public static final Parcelable.Creator<VerifierInfo> CREATOR = new Parcelable.Creator<VerifierInfo>() {
        /* class android.content.pm.VerifierInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VerifierInfo createFromParcel(Parcel source) {
            return new VerifierInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public VerifierInfo[] newArray(int size) {
            return new VerifierInfo[size];
        }
    };
    public final String packageName;
    public final PublicKey publicKey;

    @UnsupportedAppUsage
    public VerifierInfo(String packageName2, PublicKey publicKey2) {
        if (packageName2 == null || packageName2.length() == 0) {
            throw new IllegalArgumentException("packageName must not be null or empty");
        } else if (publicKey2 != null) {
            this.packageName = packageName2;
            this.publicKey = publicKey2;
        } else {
            throw new IllegalArgumentException("publicKey must not be null");
        }
    }

    private VerifierInfo(Parcel source) {
        this.packageName = source.readString();
        this.publicKey = (PublicKey) source.readSerializable();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeSerializable(this.publicKey);
    }
}
