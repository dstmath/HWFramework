package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;

public class KeyAttestationApplicationId implements Parcelable {
    public static final Parcelable.Creator<KeyAttestationApplicationId> CREATOR = new Parcelable.Creator<KeyAttestationApplicationId>() {
        public KeyAttestationApplicationId createFromParcel(Parcel source) {
            return new KeyAttestationApplicationId(source);
        }

        public KeyAttestationApplicationId[] newArray(int size) {
            return new KeyAttestationApplicationId[size];
        }
    };
    private final KeyAttestationPackageInfo[] mAttestationPackageInfos;

    public KeyAttestationApplicationId(KeyAttestationPackageInfo[] mAttestationPackageInfos2) {
        this.mAttestationPackageInfos = mAttestationPackageInfos2;
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
