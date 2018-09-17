package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.security.PublicKey;

public class VerifierInfo implements Parcelable {
    public static final Creator<VerifierInfo> CREATOR = new Creator<VerifierInfo>() {
        public VerifierInfo createFromParcel(Parcel source) {
            return new VerifierInfo(source, null);
        }

        public VerifierInfo[] newArray(int size) {
            return new VerifierInfo[size];
        }
    };
    public final String packageName;
    public final PublicKey publicKey;

    public VerifierInfo(String packageName, PublicKey publicKey) {
        if (packageName == null || packageName.length() == 0) {
            throw new IllegalArgumentException("packageName must not be null or empty");
        } else if (publicKey == null) {
            throw new IllegalArgumentException("publicKey must not be null");
        } else {
            this.packageName = packageName;
            this.publicKey = publicKey;
        }
    }

    private VerifierInfo(Parcel source) {
        this.packageName = source.readString();
        this.publicKey = (PublicKey) source.readSerializable();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeSerializable(this.publicKey);
    }
}
