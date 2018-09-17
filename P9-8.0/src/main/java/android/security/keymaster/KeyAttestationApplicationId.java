package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class KeyAttestationApplicationId implements Parcelable {
    public static final Creator<KeyAttestationApplicationId> CREATOR = new Creator<KeyAttestationApplicationId>() {
        public KeyAttestationApplicationId createFromParcel(Parcel source) {
            return new KeyAttestationApplicationId(source);
        }

        public KeyAttestationApplicationId[] newArray(int size) {
            return new KeyAttestationApplicationId[size];
        }
    };
    private final KeyAttestationPackageInfo[] mAttestationPackageInfos;

    public KeyAttestationApplicationId(KeyAttestationPackageInfo[] mAttestationPackageInfos) {
        this.mAttestationPackageInfos = mAttestationPackageInfos;
    }

    public KeyAttestationPackageInfo[] getAttestationPackageInfos() {
        return this.mAttestationPackageInfos;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(this.mAttestationPackageInfos, flags);
    }

    KeyAttestationApplicationId(Parcel source) {
        this.mAttestationPackageInfos = (KeyAttestationPackageInfo[]) source.createTypedArray(KeyAttestationPackageInfo.CREATOR);
    }
}
