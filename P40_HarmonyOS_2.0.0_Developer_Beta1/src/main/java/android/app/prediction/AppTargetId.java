package android.app.prediction;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class AppTargetId implements Parcelable {
    public static final Parcelable.Creator<AppTargetId> CREATOR = new Parcelable.Creator<AppTargetId>() {
        /* class android.app.prediction.AppTargetId.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AppTargetId createFromParcel(Parcel parcel) {
            return new AppTargetId(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AppTargetId[] newArray(int size) {
            return new AppTargetId[size];
        }
    };
    private final String mId;

    @SystemApi
    public AppTargetId(String id) {
        this.mId = id;
    }

    private AppTargetId(Parcel parcel) {
        this.mId = parcel.readString();
    }

    public String getId() {
        return this.mId;
    }

    public boolean equals(Object o) {
        if (!getClass().equals(o != null ? o.getClass() : null)) {
            return false;
        }
        return this.mId.equals(((AppTargetId) o).mId);
    }

    public int hashCode() {
        return this.mId.hashCode();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
    }
}
