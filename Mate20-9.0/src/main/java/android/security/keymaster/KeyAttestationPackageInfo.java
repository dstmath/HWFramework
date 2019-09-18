package android.security.keymaster;

import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;

public class KeyAttestationPackageInfo implements Parcelable {
    public static final Parcelable.Creator<KeyAttestationPackageInfo> CREATOR = new Parcelable.Creator<KeyAttestationPackageInfo>() {
        public KeyAttestationPackageInfo createFromParcel(Parcel source) {
            return new KeyAttestationPackageInfo(source);
        }

        public KeyAttestationPackageInfo[] newArray(int size) {
            return new KeyAttestationPackageInfo[size];
        }
    };
    private final String mPackageName;
    private final Signature[] mPackageSignatures;
    private final long mPackageVersionCode;

    public KeyAttestationPackageInfo(String mPackageName2, long mPackageVersionCode2, Signature[] mPackageSignatures2) {
        this.mPackageName = mPackageName2;
        this.mPackageVersionCode = mPackageVersionCode2;
        this.mPackageSignatures = mPackageSignatures2;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public long getPackageVersionCode() {
        return this.mPackageVersionCode;
    }

    public Signature[] getPackageSignatures() {
        return this.mPackageSignatures;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackageName);
        dest.writeLong(this.mPackageVersionCode);
        dest.writeTypedArray(this.mPackageSignatures, flags);
    }

    private KeyAttestationPackageInfo(Parcel source) {
        this.mPackageName = source.readString();
        this.mPackageVersionCode = source.readLong();
        this.mPackageSignatures = (Signature[]) source.createTypedArray(Signature.CREATOR);
    }
}
