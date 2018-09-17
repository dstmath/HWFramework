package android.security.keymaster;

import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeyAttestationPackageInfo implements Parcelable {
    public static final Creator<KeyAttestationPackageInfo> CREATOR = new Creator<KeyAttestationPackageInfo>() {
        public KeyAttestationPackageInfo createFromParcel(Parcel source) {
            return new KeyAttestationPackageInfo(source, null);
        }

        public KeyAttestationPackageInfo[] newArray(int size) {
            return new KeyAttestationPackageInfo[size];
        }
    };
    private final String mPackageName;
    private final Signature[] mPackageSignatures;
    private final int mPackageVersionCode;

    /* synthetic */ KeyAttestationPackageInfo(Parcel source, KeyAttestationPackageInfo -this1) {
        this(source);
    }

    public KeyAttestationPackageInfo(String mPackageName, int mPackageVersionCode, Signature[] mPackageSignatures) {
        this.mPackageName = mPackageName;
        this.mPackageVersionCode = mPackageVersionCode;
        this.mPackageSignatures = mPackageSignatures;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public int getPackageVersionCode() {
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
        dest.writeInt(this.mPackageVersionCode);
        dest.writeTypedArray(this.mPackageSignatures, flags);
    }

    private KeyAttestationPackageInfo(Parcel source) {
        this.mPackageName = source.readString();
        this.mPackageVersionCode = source.readInt();
        this.mPackageSignatures = (Signature[]) source.createTypedArray(Signature.CREATOR);
    }
}
