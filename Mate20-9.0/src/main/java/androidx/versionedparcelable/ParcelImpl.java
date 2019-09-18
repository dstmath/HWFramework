package androidx.versionedparcelable;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY})
public class ParcelImpl implements Parcelable {
    public static final Parcelable.Creator<ParcelImpl> CREATOR = new Parcelable.Creator<ParcelImpl>() {
        public ParcelImpl createFromParcel(Parcel in) {
            return new ParcelImpl(in);
        }

        public ParcelImpl[] newArray(int size) {
            return new ParcelImpl[size];
        }
    };
    private final VersionedParcelable mParcel;

    public ParcelImpl(VersionedParcelable parcel) {
        this.mParcel = parcel;
    }

    protected ParcelImpl(Parcel in) {
        this.mParcel = new VersionedParcelParcel(in).readVersionedParcelable();
    }

    public <T extends VersionedParcelable> T getVersionedParcel() {
        return this.mParcel;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        new VersionedParcelParcel(dest).writeVersionedParcelable(this.mParcel);
    }
}
